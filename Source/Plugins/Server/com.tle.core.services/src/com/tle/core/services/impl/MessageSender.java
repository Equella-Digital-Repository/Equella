package com.tle.core.services.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.tle.core.services.ClusterMessagingService;

@SuppressWarnings("nls")
public class MessageSender
{
	private static final Logger LOGGER = Logger.getLogger(MessageSender.class);

	private String receiverId; // Remote node
	private final BlockingDeque<byte[]> msgQueue = new LinkedBlockingDeque<>();
	private long totalQueueSize;
	private long headOffset = 0;

	public MessageSender(String receiverId)
	{
		this.receiverId = receiverId;
	}

	public void sendMessages(DataOutputStream dos, DataInputStream dis) throws IOException, InterruptedException
	{
		byte[] msg = msgQueue.poll(5, TimeUnit.SECONDS);
		if( msg == null )
		{
			if( LOGGER.isDebugEnabled() )
			{
				LOGGER.debug(MessageFormat.format("Sending keepalive to NODE: {0}", receiverId));
			}
			dos.writeLong(-1);
			dos.flush();
			return;
		}

		boolean processed = false;
		try
		{
			dos.writeLong(headOffset);
			int msgSize = msg.length;
			dos.writeInt(msgSize);
			dos.write(msg);
			dos.flush();

			if( LOGGER.isTraceEnabled() )
			{
				LOGGER.trace(MessageFormat.format("Sending message to NODE: {0}", receiverId));
			}

			dis.readBoolean();

			synchronized( this )
			{
				totalQueueSize -= msgSize;
				headOffset++;
				processed = true;
			}
		}
		finally
		{
			if( !processed )
			{
				msgQueue.addFirst(msg);
			}
		}
	}

	public void checkExpectedOffset(DataInputStream dis) throws IOException
	{
		long expectedOffset = dis.readLong();
		if( LOGGER.isTraceEnabled() )
		{
			LOGGER.trace(MessageFormat.format("Expected offset: {0}, Head offset: {1}", expectedOffset, headOffset));
		}
		if( expectedOffset != -1 && expectedOffset != headOffset )
		{
			LOGGER.warn(MessageFormat.format("NODE: {0} was down for too long. {1} messages have been missed",
				receiverId, (headOffset - expectedOffset)));
		}
	}

	public synchronized void queueMessage(byte[] msg)
	{
		totalQueueSize += msg.length;

		if( msg.length > ClusterMessagingService.MAX_MSG_SIZE )
		{
			throw new MessagingException("Message is too large");
		}

		int droppedMsgs = 0;
		while( totalQueueSize > ClusterMessagingService.MAX_QUEUE_SIZE && !msgQueue.isEmpty() )
		{
			byte[] firstMsg = msgQueue.removeFirst();
			totalQueueSize -= firstMsg.length;
			headOffset++;
			droppedMsgs++;
		}
		if( droppedMsgs > 0 )
		{
			LOGGER.warn("Dropped " + droppedMsgs + " messages from queue for NODE: " + receiverId);
		}

		if( LOGGER.isTraceEnabled() )
		{
			LOGGER.trace(MessageFormat.format("Queueing message of size: {0}, Total queue size: {1}", msg.length,
				totalQueueSize));
		}

		msgQueue.add(msg);
	}
}

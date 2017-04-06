/*
 * Created on Jul 19, 2005
 */
package com.tle.freetext;

import java.util.Collection;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.tle.beans.item.ItemIdKey;
import com.tle.core.guice.Bind;
import com.tle.core.institution.RunAsInstitution;

@Bind
public class IndexerThread extends Thread
{
	private static final Logger LOGGER = Logger.getLogger(IndexerThread.class);

	@Inject
	private RunAsInstitution runAs;

	private BackgroundIndexerImpl background;
	private IndexedItem indexedItem;
	private Collection<IndexingExtension> extensions;

	private long started;

	public IndexerThread()
	{
		setPriority(Thread.MIN_PRIORITY);
	}

	public void setNumber(int number)
	{
		setName("IndexerThread " + number); //$NON-NLS-1$
	}

	@Inject
	public void setFreetextIndex(FreetextIndex freetextIndex)
	{
		extensions = freetextIndex.getIndexingExtensions();
	}

	public synchronized void startIndexing(IndexedItem backDoc)
	{
		this.indexedItem = backDoc;
		started = System.currentTimeMillis();
		if( backDoc.isDeadlineAfterStart() )
		{
			backDoc.setExpectedReturnTime(started + backDoc.getTimeAfterStart());
		}
		notifyAll();
	}

	public synchronized void wakeup()
	{
		notifyAll(); // NOSONAR
	}

	@Override
	public void run()
	{
		while( true )
		{
			if( background.isDead() )
			{
				return;
			}

			if( indexedItem == null ) // NOSONAR
			{
				try
				{
					synchronized( this )
					{
						if( indexedItem == null )
						{
							wait();
						}
					}
				}
				catch( InterruptedException e1 )
				{
					LOGGER.error("Interrupted", e1); //$NON-NLS-1$
				}
			}
			else
			{
				final ItemIdKey key = indexedItem.getItemIdKey();

				try
				{
					runAs.executeAsSystem(indexedItem.getInstitution(), new Callable<Void>()
					{
						@Override
						public Void call() throws Exception
						{
							synchronized( indexedItem )
							{
								for( IndexingExtension indexer : extensions )
								{
									indexer.indexFast(indexedItem);
								}
								indexedItem.notifyAll();
								indexedItem.setFinishedFastIndexing(true);
							}
							for( IndexingExtension indexer : extensions )
							{
								indexer.indexSlow(indexedItem);
							}
							background.addIndexedDoc(indexedItem);
							return null;
						}
					});
				}
				catch( Throwable e )
				{
					LOGGER.error("Error indexing: " + key, e); //$NON-NLS-1$
					indexedItem.setError(e);
					background.addErroredDoc(indexedItem);
				}

				indexedItem = null;
				background.threadFinished(this);
			}
		}
	}

	public long getStarted()
	{
		return started;
	}

	public ItemIdKey getItemId()
	{
		if( indexedItem != null )
		{
			return indexedItem.getItemIdKey();
		}
		else
		{
			return null;
		}
	}

	public IndexedItem getIndexedItem()
	{
		return indexedItem;
	}

	public void setBackground(BackgroundIndexerImpl background)
	{
		this.background = background;
	}

	public void setExtensions(Collection<IndexingExtension> extensions)
	{
		this.extensions = extensions;
	}
}

package com.tle.core.harvester.old;

import java.io.IOException;
import java.io.OutputStream;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.dytech.devlib.Base64;

/**
 * @author Nicholas Read
 */
public class TLFSaxDownloader extends DefaultHandler
{
	private static final int MAX_BUFFER_LENGTH = 2048;
	private static final int MIN_ASCII_CHARACTER = 32;

	private OutputStream out;
	private Base64 encoder;
	private boolean downloading;
	private StringBuffer buffer;
	private int length;
	private boolean error;

	public TLFSaxDownloader(OutputStream out)
	{
		this.out = out;
		encoder = new Base64();
		downloading = false;
		error = false;
		buffer = new StringBuffer();
		length = 0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
	 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String namespaceURI, String sName, String qName, Attributes attrs)
	{
		if( qName.equals("Data") ) //$NON-NLS-1$
		{
			downloading = true;
		}
		else if( qName.equals("faultstring") ) //$NON-NLS-1$
		{
			error = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String namespaceURI, String sName, String qName) throws SAXException
	{
		try
		{
			if( qName.equals("Data") ) //$NON-NLS-1$
			{
				decodeBase64();
				downloading = false;
			}
		}
		catch( Exception e )
		{
			throw new SAXException("IO", e); //$NON-NLS-1$
		}

		if( qName.equals("faultstring") ) //$NON-NLS-1$
		{
			error = false;
			throw new SAXException(getErrorMessage());
		}
	}

	private String getErrorMessage()
	{
		return buffer.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(char[] buf, int offset, int len) throws SAXException
	{
		try
		{
			if( downloading || error )
			{
				while( len > 0 )
				{
					char ch = buf[offset];
					buffer.append(ch);
					if( ch >= MIN_ASCII_CHARACTER )
					{
						length++;
						if( length == MAX_BUFFER_LENGTH )
						{
							if( !error )
							{
								decodeBase64();
							}
						}
					}
					offset++;
					len--;
				}
			}
		}
		catch( Exception e )
		{
			throw new SAXException("IO", e); //$NON-NLS-1$
		}
	}

	/**
	 * Decodes the contents of the buffer field.
	 * 
	 * @throws IOException if the content is not proper Base64 encoded.
	 */
	private void decodeBase64() throws IOException
	{
		byte[] bytes = encoder.decode(buffer.toString());
		out.write(bytes);

		buffer.setLength(0);
		length = 0;
	}
}

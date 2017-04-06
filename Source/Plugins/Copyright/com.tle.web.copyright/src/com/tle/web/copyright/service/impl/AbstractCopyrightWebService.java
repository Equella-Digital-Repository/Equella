package com.tle.web.copyright.service.impl;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.dytech.devlib.PropBagEx;
import com.google.common.io.CharStreams;
import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.core.copyright.Holding;
import com.tle.core.copyright.Portion;
import com.tle.core.copyright.Section;
import com.tle.core.copyright.service.CopyrightService;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.XsltService;
import com.tle.web.copyright.service.CopyrightWebService;
import com.tle.web.sections.SectionInfo;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public abstract class AbstractCopyrightWebService<H extends Holding> implements CopyrightWebService<H>
{
	private static final Logger LOGGER = Logger.getLogger(CopyrightWebService.class);

	private static final String ATTACHMENT_MAP = "COPYRIGHT_ATTACH_MAP";
	private static final String ACTIVATIONS_MAP = "COPYRIGHT_ACTIVATIONS_MAP";
	private static final String HOLDING = "COPYRIGHT_HOLDING";

	protected CopyrightService<H, ? extends Portion, ? extends Section> copyrightService;

	@Inject
	private XsltService xsltService;
	@Inject
	private FileSystemService fileSystemService;

	@PostConstruct
	void setupCopyrightService()
	{
		copyrightService = getCopyrightServiceImpl();
	}

	protected abstract CopyrightService<H, ? extends Portion, ? extends Section> getCopyrightServiceImpl();

	@Override
	public Map<String, Attachment> getAttachmentMap(SectionInfo info, Item item)
	{
		Map<Long, Map<String, Attachment>> attachmentMaps = info.getAttribute(ATTACHMENT_MAP);
		if( attachmentMaps == null )
		{
			attachmentMaps = new HashMap<Long, Map<String, Attachment>>();
			info.setAttribute(ATTACHMENT_MAP, attachmentMaps);
		}
		long itemId = item.getId();
		Map<String, Attachment> attachMap = attachmentMaps.get(itemId);
		if( attachMap == null )
		{
			attachMap = UnmodifiableAttachments.convertToMapUuid(item.getAttachmentsUnmodifiable());
			attachmentMaps.put(itemId, attachMap);
		}
		return attachMap;
	}

	@Override
	public int getStatus(SectionInfo info, Item item, String attachmentUuid)
	{
		String key = getAttachmentKey(attachmentUuid, item);
		Integer status = getActivations(info, item).get(key);
		if( status == null )
		{
			return ActivateRequest.TYPE_INACTIVE;
		}
		return status;
	}

	private String getAttachmentKey(String attachmentUuid, Item item)
	{
		return attachmentUuid + ':' + item.getId();
	}

	private Map<String, Integer> getActivations(SectionInfo info, Item item)
	{
		Map<String, Integer> activations = info.getAttribute(ACTIVATIONS_MAP);
		if( activations == null )
		{
			List<ActivateRequest> activationRequests = copyrightService.getCurrentOrPendingActivations(getHolding(info,
				item));
			activations = new HashMap<String, Integer>();
			for( ActivateRequest req : activationRequests )
			{
				String key = getAttachmentKey(req.getAttachment(), req.getItem());
				Integer status = activations.get(key);
				if( status == null || status == ActivateRequest.TYPE_PENDING )
				{
					activations.put(key, req.getStatus());
				}
			}
			info.setAttribute(ACTIVATIONS_MAP, activations);
		}
		return activations;
	}

	@SuppressWarnings("unchecked")
	@Override
	public H getHolding(SectionInfo info, Item item)
	{
		H holding = (H) info.getAttribute(HOLDING);
		if( holding == null )
		{
			holding = copyrightService.getHoldingForItem(item);
			info.setAttribute(HOLDING, holding);
		}
		return holding;
	}

	@Override
	public String getAgreement(FileHandle agreementFile)
	{
		if( agreementFile == null )
		{
			return null;
		}

		String filename = fileSystemService.getFileInfo(agreementFile, null).getFilename();
		if( filename.endsWith(".xsl") || filename.endsWith(".xslt") )
		{
			PropBagEx xml = new PropBagEx();
			xml.setNode("tempdir", "../../");
			xml.setNode("template", "../../0/0/cal/");

			return xsltService.transform(agreementFile, null, xml, true);
		}
		else
		{
			try (Reader r = new InputStreamReader(fileSystemService.read(agreementFile, null)))
			{
				return CharStreams.toString(r);
			}
			catch( Exception e )
			{
				LOGGER.error("Error getting license agreement file: '" + filename, e);
			}
		}
		return "ERROR creating agreement";
	}
}

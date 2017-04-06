package com.tle.core.hierarchy.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Transactional;

import com.google.inject.assistedinject.Assisted;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.hierarchy.ExportedHierarchyNode;
import com.tle.beans.hierarchy.HierarchyTopic;
import com.tle.beans.item.Item;
import com.tle.common.URLUtils;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.filesystem.StagingFile;
import com.tle.core.hierarchy.HierarchyDao;
import com.tle.core.initialiser.InitialiserCallback;
import com.tle.core.initialiser.Property;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.InitialiserService;
import com.tle.core.services.StagingService;
import com.tle.core.services.UrlService;
import com.tle.core.services.impl.SingleShotTask;
import com.tle.core.user.UserState;

@SuppressWarnings("nls")
public class ExportTask extends SingleShotTask
{
	private final UserState userState;
	private final long exportId;
	private final boolean withSecurity;

	@Inject
	private HierarchyServiceImpl hierarchyService;
	@Inject
	private HierarchyDao dao;
	@Inject
	private RunAsInstitution runAs;
	@Inject
	private InitialiserService initialiserService;
	@Inject
	private TLEAclManager aclManager;
	@Inject
	private StagingService stagingService;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private UrlService urlService;

	@Inject
	public ExportTask(@Assisted UserState userState, @Assisted long exportId, @Assisted boolean withSecurity)
	{
		this.userState = userState;
		this.exportId = exportId;
		this.withSecurity = withSecurity;
	}

	@Override
	public Priority getPriority()
	{
		return Priority.INTERACTIVE;
	}

	@Override
	public void runTask() throws Exception
	{
		runAs.execute(userState, new Callable<Void>()
		{
			@Override
			public Void call() throws Exception
			{
				doExport();
				return null;
			}
		});
	}

	@Transactional
	void doExport() throws IOException
	{
		final HierarchyTopic topic = hierarchyService.getHierarchyTopic(exportId);

		setupStatus(null, dao.countSubnodesForNode(topic));

		ExportedHierarchyNode nodes = populateExport(topic);
		nodes = initialiserService.initialise(nodes, new InitialiserCallback()
		{
			@Override
			public void set(Object obj, Property property, Object value)
			{
				if( value instanceof BaseEntity )
				{
					BaseEntity toset = (BaseEntity) value;
					toset.setUuid(((BaseEntity) property.get(obj)).getUuid());
				}
				property.set(obj, value);
			}

			@Override
			public void entitySimplified(Object old, Object newObj)
			{
				if( old instanceof BaseEntity )
				{
					BaseEntity toset = (BaseEntity) newObj;
					BaseEntity oldObj = (BaseEntity) old;
					toset.setUuid(oldObj.getUuid());
				}
				if( old instanceof Item )
				{
					Item item = (Item) old;
					Item newItem = (Item) newObj;
					newItem.setUuid(item.getUuid());
					newItem.setVersion(item.getVersion());
				}
			}
		});

		StagingFile staging = stagingService.createStagingArea();
		fileSystemService.write(staging, "topic.xml", new StringReader(hierarchyService.getXStream().toXML(nodes)),
			false);

		// Feels so dirty... Should probably be a method on
		// StagingService to get a URL. We do this exact same thing in
		// ServerBackendImpl for the file manager.
		addLogEntry(URLUtils.newURL(urlService.getInstitutionUrl(),
			"file/" + staging.getUuid() + "/$/" + URLUtils.urlEncode("topic.xml")));
	}

	private ExportedHierarchyNode populateExport(HierarchyTopic topic)
	{
		ExportedHierarchyNode node = new ExportedHierarchyNode();
		node.setTopic(topic);

		if( withSecurity )
		{
			node.setTargetList(aclManager.getTargetList(Node.HIERARCHY_TOPIC, topic));
		}

		incrementWork();

		List<ExportedHierarchyNode> childNodes = new ArrayList<ExportedHierarchyNode>();
		for( HierarchyTopic childTopic : hierarchyService.getChildTopics(topic) )
		{
			childNodes.add(populateExport(childTopic));
		}
		node.setChildren(childNodes);

		return node;
	}

	@Override
	protected String getTitleKey()
	{
		return null;
	}
}

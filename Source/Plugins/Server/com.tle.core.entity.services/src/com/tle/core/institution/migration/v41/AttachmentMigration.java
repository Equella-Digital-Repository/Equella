package com.tle.core.institution.migration.v41;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.annotations.AccessType;
import org.hibernate.classic.Session;

import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
public class AttachmentMigration extends AbstractHibernateSchemaMigration
{
	private static final int BATCH_SIZE = 1000;
	private static final String TABLE_NAME = "attachment"; //$NON-NLS-1$
	private static final String[] NEW_COLUMNS = new String[]{"md5sum", "item_id", "attindex"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(AttachmentMigration.class) + "."; //$NON-NLS-1$
	private static final Log LOGGER = LogFactory.getLog(AttachmentMigration.class);

	@SuppressWarnings("nls")
	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "attachment.schema.title", keyPrefix + "attachment.schema.description");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeItem.class, FakeAttachment.class, FakeAttachmentMapping.class, ItemAttachmentsPK.class};
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return false;
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		List<String> sql = new ArrayList<String>();
		sql.addAll(helper.getAddColumnsSQL(TABLE_NAME, NEW_COLUMNS));
		return sql;
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		List<String> dropModify = new ArrayList<String>();
		dropModify.addAll(helper.getAddNotNullSQL(TABLE_NAME, "item_id", "attindex")); //$NON-NLS-1$ //$NON-NLS-2$
		dropModify.addAll(helper.getAddIndexesAndConstraintsForColumns(TABLE_NAME, NEW_COLUMNS));
		dropModify.addAll(helper.getDropTableSql("item_attachments")); //$NON-NLS-1$
		return dropModify;
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 1;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		// get the attachments and create a md5sum for all of them
		Query query = session
			.createQuery("from ItemAttachments i inner join fetch i.attachments inner join fetch i.item"); //$NON-NLS-1$
		List<FakeAttachmentMapping> list = query.list();
		session.clear();
		int i = 0;
		for( final FakeAttachmentMapping itemAttach : list )
		{
			final FakeAttachment attachment = itemAttach.attachments;
			FakeItem item = itemAttach.item;

			attachment.attindex = itemAttach.id.attindex;
			attachment.item = item;
			if( Check.isEmpty(attachment.uuid) )
			{
				attachment.uuid = UUID.randomUUID().toString();
			}
			session.update(attachment);
			i++;
			if( i % BATCH_SIZE == 0 )
			{
				session.flush();
				session.clear();
			}
			result.incrementStatus();
		}
		session.flush();
		session.clear();

		int orphans = session.createQuery("delete from Attachment where item is null").executeUpdate(); //$NON-NLS-1$
		if( orphans > 0 )
		{
			LOGGER.warn("Found " + orphans + " orphaned attachments"); //$NON-NLS-1$//$NON-NLS-2$
		}
	}

	@Entity(name = "Item")
	@AccessType("field")
	public static class FakeItem
	{
		@Id
		long id;
		String uuid;
		int version;
	}

	@Entity(name = "Attachment")
	@AccessType("field")
	public static class FakeAttachment
	{
		@Id
		long id;
		@Column(updatable = false)
		String type;
		@Column(length = 40)
		String uuid;
		@Column(length = 32)
		String md5sum;
		@Column(updatable = false)
		String url;
		@ManyToOne
		FakeItem item;
		Integer attindex;
	}

	@Entity(name = "ItemAttachments")
	public static class FakeAttachmentMapping
	{
		@EmbeddedId
		ItemAttachmentsPK id;
		@ManyToOne
		@JoinColumn(insertable = false, updatable = false)
		FakeItem item;
		@OneToOne
		FakeAttachment attachments;
	}

	@Embeddable
	public static class ItemAttachmentsPK implements Serializable
	{
		private static final long serialVersionUID = 1L;

		@Column(name = "item_id")
		long itemId;
		int attindex;
	}
}

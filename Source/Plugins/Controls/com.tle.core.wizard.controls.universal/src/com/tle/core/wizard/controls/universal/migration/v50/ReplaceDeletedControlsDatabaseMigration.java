package com.tle.core.wizard.controls.universal.migration.v50;

import java.util.List;

import javax.inject.Singleton;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.classic.Session;
import org.w3c.dom.Node;

import com.dytech.edge.common.Constants;
import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;
import com.tle.beans.Institution;
import com.tle.common.util.XmlDocument;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.institution.XmlHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;

/**
 * @author Aaron
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class ReplaceDeletedControlsDatabaseMigration extends AbstractHibernateDataMigration
{
	@Inject
	private XmlHelper xmlHelper;

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "FROM ItemDefinition");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		final List<FakeItemDefinition> itemDefs = session.createQuery("FROM ItemDefinition").list();
		final XStream x = ReplaceDeletedControlsXmlMigration.createXStream(xmlHelper);
		for( FakeItemDefinition itemDef : itemDefs )
		{
			final FakeItemdefBlobs blob = itemDef.getSlow();
			final String wizXml = blob.getWizard();

			final XmlDocument xml = new XmlDocument(wizXml);
			final Node wizardNode = xml.node("//slow/wizard");

			if( ReplaceDeletedControlsXmlMigration.replaceAllObsoleteControls(xml, wizardNode, x) )
			{
				blob.setWizard(xml.toString());
				session.save(blob);
			}
			result.incrementStatus();
		}
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{Institution.class, FakeItemDefinition.class, FakeBaseEntity.class, FakeItemdefBlobs.class};
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.wizard.controls.universal.migration.replacecontrols.title",
			Constants.BLANK);
	}

	@Entity(name = "ItemDefinition")
	@AccessType("field")
	public static class FakeItemDefinition extends FakeBaseEntity
	{
		@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
		@Index(name = "collectionBlobs")
		private FakeItemdefBlobs slow;

		public FakeItemdefBlobs getSlow()
		{
			return slow;
		}

		public void setSlow(FakeItemdefBlobs slow)
		{
			this.slow = slow;
		}
	}

	@Entity(name = "BaseEntity")
	@AccessType("field")
	@Inheritance(strategy = InheritanceType.JOINED)
	public static class FakeBaseEntity
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		private long id;

		@JoinColumn(nullable = false)
		@ManyToOne(fetch = FetchType.LAZY)
		@Index(name = "institutionIndex")
		private Institution institution;

		public Institution getInstitution()
		{
			return institution;
		}

		public void setInstitution(Institution institution)
		{
			this.institution = institution;
		}

		public long getId()
		{
			return id;
		}

		public void setId(long id)
		{
			this.id = id;
		}
	}

	@Entity(name = "ItemdefBlobs")
	@AccessType("field")
	public static class FakeItemdefBlobs
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		private long id;

		private String wizard;

		public long getId()
		{
			return id;
		}

		public void setId(long id)
		{
			this.id = id;
		}

		public String getWizard()
		{
			return wizard;
		}

		public void setWizard(String wizard)
		{
			this.wizard = wizard;
		}
	}
}

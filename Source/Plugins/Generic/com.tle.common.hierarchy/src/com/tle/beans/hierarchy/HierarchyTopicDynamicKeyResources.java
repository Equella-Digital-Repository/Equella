package com.tle.beans.hierarchy;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.beans.Institution;

@Entity
@AccessType("field")
public class HierarchyTopicDynamicKeyResources
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@JoinColumn(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	@Index(name = "key_resource_institution")
	@XStreamOmitField
	private Institution institution;

	@Index(name = "dynamic_hierarchy_id")
	@Column(length = 1024)
	private String dynamicHierarchyId;

	@Index(name = "key_resource_item_uuid")
	@Column(length = 40)
	private String uuid;

	@Index(name = "key_resource_item_version")
	private int version;

	@Column(nullable = false)
	private Date dateCreated;

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public Institution getInstitution()
	{
		return institution;
	}

	public void setInstitution(Institution institution)
	{
		this.institution = institution;
	}

	public String getDynamicHierarchyId()
	{
		return dynamicHierarchyId;
	}

	public void setDynamicHierarchyId(String dynamicHierarchyId)
	{
		this.dynamicHierarchyId = dynamicHierarchyId;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	public Date getDateCreated()
	{
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated)
	{
		this.dateCreated = dateCreated;
	}
}

package com.tle.common.workflow;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.beans.IdCloneable;

@Entity
@AccessType("field")
public class WorkflowMessage implements Serializable, IdCloneable
{
	public static final char TYPE_COMMENT = 'c';
	public static final char TYPE_REJECT = 'r';
	public static final char TYPE_ACCEPT = 'a';
	public static final char TYPE_SUBMIT = 's';

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private char type;
	@Column(nullable = false)
	private Date date;

	@ManyToOne
	@JoinColumn(insertable = false, updatable = false, nullable = false)
	@XStreamOmitField
	@Index(name = "messagenode_idx")
	private WorkflowNodeStatus node;
	@Lob
	@Column(nullable = false)
	private String message;
	@Column(length = 255, nullable = false)
	private String user;

	@Override
	public long getId()
	{
		return id;
	}

	@Override
	public void setId(long id)
	{
		this.id = id;
	}

	public WorkflowNodeStatus getNode()
	{
		return node;
	}

	public void setNode(WorkflowNodeStatus node)
	{
		this.node = node;
	}

	public char getType()
	{
		return type;
	}

	public void setType(char type)
	{
		this.type = type;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public Date getDate()
	{
		return date;
	}

	public void setDate(Date date)
	{
		this.date = date;
	}

	public String getUser()
	{
		return user;
	}

	public void setUser(String user)
	{
		this.user = user;
	}
}

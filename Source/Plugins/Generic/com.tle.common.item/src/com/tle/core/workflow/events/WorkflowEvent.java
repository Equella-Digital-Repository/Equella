/*
 * Created on Jun 28, 2004
 */
package com.tle.core.workflow.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.HistoryEvent.Type;
import com.tle.beans.item.Item;

public abstract class WorkflowEvent implements Serializable
{
	private static final long serialVersionUID = 1L;
	protected HistoryEvent event;
	protected String userid;
	protected Date date;
	protected String stepName;
	protected String step;
	protected String tostep;
	protected String toStepName;

	public WorkflowEvent()
	{
		super();
	}

	public WorkflowEvent(HistoryEvent event)
	{
		this.event = event;
		date = event.getDate();
		userid = event.getUser();
		tostep = event.getToStep();
		step = event.getStep();
		stepName = event.getStepName();
		toStepName = event.getToStepName();
	}

	public static WorkflowEvent getEvent(HistoryEvent hevent)
	{
		WorkflowEvent event;
		switch( hevent.getType() )
		{
			case statechange:
				event = new StateChangeEvent(hevent);
				break;
			case approved:
				event = new ApprovedEvent(hevent);
				break;
			case resetworkflow:
				event = new ResetEvent(hevent);
				break;
			case rejected:
				event = new RejectedEvent(hevent);
				break;
			case edit:
				event = new EditEvent(hevent);
				break;
			case promoted:
				event = new PromotionEvent(hevent);
				break;
			case comment:
				event = new CommentEvent(hevent);
				break;
			case assign:
				event = new AssignEvent(hevent);
				break;
			case clone:
				event = new CloneEvent(hevent);
				break;
			case changeCollection:
				event = new MoveEvent(hevent);
				break;
			case newversion:
				event = new NewVersionEvent(hevent);
				break;
			case contributed:
				event = new ContributedEvent(hevent);
				break;
			default:
				// should probably throw an exception really?
				event = null;
		}
		return event;
	}

	public static WorkflowEvent[] getAllEvents(Item xml)
	{
		Collection<WorkflowEvent> events = new ArrayList<WorkflowEvent>();

		for( HistoryEvent event : xml.getHistory() )
		{
			events.add(getEvent(event));
		}

		return events.toArray(new WorkflowEvent[events.size()]);
	}

	public final Type getIntType()
	{
		return event.getType();
	}

	public abstract String getIcon();

	public Date getDate()
	{
		return date;
	}

	public void setDate(Date date)
	{
		this.date = date;
	}

	public String getStep()
	{
		return step;
	}

	public void setStep(String step)
	{
		this.step = step;
	}

	public String getUserid()
	{
		return userid;
	}

	public void setUserid(String userid)
	{
		this.userid = userid;
	}

	public String getTostep()
	{
		return tostep;
	}

	public void setTostep(String tostep)
	{
		this.tostep = tostep;
	}

	public String getStepName()
	{
		return stepName;
	}

	public void setStepName(String stepName)
	{
		this.stepName = stepName;
	}

	public String getToStepName()
	{
		return toStepName;
	}

	public void setToStepName(String toStepName)
	{
		this.toStepName = toStepName;
	}

	public String getComment()
	{
		return event.getComment();
	}

	public long getId()
	{
		return event.getId();
	}
}

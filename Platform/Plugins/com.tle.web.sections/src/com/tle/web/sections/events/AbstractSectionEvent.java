package com.tle.web.sections.events;

import java.util.EventListener;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;

public abstract class AbstractSectionEvent<L extends EventListener> implements SectionEvent<L>
{
	private boolean stopProcessing;
	private boolean abortProcessing;

	// Broadcast

	@Override
	public SectionId getForSectionId()
	{
		return null;
	}

	private int priority;

	@Override
	public int getPriority()
	{
		return priority;
	}

	public void setPriority(int priority)
	{
		this.priority = priority;
	}

	@Override
	public String getListenerId()
	{
		return null;
	}

	@Override
	public void beforeFiring(SectionInfo info, SectionTree tree)
	{
		this.stopProcessing = false;
	}

	@Override
	public void finishedFiring(SectionInfo info, SectionTree tree)
	{
		// nothing
	}

	@Override
	public int compareTo(SectionEvent<L> o)
	{
		return o.getPriority() - getPriority();
	}

	@Override
	public boolean isStopProcessing()
	{
		return stopProcessing;
	}

	@Override
	public void stopProcessing()
	{
		this.stopProcessing = true;
	}

	@Override
	public boolean isAbortProcessing()
	{
		return abortProcessing;
	}

	@Override
	public void abortProcessing()
	{
		this.abortProcessing = true;
	}

	@Override
	public boolean isContinueAfterException()
	{
		return false;
	}
}

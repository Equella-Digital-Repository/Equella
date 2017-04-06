package com.tle.cla.web.viewitem.summary;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.cla.CLAHolding;
import com.tle.beans.cla.CLAPortion;
import com.tle.beans.cla.CLASection;
import com.tle.cla.CLAConstants;
import com.tle.cla.web.service.CLAWebServiceImpl;
import com.tle.core.activation.validation.PageCounter;
import com.tle.core.copyright.Holding;
import com.tle.core.copyright.Portion;
import com.tle.core.copyright.service.CopyrightService;
import com.tle.core.guice.Bind;
import com.tle.web.copyright.section.AbstractActivateSection;
import com.tle.web.copyright.section.AbstractCopyrightSummarySection;
import com.tle.web.copyright.service.CopyrightWebService;

@NonNullByDefault
@Bind
public class CLASummarySection extends AbstractCopyrightSummarySection<CLAHolding, CLAPortion, CLASection>
{
	@Inject
	private CLAWebServiceImpl claWebService;

	@Override
	protected Class<? extends AbstractActivateSection> getActivateSectionClass()
	{
		return CLAActivateSection.class;
	}

	@Override
	protected HoldingDisplay createHoldingDisplay(Holding holding)
	{
		HoldingDisplay holdingDisplay = new HoldingDisplay();
		boolean book = holding.getType().equalsIgnoreCase(CLAConstants.BOOK);
		if( book )
		{
			holdingDisplay.setBook(true);
		}
		holdingDisplay.setShowPages(true);
		holdingDisplay.setTotalPages(PageCounter.countTotalPages(holding.getLength()));
		return holdingDisplay;
	}

	@Override
	protected String getChapterName(HoldingDisplay holdingDisplay, Portion portion)
	{
		return portion.getChapter();
	}

	@Override
	protected String getPortionId(HoldingDisplay holdingDisplay, Portion portion)
	{
		String chapter = portion.getChapter();
		if( chapter == null )
		{
			return Long.toString(portion.getId());
		}
		return chapter;
	}

	@Override
	protected CopyrightWebService<CLAHolding> getCopyrightWebServiceImpl()
	{
		return claWebService;
	}

	@Override
	protected CopyrightService<CLAHolding, CLAPortion, CLASection> getCopyrightServiceImpl()
	{
		return claWebService.getCopyrightServiceImpl();
	}

	@Override
	protected void processAvailablePages(Holding holding, HoldingDisplay holdingDisplay)
	{
		double percent = CLAConstants.PERCENTAGE;
		int totalPages = holdingDisplay.getTotalPages();

		int activatablePages = (int) (holding.getType() == CLAConstants.TYPE_STORIES ? CLAConstants.MAX_STORY_PAGES
			: (percent / 100) * totalPages);

		int activePages = (int) ((holdingDisplay.getTotalActivePercent() / 100) * totalPages);
		int pagesLeft = activatablePages - activePages;

		int totalInactive = (int) ((holdingDisplay.getTotalInactivePercent() / 100) * totalPages);

		holdingDisplay.setPagesAvailable(pagesLeft > totalInactive ? totalInactive : pagesLeft);
	}
}

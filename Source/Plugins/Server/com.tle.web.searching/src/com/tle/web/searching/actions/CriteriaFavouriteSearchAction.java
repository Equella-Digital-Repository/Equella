package com.tle.web.searching.actions;

import javax.inject.Inject;

import com.tle.web.search.actions.AbstractFavouriteSearchAction;
import com.tle.web.sections.equella.dialog.EquellaDialog;
import com.tle.web.sections.standard.annotations.Component;

public class CriteriaFavouriteSearchAction extends AbstractFavouriteSearchAction
{
	@Inject
	@Component(name = "fd")
	private CriteriaFavouriteSearchDialog dialog;

	@Override
	protected EquellaDialog<?> getDialog()
	{
		return dialog;
	}
}

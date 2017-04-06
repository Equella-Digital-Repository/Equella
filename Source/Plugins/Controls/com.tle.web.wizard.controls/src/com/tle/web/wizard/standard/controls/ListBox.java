/*
 * Created on Jun 21, 2004 For "The Learning Edge"
 */
package com.tle.web.wizard.standard.controls;

import com.tle.core.guice.Bind;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSAssignable;
import com.tle.web.sections.js.generic.function.AnonymousFunction;
import com.tle.web.sections.js.generic.function.AssignableFunction;
import com.tle.web.sections.js.generic.statement.ReturnStatement;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.wizard.controls.CListBox;
import com.tle.web.wizard.controls.Item;
import com.tle.web.wizard.controls.SimpleValueControl;

/**
 * @author jmaginnis
 */
@Bind
public class ListBox extends AbstractOptionControl implements SimpleValueControl
{
	private CListBox box;
	@Component(stateful = false)
	private SingleSelectionList<Item> list;

	@SuppressWarnings("nls")
	@Override
	protected void prepareList(OptionsModel listModel)
	{
		if( box.isExpertSearch() )
		{
			listModel.setTopOption(new KeyOption<Item>("wizard.controls.listbox.all", "", null));
		}
		else if( box.isShowSelection() )
		{
			listModel.setTopOption(new KeyOption<Item>("wizard.controls.listbox.select", "", null));
		}
	}

	@Override
	public void setWrappedControl(HTMLControl control)
	{
		this.box = (CListBox) control;
		super.setWrappedControl(control);
	}

	@Override
	protected String getTemplate()
	{
		return "listbox.ftl"; //$NON-NLS-1$
	}

	@Override
	public SingleSelectionList<Item> getList()
	{
		return list;
	}

	@Override
	public JSAssignable createEditFunction()
	{
		return AssignableFunction.get(list.createSetFunction());
	}

	@Override
	public JSAssignable createResetFunction()
	{
		return AssignableFunction.get(list.createResetFunction());
	}

	@Override
	public JSAssignable createTextFunction()
	{
		return new AnonymousFunction(new ReturnStatement(list.createGetNameExpression()));
	}

	@Override
	public JSAssignable createValueFunction()
	{
		return new AnonymousFunction(new ReturnStatement(list.createGetExpression()));
	}

	@Override
	protected ElementId getIdForLabel()
	{
		return list;
	}

}

package com.tle.web.sections.equella.render;

import java.util.Set;

import com.tle.common.Check;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.jquery.libraries.JQueryJqtransform;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlListState;
import com.tle.web.sections.standard.renderers.list.DropDownRenderer;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class JqtransformDropDownRenderer extends DropDownRenderer
{
	private static final String KEY_OPENER_CLASS = "selectOpenerClass";
	private static final String KEY_WRAPPER_CLASS = "wrapperClass";

	public JqtransformDropDownRenderer(HtmlListState state)
	{
		super(state);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		final ObjectExpression params = new ObjectExpression();
		final HtmlComponentState selectState = getHtmlState();

		String openerClass = selectState.getAttribute(KEY_OPENER_CLASS);
		if( openerClass != null )
		{
			params.put(KEY_OPENER_CLASS, openerClass);
		}

		boolean hasClass = false;

		// inherit any classes put on the SELECT element
		final Set<String> classes = selectState.getStyleClasses();
		final StringBuilder classString = new StringBuilder();
		if( !Check.isEmpty(classes) )
		{
			for( String clas : classes )
			{
				if( hasClass )
				{
					classString.append(' ');
				}
				classString.append(clas);
				hasClass = true;
			}
		}
		final String wrapperClass = selectState.getAttribute(KEY_WRAPPER_CLASS);
		if( !Check.isEmpty(wrapperClass) )
		{
			if( hasClass )
			{
				classString.append(' ');
			}
			classString.append(wrapperClass);
			hasClass = true;
		}
		if( hasClass )
		{
			params.put(KEY_WRAPPER_CLASS, classString.toString());
		}

		info.addReadyStatements(JQueryJqtransform.setupJqtransform(Jq.$(this), params));
		super.preRender(info);
	}
}

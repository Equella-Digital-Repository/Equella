package com.tle.web.sections.standard.js.impl;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.JSUtils;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.function.DynamicNamedFunction;
import com.tle.web.sections.js.generic.function.RuntimeFunction;
import com.tle.web.sections.js.generic.statement.ReturnStatement;
import com.tle.web.sections.standard.js.DelayedRenderer;

@NonNullByDefault
public abstract class DelayedFunction<T> extends RuntimeFunction
{
	private final DelayedRenderer<T> delayedRenderer;
	private final ElementId id;
	private final String prefix;
	private final int numParams;

	public DelayedFunction(DelayedRenderer<T> delayedRenderer, String prefix, ElementId id, int numParams)
	{
		this.delayedRenderer = delayedRenderer;
		this.prefix = prefix;
		this.id = id;
		this.numParams = numParams;
	}

	@Override
	protected JSCallable createFunction(RenderContext info)
	{
		T renderer = delayedRenderer.getSelectedRenderer(info);
		if( renderer != null )
		{
			return createRealFunction(info, renderer);
		}
		return new DynamicNamedFunction(prefix, id, JSUtils.createParameters(numParams))
		{
			@SuppressWarnings("nls")
			@Override
			public JSStatements createFunctionBody(RenderContext context, JSExpression[] params)
			{
				T renderer = delayedRenderer.getSelectedRenderer(context);
				if( renderer == null )
				{
					throw new SectionsRuntimeException("Trying to use function '" + prefix + id.getElementId(context)
						+ "' but failed to render component with id:" + id.getElementId(context));
				}
				JSCallable callable = createRealFunction(context, renderer);
				return new ReturnStatement(new FunctionCallExpression(callable, (Object[]) params));
			}
		};
	}

	protected abstract JSCallable createRealFunction(RenderContext info, T renderer);
}

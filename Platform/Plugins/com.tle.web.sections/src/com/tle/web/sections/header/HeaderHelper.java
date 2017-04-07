package com.tle.web.sections.header;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.render.PreRenderable;

/**
 * This interface is an abstraction of the header of a HTML page.
 * <p>
 * It is the responsibility of the "root" {@code SectionTree} to provide an
 * implementation of this interface and actually do the rendering of the header.
 * <p>
 * Typically {@link PreRenderable#preRender(PreRenderContext)} methods are
 * responsible for interacting with this interface, as they are guaranteed to be
 * called only once per page render.
 * <p>
 * You should never rely on the result of any of the JavaScript abstraction
 * calls to be a particular implementation, the examples given in each method
 * are just to give you an idea of the equivalent.
 * 
 * @author jmaginnis
 */
public interface HeaderHelper
{
	/**
	 * Get an expression that will return the page's global form.
	 * <p>
	 * Equivalent to: <code>document.forms['theform']</code>
	 * 
	 * @return The form expression
	 */
	JSExpression getFormExpression();

	/**
	 * Return a function that looks a HTML DOM element up by id.
	 * <p>
	 * Equivalent to: <code>document.getElementById($param1)</code>
	 * 
	 * @return The getElementById function
	 */
	JSCallable getElementFunction();

	JSCallAndReference getTriggerEventFunction();

	// Lack of Platform branches fail
	JSCallable getSubmitFunction(boolean validate, boolean event);

	JSCallable getSubmitFunction(boolean validate, boolean event, boolean blockFurtherSubmission);

}

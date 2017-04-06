package com.tle.web.oauth.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.dytech.edge.exceptions.WebException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.oauth.OAuthConstants;
import com.tle.web.oauth.OAuthException;
import com.tle.web.oauth.OAuthWebConstants;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public abstract class AbstractOAuthServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(AbstractOAuthServlet.class);

	protected static final String PREFIX = "com.tle.web.oauth.";

	protected ObjectMapper mapper;

	@Override
	public void init() throws ServletException
	{
		mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		super.init();
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
		IOException
	{
		try
		{
			doService(request, response);
		}
		catch( WebException e )
		{
			respondWithError(request, response, e);
		}
		catch( Exception t )
		{
			LOGGER.error(t.getMessage(), t);
			respondWithError(request, response, 500, OAuthConstants.ERROR_SERVER_ERROR, t);
		}
	}

	protected abstract void doService(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException, WebException;

	protected void respondWithError(HttpServletRequest request, HttpServletResponse response, WebException e)
		throws IOException, ServletException
	{
		LOGGER.error(e.getMessage(), e);
		respondWithError(request, response, e.getCode(), e.getError(), e);
	}

	protected abstract void respondWithError(HttpServletRequest request, HttpServletResponse response, int code,
		String error, Throwable t) throws IOException, ServletException;

	protected String getParameter(HttpServletRequest request, String paramName, boolean mandatory) throws WebException
	{
		final String paramValue = request.getParameter(paramName);
		if( mandatory && Check.isEmpty(paramValue) )
		{
			final OAuthException oauthEx = new OAuthException(400, OAuthConstants.ERROR_INVALID_REQUEST,
				CurrentLocale.get(PREFIX + "oauth.error.parammandatory", paramName));
			if( paramName.equals(OAuthWebConstants.PARAM_CLIENT_ID)
				|| paramName.equals(OAuthWebConstants.PARAM_REDIRECT_URI) )
			{
				oauthEx.setBadClientOrRedirectUri(true);
			}
			throw oauthEx;
		}
		return paramValue;
	}
}

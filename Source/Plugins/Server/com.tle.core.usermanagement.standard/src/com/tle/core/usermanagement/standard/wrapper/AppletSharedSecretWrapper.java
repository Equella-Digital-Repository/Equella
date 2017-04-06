package com.tle.core.usermanagement.standard.wrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.dytech.edge.common.Constants;
import com.tle.beans.ump.UserManagementSettings;
import com.tle.beans.usermanagement.standard.wrapper.AppletSharedSecretSettings;
import com.tle.beans.usermanagement.standard.wrapper.AppletSharedSecretSettings.AppletSharedSecretValue;
import com.tle.common.util.TokenGenerator;
import com.tle.common.util.TokenSecurity.Token;
import com.tle.core.guice.Bind;
import com.tle.core.user.CurrentInstitution;

@Bind
public class AppletSharedSecretWrapper extends AbstractSharedSecretWrapper<AppletSharedSecretValue>
{
	private static final Logger LOGGER = Logger.getLogger(AppletSharedSecretWrapper.class);

	@Override
	protected boolean initialise(UserManagementSettings settings)
	{
		// Applet tokens don't make sense unless we're in an institution, so
		// just don't bother initialising anything.
		if( CurrentInstitution.get() == null )
		{
			return false;
		}

		super.initialise(settings);

		// Ssettings may not have a list of secrets yet if this is the first
		// time this method has been called on a fresh institution.

		if( !secrets.isEmpty() )
		{
			return false;
		}

		AppletSharedSecretValue value = new AppletSharedSecretValue();
		value.setSecret(UUID.randomUUID().toString());

		List<AppletSharedSecretValue> values = new ArrayList<AppletSharedSecretValue>();
		values.add(value);

		AppletSharedSecretSettings asss = (AppletSharedSecretSettings) settings;
		asss.setSharedSecrets(values);

		// Chuck the secret in our own map for now to make things work.
		secrets.put(Constants.APPLET_SECRET_ID, value);

		return true;
	}

	@Override
	public String getGeneratedToken(String secretId, String username)
	{
		AppletSharedSecretValue secret = secrets.get(secretId);
		if( secret == null )
		{
			return null;
		}

		try
		{
			return TokenGenerator.createSecureToken(username, secretId, secret.getSecret(), username);
		}
		catch( Exception e )
		{
			LOGGER.error("Error generating applet token.");
			throw new RuntimeException(e);
		}
	}

	@Override
	protected boolean isAcceptableToken(Token token)
	{
		if( token != null && token.getId().equals(Constants.APPLET_SECRET_ID) )
		{
			return super.isAcceptableToken(token);
		}
		return false;
	}

	@Override
	protected String getUsername(AppletSharedSecretValue value, Token token)
	{
		return token.getInsecure();
	}

	@Override
	protected boolean isAutoCreate(AppletSharedSecretValue value)
	{
		return false;
	}

	@Override
	public List<String> getTokenSecretIds()
	{
		// We do not want to list the Applet shared secret ID, as it is
		// internal.
		return null;
	}

	@Override
	protected boolean isIgnoreNonExistantUser(AppletSharedSecretValue value)
	{
		return false;
	}

	@Override
	public boolean isAuditable()
	{
		return false;
	}
}

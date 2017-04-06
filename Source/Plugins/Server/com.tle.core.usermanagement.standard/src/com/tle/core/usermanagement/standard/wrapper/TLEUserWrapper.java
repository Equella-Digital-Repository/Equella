/*
 * Created on Mar 16, 2005
 */
package com.tle.core.usermanagement.standard.wrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;

import com.dytech.edge.common.valuebean.DefaultUserBean;
import com.dytech.edge.common.valuebean.UserBean;
import com.google.common.collect.Maps;
import com.tle.beans.ump.UserManagementSettings;
import com.tle.beans.user.TLEUser;
import com.tle.common.Pair;
import com.tle.core.guice.Bind;
import com.tle.core.services.user.TLEUserService;
import com.tle.core.user.DefaultUserState;
import com.tle.core.user.ModifiableUserState;
import com.tle.plugins.ump.AbstractUserDirectory;

@Bind
public class TLEUserWrapper extends AbstractUserDirectory
{
	@Inject
	private TLEUserService tleUserService;

	@Override
	protected boolean initialise(UserManagementSettings settings)
	{
		return false;
	}

	@Override
	public ModifiableUserState authenticateUser(final String username, final String password)
	{
		TLEUser user = tleUserService.getByUsername(username);
		if( user != null && tleUserService.checkPasswordMatch(user, password) )
		{
			return createState(user);
		}
		return null;
	}

	private ModifiableUserState createState(TLEUser user)
	{
		DefaultUserState state = new DefaultUserState();
		state.setLoggedInUser(convert(user));
		state.setInternal(true);
		return state;
	}

	private UserBean convert(TLEUser user)
	{
		if( user == null )
		{
			return null;
		}
		else
		{
			return new DefaultUserBean(user.getUuid(), user.getUsername(), user.getFirstName(), user.getLastName(),
				user.getEmailAddress());
		}
	}

	@Override
	public UserBean getInformationForUser(final String userID)
	{
		return convert(tleUserService.get(userID));
	}

	@Override
	public Map<String, UserBean> getInformationForUsers(final Collection<String> userIds)
	{
		Map<String, UserBean> rv = Maps.newHashMapWithExpectedSize(userIds.size());
		for( TLEUser tleu : tleUserService.getInformationForUsers(userIds) )
		{
			UserBean ub = convert(tleu);
			rv.put(ub.getUniqueID(), ub);
		}
		return rv;
	}

	@Override
	public ModifiableUserState authenticateUserFromUsername(final String username, String privateData)
	{
		TLEUser user = tleUserService.getByUsername(username);
		if( user != null )
		{
			return createState(user);
		}
		return null;
	}

	@Override
	public Pair<ChainResult, Collection<UserBean>> searchUsers(String query)
	{
		Collection<UserBean> users = new ArrayList<UserBean>();
		for( TLEUser user : tleUserService.searchUsers(query, null, false) )
		{
			users.add(new DefaultUserBean(user.getUuid(), user.getUsername(), user.getFirstName(), user.getLastName(),
				user.getEmailAddress()));
		}
		return new Pair<ChainResult, Collection<UserBean>>(ChainResult.CONTINUE, users);
	}
}

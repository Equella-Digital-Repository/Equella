package com.tle.core.services.user.impl;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.dytech.edge.common.valuebean.RoleBean;
import com.dytech.edge.common.valuebean.UserBean;
import com.tle.beans.Institution;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.user.UserService;
import com.tle.core.user.AbstractUserState;

/**
 * @author Nicholas Read
 */
public class RunAsUserState extends AbstractUserState
{
	private static final long serialVersionUID = 1L;
	private final TLEAclManager aclManager;
	private final UserService userService;
	private boolean doneAcls;
	private boolean doneGroups;
	private boolean doneRoles;
	private boolean system;

	public RunAsUserState(UserBean bean, Institution institution, TLEAclManager aclManager, UserService userService,
		boolean systemUser)
	{
		this.userService = userService;
		this.aclManager = aclManager;
		this.system = systemUser;
		setLoggedInUser(bean);
		setAuthenticated(true);
		setInstitution(institution);
		setSessionID(UUID.randomUUID().toString());
	}

	@Override
	public Set<String> getUsersRoles()
	{
		if( !doneRoles )
		{
			Set<String> roles = super.getUsersRoles();
			List<RoleBean> rolesForUser = userService.getRolesForUser(getUserBean().getUniqueID());
			for( RoleBean rolebean : rolesForUser )
			{
				roles.add(rolebean.getUniqueID());
			}
			doneRoles = true;
		}
		return super.getUsersRoles();
	}

	@Override
	public Set<String> getUsersGroups()
	{
		if( !doneGroups )
		{
			Set<String> usersGroups = super.getUsersGroups();
			usersGroups.addAll(userService.getGroupIdsContainingUser(getUserBean().getUniqueID()));
			doneGroups = true;
		}
		return super.getUsersGroups();
	}

	@Override
	public Collection<Long> getCommonAclExpressions()
	{
		ensureAcls();
		return super.getCommonAclExpressions();
	}

	@Override
	public Collection<Long> getNotOwnerAclExpressions()
	{
		ensureAcls();
		return super.getNotOwnerAclExpressions();
	}

	@Override
	public Collection<Long> getOwnerAclExpressions()
	{
		ensureAcls();
		return super.getOwnerAclExpressions();
	}

	@Override
	public boolean isSystem()
	{
		return system;
	}

	private void ensureAcls()
	{
		if( !doneAcls )
		{
			doneAcls = true;
			setAclExpressions(aclManager.getAclExpressions(this));
		}
	}
}

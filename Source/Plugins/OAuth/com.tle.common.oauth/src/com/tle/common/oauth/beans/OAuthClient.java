package com.tle.common.oauth.beans;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.tle.beans.entity.BaseEntity;

/**
 * @author aholland
 */
@Entity
@AccessType("field")
// TODO: how do you do this when it's across two tables????
// @Table(uniqueConstraints = {@UniqueConstraint(columnNames =
// {"institution_id", "clientId"})})
public final class OAuthClient extends BaseEntity
{
	private static final long serialVersionUID = 1L;

	@Index(name = "oauthClientIdIndex")
	@Column(nullable = false, length = 100)
	private String clientId;

	@Column(nullable = false, length = 100)
	private String clientSecret;

	@Index(name = "oauthRedirectURLIndex")
	@Column(nullable = false, length = 1024)
	private String redirectUrl;

	/**
	 * The set of permissions that the OAuthClient is allowed to
	 * <em>request</em>. Currently, apps can do anything
	 */
	@ElementCollection(fetch = FetchType.EAGER)
	private Set<String> permissions;

	/**
	 * Client can optionally login as a certain fixed user
	 */
	@Column(length = 255)
	private String userId;

	/**
	 * Currently, no clients require user approval
	 */
	@Column(nullable = false)
	private boolean requiresApproval;

	public String getClientId()
	{
		return clientId;
	}

	public void setClientId(String clientId)
	{
		this.clientId = clientId;
	}

	public String getClientSecret()
	{
		return clientSecret;
	}

	public void setClientSecret(String clientSecret)
	{
		this.clientSecret = clientSecret;
	}

	public String getRedirectUrl()
	{
		return redirectUrl;
	}

	public void setRedirectUrl(String redirectUrl)
	{
		this.redirectUrl = redirectUrl;
	}

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public Set<String> getPermissions()
	{
		return permissions;
	}

	public void setPermissions(Set<String> permissions)
	{
		this.permissions = permissions;
	}

	public boolean isRequiresApproval()
	{
		return requiresApproval;
	}

	public void setRequiresApproval(boolean requiresApproval)
	{
		this.requiresApproval = requiresApproval;
	}
}

package com.tle.web.oauth.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TokenResponse
{
	private String accessToken;
	private String refreshToken;
	private String tokenType;
	private long expiresIn;
	private String scope;
	private String state;

	@JsonProperty("access_token")
	public String getAccessToken()
	{
		return accessToken;
	}

	public void setAccessToken(String accessToken)
	{
		this.accessToken = accessToken;
	}

	@JsonProperty("refresh_token")
	public String getRefreshToken()
	{
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken)
	{
		this.refreshToken = refreshToken;
	}

	@JsonProperty("token_type")
	public String getTokenType()
	{
		return tokenType;
	}

	public void setTokenType(String tokenType)
	{
		this.tokenType = tokenType;
	}

	@JsonProperty("expires_in")
	public long getExpiresIn()
	{
		return expiresIn;
	}

	public void setExpiresIn(long expiresIn)
	{
		this.expiresIn = expiresIn;
	}

	public String getScope()
	{
		return scope;
	}

	public void setScope(String scope)
	{
		this.scope = scope;
	}

	public String getState()
	{
		return state;
	}

	public void setState(String state)
	{
		this.state = state;
	}
}
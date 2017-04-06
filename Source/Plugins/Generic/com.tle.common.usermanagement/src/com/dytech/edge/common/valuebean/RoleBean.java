/*
 * Created on Feb 17, 2005
 */
package com.dytech.edge.common.valuebean;

import java.io.Serializable;

/**
 * @author Nicholas Read
 */
public interface RoleBean extends Serializable
{
	/**
	 * @return a unique, unchanging ID for the role
	 */
	String getUniqueID();

	/**
	 * @return a name for the role
	 */
	String getName();
}

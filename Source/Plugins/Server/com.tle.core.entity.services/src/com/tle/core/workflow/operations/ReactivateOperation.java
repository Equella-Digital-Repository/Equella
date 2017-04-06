/*
 * Created on Jul 12, 2004 For "The Learning Edge"
 */
package com.tle.core.workflow.operations;

import com.tle.beans.item.ItemStatus;
import com.tle.common.security.SecurityConstants;
import com.tle.core.security.impl.SecureItemStatus;
import com.tle.core.security.impl.SecureOnCall;

@SecureOnCall(priv = SecurityConstants.ARCHIVE_ITEM)
@SecureItemStatus(ItemStatus.ARCHIVED)
public class ReactivateOperation extends AbstractWorkflowOperation
{
	@Override
	public boolean execute()
	{
		setState(ItemStatus.LIVE);
		getItem().setModerating(getModerationStatus().isUnarchiveModerating());
		restoreTasksForItem();
		return true;
	}
}

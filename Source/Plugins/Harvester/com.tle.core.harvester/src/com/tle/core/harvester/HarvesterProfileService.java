package com.tle.core.harvester;

import java.util.Date;

import com.tle.common.harvester.HarvesterProfile;
import com.tle.common.harvester.RemoteHarvesterProfileService;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.core.services.entity.EntityEditingBean;

public interface HarvesterProfileService
	extends
		AbstractEntityService<EntityEditingBean, HarvesterProfile>,
		RemoteHarvesterProfileService
{
	void updateLastRun(HarvesterProfile profile, Date lastRun);
}

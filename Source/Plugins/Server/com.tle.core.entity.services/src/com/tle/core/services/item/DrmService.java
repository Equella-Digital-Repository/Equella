package com.tle.core.services.item;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.dytech.edge.wizard.beans.DRMPage;
import com.tle.beans.item.DrmAcceptance;
import com.tle.beans.item.DrmSettings;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.common.Pair;

/**
 * @author Nicholas Read
 */
public interface DrmService
{
	DrmAcceptance getAgreement(String userID, Item item);

	Pair<Long, List<DrmAcceptance>> enumerateAgreements(Item item, int limit, int offset, boolean sortByName,
		Date startDate, Date endDate);

	List<DrmAcceptance> enumerateAgreements(Item item);

	boolean requiresAcceptanceCheck(ItemKey key, boolean isSummaryPage, boolean viewedInComposition);

	DrmSettings requiresAcceptance(Item item, boolean isSummaryPage, boolean viewedInComposition);

	boolean hasAcceptedOrRequiresNoAcceptance(Item item, boolean isSummaryPage, boolean viewedInComposition);

	void acceptLicense(Item item);

	void revokeAcceptance(Item item, String userID);

	void revokeAllItemAcceptances(Item item);

	void isAuthorised(Item item, String ipaddress);

	void mergeSettings(DrmSettings settings, DRMPage page);

	boolean havePreviewedThisSession(ItemKey itemId);

	void addPreviewItem(ItemKey itemId);

	boolean isReferredFromDifferentItem(HttpServletRequest request, ItemKey itemId);

	boolean isReferredFromSamePackage(HttpServletRequest request, ItemKey itemId);

}

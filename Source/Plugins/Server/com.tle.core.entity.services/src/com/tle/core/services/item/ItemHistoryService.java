package com.tle.core.services.item;

import java.util.List;

import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.ItemKey;

public interface ItemHistoryService
{
	List<HistoryEvent> getHistory(ItemKey itemId);
}

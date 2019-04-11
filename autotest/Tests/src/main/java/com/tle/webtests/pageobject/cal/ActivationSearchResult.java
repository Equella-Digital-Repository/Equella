package com.tle.webtests.pageobject.cal;

import com.tle.webtests.pageobject.searching.AbstractItemSearchResult;
import com.tle.webtests.pageobject.searching.AbstractResultList;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

public class ActivationSearchResult extends AbstractItemSearchResult<ActivationSearchResult> {

  protected ActivationSearchResult(AbstractResultList<?, ?> page, SearchContext relativeTo, By by) {
    super(page, relativeTo, by);
  }

  public boolean isActive() {
    return getDetailText("Status").equals("Active");
  }
}

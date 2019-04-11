package com.tle.webtests.pageobject.generic.component;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class StringSelectedStuff extends AbstractSelectedStuff<String, StringSelectedStuff> {
  public StringSelectedStuff(PageContext context, By parentElement) {
    super(context, parentElement);
  }

  @Override
  protected String getSelection(WebElement we) {
    return we.getText().trim();
  }

  @Override
  protected String getAdditionalNameXpathConstraint(String selection) {
    return "[normalize-space(text()) = " + quoteXPath(selection) + "]";
  }
}

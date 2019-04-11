package com.tle.webtests.test.reporting;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractReport;
import org.openqa.selenium.By;

public class ItemCountReportPage extends AbstractReport<ItemCountReportPage> {
  public ItemCountReportPage(PageContext context) {
    super(context, By.xpath("//h1/b[text()='Total Items In Basic Item Collection']"));
  }

  public String getReportResult() {
    return driver.findElement(By.xpath("//span[contains(@class,'style_9')]")).getText();
  }
}

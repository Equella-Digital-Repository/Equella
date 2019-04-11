package com.tle.webtests.pageobject.portal;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;

public class RssPortalSection extends AbstractPortalSection<RssPortalSection> {
  public RssPortalSection(PageContext context, String title) {
    super(context, title);
  }

  public int countEntries() {
    return getBoxContent().findElements(By.xpath(".//div[@class='news-item']")).size();
  }

  public String getTitle(int i) {
    return getBoxContent()
        .findElement(By.xpath(".//div[@class='news-item'][" + String.valueOf(i) + "]/h4/a"))
        .getText();
  }

  public String getDescription(int i) {
    return getBoxContent()
        .findElement(By.xpath(".//div[@class='news-item'][" + String.valueOf(i) + "]/p"))
        .getText();
  }

  public boolean itemExists(String title) {
    return isPresent(
        getBoxContent(),
        By.xpath(".//div[@class='news-item']/h4/a[text()=" + quoteXPath(title) + "]"));
  }
}

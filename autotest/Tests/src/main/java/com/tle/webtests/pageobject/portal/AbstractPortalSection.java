package com.tle.webtests.pageobject.portal;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.HomePage;
import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

public abstract class AbstractPortalSection<T extends AbstractPortalSection<T>>
    extends AbstractPage<T> {
  protected String title;

  public AbstractPortalSection(PageContext context, String title) {
    super(context);
    this.title = title;
  }

  protected WebElement getBoxHead() {
    return find(
        driver,
        By.xpath(
            "//h3[normalize-space(text())="
                + quoteXPath(getTitle())
                + "]/ancestor::div[contains(@class, 'box_head')][1]"));
  }

  protected By getBoxContentBy() {
    return By.xpath(
        "//h3[normalize-space(text())="
            + quoteXPath(getTitle())
            + "]/ancestor::div[contains(@class, 'box_head')][1]/following-sibling::div[contains(@class, 'box_content')]/div");
  }

  protected WebElement getBoxContent() {
    return find(driver, getBoxContentBy());
  }

  protected WebElement findLoadedElement() {
    return getBoxHead();
  }

  public HomePage delete() {
    ExpectedCondition<Boolean> removalCondition = removalCondition(getBoxHead());
    showButtons();
    getBoxHead().findElement(By.className("box_close")).click();
    acceptConfirmation();
    waiter.until(removalCondition);
    return new HomePage(context).get();
  }

  public T minMax() {
    boolean present = isPresent(driver, getBoxContentBy());
    WaitingPageObject<T> aWaiter;
    if (present) {
      aWaiter = removalWaiter(getBoxContent());
    } else {
      aWaiter = visibilityWaiter(driver, getBoxContentBy());
    }

    getBoxHead().findElement(By.className("box_minimise")).click();
    return aWaiter.get();
  }

  public boolean isMinimisable() {
    return isPresent(getBoxHead(), By.className("box_minimise"));
  }

  public boolean isCloseable() {
    return isPresent(getBoxHead(), By.className("box_close"));
  }

  public <P extends AbstractPortalEditPage<P>> P edit(P portal) {
    WebElement boxEditButton = getBoxHead().findElement(By.className("box_edit"));
    // 'showButtons' not working very well on Travis when the New UI is enable
    // So retry three times at most
    int retry = 3;
    while (!boxEditButton.isDisplayed() && retry > 0) {
      showButtons();
      retry--;
    }
    boxEditButton.click();
    return portal.get();
  }

  private void showButtons() {
    // hover doesn't work correctly so just force the buttons to show
    ((JavascriptExecutor) driver).executeScript("$('img.action').show();");
  }

  public String getTitle() {
    return title;
  }
}

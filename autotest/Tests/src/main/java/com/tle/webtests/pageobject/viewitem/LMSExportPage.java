package com.tle.webtests.pageobject.viewitem;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ErrorPage;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.ReceiptPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LMSExportPage extends AbstractPage<LMSExportPage> {
  private WebElement getConnector() {
    return byBaseId("_cl");
  }

  private WebElement getPublishButton() {
    return byBaseId("_publishButton");
  }

  private WebElement getSelectSummary() {
    return byBaseId("_ss");
  }

  private WebElement getSelectPackage() {
    return byBaseId("_sp");
  }

  private WebElement getShowArchived() {
    return byBaseId("_sa");
  }

  private WebElement getFilterBox() {
    return byBaseId("_fb");
  }

  private WebElement getCourseTree() {
    return byBaseId("_folderTree");
  }

  @FindBy(id = "lms-tree-ajax")
  private WebElement treeAjax;

  @FindBy(id = "lms-tree-container")
  private WebElement treeContainerAjax;

  @FindBy(xpath = "id('lms-tree-ajax')/p[@class='error']")
  private WebElement connectorError;

  private String baseid;

  public LMSExportPage(PageContext context) {
    this(context, "lmse");
  }

  public LMSExportPage(PageContext context, String baseid) {
    super(context, By.id("lmsexport"));
    this.baseid = baseid;
  }

  public WebElement byBaseId(String postfix) {
    return driver.findElement(By.id(baseid + postfix));
  }

  public LMSExportPage selectConnector(PrefixedName name) {
    WaitingPageObject<LMSExportPage> waiter = ajaxUpdateExpect(treeAjax, getCourseTree());
    new EquellaSelect(context, getConnector()).selectByVisibleText(name.toString());
    return waiter.get();
  }

  public LMSExportPage selectConnectorError(PrefixedName name) {
    WaitingPageObject<LMSExportPage> waiter = ajaxUpdateExpect(treeAjax, connectorError);
    new EquellaSelect(context, getConnector()).selectByVisibleText(name.toString());
    return waiter.get();
  }

  public LMSCourseNode clickCourse(String course) {
    LMSCourseNode courseNode = new LMSCourseNode(course, getCourseTree()).get();
    return courseNode.open();
  }

  public LMSExportPage selectSummary() {
    getSelectSummary().click();
    return get();
  }

  public LMSExportPage selectPackage() {
    getSelectPackage().click();
    return get();
  }

  public LMSExportPage selectAttachment(String attachmentName, boolean courseUpdate) {
    WaitingPageObject<LMSExportPage> waiter = ajaxUpdateExpect(treeAjax, getCourseTree());
    driver
        .findElement(By.xpath("//li/div/a[text()=" + quoteXPath(attachmentName) + "]/../../input"))
        .click();
    return courseUpdate ? waiter.get() : this.get();
  }

  public boolean hasCourse(String course) {
    return new LMSCourseNode(course, getCourseTree()).isLoaded();
  }

  public LMSExportPage publish() {
    getPublishButton().click();
    return ReceiptPage.waiterContains("ublished", new LMSExportPage(context)).get();
  }

  public ErrorPage publishWithError() {
    getPublishButton().click();
    return errorPage();
  }

  public boolean singleConnector() {
    return !isPresent(getConnector());
  }

  public SummaryPage summary() {
    driver.findElement(By.xpath("id('breadcrumbs')//a[2]")).click();
    return new SummaryPage(context).get();
  }

  public FindUsesPage findUsesPage() {
    clickCommand("Find uses");
    return new FindUsesPage(context).get();
  }

  private void clickCommand(String command) {
    driver
        .findElement(By.xpath("//a[normalize-space(text())=" + quoteXPath(command) + "]"))
        .click();
  }

  public LMSExportPage showArchived(boolean on) {
    if (on != getShowArchived().isSelected()) {
      WaitingPageObject<LMSExportPage> ajaxUpdate =
          ajaxUpdateExpect(treeContainerAjax, getCourseTree());
      getShowArchived().click();
      return ajaxUpdate.get();
    }
    return this;
  }

  public LMSExportPage filterCourses(String query, String change, boolean appear) {
    LMSCourseNode course = new LMSCourseNode(change, getCourseTree());
    WaitingPageObject<?> waiter;
    if (appear) {
      waiter = course;
    } else {
      waiter =
          ExpectWaiter.waiter(
              ExpectedConditions2.invisibilityOf(course.get().getLoadedElement()), this);
    }
    getFilterBox().clear();
    getFilterBox().sendKeys(query);
    getFilterBox().sendKeys(Keys.ENTER);
    waiter.get();
    return this;
  }

  public boolean hasSummaryCheckbox() {
    return isVisible(getSelectSummary());
  }

  public String getError() {
    return connectorError.getText();
  }

  public abstract class LMSNode<T extends LMSNode<T>> extends AbstractPage<T> {
    @FindBy(xpath = "a[contains(@class, 'hitarea')]")
    private WebElement opener;

    @FindBy(xpath = "ul")
    private WebElement sectionsElem;

    @FindBy(xpath = "ul/li[span/div[@class='lineItem']]")
    private WebElement aSectionElem;

    public LMSNode(PageContext context, SearchContext searchContext, By loadedBy) {
      super(context, searchContext, loadedBy);
    }

    public T open() {
      WaitingPageObject<T> visibilityWaiter = visibilityWaiter(aSectionElem);
      opener.click();
      return visibilityWaiter.get();
    }

    @Override
    public SearchContext getSearchContext() {
      return loadedElement;
    }

    public void clickSection(String section) {
      WebElement sectionRow =
          sectionsElem.findElement(
              By.xpath(
                  "li[span/div[@class='lineItem']/span[normalize-space(text())="
                      + quoteXPath(section)
                      + "]]"));
      WebElement checkBox = sectionRow.findElement(By.xpath("./span/div/input"));
      checkBox.click();
    }
  }

  public class LMSCourseNode extends LMSNode<LMSCourseNode> {
    public LMSCourseNode(String courseName, SearchContext searchContext) {
      super(
          LMSExportPage.this.context,
          searchContext,
          By.xpath(
              "//li[span/div/span[@class='course' and text()=" + quoteXPath(courseName) + "]]"));
    }

    public LMSFolderNode clickFolder(String folder) {
      LMSFolderNode folderNode = new LMSFolderNode(folder, this).get();
      return folderNode.open();
    }
  }

  public class LMSFolderNode extends LMSNode<LMSFolderNode> {
    public LMSFolderNode(String sectionName, LMSCourseNode courseNode) {
      super(
          LMSExportPage.this.context,
          courseNode.getLoadedElement(),
          By.xpath(
              "//li[span/div/span[@class='folder' and text()=" + quoteXPath(sectionName) + "]]"));
    }
  }

  public boolean attachmentsSelectable() {
    return isPresent(By.xpath("//ul/li/input[not(@disabled = 'disabled')]"));
  }
}

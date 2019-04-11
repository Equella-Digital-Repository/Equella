package com.tle.webtests.pageobject.remoterepo.srw;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.remoterepo.RemoteRepoViewResultPage;
import org.openqa.selenium.By;

public class RemoteRepoViewSRWResultPage
    extends RemoteRepoViewResultPage<RemoteRepoViewSRWResultPage> {
  public RemoteRepoViewSRWResultPage(PageContext context) {
    super(context, By.xpath("//button[contains(@id, '_importButton')]"));
  }

  public RemoteRepoSRWSearchPage clickRemoteRepoBreadcrumb(String repo) {
    driver.findElement(By.xpath("id('breadcrumbs')//a[text()='" + repo + "']")).click();
    return new RemoteRepoSRWSearchPage(context);
  }
}

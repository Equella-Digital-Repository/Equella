package com.tle.webtests.pageobject.integration.moodle;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;

public class MoodleModuleDownloadPage extends AbstractPage<MoodleModuleDownloadPage> {
  private final String moodleBaseUrl;

  public MoodleModuleDownloadPage(PageContext context, String moodleBaseUrl) {
    super(context, By.id("success"), 60);
    this.moodleBaseUrl = moodleBaseUrl;
  }

  @Override
  protected void loadUrl() {
    driver.get(moodleBaseUrl + "module.php");
  }
}

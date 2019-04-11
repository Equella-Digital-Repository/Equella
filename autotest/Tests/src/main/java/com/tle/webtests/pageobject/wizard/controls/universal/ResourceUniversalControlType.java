package com.tle.webtests.pageobject.wizard.controls.universal;

import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.selection.SelectionSession;
import com.tle.webtests.pageobject.wizard.controls.NewAbstractWizardControl;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class ResourceUniversalControlType
    extends NewAbstractWizardControl<ResourceUniversalControlType>
    implements AttachmentType<ResourceUniversalControlType, GenericAttachmentEditPage> {

  private WebElement getDisplayName() {
    return byWizId("_dialog_rh_displayName");
  }

  private UniversalControl universalControl;

  public ResourceUniversalControlType(UniversalControl control) {
    super(control.getContext(), control.getCtrlNum(), control.getPage(), By.id("selectresource"));
    this.universalControl = control;
  }

  @Override
  public String getType() {
    return "openEQUELLA Resource";
  }

  public SelectionSession getSelectionSession() {
    return ExpectWaiter.waiter(
            ExpectedConditions.frameToBeAvailableAndSwitchToIt("selectresource"),
            new SelectionSession(context))
        .get();
  }

  public WaitingPageObject<GenericAttachmentEditPage> editPage() {
    return new GenericAttachmentEditPage(universalControl) {

      @Override
      protected WebElement getNameField() {
        return getDisplayName();
      }

      @Override
      protected WebElement getPreviewCheckbox() {
        return null;
      }
    };
  }

  @Override
  public GenericAttachmentEditPage edit() {
    return editPage().get();
  }

  public String getFrameName() {
    return "selectresource";
  }
}

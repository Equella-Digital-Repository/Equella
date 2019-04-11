package equellatests.instgen

import java.util.UUID

import com.tle.webtests.framework.PageContext
import com.tle.webtests.pageobject.viewitem.SummaryPage
import com.tle.webtests.pageobject.wizard.controls.{EditBoxControl, UniversalControl}
import com.tle.webtests.pageobject.wizard.{ContributePage, WizardPageTab, WizardUrlPage}
import equellatests.GlobalConfig
import equellatests.domain.{TestInst, TestLogon}
import org.scalacheck.{Arbitrary, Gen}

package object fiveo {

  val fiveoInst: TestInst = GlobalConfig.createTestInst("fiveo")

  val autoTestLogon = TestLogon("AutoTest", "automated", fiveoInst)

}

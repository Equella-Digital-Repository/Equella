package equellatests.sections.search

import org.openqa.selenium.{By, WebElement}

trait QuerySection extends ResultsUpdatable {

  def queryField: WebElement = findElement(By.name("q"))

  def query: String = queryField.getAttribute("value")

  def query_=(q: String): Unit = {
    queryField.clear()
    queryField.sendKeys(q)
  }

  def searchButton: WebElement = findElement(By.id("searchform-search"))

  def search(): this.type = {
    val expect = resultsUpdateExpectation
    searchButton.click()
    waitFor(expect)
    this
  }

}

package integration;

import java.util.List;
import java.util.logging.Level;

import com.codeborne.selenide.ex.JavaScriptErrorsFound;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.assertNoJavascriptErrors;
import static com.codeborne.selenide.Selenide.getJavascriptErrors;
import static com.codeborne.selenide.Selenide.getWebDriverLogs;
import static com.codeborne.selenide.WebDriverRunner.isChrome;
import static com.codeborne.selenide.WebDriverRunner.isFirefox;
import static com.codeborne.selenide.WebDriverRunner.isHtmlUnit;
import static com.codeborne.selenide.WebDriverRunner.isPhantomjs;
import static com.codeborne.selenide.WebDriverRunner.isSafari;
import static org.openqa.selenium.logging.LogType.BROWSER;

class BrowserLogsTest extends IntegrationTest {
  @BeforeEach
  void setUp() {
    getWebDriverLogs(BROWSER); // clear logs
    openFile("page_with_js_errors.html");
  }

  @Test
  void canCheckJavaScriptErrors() {
    Assumptions.assumeFalse(isFirefox() || isChrome());  // window.onerror does not work in Firefox for unknown reason :(

    assertNoJavascriptErrors();
    $(byText("Generate JS Error")).click();

    assertThat(getJavascriptErrors())
      .hasSize(1);

    String jsError = getJavascriptErrors().get(0);
    assertThat(jsError)
      .contains("ReferenceError")
      .contains("$")
      .contains("/page_with_js_errors.html");
  }

  @Test
  void canAssertNoJavaScriptErrors() {
    Assumptions.assumeFalse(isFirefox());  // window.onerror does not work in Firefox for unknown reason :(
    $(byText("Generate JS Error")).click();
    try {
      assertNoJavascriptErrors();
      fail("Expected JavaScriptErrorsFound");
    } catch (JavaScriptErrorsFound expected) {
      assertThat(getJavascriptErrors())
        .hasSize(1);
      assertThat(expected.getJsErrors().get(0))
        .contains("ReferenceError");
    }
  }

  @Test
  void canGetWebDriverBrowserConsoleLogEntry() {
    $(byText("Generate JS Error")).click();
    List<String> webDriverLogs = getWebDriverLogs(BROWSER, Level.ALL);

    Assumptions.assumeFalse(isHtmlUnit() || isPhantomjs() || isFirefox() || isSafari() || isChrome());

    assertThat(webDriverLogs)
      .hasSize(1);

    String logEntry = webDriverLogs.get(0);
    assertThat(logEntry)
      .contains("ReferenceError")
      .contains("$")
      .contains("/page_with_js_errors.html");
  }
}

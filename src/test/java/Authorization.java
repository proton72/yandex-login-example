import com.codeborne.selenide.WebDriverRunner;
import org.apache.http.cookie.Cookie;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.sleep;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by vdenisov on 14/12/2016.
 */

public class Authorization {

    public static final String MONEY_YANDEX_URL = "http://money.yandex.ru/";
    public static final String LOGIN = "login";
    public static final String PASSWORD = "password";
    private static final String DOMAIN = ".yandex.ru";

    @Test
    public void authorizeInYandex() {
        List<Cookie> yandexProfile = AuthorizationUtil.authorizeAccount(LOGIN, PASSWORD);
        assertThat("Не удалось получить требуемые cookie, проверьте корректность аккаунта",
                yandexProfile.stream().anyMatch((cookie) -> cookie.getName().equals("L")),
                is(true));
        List<org.openqa.selenium.Cookie> seleniumCookiesMoney = new ArrayList<org.openqa.selenium.Cookie>();
        for (Cookie apacheCookie : yandexProfile) {
            org.openqa.selenium.Cookie cookieConfig = new org.openqa.selenium.Cookie(apacheCookie.getName(),
                    apacheCookie.getValue(), DOMAIN, apacheCookie.getPath(),
                    apacheCookie.getExpiryDate(), false, false);
            seleniumCookiesMoney.add(cookieConfig);

        }
        System.setProperty("webdriver.chrome.driver", "build/chromedriver/win/chromedriver.exe");
        System.setProperty("selenide.browser", "CHROME");
        open(MONEY_YANDEX_URL);
        seleniumCookiesMoney.forEach((cookie) -> WebDriverRunner.getWebDriver().manage().addCookie(cookie));
        open(MONEY_YANDEX_URL);
        sleep(100);
    }
}
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created by vdenisov on 14/12/2016.
 */
public class AuthorizationUtil {

    public static List<Cookie> authorizeAccount(String login, String passwd) {

        CookieStore myCookieStore = new BasicCookieStore();
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(myCookieStore);

        HttpUriRequest request = RequestBuilder
                .post()
                .setUri("https://passport.yandex.ru/passport")
                .addParameter("mode", "auth")
                .addParameter("from", "money")
                .addParameter("retpath", "https")
                .addParameter("msg", "money")
                .addParameter("login", login)
                .addParameter("passwd", passwd)
                .addParameter("timestamp", String.valueOf(System.currentTimeMillis()))
                .build();


        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD)
                .build();
        try {
            CloseableHttpClient client = HttpClients.custom()
                    .setDefaultRequestConfig(requestConfig)
                    .setRedirectStrategy(new LaxRedirectStrategy())
                    .setDefaultCookieStore(myCookieStore)
                    .build();
            final CloseableHttpResponse response = client.execute(request, context);

            assertThat(response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));

            InputStream bodyStream = response.getEntity().getContent();
            System.out.printf("Body of the response: %s\n", new BufferedReader(new InputStreamReader(bodyStream))
                    .lines().collect(Collectors.joining("\n")));
            for (Cookie cookie : context.getCookieStore().getCookies())
                System.out.printf("Cookies: %s\n", cookie);
        } catch (Exception e) {
            throw new IllegalStateException("Не удается запросить авторизацию, брошено исключение", e);
        }
        return context.getCookieStore().getCookies();
    }
}
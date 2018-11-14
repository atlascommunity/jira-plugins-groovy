package ru.mail.jira.plugins.groovy.impl.var;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import ru.mail.jira.plugins.groovy.api.dto.docs.ClassDoc;
import ru.mail.jira.plugins.groovy.api.script.binding.BindingDescriptor;

import javax.annotation.Nonnull;

public class HttpClientBindingDescriptor implements BindingDescriptor<CloseableHttpClient> {
    private static final int HTTP_CLIENT_TIMEOUT = 3000;

    private final CloseableHttpClient httpClient;

    public HttpClientBindingDescriptor() {
        PoolingHttpClientConnectionManager httpConnectionManager = new PoolingHttpClientConnectionManager();
        httpConnectionManager.setMaxTotal(50);
        httpConnectionManager.setDefaultMaxPerRoute(4);
        httpConnectionManager.setDefaultSocketConfig(
            SocketConfig
                .custom()
                .setSoKeepAlive(true)
                .setSoTimeout(HTTP_CLIENT_TIMEOUT)
                .build()
        );

        httpClient = HttpClients
            .custom()
            .setDefaultRequestConfig(
                RequestConfig
                    .custom()
                    .setConnectionRequestTimeout(HTTP_CLIENT_TIMEOUT)
                    .setConnectTimeout(HTTP_CLIENT_TIMEOUT)
                    .setSocketTimeout(HTTP_CLIENT_TIMEOUT)
                    .setCookieSpec(CookieSpecs.STANDARD)
                    .build()
            )
            .setConnectionManager(httpConnectionManager)
            .build();
    }

    @Override
    public CloseableHttpClient getValue(String scriptId) {
        return this.httpClient;
    }

    @Nonnull
    @Override
    public Class<CloseableHttpClient> getType() {
        return CloseableHttpClient.class;
    }

    @Nonnull
    @Override
    public ClassDoc getDoc() {
        return new ClassDoc(
            true, getType().getCanonicalName(),
            "https://hc.apache.org/httpcomponents-client-4.5.x/httpclient/apidocs/org/apache/http/impl/client/CloseableHttpClient.html"
        );
    }

    @Override
    public void dispose() throws Exception {
        this.httpClient.close();
    }
}

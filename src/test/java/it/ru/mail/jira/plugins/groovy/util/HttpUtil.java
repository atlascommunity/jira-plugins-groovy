package it.ru.mail.jira.plugins.groovy.util;

import com.google.common.io.BaseEncoding;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;

public final class HttpUtil {
    private HttpUtil() {}

    public static Header basicAuthHeader(String username, String password) {
        return new BasicHeader(
            HttpHeaders.AUTHORIZATION,
            "Basic " + BaseEncoding.base64().encode((username + ":" + password).getBytes())
        );
    }
}

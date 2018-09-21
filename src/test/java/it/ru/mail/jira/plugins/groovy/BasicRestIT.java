package it.ru.mail.jira.plugins.groovy;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableSet;
import it.ru.mail.jira.plugins.groovy.util.*;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.container.test.api.BeforeDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.*;
import org.junit.runner.RunWith;
import ru.mail.jira.plugins.groovy.api.dto.rest.HttpMethod;
import ru.mail.jira.plugins.groovy.api.dto.rest.RestScriptDto;
import ru.mail.jira.plugins.groovy.api.dto.rest.RestScriptForm;
import ru.mail.jira.plugins.groovy.api.repository.RestRepository;
import ru.mail.jira.plugins.groovy.impl.FileUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class BasicRestIT {
    private static final Set<String> requiredScripts = ImmutableSet.of(
        "tests/basicRest"
    );

    @ArquillianResource
    private URI baseURI;

    @Inject
    private UserHelper userHelper;

    @ComponentImport
    @Inject
    private RestRepository restRepository;

    private CloseableHttpClient httpClient;
    private Integer scriptId;
    private String url;

    @BeforeDeployment
    public static Archive<?> prepareArchive(Archive<?> archive) {
        return ArquillianUtil.prepareArchive(archive, requiredScripts);
    }

    @Before
    public void beforeEach() throws Exception {
        ApplicationUser admin = userHelper.getAdmin();
        userHelper.getUser(); //make sure that user exists

        httpClient = HttpClients.createMinimal();

        String scriptName = "basicScript" + System.currentTimeMillis();
        String script = FileUtil.readArquillianExample("tests/basicRest");

        RestScriptForm form = new RestScriptForm();
        form.setName(scriptName);
        form.setMethods(ImmutableSet.of(HttpMethod.GET));
        form.setGroups(ImmutableSet.of("jira-administrators"));
        form.setScriptBody(script);

        RestScriptDto scriptObject = this.restRepository.createScript(admin, form);

        scriptId = scriptObject.getId();

        this.url = baseURI.toString() + "rest/my-groovy/latest/custom/" + scriptName;
    }

    @After
    public void afterEach() throws IOException {
        if (scriptId != null) {
            restRepository.deleteScript(userHelper.getAdmin(), scriptId);
        }
        if (httpClient != null) {
            httpClient.close();
        }
    }

    @Test
    public void shouldWorkForAdmin() throws Exception {
        HttpGet request = new HttpGet(url);
        request.addHeader(HttpUtil.basicAuthHeader("admin", "admin"));
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();

            assertEquals("kek admin", EntityUtils.toString(entity));
            assertEquals("admin", response.getFirstHeader("X-AUSERNAME").getValue());
            assertEquals(200, response.getStatusLine().getStatusCode());
            assertEquals(ContentType.TEXT_PLAIN.getMimeType(), ContentType.get(entity).getMimeType());
        }
    }

    @Test
    public void unsupportedMethodShouldNotWork() throws IOException {
        HttpPost request = new HttpPost(url);
        request.addHeader(HttpUtil.basicAuthHeader("admin", "admin"));
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            assertEquals("admin", response.getFirstHeader("X-AUSERNAME").getValue());
            assertEquals(404, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    public void shouldNotWorkForAnonymous() throws Exception {
        HttpGet request = new HttpGet(url); //anonymous
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            assertEquals("anonymous", response.getFirstHeader("X-AUSERNAME").getValue());
            assertEquals(403, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    public void shouldNotWorkForUser() throws Exception {
        HttpGet request = new HttpGet(url); //user
        request.addHeader(HttpUtil.basicAuthHeader("user", "user"));
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            assertEquals("user", response.getFirstHeader("X-AUSERNAME").getValue());
            assertEquals(403, response.getStatusLine().getStatusCode());
        }
    }
}
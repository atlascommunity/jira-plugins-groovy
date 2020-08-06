package it.ru.mail.jira.plugins.groovy;


import com.google.common.collect.ImmutableSet;
import it.ru.mail.jira.plugins.groovy.util.ArquillianUtil;
import org.jboss.arquillian.container.test.api.BeforeDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import ru.mail.jira.plugins.groovy.api.service.TestHelperService;

import javax.inject.Inject;

@RunWith(Arquillian.class)
public class ClassLoaderIT {
    @BeforeDeployment
    public static Archive<?> prepareArchive(Archive<?> archive) {
        return ArquillianUtil.prepareArchive(archive, ImmutableSet.of());
    }

    @Inject
    private TestHelperService testHelperService;

    @Test
    public void javaxAnnotationShouldWork() throws Exception {
        testHelperService.loadClass("javax.annotation.PostConstruct");
        testHelperService.loadClass("javax.annotation.Nonnull");
    }
}

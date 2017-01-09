package fi.riista.feature.mail.queue;

import fi.riista.config.MailServiceTestContext;
import fi.riista.util.Locales;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@ContextConfiguration(classes = MailServiceTestContext.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class MailMessageTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    private MessageSource messageSource;

    @Test
    public void testMessageSourceWorks() {
        // Classpath problems with test resources could cause problems
        assertThat(messageSource.getMessage("site.name", null, Locales.FI), is(equalTo("Riistakeskus")));
    }

}

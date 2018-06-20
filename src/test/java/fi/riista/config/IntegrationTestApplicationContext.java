package fi.riista.config;

import com.google.common.base.Throwables;
import fi.riista.MainApplicationContext;
import fi.riista.util.JaxbUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
@Import({MainApplicationContext.class, HttpSessionConfig.class})
@PropertySource("classpath:configuration/application.properties")
@ComponentScan({"fi.riista.test", "fi.riista.util"})
public class IntegrationTestApplicationContext {

    static {
        LiquibaseConfig.replaceLiquibaseServiceLocator();
    }

    @Primary
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public Validator mvcValidator() {
        return new LocalValidatorFactoryBean();
    }

    @Bean(name = "shootingTestExportMarshaller")
    public Jaxb2Marshaller shootingTestExportJaxbMarshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setMarshallerProperties(JaxbUtils.getDefaultMarshallerProperties(true));
        marshaller.setContextPath("fi.riista.integration.metsastajarekisteri.shootingtest");
        marshaller.setSchema(new ClassPathResource("/xsd/mr/ShootingTestExport.xsd"));

        marshaller.setValidationEventHandler(event -> {
            final Throwable linkedException = event.getLinkedException();
            Throwables.throwIfUnchecked(linkedException);
            throw new RuntimeException(event.getMessage(), linkedException);
        });

        try {
            marshaller.afterPropertiesSet();
        } catch (final Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }

        return marshaller;
    }
}

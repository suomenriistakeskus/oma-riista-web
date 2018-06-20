package fi.riista.config;

import fi.riista.util.JaxbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class JaxbConfig {

    private static final Logger LOG = LoggerFactory.getLogger(JaxbConfig.class);

    @Bean(name = "lupaHallintaExportMarshaller")
    public Jaxb2Marshaller lupaHallintaExportJaxbMarshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setMarshallerProperties(JaxbUtils.getDefaultMarshallerProperties(true));
        marshaller.setPackagesToScan("fi.riista.integration.lupahallinta.model");
        marshaller.setSchema(new ClassPathResource("/xsd/lupahallinta/LupaHallintaExport.xsd"));

        return marshaller;
    }

    @Bean(name = "mooseDataCardMarshaller")
    public Jaxb2Marshaller mooseDataCardJaxbMarshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setMarshallerProperties(JaxbUtils.getDefaultMarshallerProperties(true));
        marshaller.setPackagesToScan("fi.riista.integration.luke_import.model.v1_0");
        marshaller.setSchemas(
                new ClassPathResource("/xsd/luke/FormData_v1.0.xsd"),
                new ClassPathResource("/xsd/luke/Hirvitietokortti_v1.0.xsd"),
                new ClassPathResource("/xsd/luke/AdditionalFormData_v1.0.xsd"));

        return marshaller;
    }

    @Bean(name = "srvaRvrExportMarshaller")
    public Jaxb2Marshaller srvaRvrExportJaxbMarshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setMarshallerProperties(JaxbUtils.getDefaultMarshallerProperties(true));
        marshaller.setPackagesToScan("fi.riista.integration.srva.rvr");
        marshaller.setSchema(new ClassPathResource("/xsd/srva/rvr/SrvaRvrExport.xsd"));

        return marshaller;
    }

    @Bean(name = "lukeMooselikeharvestsExportMarshaller")
    public Jaxb2Marshaller lukeMooselikeharvestsExportJaxbMarshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setMarshallerProperties(JaxbUtils.getDefaultMarshallerProperties(true));
        marshaller.setContextPath("fi.riista.integration.luke_export.mooselikeharvests");
        marshaller.setSchema(new ClassPathResource("/xsd/luke/export-mooselikeharvests-v1.xsd"));

        return activateLoggingValidation(marshaller);
    }

    @Bean(name = "paytrailMarshaller")
    public Jaxb2Marshaller paytrailMarshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setMarshallerProperties(JaxbUtils.getDefaultMarshallerProperties(false));
        marshaller.setPackagesToScan("fi.riista.integration.paytrail.rest.model");

        return marshaller;
    }

    private static Jaxb2Marshaller activateLoggingValidation(final Jaxb2Marshaller marshaller) {
        // activate
        try {
            // need to call this to really activate validation
            marshaller.afterPropertiesSet();
        } catch (final Exception e) {
            LOG.error("Exception when creating marshaller", e);
        }

        // log validation message
        marshaller.setValidationEventHandler(event -> {
            LOG.info(event.getMessage());
            return true;
        });

        return marshaller;
    }
}

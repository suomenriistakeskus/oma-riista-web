package fi.riista.config;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.xml.bind.Marshaller;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class JaxbConfig {

    private static final Logger LOG = LoggerFactory.getLogger(JaxbConfig.class);

    @Bean(name = "lupaHallintaExportMarshaller")
    public Jaxb2Marshaller lupaHallintaExportJaxbMarshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setMarshallerProperties(getMarshallerDefaultProperties());
        marshaller.setPackagesToScan("fi.riista.integration.lupahallinta.model");
        marshaller.setSchema(new ClassPathResource("/xsd/lupahallinta/LupaHallintaExport.xsd"));

        return marshaller;
    }

    @Bean(name = "lupaHallintaPermitAreaExportMarshaller")
    public Jaxb2Marshaller lupaHallintaPermitAreaExportJaxbMarshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setMarshallerProperties(getMarshallerDefaultProperties());
        marshaller.setPackagesToScan("fi.riista.integration.lupahallinta.permitarea");
        marshaller.setSchema(new ClassPathResource("/xsd/lupahallinta/PermitAreaExport.xsd"));

        return marshaller;
    }

    @Bean(name = "mooseDataCardMarshaller")
    public Jaxb2Marshaller mooseDataCardJaxbMarshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setMarshallerProperties(getMarshallerDefaultProperties());
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

        marshaller.setMarshallerProperties(getMarshallerDefaultProperties());
        marshaller.setPackagesToScan("fi.riista.integration.srva.rvr");
        marshaller.setSchema(new ClassPathResource("/xsd/srva/rvr/SrvaRvrExport.xsd"));

        return marshaller;
    }

    @Bean(name = "lukeMooselikeharvestsExportMarshaller")
    public Jaxb2Marshaller lukeMooselikeharvestsExportJaxbMarshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setMarshallerProperties(getMarshallerDefaultProperties());
        marshaller.setContextPath("fi.riista.integration.luke_export.mooselikeharvests");
        marshaller.setSchema(new ClassPathResource("/xsd/luke/export-mooselikeharvests-v1.xsd"));

        return activateLoggingValidation(marshaller);
    }

    private static Jaxb2Marshaller activateLoggingValidation(final Jaxb2Marshaller marshaller) {
        // activate
        try {
            // need to call this to really activate validation
            marshaller.afterPropertiesSet();
        } catch (Exception e) {
            LOG.error("Exception when creating marshaller", e);
        }

        // log validation message
        marshaller.setValidationEventHandler(event -> {
            LOG.info(event.getMessage());
            return true;
        });
        return marshaller;
    }

    private static Map<String, Object> getMarshallerDefaultProperties() {
        final Map<String, Object> props = new HashMap<>();
        props.put(Marshaller.JAXB_ENCODING, Constants.DEFAULT_ENCODING);
        props.put(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        props.put(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        props.put("com.sun.xml.bind.xmlHeaders", String.format(
                "<?xml version=\"1.0\" encoding=\"%s\"?>\n", Constants.DEFAULT_ENCODING));
        props.put("com.sun.xml.bind.namespacePrefixMapper", new CustomNamespacePrefixMapper());

        return props;
    }

    private static class CustomNamespacePrefixMapper extends NamespacePrefixMapper {

        @Override
        public String getPreferredPrefix(
                final String namespaceUri, final String suggestion, final boolean requirePrefix) {

            return suggestion;
        }

        @Override
        public String[] getPreDeclaredNamespaceUris2() {
            return new String[] {
                    "xsd", "http://www.w3.org/2001/XMLSchema",
                    "xsi", "http://www.w3.org/2001/XMLSchema-instance"
            };
        }
    }

}

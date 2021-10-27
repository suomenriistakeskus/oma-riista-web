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

    @Bean(name = "lukeWhiteTailedDeerExportMarshaller")
    public Jaxb2Marshaller lukeWhiteTailedDeerExportJaxbMarshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setMarshallerProperties(JaxbUtils.getDefaultMarshallerProperties(true));
        marshaller.setContextPath("fi.riista.integration.luke_export.deerharvests");
        marshaller.setSchema(new ClassPathResource("/xsd/luke/export-deerharvests-v1.xsd"));

        return activateLoggingValidation(marshaller);
    }

    @Bean(name = "mrJhtExportMarshaller")
    public Jaxb2Marshaller mrJhtExportMarshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setMarshallerProperties(JaxbUtils.getDefaultMarshallerProperties(true));
        marshaller.setContextPath("fi.riista.integration.metsastajarekisteri.jht");
        marshaller.setSchema(new ClassPathResource("xsd/mr/jht/mr-jht-v1.xsd"));

        return activateLoggingValidation(marshaller);
    }

    @Bean(name = "commonHarvestExportMarshaller")
    public Jaxb2Marshaller commonHarvestExportJaxbMarshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setMarshallerProperties(JaxbUtils.getDefaultMarshallerProperties(true));
        marshaller.setContextPath("fi.riista.integration.common.export.harvests");
        marshaller.setSchema(new ClassPathResource("/xsd/common-export/export-harvests-v1.xsd"));

        return activateLoggingValidation(marshaller);
    }

    @Bean(name = "commonObservationExportMarshaller")
    public Jaxb2Marshaller commonObservationExportJaxbMarshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setMarshallerProperties(JaxbUtils.getDefaultMarshallerProperties(true));
        marshaller.setContextPath("fi.riista.integration.common.export.observations");
        marshaller.setSchema(new ClassPathResource("/xsd/common-export/export-observations-v1.xsd"));

        return activateLoggingValidation(marshaller);
    }

    @Bean(name = "commonSrvaEventExportMarshaller")
    public Jaxb2Marshaller commonSrvaEventExportJaxbMarshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setMarshallerProperties(JaxbUtils.getDefaultMarshallerProperties(true));
        marshaller.setContextPath("fi.riista.integration.common.export.srva");
        marshaller.setSchema(new ClassPathResource("/xsd/common-export/export-srva-events-v1.xsd"));

        return activateLoggingValidation(marshaller);
    }

    @Bean(name = "commonHarvestPermitExportMarshaller")
    public Jaxb2Marshaller commonHarvestPermitExportJaxbMarshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setMarshallerProperties(JaxbUtils.getDefaultMarshallerProperties(true));
        marshaller.setContextPath("fi.riista.integration.common.export.permits");
        marshaller.setSchema(new ClassPathResource("/xsd/common-export/export-permits-v1.xsd"));

        return activateLoggingValidation(marshaller);
    }

    @Bean(name = "commonMooseHuntingSummaryExportMarshaller")
    public Jaxb2Marshaller commonMooseHuntingSummaryExportJaxbMarshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setMarshallerProperties(JaxbUtils.getDefaultMarshallerProperties(true));
        marshaller.setContextPath("fi.riista.integration.common.export.huntingsummaries");
        marshaller.setSchema(new ClassPathResource("/xsd/common-export/export-hunting-summaries-v1.xsd"));

        return activateLoggingValidation(marshaller);
    }

    @Bean(name = "paytrailMarshaller")
    public Jaxb2Marshaller paytrailMarshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setMarshallerProperties(JaxbUtils.getDefaultMarshallerProperties(false));
        marshaller.setPackagesToScan("fi.riista.integration.paytrail.rest.model");

        return marshaller;
    }

    @Bean(name = "habidesReportExportJaxbMarshaller")
    public Jaxb2Marshaller habidesReportExportJaxbMarshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setMarshallerProperties(JaxbUtils.getHabidesMarshallerProperties());
        marshaller.setContextPath("fi.riista.integration.habides.export.derogations");
        marshaller.setSchema(new ClassPathResource("/xsd/habides/derogations-v1.xsd"));

        return activateLoggingValidation(marshaller);
    }

    @Bean(name = "otherwiseDeceasedExportJaxbMarshaller")
    public Jaxb2Marshaller otherwiseDeceasedExportJaxbMarshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setMarshallerProperties(JaxbUtils.getDefaultMarshallerProperties(true));
        marshaller.setContextPath("fi.riista.integration.common.export.otherwisedeceased");
        marshaller.setSchema(new ClassPathResource("/xsd/rvr/export-otherwise-deceased-v1.xsd"));

        return activateLoggingValidation(marshaller);
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

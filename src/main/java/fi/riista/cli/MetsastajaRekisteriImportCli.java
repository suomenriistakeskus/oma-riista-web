package fi.riista.cli;

import fi.riista.config.BatchConfig;
import fi.riista.config.Constants;
import fi.riista.config.DataSourceConfig;
import fi.riista.config.JPAConfig;
import fi.riista.config.LiquibaseConfig;
import fi.riista.config.SerializationConfig;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.organization.person.ProcessDeceasedPersonFeature;
import fi.riista.feature.storage.FileStorageServiceImpl;
import fi.riista.feature.storage.backend.db.DatabaseFileStorage;
import fi.riista.integration.metsastajarekisteri.InnofactorImportConfig;
import fi.riista.integration.metsastajarekisteri.InnofactorImportRunner;
import fi.riista.integration.metsastajarekisteri.input.PendingImportFile;
import fi.riista.util.JCEUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.nio.file.Paths;
import java.security.Security;

public class MetsastajaRekisteriImportCli {

    private static final Logger LOG = LoggerFactory.getLogger(MetsastajaRekisteriImportCli.class);

    @PropertySource("configuration/application.properties")
    @ComponentScan(
            basePackageClasses = InnofactorImportConfig.class,
            excludeFilters = {
                    @Filter(type = FilterType.REGEX,
                            pattern = "fi.riista.integration.metsastajarekisteri.jht.*"),
                    @Filter(type = FilterType.REGEX,
                            pattern = "fi.riista.integration.metsastajarekisteri.shootingtest.*")
            })
    @Import({
            DataSourceConfig.class,
            JPAConfig.class,
            LiquibaseConfig.class,
            RuntimeEnvironmentUtil.class,
            BatchConfig.class,
            DatabaseFileStorage.class,
            FileStorageServiceImpl.class,
            ProcessDeceasedPersonFeature.class,
            SerializationConfig.class,
            InnofactorImportConfig.class
    })
    public static class CmdBatchJobRunnerContext {
        @Bean
        public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }
    }

    public static void main(final String[] cmdArgs) {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.getEnvironment().addActiveProfile(Constants.STANDARD_DATABASE);
            ctx.register(CmdBatchJobRunnerContext.class);
            ctx.refresh();
            ctx.start();

            JCEUtil.removeJavaCryptographyAPIRestrictions();
            Security.addProvider(new BouncyCastleProvider());

            try {
                // gzip jasen.csv
                // echo 'password' | openssl enc -aes-256-cbc -salt -pass stdin -in jasen.csv.gz -out jasen.csv.enc.gz
                // openssl enc -d -salt -aes-256-cbc -pass pass:password -in jasen.csv.gz.enc | gunzip - > jasen.csv
                final InnofactorImportRunner importRunner = ctx.getBean(InnofactorImportRunner.class);

                for (final PendingImportFile pendingImportFile : importRunner.scanForPendingUploads(Paths.get("~"))) {
                    importRunner.run(pendingImportFile);
                }

            } catch (final Exception e) {
                LOG.error("Job execution has failed with error", e);
            }
        }
    }
}

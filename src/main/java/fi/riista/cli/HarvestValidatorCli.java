package fi.riista.cli;

import com.google.common.collect.Lists;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.config.Constants;
import fi.riista.config.DataSourceConfig;
import fi.riista.config.JPAConfig;
import fi.riista.config.LiquibaseConfig;
import fi.riista.config.PapertrailConfig;
import fi.riista.config.SerializationConfig;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestFieldValidator;
import fi.riista.feature.gamediary.harvest.HarvestReportingType;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.QHarvest;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFields;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenValidator;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.util.List;

import static java.util.Optional.ofNullable;

public class HarvestValidatorCli {
    private static final Logger LOG = LoggerFactory.getLogger(HarvestValidatorCli.class);

    @PropertySource({"configuration/application.properties", "configuration/aws.properties"})
    @Import({
            DataSourceConfig.class,
            JPAConfig.class,
            LiquibaseConfig.class,
            RuntimeEnvironmentUtil.class,
            SerializationConfig.class,
            PapertrailConfig.class
    })
    public static class Context {
        @Bean
        public PlatformTransactionManager transactionManager(final DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }

        @Bean
        public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }
    }

    public static void main(final String[] cmdArgs) {
        try (final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.getEnvironment().addActiveProfile(Constants.STANDARD_DATABASE);
            ctx.register(Context.class);
            ctx.refresh();
            ctx.start();

            final JPQLQueryFactory queryFactory = ctx.getBean(JPQLQueryFactory.class);
            final EntityManager entityManager = ctx.getBean(EntityManager.class);
            final PlatformTransactionManager transactionManager = ctx.getBean(PlatformTransactionManager.class);
            final TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            transactionTemplate.setReadOnly(true);

            final QHarvest HARVEST = QHarvest.harvest;

            final List<Long> harvestIdList =
                    queryFactory.select(HARVEST.id).from(HARVEST).orderBy(HARVEST.id.asc()).fetch();

            // TODO: Check whether this needs to be set otherwise.
            final boolean isDeerPilot2020Activated = false;

            for (final List<Long> partition : Lists.partition(harvestIdList, 1000)) {
                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(final TransactionStatus transactionStatus) {
                        final List<Harvest> harvestList = queryFactory.selectFrom(HARVEST)
                                .where(HARVEST.id.in(partition))
                                .join(HARVEST.species).fetchJoin()
                                .orderBy(HARVEST.id.asc())
                                .fetch();

                        // TODO: Fetch persons'
                        validateBatch(harvestList, isDeerPilot2020Activated);

                        entityManager.clear();
                    }
                });
            }

            LOG.info("Total specimen errors: " + TOTAL_SPECIMEN_ERRORS);
            LOG.info("Total harvest errors: " + TOTAL_HARVEST_ERRORS);
        }
    }

    private static int TOTAL_HARVEST_ERRORS = 0;
    private static int TOTAL_SPECIMEN_ERRORS = 0;

    // TODO Remove `isDeerPilot2020Enabled` when deer pilot 2020 is over.
    private static void validateBatch(final List<Harvest> harvestList, final boolean isDeerPilot2020Enabled) {
        for (final Harvest harvest : harvestList) {
            if (harvest.getHuntingDayOfGroup() != null) {
                if (harvest.getHuntingDayOfGroup().getGroup().isFromMooseDataCard()) {
                    continue;
                }
            }

            // Validate updated fields
            final int speciesCode = harvest.getSpecies().getOfficialCode();
            final GroupHuntingDay huntingDayOfGroup = harvest.getHuntingDayOfGroup();
            final boolean associatedWithHuntingDay = huntingDayOfGroup != null;
            final boolean legallyMandatoryFieldsOnly =
                    ofNullable(huntingDayOfGroup).map(GroupHuntingDay::isCreatedBySystem).orElse(false);

            final int huntingYear = DateUtil.huntingYearContaining(harvest.getPointOfTimeAsLocalDate());
            final HarvestReportingType reportingType = harvest.resolveReportingType();

            // TODO Remove deer pilot translation when deer pilot 2020 is over.
            final HarvestSpecVersion revisedSpecVersion =
                    HarvestSpecVersion.CURRENTLY_SUPPORTED.revertIfNotOnDeerPilot(isDeerPilot2020Enabled);

            final RequiredHarvestFields.Specimen specimenFieldRequirements = RequiredHarvestFields.getSpecimenFields(
                    huntingYear, speciesCode, harvest.getHuntingMethod(), reportingType, legallyMandatoryFieldsOnly,
                    revisedSpecVersion);

            final RequiredHarvestFields.Report reportRequirements = RequiredHarvestFields.getFormFields(
                    huntingYear, speciesCode, reportingType, legallyMandatoryFieldsOnly, isDeerPilot2020Enabled);

            final HarvestFieldValidator harvestFieldValidator = new HarvestFieldValidator(reportRequirements, harvest);
            harvestFieldValidator.validateAll();

            if (harvestFieldValidator.hasErrors()) {
                TOTAL_HARVEST_ERRORS++;

                LOG.error(String.format("Harvest id=%d reportingType=%s speciesCode=%d huntingYear=%d huntingDay=%s " +
                                "has missing fields %s and invalid fields %s",
                        harvest.getId(),
                        reportingType,
                        speciesCode,
                        huntingYear,
                        associatedWithHuntingDay,
                        harvestFieldValidator.getMissingFields(),
                        harvestFieldValidator.getIllegalFields()));
            }

            for (final HarvestSpecimen specimen : harvest.getSortedSpecimens()) {
                final HarvestSpecimenValidator specimenValidator =
                        new HarvestSpecimenValidator(specimenFieldRequirements, specimen, speciesCode,
                                associatedWithHuntingDay, legallyMandatoryFieldsOnly);
                specimenValidator.validateAll();

                if (specimenValidator.hasErrors()) {
                    TOTAL_SPECIMEN_ERRORS++;

                    LOG.error(String.format("Harvest id=%d specimen id=%d reportingType=%s speciesCode=%d " +
                                    "huntingYear=%d huntingDay=%s age=%s gender=%s has missing fields %s and illegal " +
                                    "values %s illegal fields %s and missing moose weight %s",
                            harvest.getId(),
                            specimen.getId(),
                            reportingType,
                            speciesCode,
                            huntingYear,
                            associatedWithHuntingDay,
                            specimen.getAge(),
                            specimen.getGender(),
                            specimenValidator.getMissingFields(),
                            specimenValidator.getIllegalValues(),
                            specimenValidator.getIllegalFields(),
                            specimenValidator.isMissingMooseWeight()));

                }
            }
        }
    }
}

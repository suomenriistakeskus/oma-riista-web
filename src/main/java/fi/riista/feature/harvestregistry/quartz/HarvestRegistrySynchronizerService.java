package fi.riista.feature.harvestregistry.quartz;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.querydsl.core.group.Group;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.common.repository.MunicipalityRepository;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.QHarvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.harvest.specimen.QHarvestSpecimen;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestregistry.HarvestRegistryItem;
import fi.riista.feature.harvestregistry.HarvestRegistryItemRepository;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.integration.common.entity.Integration;
import fi.riista.integration.common.repository.IntegrationRepository;
import fi.riista.util.Collect;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.LocalisedString;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Weeks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_AMERICAN_MINK;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MUSKRAT;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_NUTRIA;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RACCOON;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RACCOON_DOG;
import static fi.riista.feature.harvestpermit.report.HarvestReportState.APPROVED;
import static fi.riista.feature.permit.PermitTypeCode.DEROGATION_PERMIT_CODES;
import static fi.riista.util.DateUtil.now;
import static java.util.stream.Collectors.toList;

@Service
public class HarvestRegistrySynchronizerService {
    private static final Logger LOG = LoggerFactory.getLogger(HarvestRegistrySynchronizerService.class);

    /*package*/ static final LocalDate REGISTRY_START_TIME_STAMP = new LocalDate(2017, 8, 1);
    private static final int PAGE_SIZE = 4096;
    private static final Weeks MAX_INTERVAL = Weeks.weeks(1);

    private static final QHarvest HARVEST = QHarvest.harvest;
    private static final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
    private static final QGameSpecies SPECIES = QGameSpecies.gameSpecies;
    private static final QHarvestSpecimen SPECIMEN = QHarvestSpecimen.harvestSpecimen;
    private static final QPerson PERSON = QPerson.person;
    private static final QOrganisation RKA_ORG = QOrganisation.organisation;
    private static final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;

    private static final BooleanExpression DEROGATION = new CaseBuilder()
            .when(PERMIT.permitTypeCode.in(DEROGATION_PERMIT_CODES))
            .then(true)
            .otherwise(false);

    private static final BooleanExpression isOfficialHarvest() {
        return HARVEST.harvestReportState.eq(APPROVED).or(HARVEST.pointOfTimeApprovedToHuntingDay.isNotNull());
    }

    // Haitalliset vieraslajit 1.6.2019 alkaen -> ei saalisrekisteriin
    // Minkki, pesukarhu, piisami, supikoira, rämemajava
    private static final Set<Integer> OMITTED_SPECIES_CODES = ImmutableSet.of(
            OFFICIAL_CODE_AMERICAN_MINK,
            OFFICIAL_CODE_RACCOON,
            OFFICIAL_CODE_MUSKRAT,
            OFFICIAL_CODE_RACCOON_DOG,
            OFFICIAL_CODE_NUTRIA);

    @Resource
    private JPQLQueryFactory queryFactory;

    @Resource
    private HarvestRegistryItemRepository harvestRegistryItemRepository;

    @Resource
    MunicipalityRepository municipalityRepository;

    @Resource
    private IntegrationRepository integrationRepository;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Transactional
    public void synchronize() {
        final DateTime now = now();

        final Integration integration = getOrCreateIntegration();
        final DateTime start = integration.getLastRun();

        DateTime end = start.plus(MAX_INTERVAL).isAfter(now)
                ? now
                : start.plus(MAX_INTERVAL);

        doSynchronize(start, end);

        integration.setLastRun(end);
        integrationRepository.save(integration);
    }

    private Integration getOrCreateIntegration() {
        return integrationRepository.findById(Integration.HARVEST_REGISTRY_SYNC_ID)
                .orElseGet(() -> {
                    final Integration createdIntegration = new Integration();
                    createdIntegration.setId(Integration.HARVEST_REGISTRY_SYNC_ID);
                    createdIntegration.setLastRun(DateUtil.toDateTimeNullSafe(REGISTRY_START_TIME_STAMP));
                    return createdIntegration;
                });
    }

    private void doSynchronize(final DateTime start, final DateTime end) {
        // Remove items which have their harvests modified after previous round and create new items
        removeItemsWithHarvestModifiedAfter(start, end);
        createItemsByOfficialHarvests(start, end);
    }

    private void createItemsByOfficialHarvests(final DateTime start, final DateTime end) {

        final Map<Long, Group> harvestInfoMap = fetchHarvests(start, end);

        final Map<Long, Set<HarvestSpecimen>> specimenMap = fetchSpecimens(harvestInfoMap.keySet());

        final Map<String, LocalisedString> municipalities =
                fetchMunicipalities(
                        F.mapNonNullsToList(harvestInfoMap.values(),
                        group -> group.getOne(HARVEST).getMunicipalityCode()));

        final List<HarvestRegistryItem> items = mapToRegistryItems(harvestInfoMap, specimenMap, municipalities);

        harvestRegistryItemRepository.saveAll(items);

        LOG.info("Persisted {} items between {} - {}", items.size(), start, end);
    }

    private static List<HarvestRegistryItem> mapToRegistryItems(final Map<Long, Group> harvestInfoMap,
                                                                final Map<Long, Set<HarvestSpecimen>> specimenMap,
                                                                final Map<String, LocalisedString> municipalities) {
        return harvestInfoMap.entrySet()
                .stream()
                .flatMap(
                        entry -> {
                            final Group group = entry.getValue();
                            return HarvestRegistryItemMapper.transform(
                                    group.getOne(HARVEST),
                                    group.getOne(PERSON),
                                    specimenMap.getOrDefault(entry.getKey(), ImmutableSet.of()),
                                    group.getOne(DEROGATION),
                                    group.getOne(RKA_ORG.officialCode),
                                    group.getOne(RHY.officialCode),
                                    municipalities);
                        })
                .collect(toList());
    }

    private Map<String, LocalisedString> fetchMunicipalities(Collection<String> municipalityCodes) {
        return municipalityRepository.findAllById(municipalityCodes)
                .stream()
                .collect(Collect.toMap(Municipality::getOfficialCode, Municipality::getNameLocalisation, HashMap::new));
    }

    private Map<Long, Group> fetchHarvests(final DateTime start, final DateTime end) {
        return queryFactory
                .select(HARVEST, PERSON, DEROGATION, SPECIES, RKA_ORG.officialCode, RHY.officialCode)
                .from(HARVEST)
                .leftJoin(HARVEST.harvestPermit, PERMIT)
                .innerJoin(HARVEST.species, SPECIES)
                .innerJoin(HARVEST.actualShooter, PERSON)
                .innerJoin(HARVEST.rhy, RHY)
                .innerJoin(RHY.parentOrganisation, RKA_ORG)
                .where(SPECIES.officialCode.notIn(OMITTED_SPECIES_CODES))
                .where(HARVEST.lifecycleFields.modificationTime.between(start, end))
                .where(isOfficialHarvest())
                .where(HARVEST.pointOfTime.after(DateUtil.toDateTimeNullSafe(REGISTRY_START_TIME_STAMP)))
                .transform(GroupBy.groupBy(HARVEST.id).as(HARVEST, PERSON, DEROGATION, SPECIES, RKA_ORG.officialCode,
                        RHY.officialCode));
    }

    private Map<Long, Set<HarvestSpecimen>> fetchSpecimens(final Set<Long> harvestIds) {
        return Lists.partition(ImmutableList.copyOf(harvestIds), PAGE_SIZE)
                .stream()
                .flatMap(partition -> queryFactory
                        .selectFrom(SPECIMEN)
                        .where(SPECIMEN.harvest.id.in(partition))
                        .transform(GroupBy.groupBy(SPECIMEN.harvest.id).as(GroupBy.set(SPECIMEN)))
                        .entrySet()
                        .stream())
                .collect(Collect.entriesToMap());
    }

    private void removeItemsWithHarvestModifiedAfter(final DateTime start, final DateTime end) {
        final List<Harvest> unofficialHarvests = queryFactory
                .selectFrom(HARVEST)
                .where(HARVEST.lifecycleFields.modificationTime.between(start, end))
                .fetch();
        Lists.partition(unofficialHarvests, PAGE_SIZE).forEach(partition ->
                harvestRegistryItemRepository.deleteByHarvestId(partition));
    }

}

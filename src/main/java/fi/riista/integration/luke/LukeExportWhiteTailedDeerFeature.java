package fi.riista.integration.luke;

import com.google.common.collect.Lists;
import fi.riista.feature.gamediary.GameDiaryEntry;
import fi.riista.feature.gamediary.GameDiaryEntry_;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenRepository;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen_;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationRepository;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount_;
import fi.riista.feature.harvestpermit.HarvestPermitSpecs;
import fi.riista.feature.harvestpermit.HarvestPermit_;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.HuntingClubGroupRepository;
import fi.riista.feature.huntingclub.group.HuntingClubGroup_;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayRepository;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay_;
import fi.riista.feature.huntingclub.permit.statistics.ClubHuntingSummaryBasicInfoByPermitAndClub;
import fi.riista.feature.huntingclub.permit.statistics.ClubHuntingSummaryBasicInfoService;
import fi.riista.feature.huntingclub.permit.statistics.HarvestCountByPermitAndClub;
import fi.riista.feature.huntingclub.permit.statistics.HarvestCountService;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.integration.luke_export.deerharvests.LED_Permits;
import fi.riista.util.F;
import fi.riista.util.JaxbUtils;
import fi.riista.util.jpa.JpaSubQuery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static fi.riista.util.Collect.groupingByIdOf;
import static fi.riista.util.Collect.idSet;
import static fi.riista.util.jpa.JpaSpecs.and;
import static fi.riista.util.jpa.JpaSpecs.equal;
import static fi.riista.util.jpa.JpaSpecs.inCollection;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Service
public class LukeExportWhiteTailedDeerFeature {

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private HarvestPermitRepository permitRepository;

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    @Resource
    private ClubHuntingSummaryBasicInfoService clubHuntingSummaryBasicInfoService;

    @Resource
    private HuntingClubGroupRepository huntingClubGroupRepository;

    @Resource
    private GroupHuntingDayRepository groupHuntingDayRepository;

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private HarvestSpecimenRepository harvestSpecimenRepository;

    @Resource
    private ObservationRepository observationRepository;

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource(name = "lukeWhiteTailedDeerExportMarshaller")
    private Jaxb2Marshaller jaxbMarshaller;

    @Resource
    private HuntingClubRepository huntingClubRepository;

    @Resource
    private HarvestCountService harvestCountService;

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_LUKE_MOOSE')")
    public String exportDeerXml(final int huntingYear) {
        return JaxbUtils.marshalToString(exportDeer(huntingYear), jaxbMarshaller);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_LUKE_MOOSE')") // TODO: Deer privilege needed?
    public LED_Permits exportDeer(final int huntingYear) {
        return exportDeer(huntingYear, 16_000);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_LUKE_MOOSE')") // TODO: Deer privilege needed?
    public LED_Permits exportDeer(final int huntingYear, final int batchSize) {
        final GameSpecies species = getSpecies();
        final List<HarvestPermit> permits = findPermits(huntingYear, species, PermitTypeCode.MOOSELIKE);
        final Map<Long, HarvestPermitSpeciesAmount> amounts = findSpeciesAmountsByPermitId(permits, species);

        final List<HuntingClubGroup> groups = findGroups(permits, species);
        final Map<Long, List<HarvestPermitSpeciesAmount>> amendmentAmounts = findAmendmentAmountsBySpeciesAmountId(huntingYear, species); // 2 DB queries

        final List<GroupHuntingDay> days = findDays(groups);
        final List<Harvest> harvests = findHarvests(days, species, batchSize);
        final Map<Long, HarvestSpecimen> harvestSpecimens = findHarvestSpecimensByHarvestId(harvests, batchSize);
        final List<Observation> observations = findObservations(days, species, batchSize);

        final ClubHuntingSummaryBasicInfoByPermitAndClub summaries = findSummaries(permits);
        final HarvestCountByPermitAndClub moderatedHarvestCounts = findModeratedHarvestCounts(permits);
        final Map<Long, String> rhyOfficialCodes = findRhyOfficialCodesByRhyId(permits);
        final Map<Long, HuntingClub> clubs = findClubIndex(summaries.allClubIds());

        return WhiteTailedDeerObjectFactory
                .create(permits, amounts, groups, amendmentAmounts, harvests, harvestSpecimens, observations, summaries,
                        moderatedHarvestCounts, rhyOfficialCodes, clubs)
                .generate();
    }

    private GameSpecies getSpecies() {
        return gameSpeciesService.requireByOfficialCode(GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER);
    }

    private List<HarvestPermit> findPermits(final int huntingYear, final GameSpecies species, final String permitTypeCode) {
        return permitRepository.findAll(and(
                HarvestPermitSpecs.validWithinHuntingYear(huntingYear),
                JpaSubQuery.of(HarvestPermit_.speciesAmounts).exists(
                        (root, cb) -> cb.equal(root.get(HarvestPermitSpeciesAmount_.gameSpecies), species)),
                equal(HarvestPermit_.permitTypeCode, permitTypeCode)));
    }

    private List<HuntingClubGroup> findGroups(final List<HarvestPermit> permits, final GameSpecies species) {
        return huntingClubGroupRepository.findAll(and(
                inCollection(HuntingClubGroup_.harvestPermit, permits),
                equal(HuntingClubGroup_.species, species)));
    }


    private Map<Long, List<HarvestPermitSpeciesAmount>> findAmendmentAmountsBySpeciesAmountId(final int huntingYear,
                                                                                              final GameSpecies species) {
        final List<HarvestPermit> amendmentPermits = findPermits(huntingYear, species, PermitTypeCode.MOOSELIKE_AMENDMENT);
        final Map<Long, HarvestPermitSpeciesAmount> speciesAmounts = findSpeciesAmountsByPermitId(amendmentPermits, species);
        return amendmentPermits.stream()
                .map(permit -> speciesAmounts.get(permit.getId()))
                .collect(groupingByIdOf(amount -> amount.getHarvestPermit().getOriginalPermit())); // No N+1 due permit is already in Hibernate's session cache
    }

    private Map<Long, HarvestPermitSpeciesAmount> findSpeciesAmountsByPermitId(final List<HarvestPermit> permits, final GameSpecies species) {
        return F.index(
                harvestPermitSpeciesAmountRepository.findAll(and(
                        inCollection(HarvestPermitSpeciesAmount_.harvestPermit, permits),
                        equal(HarvestPermitSpeciesAmount_.gameSpecies, species))),
                amount -> amount.getHarvestPermit().getId());
    }

    private List<GroupHuntingDay> findDays(final List<HuntingClubGroup> groups) {
            return groupHuntingDayRepository.findAll(inCollection(GroupHuntingDay_.group, groups));
    }

    private List<Harvest> findHarvests(final List<GroupHuntingDay> days, final GameSpecies species, final int batchSize) {
        final List<Harvest> resultList = Lists.newArrayListWithExpectedSize(Math.max(days.size(), batchSize));
        Lists.partition(days, batchSize).forEach(dayBatch -> {
            resultList.addAll(harvestRepository.findAll(and(withinDays(dayBatch), equal(GameDiaryEntry_.species, species))));
        });

        return resultList;
    }

    private Map<Long, HarvestSpecimen> findHarvestSpecimensByHarvestId(final List<Harvest> harvests, final int batchSize) {
        final List<HarvestSpecimen> resultList = Lists.newArrayListWithExpectedSize(Math.max(harvests.size(), batchSize));
        Lists.partition(harvests, batchSize).forEach(harvestBatch -> {
            resultList.addAll(harvestSpecimenRepository.findAll(inCollection(HarvestSpecimen_.harvest, harvestBatch)));
        });

        return F.index(resultList, specimen -> specimen.getHarvest().getId());
    }

    private List<Observation> findObservations(final List<GroupHuntingDay> days, final GameSpecies species, final int batchSize) {
        final List<Observation> resultList = Lists.newArrayListWithExpectedSize(Math.max(days.size(), batchSize));
        Lists.partition(days, batchSize).forEach(dayBatch -> {
            resultList.addAll(observationRepository.findAll(and(withinDays(dayBatch), equal(GameDiaryEntry_.species, species))));
        });

        return resultList;
    }

    private static <T extends GameDiaryEntry> Specification<T> withinDays(final List<GroupHuntingDay> days) {
        return inCollection(GameDiaryEntry_.huntingDayOfGroup, days);
    }

    private ClubHuntingSummaryBasicInfoByPermitAndClub findSummaries(final List<HarvestPermit> permits) {
        return clubHuntingSummaryBasicInfoService.getHuntingSummaries(F.getUniqueIds(permits),
                                                                      GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER);
    }

    private HarvestCountByPermitAndClub findModeratedHarvestCounts(final List<HarvestPermit> permits) {
        final Set<Long> permitIds = permits.stream().map(HarvestPermit::getId).collect(toSet());
        return new HarvestCountByPermitAndClub(
                harvestCountService.getModeratedHarvestCounts(permitIds, GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER));
    }

    private Map<Long, String> findRhyOfficialCodesByRhyId(final List<HarvestPermit> permits) {
        final Set<Long> rhyIds = permits.stream().map(HarvestPermit::getRhy).collect(idSet());
        return organisationRepository.findAllById(rhyIds)
                .stream()
                .collect(toMap(Organisation::getId, Organisation::getOfficialCode));
    }

    private Map<Long, HuntingClub> findClubIndex(final Set<Long> clubIds) {
        return F.indexById(huntingClubRepository.findAllById(clubIds));
    }

}

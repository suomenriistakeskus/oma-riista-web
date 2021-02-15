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
import fi.riista.feature.gamediary.observation.ObservationCategory;
import fi.riista.feature.gamediary.observation.ObservationRepository;
import fi.riista.feature.gamediary.observation.Observation_;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenRepository;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen_;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount_;
import fi.riista.feature.harvestpermit.HarvestPermitSpecs;
import fi.riista.feature.harvestpermit.HarvestPermit_;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.HuntingClubGroupRepository;
import fi.riista.feature.huntingclub.group.HuntingClubGroup_;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayRepository;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay_;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.BasicClubHuntingSummary;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.BasicClubHuntingSummaryRepository;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.MooseHuntingSummary;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.MooseHuntingSummaryRepository;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.MooseHuntingSummary_;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationSort;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_Club;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_Permit;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_Permits;
import fi.riista.util.F;
import fi.riista.util.JaxbUtils;
import fi.riista.util.jpa.JpaSubQuery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;

import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;
import static fi.riista.util.Collect.groupingByIdOf;
import static fi.riista.util.Collect.indexingBy;
import static fi.riista.util.Collect.indexingByIdOf;
import static fi.riista.util.Collect.leastAfterGroupingBy;
import static fi.riista.util.jpa.JpaSpecs.and;
import static fi.riista.util.jpa.JpaSpecs.equal;
import static fi.riista.util.jpa.JpaSpecs.inCollection;
import static fi.riista.util.jpa.JpaSpecs.notEqual;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.jpa.domain.Specification.where;

@Service
public class LukeExportFeature {

    @Resource
    private GroupHuntingDayRepository groupHuntingDayRepository;

    @Resource
    private HarvestPermitRepository permitRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private HuntingClubGroupRepository huntingClubGroupRepository;

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private ObservationRepository observationRepository;

    @Resource
    private HarvestSpecimenRepository harvestSpecimenRepository;

    @Resource
    private ObservationSpecimenRepository observationSpecimenRepository;

    @Resource
    private MooseHuntingSummaryRepository mooseHuntingSummaryRepository;

    @Resource
    private BasicClubHuntingSummaryRepository basicSummaryRepo;

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @PersistenceContext
    private EntityManager entityManager;

    @Resource(name = "lukeMooselikeharvestsExportMarshaller")
    private Jaxb2Marshaller jaxbMarshaller;

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_LUKE_MOOSE')")
    public String exportMooseXml(final int huntingYear) {
        return JaxbUtils.marshalToString(exportMoose(huntingYear), jaxbMarshaller);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_LUKE_MOOSE')")
    public LEM_Permits exportMoose(final int huntingYear) {
        final GameSpecies moose = gameSpeciesService.requireByOfficialCode(GameSpecies.OFFICIAL_CODE_MOOSE);
        final List<HarvestPermit> allPermits = findPermits(huntingYear, moose, PermitTypeCode.MOOSELIKE);

        final Map<Long, HarvestPermitSpeciesAmount> allPermitsSpeciesAmounts = F.index(
                harvestPermitSpeciesAmountRepository.findAll(
                        where(inCollection(HarvestPermitSpeciesAmount_.harvestPermit, allPermits))
                                .and(equal(HarvestPermitSpeciesAmount_.gameSpecies, moose))),
                hpsa -> hpsa.getHarvestPermit().getId());

        final Map<HarvestPermit, Map<HuntingClub, MooseHuntingSummary>> permitToClubToSummary = findSummaries(allPermits);
        final List<HarvestPermit> amendmentPermits = findPermits(huntingYear, moose, PermitTypeCode.MOOSELIKE_AMENDMENT);
        final Map<Long, List<HarvestPermitSpeciesAmount>> amendmentPermitSpas = amendmentPermits.stream()
                .map(hp -> findAnyMooseSpeciesAmount(hp.getSpeciesAmounts()).orElseThrow(
                        () -> new IllegalStateException("Could not find moose speciesAmount for harvestPermitId=" + hp.getId())))
                .collect(groupingByIdOf(hpsa -> hpsa.getHarvestPermit().getOriginalPermit()));

        // To prevent too many parameters to SQL IN clause, process permits one by one
        // One by one processing is too slow (because of many queries), process in bigger batches
        final List<LEM_Permit> resultList = new LinkedList<>();
        Lists.partition(F.getNonNullIds(allPermits), 50).forEach(moosePermitIds -> {

            final List<HarvestPermit> moosePermits = permitRepository.findAll(inCollection(HarvestPermit_.id, moosePermitIds));

            final List<HuntingClubGroup> groups = huntingClubGroupRepository.findAll(
                    where(inCollection(HuntingClubGroup_.harvestPermit, moosePermits))
                            .and(equal(HuntingClubGroup_.species, moose)));

            final List<GroupHuntingDay> days = groupHuntingDayRepository.findAll(huntingDayPredicate(groups));
            final List<Harvest> harvests = harvestRepository.findAll(and(withinDays(days), equal(GameDiaryEntry_.species, moose)));
            final List<Observation> observations = observationRepository.findAll(and(withinDays(days), equal(Observation_.observationCategory, ObservationCategory.MOOSE_HUNTING)));

            final Map<Long, List<HuntingClubGroup>> clubGroups = groups.stream().collect(groupingByIdOf(Organisation::getParentOrganisation));
            final Map<Long, List<GroupHuntingDay>> groupDays = days.stream().collect(groupingByIdOf(GroupHuntingDay::getGroup));
            final Map<Long, List<Harvest>> dayHarvests = harvests.stream().collect(groupByHuntingDayGroupId());
            final Map<Long, List<Observation>> dayObservations = observations.stream().collect(groupByHuntingDayGroupId());

            final Map<Long, List<HarvestSpecimen>> harvestSpecimens = harvestSpecimenRepository
                    .findAll(inCollection(HarvestSpecimen_.harvest, harvests))
                    .stream()
                    .collect(groupingByIdOf(HarvestSpecimen::getHarvest));

            final Map<Long, List<ObservationSpecimen>> observationSpecimens = observationSpecimenRepository
                    .findAll(inCollection(ObservationSpecimen_.observation, observations))
                    .stream()
                    .collect(groupingByIdOf(ObservationSpecimen::getObservation));

            resultList.addAll(moosePermits.stream().map(moosePermit -> {
                final Set<HuntingClub> clubs = moosePermit.getPermitPartners();
                final Map<Long, Occupation> clubContacts = findClubContacts(clubs); // N+1
                final HarvestPermitSpeciesAmount spa = allPermitsSpeciesAmounts.get(moosePermit.getId());
                final List<HarvestPermitSpeciesAmount> amendmentSpas = amendmentPermitSpas.get(moosePermit.getId());
                final Map<Long, BasicClubHuntingSummary> clubOverrides = basicSummaryRepo
                        .findModeratorOverriddenHuntingSummaries(spa)
                        .stream()
                        .collect(indexingByIdOf(BasicClubHuntingSummary::getClub));

                final List<LEM_Club> clubPermitData = clubs.stream()
                        .sorted(comparing(Organisation::getId))
                        .map(club -> MooselikeHarvestsObjectFactory.createClub(club, moosePermit, clubContacts,
                                clubGroups, groupDays, dayHarvests, dayObservations, harvestSpecimens,
                                observationSpecimens, permitToClubToSummary, clubOverrides))
                        .filter(Objects::nonNull)
                        .collect(toList());

                return MooselikeHarvestsObjectFactory.createPermit(
                        moosePermit.getPermitNumber(), moosePermit.getRhy().getOfficialCode(),
                        moosePermit.getOriginalContactPerson(), spa, amendmentSpas, clubPermitData);
            }).collect(toList()));

            entityManager.clear(); //hopefully less memory used
        });

        final LEM_Permits permits = new LEM_Permits();
        permits.setPermits(resultList);
        return permits;
    }

    private static Specification<GroupHuntingDay> huntingDayPredicate(final List<HuntingClubGroup> groups) {
        return and(inCollection(GroupHuntingDay_.group, groups), notEqual(GroupHuntingDay_.createdBySystem, true));
    }

    private Map<HarvestPermit, Map<HuntingClub, MooseHuntingSummary>> findSummaries(final List<HarvestPermit> moosePermits) {
        return mooseHuntingSummaryRepository
                .findAll(where(inCollection(MooseHuntingSummary_.harvestPermit, moosePermits))
                        .and(equal(MooseHuntingSummary_.huntingFinished, true)))
                .stream()
                .collect(groupingBy(MooseHuntingSummary::getHarvestPermit, indexingBy(MooseHuntingSummary::getClub)));
    }

    private static Optional<HarvestPermitSpeciesAmount> findAnyMooseSpeciesAmount(final List<HarvestPermitSpeciesAmount> spas) {
        return spas.stream().filter(s -> s.getGameSpecies().isMoose()).findAny();
    }

    private List<HarvestPermit> findPermits(final int huntingYear, final GameSpecies moose, final String permitTypeCode) {
        return permitRepository.findAll(and(
                HarvestPermitSpecs.validWithinHuntingYear(huntingYear),
                JpaSubQuery.of(HarvestPermit_.speciesAmounts).exists((root, cb) -> cb.equal(root.get(HarvestPermitSpeciesAmount_.gameSpecies), moose)),
                equal(HarvestPermit_.permitTypeCode, permitTypeCode)));
    }

    private static <T extends GameDiaryEntry> Specification<T> withinDays(final List<GroupHuntingDay> days) {
        return inCollection(GameDiaryEntry_.huntingDayOfGroup, days);
    }

    private static <T extends GameDiaryEntry> Collector<T, ?, Map<Long, List<T>>> groupByHuntingDayGroupId() {
        return groupingByIdOf(GameDiaryEntry::getHuntingDayOfGroup);
    }

    @Nonnull
    private Map<Long, Occupation> findClubContacts(final Set<HuntingClub> clubs) {

        if (clubs.isEmpty()) {
            return Collections.emptyMap();
        }

        return occupationRepository
                .findActiveByOrganisationsAndTypes(F.getUniqueIds(clubs), EnumSet.of(SEURAN_YHDYSHENKILO))
                .stream()
                .filter(o -> o.getCallOrder() != null)
                .collect(leastAfterGroupingBy(o -> o.getOrganisation().getId(), OccupationSort.BY_CALL_ORDER));
    }
}

package fi.riista.integration.luke;

import com.google.common.collect.Lists;
import fi.riista.feature.gamediary.GameDiaryEntry;
import fi.riista.feature.gamediary.GameDiaryEntry_;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesRepository;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenRepository;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen_;
import fi.riista.feature.gamediary.observation.Observation;
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
import fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummary;
import fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummaryRepository;
import fi.riista.feature.huntingclub.permit.summary.MooseHuntingSummary;
import fi.riista.feature.huntingclub.permit.summary.MooseHuntingSummaryRepository;
import fi.riista.feature.huntingclub.permit.summary.MooseHuntingSummary_;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_Club;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_Permit;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_Permits;
import fi.riista.util.F;
import fi.riista.util.JaxbUtils;
import fi.riista.util.jpa.JpaSubQuery;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;

import static fi.riista.util.jpa.JpaSpecs.and;
import static fi.riista.util.jpa.JpaSpecs.equal;
import static fi.riista.util.jpa.JpaSpecs.inCollection;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparingInt;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.minBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.data.jpa.domain.Specifications.where;

@Service
public class LukeExportFeature {

    @Resource
    private GroupHuntingDayRepository groupHuntingDayRepository;

    @Resource
    private GameSpeciesRepository gameSpeciesRepository;

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
        final GameSpecies moose = gameSpeciesRepository.findByOfficialCode(GameSpecies.OFFICIAL_CODE_MOOSE)
                .orElseThrow(() -> new IllegalStateException("Could not found moose species in db"));

        final List<HarvestPermit> allPermits = findPermits(huntingYear, moose, HarvestPermit.MOOSELIKE_PERMIT_TYPE);

        final Map<Long, HarvestPermitSpeciesAmount> allPermitsSpeciesAmounts = F.index(
                harvestPermitSpeciesAmountRepository.findAll(
                        where(inCollection(HarvestPermitSpeciesAmount_.harvestPermit, allPermits))
                                .and(equal(HarvestPermitSpeciesAmount_.gameSpecies, moose))),
                hpsa -> hpsa.getHarvestPermit().getId());

        final Map<HarvestPermit, Map<HuntingClub, MooseHuntingSummary>> permitToClubToSummary = findSummaries(allPermits);
        final List<HarvestPermit> amendmentPermits = findPermits(huntingYear, moose, HarvestPermit.MOOSELIKE_AMENDMENT_PERMIT_TYPE);
        final Map<Long, List<HarvestPermitSpeciesAmount>> amendmentPermitSpas = amendmentPermits.stream()
                .map(hp -> findAnyMooseSpeciesAmount(hp.getSpeciesAmounts()).orElseThrow(
                        () -> new IllegalStateException("Could not find moose speciesAmount for harvestPermitId=" + hp.getId())))
                .collect(groupingBy(hpsa -> hpsa.getHarvestPermit().getOriginalPermit().getId()));

        // To prevent too many parameters to SQL IN clause, process permits one by one
        // One by one processing is too slow (because of many queries), process in bigger batches
        final List<LEM_Permit> resultList = new LinkedList<>();
        Lists.partition(F.getNonNullIds(allPermits), 50).forEach(moosePermitIds -> {

            final List<HarvestPermit> moosePermits = permitRepository.findAll(inCollection(HarvestPermit_.id, moosePermitIds));

            final List<HuntingClubGroup> groups = huntingClubGroupRepository.findAll(
                    where(inCollection(HuntingClubGroup_.harvestPermit, moosePermits))
                            .and(equal(HuntingClubGroup_.species, moose)));

            final List<GroupHuntingDay> days = groupHuntingDayRepository.findAll(inCollection(GroupHuntingDay_.group, groups));
            final List<Harvest> harvests = harvestRepository.findAll(and(withinDays(days), equal(GameDiaryEntry_.species, moose)));
            final List<Observation> observations = observationRepository.findAll(and(withinDays(days), equal(Observation_.withinMooseHunting, true)));

            final Map<Long, List<HuntingClubGroup>> clubGroups = groups.stream().collect(groupingBy(group -> group.getParentOrganisation().getId()));
            final Map<Long, List<GroupHuntingDay>> groupDays = days.stream().collect(groupingBy(day -> day.getGroup().getId()));
            final Map<Long, List<Harvest>> dayHarvests = harvests.stream().collect(groupByHuntingDayGroupId());
            final Map<Long, List<Observation>> dayObservations = observations.stream().collect(groupByHuntingDayGroupId());

            final Map<Long, List<HarvestSpecimen>> harvestSpecimens = harvestSpecimenRepository.findAll(inCollection(HarvestSpecimen_.harvest, harvests))
                    .stream().collect(groupingBy(hs -> hs.getHarvest().getId()));

            final Map<Long, List<ObservationSpecimen>> observationSpecimens =
                    observationSpecimenRepository.findAll(inCollection(ObservationSpecimen_.observation, observations))
                            .stream().collect(groupingBy(o -> o.getObservation().getId()));

            resultList.addAll(moosePermits.stream().map(moosePermit -> {
                final Set<HuntingClub> clubs = moosePermit.getPermitPartners();
                final Map<Long, Occupation> clubContacts = findClubContacts(clubs); // N+1
                final HarvestPermitSpeciesAmount spa = allPermitsSpeciesAmounts.get(moosePermit.getId());
                final List<HarvestPermitSpeciesAmount> amendmentSpas = amendmentPermitSpas.get(moosePermit.getId());
                final Map<Long, BasicClubHuntingSummary> clubOverrides = F.index(
                        basicSummaryRepo.findModeratorOverriddenHuntingSummaries(spa),
                        s -> s.getClub().getId());

                final List<LEM_Club> clubPermitData = clubs.stream()
                        .sorted(Comparator.comparing(Organisation::getId))
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

    private Map<HarvestPermit, Map<HuntingClub, MooseHuntingSummary>> findSummaries(final List<HarvestPermit> moosePermits) {
        final List<MooseHuntingSummary> summaries = mooseHuntingSummaryRepository.findAll(
                where(inCollection(MooseHuntingSummary_.harvestPermit, moosePermits))
                        .and(equal(MooseHuntingSummary_.huntingFinished, true)));

        return summaries.stream().collect(groupingBy(MooseHuntingSummary::getHarvestPermit,
                toMap(MooseHuntingSummary::getClub, identity())));
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
        return groupingBy(h -> h.getHuntingDayOfGroup().getId());
    }

    @Nonnull
    private Map<Long, Occupation> findClubContacts(final Set<HuntingClub> clubs) {
        final List<Occupation> activeContactPersons = occupationRepository.findActiveByOrganisationsAndTypes(
                F.getNonNullIds(clubs), EnumSet.of(OccupationType.SEURAN_YHDYSHENKILO));

        return activeContactPersons.stream()
                .filter(o -> o.getCallOrder() != null)
                .collect(groupingBy(
                        o -> o.getOrganisation().getId(),
                        collectingAndThen(
                                minBy(comparingInt(Occupation::getCallOrder)),
                                o -> o.orElse(null))));
    }
}

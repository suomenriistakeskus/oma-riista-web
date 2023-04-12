package fi.riista.feature.harvestpermit.report.jhtarchive;

import com.google.common.collect.ImmutableSet;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.common.decision.DecisionStatus;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.gamediary.harvest.QHarvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.nestremoval.HarvestPermitNestRemovalUsage;
import fi.riista.feature.harvestpermit.nestremoval.QHarvestPermitNestRemovalUsage;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.QGroupHuntingDay;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.BasicClubHuntingSummary;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.ModeratedHarvestCounts;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.QBasicClubHuntingSummary;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.QHarvestPermitApplication;
import fi.riista.feature.permit.application.QHarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.QPermitDecision;
import fi.riista.util.F;
import fi.riista.util.Localiser;
import fi.riista.util.jpa.CriteriaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.sum;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Component
public class JhtArchiveFeature {

    private static final Logger LOG = LoggerFactory.getLogger(JhtArchiveFeature.class);

    private static final Set<String> PERMIT_TYPES = ImmutableSet.of(
            PermitTypeCode.MOOSELIKE,
            PermitTypeCode.MOOSELIKE_AMENDMENT,
            PermitTypeCode.FOWL_AND_UNPROTECTED_BIRD,
            PermitTypeCode.ANNUAL_UNPROTECTED_BIRD,
            PermitTypeCode.BEAR_KANNAHOIDOLLINEN,
            PermitTypeCode.LYNX_KANNANHOIDOLLINEN,
            PermitTypeCode.WOLF_KANNANHOIDOLLINEN,
            PermitTypeCode.MAMMAL_DAMAGE_BASED,
            PermitTypeCode.NEST_REMOVAL_BASED,
            PermitTypeCode.LAW_SECTION_TEN_BASED,
            PermitTypeCode.IMPORTING,
            PermitTypeCode.DEPORTATION,
            PermitTypeCode.RESEARCH,
            PermitTypeCode.GAME_MANAGEMENT,
            PermitTypeCode.FORBIDDEN_METHODS,
            PermitTypeCode.EUROPEAN_BEAVER,
            PermitTypeCode.PARTRIDGE);

    private static final Set<String> IMMATERIAL_PERMIT_TYPES = ImmutableSet.of(
            PermitTypeCode.DISABILITY_BASED,
            PermitTypeCode.WEAPON_TRANSPORTATION_BASED,
            PermitTypeCode.DOG_DISTURBANCE_BASED,
            PermitTypeCode.DOG_UNLEASH_BASED
    );

    private static final QPermitDecision DECISION = QPermitDecision.permitDecision;
    private static final QGameSpecies SPECIES = QGameSpecies.gameSpecies;
    private static final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private RiistanhoitoyhdistysRepository rhyRepository;

    @Resource
    private OrganisationRepository organisationRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    public List<JhtArchiveExcelDTO> immaterialPermitDataForExcel(final String permitTypeCode,
                                                                 final int calendarYear) {

        final BooleanExpression selectedPermitType = permitTypeCode == null
                ? PERMIT.permitTypeCode.isNotNull() // All types goes
                : PERMIT.permitTypeCode.eq(permitTypeCode);

        final Set<HarvestPermit> permits = new HashSet<>(
                jpqlQueryFactory
                        .from(PERMIT)
                        .where(selectedPermitType,
                               PERMIT.permitTypeCode.in(IMMATERIAL_PERMIT_TYPES),
                               DECISION.status.in(DecisionStatus.LOCKED, DecisionStatus.PUBLISHED),
                               PERMIT.permitYear.eq(calendarYear))
                            .select(PERMIT)
                        .fetch());

        final Function<HarvestPermit, Riistanhoitoyhdistys> rhyMapper = getRhyMapper(permits);
        final Function<Riistanhoitoyhdistys, Organisation> rkaMapper = getRkaMapper(permits, rhyMapper);

        return permits.stream()
                .sorted(Comparator.comparing(HarvestPermit::getPermitNumber))
                .map(permit -> JhtArchiveExcelDTO.builder()
                        .withHarvestPermit(permit)
                        .withRka(rkaMapper.apply(rhyMapper.apply(permit)))
                        .build())
                .collect(toList());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    public List<JhtArchiveExcelDTO> permitDataForExcel(final String permitTypeCode,
                                                       final Integer speciesCode,
                                                       final int calendarYear) {

        LOG.debug("permitTypeCode: \"" + permitTypeCode + "\", speciesCode: " + speciesCode + ", calendarYear: " + calendarYear);

        final GameSpecies species = getSpecies(speciesCode);
        final List<PermitData> permitData = queryPermitData(permitTypeCode, species, calendarYear);
        final Map<DecisionAndSpeciesKey, HarvestPermitApplicationSpeciesAmount> applicationAmountsByDecision = queryApplicationAmountMap(permitData);
        final Set<HarvestPermit> permits = permitData.stream().map(PermitData::getHarvestPermit).collect(toSet());
        final Function<HarvestPermit, Riistanhoitoyhdistys> rhyMapper = getRhyMapper(permits);
        final Function<Riistanhoitoyhdistys, Organisation> rkaMapper = getRkaMapper(permits, rhyMapper);
        final Map<PermitAndSpeciesKey, Integer> harvestAmountMapper = queryHarvestAmountMap(permits);
        final Map<PermitAndSpeciesKey, Integer> mooselikeHarvestAmountMapper = queryMooselikeHarvestAmountMap(permits);
        final Map<PermitAndSpeciesKey, BasicClubHuntingSummary> mooselikeOverrideMap = queryMooselikeAmountOverrideMap(permits);
        final Map<PermitAndSpeciesKey, HarvestPermitNestRemovalUsage> nestRemovalUsageMap = queryHarvestPermitNestRemovalUsage(permits);

        return permitData.stream()
                .sorted(Comparator.comparing(item -> item.getHarvestPermit().getPermitNumber()))
                .map(item -> JhtArchiveExcelDTO.builder()
                        .withHarvestPermit(item.getHarvestPermit())
                        .withHarvestPermitSpeciesAmount(item.getHarvestPermitSpeciesAmount())
                        .withGameSpecies(item.getGameSpecies())
                        .withApplicationSpeciesAmount(applicationAmountsByDecision.get(item.getDecisionAndSpeciesKey()))
                        .withRhy(rhyMapper.apply(item.getHarvestPermit()))
                        .withRka(rkaMapper.apply(rhyMapper.apply(item.getHarvestPermit())))
                        .withHarvestedSpecimens(getHarvestValue(item.getPermitAndSpeciesKey(),
                                                                harvestAmountMapper,
                                                                mooselikeHarvestAmountMapper,
                                                                mooselikeOverrideMap))
                        .withHarvestedNests(F.mapNullable(nestRemovalUsageMap.get(item.getPermitAndSpeciesKey()), HarvestPermitNestRemovalUsage::getNestAmount))
                        .withHarvestedEggs(F.mapNullable(nestRemovalUsageMap.get(item.getPermitAndSpeciesKey()), HarvestPermitNestRemovalUsage::getEggAmount))
                        .withHarvestedConstructions(F.mapNullable(nestRemovalUsageMap.get(item.getPermitAndSpeciesKey()), HarvestPermitNestRemovalUsage::getConstructionAmount))
                        .build())
                .collect(toList());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    public String searchParametersAsString(final Localiser localiser,
                                           final String permitTypeCode,
                                           final Integer speciesCode,
                                           final int calendarYear) {
        return String.format(
                "%s%s_%s",
                permitTypeCode != null ? "_" + permitTypeCode : "",
                speciesCode != null ? "_" + localiser.getTranslation(getSpecies(speciesCode).getNameLocalisation()) : "",
                calendarYear);
    }

    private Float getHarvestValue(final PermitAndSpeciesKey key,
                                  final Map<PermitAndSpeciesKey, Integer> harvestAmountMapper,
                                  final Map<PermitAndSpeciesKey, Integer> mooselikeHarvestAmountMapper,
                                  final Map<PermitAndSpeciesKey, BasicClubHuntingSummary> mooselikeOverrideMap) {

        if (mooselikeOverrideMap.containsKey(key)) {
            final ModeratedHarvestCounts override = mooselikeOverrideMap.get(key).getModeratedHarvestCounts();
            if (override != null) {
                return  Integer.valueOf(override.getNumberOfAdultFemales()
                                                + override.getNumberOfAdultMales()
                                                + override.getNumberOfYoungFemales()
                                                + override.getNumberOfYoungMales())
                        .floatValue();
            }
        }
        final Integer harvests =  harvestAmountMapper.getOrDefault(key, mooselikeHarvestAmountMapper.getOrDefault(key,null));
        return harvests != null ? harvests.floatValue() : null;
    }

    private GameSpecies getSpecies(final Integer officialCode) {
        return officialCode == null ? null : gameSpeciesService.requireByOfficialCode(officialCode);
    }

    private List<PermitData> queryPermitData(final String permitTypeCode, final GameSpecies species, final int calendarYear) {
        final QHarvestPermitSpeciesAmount AMOUNT = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;

        final BooleanExpression selectedSpecies = species == null
                ? AMOUNT.gameSpecies.isNotNull() // All species goes -- should be always true
                : AMOUNT.gameSpecies.eq(species);

        final BooleanExpression selectedPermitType = permitTypeCode == null
                ? PERMIT.permitTypeCode.isNotNull() // All permit types goes
                : PERMIT.permitTypeCode.eq(permitTypeCode);

        return jpqlQueryFactory.from(AMOUNT)
                .join(AMOUNT.harvestPermit, PERMIT)
                .join(AMOUNT.gameSpecies, SPECIES)
                .join(PERMIT.permitDecision, DECISION)
                .where(PERMIT.permitYear.eq(calendarYear),
                       selectedSpecies,
                       selectedPermitType,
                       PERMIT.permitTypeCode.in(PERMIT_TYPES),
                       DECISION.status.in(DecisionStatus.LOCKED, DecisionStatus.PUBLISHED))
                .select(PERMIT, AMOUNT, DECISION, SPECIES)
                .fetch()
                .stream()
                .map(PermitData::create)
                .collect(toList());
    }

    private Map<DecisionAndSpeciesKey, HarvestPermitApplicationSpeciesAmount> queryApplicationAmountMap(final List<PermitData> permitData) {
        final QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;
        final QHarvestPermitApplicationSpeciesAmount AMOUNT = QHarvestPermitApplicationSpeciesAmount.harvestPermitApplicationSpeciesAmount;

        final List<PermitDecision> decisions = permitData.stream().map(PermitData::getPermitDecision).collect(toList());

        return jpqlQueryFactory.from(AMOUNT)
                .join(AMOUNT.harvestPermitApplication, APPLICATION)
                .join(AMOUNT.gameSpecies, SPECIES)
                .join(DECISION).on(DECISION.application.eq(APPLICATION))
                .where(DECISION.in(decisions))
                .select(DECISION, SPECIES, AMOUNT)
                .transform(groupBy(DecisionAndSpeciesKey.createProjection()).as(AMOUNT));
    }

    private Map<PermitAndSpeciesKey, Integer> queryHarvestAmountMap(final Collection<HarvestPermit> permits) {
        final QHarvest HARVEST = QHarvest.harvest;

        return jpqlQueryFactory.from(HARVEST)
                .join(HARVEST.species, SPECIES)
                .join(HARVEST.harvestPermit, PERMIT)
                .select(PERMIT, SPECIES, HARVEST)
                .where(PERMIT.in(permits),
                       HARVEST.huntingDayOfGroup.isNull(),
                       HARVEST.harvestReportState.eq(HarvestReportState.APPROVED))
                .transform(groupBy(PermitAndSpeciesKey.createProjection()).as(sum(HARVEST.amount)));
    }

    private Map<PermitAndSpeciesKey, HarvestPermitNestRemovalUsage> queryHarvestPermitNestRemovalUsage(final Collection<HarvestPermit> permits) {
        final QHarvestPermitSpeciesAmount AMOUNT = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final QHarvestPermitNestRemovalUsage USAGE = QHarvestPermitNestRemovalUsage.harvestPermitNestRemovalUsage;

        return jpqlQueryFactory.from(USAGE)
                .join(USAGE.harvestPermitSpeciesAmount, AMOUNT)
                .join(AMOUNT.harvestPermit, PERMIT)
                .join(AMOUNT.gameSpecies, SPECIES)
                .select(PERMIT, SPECIES, USAGE)
                .where(PERMIT.in(permits))
                .transform(groupBy(PermitAndSpeciesKey.createProjection()).as(USAGE));
    }

    private Map<PermitAndSpeciesKey, Integer> queryMooselikeHarvestAmountMap(final Collection<HarvestPermit> permits) {
        final QHuntingClubGroup HUNTING_GROUP = QHuntingClubGroup.huntingClubGroup;
        final QGroupHuntingDay HUNTING_DAY = QGroupHuntingDay.groupHuntingDay;
        final QHarvest HARVEST = QHarvest.harvest;

        return jpqlQueryFactory.from(HARVEST)
                .join(HARVEST.species, SPECIES)
                .join(HARVEST.huntingDayOfGroup, HUNTING_DAY)
                .join(HUNTING_DAY.group, HUNTING_GROUP)
                .join(HUNTING_GROUP.harvestPermit, PERMIT)
                .select(PERMIT, SPECIES, HARVEST)
                .where(PERMIT.in(permits))
                .transform(groupBy(PermitAndSpeciesKey.createProjection()).as(sum(HARVEST.amount)));
    }

    private Map<PermitAndSpeciesKey, BasicClubHuntingSummary> queryMooselikeAmountOverrideMap(final Collection<HarvestPermit> permits) {
        final QHarvestPermitSpeciesAmount AMOUNTS = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final QBasicClubHuntingSummary SUMMARY = QBasicClubHuntingSummary.basicClubHuntingSummary;

        return jpqlQueryFactory.from(SUMMARY)
                .join(SUMMARY.speciesAmount, AMOUNTS)
                .join(AMOUNTS.harvestPermit, PERMIT)
                .join(AMOUNTS.gameSpecies, SPECIES)
                .select(PERMIT, SPECIES, SUMMARY)
                .where(PERMIT.in(permits),
                       SUMMARY.moderatorOverride.isTrue())
                .transform(groupBy(PermitAndSpeciesKey.createProjection()).as(SUMMARY));
    }

    private Function<HarvestPermit, Riistanhoitoyhdistys> getRhyMapper(final Collection<HarvestPermit> permits) {
        return CriteriaUtils.singleQueryFunction(permits, HarvestPermit::getRhy, rhyRepository, true);
    }

    private Function<Riistanhoitoyhdistys, Organisation> getRkaMapper(final Collection<HarvestPermit> permits,
                                                                      final Function<HarvestPermit, Riistanhoitoyhdistys> rhyMapper) {
        final Set<Riistanhoitoyhdistys> rhys = permits.stream().map(rhyMapper).collect(toSet());
        return CriteriaUtils.singleQueryFunction(rhys, Riistanhoitoyhdistys::getRiistakeskuksenAlue, organisationRepository, true);
    }

    /**
     * Helper class for managing permit related tuple.
     */

    public static class PermitData {

        public static PermitData create(final Tuple tuple) {
            return new PermitData(tuple);
        }

        private final Tuple tuple;

        public PermitData(final Tuple tuple) {
            this.tuple = tuple;
        }

        public HarvestPermit getHarvestPermit() {
            return tuple.get(0, HarvestPermit.class);
        }

        public HarvestPermitSpeciesAmount getHarvestPermitSpeciesAmount() {
            return tuple.get(1, HarvestPermitSpeciesAmount.class);
        }

        public PermitDecision getPermitDecision() {
            return tuple.get(2, PermitDecision.class);
        }

        public GameSpecies getGameSpecies() {
            return tuple.get(3, GameSpecies.class);
        }

        public DecisionAndSpeciesKey getDecisionAndSpeciesKey() {
            return new DecisionAndSpeciesKey(getPermitDecision(), getGameSpecies());
        }

        public PermitAndSpeciesKey getPermitAndSpeciesKey() {
            return new PermitAndSpeciesKey(getHarvestPermit(), getGameSpecies());
        }
    }

    /**
     * Helper classes for mapping with two values
     */

    public static class DecisionAndSpeciesKey {
        private Long decisionId;
        private Long speciesId;

        public static ConstructorExpression<DecisionAndSpeciesKey> createProjection() {
            return Projections.constructor(DecisionAndSpeciesKey.class, DECISION, SPECIES);
        }

        public DecisionAndSpeciesKey(final PermitDecision decision, final GameSpecies species) {
            decisionId = decision.getId();
            speciesId = species.getId();
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final DecisionAndSpeciesKey that = (DecisionAndSpeciesKey) o;
            return Objects.equals(decisionId, that.decisionId) && Objects.equals(speciesId, that.speciesId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(decisionId, speciesId);
        }
    }

    public static class PermitAndSpeciesKey {
        private Long permitId;
        private Long speciesId;

        public static ConstructorExpression<PermitAndSpeciesKey> createProjection() {
            return Projections.constructor(PermitAndSpeciesKey.class, PERMIT, SPECIES);
        }


        public PermitAndSpeciesKey(final HarvestPermit permit, final GameSpecies species) {
            permitId = permit.getId();
            speciesId = species.getId();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final PermitAndSpeciesKey other = (PermitAndSpeciesKey) obj;
            return permitId.equals(other.permitId) && speciesId.equals(other.speciesId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(permitId, speciesId);
        }
    }
}

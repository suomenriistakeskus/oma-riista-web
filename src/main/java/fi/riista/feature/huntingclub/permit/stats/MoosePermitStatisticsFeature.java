package fi.riista.feature.huntingclub.permit.stats;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.sql.JPASQLQuery;
import com.querydsl.sql.SQLTemplates;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.gis.hta.QGISHirvitalousalue;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.QHuntingClub;
import fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummaryDTO;
import fi.riista.feature.huntingclub.permit.harvestreport.QMooseHarvestReport;
import fi.riista.feature.huntingclub.permit.summary.ClubHuntingSummaryService;
import fi.riista.feature.organization.QRiistakeskuksenAlue;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.security.EntityPermission;
import fi.riista.sql.SQHta;
import fi.riista.sql.SQOrganisation;
import fi.riista.util.F;
import fi.riista.util.GISUtils;
import fi.riista.util.LocalisedString;
import javaslang.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.querydsl.core.group.GroupBy.groupBy;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingDouble;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
public class MoosePermitStatisticsFeature {

    private static final Logger LOG = LoggerFactory.getLogger(MoosePermitStatisticsFeature.class);

    @Resource
    private ClubHuntingSummaryService clubHuntingSummaryService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Resource
    private SQLTemplates sqlTemplates;

    @PersistenceContext
    private EntityManager em;

    private <T> JPASQLQuery<T> createNativeQuery() {
        return new JPASQLQuery<>(em, sqlTemplates);
    }

    @Transactional(readOnly = true)
    public List<OrgList> listOrganisations(final long rhyId, final Locale locale) {
        final Riistanhoitoyhdistys viewedRhy = requireEntityService.requireRiistanhoitoyhdistys(rhyId, EntityPermission.READ);

        final String viewedRhyHtaOfficialCode = findHta(rhyId);

        final QGISHirvitalousalue hta = QGISHirvitalousalue.gISHirvitalousalue;
        final List<OrgList.Org> htas = listOrgs(locale, viewedRhyHtaOfficialCode, hta, hta.number, hta.nameFinnish, hta.nameSwedish);

        final QRiistakeskuksenAlue rka = QRiistakeskuksenAlue.riistakeskuksenAlue;
        final List<OrgList.Org> rkas = listOrgs(locale, viewedRhy.getParentOrganisation().getOfficialCode(), rka, rka.officialCode, rka.nameFinnish, rka.nameSwedish);

        final QRiistanhoitoyhdistys rhy = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
        final List<OrgList.Org> rhys = listOrgs(locale, viewedRhy.getOfficialCode(), rhy, rhy.officialCode, rhy.nameFinnish, rhy.nameSwedish);

        return Arrays.asList(new OrgList(OrgType.RHY, rhys), new OrgList(OrgType.HTA, htas), new OrgList(OrgType.RKA, rkas));
    }

    private List<OrgList.Org> listOrgs(final Locale locale,
                                       final String viewedRhyHtaOfficialCode,
                                       final EntityPathBase<?> hta,
                                       final StringPath number,
                                       final StringPath nameFinnish,
                                       final StringPath nameSwedish) {
        return jpqlQueryFactory.select(number, nameFinnish, nameSwedish)
                .from(hta)
                .fetch()
                .stream()
                .map(t -> {
                    final String name = LocalisedString.of(t.get(nameFinnish), t.get(nameSwedish))
                            .getAnyTranslation(locale);
                    final String officialCode = t.get(number);
                    return new OrgList.Org(officialCode, name, Objects.equals(officialCode, viewedRhyHtaOfficialCode));
                })
                .sorted(comparing(o -> o.name))
                .collect(toList());
    }

    private String findHta(final long rhyId) {
        final SQOrganisation org = SQOrganisation.organisation;
        final SQHta hta = SQHta.hta;
        final List<String> htas = createNativeQuery().select(hta.numero)
                .from(org)
                .join(hta).on(hta.geom.intersects(GISUtils.createPointWithDefaultSRID(org.longitude, org.latitude)))
                .where(org.organisationId.eq(rhyId),
                        org.latitude.isNotNull(),
                        org.longitude.isNotNull())
                .fetch();

        if (htas.size() == 1) {
            return htas.get(0);
        }
        LOG.warn("Could not resolve HTA for rhyId:" + rhyId + ", htas:" + htas);
        return htas.isEmpty() ? null : htas.get(0);
    }

    public enum OrgType {
        RHY, RKA, HTA
    }

    public static class OrgList {
        public final OrgType type;
        public final List<Org> organisations;

        public OrgList(OrgType type, List<Org> organisations) {
            this.type = type;
            this.organisations = organisations;
        }

        static class Org {
            public final String officialCode;
            public final String name;
            public final boolean selected;

            public Org(final String officialCode, final String name, final boolean selected) {
                this.officialCode = officialCode;
                this.name = name;
                this.selected = selected;
            }
        }
    }

    @Transactional(readOnly = true)
    public List<MoosePermitStatisticsDTO> calculateByPartner(
            final Locale locale, final int speciesCode, final int huntingYear, final List<HarvestPermit> permits) {

        final Map<Long, Double> permitAmounts = findPermitAmounts(permits, speciesCode, huntingYear);
        final Map<Long, Boolean> permitIdToPermitLocked =
                getPermitIdToMooseHarvestReportExistsMapping(permits, speciesCode, huntingYear);

        return permits.stream()
                .flatMap(permit -> {
                    final Map<Long, BasicClubHuntingSummaryDTO> partnerSummaries =
                            clubHuntingSummaryService.getHuntingSummariesForModeration(permit, speciesCode).stream()
                                    .collect(toMap(BasicClubHuntingSummaryDTO::getClubId, Function.identity()));

                    final Set<HuntingClub> partners = permit.getPermitPartners();
                    return partners.stream()
                            .map(partner -> Tuple.of(permit, partner, partnerSummaries.get(partner.getId())));
                })
                .map(t -> {
                    final HarvestPermit permit = t._1;
                    final HuntingClub club = t._2;
                    final List<BasicClubHuntingSummaryDTO> summaries = singletonList(t._3);

                    return createStatisticsDto(permitAmounts, permitIdToPermitLocked, permit, club, summaries, false);
                })
                .sorted(getSort(locale))
                .collect(toList());
    }

    @Transactional(readOnly = true)
    public List<MoosePermitStatisticsDTO> calculateByHolder(final long rhyId, final Locale locale, final int speciesCode,
                                                            final int huntingYear, final OrgType orgType, final String orgCode) {

        requireEntityService.requireRiistanhoitoyhdistys(rhyId, EntityPermission.READ);

        final List<HarvestPermit> permits = findPermits(orgType, orgCode, speciesCode, huntingYear);
        final List<MoosePermitStatisticsDTO> stats = calculateByHolder(locale, speciesCode, huntingYear, permits);
        return F.concat(singletonList(createTotal(stats)), stats);
    }

    private List<HarvestPermit> findPermits(final OrgType orgType, final String orgCode,
                                            final int speciesCode, final int huntingYear) {

        final QHarvestPermit permit = QHarvestPermit.harvestPermit;
        final QHarvestPermitSpeciesAmount speciesAmount = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final QGameSpecies gameSpecies = QGameSpecies.gameSpecies;
        final QHuntingClub permitHolder = QHuntingClub.huntingClub;

        final BooleanExpression orgPredicate = findOrganisationPredicate(orgType, orgCode, permit);
        return jpqlQueryFactory.selectFrom(permit)
                .join(permit.permitHolder, permitHolder).fetchJoin()
                .join(permit.speciesAmounts, speciesAmount)
                .join(speciesAmount.gameSpecies, gameSpecies)
                .where(orgPredicate,
                        permit.permitTypeCode.eq(HarvestPermit.MOOSELIKE_PERMIT_TYPE),
                        gameSpecies.officialCode.eq(speciesCode),
                        speciesAmount.validOnHuntingYear(huntingYear))
                .fetch();
    }

    private List<MoosePermitStatisticsDTO> calculateByHolder(
            final Locale locale, final int speciesCode, final int huntingYear, final List<HarvestPermit> permits) {

        final Map<Long, Double> permitAmounts = findPermitAmounts(permits, speciesCode, huntingYear);
        final Map<Long, Boolean> permitIdToPermitLocked =
                getPermitIdToMooseHarvestReportExistsMapping(permits, speciesCode, huntingYear);

        return permits.stream()
                .map(permit -> {
                    final HuntingClub permitHolder = permit.getPermitHolder();
                    final List<BasicClubHuntingSummaryDTO> basicHuntingSummaries =
                            clubHuntingSummaryService.getHuntingSummariesForModeration(permit, speciesCode);
                    return createStatisticsDto(permitAmounts, permitIdToPermitLocked, permit, permitHolder, basicHuntingSummaries, true);
                })
                .sorted(getSort(locale))
                .collect(toList());
    }

    private static MoosePermitStatisticsDTO createStatisticsDto(final Map<Long, Double> permitAmounts,
                                                                final Map<Long, Boolean> permitIdToPermitLocked,
                                                                final HarvestPermit permit,
                                                                final HuntingClub permitHolder,
                                                                final List<BasicClubHuntingSummaryDTO> basicHuntingSummaries,
                                                                final boolean limitToPermitAreaSize) {

        final double permitAmount = permitAmounts.getOrDefault(permit.getId(), 0.0);
        final LocalisedString permitHolderName = permitHolder.getNameLocalisation();
        final String permitHolderOfficialCode = permitHolder.getOfficialCode();
        final boolean locked = permitIdToPermitLocked.get(permit.getId());
        final MoosePermitStatisticsCount harvestCount = MoosePermitStatisticsCount.create(
                permit.getPermitAreaSize(), limitToPermitAreaSize, basicHuntingSummaries);

        return new MoosePermitStatisticsDTO()
                .withPermitNumber(permit.getPermitNumber())
                .withPermitHolderName(permitHolderName)
                .withPermitHolderOfficialCode(permitHolderOfficialCode)
                .withHuntingFinished(locked)
                .withPermitAmount(permitAmount)
                .withHarvestCount(harvestCount);
    }

    private static Comparator<MoosePermitStatisticsDTO> getSort(Locale locale) {
        return Comparator.<MoosePermitStatisticsDTO, String> comparing(MoosePermitStatisticsDTO::getPermitNumber)
                .thenComparing(comparing(a -> a.getPermitHolderLocalisedString().getAnyTranslation(locale)));
    }


    private static BooleanExpression findOrganisationPredicate(final OrgType orgType, final String orgCode, final QHarvestPermit permit) {
        final QRiistanhoitoyhdistys rhy = QRiistanhoitoyhdistys.riistanhoitoyhdistys;

        switch (orgType) {
            case RHY:
                return permit.rhy.id.eq(JPAExpressions.select(rhy.id).from(rhy).where(rhy.officialCode.eq(orgCode)));
            case RKA:
                final QRiistakeskuksenAlue rka = QRiistakeskuksenAlue.riistakeskuksenAlue;
                return permit.rhy.id.in(JPAExpressions.select(rhy.id)
                        .from(rka)
                        .join(rka.subOrganisations, rhy._super)
                        .where(rka.officialCode.eq(orgCode)));
            case HTA:
                final QGISHirvitalousalue hta = QGISHirvitalousalue.gISHirvitalousalue;
                return permit.mooseArea.id.eq(JPAExpressions.select(hta.id).from(hta).where(hta.number.eq(orgCode)));
            default:
                // Fall-through to throwing exception.
        }
        throw new IllegalStateException("Unknown orgType: " + orgType);
    }

    private Map<Long, Double> findPermitAmounts(final List<HarvestPermit> permits,
                                                final int speciesCode,
                                                final int huntingYear) {
        final Map<Long, Float> amounts = findAmountsByPermitId(permits, speciesCode, huntingYear);
        final Map<Long, Float> amendmentAmounts = findAmendmentAmountsByOriginalPermitId(permits, speciesCode, huntingYear);

        return Stream.concat(amounts.entrySet().stream(), amendmentAmounts.entrySet().stream())
                .collect(groupingBy(Map.Entry::getKey, summingDouble(Map.Entry::getValue)));
    }

    private Map<Long, Float> findAmountsByPermitId(final List<HarvestPermit> permits,
                                                   final int speciesCode,
                                                   final int huntingYear) {

        final QHarvestPermitSpeciesAmount speciesAmount = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final QGameSpecies species = QGameSpecies.gameSpecies;

        return jpqlQueryFactory.select(speciesAmount.harvestPermit.id, speciesAmount.amount)
                .from(speciesAmount)
                .join(speciesAmount.gameSpecies, species)
                .where(speciesAmount.harvestPermit.in(permits),
                        species.officialCode.eq(speciesCode),
                        speciesAmount.validOnHuntingYear(huntingYear))
                .transform(groupBy(speciesAmount.harvestPermit.id).as(speciesAmount.amount));
    }

    private Map<Long, Float> findAmendmentAmountsByOriginalPermitId(final List<HarvestPermit> originalPermits,
                                                                    final int speciesCode,
                                                                    final int huntingYear) {

        final QHarvestPermitSpeciesAmount speciesAmount = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final QGameSpecies species = QGameSpecies.gameSpecies;
        final QHarvestPermit amendmentPermit = new QHarvestPermit("amendmentPermit");

        final NumberPath<Long> originalPermitId = amendmentPermit.originalPermit.id;
        final NumberExpression<Float> sumOfAmendmentPermitAmounts = speciesAmount.amount.sum();

        return jpqlQueryFactory.select(originalPermitId, sumOfAmendmentPermitAmounts)
                .from(speciesAmount)
                .join(speciesAmount.gameSpecies, species)
                .join(speciesAmount.harvestPermit, amendmentPermit)
                .where(amendmentPermit.originalPermit.in(originalPermits),
                        species.officialCode.eq(speciesCode),
                        speciesAmount.validOnHuntingYear(huntingYear))
                .groupBy(originalPermitId)
                .transform(groupBy(originalPermitId).as(sumOfAmendmentPermitAmounts));
    }

    private Map<Long, Boolean> getPermitIdToMooseHarvestReportExistsMapping(final List<HarvestPermit> permits,
                                                                            final int speciesCode,
                                                                            final int huntingYear) {
        final QHarvestPermitSpeciesAmount speciesAmount = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final QMooseHarvestReport report = QMooseHarvestReport.mooseHarvestReport;
        final QGameSpecies species = QGameSpecies.gameSpecies;

        final JPQLQuery<Object> mooseHarvestReportQuery = jpqlQueryFactory.select(Expressions.nullExpression())
                .from(report)
                .where(report.speciesAmount.eq(speciesAmount));

        final Expression<Boolean> mooseHarvestReportExists = new CaseBuilder()
                .when(mooseHarvestReportQuery.exists()).then(Expressions.constant(true))
                .otherwise(Expressions.constant(false));

        return jpqlQueryFactory.select(speciesAmount.harvestPermit.id, mooseHarvestReportExists)
                .from(speciesAmount)
                .join(speciesAmount.gameSpecies, species)
                .where(speciesAmount.harvestPermit.in(permits),
                        species.officialCode.eq(speciesCode),
                        speciesAmount.validOnHuntingYear(huntingYear))
                .transform(groupBy(speciesAmount.harvestPermit.id).as(mooseHarvestReportExists));
    }

    private static MoosePermitStatisticsDTO createTotal(final List<MoosePermitStatisticsDTO> stats) {

        double permitAmount = stats.stream().mapToDouble(MoosePermitStatisticsDTO::getPermitAmount).sum();

        final int totalPermitAreaSize = stats.stream().mapToInt(s -> s.getHarvestCount().getTotalAreaSize()).sum();
        final int effectiveAreaSize = stats.stream().mapToInt(s -> s.getHarvestCount().getEffectiveAreaSize()).sum();
        final int remainingPopulationInTotalArea = stats.stream().mapToInt(s -> s.getHarvestCount().getRemainingPopulationInTotalArea()).sum();
        final int remainingPopulationInEffectiveArea = stats.stream().mapToInt(s -> s.getHarvestCount().getRemainingPopulationInEffectiveArea()).sum();
        final int adultMales = stats.stream().mapToInt(s -> s.getHarvestCount().getAdultMales()).sum();
        final int adultFemales = stats.stream().mapToInt(s -> s.getHarvestCount().getAdultFemales()).sum();
        final int youngMales = stats.stream().mapToInt(s -> s.getHarvestCount().getYoungMales()).sum();
        final int youngFemales = stats.stream().mapToInt(s -> s.getHarvestCount().getYoungFemales()).sum();
        final int adultsNonEdible = stats.stream().mapToInt(s -> s.getHarvestCount().getAdultsNonEdible()).sum();
        final int youngNonEdible = stats.stream().mapToInt(s -> s.getHarvestCount().getYoungNonEdible()).sum();

        final MoosePermitStatisticsCount harvestCount = new MoosePermitStatisticsCount(
                totalPermitAreaSize,
                effectiveAreaSize,
                remainingPopulationInTotalArea,
                remainingPopulationInEffectiveArea,
                adultMales, adultFemales, youngMales, youngFemales, adultsNonEdible, youngNonEdible);
        return new MoosePermitStatisticsDTO()
                .withPermitAmount(permitAmount)
                .withHarvestCount(harvestCount);
    }

}

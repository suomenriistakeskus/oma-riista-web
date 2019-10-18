package fi.riista.integration.common.export;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.querydsl.core.Tuple;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.huntingclub.QHuntingClub;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.permit.decision.QPermitDecision;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType;
import fi.riista.feature.permit.decision.derogation.QPermitDecisionDerogationReason;
import fi.riista.integration.common.export.permits.CPER_GeoLocation;
import fi.riista.integration.common.export.permits.CPER_Permit;
import fi.riista.integration.common.export.permits.CPER_PermitPartner;
import fi.riista.integration.common.export.permits.CPER_PermitSpeciesAmount;
import fi.riista.integration.common.export.permits.CPER_Permits;
import fi.riista.integration.common.export.permits.CPER_ValidityTimeInterval;
import fi.riista.util.F;
import fi.riista.util.JaxbUtils;
import org.joda.time.LocalDate;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.types.dsl.Expressions.numberPath;
import static fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount.RestrictionType.AE;
import static fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount.RestrictionType.AU;
import static fi.riista.integration.common.export.RvrConstants.RVR_PERMIT_TYPE_CODES;
import static fi.riista.util.Collect.entriesToMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
public class CommonHarvestPermitExportFeature {

    private static final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
    private static final QHarvestPermit ORIGINAL_PERMIT = new QHarvestPermit("originalPermit");
    private static final QHuntingClub CLUB = QHuntingClub.huntingClub;
    private static final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
    private static final QHarvestPermitSpeciesAmount SPA = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
    private static final QGameSpecies SPECIES = QGameSpecies.gameSpecies;
    private static final QPermitDecision DECISION = QPermitDecision.permitDecision;
    private static final QPermitDecisionDerogationReason DEROGATION_REASON =
            QPermitDecisionDerogationReason.permitDecisionDerogationReason;
    private static final int PAGE_SIZE = 4096;

    @Resource
    private JPQLQueryFactory queryFactory;

    @Resource(name = "commonHarvestPermitExportMarshaller")
    private Jaxb2Marshaller jaxbMarshaller;


    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_RVR_COMMON')")
    public String exportPermitsAsXml(final int year) {
        return JaxbUtils.marshalToString(exportPermits(year), jaxbMarshaller);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_RVR_COMMON')")
    public CPER_Permits exportPermits(final int year) {

        final List<Tuple> permits = findPermits(year);
        final List<Long> permitIds = F.mapNonNullsToList(permits, t -> t.get(PERMIT.id));


        final Collection<CPER_Permit> permitPojos = mapPermits(permits, permitIds);
        final Collection<CPER_PermitPartner> partners = mapPartners(permitIds);
        final Collection<CPER_PermitSpeciesAmount> speciesAmounts = findSpeciesAmounts(permitIds);

        return new CPER_Permits()
                .withPermit(permitPojos)
                .withPermitPartner(partners)
                .withPermitSpeciesAmount(speciesAmounts);
    }

    // PERMITS

    private List<Tuple> findPermits(final int year) {
        return queryFactory.select(
                PERMIT.id,
                PERMIT.permitNumber,
                PERMIT.permitYear,
                PERMIT.permitType,
                PERMIT.permitTypeCode,
                ORIGINAL_PERMIT.permitNumber,
                RHY.officialCode,
                CLUB.geoLocation.latitude.coalesce(RHY.geoLocation.latitude).as("lat"),
                CLUB.geoLocation.longitude.coalesce(RHY.geoLocation.longitude).as("lon"),
                PERMIT.harvestReportState)
                .from(PERMIT)
                .leftJoin(PERMIT.huntingClub, CLUB)
                .innerJoin(PERMIT.rhy, RHY)
                .leftJoin(PERMIT.originalPermit, ORIGINAL_PERMIT)
                .where(PERMIT.permitTypeCode.in(RVR_PERMIT_TYPE_CODES)
                        .and(PERMIT.permitYear.eq(year)))
                .fetch();
    }

    private Collection<CPER_Permit> mapPermits(final Collection<Tuple> permits, final List<Long> permitIds) {

        final NumberExpression<Long> finished = new CaseBuilder()
                .when(SPA.mooselikeHuntingFinished.isTrue()).then(1L).otherwise(0L);

        final Map<Long, Boolean> finishedMap = Lists.partition(permitIds, PAGE_SIZE)
                .stream()
                .flatMap(partition ->
                        queryFactory.from(SPA)
                                .groupBy(SPA.harvestPermit.id)
                                .select(SPA.harvestPermit.id,
                                        SPA.mooselikeHuntingFinished.count().as("speciesCount"),
                                        finished.sum().as("finishedSpeciesCount"))
                                .where(SPA.harvestPermit.id.in(partition))
                                .fetch()
                                .stream()
                ).collect(toMap(
                        keyTuple -> keyTuple.get(SPA.harvestPermit.id),
                        valueTuple ->
                                valueTuple.get(numberPath(Long.class, "speciesCount")).equals(
                                        valueTuple.get(numberPath(Long.class, "finishedSpeciesCount")))));

        Map<Long, Set<PermitDecisionDerogationReasonType>> reasonMap = fetchReasons(permitIds);

        return permits
                .stream()
                .map(t -> mapPermitToPojo(t, finishedMap, reasonMap))
                .collect(toList());

    }

    private Map<Long, Set<PermitDecisionDerogationReasonType>> fetchReasons(final List<Long> permitIds) {
        return Lists.partition(permitIds, PAGE_SIZE)
                .stream()
                .flatMap(partition -> {
                    final Map<Long, Long> decisionToPermit = queryFactory
                            .select(PERMIT.id, DECISION.id)
                            .from(PERMIT)
                            .innerJoin(PERMIT.permitDecision, DECISION)
                            .transform(groupBy(DECISION.id).as(PERMIT.id));

                    final Map<Long, Set<PermitDecisionDerogationReasonType>> decisionToReasons = queryFactory
                            .select(DEROGATION_REASON.reasonType, DECISION.id)
                            .from(DEROGATION_REASON)
                            .innerJoin(DEROGATION_REASON.permitDecision, DECISION)
                            .where(DECISION.id.in(decisionToPermit.keySet()))
                            .transform(groupBy(DECISION.id).as(GroupBy.set(DEROGATION_REASON.reasonType)));

                    // Transform map to permit id -> set of reasons
                    return decisionToPermit.entrySet()
                            .stream()
                            .collect(Collectors.toMap(e -> e.getValue(),
                                    e -> decisionToReasons.getOrDefault(e.getKey(), ImmutableSet.of())))
                            .entrySet().stream();
                })
                .collect(entriesToMap());
    }

    private CPER_Permit mapPermitToPojo(final Tuple tuple,
                                        final Map<Long, Boolean> finishedMap,
                                        final Map<Long, Set<PermitDecisionDerogationReasonType>> reasonMap) {
        return new CPER_Permit()
                .withPermitNumber(tuple.get(PERMIT.permitNumber))
                .withPermitYear(tuple.get(PERMIT.permitYear))
                .withRhyOfficialCode(tuple.get(RHY.officialCode))
                .withGeoLocation(new CPER_GeoLocation()
                        .withLatitude(tuple.get(numberPath(Integer.class, "lat")))
                        .withLongitude(tuple.get(numberPath(Integer.class, "lon"))))
                .withOriginalPermitNumber(tuple.get(ORIGINAL_PERMIT.permitNumber))
                .withDerogationReasons(getReasons(tuple, reasonMap))
                .withPermitDisplayName(tuple.get(PERMIT.permitType))
                .withHuntingFinished(resolveHuntingFinished(tuple, finishedMap));
    }

    private Set<String> getReasons(final Tuple tuple,
                                   final Map<Long, Set<PermitDecisionDerogationReasonType>> reasonMap) {
        return F.mapNonNullsToSet(reasonMap.getOrDefault(tuple.get(PERMIT.id), ImmutableSet.of()),
                PermitDecisionDerogationReasonType::getHabidesCodeForCarnivore);
    }

    private boolean resolveHuntingFinished(final Tuple tuple, final Map<Long, Boolean> finishedMap) {
        HarvestReportState reportState = tuple.get(PERMIT.harvestReportState);
        return HarvestReportState.APPROVED == reportState ||
                finishedMap.getOrDefault(tuple.get(PERMIT.id), false);
    }

    // PARTNERS

    private Collection<CPER_PermitPartner> mapPartners(final List<Long> permitIds) {
        return Lists.partition(permitIds, PAGE_SIZE).stream().flatMap(list ->
                queryFactory.select(PERMIT.id,
                        PERMIT.permitNumber,
                        CLUB.officialCode,
                        CLUB.nameFinnish,
                        CLUB.geoLocation.latitude,
                        CLUB.geoLocation.longitude,
                        RHY.officialCode)
                        .from(PERMIT)
                        .innerJoin(PERMIT.rhy, RHY)
                        .innerJoin(PERMIT.permitPartners, CLUB)
                        .where(PERMIT.id.in(list))
                        .fetch()
                        .stream()
                        .map(this::mapPartnerToPojo))
                .collect(Collectors.toList());

    }

    private CPER_PermitPartner mapPartnerToPojo(final Tuple tuple) {
        return new CPER_PermitPartner()
                .withPermitNumber(tuple.get(PERMIT.permitNumber))
                .withNameFinnish(tuple.get(CLUB.nameFinnish))
                .withClubOfficialCode(tuple.get(CLUB.officialCode))
                .withGeoLocation(createGeoLocationNullable(
                        tuple.get(CLUB.geoLocation.latitude),
                        tuple.get(CLUB.geoLocation.longitude)))
                .withRhyOfficialCode(tuple.get(RHY.officialCode));
    }

    private static CPER_GeoLocation createGeoLocationNullable(final Integer latitude,
                                                              final Integer longitude) {

        if (latitude != null && longitude != null) {
            return new CPER_GeoLocation()
                    .withLatitude(latitude)
                    .withLongitude(longitude);
        }
        return null;
    }

    // SPECIES AMOUNTS

    private Collection<CPER_PermitSpeciesAmount> findSpeciesAmounts(final List<Long> permitIds) {

        return Lists.partition(permitIds, PAGE_SIZE).stream().flatMap(list ->
                queryFactory.select(PERMIT.id,
                        PERMIT.permitNumber,
                        SPA.gameSpecies.officialCode,
                        SPA.beginDate,
                        SPA.endDate,
                        SPA.beginDate2,
                        SPA.endDate2,
                        SPA.amount,
                        SPA.restrictionAmount,
                        SPA.restrictionType)
                        .from(SPA)
                        .innerJoin(SPA.harvestPermit, PERMIT)
                        .innerJoin(SPA.gameSpecies, SPECIES)
                        .where(PERMIT.id.in(list))
                        .fetch()
                        .stream()
                        .map(tuple -> mapSpeciesAmountToPojo(tuple)))
                .collect(toList());

    }

    private CPER_PermitSpeciesAmount mapSpeciesAmountToPojo(final Tuple tuple) {
        final CPER_PermitSpeciesAmount amount = new CPER_PermitSpeciesAmount()
                .withPermitNumber(tuple.get(PERMIT.permitNumber))
                .withGameSpeciesCode(tuple.get(SPA.gameSpecies.officialCode))
                .withAmount(tuple.get(SPA.amount))
                .withRestrictedAmountAdult(mapRestriction(AE, tuple))
                .withRestrictedAmountAdultMale(mapRestriction(AU, tuple));

        return addValidityPeriods(amount, tuple);
    }

    private CPER_PermitSpeciesAmount addValidityPeriods(final CPER_PermitSpeciesAmount amount, final Tuple tuple) {
        final LocalDate begin = tuple.get(SPA.beginDate);
        final LocalDate end = tuple.get(SPA.endDate);
        final LocalDate begin2 = tuple.get(SPA.beginDate2);
        final LocalDate end2 = tuple.get(SPA.endDate2);
        amount.withValidityPeriod(new CPER_ValidityTimeInterval().withBeginDate(begin).withEndDate(end));
        if (begin2 != null && end2 != null) {
            amount.withValidityPeriod(new CPER_ValidityTimeInterval().withBeginDate(begin2).withEndDate(end2));
        }
        return amount;
    }

    private Float mapRestriction(final HarvestPermitSpeciesAmount.RestrictionType type, final Tuple tuple) {
        if (tuple.get(SPA.restrictionType) == type) {
            return tuple.get(SPA.restrictionAmount);
        }
        return null;
    }

}

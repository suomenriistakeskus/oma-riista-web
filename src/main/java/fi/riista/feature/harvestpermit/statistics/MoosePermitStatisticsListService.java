package fi.riista.feature.harvestpermit.statistics;

import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.gis.hta.QGISHirvitalousalue;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.huntingclub.QHuntingClub;
import fi.riista.feature.organization.QRiistakeskuksenAlue;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.permit.application.QHarvestPermitApplication;
import fi.riista.feature.permit.application.QHarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.decision.QPermitDecision;
import fi.riista.util.LocalisedString;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;

import static fi.riista.util.Collect.indexingBy;

@Service
public class MoosePermitStatisticsListService {
    private static final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public Map<Long, MoosePermitStatisticsPermitInfo> findPermits(final int speciesCode, final int huntingYear) {
        return findPermitsInternal(speciesCode, huntingYear, null);
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public Map<Long, MoosePermitStatisticsPermitInfo> findPermits(final Set<Long> harvestPermitIds, final int speciesCode, final int huntingYear) {
        return findPermitsInternal(speciesCode, huntingYear, PERMIT.id.in(harvestPermitIds));
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public Map<Long, MoosePermitStatisticsPermitInfo> findPermitsByRhy(final String rhyCode, final int speciesCode, final int huntingYear) {
        return findPermitsInternal(speciesCode, huntingYear, permitRhyPredicate(rhyCode));
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public Map<Long, MoosePermitStatisticsPermitInfo> findPermitsByRka(final String rkaCode, final int speciesCode, final int huntingYear) {
        return findPermitsInternal(speciesCode, huntingYear, permitRkaPredicate(rkaCode));
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public Map<Long, MoosePermitStatisticsPermitInfo> findPermitsByHta(final String htaCode, final int speciesCode, final int huntingYear) {
        return findPermitsInternal(speciesCode, huntingYear, permitHtaPredicate(htaCode));
    }

    private Map<Long, MoosePermitStatisticsPermitInfo> findPermitsInternal(final int speciesCode,
                                                                           final int huntingYear,
                                                                           final BooleanExpression extraPermitPredicate) {
        final QGameSpecies SPECIES = QGameSpecies.gameSpecies;
        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
        final QRiistakeskuksenAlue RKA = QRiistakeskuksenAlue.riistakeskuksenAlue;
        final QGISHirvitalousalue HTA = QGISHirvitalousalue.gISHirvitalousalue;
        final QHuntingClub CLUB = QHuntingClub.huntingClub;
        final QPerson PERSON = QPerson.person;
        final QHarvestPermitApplicationSpeciesAmount APPLICATION_SPECIES_AMOUNT =
                QHarvestPermitApplicationSpeciesAmount.harvestPermitApplicationSpeciesAmount;
        final QPermitDecision DECISION = QPermitDecision.permitDecision;
        final QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;

        final BooleanExpression huntingYearPredicate = PERMIT.permitYear.eq(huntingYear);
        final BooleanExpression speciesPredicate = APPLICATION_SPECIES_AMOUNT.gameSpecies.eq(JPAExpressions
                .selectFrom(SPECIES)
                .where(SPECIES.officialCode.eq(speciesCode)));

        final Map<Long, Integer> partnerCountMapping = jpqlQueryFactory.select(PERMIT.id, CLUB.count().intValue())
                .from(PERMIT)
                .join(PERMIT.permitDecision, DECISION)
                .join(DECISION.application, APPLICATION)
                .join(APPLICATION.speciesAmounts, APPLICATION_SPECIES_AMOUNT)
                .join(PERMIT.permitPartners, CLUB)
                .where(speciesPredicate, huntingYearPredicate, extraPermitPredicate)
                .groupBy(PERMIT.id)
                .transform(GroupBy.groupBy(PERMIT.id).as(CLUB.count().intValue()));

        return jpqlQueryFactory
                .select(PERMIT.id, PERMIT.permitNumber, PERMIT.permitAreaSize,
                        CLUB.officialCode, CLUB.nameFinnish, CLUB.nameSwedish, PERSON.firstName, PERSON.lastName,
                        RHY.id, RKA.id, HTA.id)
                .from(PERMIT)
                .join(PERMIT.permitDecision, DECISION)
                .join(DECISION.application, APPLICATION)
                .join(APPLICATION.speciesAmounts, APPLICATION_SPECIES_AMOUNT)
                .join(PERMIT.originalContactPerson, PERSON)
                .join(PERMIT.rhy, RHY)
                .join(RHY.parentOrganisation, RKA._super)
                .leftJoin(PERMIT.mooseArea, HTA)
                .leftJoin(PERMIT.huntingClub, CLUB)
                .where(PERMIT.isMooselikePermit(),
                        speciesPredicate,
                        huntingYearPredicate,
                        extraPermitPredicate)
                .fetch().stream().map(tuple -> {
                    final Long permitId = tuple.get(PERMIT.id);
                    final String permitNumber = tuple.get(PERMIT.permitNumber);

                    final String clubOfficialCode = tuple.get(CLUB.officialCode);
                    final String clubNameFinnish = tuple.get(CLUB.nameFinnish);
                    final String clubNameSwedish = tuple.get(CLUB.nameSwedish);
                    final String firstName = tuple.get(PERSON.firstName);
                    final String lastName = tuple.get(PERSON.lastName);

                    final LocalisedString permitHolderName;
                    final String permitHolderOfficialCode;

                    if (clubOfficialCode != null) {
                        permitHolderName = LocalisedString.of(clubNameFinnish, clubNameSwedish);
                        permitHolderOfficialCode = clubOfficialCode;
                    } else {
                        permitHolderName = LocalisedString.of(String.format("%s %s", firstName, lastName));
                        permitHolderOfficialCode = "";
                    }

                    final int partnerCount = partnerCountMapping.getOrDefault(permitId, 0);
                    final Integer permitAreaSize = tuple.get(PERMIT.permitAreaSize);
                    final Long rhyId = tuple.get(RHY.id);
                    final Long rkaId = tuple.get(RKA.id);
                    final Integer mooseAreaId = tuple.get(HTA.id);

                    return new MoosePermitStatisticsPermitInfo(permitId, permitNumber, permitHolderName,
                            permitHolderOfficialCode, partnerCount, permitAreaSize, rhyId, rkaId, mooseAreaId);

                }).collect(indexingBy(MoosePermitStatisticsPermitInfo::getPermitId));
    }

    private static BooleanExpression permitRhyPredicate(final String orgCode) {
        final QRiistanhoitoyhdistys RHY = new QRiistanhoitoyhdistys("sub_rhy");

        return PERMIT.rhy.id.eq(JPAExpressions.select(RHY.id)
                .from(RHY)
                .where(RHY.officialCode.eq(orgCode)));
    }

    private static BooleanExpression permitRkaPredicate(final String orgCode) {
        final QRiistanhoitoyhdistys RHY = new QRiistanhoitoyhdistys("sub_rhy");
        final QRiistakeskuksenAlue RKA = new QRiistakeskuksenAlue("sub_rka");

        return PERMIT.rhy.id.in(JPAExpressions.select(RHY.id)
                .from(RHY)
                .join(RHY.parentOrganisation, RKA._super)
                .where(RKA.officialCode.eq(orgCode)));
    }

    private static BooleanExpression permitHtaPredicate(final String orgCode) {
        final QGISHirvitalousalue HTA = new QGISHirvitalousalue("sub_hta");

        return PERMIT.mooseArea.id.eq(JPAExpressions.select(HTA.id)
                .from(HTA)
                .where(HTA.number.eq(orgCode)));
    }
}

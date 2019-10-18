package fi.riista.feature.huntingclub.permit.statistics;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.sql.SQBasicClubHuntingSummary;
import fi.riista.sql.SQGameSpecies;
import fi.riista.sql.SQHarvestPermit;
import fi.riista.sql.SQHarvestPermitPartners;
import fi.riista.sql.SQHarvestPermitSpeciesAmount;
import fi.riista.sql.SQMooseHuntingSummary;
import fi.riista.sql.SQOrganisation;
import fi.riista.util.DateUtil;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;

@Component
public class ClubHuntingSummaryBasicInfoService {

    @Resource
    private SQLQueryFactory sqlQueryFactory;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Map<Long, ClubHuntingSummaryBasicInfoDTO> getHuntingSummariesGroupedByClubId(final @Nonnull HarvestPermit permit,
                                                                                        final int speciesCode) {
        return getHuntingSummaries(singleton(permit.getId()), speciesCode).indexByClubId(permit);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public ClubHuntingSummaryBasicInfoByPermitAndClub getHuntingSummaries(final @Nonnull Set<Long> permitIds,
                                                                          final int speciesCode) {
        if (permitIds.isEmpty()) {
            return new ClubHuntingSummaryBasicInfoByPermitAndClub(emptyMap());
        }

        final boolean isMoose = GameSpecies.isMoose(speciesCode);

        return new ClubHuntingSummaryBasicInfoByPermitAndClub(getSummaries(speciesCode, permitIds, isMoose));
    }

    private Map<PermitAndClubId, ClubHuntingSummaryBasicInfoDTO> getSummaries(final int speciesCode,
                                                                              final Collection<Long> permitIds,
                                                                              final boolean isMoose) {
        final SQHarvestPermit PERMIT = SQHarvestPermit.harvestPermit;
        final SQHarvestPermitSpeciesAmount SPECIES_AMOUNT = SQHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final SQOrganisation CLUB = SQOrganisation.organisation;
        final SQGameSpecies SPECIES = SQGameSpecies.gameSpecies;
        final SQHarvestPermitPartners PARTNER = SQHarvestPermitPartners.harvestPermitPartners;
        final SQMooseHuntingSummary MOOSE_SUMMARY = SQMooseHuntingSummary.mooseHuntingSummary;
        final SQBasicClubHuntingSummary BASIC_SUMMARY = SQBasicClubHuntingSummary.basicClubHuntingSummary;
        final SQOrganisation GROUP = SQOrganisation.organisation;

        final SQLQuery<Long> mooseDataCardGroupIds = SQLExpressions.select(GROUP.organisationId)
                .from(GROUP)
                .where(GROUP.fromMooseDataCard.eq(true)
                        .and(GROUP.parentOrganisationId.eq(PARTNER.organisationId))
                        .and(GROUP.harvestPermitId.eq(PERMIT.harvestPermitId)));

        final Expression<Boolean> mooseDataCardGroupExists = isMoose
                ? new CaseBuilder().when(mooseDataCardGroupIds.exists()).then(true).otherwise(false)
                : Expressions.constant(false);

        return sqlQueryFactory
                .from(PERMIT)
                .innerJoin(PERMIT._harvestPermitSpeciesAmountPermitFk, SPECIES_AMOUNT)
                .innerJoin(SPECIES_AMOUNT.harvestPermitSpeciesAmountGameSpeciesFk, SPECIES).on(
                        SPECIES.officialCode.eq(speciesCode))
                .innerJoin(PERMIT._harvestPermitPartnersHarvestPermitFk, PARTNER)
                .innerJoin(PARTNER.harvestPermitPartnersOrganisationFk, CLUB)
                .leftJoin(SPECIES_AMOUNT._basicClubHuntingSummarySpeciesAmountFk, BASIC_SUMMARY).on(
                        BASIC_SUMMARY.clubId.eq(PARTNER.organisationId)
                                .and(isMoose ? BASIC_SUMMARY.moderatorOverride.eq(true) : null))
                .leftJoin(PERMIT._mooseHuntingSummaryPermitFk, MOOSE_SUMMARY).on(
                        MOOSE_SUMMARY.clubId.eq(PARTNER.organisationId)
                                .and(ExpressionUtils.eqConst(SPECIES.officialCode, OFFICIAL_CODE_MOOSE)))
                .where(PERMIT.harvestPermitId.in(permitIds))
                .select(PERMIT.harvestPermitId, CLUB.organisationId,
                        BASIC_SUMMARY.huntingSummaryId, MOOSE_SUMMARY.mooseHuntingSummaryId,
                        BASIC_SUMMARY.consistencyVersion, BASIC_SUMMARY.moderatorOverride,
                        BASIC_SUMMARY.huntingFinished, MOOSE_SUMMARY.huntingFinished,
                        BASIC_SUMMARY.huntingEndDate, MOOSE_SUMMARY.huntingEndDate,
                        BASIC_SUMMARY.totalHuntingArea, MOOSE_SUMMARY.totalHuntingArea,
                        BASIC_SUMMARY.effectiveHuntingArea, MOOSE_SUMMARY.effectiveHuntingArea,
                        BASIC_SUMMARY.remainingPopulationInTotalArea, MOOSE_SUMMARY.moosesRemainingInTotalHuntingArea,
                        BASIC_SUMMARY.remainingPopulationInEffectiveArea, MOOSE_SUMMARY.moosesRemainingInEffectiveHuntingArea,
                        MOOSE_SUMMARY.effectiveHuntingAreaPercentage.doubleValue(),
                        mooseDataCardGroupExists)
                .fetch()
                .stream()
                .collect(Collectors.toMap(tuple -> {
                    final Long permitId = tuple.get(PERMIT.harvestPermitId);
                    final Long clubId = tuple.get(CLUB.organisationId);

                    return new PermitAndClubId(permitId, clubId);
                }, tuple -> {
                    final Long clubId = tuple.get(CLUB.organisationId);
                    final Long permitId = tuple.get(PERMIT.harvestPermitId);

                    if (tuple.get(BASIC_SUMMARY.huntingSummaryId) != null) {
                        return new ClubHuntingSummaryBasicInfoDTO(permitId, clubId, speciesCode,
                                tuple.get(BASIC_SUMMARY.huntingSummaryId), tuple.get(BASIC_SUMMARY.consistencyVersion),
                                Boolean.TRUE.equals(tuple.get(BASIC_SUMMARY.huntingFinished)),
                                DateUtil.toLocalDateNullSafe(tuple.get(BASIC_SUMMARY.huntingEndDate)),
                                Boolean.TRUE.equals(tuple.get(mooseDataCardGroupExists)),
                                Boolean.TRUE.equals(tuple.get(BASIC_SUMMARY.moderatorOverride)),
                                tuple.get(BASIC_SUMMARY.totalHuntingArea),
                                tuple.get(BASIC_SUMMARY.effectiveHuntingArea),
                                null,
                                tuple.get(BASIC_SUMMARY.remainingPopulationInTotalArea),
                                tuple.get(BASIC_SUMMARY.remainingPopulationInEffectiveArea));

                    } else if (tuple.get(MOOSE_SUMMARY.mooseHuntingSummaryId) != null) {
                        return new ClubHuntingSummaryBasicInfoDTO(permitId, clubId, speciesCode,
                                null, null,
                                Boolean.TRUE.equals(tuple.get(MOOSE_SUMMARY.huntingFinished)),
                                DateUtil.toLocalDateNullSafe(tuple.get(MOOSE_SUMMARY.huntingEndDate)),
                                Boolean.TRUE.equals(tuple.get(mooseDataCardGroupExists)),
                                false,
                                tuple.get(MOOSE_SUMMARY.totalHuntingArea),
                                tuple.get(MOOSE_SUMMARY.effectiveHuntingArea),
                                tuple.get(MOOSE_SUMMARY.effectiveHuntingAreaPercentage.doubleValue()),
                                tuple.get(MOOSE_SUMMARY.moosesRemainingInTotalHuntingArea),
                                tuple.get(MOOSE_SUMMARY.moosesRemainingInEffectiveHuntingArea));

                    } else {
                        return new ClubHuntingSummaryBasicInfoDTO(permitId, clubId, speciesCode,
                                null, null,
                                false,
                                null,
                                Boolean.TRUE.equals(tuple.get(mooseDataCardGroupExists)),
                                false,
                                null,
                                null,
                                null,
                                null,
                                null);
                    }
                }));
    }
}

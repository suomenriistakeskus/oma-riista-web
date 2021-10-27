package fi.riista.feature.harvestpermit;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.group.Group;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.api.pub.PublicCarnivorePermitDTO;
import fi.riista.feature.common.repository.BaseRepositoryImpl;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.harvestpermit.endofhunting.reminder.EndOfHuntingReminderDTO;
import fi.riista.feature.harvestpermit.endofhunting.reminder.EndOfHuntingReminderFeature.EndOfHuntingReminderType;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.permit.DocumentNumberUtil;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.application.QHarvestPermitApplication;
import fi.riista.feature.permit.decision.QPermitDecision;
import fi.riista.feature.permit.decision.species.QPermitDecisionSpeciesAmount;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
import static fi.riista.feature.common.decision.DecisionStatus.PUBLISHED;
import static fi.riista.feature.gamediary.GameSpecies.LARGE_CARNIVORES;
import static fi.riista.feature.permit.PermitTypeCode.ANNUAL_UNPROTECTED_BIRD;
import static fi.riista.feature.permit.PermitTypeCode.DEROGATION_PERMIT_CODES;
import static fi.riista.feature.permit.PermitTypeCode.NEST_REMOVAL_BASED;
import static fi.riista.feature.permit.decision.PermitDecision.DecisionType.HARVEST_PERMIT;
import static fi.riista.util.DateUtil.beginOfCalendarYear;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@Repository
public class HarvestPermitRepositoryImpl implements HarvestPermitRepositoryCustom {

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Override
    @Transactional(readOnly = true)
    public Slice<PublicCarnivorePermitDTO> findCarnivorePermits(
            final String permitNumber, final Integer speciesCode, final Integer calendarYear, final String rkaCode,
            final Pageable pageRequest) {

        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
        final QOrganisation RKA = QOrganisation.organisation;
        final QPermitDecision DECISION = QPermitDecision.permitDecision;
        final QPermitDecisionSpeciesAmount SPA = QPermitDecisionSpeciesAmount.permitDecisionSpeciesAmount;
        final QGameSpecies SPECIES = QGameSpecies.gameSpecies;

        final BooleanBuilder builder = buildBasePredicate(DECISION, SPECIES);

        if (permitNumber != null) {
            builder.and(DECISION.decisionNumber.eq(DocumentNumberUtil.extractOrderNumber(permitNumber)));
        }

        if (speciesCode != null) {
            builder.and(SPECIES.officialCode.eq(speciesCode));
        }

        if (calendarYear != null) {
            builder
                    .and(DECISION.publishDate.goe(beginOfCalendarYear(calendarYear)))
                    .and(DECISION.publishDate.lt(beginOfCalendarYear(calendarYear + 1)));
        }

        if (rkaCode != null) {
            builder.and(RKA.officialCode.eq(rkaCode));
        }

        final List<PublicCarnivorePermitDTO> list = jpqlQueryFactory.from(SPA)
                .select(DECISION.decisionYear, DECISION.decisionNumber, DECISION.publishDate, SPECIES.officialCode, RKA.officialCode)
                .innerJoin(SPA.permitDecision, DECISION)
                .innerJoin(SPA.gameSpecies, SPECIES)
                .innerJoin(DECISION.rhy, RHY)
                .innerJoin(RHY.parentOrganisation, RKA)
                .where(builder)
                .orderBy(DECISION.decisionNumber.desc())
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize() + 1)
                .fetch().stream().map(t -> PublicCarnivorePermitDTO.Builder.builder()
                        .withPermitNumber(DocumentNumberUtil.createDocumentNumber(t.get(DECISION.decisionYear), 1, t.get(DECISION.decisionNumber)))
                        .withSpeciesCode(t.get(SPECIES.officialCode))
                        .withDecisionDate(t.get(DECISION.publishDate).toLocalDate())
                        .withRkaCode(t.get(RKA.officialCode))
                        .build())
                .collect(toList());

        return BaseRepositoryImpl.toSlice(list, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> isCarnivorePermitAvailable(final String permitNumber) {
        final int decisionNumber = DocumentNumberUtil.extractOrderNumber(permitNumber);
        final QPermitDecision DECISION = QPermitDecision.permitDecision;
        final QPermitDecisionSpeciesAmount SPA = QPermitDecisionSpeciesAmount.permitDecisionSpeciesAmount;
        final QGameSpecies SPECIES = QGameSpecies.gameSpecies;

        final BooleanBuilder builder =
                buildBasePredicate(DECISION, SPECIES).and(DECISION.decisionNumber.eq(decisionNumber));

        return Optional.ofNullable(jpqlQueryFactory.select(DECISION.id).from(SPA)
                .innerJoin(SPA.permitDecision, DECISION)
                .innerJoin(SPA.gameSpecies, SPECIES)
                .where(builder)
                .fetchOne());
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<EndOfHuntingReminderDTO> findMissingEndOfHuntingReports(final LocalDate permitEndDate,
                                                                        final EndOfHuntingReminderType endOfHuntingReminderType) {
        final QHarvestPermitSpeciesAmount PERMIT_SPECIES_AMOUNT = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final BooleanExpression endDatePredicate =
                PERMIT_SPECIES_AMOUNT.endDate2.coalesce(PERMIT_SPECIES_AMOUNT.endDate).asDate().eq(permitEndDate);

        return findMissingEndOfHuntingReports(endDatePredicate, endOfHuntingReminderType, PERMIT_SPECIES_AMOUNT);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<EndOfHuntingReminderDTO> findMissingEndOfHuntingReports(final int permitEndYear,
                                                                        final EndOfHuntingReminderType endOfHuntingReminderType) {
        final QHarvestPermitSpeciesAmount PERMIT_SPECIES_AMOUNT = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final LocalDate beginDate = new LocalDate(permitEndYear, 1, 1);
        final LocalDate endDate = new LocalDate(permitEndYear, 12, 31);
        final BooleanExpression endDatePredicate =
                PERMIT_SPECIES_AMOUNT.endDate2.coalesce(PERMIT_SPECIES_AMOUNT.endDate).asDate().between(beginDate, endDate);

        return findMissingEndOfHuntingReports(endDatePredicate, endOfHuntingReminderType, PERMIT_SPECIES_AMOUNT);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Map<HuntingClubGroup, HarvestPermit> getByHuntingGroup(final Collection<HuntingClubGroup> groups) {
        QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
        QHuntingClubGroup GROUP = QHuntingClubGroup.huntingClubGroup;

        if (requireNonNull(groups).isEmpty()) {
            return emptyMap();
        }

        return jpqlQueryFactory
                .from(GROUP)
                .join(GROUP.harvestPermit, PERMIT)
                .where(GROUP.in(groups))
                .transform(groupBy(GROUP).as(PERMIT));
    }

    /**
     * Decisions should be listed the day after publishing clock 23.59 onwards.
     * @return DateTime Decisions must be published before this timestamp to be included in the listing
     */
    private static DateTime calculatePublishTimeBeforeCriterion() {
        return DateUtil.now().plusMinutes(1).toLocalDate().minusDays(1).toDateTimeAtStartOfDay();
    }

    private static BooleanBuilder buildBasePredicate(final QPermitDecision DECISION, final QGameSpecies SPECIES) {
        return new BooleanBuilder(DECISION.permitTypeCode.in(PermitTypeCode.CARNIVORE_PERMIT_CODES)
                .and(DECISION.status.eq(PUBLISHED))
                .and(DECISION.publishDate.before(calculatePublishTimeBeforeCriterion()))
                .and(SPECIES.officialCode.in(LARGE_CARNIVORES))
                .and(DECISION.decisionType.eq(HARVEST_PERMIT)));
    }

    private BooleanExpression getPermitTypesAndValidityYearsPredicate(final EndOfHuntingReminderType endOfHuntingReminderType,
                                                                      final QHarvestPermit PERMIT,
                                                                      final QPermitDecision DECISION) {
        final Set ALL_PERMIT_TYPES = Sets.difference(DEROGATION_PERMIT_CODES, ImmutableSet.of(NEST_REMOVAL_BASED));
        final Set ONE_YEAR_PERMIT_TYPES = Sets.difference(ALL_PERMIT_TYPES, ImmutableSet.of(ANNUAL_UNPROTECTED_BIRD));

        switch (endOfHuntingReminderType) {
            case ALL:
                return PERMIT.permitTypeCode.in(ALL_PERMIT_TYPES);
            case MULTI_YEAR:
                final BooleanExpression annualRenewal = PERMIT.permitTypeCode.eq(ANNUAL_UNPROTECTED_BIRD);
                final BooleanExpression otherMultiYear = PERMIT.permitTypeCode.in(ONE_YEAR_PERMIT_TYPES).and(DECISION.validityYears.gt(1));
                return annualRenewal.or(otherMultiYear);
            case ONE_YEAR:
                return PERMIT.permitTypeCode.in(ONE_YEAR_PERMIT_TYPES).and(DECISION.validityYears.eq(1));
            default:
                return null;
        }
    }

    private List<EndOfHuntingReminderDTO> findMissingEndOfHuntingReports(final BooleanExpression endDatePredicate,
                                                                         final EndOfHuntingReminderType endOfHuntingReminderType,
                                                                         final QHarvestPermitSpeciesAmount PERMIT_SPECIES_AMOUNT) {
        final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
        final QGameSpecies GAME_SPECIES = QGameSpecies.gameSpecies;
        final QPermitDecision DECISION = QPermitDecision.permitDecision;
        final QPerson CONTACT_PERSON = QPerson.person;

        final BooleanExpression endOfHuntingReportMissingPredicate = PERMIT.harvestReportState.isNull();

        final BooleanExpression permitTypesAndYearsPredicate =
                getPermitTypesAndValidityYearsPredicate(endOfHuntingReminderType, PERMIT, DECISION);

        final Map<Long, Group> missingReports = jpqlQueryFactory
                .select(CONTACT_PERSON.email, PERMIT.id, PERMIT.permitNumber, GAME_SPECIES.nameLocalisation())
                .from(PERMIT_SPECIES_AMOUNT)
                .join(PERMIT_SPECIES_AMOUNT.harvestPermit, PERMIT)
                .join(PERMIT_SPECIES_AMOUNT.gameSpecies, GAME_SPECIES)
                .join(PERMIT.permitDecision, DECISION)
                .join(PERMIT.originalContactPerson, CONTACT_PERSON)
                .where(endOfHuntingReportMissingPredicate.and(endDatePredicate.and(permitTypesAndYearsPredicate)))
                .transform(GroupBy.groupBy(PERMIT.id).as(PERMIT.permitNumber, CONTACT_PERSON.email, list(GAME_SPECIES.nameLocalisation())));

        if (missingReports.isEmpty()) {
            return emptyList();
        }

        final QHarvestPermitContactPerson ADDITIONAL_CONTACT_PERSON = QHarvestPermitContactPerson.harvestPermitContactPerson;
        final Map<String, List<String>> additionalContactsMapping = jpqlQueryFactory
                .select(CONTACT_PERSON.email)
                .from(ADDITIONAL_CONTACT_PERSON)
                .join(ADDITIONAL_CONTACT_PERSON.harvestPermit, PERMIT)
                .join(ADDITIONAL_CONTACT_PERSON.contactPerson, CONTACT_PERSON)
                .where(PERMIT.id.in(new ArrayList<>(missingReports.keySet())))
                .transform(GroupBy.groupBy(PERMIT.permitNumber).as(list(CONTACT_PERSON.email)));

        return missingReports.entrySet().stream()
                .filter(t -> {
                    final Group group = t.getValue();
                    final String contactPersonEmail = group.getOne(CONTACT_PERSON.email);
                    final List<String> additionalContactEmails = additionalContactsMapping.get(group.getOne(PERMIT.permitNumber));
                    return contactPersonEmail != null || F.anyNonNull(additionalContactEmails);
                })
                .map(t -> {
                    final Group group = t.getValue();
                    final String permitNumber = group.getOne(PERMIT.permitNumber);
                    return new EndOfHuntingReminderDTO(
                            group.getOne(CONTACT_PERSON.email),
                            additionalContactsMapping.get(permitNumber),
                            t.getKey(),
                            permitNumber,
                            group.getList(GAME_SPECIES.nameLocalisation()));
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<HarvestPermit> findByHuntingYearAndSpeciesAndCategory(final int huntingYear,
                                                                      final GameSpecies species,
                                                                      final HarvestPermitCategory category) {
        final QHarvestPermitSpeciesAmount SPA = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
        final QGameSpecies SPECIES = QGameSpecies.gameSpecies;
        final QPermitDecision DECISION = QPermitDecision.permitDecision;
        final QHarvestPermitApplication APP = QHarvestPermitApplication.harvestPermitApplication;

        final LocalDate huntingYearStart = DateUtil.huntingYearBeginDate(huntingYear);
        final LocalDate huntingYearEnd = DateUtil.huntingYearEndDate(huntingYear);

        return jpqlQueryFactory
                .select(PERMIT)
                .from(SPA)
                .innerJoin(SPA.harvestPermit, PERMIT)
                .innerJoin(SPA.gameSpecies, SPECIES)
                .innerJoin(PERMIT.permitDecision, DECISION)
                .innerJoin(DECISION.application, APP)
                .where(SPECIES.eq(species)
                        .and(SPA.beginDate.between(huntingYearStart, huntingYearEnd))
                        .and(APP.harvestPermitCategory.eq(category)))
                .fetch();
    }
}

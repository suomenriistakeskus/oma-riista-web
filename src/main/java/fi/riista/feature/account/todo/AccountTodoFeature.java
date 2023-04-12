package fi.riista.feature.account.todo;

import static fi.riista.feature.organization.rhy.taxation.HarvestTaxationReportingFeature.LAST_FILLING_DAY_OF_MONTH;
import static fi.riista.feature.organization.rhy.taxation.HarvestTaxationReportingFeature.LAST_FILLING_MONTH;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.util.DateUtil.today;

import com.google.common.collect.Sets;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.QHarvest;
import fi.riista.feature.gamediary.srva.SrvaEventRepository;
import fi.riista.feature.gamediary.srva.SrvaEventStateEnum;
import fi.riista.feature.gamediary.srva.SrvaSpecs;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermitContactPerson;
import fi.riista.feature.harvestpermit.QHarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.members.invitation.HuntingClubMemberInvitationRepository;
import fi.riista.feature.huntingclub.members.invitation.HuntingClubMemberInvitation_;
import fi.riista.feature.huntingclub.permit.endofhunting.HuntingFinishingService;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysAuthorization.RhyPermission;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEvent;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventRepository;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventStatus;
import fi.riista.feature.organization.rhy.taxation.HarvestTaxationRepository;
import fi.riista.feature.organization.rhy.taxation.QHarvestTaxationReport;
import fi.riista.feature.shootingtest.ShootingTest;
import fi.riista.feature.shootingtest.ShootingTestEventRepository;
import fi.riista.util.DateUtil;
import fi.riista.util.jpa.JpaSpecs;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Resource;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AccountTodoFeature {

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private HuntingClubMemberInvitationRepository invitationRepository;

    @Resource
    private SrvaEventRepository srvaEventRepository;

    @Resource
    private ShootingTestEventRepository shootingTestEventRepository;

    @Resource
    private HarvestTaxationRepository harvestTaxationRepository;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Resource
    private HuntingControlEventRepository huntingControlEventRepository;

    @Resource
    private HuntingFinishingService huntingFinishingService;

    @Transactional(readOnly = true)
    public AccountPermitTodoCountDTO countPermitTodos() {
        final Person person = activeUserService.requireActiveUser().getPerson();

        if (person == null) {
            return new AccountPermitTodoCountDTO(Collections.emptySet());
        }

        final Set<Long> nonMooselikeTodos = listNonMooselikePermitsRequiringAction(person);
        final Set<Long> mooselikeTodos = listMooselikePermitsRequiringAction(person);
        return new AccountPermitTodoCountDTO(Sets.union(nonMooselikeTodos, mooselikeTodos));
    }

    private Set<Long> listNonMooselikePermitsRequiringAction(@Nonnull final Person person) {
        final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;

        final BooleanExpression permitContactPerson = getPermitContactPersonPredicate(person, PERMIT);

        final QHarvestPermitSpeciesAmount SPECIES_AMOUNT = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;

        final BooleanExpression amountsPresent = JPAExpressions
                .selectOne()
                .from(SPECIES_AMOUNT)
                .where(SPECIES_AMOUNT.harvestPermit.eq(PERMIT))
                .exists();

        final BooleanExpression huntingEnded = JPAExpressions
                .selectOne()
                .from(SPECIES_AMOUNT)
                .where(SPECIES_AMOUNT.harvestPermit.eq(PERMIT))
                .where(SPECIES_AMOUNT.endDate2.coalesce(SPECIES_AMOUNT.endDate).getValue().goe(today()))
                .notExists();

        final QHarvest HARVEST = QHarvest.harvest;
        final BooleanExpression proposedHarvests = JPAExpressions
                .selectOne()
                .from(HARVEST)
                .where(HARVEST.harvestPermit.eq(PERMIT))
                .where(HARVEST.stateAcceptedToHarvestPermit.eq(Harvest.StateAcceptedToHarvestPermit.PROPOSED))
                .exists();

        final List<Long> ids = jpqlQueryFactory
                .select(PERMIT.id)
                .from(PERMIT)
                .where(PERMIT.harvestReportState.isNull())
                .where(PERMIT.isMooselikeOrAmendmentPermit().not())
                .where(permitContactPerson)
                .where(amountsPresent)
                .where(huntingEnded.or(proposedHarvests))
                .fetch();
        return Sets.newHashSet(ids);
    }

    private Set<Long> listMooselikePermitsRequiringAction(final Person person) {
        final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
        final QHarvestPermitSpeciesAmount SPECIES_AMOUNT = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final BooleanExpression permitContactPerson = getPermitContactPersonPredicate(person, PERMIT);
        final int year = DateUtil.huntingYear();

        final List<Tuple> permitAndSpeciesAmounts = jpqlQueryFactory.select(PERMIT, SPECIES_AMOUNT)
                .from(PERMIT)
                .join(PERMIT.speciesAmounts, SPECIES_AMOUNT)
                .where(permitContactPerson)
                .where(PERMIT.isMooselikePermit())
                .where(SPECIES_AMOUNT.mooselikeHuntingFinished.isFalse())
                .where(SPECIES_AMOUNT.validOnHuntingYear(year))
                .fetch();

        return permitAndSpeciesAmounts.stream()
                .filter(t -> {
                    final HarvestPermit permit = t.get(0, HarvestPermit.class);
                    final HarvestPermitSpeciesAmount spa = t.get(1, HarvestPermitSpeciesAmount.class);
                    return huntingFinishingService.allPartnersFinishedHunting(permit, spa.getGameSpecies().getOfficialCode());
                })
                .map(t -> {
                    final HarvestPermit permit = t.get(0, HarvestPermit.class);
                    return permit.getId();
                }).collect(Collectors.toSet());
    }

    private static BooleanExpression getPermitContactPersonPredicate(final Person person, final QHarvestPermit PERMIT) {
        final QHarvestPermitContactPerson CONTACT_PERSON = QHarvestPermitContactPerson.harvestPermitContactPerson;
        return PERMIT.originalContactPerson.eq(person)
                .or(JPAExpressions
                        .selectOne()
                        .from(CONTACT_PERSON)
                        .where(CONTACT_PERSON.harvestPermit.eq(PERMIT))
                        .where(CONTACT_PERSON.contactPerson.eq(person))
                        .exists());
    }

    @Transactional(readOnly = true)
    public AccountTodoCountDTO countInvitationTodos() {
        return countTodos(this::countInvitations);
    }

    private long countInvitations(final @Nonnull Person person) {
        return invitationRepository.count(JpaSpecs.and(
                JpaSpecs.equal(HuntingClubMemberInvitation_.person, person),
                JpaSpecs.isNull(HuntingClubMemberInvitation_.userRejectedTime)));
    }

    @Transactional(readOnly = true)
    public AccountTodoCountDTO countSrvaTodos(final long rhyId) {
        return countTodos(person -> countUnfinishedSrvaEvents(rhyId));
    }

    private long countUnfinishedSrvaEvents(final long rhyId) {
        final Riistanhoitoyhdistys rhy =
                requireEntityService.requireRiistanhoitoyhdistys(rhyId, RhyPermission.LIST_SRVA);

        return srvaEventRepository.count(JpaSpecs.and(
                SrvaSpecs.equalRhy(rhy),
                SrvaSpecs.equalState(SrvaEventStateEnum.UNFINISHED)));
    }

    @Transactional(readOnly = true)
    public AccountTodoCountDTO countShootingTestTodos(final long rhyId) {
        final Riistanhoitoyhdistys rhy =
                requireEntityService.requireRiistanhoitoyhdistys(rhyId, RhyPermission.VIEW_SHOOTING_TEST_EVENTS);

        final boolean isCoordinatorOrModerator =
                activeUserService.isModeratorOrAdmin() || userAuthorizationHelper.isCoordinator(rhy);

        final LocalDate beginDate = ShootingTest.getBeginDateOfShootingTestEventList(!isCoordinatorOrModerator);
        final LocalDate endDate = today().minusDays(1);

        return countTodos(person -> shootingTestEventRepository.countShootingTestEventsNotProperlyFinished(rhy, beginDate, endDate));
    }

    @Transactional(readOnly = true)
    public AccountTodoCountDTO countTaxationTodos(final long rhyId) {
        final Riistanhoitoyhdistys rhy =
                requireEntityService.requireRiistanhoitoyhdistys(rhyId, READ);

        final boolean isCoordinatorOrModerator =
                activeUserService.isModeratorOrAdmin() || userAuthorizationHelper.isCoordinator(rhy);

        return countTodos(person -> {
            if (!isCoordinatorOrModerator) {
                return 0L;
            }
            final LocalDate today = today();
            final org.joda.time.LocalDate lastFillingDateOfTheYear = new org.joda.time.LocalDate(
                    today.getYear(),
                    LAST_FILLING_MONTH,
                    LAST_FILLING_DAY_OF_MONTH);
            if (today.isAfter(lastFillingDateOfTheYear)) {
                // don't show notification after the last filling date of the year
                return 0L;
            }
            // every HTA must contain one report per species
            final int expectedTotalReportCount = rhy.getRelatedHtas().size() * GameSpecies.MOOSE_AND_DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING.size();

            final int nextHuntingYear = DateUtil.huntingYear() + 1;
            final QHarvestTaxationReport TAX = QHarvestTaxationReport.harvestTaxationReport;
            final BooleanExpression predicate = TAX.rhy.eq(rhy)
                    .and(TAX.huntingYear.eq(nextHuntingYear));

            final long foundReportCount = harvestTaxationRepository.findAllAsList(predicate).size();
            return expectedTotalReportCount - foundReportCount;
        });
    }

    @Transactional(readOnly = true)
    public AccountTodoCountDTO countHuntingControlEventTodos(final long rhyId) {
        final Riistanhoitoyhdistys rhy =
                requireEntityService.requireRiistanhoitoyhdistys(rhyId, RhyPermission.LIST_HUNTING_CONTROL_EVENTS);

        final LocalDate today = DateUtil.today();
        // Check last year proposed events until 15th of January following year
        final int startYear = today.minusDays(15).getYear() < today.getYear() ? today.getYear() - 1 : today.getYear();
        final LocalDate startDate = new LocalDate(startYear, 1, 1);

        return countTodos(person -> {
            final List<HuntingControlEvent> events =
                    huntingControlEventRepository.findByRhyIdAndDateBetweenAndStatusOrderByDateDesc(rhy.getId(), startDate, today, HuntingControlEventStatus.PROPOSED);
            return new Integer(events.size()).longValue();
        });
    }


    private AccountTodoCountDTO countTodos(final Function<? super Person, Long> todoCountFn) {
        final long todoCount = Optional
                .ofNullable(activeUserService.requireActiveUser().getPerson())
                .map(todoCountFn)
                .orElse(0L);

        return new AccountTodoCountDTO(todoCount);
    }
}

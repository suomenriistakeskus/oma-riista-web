package fi.riista.feature.account.todo;

import com.google.common.collect.Sets;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.QHarvest;
import fi.riista.feature.gamediary.srva.SrvaEventRepository;
import fi.riista.feature.gamediary.srva.SrvaEventStateEnum;
import fi.riista.feature.gamediary.srva.SrvaSpecs;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermitContactPerson;
import fi.riista.feature.harvestpermit.QHarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.members.invitation.HuntingClubMemberInvitationRepository;
import fi.riista.feature.huntingclub.members.invitation.HuntingClubMemberInvitation_;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysAuthorization.RhyPermission;
import fi.riista.util.jpa.JpaSpecs;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static fi.riista.util.DateUtil.today;

@Component
public class AccountTodoFeature {

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HuntingClubMemberInvitationRepository invitationRepository;

    @Resource
    private SrvaEventRepository srvaEventRepository;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Transactional(readOnly = true)
    public AccountTodoCountDTO countTodos() {
        final Person person = activeUserService.requireActiveUser().getPerson();

        if (person == null) {
            return new AccountTodoCountDTO(Collections.emptySet(), 0);
        }

        return new AccountTodoCountDTO(
                listAllPermitsRequiringAction(person),
                countInvitations(person));
    }

    private Set<Long> listAllPermitsRequiringAction(@Nonnull final Person person) {
        final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;

        final QHarvestPermitContactPerson CONTACT_PERSON = QHarvestPermitContactPerson.harvestPermitContactPerson;
        final BooleanExpression permitcontactPerson = PERMIT.originalContactPerson.eq(person)
                .or(JPAExpressions
                        .selectOne()
                        .from(CONTACT_PERSON)
                        .where(CONTACT_PERSON.harvestPermit.eq(PERMIT))
                        .where(CONTACT_PERSON.contactPerson.eq(person))
                        .exists());

        final QHarvestPermitSpeciesAmount SPECIES_AMOUNT = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
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
                .where(permitcontactPerson)
                .where(huntingEnded.or(proposedHarvests))
                .fetch();
        return Sets.newHashSet(ids);
    }

    private long countInvitations(final @Nonnull Person person) {
        return invitationRepository.count(JpaSpecs.and(
                JpaSpecs.equal(HuntingClubMemberInvitation_.person, person),
                JpaSpecs.isNull(HuntingClubMemberInvitation_.userRejectedTime)));
    }

    @Transactional(readOnly = true)
    public AccountSrvaTodoCountDTO countSrvaTodos(final long rhyId) {
        final Person person = activeUserService.requireActiveUser().getPerson();

        if (person == null) {
            return new AccountSrvaTodoCountDTO(0);
        }

        return new AccountSrvaTodoCountDTO(countUnfinishedSrvaEvents(rhyId));
    }

    private long countUnfinishedSrvaEvents(final long rhyId) {
        final Riistanhoitoyhdistys rhy =
                requireEntityService.requireRiistanhoitoyhdistys(rhyId, RhyPermission.LIST_SRVA);

        return srvaEventRepository.count(JpaSpecs.and(
                SrvaSpecs.equalRhy(rhy),
                SrvaSpecs.equalState(SrvaEventStateEnum.UNFINISHED)));
    }
}

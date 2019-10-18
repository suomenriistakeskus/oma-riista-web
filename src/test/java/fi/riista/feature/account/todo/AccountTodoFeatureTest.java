package fi.riista.feature.account.todo;

import com.google.common.collect.Sets;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEventStateEnum;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.test.TestUtils;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;

import static fi.riista.test.Asserts.assertEmpty;
import static fi.riista.util.DateUtil.now;
import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;

public class AccountTodoFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private AccountTodoFeature accountTodoFeature;

    private Riistanhoitoyhdistys rhy;

    @Before
    public void initRhy() {
        this.rhy = model().newRiistanhoitoyhdistys();
    }

    @Test
    public void testCountInvitations() {
        withPerson(person -> {
            model().newHuntingClubInvitation(person, model().newHuntingClub(rhy), OccupationType.SEURAN_JASEN);

            onSavedAndAuthenticated(createUser(person), () -> {
                final AccountTodoCountDTO dto = accountTodoFeature.countInvitationTodos();
                assertEquals(1, dto.getTodoCount());
            });
        });
    }

    @Test
    public void testCountPermits_withNothing() {
        persistAndAuthenticateWithNewUser(true);
        assertEmpty(accountTodoFeature.countPermitTodos().getPermitIds());
    }

    @Test
    public void testCountPermits_huntingTimeNotEnded() {
        withPerson(person -> {
            createPermit(person, today().minusDays(1), today().plusDays(1));
            onSavedAndAuthenticated(createUser(person), () -> {
                final AccountPermitTodoCountDTO dto = accountTodoFeature.countPermitTodos();
                assertEmpty(dto.getPermitIds());
            });
        });
    }

    @Test
    public void testCountPermits_huntingTimeEnded() {
        withPerson(person -> {
            final HarvestPermit permit = createPermit(person, today().minusDays(1), null)._1;
            onSavedAndAuthenticated(createUser(person), () -> {
                final AccountPermitTodoCountDTO dto = accountTodoFeature.countPermitTodos();
                assertEquals(Sets.newHashSet(permit.getId()), dto.getPermitIds());
            });
        });
    }

    @Test
    public void testCountPermits_huntingTimeEnded_permitCompleted() {
        withPerson(person -> {
            final HarvestPermit permit = createPermit(person, today().minusDays(1), null)._1;
            permit.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
            permit.setHarvestReportAuthor(person);
            permit.setHarvestReportDate(now());
            permit.setHarvestReportModeratorOverride(false);

            onSavedAndAuthenticated(createUser(person), () -> {
                final AccountPermitTodoCountDTO dto = accountTodoFeature.countPermitTodos();
                assertEmpty(dto.getPermitIds());
            });
        });
    }

    @Test
    public void testCountPermits_proposedHarvests() {
        withPerson(person -> {
            final Tuple2<HarvestPermit, HarvestPermitSpeciesAmount> t = createPermit(person, today().plusDays(1), null);
            final HarvestPermit permit = t._1;
            final HarvestPermitSpeciesAmount spa = t._2;
            model().newHarvest(permit, spa.getGameSpecies());

            onSavedAndAuthenticated(createUser(person), () -> {
                final AccountPermitTodoCountDTO dto = accountTodoFeature.countPermitTodos();
                assertEquals(Sets.newHashSet(permit.getId()), dto.getPermitIds());
            });
        });
    }

    private Tuple2<HarvestPermit, HarvestPermitSpeciesAmount> createPermit(final Person person,
                                                                           final LocalDate dateValid,
                                                                           final LocalDate dateValid2) {
        final HarvestPermit permit = model().newHarvestPermit(this.rhy);
        permit.setOriginalContactPerson(person);

        final HarvestPermitSpeciesAmount spa =
                model().newHarvestPermitSpeciesAmount(permit, model().newGameSpecies(), 2.0f);
        spa.setBeginDate(dateValid);
        spa.setEndDate(dateValid);
        spa.setBeginDate2(dateValid2);
        spa.setEndDate2(dateValid2);
        return Tuple.of(permit, spa);
    }

    @Test
    public void testCountUnfinishedSrvaEvents() {
        withPerson(person -> withRhy(rhy -> {
            model().newOccupation(rhy, person, OccupationType.SRVA_YHTEYSHENKILO);
            person.setRhyMembership(rhy);

            final SystemUser moderator = createNewUser(SystemUser.Role.ROLE_MODERATOR);

            TestUtils.createList(5, () -> {
                final SrvaEvent event = model().newSrvaEvent(person, rhy);
                event.setState(SrvaEventStateEnum.UNFINISHED);
                return event;
            });

            TestUtils.createList(7, () -> {
                final SrvaEvent event = model().newSrvaEvent(person, rhy);
                event.setState(SrvaEventStateEnum.APPROVED);
                event.setApproverAsUser(moderator);
                return event;
            });

            TestUtils.createList(13, () -> {
                final SrvaEvent event = model().newSrvaEvent(person, rhy);
                event.setState(SrvaEventStateEnum.REJECTED);
                event.setApproverAsUser(moderator);
                return event;
            });

            onSavedAndAuthenticated(createUser(person), () -> {
                final AccountTodoCountDTO todo = accountTodoFeature.countSrvaTodos(rhy.getId());
                assertEquals(5, todo.getTodoCount());
            });
        }));
    }

}

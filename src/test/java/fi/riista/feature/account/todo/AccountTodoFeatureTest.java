package fi.riista.feature.account.todo;

import static fi.riista.test.Asserts.assertEmpty;
import static fi.riista.test.Asserts.assertThat;
import static fi.riista.util.DateUtil.now;
import static fi.riista.util.DateUtil.today;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.Sets;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEventStateEnum;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.test.TestUtils;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import javax.annotation.Resource;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

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

    @Test
    public void testCountPermits_todoNotPresentForRejectedApplication() {
        withPerson(person -> {
            final HarvestPermit permit = model().newHarvestPermit(this.rhy);
            permit.setOriginalContactPerson(person);

            onSavedAndAuthenticated(createUser(person), () -> {
                final AccountPermitTodoCountDTO dto = accountTodoFeature.countPermitTodos();
                assertThat(dto.getPermitIds(), is(empty()));
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
    public void testCountPermits_mooselike_nothingSent() {
        withPerson(person -> {
            createMooselikePermit(person);

            onSavedAndAuthenticated(createUser(person), () -> {
                final AccountPermitTodoCountDTO dto = accountTodoFeature.countPermitTodos();
                assertThat(dto.getPermitIds(), is(empty()));
            });
        });
    }

    @Test
    public void testCountPermits_moose_somePartnersSentSummary() {
        withPerson(person -> {
            final Tuple3<HarvestPermit, Tuple2<HarvestPermitSpeciesAmount, HarvestPermitSpeciesAmount>, Tuple2<HuntingClub, HuntingClub>> t = createMooselikePermit(person);
            final HarvestPermit permit = t._1;
            final HuntingClub club1 = t._3._1;

            persistInNewTransaction();
            model().newMooseHuntingSummary(permit, club1, true);

            onSavedAndAuthenticated(createUser(person), () -> {
                final AccountPermitTodoCountDTO dto = accountTodoFeature.countPermitTodos();
                assertThat(dto.getPermitIds(), is(empty()));
            });
        });
    }

    @Test
    public void testCountPermits_moose_allPartnersSentSummary() {
        withPerson(person -> {
            final Tuple3<HarvestPermit, Tuple2<HarvestPermitSpeciesAmount, HarvestPermitSpeciesAmount>, Tuple2<HuntingClub, HuntingClub>> t = createMooselikePermit(person);
            final HarvestPermit permit = t._1;
            final HuntingClub club1 = t._3._1;
            final HuntingClub club2 = t._3._2;

            persistInNewTransaction();
            model().newMooseHuntingSummary(permit, club1, true);
            model().newMooseHuntingSummary(permit, club2, true);

            onSavedAndAuthenticated(createUser(person), () -> {
                final AccountPermitTodoCountDTO dto = accountTodoFeature.countPermitTodos();
                assertEquals(Sets.newHashSet(permit.getId()), dto.getPermitIds());
            });
        });
    }

    @Test
    public void testCountPermits_moose_allPartnersSentSummary_prevYear() {
        withPerson(person -> {
            final Tuple3<HarvestPermit, Tuple2<HarvestPermitSpeciesAmount, HarvestPermitSpeciesAmount>, Tuple2<HuntingClub, HuntingClub>> t = createMooselikePermit(person);
            final HarvestPermit permit = t._1;
            final HuntingClub club1 = t._3._1;
            final HuntingClub club2 = t._3._2;

            final HarvestPermitSpeciesAmount spaMoose = t._2._1;
            spaMoose.setBeginDate(spaMoose.getBeginDate().minusYears(1));
            spaMoose.setEndDate(spaMoose.getEndDate().minusYears(1));

            final HarvestPermitSpeciesAmount spaWhiteTail = t._2._2;
            spaWhiteTail.setBeginDate(spaWhiteTail.getBeginDate().minusYears(1));
            spaWhiteTail.setEndDate(spaWhiteTail.getEndDate().minusYears(1));

            persistInNewTransaction();
            model().newMooseHuntingSummary(permit, club1, true);
            model().newMooseHuntingSummary(permit, club2, true);

            onSavedAndAuthenticated(createUser(person), () -> {
                final AccountPermitTodoCountDTO dto = accountTodoFeature.countPermitTodos();
                assertThat(dto.getPermitIds(), is(empty()));
            });
        });
    }

    @Test
    public void testCountPermits_moose_allPartnersSentSummaryAndHuntingFinished() {
        withPerson(person -> {
            final Tuple3<HarvestPermit, Tuple2<HarvestPermitSpeciesAmount, HarvestPermitSpeciesAmount>, Tuple2<HuntingClub, HuntingClub>> t = createMooselikePermit(person);
            final HarvestPermit permit = t._1;
            final HarvestPermitSpeciesAmount spaMoose = t._2._1;
            final HuntingClub club1 = t._3._1;
            final HuntingClub club2 = t._3._2;

            spaMoose.setMooselikeHuntingFinished(true);
            persistInNewTransaction();
            model().newMooseHuntingSummary(permit, club1, true);
            model().newMooseHuntingSummary(permit, club2, true);

            onSavedAndAuthenticated(createUser(person), () -> {
                final AccountPermitTodoCountDTO dto = accountTodoFeature.countPermitTodos();
                assertThat(dto.getPermitIds(), is(empty()));
            });
        });
    }

    @Test
    public void testCountPermits_whiteTail_somePartnersSentSummary() {
        withPerson(person -> {
            final Tuple3<HarvestPermit, Tuple2<HarvestPermitSpeciesAmount, HarvestPermitSpeciesAmount>, Tuple2<HuntingClub, HuntingClub>> t = createMooselikePermit(person);
            final HarvestPermitSpeciesAmount spaWhiteTail = t._2._2;
            final HuntingClub club2 = t._3._2;

            model().newBasicHuntingSummary(spaWhiteTail, club2, true);

            onSavedAndAuthenticated(createUser(person), () -> {
                final AccountPermitTodoCountDTO dto = accountTodoFeature.countPermitTodos();
                assertThat(dto.getPermitIds(), is(empty()));
            });
        });
    }

    @Test
    public void testCountPermits_whiteTail_allPartnersSentSummary() {
        withPerson(person -> {
            final Tuple3<HarvestPermit, Tuple2<HarvestPermitSpeciesAmount, HarvestPermitSpeciesAmount>, Tuple2<HuntingClub, HuntingClub>> t = createMooselikePermit(person);
            final HarvestPermit permit = t._1;
            final HarvestPermitSpeciesAmount spaWhiteTail = t._2._2;
            final HuntingClub club1 = t._3._1;
            final HuntingClub club2 = t._3._2;

            model().newBasicHuntingSummary(spaWhiteTail, club2, true);
            model().newBasicHuntingSummary(spaWhiteTail, club1, true);

            onSavedAndAuthenticated(createUser(person), () -> {
                final AccountPermitTodoCountDTO dto = accountTodoFeature.countPermitTodos();
                assertEquals(Sets.newHashSet(permit.getId()), dto.getPermitIds());
            });
        });
    }

    @Test
    public void testCountPermits_whiteTail_allPartnersSentSummaryAndHuntingFinished() {
        withPerson(person -> {
            final Tuple3<HarvestPermit, Tuple2<HarvestPermitSpeciesAmount, HarvestPermitSpeciesAmount>, Tuple2<HuntingClub, HuntingClub>> t = createMooselikePermit(person);
            final HarvestPermitSpeciesAmount spaWhiteTail = t._2._2;
            final HuntingClub club1 = t._3._1;
            final HuntingClub club2 = t._3._2;

            model().newBasicHuntingSummary(spaWhiteTail, club2, true);
            model().newBasicHuntingSummary(spaWhiteTail, club1, true);

            spaWhiteTail.setMooselikeHuntingFinished(true);

            onSavedAndAuthenticated(createUser(person), () -> {
                final AccountPermitTodoCountDTO dto = accountTodoFeature.countPermitTodos();
                assertThat(dto.getPermitIds(), is(empty()));
            });
        });
    }

    private Tuple3<HarvestPermit, Tuple2<HarvestPermitSpeciesAmount, HarvestPermitSpeciesAmount>, Tuple2<HuntingClub, HuntingClub>> createMooselikePermit(final Person person) {
        final HarvestPermit permit = model().newHarvestPermit(this.rhy);
        permit.setOriginalContactPerson(person);
        permit.setPermitTypeCode(PermitTypeCode.MOOSELIKE);
        permit.setPermitAreaSize(1000);

        final HarvestPermitArea permitArea = model().newHarvestPermitArea();

        final HuntingClub club1 = model().newHuntingClub();
        final HuntingClubArea area = model().newHuntingClubArea(club1, model().newGISZone());
        model().newHarvestPermitAreaPartner(permitArea, area);
        permit.getPermitPartners().add(club1);

        final HuntingClub club2 = model().newHuntingClub();
        final HuntingClubArea area2 = model().newHuntingClubArea(club2, model().newGISZone());
        model().newHarvestPermitAreaPartner(permitArea, area2);
        permit.getPermitPartners().add(club2);

        final HarvestPermitSpeciesAmount spa1 = model().newHarvestPermitSpeciesAmount(permit, model().newGameSpeciesMoose(), 2.0f);
        spa1.setBeginDate(today().minusDays(2));
        spa1.setEndDate(today().minusDays(1));
        spa1.setMooselikeHuntingFinished(false);

        final HarvestPermitSpeciesAmount spa2 = model().newHarvestPermitSpeciesAmount(permit, model().newGameSpeciesWhiteTailedDeer(), 2.0f);
        spa2.setBeginDate(today().minusDays(2));
        spa2.setEndDate(today().minusDays(1));
        spa2.setMooselikeHuntingFinished(false);

        return Tuple.of(permit, Tuple.of(spa1, spa2), Tuple.of(club1, club2));
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

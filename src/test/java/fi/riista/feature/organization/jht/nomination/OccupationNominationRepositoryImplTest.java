package fi.riista.feature.organization.jht.nomination;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.feature.organization.jht.nomination.OccupationNomination.NominationStatus.ESITETTY;
import static fi.riista.feature.organization.jht.nomination.OccupationNomination.NominationStatus.HYLATTY;
import static fi.riista.feature.organization.jht.nomination.OccupationNomination.NominationStatus.NIMITETTY;
import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static fi.riista.util.DateUtil.today;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;

@Component
public class OccupationNominationRepositoryImplTest extends EmbeddedDatabaseTest {

    @Resource
    private OccupationNominationRepository occupationNominationRepository;

    private final PageRequest pageRequest = PageRequest.of(0, 1000);

    private RiistakeskuksenAlue rka;
    private Riistanhoitoyhdistys rhy;
    private Person proposedPerson;
    private Person nominatedPerson;
    private Person rejectedPerson;
    private Person proposingPerson;
    private OccupationNomination proposedNomination;
    private OccupationNomination approvedNomination;
    private OccupationNomination deletedNomination;
    private SystemUser moderator;

    @Before
    public void setup() {
        rka = model().newRiistakeskuksenAlue("500");
        rhy = model().newRiistanhoitoyhdistys(rka, "550");
        proposedPerson = model().newPerson();
        nominatedPerson = model().newPerson();
        rejectedPerson = model().newPerson();
        proposingPerson = model().newPerson();
        moderator = createNewModerator();

        proposedNomination = createNomination(rhy, ESITETTY, proposedPerson);
        approvedNomination = createNomination(rhy, NIMITETTY, nominatedPerson);
        deletedNomination = createNomination(rhy, HYLATTY, rejectedPerson);

        persistInNewTransaction();
    }

    @Test
    public void testFindsByRka() {
        final Riistanhoitoyhdistys anotherRhy = model().newRiistanhoitoyhdistys(rka);
        createNomination(anotherRhy, ESITETTY, proposedPerson);
        persistInNewTransaction();

        runInTransaction(() -> {
            final Page<OccupationNomination> occupationNominations =
                    occupationNominationRepository.searchPage(pageRequest, AMPUMAKOKEEN_VASTAANOTTAJA,
                            ESITETTY, rka, null, null, null, null);
            final List<OccupationNomination> content = occupationNominations.getContent();
            assertThat(content, hasSize(2));
            content.forEach(nomination ->
                    assertEquals(proposedPerson, nomination.getPerson()));

            final List<Riistanhoitoyhdistys> rhys = F.mapNonNullsToList(content, OccupationNomination::getRhy);
            assertThat(rhys, containsInAnyOrder(rhy, anotherRhy));
        });
    }

    @Test
    public void testFindsByRhy() {
        runInTransaction(() -> {
            final Page<OccupationNomination> occupationNominations =
                    occupationNominationRepository.searchPage(pageRequest, AMPUMAKOKEEN_VASTAANOTTAJA,
                            ESITETTY, rka, rhy, null, null, null);
            assertThat(occupationNominations.getContent(), hasSize(1));
            assertEquals(proposedPerson, occupationNominations.getContent().get(0).getPerson());
        });
    }

    @Test
    public void testFindsByNominationDate() {

        final OccupationNomination anotherNomination = createNomination(rhy, ESITETTY, model().newPerson());
        anotherNomination.setNominationDate(proposedNomination.getNominationDate().minusDays(5));
        persistInNewTransaction();

        runInTransaction(() -> {
            final Page<OccupationNomination> page =
                    occupationNominationRepository.searchPage(pageRequest, AMPUMAKOKEEN_VASTAANOTTAJA,
                            ESITETTY, null, null, null, null, anotherNomination.getNominationDate());

            assertThat(page.getContent(), hasSize(1));
            assertEquals(anotherNomination.getPerson(), page.getContent().get(0).getPerson());
        });
    }

    @Test
    public void testFindsByDecisionDate_approved() {
        final OccupationNomination anotherNomination = createNomination(rhy, NIMITETTY, model().newPerson());
        anotherNomination.setDecisionDate(approvedNomination.getDecisionDate().minusDays(5));
        persistInNewTransaction();

        runInTransaction(() -> {
            final Page<OccupationNomination> page =
                    occupationNominationRepository.searchPage(pageRequest, AMPUMAKOKEEN_VASTAANOTTAJA,
                            NIMITETTY, null, null, null, null, anotherNomination.getDecisionDate());

            assertThat(page.getContent(), hasSize(1));
            assertEquals(anotherNomination.getPerson(), page.getContent().get(0).getPerson());
        });
    }

    @Test
    public void testFindsByDecisionDate_rejected() {
        final OccupationNomination anotherNomination = createNomination(rhy, HYLATTY, model().newPerson());
        anotherNomination.setDecisionDate(deletedNomination.getDecisionDate().minusDays(5));
        persistInNewTransaction();

        runInTransaction(() -> {
            final Page<OccupationNomination> page =
                    occupationNominationRepository.searchPage(pageRequest, AMPUMAKOKEEN_VASTAANOTTAJA,
                            HYLATTY, null, null, null, null, anotherNomination.getDecisionDate());

            assertThat(page.getContent(), hasSize(1));
            assertEquals(anotherNomination.getPerson(), page.getContent().get(0).getPerson());
        });
    }

    @Test
    public void testFindsByPerson() {
        final OccupationNomination anotherNomination = createNomination(rhy, NIMITETTY, model().newPerson());
        persistInNewTransaction();

        runInTransaction(() -> {
            final Page<OccupationNomination> page =
                    occupationNominationRepository.searchPage(pageRequest, AMPUMAKOKEEN_VASTAANOTTAJA,
                            NIMITETTY, null, null, anotherNomination.getPerson(), null, null);

            assertThat(page.getContent(), hasSize(1));
            assertEquals(anotherNomination.getPerson(), page.getContent().get(0).getPerson());
        });
    }

    private OccupationNomination createNomination(final Riistanhoitoyhdistys rhy,
                                                  final OccupationNomination.NominationStatus status,
                                                  final Person person) {

        final OccupationNomination nomination = model().newOccupationNomination(rhy,
                AMPUMAKOKEEN_VASTAANOTTAJA, person, proposingPerson);
        nomination.setNominationStatus(status);
        nomination.setNominationDate(today());
        nomination.setModeratorUser(moderator);
        nomination.setDecisionDate(today());
        return nomination;
    }
}

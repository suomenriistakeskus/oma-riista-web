package fi.riista.feature.organization.occupation;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.person.PersonDTO;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.person.PersonIsDeceasedException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static fi.riista.feature.organization.occupation.OccupationType.PETOYHDYSHENKILO;
import static fi.riista.feature.organization.occupation.OccupationType.SRVA_YHTEYSHENKILO;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class OccupationCrudFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private OccupationCrudFeature occupationCrudFeature;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testModeratorCanAddCoordinatorOccupation() {
        withRhy(rhy -> {
            final Person person = model().newPerson();

            onSavedAndAuthenticated(createNewModerator(), () -> {
                OccupationDTO dto = createOccupationDTO(rhy, person, TOIMINNANOHJAAJA);
                occupationCrudFeature.create(dto);
            });
        });
    }

    @Test
    public void testCoordinatorIsNotAllowedToAddCoordinatorOccupation() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final Person person = model().newPerson();

            thrown.expect(AccessDeniedException.class);

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                occupationCrudFeature.create(createOccupationDTO(rhy, person, TOIMINNANOHJAAJA));
            });
        });
    }

    @Test
    public void testCoordinatorIsAllowedToAddOtherThanCoordinatorOccupation() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            Person person = model().newPerson();

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                occupationCrudFeature.create(createOccupationDTO(rhy, person, PETOYHDYSHENKILO));
            });
        });
    }

    @Test
    public void testModeratorCanDeleteCoordinatorOccupation() {
        withRhyAndCoordinatorOccupation((rhy, occupation) -> {
            onSavedAndAuthenticated(createNewModerator(), () -> occupationCrudFeature.delete(occupation.getId()));
        });
    }

    @Test
    public void testCoordinatorIsNotAllowedToDeleteCoordinatorOccupation() {
        withRhyAndCoordinatorOccupation((rhy, occupation) -> {

            thrown.expect(AccessDeniedException.class);

            onSavedAndAuthenticated(createUser(occupation.getPerson()),
                    () -> occupationCrudFeature.delete(occupation.getId()));
        });
    }

    @Test
    public void testCoordinatorCannotCreateJHTOccupation() {
        withRhyAndCoordinator((rhy, coordinator) -> {

            final Person person = model().newPerson();

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                OccupationType.jhtValues().forEach(occupationType -> {
                    final OccupationDTO dto = createOccupationDTO(rhy, person, occupationType);

                    try {
                        occupationCrudFeature.create(dto);

                    } catch (final AccessDeniedException ignore) {
                        assertEquals("JHT occupation can be edited only by moderator or admin", ignore.getMessage());
                        return;
                    }

                    fail("Should prohibit creating occupationType " + occupationType);
                });
            });
        });
    }

    @Test
    public void testCoordinatorCannotUpdateJHTOccupation() {
        withRhyAndCoordinator((rhy, coordinator) -> {

            final Person person = model().newPerson();

            final List<Occupation> existingOccupations = OccupationType.jhtValues().stream()
                    .map(occupationType -> model().newOccupation(rhy, person, occupationType))
                    .collect(Collectors.toList());

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                existingOccupations.forEach((existingOccupation) -> {
                    try {
                        final OccupationDTO dto = OccupationDTO.createWithPerson(existingOccupation);

                        occupationCrudFeature.update(dto);

                    } catch (final AccessDeniedException ignore) {
                        assertEquals("JHT occupation can be edited only by moderator or admin", ignore.getMessage());
                        return;
                    }

                    fail("Should prohibit updating occupationType " + existingOccupation.getOccupationType());
                });
            });
        });
    }

    @Test
    public void testCoordinatorCannotDeleteJHTOccupation() {
        withRhyAndCoordinator((rhy, coordinator) -> onSavedAndAuthenticated(createUser(coordinator), () -> {
            OccupationType.jhtValues().forEach(occupationType -> {
                try {
                    final Occupation occupation = model().newOccupation(rhy, coordinator, occupationType);

                    persistInNewTransaction();

                    occupationCrudFeature.delete(occupation.getId());

                } catch (final AccessDeniedException ignore) {
                    assertEquals("JHT occupation can be edited only by moderator or admin", ignore.getMessage());
                    return;
                }

                fail("Should prohibit deleting occupationType " + occupationType);
            });
        }));
    }

    @Test
    public void testDeceasedPerson_CannotCreate() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final Person deadPerson = model().newPerson();
            deadPerson.setDeletionCode(Person.DeletionCode.D);

            thrown.expect(PersonIsDeceasedException.class);

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                occupationCrudFeature.create(createOccupationDTO(rhy, deadPerson, SRVA_YHTEYSHENKILO));
            });
        });
    }

    @Test
    public void testDeceasedPerson_CanUpdate() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final Person deadPerson = model().newPerson();
            final Occupation existingOccupation = model().newOccupation(rhy, deadPerson, SRVA_YHTEYSHENKILO);
            deadPerson.setDeletionCode(Person.DeletionCode.D);

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                occupationCrudFeature.update(OccupationDTO.createWithPerson(existingOccupation));
            });
        });
    }

    @Test
    public void testDeceasedPerson_SkipCandidates() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final Person deadPerson = model().newPerson();
            model().newOccupation(rhy, deadPerson, SRVA_YHTEYSHENKILO);
            deadPerson.setDeletionCode(Person.DeletionCode.D);

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final List<PersonDTO> candidates = occupationCrudFeature.listCandidateForNewOccupation(rhy.getId());

                assertEquals(1, candidates.size());
                assertEquals(coordinator.getId(), candidates.get(0).getId());
            });
        });
    }

    private static OccupationDTO createOccupationDTO(Riistanhoitoyhdistys rhy, Person person, OccupationType type) {
        OccupationDTO dto = new OccupationDTO();
        dto.setOrganisationId(rhy.getId());

        PersonDTO personDTO = new PersonDTO();
        personDTO.setId(person.getId());
        dto.setPerson(personDTO);
        dto.setOccupationType(type);

        return dto;
    }

    @Test
    public void testClubMemberCannotListOccupations() {
        final HuntingClub club = model().newHuntingClub();
        final Person person = model().newPerson();
        model().newOccupation(club, person, OccupationType.SEURAN_JASEN);

        onSavedAndAuthenticated(createUser(person), () -> {
            thrown.expect(AccessDeniedException.class);
            thrown.expectMessage("Cannot list occupations for organisationType CLUB");
            occupationCrudFeature.listOccupations(club.getId());
        });
    }

    @Test
    public void testClubContactPersonCannotListOccupations() {
        final HuntingClub club = model().newHuntingClub();
        final Person person = model().newPerson();
        model().newOccupation(club, person, OccupationType.SEURAN_YHDYSHENKILO);

        onSavedAndAuthenticated(createUser(person), () -> {
            thrown.expect(AccessDeniedException.class);
            thrown.expectMessage("Cannot list occupations for organisationType CLUB");
            occupationCrudFeature.listOccupations(club.getId());
        });
    }

    @Test
    public void testClubMemberCannotListOccupationCandidates() {
        final HuntingClub club = model().newHuntingClub();
        final Person person = model().newPerson();
        model().newOccupation(club, person, OccupationType.SEURAN_JASEN);

        onSavedAndAuthenticated(createUser(person), () -> {
            thrown.expect(AccessDeniedException.class);
            thrown.expectMessage("Cannot list occupations for organisationType CLUB");
            occupationCrudFeature.listCandidateForNewOccupation(club.getId());
        });
    }

    @Test
    public void testClubContactPersonCannotListCandidates() {
        final HuntingClub club = model().newHuntingClub();
        final Person person = model().newPerson();
        model().newOccupation(club, person, OccupationType.SEURAN_YHDYSHENKILO);

        onSavedAndAuthenticated(createUser(person), () -> {
            thrown.expect(AccessDeniedException.class);
            thrown.expectMessage("Cannot list occupations for organisationType CLUB");
            occupationCrudFeature.listCandidateForNewOccupation(club.getId());
        });
    }
}

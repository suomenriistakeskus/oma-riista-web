package fi.riista.feature.organization.occupation;

import fi.riista.feature.account.mobile.MobileOccupationDTO;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.organization.person.PersonIsDeceasedException;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static fi.riista.feature.organization.OrganisationType.ARN;
import static fi.riista.feature.organization.OrganisationType.RHY;
import static fi.riista.feature.organization.OrganisationType.RK;
import static fi.riista.feature.organization.OrganisationType.VRN;
import static fi.riista.feature.organization.occupation.OccupationType.HALLITUKSEN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.METSASTYKSENVALVOJA;
import static fi.riista.feature.organization.occupation.OccupationType.PETOYHDYSHENKILO;
import static fi.riista.feature.organization.occupation.OccupationType.PUHEENJOHTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.SRVA_YHTEYSHENKILO;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static fi.riista.feature.organization.occupation.OccupationType.VARAPUHEENJOHTAJA;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

@RunWith(Theories.class)
public class OccupationCrudFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private OccupationCrudFeature occupationCrudFeature;

    @Resource
    private PersonRepository personRepository;

    @Resource
    private OccupationRepository occupationRepository;

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
                final List<PersonContactInfoDTO> candidates = occupationCrudFeature.listCandidatesForNewOccupation(rhy.getId());

                assertEquals(1, candidates.size());
                assertEquals(coordinator.getId(), candidates.get(0).getId());
            });
        });
    }

    private static OccupationDTO createOccupationDTO(Riistanhoitoyhdistys rhy, Person person, OccupationType type) {
        OccupationDTO dto = new OccupationDTO();
        dto.setOrganisationId(rhy.getId());

        PersonContactInfoDTO personDTO = new PersonContactInfoDTO();
        personDTO.setId(person.getId());
        dto.setPerson(personDTO);
        dto.setOccupationType(type);

        return dto;
    }

    private static OccupationDTO createOccupationDTO(final Organisation org, final Person person, final OccupationType type) {
        final OccupationDTO dto = new OccupationDTO();
        dto.setOrganisationId(org.getId());

        final PersonContactInfoDTO personDTO = new PersonContactInfoDTO();
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
            occupationCrudFeature.listCandidatesForNewOccupation(club.getId());
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
            occupationCrudFeature.listCandidatesForNewOccupation(club.getId());
        });
    }

    @Test
    public void testCreateOccupation_rhyCoordinator() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            Person person = model().newPerson();
            Person substitute = model().newPerson();

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final OccupationDTO boardMemberDTO = createOccupationDTO(rhy, person, PUHEENJOHTAJA);
                final PersonContactInfoDTO substituteDTO = PersonContactInfoDTO.create(substitute);
                boardMemberDTO.setSubstitute(substituteDTO);

                final OccupationDTO outputDTO = occupationCrudFeature.create(boardMemberDTO);
                runInTransaction(() -> {
                    final Person created = personRepository.getOne(outputDTO.getSubstitute().getId());
                    assertNotNull(created);
                    assertEquals(substitute.getSsn(), created.getSsn());
                });
            });
        });
    }

    @Theory
    public void testCreateOccupation_boardMember(final OrganisationType organisationType) {
        assumeTrue(asList(RK, VRN, ARN, RHY).contains(organisationType));

        Organisation org = null;
        switch (organisationType) {
            case RK:
                org = model().getRiistakeskus();
                break;
            case VRN:
                org = model().newValtakunnallinenRiistaneuvosto();
                break;
            case ARN:
                org = model().newAlueellinenRiistaneuvosto();
                break;
            case RHY:
                org = model().newRiistanhoitoyhdistys();
                break;
        }
        final Person person = model().newPerson();
        final Person substitute = model().newPerson();

        final Organisation finalOrg = org;
        onSavedAndAuthenticated(createNewModerator(), () -> {
            final OccupationDTO boardMemberDTO = createOccupationDTO(finalOrg, person, PUHEENJOHTAJA);
            final PersonContactInfoDTO substituteDTO = PersonContactInfoDTO.create(substitute);
            boardMemberDTO.setSubstitute(substituteDTO);

            final OccupationDTO outputDTO = occupationCrudFeature.create(boardMemberDTO);
            runInTransaction(() -> {
                final Person created = personRepository.getOne(outputDTO.getSubstitute().getId());
                assertThat(created, is(notNullValue()));
                assertThat(created.getSsn(), is(equalTo(substitute.getSsn())));
            });
        });
    }

    @Test
    public void testListRhyBoardMemberOccupations() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final Person chair = model().newPerson();
            final Person chairSubstitute = model().newPerson();
            final Occupation chairOccupation = model().newOccupation(rhy, chair, PUHEENJOHTAJA);
            chairOccupation.setSubstitute(chairSubstitute);

            final Person viceChair = model().newPerson();
            final Person viceChairSubstitute = model().newPerson();
            final Occupation viceChairOccupation = model().newOccupation(rhy, viceChair, VARAPUHEENJOHTAJA);
            viceChairOccupation.setSubstitute(viceChairSubstitute);

            final Person member = model().newPerson();
            final Person memberSubstitute = model().newPerson();
            final Occupation memberOccupation = model().newOccupation(rhy, member, HALLITUKSEN_JASEN);
            memberOccupation.setSubstitute(memberSubstitute);

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final List<OccupationDTO> occupations = occupationCrudFeature.listOccupations(rhy.getId());
                final OccupationDTO chairOccupationDTO = getAndAssertType(occupations, PUHEENJOHTAJA, 1).get(0);
                final OccupationDTO viceChairOccupationDTO = getAndAssertType(occupations, VARAPUHEENJOHTAJA, 1).get(0);
                final OccupationDTO memberOccupationDTO = getAndAssertType(occupations, HALLITUKSEN_JASEN, 1).get(0);

                final PersonContactInfoDTO chairSubstituteDTO = chairOccupationDTO.getSubstitute();
                assertEquals(chairSubstitute.getId(), chairSubstituteDTO.getId());

                final PersonContactInfoDTO viceChairSubstituteDTO = viceChairOccupationDTO.getSubstitute();
                assertEquals(viceChairSubstitute.getId(), viceChairSubstituteDTO.getId());

                final PersonContactInfoDTO memberSubstituteDTO = memberOccupationDTO.getSubstitute();
                assertEquals(memberSubstitute.getId(), memberSubstituteDTO.getId());
            });

        });
    }

    @Test
    public void testListOccupationsByType() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final Person person = model().newPerson();
            final Occupation carnivoreContact = model().newOccupation(rhy, person, PETOYHDYSHENKILO);
            model().newOccupation(rhy, person, METSASTYKSENVALVOJA);

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final List<OccupationDTO> occupationList = occupationCrudFeature.listOccupationsByType(rhy.getId(), PETOYHDYSHENKILO);
                assertThat(occupationList, is(notNullValue()));
                assertThat(occupationList, hasSize(1));
                assertThat(occupationList.get(0).getOccupationType(), is(equalTo(carnivoreContact.getOccupationType())));
            });

        });
    }

    private static List<OccupationDTO> getAndAssertType(final List<OccupationDTO> occupations,
                                                        final OccupationType type,
                                                        final long expected) {
        final List<OccupationDTO> occupationsByType = occupations.stream()
                .filter(occ -> occ.getOccupationType() == type)
                .collect(Collectors.toList());
        assertEquals(expected, occupationsByType.size());

        return occupationsByType;
    }

    @Test
    public void testUpdateContactInfoVisibility() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final Person person = model().newPerson();
            final Occupation occupation = model().newOccupation(rhy, person, METSASTYKSENVALVOJA);

            onSavedAndAuthenticated(createUser(person), () -> {
                final OccupationContactInfoVisibilityDTO visibility =
                        new OccupationContactInfoVisibilityDTO(
                                occupation.getId(),
                                occupation.isNameVisibility(),
                                occupation.isPhoneNumberVisibility(),
                                false);
                occupationCrudFeature.updateContactInfoVisibility(Collections.singletonList(visibility));

                runInTransaction(() -> {
                    final Occupation updated = occupationRepository.getOne(occupation.getId());
                    assertThat(updated, is(notNullValue()));

                    assertThat(updated.isNameVisibility(), is(equalTo(occupation.isNameVisibility())));
                    assertThat(updated.isPhoneNumberVisibility(), is(equalTo(occupation.isPhoneNumberVisibility())));
                    assertThat(updated.isEmailVisibility(), is(equalTo(false)));
                });
            });
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateContactInfoVisibility_illegalSetting() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final Person person = model().newPerson();
            final Occupation occupation = model().newOccupation(rhy, person, METSASTYKSENVALVOJA);

            onSavedAndAuthenticated(createUser(person), () -> {
                final OccupationContactInfoVisibilityDTO visibility =
                        new OccupationContactInfoVisibilityDTO(
                                occupation.getId(),
                                false,
                                occupation.isPhoneNumberVisibility(),
                                false);
                occupationCrudFeature.updateContactInfoVisibility(Collections.singletonList(visibility));
            });
        });
    }

    @Test
    public void testListMyClubMemberships() {
        final HuntingClub club = model().newHuntingClub();
        final Person person = model().newPerson();
        model().newOccupation(club, person, OccupationType.SEURAN_JASEN);
        final HuntingClub club2 = model().newHuntingClub();
        model().newOccupation(club2, person, OccupationType.SEURAN_JASEN);
        final Person person2 = model().newPerson();
        model().newOccupation(club, person2, OccupationType.SEURAN_JASEN);

        onSavedAndAuthenticated(createUser(person), () -> {
            final List<MobileOccupationDTO> occupations = occupationCrudFeature.listMyClubMemberships();
            assertThat(occupations, hasSize(2));
        });
    }

}

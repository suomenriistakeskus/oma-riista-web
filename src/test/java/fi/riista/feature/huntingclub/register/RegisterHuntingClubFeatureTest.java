package fi.riista.feature.huntingclub.register;

import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.organization.lupahallinta.LHOrganisation;
import fi.riista.feature.organization.lupahallinta.LHOrganisationSearchDTO;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RegisterHuntingClubFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private RegisterHuntingClubFeature feature;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private HuntingClubRepository huntingClubRepository;

    @Test
    @Ignore("h2 does not support function TRGM_MATCH")
    public void testFindByName() {
    }

    @Test
    public void testFindByOfficialCode() {
        withRhy(rhy -> {
            final LHOrganisation lhOrg = model().newLHOrganisation(rhy);

            final LHOrganisation lhOrgInvalidRhy = model().newLHOrganisation(rhy);
            lhOrgInvalidRhy.setRhyOfficialCode("abc");

            onSavedAndAuthenticated(createNewUser(), () -> {
                //found
                List<LHOrganisationSearchDTO> r = feature.findByOfficialCode(lhOrg.getOfficialCode());
                assertEquals(1, r.size());
                assertEquals(lhOrg.getId(), r.get(0).getId());
                //  having invalid rhy not found
                assertEquals(0, feature.findByOfficialCode(lhOrgInvalidRhy.getOfficialCode()).size());
                //not found
                assertEquals(0, feature.findByOfficialCode("notfound").size());
            });
        });
    }

    @Test
    public void testFindByOfficialCode_existingContactIsPrimary() {
        withRhy(rhy -> {
            final LHOrganisation lhOrg = model().newLHOrganisation(rhy);
            final HuntingClub club = model().newHuntingClub(rhy);
            club.setOfficialCode(lhOrg.getOfficialCode());

            model().newHuntingClubMember(club, OccupationType.SEURAN_YHDYSHENKILO);

            final Occupation deletedPrimaryContact =
                    model().newHuntingClubMember(club, OccupationType.SEURAN_YHDYSHENKILO);
            deletedPrimaryContact.setCallOrder(0);
            deletedPrimaryContact.softDelete();

            final Occupation primaryContact = model().newHuntingClubMember(club, OccupationType.SEURAN_YHDYSHENKILO);
            primaryContact.setCallOrder(0);

            model().newHuntingClubMember(club, OccupationType.SEURAN_YHDYSHENKILO);

            onSavedAndAuthenticated(createNewUser(), () -> {
                List<LHOrganisationSearchDTO> r = feature.findByOfficialCode(club.getOfficialCode());
                assertEquals(1, r.size());
                LHOrganisationSearchDTO dto = r.get(0);
                assertEquals(lhOrg.getId(), dto.getId());
                assertEquals(primaryContact.getPerson().getFullName(), dto.getContactPersonName());
            });
        });
    }

    @Test
    public void testFindByOfficialCode_deactiveNotFound() {
        withRhy(rhy -> {
            final LHOrganisation lhOrg = model().newLHOrganisation(rhy);
            final HuntingClub club = model().newHuntingClub(rhy);
            club.setOfficialCode(lhOrg.getOfficialCode());

            final LHOrganisation lhOrg2 = model().newLHOrganisation(rhy);
            final HuntingClub club2 = model().newHuntingClub(rhy);
            club2.setOfficialCode(lhOrg2.getOfficialCode());
            club2.setActive(false);

            onSavedAndAuthenticated(createNewUser(), () -> {
                List<LHOrganisationSearchDTO> r = feature.findByOfficialCode(lhOrg.getOfficialCode());
                assertEquals(1, r.size());
                assertEquals(lhOrg.getId(), r.get(0).getId());

                assertEquals(0, feature.findByOfficialCode(lhOrg2.getOfficialCode()).size());
            });
        });
    }

    @Test
    public void testRegister_ok_clubCreated() {
        final LHOrganisation lhOrg = model().newLHOrganisation();
        withPerson(person -> onSavedAndAuthenticated(createUser(person), () -> {
            assertSuccess(feature.register(dto(lhOrg)));

            runInTransaction(() -> {
                HuntingClub c = huntingClubRepository.findByOfficialCode(lhOrg.getOfficialCode());
                assertClubEqualsLhOrg(lhOrg, c);
                assertYhteyshenkilo(1, c, person);
            });
        }));
    }

    @Test
    public void testRegisterAsModerator_ok_clubCreated() {
        final LHOrganisation lhOrg = model().newLHOrganisation();
        withPerson(person -> onSavedAndAuthenticated(createNewAdmin(), () -> {
            final LHOrganisationSearchDTO dto = dto(lhOrg);
            dto.setPersonId(person.getId());
            assertSuccess(feature.register(dto));

            runInTransaction(() -> {
                HuntingClub c = huntingClubRepository.findByOfficialCode(lhOrg.getOfficialCode());
                assertClubEqualsLhOrg(lhOrg, c);
                assertYhteyshenkilo(1, c, person);
            });
        }));
    }

    @Test
    public void testRegister_ok_clubExists() {
        withPerson(person -> withRhy(rhy -> {
            final LHOrganisation lhOrg = model().newLHOrganisation(rhy);
            final HuntingClub club = model().newHuntingClub(rhy);
            club.setOfficialCode(lhOrg.getOfficialCode());

            onSavedAndAuthenticated(createUser(person), () -> {
                assertSuccess(feature.register(dto(lhOrg)));

                runInTransaction(() -> {
                    HuntingClub c = huntingClubRepository.findByOfficialCode(lhOrg.getOfficialCode());
                    assertClubsEquals(club, c);
                    assertYhteyshenkilo(1, c, person);
                });
            });
        }));
    }

    @Test
    public void testRegister_nok_alreadyRegistered() {
        withPerson(person -> withRhy(rhy -> {
            final LHOrganisation lhOrg = model().newLHOrganisation(rhy);
            final HuntingClub club = model().newHuntingClub(rhy);
            club.setOfficialCode(lhOrg.getOfficialCode());

            model().newHuntingClubMember(club, OccupationType.SEURAN_YHDYSHENKILO);

            onSavedAndAuthenticated(createUser(person), () -> {
                assertFailure(feature.register(dto(lhOrg)));
                runInTransaction(() -> assertYhteyshenkilo(0, club, person));
            });
        }));
    }

    @Test
    public void testRegister_ok_clubMembershipExists() {
        withRhy(rhy -> {
            final LHOrganisation lhOrganisation = model().newLHOrganisation(rhy);
            final HuntingClub huntingClub = model().newHuntingClub(rhy);
            huntingClub.setOfficialCode(lhOrganisation.getOfficialCode());

            final Occupation existingMembership =
                    model().newHuntingClubMember(huntingClub, OccupationType.SEURAN_JASEN);
            final Person person = existingMembership.getPerson();

            onSavedAndAuthenticated(createUser(person), () -> {
                assertSuccess(feature.register(dto(lhOrganisation)));

                runInTransaction(() -> {
                    assertEquals(1, occupationRepository.countActiveByTypeAndPersonAndOrganizationIn(
                            Collections.singleton(huntingClub.getId()),
                            person,
                            EnumSet.of(OccupationType.SEURAN_YHDYSHENKILO)));

                    assertEquals(0, occupationRepository.countActiveByTypeAndPersonAndOrganizationIn(
                            Collections.singleton(huntingClub.getId()),
                            person,
                            EnumSet.of(OccupationType.SEURAN_JASEN)));
                });
            });
        });
    }

    private static void assertSuccess(Map<String, Object> res) {
        assertEquals(2, res.size());
        assertEquals("success", res.get("result"));
        assertNotNull(res.get("clubId"));
    }

    private static void assertFailure(Map<String, Object> res) {
        assertEquals("exists", res.get("result"));
    }

    public static void assertClubEqualsLhOrg(LHOrganisation lhOrg, HuntingClub c) {
        assertNotNull(c);
        assertEquals(lhOrg.getOfficialCode(), c.getOfficialCode());
        assertEquals(lhOrg.getNameFinnish(), c.getNameFinnish());
        assertEquals(lhOrg.getNameSwedish(), c.getNameSwedish());
        assertEquals(lhOrg.getRhyOfficialCode(), c.getParentOrganisation().getOfficialCode());
    }

    public static void assertClubsEquals(HuntingClub a, HuntingClub b) {
        assertEquals(a.getId(), b.getId());
        assertEquals(a.getNameFinnish(), b.getNameFinnish());
        assertEquals(a.getNameSwedish(), b.getNameSwedish());
        assertEquals(a.getParentOrganisation().getId(), b.getParentOrganisation().getId());
    }

    private void assertYhteyshenkilo(int amount, HuntingClub c, Person person) {
        final List<Occupation> clubOccupations = occupationRepository.findActiveByOrganisationAndPerson(c, person);
        assertEquals(amount, clubOccupations.size());

        for (Occupation clubOccupation : clubOccupations) {
            assertEquals(OccupationType.SEURAN_YHDYSHENKILO, clubOccupation.getOccupationType());
            assertEquals(0, (long) clubOccupation.getCallOrder());
        }
    }

    private static LHOrganisationSearchDTO dto(LHOrganisation lhOrg) {
        return LHOrganisationSearchDTO.create(lhOrg);
    }
}

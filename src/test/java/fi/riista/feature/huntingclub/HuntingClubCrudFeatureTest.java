package fi.riista.feature.huntingclub;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gis.HtaNotResolvableByGeoLocationException;
import fi.riista.feature.gis.MockGISQueryService;
import fi.riista.feature.gis.RhyNotResolvableByGeoLocationException;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.gis.hta.GISHirvitalousalueDTO;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HuntingClubCrudFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HuntingClubCrudFeature clubCrudFeature;

    @Resource
    private HuntingClubRepository clubRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Test
    public void testCreate() {
        withPerson(person -> withRhy(rhy -> {
            final GISHirvitalousalue hta = model().newGISHirvitalousalue();
            rhy.setGeoLocation(new GeoLocation(1, 2));

            final CreateHuntingClubDTO create = new CreateHuntingClubDTO();
            create.setNameFI("clubNameFi");
            create.setNameSV("clubNameSv");
            create.setGeoLocation(rhy.getGeoLocation());

            onSavedAndAuthenticated(createUser(person), () -> {
                final HuntingClubDTO dto = clubCrudFeature.create(create.toHuntingClubDTO());
                assertEquals(create.getNameFI(), dto.getNameFI());
                assertEquals(create.getNameSV(), dto.getNameSV());
                assertEquals(rhy.getId(), dto.getRhy().getId());
                assertEquals(hta.getNumber(), dto.getMooseArea().getNumber());
                assertTrue(Long.parseLong(dto.getOfficialCode()) > 5_000_000);

                runInTransaction((() -> {
                    final HuntingClub club = clubRepository.getOne(dto.getId());
                    final List<Occupation> clubOccupations =
                            occupationRepository.findActiveByOrganisationAndPerson(club, person);

                    assertEquals(1, clubOccupations.size());

                    for (Occupation clubOccupation : clubOccupations) {
                        assertEquals(OccupationType.SEURAN_YHDYSHENKILO, clubOccupation.getOccupationType());
                        assertEquals(0, (long) clubOccupation.getCallOrder());
                    }
                }));
            });
        }));
    }

    @Test(expected = RhyNotResolvableByGeoLocationException.class)
    public void testCreate_rhyNotFound() {
        withPerson(person -> withRhy(rhy -> {
            final GISHirvitalousalue hta = model().newGISHirvitalousalue();
            rhy.setGeoLocation(MockGISQueryService.RHY_GEOLOCATION_NOT_FOUND);

            final CreateHuntingClubDTO create = new CreateHuntingClubDTO();
            create.setNameFI("clubNameFi");
            create.setNameSV("clubNameSv");
            create.setGeoLocation(rhy.getGeoLocation());

            onSavedAndAuthenticated(createUser(person), () -> clubCrudFeature.create(create.toHuntingClubDTO()));
        }));
    }

    @Test(expected = HtaNotResolvableByGeoLocationException.class)
    public void testCreate_htaNotFound() {
        withPerson(person -> withRhy(rhy -> {
            final GISHirvitalousalue hta = model().newGISHirvitalousalue();
            rhy.setGeoLocation(MockGISQueryService.HTA_GEOLOCATION_NOT_FOUND);

            final CreateHuntingClubDTO create = new CreateHuntingClubDTO();
            create.setNameFI("clubNameFi");
            create.setNameSV("clubNameSv");
            create.setGeoLocation(rhy.getGeoLocation());

            onSavedAndAuthenticated(createUser(person), () -> clubCrudFeature.create(create.toHuntingClubDTO()));
        }));
    }


    @Test
    public void testUpdate_contactPerson() {
        withPerson(person -> {
            final HuntingClub club = model().newHuntingClub();
            model().newOccupation(club, person, OccupationType.SEURAN_YHDYSHENKILO);

            onSavedAndAuthenticated(createUser(person), () -> {
                final HuntingClubDTO dto = HuntingClubDTO.create(club, false, null, null);
                final String originalName = dto.getNameFI();
                dto.setNameFI("edited");

                clubCrudFeature.update(dto);
                // assert that name is NOT updated because user is not moderator
                runInTransaction(() -> assertEquals(originalName, clubRepository.getOne(club.getId()).getNameFinnish()));
            });
        });
    }

    @Test
    public void testUpdate_moderator() {
        final HuntingClub club = model().newHuntingClub();

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final HuntingClubDTO dto = HuntingClubDTO.create(club, false, null, null);
            dto.setNameFI("edited");

            clubCrudFeature.update(dto);

            runInTransaction(() -> assertEquals("edited", clubRepository.getOne(club.getId()).getNameFinnish()));
        });
    }

    @Test
    public void testUpdate_rhyAndHtaNotChanged() {
        withPerson(person -> {
            withRhy(originalRhy -> {
                final HuntingClub club = model().newHuntingClub(originalRhy);
                final GISHirvitalousalue originalHta = model().newGISHirvitalousalue();
                club.setMooseArea(originalHta);

                withRhy(newRhy -> {
                    final GISHirvitalousalue newHta = model().newGISHirvitalousalue();

                    model().newOccupation(club, person, OccupationType.SEURAN_YHDYSHENKILO);

                    onSavedAndAuthenticated(createUser(person), () -> {
                        final GISHirvitalousalueDTO htaDto = new GISHirvitalousalueDTO();
                        htaDto.setNumber(newHta.getNumber());

                        final HuntingClubDTO dto = HuntingClubDTO.create(club, false, null, htaDto);
                        dto.getRhy().setOfficialCode(newRhy.getOfficialCode());

                        clubCrudFeature.update(dto);

                        runInTransaction(() -> {
                            final HuntingClub updated = clubRepository.getOne(club.getId());
                            assertEquals(originalRhy.getId(), updated.getParentOrganisation().getId());
                            assertEquals(originalHta.getId(), updated.getMooseArea().getId());
                        });
                    });
                });
            });
        });
    }

}

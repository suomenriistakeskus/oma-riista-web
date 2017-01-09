package fi.riista.feature.huntingclub;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gis.HtaNotResolvableByGeoLocationException;
import fi.riista.feature.gis.MockGISQueryService;
import fi.riista.feature.gis.RhyNotResolvableByGeoLocationException;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.gis.hta.GISHirvitalousalueDTO;
import fi.riista.feature.organization.occupation.OccupationType;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HuntingClubCrudFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HuntingClubCrudFeature feature;

    @Resource
    private HuntingClubRepository repository;

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
                final HuntingClubDTO dto = feature.create(create.toHuntingClubDTO());
                assertEquals(create.getNameFI(), dto.getNameFI());
                assertEquals(create.getNameSV(), dto.getNameSV());
                assertEquals(rhy.getId(), dto.getRhy().getId());
                assertEquals(hta.getNumber(), dto.getMooseArea().getNumber());
                assertTrue(Long.parseLong(dto.getOfficialCode()) > 5_000_000);
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

            onSavedAndAuthenticated(createUser(person), () -> feature.create(create.toHuntingClubDTO()));
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

            onSavedAndAuthenticated(createUser(person), () -> feature.create(create.toHuntingClubDTO()));
        }));
    }


    @Test
    public void testUpdate() {
        withPerson(person -> {
            final HuntingClub club = model().newHuntingClub();
            model().newOccupation(club, person, OccupationType.SEURAN_YHDYSHENKILO);

            onSavedAndAuthenticated(createUser(person), () -> {
                final HuntingClubDTO dto = HuntingClubDTO.create(club, false, null, null);
                dto.setNameFI("edited");

                feature.update(dto);

                runInTransaction(() -> assertEquals("edited", repository.getOne(club.getId()).getNameFinnish()));
            });
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

                        feature.update(dto);

                        runInTransaction(() -> {
                            final HuntingClub updated = repository.getOne(club.getId());
                            assertEquals(originalRhy.getId(), updated.getParentOrganisation().getId());
                            assertEquals(originalHta.getId(), updated.getMooseArea().getId());
                        });
                    });
                });
            });
        });
    }

}

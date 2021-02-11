package fi.riista.feature.huntingclub.area;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZone.SourceType;
import fi.riista.feature.gis.zone.GISZoneConstants;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.feature.gis.zone.GISZoneSizeDTO.Status.FAILED;
import static fi.riista.feature.gis.zone.GISZoneSizeDTO.Status.OK;
import static fi.riista.feature.gis.zone.GISZoneSizeDTO.Status.PROCESSING;
import static fi.riista.util.NumberUtils.EPSILON;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class HuntingClubAreaDTOTransformerTest extends EmbeddedDatabaseTest {

    @Resource
    private HuntingClubAreaDTOTransformer transformer;

    @Test
    public void testZoneMapping() {
        withRhy(rhy -> {
            final HuntingClub club1 = model().newHuntingClub(rhy);
            final HuntingClub club2 = model().newHuntingClub(rhy);
            final HuntingClub club3 = model().newHuntingClub(rhy);
            final HuntingClub club4 = model().newHuntingClub(rhy);
            final HuntingClub club5 = model().newHuntingClub(rhy);

            final HuntingClubArea club1Area = model().newHuntingClubArea(club1);
            final HuntingClubArea club2Area = model().newHuntingClubArea(club2);
            final HuntingClubArea club3Area = model().newHuntingClubArea(club3);
            final HuntingClubArea club4Area = model().newHuntingClubArea(club4);
            final HuntingClubArea club5Area = model().newHuntingClubArea(club5);

            final GISZone club1Zone = model().newGISZone(100);
            club1Area.setZone(club1Zone);

            final GISZone club2Zone = model().newGISZone(200);
            club2Zone.setSourceType(SourceType.EXTERNAL);
            club2Area.setZone(club2Zone);

            final GISZone club4Zone = model().newGISZone(200);
            club4Zone.setComputedAreaSize(GISZoneConstants.AREA_SIZE_NOT_AVAILABLE);
            club4Zone.setWaterAreaSize(GISZoneConstants.AREA_SIZE_NOT_AVAILABLE);
            club4Zone.setStateLandAreaSize(null);
            club4Zone.setPrivateLandAreaSize(null);
            club4Area.setZone(club4Zone);

            final GISZone club5Zone = model().newGISZone(200);
            club5Zone.setComputedAreaSize(GISZoneConstants.AREA_SIZE_CALCULATION_FAILED);
            club5Zone.setWaterAreaSize(GISZoneConstants.AREA_SIZE_CALCULATION_FAILED);
            club5Zone.setStateLandAreaSize(null);
            club5Zone.setPrivateLandAreaSize(null);
            club5Area.setZone(club5Zone);

            onSavedAndAuthenticated(createNewAdmin(), () -> {
                final List<HuntingClubAreaDTO> results = transformer.apply(asList(club1Area, club2Area, club3Area, club4Area, club5Area));
                assertThat(results, hasSize(5));

                final HuntingClubAreaDTO club1AreaDTO = results.get(0);

                assertEquals(club1Zone.getId(), club1AreaDTO.getZoneId());
                assertEquals(club1Zone.getComputedAreaSize(), club1AreaDTO.getSize().getAll().getTotal(), EPSILON);
                assertEquals(club1Zone.getWaterAreaSize(), club1AreaDTO.getSize().getAll().getWater(), EPSILON);
                assertEquals(club1Zone.getStateLandAreaSize(), club1AreaDTO.getSize().getStateLandAreaSize(), EPSILON);
                assertEquals(club1Zone.getPrivateLandAreaSize(), club1AreaDTO.getSize().getPrivateLandAreaSize(), EPSILON);
                assertEquals(club1Zone.getSourceType(), club1AreaDTO.getSourceType());
                assertEquals(OK, club1AreaDTO.getSize().getStatus());

                final HuntingClubAreaDTO club2AreaDTO = results.get(1);

                assertEquals(club2Zone.getId(), club2AreaDTO.getZoneId());
                assertEquals(club2Zone.getComputedAreaSize(), club2AreaDTO.getSize().getAll().getTotal(), EPSILON);
                assertEquals(club2Zone.getWaterAreaSize(), club2AreaDTO.getSize().getAll().getWater(), EPSILON);
                assertEquals(club2Zone.getStateLandAreaSize(), club2AreaDTO.getSize().getStateLandAreaSize(), EPSILON);
                assertEquals(club2Zone.getPrivateLandAreaSize(), club2AreaDTO.getSize().getPrivateLandAreaSize(), EPSILON);
                assertEquals(club2Zone.getSourceType(), club2AreaDTO.getSourceType());
                assertEquals(OK, club2AreaDTO.getSize().getStatus());

                final HuntingClubAreaDTO club3AreaDTO = results.get(2);
                assertNull(club3AreaDTO.getZoneId());
                assertEquals(SourceType.LOCAL, club3AreaDTO.getSourceType());
                assertEquals(0, club3AreaDTO.getSize().getAll().getTotal(), EPSILON);
                assertEquals(0, club3AreaDTO.getSize().getAll().getWater(), EPSILON);
                assertEquals(0, club3AreaDTO.getSize().getStateLandAreaSize(), EPSILON);
                assertEquals(0, club3AreaDTO.getSize().getPrivateLandAreaSize(), EPSILON);
                assertEquals(OK, club3AreaDTO.getSize().getStatus());

                final HuntingClubAreaDTO club4AreaDTO = results.get(3);
                assertEquals(club4Zone.getId(), club4AreaDTO.getZoneId());
                assertEquals(SourceType.LOCAL, club4AreaDTO.getSourceType());
                assertEquals(0, club4AreaDTO.getSize().getAll().getTotal(), EPSILON);
                assertEquals(0, club4AreaDTO.getSize().getAll().getWater(), EPSILON);
                assertEquals(0, club4AreaDTO.getSize().getStateLandAreaSize(), EPSILON);
                assertEquals(0, club4AreaDTO.getSize().getPrivateLandAreaSize(), EPSILON);
                assertEquals(PROCESSING, club4AreaDTO.getSize().getStatus());

                final HuntingClubAreaDTO club5AreaDTO = results.get(4);
                assertEquals(club5Zone.getId(), club5AreaDTO.getZoneId());
                assertEquals(SourceType.LOCAL, club5AreaDTO.getSourceType());
                assertEquals(0, club5AreaDTO.getSize().getAll().getTotal(), EPSILON);
                assertEquals(0, club5AreaDTO.getSize().getAll().getWater(), EPSILON);
                assertEquals(0, club5AreaDTO.getSize().getStateLandAreaSize(), EPSILON);
                assertEquals(0, club5AreaDTO.getSize().getPrivateLandAreaSize(), EPSILON);
                assertEquals(FAILED, club5AreaDTO.getSize().getStatus());
            });
        });
    }

    @Test
    public void testModifierMapping() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();

        final SystemUser admin = createNewAdmin();
        admin.setFirstName("A");
        admin.setLastName("B");

        onSavedAndAuthenticated(admin, () -> {

            final HuntingClub club1 = model().newHuntingClub(rhy);
            final HuntingClubArea club1Area = model().newHuntingClubArea(club1);

            final SystemUser moderator = createNewModerator();
            moderator.setFirstName("C");
            moderator.setLastName("D");

            onSavedAndAuthenticated(moderator, () -> withPerson(clubContact -> {

                final HuntingClub club2 = model().newHuntingClub(rhy);
                final HuntingClubArea club2Area = model().newHuntingClubArea(club2);

                onSavedAndAuthenticated(createUser(clubContact), () -> {

                    final HuntingClub club3 = model().newHuntingClub(rhy);
                    final HuntingClubArea club3Area = model().newHuntingClubArea(club3);
                    model().newOccupation(club3, clubContact, OccupationType.SEURAN_YHDYSHENKILO);

                    persistInNewTransaction();

                    final List<HuntingClubAreaDTO> results = transformer.apply(asList(club1Area, club2Area, club3Area));
                    assertThat(results, hasSize(3));

                    assertEquals(admin.getFullName(), results.get(0).getLastModifierName());
                    assertTrue(results.get(0).isLastModifierRiistakeskus());

                    assertEquals(moderator.getFullName(), results.get(1).getLastModifierName());
                    assertTrue(results.get(1).isLastModifierRiistakeskus());

                    assertEquals(clubContact.getFullName(), results.get(2).getLastModifierName());
                    assertFalse(results.get(2).isLastModifierRiistakeskus());
                });
            }));
        });
    }
}

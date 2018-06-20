package fi.riista.feature.huntingclub.area;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZone.SourceType;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import io.vavr.Tuple;
import io.vavr.Tuple4;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.util.NumberUtils.EPSILON;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

            final HuntingClubArea club1Area = model().newHuntingClubArea(club1);
            final HuntingClubArea club2Area = model().newHuntingClubArea(club2);
            final HuntingClubArea club3Area = model().newHuntingClubArea(club3);

            final GISZone club1Zone = model().newGISZone(100);
            club1Area.setZone(club1Zone);

            final GISZone club2Zone = model().newGISZone(200);
            club2Zone.setSourceType(SourceType.EXTERNAL);
            club2Area.setZone(club2Zone);

            onSavedAndAuthenticated(createNewAdmin(), () -> {
                final List<HuntingClubAreaDTO> results = transformer.apply(asList(club1Area, club2Area, club3Area));
                assertEquals(3, results.size());

                assertZoneMapping(results.get(0), toTuple(club1Zone));
                assertZoneMapping(results.get(1), toTuple(club2Zone));
                assertZoneMapping(results.get(2), Tuple.of(null, 0.0, 0.0, SourceType.LOCAL));
            });
        });
    }

    private static void assertZoneMapping(final HuntingClubAreaDTO dto,
                                          final Tuple4<Long, Double, Double, SourceType> tuple) {

        assertEquals(tuple._1, dto.getZoneId());
        assertEquals(tuple._2, dto.getComputedAreaSize(), EPSILON);
        assertEquals(tuple._3, dto.getWaterAreaSize(), EPSILON);
        assertEquals(tuple._4, dto.getSourceType());
    }

    private static Tuple4<Long, Double, Double, SourceType> toTuple(final GISZone zone) {
        return Tuple.of(zone.getId(), zone.getComputedAreaSize(), zone.getWaterAreaSize(), zone.getSourceType());
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
                    assertEquals(3, results.size());

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

package fi.riista.feature.gis.mobile;

import fi.riista.feature.account.area.PersonalArea;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.moderatorarea.ModeratorArea;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.LocalisedString;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MobileMapFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private MobileMapFeature feature;

    @Test
    public void testFindsHuntingClubArea() {
        final GISZone gisZone = model().newGISZone(1000);
        final HuntingClub club = model().newHuntingClub();
        final HuntingClubArea huntingClubArea = model().newHuntingClubArea(club, gisZone);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final MobileAreaDTO mobileAreaDTO = feature.findByExternalId(huntingClubArea.getExternalId());

            assertNotNull(mobileAreaDTO);
            assertEquals(MobileAreaDTO.AreaType.CLUB, mobileAreaDTO.getType());
            assertEquals(huntingClubArea.getExternalId(), mobileAreaDTO.getExternalId());
            assertEquals(huntingClubArea.getHuntingYear(), mobileAreaDTO.getHuntingYear());
            assertEquals(huntingClubArea.getNameLocalisation().asMap(), mobileAreaDTO.getName());
            assertEquals(club.getNameLocalisation().asMap(), mobileAreaDTO.getClubName());
            assertEquals(DateUtil.toLocalDateTimeNullSafe(huntingClubArea.getModificationTime()),
                         mobileAreaDTO.getModificationTime());
        });
    }

    @Test
    public void testFindsHarvestPermitArea() {
        final GISZone gisZone = model().newGISZone(1000);
        final HarvestPermitArea harvestPermitArea = model().newHarvestPermitArea();
        harvestPermitArea.setZone(gisZone);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final MobileAreaDTO mobileAreaDTO = feature.findByExternalId(harvestPermitArea.getExternalId());

            assertNotNull(mobileAreaDTO);
            assertEquals(MobileAreaDTO.AreaType.PERMIT, mobileAreaDTO.getType());
            assertEquals(harvestPermitArea.getExternalId(), mobileAreaDTO.getExternalId());
            assertEquals(harvestPermitArea.getHuntingYear(), mobileAreaDTO.getHuntingYear());
            assertEquals(LocalisedString.of(harvestPermitArea.getExternalId(), harvestPermitArea.getExternalId()).asMap(), mobileAreaDTO.getName());
            assertEquals(MobileAreaDTO.PERMIT_AREA_NAME.asMap(), mobileAreaDTO.getClubName());
            assertEquals(DateUtil.toLocalDateTimeNullSafe(harvestPermitArea.getModificationTime()),
                         mobileAreaDTO.getModificationTime());
        });
    }

    @Test
    public void testFindsPersonalArea() {
        final PersonalArea personalArea = model().newPersonalArea();

        onSavedAndAuthenticated(createNewUser(), () -> {
            final MobileAreaDTO mobileAreaDTO = feature.findByExternalId(personalArea.getExternalId());

            assertNotNull(mobileAreaDTO);
            assertEquals(MobileAreaDTO.AreaType.CLUB, mobileAreaDTO.getType());
            assertEquals(personalArea.getExternalId(), mobileAreaDTO.getExternalId());
            assertEquals(DateUtil.huntingYear(), mobileAreaDTO.getHuntingYear());
            assertEquals(LocalisedString.of(personalArea.getName(), personalArea.getName()).asMap(),
                         mobileAreaDTO.getName());
            assertEquals(DateUtil.toLocalDateTimeNullSafe(personalArea.getModificationTime()),
                         mobileAreaDTO.getModificationTime());
        });
    }

    @Test
    public void testFindsModeratorArea() {
        final SystemUser moderator = createNewModerator();
        final ModeratorArea moderatorArea = model().newModeratorArea(moderator);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final MobileAreaDTO mobileAreaDTO = feature.findByExternalId(moderatorArea.getExternalId());

            assertNotNull(mobileAreaDTO);
            assertEquals(MobileAreaDTO.AreaType.PERMIT, mobileAreaDTO.getType());
            assertEquals(moderatorArea.getExternalId(), mobileAreaDTO.getExternalId());
            assertEquals(moderatorArea.getYear(), mobileAreaDTO.getHuntingYear());
            assertEquals(LocalisedString.of(moderatorArea.getName(), moderatorArea.getName()).asMap(),
                         mobileAreaDTO.getName());
            assertEquals(MobileAreaDTO.PERMIT_AREA_NAME.asMap(), mobileAreaDTO.getClubName());
            assertEquals(DateUtil.toLocalDateTimeNullSafe(moderatorArea.getModificationTime()),
                         mobileAreaDTO.getModificationTime());
        });
    }
}

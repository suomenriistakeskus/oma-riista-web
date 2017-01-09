package fi.riista.feature.huntingclub.permit.harvestreport;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.support.HuntingClubTestDataHelper;
import fi.riista.feature.huntingclub.permit.HasHarvestCountsForPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.util.DateUtil;
import fi.riista.util.MediaTypeExtras;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;

import javax.annotation.Resource;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class MooseHarvestReportCrudFeatureTest extends EmbeddedDatabaseTest {

    private enum Action {
        ATTACH_FILE,
        NO_HARVESTS,
        MODERATOR_OVERRIDE
    }

    @Resource
    private MooseHarvestReportCrudFeature feature;

    private HuntingClubTestDataHelper helper = new HuntingClubTestDataHelper() {
        @Override protected EntitySupplier model() {
            return MooseHarvestReportCrudFeatureTest.this.model();
        }
    };

    @Test
    public void testReceiptAttachedHarvestReport_withHarvests() {
        doTest(true, Action.ATTACH_FILE);
    }

    @Test(expected = MooseHarvestReportException.class)
    public void testReceiptAttachedHarvestReport_noHarvests() {
        doTest(false, Action.ATTACH_FILE);
    }

    @Test
    public void testNoHarvestsHarvestReport_noHarvests() {
        doTest(false, Action.NO_HARVESTS);
    }

    @Test(expected = MooseHarvestReportException.class)
    public void testNoHarvestsHarvestReport_withHarvests() {
        doTest(true, Action.NO_HARVESTS);
    }

    @Test
    public void testModeratorOverride() {
        doTest(true, Action.MODERATOR_OVERRIDE);
    }

    @Test(expected = MooseHarvestReportException.class)
    public void testModeratorOverride_noHarvests() {
        doTest(true, Action.MODERATOR_OVERRIDE, dto -> {
            dto.setNoHarvests(true);
            return dto;
        });
    }

    private void doTest(boolean createHarvest, Action action) {
        doTest(createHarvest, action, Function.identity());
    }

    private void doTest(boolean createHarvest, Action action, Function<MooseHarvestReportDTO, MooseHarvestReportDTO> dtoMutator) {
        final HarvestPermit permit = model().newHarvestPermit();

        final GameSpecies species = model().newGameSpeciesMoose();
        final int year = DateUtil.getFirstCalendarYearOfCurrentHuntingYear();
        model().newHarvestPermitSpeciesAmount(permit, species, year);
        model().newMooselikePrice(year, species);

        final HuntingClub club = model().newHuntingClub(permit.getRhy());
        permit.getPermitPartners().add(club);
        final HuntingClubGroup group = model().newHuntingClubGroup(club, species);
        group.updateHarvestPermit(permit);
        if (createHarvest) {
            HasHarvestCountsForPermit counts = HasHarvestCountsForPermit.of(6, 5, 4, 3, 2, 1);
            helper.createHarvestsForHuntingGroup(group, model().newPerson(), counts);
        }

        persistAndAuthenticateWithNewUser(SystemUser.Role.ROLE_MODERATOR);

        final MooseHarvestReportDTO dto = feature.create(dtoMutator.apply(createDto(action, permit)));
        assertEquals(permit.getId(), dto.getHarvestPermitId());
    }

    private static MooseHarvestReportDTO createDto(Action action, HarvestPermit permit) {
        switch (action) {
            case ATTACH_FILE:
                return createDtoWithReceipt(permit);
            case NO_HARVESTS:
                return createDtoWithNoHarvests(permit);
            case MODERATOR_OVERRIDE:
                return createDtoWithModeratorOverride(permit);
        }
        throw new IllegalStateException("Unknown enum type:" + action);
    }

    private static MooseHarvestReportDTO createDtoWithReceipt(HarvestPermit permit) {
        return MooseHarvestReportDTO.withReceipt(permit.getId(), GameSpecies.OFFICIAL_CODE_MOOSE, mockFile());
    }

    private static MooseHarvestReportDTO createDtoWithNoHarvests(HarvestPermit permit) {
        return MooseHarvestReportDTO.withNoHarvests(permit.getId(), GameSpecies.OFFICIAL_CODE_MOOSE);
    }

    private static MooseHarvestReportDTO createDtoWithModeratorOverride(HarvestPermit permit) {
        return MooseHarvestReportDTO.withModeratorOverride(permit.getId(), GameSpecies.OFFICIAL_CODE_MOOSE);
    }

    private static MockMultipartFile mockFile() {
        return new MockMultipartFile("receipt", "receipt", MediaTypeExtras.APPLICATION_PDF_VALUE, new byte[]{1, 2, 3});
    }
}

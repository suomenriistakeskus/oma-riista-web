package fi.riista.feature.gamediary.harvest.mutation.basic;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.mutation.HarvestMutationRole;
import fi.riista.feature.gamediary.harvest.mutation.exception.HarvestSpeciesChangeForbiddenException;
import fi.riista.feature.gamediary.mobile.MobileHarvestDTO;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HarvestCommonMutationTest {

    private static HarvestDTO createDto(final LocalDateTime pointOfTime, final int amount) {
        final HarvestDTO dto = new HarvestDTO();
        dto.setPointOfTime(pointOfTime);
        dto.setAmount(amount);
        return dto;
    }

    private static MobileHarvestDTO createMobileDto(final LocalDateTime pointOfTime, final int amount) {
        final MobileHarvestDTO dto = new MobileHarvestDTO();
        dto.setPointOfTime(pointOfTime);
        dto.setAmount(amount);
        return dto;
    }

    private LocalDateTime now;

    @Before
    public void init() {
        this.now = DateUtil.localDateTime();
    }

    @Test
    public void testMobile() {
        doMobileCreate(10, 10);
    }

    private void doMobileCreate(final int dtoAmount, final int expectedAmount) {
        final Harvest harvest = new Harvest();
        final MobileHarvestDTO dto = createMobileDto(now, dtoAmount);
        final GameSpecies species = new GameSpecies();
        new HarvestCommonMutation(dto, species, HarvestMutationRole.AUTHOR_OR_ACTOR).accept(harvest);

        assertEquals(species, harvest.getSpecies());
        assertEquals(now, harvest.getPointOfTime().toLocalDateTime());
        assertEquals(expectedAmount, harvest.getAmount());
        assertFalse(harvest.isModeratorOverride());
    }

    @Test
    public void testWeb_Create() {
        doWebCreate(HarvestMutationRole.AUTHOR_OR_ACTOR);
    }

    private void doWebCreate(final HarvestMutationRole mutationRole) {
        final Harvest harvest = new Harvest();
        final HarvestDTO dto = createDto(now, 10);

        final GameSpecies species = new GameSpecies();
        new HarvestCommonMutation(dto, species, mutationRole).accept(harvest);

        assertEquals(species, harvest.getSpecies());
        assertEquals(now, harvest.getPointOfTime().toLocalDateTime());
        assertEquals(10, harvest.getAmount());
        assertEquals(mutationRole == HarvestMutationRole.MODERATOR, harvest.isModeratorOverride());
    }

    @Test
    public void testWeb_Update_CanUpdateSpeciesAsAuthor() {
        doWebHarvestUpdate(HarvestMutationRole.AUTHOR_OR_ACTOR);
    }

    @Test(expected = HarvestSpeciesChangeForbiddenException.class)
    public void testWeb_Update_SpeciesUpdateForbiddenForModerator() {
        doWebHarvestUpdate(HarvestMutationRole.MODERATOR);
    }

    @Test(expected = HarvestSpeciesChangeForbiddenException.class)
    public void testWeb_Update_SpeciesUpdateForbiddenForContactPerson() {
        doWebHarvestUpdate(HarvestMutationRole.PERMIT_CONTACT_PERSON);
    }

    @Test(expected = HarvestSpeciesChangeForbiddenException.class)
    public void testWeb_Update_SpeciesUpdateForbiddenForOther() {
        doWebHarvestUpdate(HarvestMutationRole.OTHER);
    }

    private void doWebHarvestUpdate(final HarvestMutationRole mutationRole) {
        final Harvest harvest = new Harvest();
        harvest.setAmount(10);
        harvest.setSpecies(new GameSpecies());

        final HarvestDTO dto = createDto(now, 20);

        final GameSpecies species = new GameSpecies();
        new HarvestCommonMutation(dto, species, mutationRole).accept(harvest);

        assertEquals(species, harvest.getSpecies());
        assertEquals(now, harvest.getPointOfTime().toLocalDateTime());
        assertEquals(20, harvest.getAmount());
        assertEquals(mutationRole == HarvestMutationRole.MODERATOR, harvest.isModeratorOverride());
    }

    @Test
    public void testWeb_Update_RetainModeratorOverride_AsAuthorOrActor() {
        doWebUpdate_RetainModeratorOverride(HarvestMutationRole.AUTHOR_OR_ACTOR);
    }

    @Test
    public void testWeb_Update_RetainModeratorOverride_AsContactPerson() {
        doWebUpdate_RetainModeratorOverride(HarvestMutationRole.PERMIT_CONTACT_PERSON);
    }

    @Test
    public void testWeb_Update_RetainModeratorOverride_AsOther() {
        doWebUpdate_RetainModeratorOverride(HarvestMutationRole.OTHER);
    }

    private void doWebUpdate_RetainModeratorOverride(final HarvestMutationRole mutationRole) {
        final Harvest harvest = new Harvest();
        harvest.setModeratorOverride(true);

        final HarvestDTO dto = createDto(now, 20);

        new HarvestCommonMutation(dto, new GameSpecies(), mutationRole).accept(harvest);

        assertTrue(harvest.isModeratorOverride());
    }

}

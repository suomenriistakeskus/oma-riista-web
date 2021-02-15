package fi.riista.feature.gamediary.harvest.mutation.basic;

import fi.riista.feature.gamediary.DeerHuntingType;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.mutation.exception.HarvestPermitNotApplicableForDeerHuntingException;
import fi.riista.feature.gamediary.mobile.MobileHarvestDTO;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import javax.annotation.Nonnull;

import static fi.riista.feature.gamediary.DeerHuntingType.DOG_HUNTING;
import static fi.riista.feature.gamediary.DeerHuntingType.OTHER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

@RunWith(Theories.class)
public class HarvestDeerHuntingMutationTest {

    @Nonnull
    private static Harvest createExistingHarvest(final DeerHuntingType deerHuntingType, final String description) {
        final Harvest harvest = new Harvest();
        harvest.setId(1L);
        harvest.setDeerHuntingType(deerHuntingType);
        harvest.setDeerHuntingOtherTypeDescription(description);
        return harvest;
    }

    @Nonnull
    private static HarvestDeerHuntingMutation forWeb(final DeerHuntingType deerHuntingType, final String description) {
        final HarvestDTO dto = new HarvestDTO();
        dto.setDeerHuntingType(deerHuntingType);
        dto.setDeerHuntingOtherTypeDescription(description);
        dto.setGameSpeciesCode(GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER);
        return new HarvestDeerHuntingMutation(dto);
    }

    @Nonnull
    private static HarvestDeerHuntingMutation forMobile(final HarvestSpecVersion specVersion,
                                                        final DeerHuntingType deerHuntingType,
                                                        final String description) {
        final MobileHarvestDTO dto = new MobileHarvestDTO();
        dto.setHarvestSpecVersion(specVersion);
        dto.setDeerHuntingType(deerHuntingType);
        dto.setDeerHuntingOtherTypeDescription(description);
        dto.setGameSpeciesCode(GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER);
        return new HarvestDeerHuntingMutation(dto);
    }

    @Test
    public void testWeb() {
        final Harvest harvest = createExistingHarvest(null, null);

        final HarvestDeerHuntingMutation mutation = forWeb(DOG_HUNTING, null);

        mutation.accept(harvest);
        assertEquals(DOG_HUNTING, harvest.getDeerHuntingType());
        assertNull(harvest.getDeerHuntingOtherTypeDescription());
    }

    @Theory
    public void testMobileWhenSupported(final HarvestSpecVersion specVersion) {
        assumeTrue(specVersion.supportsDeerHuntingType());

        final Harvest harvest = createExistingHarvest(null, null);

        final HarvestDeerHuntingMutation mutation = forMobile(specVersion, DOG_HUNTING, null);

        mutation.accept(harvest);
        assertEquals(DOG_HUNTING, harvest.getDeerHuntingType());
        assertNull(harvest.getDeerHuntingOtherTypeDescription());
    }

    @Theory
    public void testMobileWhenNotSupported(final HarvestSpecVersion specVersion) {
        assumeFalse(specVersion.supportsDeerHuntingType());

        final Harvest harvest = createExistingHarvest(OTHER, "description");

        final HarvestDeerHuntingMutation mutation = forMobile(specVersion, DOG_HUNTING, null);

        mutation.accept(harvest);
        assertEquals(OTHER, harvest.getDeerHuntingType());
        assertEquals("description", harvest.getDeerHuntingOtherTypeDescription());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForNonWhiteTailedDeer() {
        final HarvestDTO dto = new HarvestDTO();
        dto.setDeerHuntingType(DOG_HUNTING);
        dto.setGameSpeciesCode(GameSpecies.OFFICIAL_CODE_MOOSE);
        new HarvestDeerHuntingMutation(dto);
        fail("should have thrown an exception");
    }

    @Test(expected = HarvestPermitNotApplicableForDeerHuntingException.class)
    public void testSetHuntingTypeWhenPermitPresent() {
        final HarvestDTO dto = new HarvestDTO();
        dto.setDeerHuntingType(DOG_HUNTING);
        dto.setGameSpeciesCode(GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER);
        dto.setPermitNumber("2019-1-200-00037-5");
        new HarvestDeerHuntingMutation(dto);
        fail("should have thrown an exception");
    }
}

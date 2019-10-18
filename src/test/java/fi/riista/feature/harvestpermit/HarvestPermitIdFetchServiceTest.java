package fi.riista.feature.harvestpermit;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;
import static fi.riista.util.DateUtil.huntingYear;
import static java.util.Collections.emptySet;
import static org.junit.Assert.assertEquals;

public class HarvestPermitIdFetchServiceTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestPermitIdFetchService service;

    @Test
    public void testFindMooselikePermitIdsGroupedByRkaId() {
        final RiistakeskuksenAlue rka1 = model().newRiistakeskuksenAlue();
        final RiistakeskuksenAlue rka2 = model().newRiistakeskuksenAlue();
        final RiistakeskuksenAlue rka3 = model().newRiistakeskuksenAlue();
        final RiistakeskuksenAlue rka4 = model().newRiistakeskuksenAlue();

        final GameSpecies moose = model().newGameSpeciesMoose();
        final GameSpecies whiteTailedDeer = model().newGameSpecies(OFFICIAL_CODE_WHITE_TAILED_DEER);

        final int huntingYear = huntingYear();

        final HarvestPermit rka1Permit1 =
                model().newMooselikePermit(model().newRiistanhoitoyhdistys(rka1), huntingYear);
        final HarvestPermit rka1Permit2 =
                model().newMooselikePermit(model().newRiistanhoitoyhdistys(rka1), huntingYear);
        final HarvestPermit rka2Permit =
                model().newMooselikePermit(model().newRiistanhoitoyhdistys(rka2), huntingYear);
        final HarvestPermit rka3Permit =
                model().newMooselikePermit(model().newRiistanhoitoyhdistys(rka3), huntingYear);
        final HarvestPermit rka4Permit =
                model().newMooselikePermit(model().newRiistanhoitoyhdistys(rka4), huntingYear - 1);

        // These should affect the result.
        model().newHarvestPermitSpeciesAmount(rka1Permit1, moose);
        model().newHarvestPermitSpeciesAmount(rka1Permit2, moose);
        model().newHarvestPermitSpeciesAmount(rka2Permit, moose);

        // Should not affect the result because species is different than what is requested.
        model().newHarvestPermitSpeciesAmount(rka3Permit, whiteTailedDeer);

        // Should not affect the result because hunting year is different than what is requested.
        model().newHarvestPermitSpeciesAmount(rka4Permit, moose);

        persistInNewTransaction();

        final Map<Long, Set<Long>> result =
                service.findMooselikePermitIdsGroupedByRkaId(huntingYear, moose.getOfficialCode());

        final Map<Long, Set<Long>> expected = ImmutableMap.of(
                rka1.getId(), F.getUniqueIds(rka1Permit1, rka1Permit2),
                rka2.getId(), F.getUniqueIds(rka2Permit));

        assertEquals(expected, result);
    }

    @Test
    public void testGetMooselikePermitIdsGroupedByRka() {
        final RiistakeskuksenAlue rka1 = model().newRiistakeskuksenAlue();
        final RiistakeskuksenAlue rka2 = model().newRiistakeskuksenAlue();
        final RiistakeskuksenAlue rka3 = model().newRiistakeskuksenAlue();
        final RiistakeskuksenAlue rka4 = model().newRiistakeskuksenAlue();
        final RiistakeskuksenAlue rka5 = model().newRiistakeskuksenAlue();

        final GameSpecies moose = model().newGameSpeciesMoose();
        final GameSpecies whiteTailedDeer = model().newGameSpecies(OFFICIAL_CODE_WHITE_TAILED_DEER);

        final int huntingYear = huntingYear();

        final HarvestPermit rka1Permit1 =
                model().newMooselikePermit(model().newRiistanhoitoyhdistys(rka1), huntingYear);
        final HarvestPermit rka1Permit2 =
                model().newMooselikePermit(model().newRiistanhoitoyhdistys(rka1), huntingYear);
        final HarvestPermit rka2Permit = model().newMooselikePermit(model().newRiistanhoitoyhdistys(rka2), huntingYear);
        final HarvestPermit rka3Permit = model().newMooselikePermit(model().newRiistanhoitoyhdistys(rka3), huntingYear);
        final HarvestPermit rka4Permit =
                model().newMooselikePermit(model().newRiistanhoitoyhdistys(rka4), huntingYear - 1);

        // These should affect the result.
        model().newHarvestPermitSpeciesAmount(rka1Permit1, moose);
        model().newHarvestPermitSpeciesAmount(rka1Permit2, moose);
        model().newHarvestPermitSpeciesAmount(rka2Permit, moose);

        // Should not affect the result because species is different than what is requested.
        model().newHarvestPermitSpeciesAmount(rka3Permit, whiteTailedDeer);

        // Should not affect the result because hunting year is different than what is requested.
        model().newHarvestPermitSpeciesAmount(rka4Permit, moose);

        persistInNewTransaction();

        final Map<RiistakeskuksenAlue, Set<Long>> result =
                service.getMooselikePermitIdsGroupedByRka(huntingYear, moose.getOfficialCode());

        // All known RiistakeskuksenAlue instances are expected to be found in the result.
        final Map<RiistakeskuksenAlue, Set<Long>> expected = ImmutableMap.of(
                rka1, F.getUniqueIds(rka1Permit1, rka1Permit2),
                rka2, F.getUniqueIds(rka2Permit),
                rka3, emptySet(),
                rka4, emptySet(),
                rka5, emptySet());

        assertEquals(expected, result);
    }
}

package fi.riista.feature.organization.rhy;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.permit.MooselikeHuntingYearDTO;
import fi.riista.feature.huntingclub.permit.MooselikePermitListingDTO;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.util.DateUtil;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;

public class RiistanhoitoyhdistysCrudFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private RiistanhoitoyhdistysCrudFeature feature;

    private RiistakeskuksenAlue rka;

    @Before
    public void initRka() {
        this.rka = model().newRiistakeskuksenAlue();
    }

    @Test
    public void testMooselikePermits() {
        runTest((species, rhy) -> newHarvestPermit(rhy, species));
    }

    @Test
    public void testMooselikePermits_relatedRhy() {
        runTest((species, rhy) -> {
            final HarvestPermit p = newHarvestPermit(model().newRiistanhoitoyhdistys(this.rka), species);
            p.setRelatedRhys(Collections.singleton(rhy));
            return p;
        });
    }

    private HarvestPermit newHarvestPermit(Riistanhoitoyhdistys rhy, GameSpecies species) {
        final HarvestPermit permit = model().newHarvestPermit(rhy);
        permit.setPermitTypeCode(HarvestPermit.MOOSELIKE_PERMIT_TYPE);
        permit.setPermitHolder(model().newHuntingClub(rhy));

        model().newHarvestPermitSpeciesAmount(permit, species).setCreditorReference(creditorReference());

        return permit;
    }

    private void runTest(BiFunction<GameSpecies, Riistanhoitoyhdistys, HarvestPermit> permitCreator) {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys(this.rka);
        final Riistanhoitoyhdistys otherRhy = model().newRiistanhoitoyhdistys(this.rka);

        final GameSpecies moose = model().newGameSpeciesMoose();
        model().newMooselikePrice(
                DateUtil.getFirstCalendarYearOfCurrentHuntingYear(),
                moose,
                BigDecimal.valueOf(120),
                BigDecimal.valueOf(50)
        );

        final HarvestPermit permit = permitCreator.apply(moose, rhy);
        permitCreator.apply(moose, otherRhy);

        onSavedAndAuthenticated(createNewAdmin(), tx(() -> {
            final List<MooselikeHuntingYearDTO> years = feature.listMooselikeHuntingYears(rhy.getId());
            assertEquals(1, years.size());

            final Integer year = DateUtil.getFirstCalendarYearOfCurrentHuntingYear();
            final List<MooselikePermitListingDTO> permits = feature.listPermits(rhy.getId(), year, GameSpecies.OFFICIAL_CODE_MOOSE, null);
            assertEquals(1, permits.size());
            assertEquals((long) permit.getId(), permits.get(0).getId());
        }));
    }
}

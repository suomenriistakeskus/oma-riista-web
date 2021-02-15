package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.GameSpecies;
import org.junit.experimental.theories.Theories;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class HarvestSpecimenOps_CopyFallowDeerFields_MostRecentSpecTest
        extends HarvestSpecimenOps_CopyOtherDeerFields_MostRecentSpecTestBase {

    @Override
    protected int getSpeciesCode() {
        return GameSpecies.OFFICIAL_CODE_FALLOW_DEER;
    }
}

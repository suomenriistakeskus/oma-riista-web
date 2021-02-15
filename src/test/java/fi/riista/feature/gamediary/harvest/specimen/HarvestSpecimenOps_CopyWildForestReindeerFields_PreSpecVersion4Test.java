package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.GameSpecies;
import org.junit.experimental.theories.Theories;
import org.junit.runner.RunWith;

// Tests covering specVersions before 4.
// This class can be removed as a whole when min specVersion is upgraded to 4.
@RunWith(Theories.class)
public class HarvestSpecimenOps_CopyWildForestReindeerFields_PreSpecVersion4Test
        extends HarvestSpecimenOps_CopyPermitBasedDeerFields_PreSpecVersion4TestBase {

    @Override
    protected int getSpeciesCode() {
        return GameSpecies.OFFICIAL_CODE_WILD_FOREST_REINDEER;
    }
}

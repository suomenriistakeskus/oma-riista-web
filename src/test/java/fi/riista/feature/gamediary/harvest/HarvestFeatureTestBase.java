package fi.riista.feature.gamediary.harvest;

import fi.riista.feature.gamediary.GameDiaryEntryFeatureTest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenRepository;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen_;
import org.springframework.data.jpa.domain.JpaSort;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.test.Asserts.assertThat;
import static fi.riista.test.TestUtils.createList;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;

public class HarvestFeatureTestBase extends GameDiaryEntryFeatureTest {

    @Resource
    protected HarvestFeature feature;

    @Resource
    protected HarvestSpecimenRepository harvestSpecimenRepo;

    protected HarvestDTO invokeCreateHarvest(final HarvestDTO input) {
        return withVersionChecked(feature.createHarvest(input));
    }

    protected HarvestDTO invokeUpdateHarvest(final HarvestDTO input) {
        return withVersionChecked(feature.updateHarvest(input));
    }

    protected HarvestDTO withVersionChecked(final HarvestDTO dto) {
        return checkDtoVersionAgainstEntity(dto, Harvest.class);
    }

    protected List<HarvestSpecimen> findSpecimens(final Harvest harvest) {
        return harvestSpecimenRepo.findByHarvest(harvest, JpaSort.of(HarvestSpecimen_.id));
    }

    protected List<HarvestSpecimen> createSpecimens(final Harvest harvest, final int numSpecimens) {
        return createList(numSpecimens, () -> model().newHarvestSpecimen(harvest));
    }

    protected void assertCreateThrows(final Class<? extends Throwable> exceptionClass, final HarvestDTO dto) {
        assertThrows(exceptionClass, () -> invokeCreateHarvest(dto));

        assertThat(harvestRepo.findAll(), is(empty()),
                "No harvest should have been created as side-effect");
    }

    protected void assertUpdateThrows(final Class<? extends Throwable> exceptionClass, final HarvestDTO dto) {
        assertThrows(exceptionClass, () -> invokeUpdateHarvest(dto));

        assertVersion(getHarvest(dto.getId()), 0);
    }
}

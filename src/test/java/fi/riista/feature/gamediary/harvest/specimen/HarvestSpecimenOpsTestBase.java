package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.fixture.HarvestSpecimenType;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.util.NumberGenerator;
import fi.riista.util.NumberSequence;
import fi.riista.util.ValueGeneratorMixin;
import org.junit.Before;
import org.mockito.Answers;
import org.mockito.Mockito;

import javax.annotation.Nonnull;

import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion.MOST_RECENT;
import static fi.riista.util.DateUtil.huntingYear;

public abstract class HarvestSpecimenOpsTestBase implements ValueGeneratorMixin {

    protected HarvestSpecimen specimen;

    @Override
    public NumberGenerator getNumberGenerator() {
        return NumberSequence.INSTANCE;
    }

    @Before
    public void setUp() {
        // Mocked in order to be able to instantiate without providing a reference to a Harvest instance.
        specimen = Mockito.mock(HarvestSpecimen.class, Answers.CALLS_REAL_METHODS);
    }

    // Populates entity according to most recent spec version and current hunting year.
    protected void populateEntity(final int speciesCode, @Nonnull final HarvestSpecimenType specimenType) {
        populateEntity(speciesCode, specimenType, huntingYear());
    }

    protected void populateEntity(final int speciesCode,
                                  @Nonnull final HarvestSpecimenType specimenType,
                                  final int huntingYear) {

        specimen.clearBusinessFields();

        createPopulator(speciesCode, MOST_RECENT, huntingYear).mutateContent(specimen, specimenType);
    }

    protected HarvestSpecimenDTO createDTO(final int speciesCode,
                                           @Nonnull final HarvestSpecimenType specimenType,
                                           @Nonnull final HarvestSpecVersion version,
                                           final int huntingYear) {

        final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();
        createPopulator(speciesCode, version, huntingYear).mutateContent(dto, specimenType);
        return dto;
    }

    protected HarvestSpecimenOps createOps(final int speciesCode,
                                           @Nonnull final HarvestSpecVersion version,
                                           final int huntingYear) {

        return new HarvestSpecimenOps(speciesCode, version, huntingYear);
    }

    protected HarvestSpecimenPopulator createPopulator(final int speciesCode,
                                                       @Nonnull final HarvestSpecVersion version,
                                                       final int huntingYear) {

        return new HarvestSpecimenPopulator(speciesCode, version, huntingYear, getNumberGenerator());
    }
}

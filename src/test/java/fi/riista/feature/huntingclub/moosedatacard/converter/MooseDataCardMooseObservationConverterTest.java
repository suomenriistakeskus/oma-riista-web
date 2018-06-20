package fi.riista.feature.huntingclub.moosedatacard.converter;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory;
import fi.riista.feature.organization.person.Person;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardObservation;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MooseDataCardMooseObservationConverterTest
        extends MooseDataCardObservationConverterTest<MooseDataCardObservation> {

    @Override
    protected Stream<Observation> convert(
            final MooseDataCardObservation source, final Person contactPerson, final GeoLocation defaultCoordinates) {

        final GameSpecies moose = mooseSpeciesAmount.getGameSpecies();
        final int huntingYear = mooseSpeciesAmount.resolveHuntingYear();

        return new MooseDataCardMooseObservationConverter(moose, contactPerson, huntingYear, defaultCoordinates)
                .apply(source);
    }

    @Override
    protected MooseDataCardObservation newObservationSource(@Nullable final LocalDate date) {
        return MooseDataCardObjectFactory.newMooseObservation(date);
    }

    @Test
    public void testValidMooseSpecificFields() {
        final MooseDataCardObservation source = newObservationSourceWithinSeason();

        assertConversion(source, output -> {
            assertEquals(source.getAU(), output.getMooselikeMaleAmount());
            assertEquals(source.getN0(), output.getMooselikeFemaleAmount());
            assertEquals(source.getN1(), output.getMooselikeFemale1CalfAmount());
            assertEquals(source.getN2(), output.getMooselikeFemale2CalfsAmount());
            assertEquals(source.getN3(), output.getMooselikeFemale3CalfsAmount());
            assertEquals(source.getY(), output.getMooselikeCalfAmount());
            assertEquals(source.getT(), output.getMooselikeUnknownSpecimenAmount());
        });
    }

    @Test
    public void testAllButOneAmountFieldContainingIllegalValue() {
        final MooseDataCardObservation source =
                newObservationSourceWithinSeason().withAU(-1).withN0(-1).withN1(-1).withN2(-1).withN3(-1).withY(-1);

        final Integer zeroIntObj = Integer.valueOf(0);

        assertConversion(source, output -> {
            assertEquals(zeroIntObj, output.getMooselikeMaleAmount());
            assertEquals(zeroIntObj, output.getMooselikeFemaleAmount());
            assertEquals(zeroIntObj, output.getMooselikeFemale1CalfAmount());
            assertEquals(zeroIntObj, output.getMooselikeFemale2CalfsAmount());
            assertEquals(zeroIntObj, output.getMooselikeFemale3CalfsAmount());
            assertEquals(zeroIntObj, output.getMooselikeCalfAmount());
            assertEquals(source.getT(), output.getMooselikeUnknownSpecimenAmount());
        });
    }

    @Test
    public void testAllAmountFieldsContainingIllegalValue() {
        assertEmptyStreamResult(newObservationSourceWithinSeason()
                .withAU(-1).withN0(-1).withN1(-1).withN2(-1).withN3(-1).withY(-1).withT(-1));
    }

    private void assertConversion(final MooseDataCardObservation source, final Consumer<Observation> extraAssertions) {
        final List<Observation> list = convert(source).collect(toList());
        assertEquals(1, list.size());

        final Observation output = list.get(0);
        assertEquals(mooseSpeciesAmount.getGameSpecies(), output.getSpecies());
        assertEquals(ObservationType.NAKO, output.getObservationType());
        assertNull(output.getMooselikeFemale4CalfsAmount());
        extraAssertions.accept(output);
    }
}

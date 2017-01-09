package fi.riista.feature.huntingclub.moosedatacard.converter;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardObservation;

import org.junit.Test;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class MooseDataCardMooseObservationConverterTest
        extends MooseDataCardObservationConverterTestBase<MooseDataCardObservation> {

    private final GameSpecies moose = new GameSpecies();

    @Override
    protected MooseDataCardObservation newSourceObject() {
        return MooseDataCardObjectFactory.newMooseObservation();
    }

    @Override
    protected Stream<Observation> convert(
            final MooseDataCardObservation source, final Person contactPerson, final GeoLocation defaultCoordinates) {

        return new MooseDataCardMooseObservationConverter(moose, contactPerson, defaultCoordinates).apply(source);
    }

    @Test
    public void testValidMooseSpecificFields() {
        final MooseDataCardObservation source = newSourceObject();

        assertConversion(source, output -> {
            assertEquals(source.getAU(), output.getMooselikeMaleAmount());
            assertEquals(source.getN0(), output.getMooselikeFemaleAmount());
            assertEquals(source.getN1(), output.getMooselikeFemale1CalfAmount());
            assertEquals(source.getN2(), output.getMooselikeFemale2CalfsAmount());
            assertEquals(source.getN3(), output.getMooselikeFemale3CalfsAmount());
            assertEquals(source.getT(), output.getMooselikeUnknownSpecimenAmount());
        });
    }

    @Test
    public void testAllButOneAmountFieldContainingIllegalValue() {
        final MooseDataCardObservation source =
                newSourceObject().withAU(-1).withN0(-1).withN1(-1).withN2(-1).withN3(-1);

        assertConversion(source, output -> {
            assertEquals(Integer.valueOf(0), output.getMooselikeMaleAmount());
            assertEquals(Integer.valueOf(0), output.getMooselikeFemaleAmount());
            assertEquals(Integer.valueOf(0), output.getMooselikeFemale1CalfAmount());
            assertEquals(Integer.valueOf(0), output.getMooselikeFemale2CalfsAmount());
            assertEquals(Integer.valueOf(0), output.getMooselikeFemale3CalfsAmount());
            assertEquals(source.getT(), output.getMooselikeUnknownSpecimenAmount());
        });
    }

    @Test
    public void testAllAmountFieldsContainingIllegalValue() {
        assertEmptyStreamResult(newSourceObject().withAU(-1).withN0(-1).withN1(-1).withN2(-1).withN3(-1).withT(-1));
    }

    private void assertConversion(
            final MooseDataCardObservation source, final Consumer<Observation> extraAssertions) {

        final List<Observation> list = convert(source, new Person(), geoLocation()).collect(toList());
        assertEquals(1, list.size());

        final Observation output = list.get(0);
        assertEquals(moose, output.getSpecies());
        assertEquals(ObservationType.NAKO, output.getObservationType());
        assertNull(output.getMooselikeFemale4CalfsAmount());
        extraAssertions.accept(output);
    }

}

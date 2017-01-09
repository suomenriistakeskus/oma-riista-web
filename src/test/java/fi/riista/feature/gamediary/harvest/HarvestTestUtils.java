package fi.riista.feature.gamediary.harvest;

import fi.riista.feature.gamediary.harvest.specimen.HasMooseFields;
import fi.riista.feature.gamediary.harvest.specimen.HasMooselikeFields;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HarvestTestUtils {

    public enum MooselikeFieldsPresence {

        NONE, ESTIMATED_WEIGHT, ALL
    }

    // Estimated weight which MUST be the first element in the stream.
    private static final Function<HasMooselikeFields, Stream<Object>> EXTRACT_MOOSELIKE_FIELDS =
            obj -> Stream.of(
                    obj.getWeightEstimated(), obj.getWeightMeasured(), obj.getAntlersWidth(),
                    obj.getAntlerPointsLeft(), obj.getAntlerPointsRight(), obj.getNotEdible(), obj.getAdditionalInfo());

    private static final Function<HasMooseFields, Stream<Object>> EXTRACT_MOOSE_ONLY_FIELDS =
            obj -> Stream.of(obj.getFitnessClass(), obj.getAntlersType());

    private static final Function<HasMooseFields, Stream<Object>> EXTRACT_MOOSE_FIELDS =
            obj -> concat(EXTRACT_MOOSELIKE_FIELDS.apply(obj), EXTRACT_MOOSE_ONLY_FIELDS.apply(obj));

    public static <T extends HasMooselikeFields> void assertMooselikeFieldsPresent(final Collection<T> specimens) {
        assertPresenceOfValues(specimens, EXTRACT_MOOSELIKE_FIELDS, MooselikeFieldsPresence.ALL);
    }

    public static <T extends HasMooselikeFields> void assertMooselikeFieldsNotPresent(final Collection<T> specimens) {
        assertPresenceOfValues(specimens, EXTRACT_MOOSELIKE_FIELDS, MooselikeFieldsPresence.NONE);
    }

    public static <T extends HasMooselikeFields> void assertPresenceOfMooselikeFields(
            final Collection<T> specimens, final MooselikeFieldsPresence presence) {

        assertPresenceOfValues(specimens, EXTRACT_MOOSELIKE_FIELDS, presence);
    }

    public static <T extends HasMooseFields> void assertMooseFieldsPresent(final Collection<T> specimens) {
        assertPresenceOfValues(specimens, EXTRACT_MOOSE_FIELDS, MooselikeFieldsPresence.ALL);
    }

    public static <T extends HasMooseFields> void assertMooseFieldsNotPresent(final Collection<T> specimens) {
        assertPresenceOfValues(specimens, EXTRACT_MOOSE_FIELDS, MooselikeFieldsPresence.NONE);
    }

    public static <T extends HasMooseFields> void assertMooseOnlyFieldsNotPresent(final Collection<T> specimens) {
        assertPresenceOfValues(specimens, EXTRACT_MOOSE_ONLY_FIELDS, MooselikeFieldsPresence.NONE);
    }

    public static <T extends HasMooseFields> void assertPresenceOfMooseFields(
            final Collection<T> specimens, final MooselikeFieldsPresence presence) {

        assertPresenceOfValues(specimens, EXTRACT_MOOSE_FIELDS, presence);
    }

    private static <T extends HasMooselikeFields> void assertPresenceOfValues(
            final Collection<T> specimens,
            final Function<? super T, Stream<Object>> extractFieldsFn,
            final MooselikeFieldsPresence presence) {

        specimens.forEach(specimen -> {

            final Stream<Object> fieldValues = extractFieldsFn.apply(specimen);

            if (presence == MooselikeFieldsPresence.ESTIMATED_WEIGHT) {
                // Skip estimated weight which MUST be the first element in the stream.
                assertTrue(fieldValues.skip(1).allMatch(Objects::isNull));
                assertNotNull(specimen.getWeightEstimated());
            } else {
                assertTrue(fieldValues.allMatch(
                        presence == MooselikeFieldsPresence.ALL ? Objects::nonNull : Objects::isNull));
            }
        });
    }

}

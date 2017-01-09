package fi.riista.feature.gamediary.observation;

import static fi.riista.util.Asserts.assertEmpty;
import static fi.riista.util.EqualityHelper.equalIdAndContent;
import static fi.riista.util.TestUtils.createList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationDTO;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenDTO;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.observation.ObservationDTOTransformer;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.Functions;

import javaslang.Tuple;
import javaslang.Tuple2;

import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ObservationDTOTransformerTest extends EmbeddedDatabaseTest {

    @Resource
    private ObservationDTOTransformer transformer;

    @Test(expected = RuntimeException.class)
    public void testUserNotAuthenticated() {
        final List<Observation> observations = createList(5, model()::newObservation);
        persistInNewTransaction();
        transformer.apply(observations);
    }

    @Test
    public void testWithoutCreatingPluralAssociations() {
        withPerson(person -> {

            final List<Observation> observations = createList(5, () -> {
                final Observation obs = model().newObservation(person);
                obs.setWithinMooseHunting(true);
                return obs.withMooselikeAmounts(1, 2, 3, 4, 5, 6, 7);
            });

            // Generate extra observation that is not included in input and thus should not affect output either.
            model().newObservation(person);

            onSavedAndAuthenticated(createUser(person), () -> {

                final List<ObservationDTO> dtos = transformer.apply(observations);

                assertNotNull(dtos);
                assertEquals(observations.size(), dtos.size());

                for (int i = 0; i < observations.size(); i++) {
                    final ObservationDTO dto = dtos.get(i);

                    assertNotNull(dto);
                    assertFieldsExcludePluralAssociations(observations.get(i), dto);

                    assertEmpty(dto.getImageIds());
                    assertEmpty(dto.getSpecimens());

                    assertTrue(dto.isCanEdit());
                }
            });
        });
    }

    @Test
    public void testWithSpecimens() {
        withPerson(person -> {

            final List<Tuple2<Observation, List<ObservationSpecimen>>> pairs =
                    createList(5, () -> newObservationWithSpecimens(10, person));

            // Generate extra observation that is not included in input and thus should not affect output either.
            newObservationWithSpecimens(5, person);

            onSavedAndAuthenticated(createUser(person), () -> {

                final List<ObservationDTO> dtos = transformer.apply(F.nonNullKeys(pairs));

                assertNotNull(dtos);
                assertEquals(pairs.size(), dtos.size());

                for (int i = 0; i < pairs.size(); i++) {
                    final Observation observation = pairs.get(i)._1;
                    final ObservationDTO dto = dtos.get(i);

                    assertNotNull(dto);
                    assertFieldsExcludePluralAssociations(observation, dto);

                    assertTrue(equalIdAndContent(
                            pairs.get(i)._2, dto.getSpecimens(), ObservationSpecimenDTO.EQUAL_TO_ENTITY));

                    assertEmpty(dto.getImageIds());
                    assertTrue(dto.isCanEdit());
                }
            });
        });
    }

    @Test
    public void testWithImages() {
        withPerson(person -> {

            final List<Tuple2<Observation, List<GameDiaryImage>>> pairs =
                    createList(5, () -> newObservationWithImages(5, person));

            // Generate extra observation that is not included in input and thus should not affect output either.
            newObservationWithImages(5, person);

            onSavedAndAuthenticated(createUser(person), () -> {

                final List<ObservationDTO> dtos = transformer.apply(F.nonNullKeys(pairs));

                assertNotNull(dtos);
                assertEquals(pairs.size(), dtos.size());

                for (int i = 0; i < pairs.size(); i++) {
                    final ObservationDTO dto = dtos.get(i);

                    assertNotNull(dto);
                    assertFieldsExcludePluralAssociations(pairs.get(i)._1(), dto);

                    verifyImageIds(pairs.get(i)._2(), dto);

                    assertTrue(dto.isCanEdit());
                }
            });
        });
    }

    private static void assertFieldsExcludePluralAssociations(
            final Observation observation, final ObservationDTO dto) {

        // To verify integrity of test fixture.
        assertNotNull(observation.getId());
        assertNotNull(observation.getConsistencyVersion());

        assertEquals(observation.getId(), dto.getId());
        assertEquals(observation.getConsistencyVersion(), dto.getRev());
        Assert.assertEquals(GameDiaryEntryType.OBSERVATION, dto.getType());
        assertEquals(observation.getSpecies().getOfficialCode(), dto.getGameSpeciesCode());
        assertEquals(observation.getGeoLocation(), dto.getGeoLocation());
        assertEquals(DateUtil.toLocalDateTimeNullSafe(observation.getPointOfTime()), dto.getPointOfTime());
        assertEquals(observation.getWithinMooseHunting(), dto.getWithinMooseHunting());
        assertEquals(observation.getDescription(), dto.getDescription());

        assertTrue(observation.isAmountEqualTo(dto.getAmount()));
        assertEquals(observation.getMooselikeMaleAmount(), dto.getMooselikeMaleAmount());
        assertEquals(observation.getMooselikeFemaleAmount(), dto.getMooselikeFemaleAmount());
        assertEquals(observation.getMooselikeFemale1CalfAmount(), dto.getMooselikeFemale1CalfAmount());
        assertEquals(observation.getMooselikeFemale2CalfsAmount(), dto.getMooselikeFemale2CalfsAmount());
        assertEquals(observation.getMooselikeFemale3CalfsAmount(), dto.getMooselikeFemale3CalfsAmount());
        assertEquals(observation.getMooselikeFemale4CalfsAmount(), dto.getMooselikeFemale4CalfsAmount());
        assertEquals(observation.getMooselikeUnknownSpecimenAmount(), dto.getMooselikeUnknownSpecimenAmount());
    }

    private static void verifyImageIds(final Iterable<GameDiaryImage> images, final ObservationDTO dto) {
        assertEquals(getUniqueImageUuids(images), new HashSet<>(dto.getImageIds()));
    }

    private static Set<UUID> getUniqueImageUuids(final Iterable<GameDiaryImage> images) {
        return F.mapNonNullsToSet(images, Functions.idOf(GameDiaryImage::getFileMetadata));
    }

    private Tuple2<Observation, List<GameDiaryImage>> newObservationWithImages(
            final int numImages, final Person author) {

        final Observation observation = model().newObservation(author);

        return Tuple.of(observation, createList(numImages, () -> model().newGameDiaryImage(observation)));
    }

    private Tuple2<Observation, List<ObservationSpecimen>> newObservationWithSpecimens(
            final int numSpecimens, final Person author) {

        final Observation observation = model().newObservation(author);

        // With one undefined specimen
        observation.setAmount(numSpecimens + 1);

        return Tuple.of(observation, createList(numSpecimens, () -> model().newObservationSpecimen(observation)));
    }

}

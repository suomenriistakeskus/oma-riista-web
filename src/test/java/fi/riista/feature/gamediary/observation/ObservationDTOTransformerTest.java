package fi.riista.feature.gamediary.observation;

import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.observation.specimen.GameMarking;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.gamediary.observation.specimen.ObservedGameAge;
import fi.riista.feature.gamediary.observation.specimen.ObservedGameState;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.feature.gamediary.image.GameDiaryImage.getUniqueImageIds;
import static fi.riista.test.Asserts.assertEmpty;
import static fi.riista.test.TestUtils.createList;
import static fi.riista.util.EqualityHelper.equalIdAndContent;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
    public void testWithoutCollectionAssociations() {
        withPerson(person -> {

            final List<Observation> observations = createList(5, () -> {
                final Observation obs = model().newObservation(person);
                obs.setWithinMooseHunting(true);
                obs.setMooselikeMaleAmount(1);
                obs.setMooselikeFemaleAmount(2);
                obs.setMooselikeCalfAmount(3);
                obs.setMooselikeFemale1CalfAmount(4);
                obs.setMooselikeFemale2CalfsAmount(5);
                obs.setMooselikeFemale3CalfsAmount(6);
                obs.setMooselikeFemale4CalfsAmount(7);
                obs.setMooselikeUnknownSpecimenAmount(8);
                obs.setAmountToSumOfMooselikeAmounts();
                return obs;
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
    public void testSpecimens() {
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

                    assertTrue(equalIdAndContent(pairs.get(i)._2, dto.getSpecimens(), dto.specimenOps()::equalContent));

                    assertEmpty(dto.getImageIds());
                    assertTrue(dto.isCanEdit());
                }
            });
        });
    }

    @Test
    public void testSpecimens_isNullWhenAmountIsNull() {
        withPerson(person -> {
            final Observation observation = model().newObservation(person);
            observation.setAmount(null);

            onSavedAndAuthenticated(createUser(person), () -> {
                final ObservationDTO dto = transformer.apply(observation);
                assertNull(dto.getAmount());
                assertNull(dto.getSpecimens());
            });
        });
    }

    @Test
    public void testSpecimens_isEmptyWhenAmountIsNotNull() {
        withPerson(person -> {
            final Observation observation = model().newObservation(person);
            observation.setAmount(1);

            onSavedAndAuthenticated(createUser(person), () -> {
                final ObservationDTO dto = transformer.apply(observation);
                assertNotNull(dto.getAmount());
                assertEmpty(dto.getSpecimens());
            });
        });
    }

    @Test
    public void testImages() {
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
                    assertFieldsExcludePluralAssociations(pairs.get(i)._1, dto);

                    assertThat(dto.getImageIds(), containsInAnyOrder(getUniqueImageIds(pairs.get(i)._2).toArray()));

                    assertTrue(dto.isCanEdit());
                }
            });
        });
    }

    @Test
    public void testLargeCarnivoreFields_presentWhenAuthor() {
        withPerson(person -> {
            final Observation observation = model().newObservation(person);
            observation.setInYardDistanceToResidence(99);
            observation.setVerifiedByCarnivoreAuthority(true);
            observation.setObserverName("abc");
            observation.setObserverPhoneNumber(phoneNumber());
            observation.setOfficialAdditionalInfo("xyz");

            onSavedAndAuthenticated(createUser(person), () -> {
                final ObservationDTO dto = transformer.apply(observation);
                assertNotNull(dto.getVerifiedByCarnivoreAuthority());
                assertNotNull(dto.getObserverName());
                assertNotNull(dto.getObserverPhoneNumber());
                assertNotNull(dto.getOfficialAdditionalInfo());

                // Not yet supported.
                assertNull(dto.getInYardDistanceToResidence());
            });
        });
    }

    @Test
    public void testLargeCarnivoreFields_absentWhenNotAuthorNorObserver() {
        withPerson(observer -> {
            final Observation observation = model().newObservation(observer);
            observation.setInYardDistanceToResidence(99);
            observation.setVerifiedByCarnivoreAuthority(true);
            observation.setObserverName("abc");
            observation.setObserverPhoneNumber(phoneNumber());
            observation.setOfficialAdditionalInfo("xyz");

            onSavedAndAuthenticated(createUser(model().newPerson()), () -> {
                final ObservationDTO dto = transformer.apply(observation);
                assertNull(dto.getInYardDistanceToResidence());
                assertNull(dto.getVerifiedByCarnivoreAuthority());
                assertNull(dto.getObserverName());
                assertNull(dto.getObserverPhoneNumber());
                assertNull(dto.getOfficialAdditionalInfo());
            });
        });
    }

    private static void assertFieldsExcludePluralAssociations(final Observation observation, final ObservationDTO dto) {
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

        assertEquals(observation.getAmount(), dto.getAmount());
        assertEquals(observation.getMooselikeMaleAmount(), dto.getMooselikeMaleAmount());
        assertEquals(observation.getMooselikeFemaleAmount(), dto.getMooselikeFemaleAmount());
        assertEquals(observation.getMooselikeCalfAmount(), dto.getMooselikeCalfAmount());
        assertEquals(observation.getMooselikeFemale1CalfAmount(), dto.getMooselikeFemale1CalfAmount());
        assertEquals(observation.getMooselikeFemale2CalfsAmount(), dto.getMooselikeFemale2CalfsAmount());
        assertEquals(observation.getMooselikeFemale3CalfsAmount(), dto.getMooselikeFemale3CalfsAmount());
        assertEquals(observation.getMooselikeFemale4CalfsAmount(), dto.getMooselikeFemale4CalfsAmount());
        assertEquals(observation.getMooselikeUnknownSpecimenAmount(), dto.getMooselikeUnknownSpecimenAmount());

        assertEquals(observation.getVerifiedByCarnivoreAuthority(), dto.getVerifiedByCarnivoreAuthority());
        assertEquals(observation.getObserverName(), dto.getObserverName());
        assertEquals(observation.getObserverPhoneNumber(), dto.getObserverPhoneNumber());
        assertEquals(observation.getOfficialAdditionalInfo(), dto.getOfficialAdditionalInfo());
    }

    private Tuple2<Observation, List<GameDiaryImage>> newObservationWithImages(final int numImages,
                                                                               final Person author) {

        final Observation observation = model().newObservation(author);

        return Tuple.of(observation, createList(numImages, () -> model().newGameDiaryImage(observation)));
    }

    private Tuple2<Observation, List<ObservationSpecimen>> newObservationWithSpecimens(final int numSpecimens,
                                                                                       final Person author) {

        final Observation observation = model().newObservation(author);

        // With one undefined specimen
        observation.setAmount(numSpecimens + 1);

        return Tuple.of(observation, createList(numSpecimens, () -> {
            final ObservationSpecimen specimen = model().newObservationSpecimen(observation);
            specimen.setAge(some(ObservedGameAge.class));
            specimen.setGender(some(GameGender.class));
            specimen.setState(some(ObservedGameState.class));
            specimen.setMarking(some(GameMarking.class));
            return specimen;
        }));
    }
}

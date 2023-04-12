package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.fixture.ObservationFixtureMixin;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Stream;

import static fi.riista.feature.gamediary.DeerHuntingType.STAND_HUNTING;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAN_GOOSE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_CANADIAN_BEAVER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_EUROPEAN_BEAVER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_GREYLAG_GOOSE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;
import static fi.riista.feature.gamediary.observation.ObservationCategory.DEER_HUNTING;
import static fi.riista.feature.gamediary.observation.ObservationCategory.NORMAL;
import static fi.riista.feature.gamediary.observation.ObservationSpecVersion.LOWEST_VERSION_SUPPORTING_BIRD_LITTER_AND_COUPLE;
import static fi.riista.feature.gamediary.observation.ObservationSpecVersion.LOWEST_VERSION_SUPPORTING_CATEGORY;
import static fi.riista.feature.gamediary.observation.ObservationSpecVersion.LOWEST_VERSION_SUPPORTING_XTRA_BEAVER_TYPES;
import static fi.riista.feature.gamediary.observation.ObservationSpecVersion.MOST_RECENT;
import static fi.riista.feature.gamediary.observation.ObservationType.NAKO;
import static fi.riista.feature.gamediary.observation.ObservationType.PARI;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA_KEKO;
import static fi.riista.feature.gamediary.observation.ObservationType.POIKUE;
import static fi.riista.test.Asserts.assertEmpty;
import static fi.riista.test.TestUtils.createList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeTrue;

@RunWith(Theories.class)
public class MobileObservationDTOTransformerTest extends EmbeddedDatabaseTest implements ObservationFixtureMixin {

    @DataPoints("specVersions")
    public static final ObservationSpecVersion[] SPEC_VERSIONS = ObservationSpecVersion.values();

    @Resource
    private MobileObservationDTOTransformer transformer;

    @Test(expected = RuntimeException.class)
    public void testUserNotAuthenticated() {
        final List<Observation> observations = createList(5, model()::newObservation);
        persistInNewTransaction();
        transformer.apply(observations);
    }

    @Theory
    public void testSpecimens_isNullWhenAmountIsNull(final ObservationSpecVersion version) {
        withPerson(person -> {

            final GameSpecies species = model().newGameSpecies();
            model().newObservationBaseFields(species, version);

            final Observation observation = model().newObservation(species, person);
            observation.setAmount(null);

            onSavedAndAuthenticated(createUser(person), () -> {
                final MobileObservationDTO dto = transformer.apply(observation, version);
                assertNull(dto.getAmount());
                assertNull(dto.getSpecimens());
            });
        });
    }

    @Theory
    public void testSpecimens_isEmptyWhenAmountIsNotNull(final ObservationSpecVersion version) {
        withPerson(person -> {

            final GameSpecies species = model().newGameSpecies();
            model().newObservationBaseFields(species, version);

            final Observation observation = model().newObservation(species, person);
            observation.setAmount(1);

            onSavedAndAuthenticated(createUser(person), () -> {
                final MobileObservationDTO dto = transformer.apply(observation, version);
                assertNotNull(dto.getAmount());
                assertEmpty(dto.getSpecimens());
            });
        });
    }

    @Theory
    public void testTranslationOfObsoleteBeaverObservationType(final ObservationSpecVersion version) {
        assumeTrue(version.lessThan(LOWEST_VERSION_SUPPORTING_XTRA_BEAVER_TYPES));

        withPerson(author -> {

            Stream.of(OFFICIAL_CODE_CANADIAN_BEAVER, OFFICIAL_CODE_EUROPEAN_BEAVER, OFFICIAL_CODE_BEAR)
                    .forEach(speciesCode -> {

                        final GameSpecies species = model().newGameSpecies(speciesCode);

                        createObservationMetaF(species, MOST_RECENT, PESA_KEKO).forMobile().consumeBy(currentMeta -> {

                            createObservationMetaF(species, version, PESA).forMobile().consumeBy(oldMeta -> {

                                final List<Observation> observations =
                                        createList(5, () -> model().newMobileObservation(author, currentMeta));

                                onSavedAndAuthenticated(createUser(author), () -> {

                                    final List<MobileObservationDTO> dtos = transformer.apply(observations, version);

                                    for (int i = 0; i < observations.size(); i++) {
                                        final MobileObservationDTO dto = dtos.get(i);
                                        assertNotNull(dto);

                                        assertEquals(species.isBeaver() ? PESA : PESA_KEKO, dto.getObservationType());
                                        assertFieldsExcludingPluralAssociations(observations.get(i), dto, false);
                                    }
                                });
                            });
                        });
                    });
        });
    }

    @Theory
    public void testTranslationOfPariObservationType(final ObservationSpecVersion version) {
        assumeTrue(version.lessThan(LOWEST_VERSION_SUPPORTING_BIRD_LITTER_AND_COUPLE));

        withPerson(author -> {

            Stream.of(OFFICIAL_CODE_BEAN_GOOSE, OFFICIAL_CODE_GREYLAG_GOOSE).forEach(speciesCode -> {

                final GameSpecies species = model().newGameSpecies(speciesCode);

                createObservationMetaF(species, MOST_RECENT, PARI).forMobile().consumeBy(currentMeta -> {

                    createObservationMetaF(species, version, NAKO).forMobile().consumeBy(oldMeta -> {

                        final List<Observation> observations =
                                createList(5, () -> model().newMobileObservation(author, currentMeta));

                        onSavedAndAuthenticated(createUser(author), () -> {

                            final List<MobileObservationDTO> dtos = transformer.apply(observations, version);

                            for (int i = 0; i < observations.size(); i++) {
                                final MobileObservationDTO dto = dtos.get(i);
                                assertNotNull(dto);

                                assertEquals(NAKO, dto.getObservationType());
                            }
                        });
                    });
                });
            });
        });
    }

        @Theory
    public void testTranslationOfPoikueObservationType(final ObservationSpecVersion version) {
        assumeTrue(version.lessThan(LOWEST_VERSION_SUPPORTING_BIRD_LITTER_AND_COUPLE));

        withPerson(author -> {

            Stream.of(OFFICIAL_CODE_BEAN_GOOSE, OFFICIAL_CODE_GREYLAG_GOOSE).forEach(speciesCode -> {

                final GameSpecies species = model().newGameSpecies(speciesCode);

                createObservationMetaF(species, MOST_RECENT, POIKUE).forMobile().consumeBy(currentMeta -> {

                    createObservationMetaF(species, version, NAKO).forMobile().consumeBy(oldMeta -> {

                        final List<Observation> observations =
                                createList(5, () -> model().newMobileObservation(author, currentMeta));

                        onSavedAndAuthenticated(createUser(author), () -> {

                            final List<MobileObservationDTO> dtos = transformer.apply(observations, version);

                            for (int i = 0; i < observations.size(); i++) {
                                final MobileObservationDTO dto = dtos.get(i);
                                assertNotNull(dto);

                                assertEquals(NAKO, dto.getObservationType());
                            }
                        });
                    });
                });
            });
        });
    }

    @Theory
    public void testTranslationOfObservationWithinDeerHunting(final ObservationSpecVersion version) {
        assumeTrue(version.lessThan(LOWEST_VERSION_SUPPORTING_CATEGORY));

        withPerson(author -> {

            final GameSpecies species = model().newGameSpecies(OFFICIAL_CODE_WHITE_TAILED_DEER);

            createObservationMetaF(species, MOST_RECENT, DEER_HUNTING, NAKO).forMobile().consumeBy(newMeta -> {

                final List<Observation> observations = createList(5, () -> model().newObservation(author, newMeta, o -> {
                    o.setDeerHuntingType(STAND_HUNTING);
                    o.setDeerHuntingTypeDescription("Description");
                }));

                createObservationMetaF(species, version, NORMAL, NAKO).forMobile().consumeBy(oldMeta -> {

                    onSavedAndAuthenticated(createUser(author), () -> {
                        final List<MobileObservationDTO> dtos = transformer.apply(observations, version);

                        for (int i = 0; i < observations.size(); i++) {

                            // Sanity checks for observation are done here even though the DTO assertions are
                            // the beef of the test.
                            final Observation obs = observations.get(i);
                            assertEquals(DEER_HUNTING, obs.getObservationCategory());
                            assertEquals(STAND_HUNTING, obs.getDeerHuntingType());
                            assertEquals("Description", obs.getDeerHuntingTypeDescription());

                            final MobileObservationDTO dto = dtos.get(i);
                            assertNotNull(dto);
                            assertNull(dto.getObservationCategory());
                            assertNull(dto.getWithinMooseHunting());
                            assertNull(dto.getDeerHuntingType());
                            assertNull(dto.getDeerHuntingTypeDescription());
                            assertFalse(dto.isCanEdit());
                        }
                    });
                });
            });
        });
    }

    private static void assertFieldsExcludingPluralAssociations(final Observation observation,
                                                                final MobileObservationDTO dto,
                                                                final boolean canBeObservedWithinMooseHunting) {

        assertEquals(GameDiaryEntryType.OBSERVATION, dto.getType());

        assertNotNull(observation.getId());
        assertEquals(observation.getId(), dto.getId());

        assertNotNull(observation.getConsistencyVersion());
        assertEquals(observation.getConsistencyVersion(), dto.getRev());

        assertEquals(observation.getSpecies().getOfficialCode(), dto.getGameSpeciesCode());

        if (dto.getObservationSpecVersion().supportsCategory()) {
            assertEquals(observation.getObservationCategory(), dto.getObservationCategory());
        } else {
            if (canBeObservedWithinMooseHunting) {
                assertNotNull(dto.getWithinMooseHunting());
                assertEquals(
                        observation.getObservationCategory().isWithinMooseHunting(),
                        dto.getWithinMooseHunting().booleanValue());
            } else {
                assertNull(dto.getWithinMooseHunting());
            }
        }

        assertEquals(observation.getGeoLocation(), dto.getGeoLocation());
        assertEquals(observation.getPointOfTime().toLocalDateTime(), dto.getPointOfTime());
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
}

package fi.riista.feature.gamediary.mobile;

import com.google.common.collect.ImmutableMap;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenDTO;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.gamediary.image.GameDiaryImageRepository;
import fi.riista.feature.gamediary.observation.ObservationRepository;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenRepository;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.util.DateUtil;
import fi.riista.util.VersionedTestExecutionSupport;

import javaslang.Tuple;

import org.junit.Test;

import javax.annotation.Resource;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static fi.riista.feature.common.entity.Required.NO;
import static fi.riista.feature.common.entity.Required.VOLUNTARY;
import static fi.riista.feature.common.entity.Required.YES;
import static fi.riista.feature.gamediary.observation.ObservationSpecVersion.LOWEST_VERSION_SUPPORTING_XTRA_BEAVER_TYPES;
import static fi.riista.feature.gamediary.observation.ObservationSpecVersion.MOST_RECENT;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_CANADIAN_BEAVER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_EUROPEAN_BEAVER;
import static fi.riista.feature.gamediary.observation.ObservationType.AANI;
import static fi.riista.feature.gamediary.observation.ObservationType.JALKI;
import static fi.riista.feature.gamediary.observation.ObservationType.NAKO;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA_KEKO;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA_PENKKA;
import static fi.riista.feature.gamediary.observation.ObservationType.ULOSTE;
import static fi.riista.util.Asserts.assertEmpty;
import static fi.riista.util.DateUtil.today;
import static fi.riista.util.EqualityHelper.equalNotNull;
import static fi.riista.util.TestUtils.createList;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public abstract class MobileObservationFeatureTest extends EmbeddedDatabaseTest
        implements VersionedTestExecutionSupport<ObservationSpecVersion> {

    @Resource
    protected ObservationRepository observationRepo;

    @Resource
    protected ObservationSpecimenRepository specimenRepo;

    @Resource
    protected PersonRepository personRepo;

    @Resource
    protected GameDiaryImageRepository imageRepo;

    protected abstract MobileGameDiaryFeature feature();

    @Override
    public void onAfterVersionedTestExecution() {
        reset();
    }

    @Test
    public void testCreateObservation() {
        forEachVersion(v -> withPerson(author -> Stream.of(TRUE, FALSE, null).forEach(withinMooseHunting -> {

            // Add test coverage by testing "amount" field with all Required enums and with defined specimens or not.
            ImmutableMap.of(NAKO, YES, JALKI, VOLUNTARY, ULOSTE, NO).forEach((type, amount) -> {

                createObservationMetaF(v, withinMooseHunting, type).forMobile(true).withAmount(amount).consumeBy(m -> {

                    final Stream<Boolean> specimenInclusion = amount == NO ? Stream.of(false) : Stream.of(true, false);

                    onSavedAndAuthenticated(createUser(author), () -> specimenInclusion.forEach(includeSpecimens -> {

                        final MobileObservationDTO inputDto = m.dtoBuilder(includeSpecimens ? 5 : 0).build();
                        final MobileObservationDTO outputDto = invokeCreateObservation(inputDto);

                        doCreateAssertions(outputDto.getId(), inputDto, author);
                    }));
                });
            });
        })));
    }

    @Test
    public void testCreateObservation_forTranslationOfObsoleteBeaverObservationType() {
        forEachVersionBefore(LOWEST_VERSION_SUPPORTING_XTRA_BEAVER_TYPES, v -> withPerson(author -> {

            Stream.of(OFFICIAL_CODE_CANADIAN_BEAVER, OFFICIAL_CODE_EUROPEAN_BEAVER, OFFICIAL_CODE_BEAR)
                    .forEach(speciesCode -> {

                        final GameSpecies species = model().newGameSpecies(speciesCode);

                        createObservationMetaF(species, MOST_RECENT, PESA_KEKO).forMobile().consumeBy(updatedMeta -> {

                            createObservationMetaF(species, v, PESA).forMobile().consumeBy(oldMeta -> {

                                if (!species.isBeaver()) {
                                    model().newObservationContextSensitiveFields(
                                            species, false, PESA, MOST_RECENT.toIntValue());
                                }

                                onSavedAndAuthenticated(createUser(author), () -> {

                                    final MobileObservationDTO inputDto = oldMeta.dtoBuilder().build();
                                    final MobileObservationDTO outputDto = invokeCreateObservation(inputDto);

                                    final MobileObservationDTO expectedValues =
                                            species.isBeaver() ? updatedMeta.dtoBuilder(inputDto).build() : inputDto;

                                    doCreateAssertions(outputDto.getId(), expectedValues, author);
                                });
                            });
                        });
                    });
        }));
    }

    @Test
    public void testUpdateObservation() {
        forEachVersion(v -> withRhy(rhy -> withPerson(author -> {

            final ObservationType obsType = some(ObservationType.class);

            createObservationMetaF(v, obsType).forMobile(true).withAmount(VOLUNTARY).consumeBy(m -> {

                // Add test coverage by varying the voluntary "amount" field and number of specimens.
                // Tuple consists of (total amount of specimens / number of defined specimens).

                Stream.of(Tuple.of(5, 2), Tuple.of(3, 0), Tuple.<Integer, Integer> of(null, null)).forEach(tup -> {

                    final Observation observation = model().newMobileObservation(author, m);

                    onSavedAndAuthenticated(createUser(author), () -> {

                        final MobileObservationDTO inputDto = m.dtoBuilder(observation)
                                .mutate()
                                .withAmount(tup._1)
                                .chain(builder -> Optional.ofNullable(tup._2).ifPresent(builder::withSpecimens))
                                .build();

                        invokeUpdateObservation(inputDto);

                        doUpdateAssertions(observation.getId(), inputDto, author, 1, o -> {
                            assertNotNull(o.getRhy());
                            assertEquals(rhy.getOfficialCode(), o.getRhy().getOfficialCode());
                        });
                    });
                });
            });
        })));
    }

    @Test
    public void testUpdateObservation_whenNoChanges() {
        forEachVersion(v -> createObservationMetaF(v, NAKO).forMobile(true).createSpecimensF(5).consumeBy((m, f) -> {

            onSavedAndAuthenticated(createUser(f.author), () -> {

                final MobileObservationDTO inputDto =
                        m.dtoBuilder(f.observation).populateSpecimensWith(f.specimens).build();

                invokeUpdateObservation(inputDto);

                doUpdateAssertions(f.observation.getId(), inputDto, f.author, 0);
            });
        }));
    }

    @Test
    public void testUpdateObservation_whenUpdatingSpecimensOnly() {
        forEachVersion(v -> createObservationMetaF(v, NAKO).forMobile(true).createSpecimensF(5).consumeBy((m, f) -> {

            onSavedAndAuthenticated(createUser(f.author), () -> {

                final MobileObservationDTO inputDto = m.dtoBuilder(f.observation)
                        .populateSpecimensWith(f.specimens)
                        .mutateSpecimens()
                        .build();

                invokeUpdateObservation(inputDto);

                doUpdateAssertions(f.observation.getId(), inputDto, f.author, 1);
            });
        }));
    }

    @Test
    public void testUpdateObservation_forTranslationOfObsoleteBeaverObservationType() {
        forEachVersionBefore(LOWEST_VERSION_SUPPORTING_XTRA_BEAVER_TYPES, v -> withPerson(author -> {

            Stream.of(OFFICIAL_CODE_CANADIAN_BEAVER, OFFICIAL_CODE_EUROPEAN_BEAVER, OFFICIAL_CODE_BEAR)
                    .forEach(speciesCode -> {

                        final GameSpecies species = model().newGameSpecies(speciesCode);

                        createObservationMetaF(species, MOST_RECENT, PESA_KEKO).forMobile().consumeBy(updatedMeta -> {

                            createObservationMetaF(species, v, PESA).forMobile().consumeBy(oldMeta -> {

                                if (!species.isBeaver()) {
                                    model().newObservationContextSensitiveFields(
                                            species, false, PESA, MOST_RECENT.toIntValue());
                                }

                                final Observation observation = model().newMobileObservation(author, oldMeta);

                                onSavedAndAuthenticated(createUser(author), () -> {

                                    final MobileObservationDTO inputDto =
                                            oldMeta.dtoBuilder(observation).mutate().build();

                                    invokeUpdateObservation(inputDto);

                                    final MobileObservationDTO expectedValues =
                                            species.isBeaver() ? updatedMeta.dtoBuilder(inputDto).build() : inputDto;

                                    doUpdateAssertions(observation.getId(), expectedValues, author, 1);
                                });
                            });
                        });
                    });
        }));
    }

    @Test
    public void testUpdateObservation_updatedObservationTypeShouldNotBeReplacedWithDefaultTranslation() {
        forEachVersionBefore(LOWEST_VERSION_SUPPORTING_XTRA_BEAVER_TYPES, v -> withPerson(author -> {

            Stream.of(OFFICIAL_CODE_CANADIAN_BEAVER, OFFICIAL_CODE_EUROPEAN_BEAVER, OFFICIAL_CODE_BEAR)
                    .forEach(speciesCode -> {

                        final GameSpecies species = model().newGameSpecies(speciesCode);

                        createObservationMetaF(species, MOST_RECENT, PESA_PENKKA).forMobile().consumeBy(currentMeta -> {

                            createObservationMetaF(species, v, PESA).forMobile().consumeBy(oldMeta -> {

                                if (!species.isBeaver()) {
                                    model().newObservationContextSensitiveFields(
                                            species, false, PESA, MOST_RECENT.toIntValue());
                                }

                                final Observation observation = model().newMobileObservation(author, currentMeta);

                                onSavedAndAuthenticated(createUser(author), () -> {

                                    final MobileObservationDTO inputDto =
                                            oldMeta.dtoBuilder(observation).mutate().build();

                                    invokeUpdateObservation(inputDto);

                                    final MobileObservationDTO expectedValues =
                                            species.isBeaver() ? currentMeta.dtoBuilder(inputDto).build() : inputDto;

                                    doUpdateAssertions(observation.getId(), expectedValues, author, 1);
                                });
                            });
                        });
                    });
        }));
    }

    @Test
    public void testUpdateObservation_forAmountChange_whenSpecimensPresent() {
        forEachVersion(v -> createObservationMetaF(v, NAKO).forMobile(true).createSpecimensF(5).consumeBy((m, f) -> {

            onSavedAndAuthenticated(createUser(f.author), () -> {

                final MobileObservationDTO inputDto = m.dtoBuilder(f.observation)
                        .populateSpecimensWith(f.specimens)
                        .withAmount(f.specimens.size() + 10)
                        .build();

                invokeUpdateObservation(inputDto);

                doUpdateAssertions(f.observation.getId(), inputDto, f.author, 1);
            });
        }));
    }

    @Test
    public void testUpdateObservation_forAmountChange_whenSpecimensNotPresent() {
        forEachVersion(v -> withPerson(author -> createObservationMetaF(v, NAKO).forMobile(true).consumeBy(m -> {

            final Observation observation = model().newMobileObservation(author, m, 5);

            onSavedAndAuthenticated(createUser(author), () -> {

                final MobileObservationDTO inputDto = m.dtoBuilder(observation).withAmount(10).build();
                invokeUpdateObservation(inputDto);

                doUpdateAssertions(observation.getId(), inputDto, author, 1);
            });
        })));
    }

    @Test
    public void testUpdateObservation_whenChangingToObservationTypeNotAcceptingAmount() {
        forEachVersion(v -> createObservationMetaF(v, NAKO).forMobile(true).createSpecimensF(5).consumeBy(f -> {

            createObservationMetaF(v, AANI).forMobile(true).withAmount(NO).consumeBy(m -> {

                onSavedAndAuthenticated(createUser(f.author), () -> {

                    final MobileObservationDTO inputDto = m.dtoBuilder(f.observation).mutate().withAmount(null).build();
                    invokeUpdateObservation(inputDto);

                    doUpdateAssertions(f.observation.getId(), inputDto, f.author, 1);
                });
            });
        }));
    }

    @Test
    public void testUpdateObservation_whenGroupOriginatingFromMooseDataCard() {
        forEachVersion(version -> createObservationMetaF(version, true, NAKO).forMobile().consumeBy(m -> {

            withHuntingGroupFixture(m.getSpecies(), f -> {
                f.group.setFromMooseDataCard(true);

                final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today());
                final Observation observation = model().newMobileObservation(f.groupMember, m, huntingDay);

                createObservationMetaF(version, true, JALKI).forMobile().consumeBy(m2 -> {

                    onSavedAndAuthenticated(createUser(f.groupMember), () -> {

                        final MobileObservationDTO inputDto = m2.dtoBuilder(observation).mutate().build();
                        invokeUpdateObservation(inputDto);

                        final MobileObservationDTO expectedValues =
                                m.dtoBuilder(observation).withDescription(inputDto.getDescription()).build();

                        doUpdateAssertions(observation.getId(), expectedValues, f.groupMember, 1);
                    });
                });
            });
        }));
    }

    @Test
    public void testDeleteObservation_whenNotAttachedToHuntingDay() {
        forEachVersion(v -> createObservationMetaF(v, false, NAKO).forMobile().createSpecimensF(1).consumeBy(f -> {
            onSavedAndAuthenticated(createUser(f.author), () -> {
                final Long id = f.observation.getId();
                feature().deleteObservation(id);
                assertNull(observationRepo.findOne(id));
            });
        }));
    }

    @Test
    public void testDeleteObservation_whenAttachedToHuntingDay() {
        forEachVersion(v -> createObservationMetaF(v, true, NAKO).consumeBy(m -> withHuntingGroupFixture(m.getSpecies(),
                f -> {
                    final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today());
                    final Observation observation = model().newMobileObservation(f.groupMember, m, huntingDay);

                    onSavedAndAuthenticated(createUser(f.groupMember), () -> {
                        try {
                            feature().deleteObservation(observation.getId());
                            fail("Deletion of observation associated with a hunting day should fail");
                        } catch (final RuntimeException e) {
                            // Expected
                        }
                        assertNotNull(observationRepo.findOne(observation.getId()));
                    });
                })));
    }

    protected void doCreateAssertions(
            final long observationId, final MobileObservationDTO expectedValues, final Person expectedAuthor) {

        doCreateAssertions(observationId, expectedValues, expectedAuthor, o -> {});
    }

    protected void doCreateAssertions(
            final long observationId,
            final MobileObservationDTO expectedValues,
            final Person expectedAuthor,
            final Consumer<Observation> extraAssertions) {

        runInTransaction(() -> {
            final Observation observation = observationRepo.findOne(observationId);
            assertNotNull(observation);
            assertVersion(observation, 0);

            assertCommonExpectations(observation, expectedValues);

            final Person author = assertValidAuthor(observation, expectedAuthor.getId());
            assertTrue(observation.isActor(author));

            extraAssertions.accept(observation);
        });
    }

    protected void doUpdateAssertions(
            final long observationId,
            final MobileObservationDTO expectedValues,
            final Person expectedAuthor,
            final int expectedRevision) {

        doUpdateAssertions(observationId, expectedValues, expectedAuthor, expectedRevision, o -> {});
    }

    protected void doUpdateAssertions(
            final long observationId,
            final MobileObservationDTO expectedValues,
            final Person expectedAuthor,
            final int expectedRevision,
            final Consumer<Observation> extraAssertions) {

        runInTransaction(() -> {
            final Observation observation = observationRepo.findOne(observationId);
            assertNotNull(observation);
            assertVersion(observation, expectedRevision);

            assertCommonExpectations(observation, expectedValues);
            assertValidAuthor(observation, expectedAuthor.getId());

            extraAssertions.accept(observation);
        });
    }

    protected void assertCommonExpectations(final Observation observation,
                                            final MobileObservationDTO expectedValues) {

        assertNotNull(observation);

        assertTrue(observation.isFromMobile());
        assertNotNull(observation.getMobileClientRefId());
        assertEquals(expectedValues.getMobileClientRefId(), observation.getMobileClientRefId());

        assertEquals(expectedValues.getGeoLocation(), observation.getGeoLocation());
        assertEquals(GeoLocation.Source.GPS_DEVICE, observation.getGeoLocation().getSource());

        assertEquals(DateUtil.toDateNullSafe(expectedValues.getPointOfTime()), observation.getPointOfTime());
        assertEquals(expectedValues.getGameSpeciesCode(), observation.getSpecies().getOfficialCode());
        assertEquals(expectedValues.getWithinMooseHunting(), observation.getWithinMooseHunting());
        assertEquals(expectedValues.getObservationType(), observation.getObservationType());
        assertEquals(expectedValues.getDescription(), observation.getDescription());

        assertTrue(observation.isAmountEqualTo(expectedValues.getAmount()));
        assertEquals(expectedValues.getMooselikeMaleAmount(), observation.getMooselikeMaleAmount());
        assertEquals(expectedValues.getMooselikeFemaleAmount(), observation.getMooselikeFemaleAmount());
        assertEquals(expectedValues.getMooselikeFemale1CalfAmount(), observation.getMooselikeFemale1CalfAmount());
        assertEquals(expectedValues.getMooselikeFemale2CalfsAmount(), observation.getMooselikeFemale2CalfsAmount());
        assertEquals(expectedValues.getMooselikeFemale3CalfsAmount(), observation.getMooselikeFemale3CalfsAmount());
        assertEquals(expectedValues.getMooselikeFemale4CalfsAmount(), observation.getMooselikeFemale4CalfsAmount());
        assertEquals(
                expectedValues.getMooselikeUnknownSpecimenAmount(), observation.getMooselikeUnknownSpecimenAmount());

        assertSpecimens(
                specimenRepo.findByObservation(observation),
                expectedValues.getSpecimens(),
                expectedValues.getObservationSpecVersion());

        assertEmpty(imageRepo.findByObservation(observation));
    }

    protected Person assertValidAuthor(final Observation observation, final long expectedAuthorId) {
        final Person author = personRepo.findOne(expectedAuthorId);
        assertNotNull(author);
        assertTrue(observation.isAuthor(author));
        return author;
    }

    protected void assertSpecimens(
            final List<ObservationSpecimen> specimens,
            final List<ObservationSpecimenDTO> expectedSpecimens,
            final ObservationSpecVersion version) {

        final int numSpecimenDTOs = Optional.ofNullable(expectedSpecimens).map(List::size).orElse(0);
        assertEquals(numSpecimenDTOs, specimens.size());

        if (numSpecimenDTOs > 0) {
            assertTrue(equalNotNull(specimens, expectedSpecimens, ObservationSpecimenDTO.equalToEntity(version)));
        }
    }

    protected List<ObservationSpecimen> createSpecimens(final Observation observation, final int numSpecimens) {
        return createList(numSpecimens, () -> model().newObservationSpecimen(observation));
    }

    protected MobileObservationDTO invokeCreateObservation(final MobileObservationDTO input) {
        return withVersionChecked(feature().createObservation(input));
    }

    protected MobileObservationDTO invokeUpdateObservation(final MobileObservationDTO input) {
        return withVersionChecked(feature().updateObservation(input));
    }

    private MobileObservationDTO withVersionChecked(final MobileObservationDTO dto) {
        return checkDtoVersionAgainstEntity(dto, Observation.class);
    }

}

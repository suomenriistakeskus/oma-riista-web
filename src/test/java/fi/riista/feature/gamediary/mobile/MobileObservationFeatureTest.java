package fi.riista.feature.gamediary.mobile;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Required;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.fixture.ObservationFixtureMixin;
import fi.riista.feature.gamediary.image.GameDiaryImageRepository;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationRepository;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence;
import fi.riista.feature.gamediary.observation.metadata.ObservationContextParameters;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenDTO;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenRepository;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.VersionedTestExecutionSupport;
import io.vavr.Tuple;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_CANADIAN_BEAVER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_EUROPEAN_BEAVER;
import static fi.riista.feature.gamediary.observation.ObservationSpecVersion.LOWEST_VERSION_SUPPORTING_LARGE_CARNIVORE_FIELDS;
import static fi.riista.feature.gamediary.observation.ObservationSpecVersion.LOWEST_VERSION_SUPPORTING_XTRA_BEAVER_TYPES;
import static fi.riista.feature.gamediary.observation.ObservationSpecVersion.MOST_RECENT;
import static fi.riista.feature.gamediary.observation.ObservationType.AANI;
import static fi.riista.feature.gamediary.observation.ObservationType.JALKI;
import static fi.riista.feature.gamediary.observation.ObservationType.NAKO;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA_KEKO;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA_PENKKA;
import static fi.riista.feature.gamediary.observation.ObservationType.ULOSTE;
import static fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence.NO;
import static fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence.VOLUNTARY;
import static fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence.VOLUNTARY_CARNIVORE_AUTHORITY;
import static fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence.YES;
import static fi.riista.feature.organization.occupation.OccupationType.PETOYHDYSHENKILO;
import static fi.riista.test.Asserts.assertEmpty;
import static fi.riista.util.DateUtil.today;
import static fi.riista.util.EqualityHelper.equalNotNull;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public abstract class MobileObservationFeatureTest extends EmbeddedDatabaseTest
        implements HuntingGroupFixtureMixin, ObservationFixtureMixin,
        VersionedTestExecutionSupport<ObservationSpecVersion> {

    @Resource
    protected MobileGameDiaryFeature feature;

    @Resource
    protected ObservationRepository observationRepo;

    @Resource
    protected ObservationSpecimenRepository specimenRepo;

    @Resource
    protected PersonRepository personRepo;

    @Resource
    protected GameDiaryImageRepository imageRepo;

    @Resource
    protected UserAuthorizationHelper userAuthorizationHelper;

    protected final ObservationContextParameters params = new ObservationContextParameters(() -> {
        return callInTransaction(() -> userAuthorizationHelper.isCarnivoreContactPersonAnywhere(today()))
                .booleanValue();
    });

    @Override
    public void onAfterVersionedTestExecution() {
        reset();
    }

    @Test
    public void testCreateObservation() {
        forEachVersion(v -> withPerson(author -> Stream.of(TRUE, FALSE, null).forEach(withinMooseHunting -> {

            final SystemUser user = createUser(author);

            // Add test coverage by testing 'amount' field with all Required enums with specified
            // specimens and without them.
            ImmutableMap.of(NAKO, YES, JALKI, VOLUNTARY, ULOSTE, NO).forEach((type, amount) -> {

                createObservationMetaF(v, withinMooseHunting, type).forMobile(true).withAmount(amount).consumeBy(m -> {

                    (amount == NO ? IntStream.of(0) : IntStream.of(5, 0)).forEach(numSpecimens -> {

                        onSavedAndAuthenticated(user, () -> {

                            final MobileObservationDTO inputDto =
                                    m.dtoBuilder(params).withAmountAndSpecimens(numSpecimens).build();
                            final MobileObservationDTO outputDto = invokeCreateObservation(inputDto);

                            doCreateAssertions(outputDto.getId(), inputDto, author);
                        });
                    });
                });
            });
        })));
    }

    @Test
    public void testCreateObservation_forTranslationOfObsoleteBeaverObservationType() {
        forEachVersionBefore(LOWEST_VERSION_SUPPORTING_XTRA_BEAVER_TYPES, v -> withPerson(author -> {

            final SystemUser user = createUser(author);

            Stream.of(OFFICIAL_CODE_CANADIAN_BEAVER, OFFICIAL_CODE_EUROPEAN_BEAVER, OFFICIAL_CODE_BEAR)
                    .forEach(speciesCode -> {

                        final GameSpecies species = model().newGameSpecies(speciesCode);

                        createObservationMetaF(species, PESA_KEKO).forMobile().consumeBy(updatedMeta -> {

                            createObservationMetaF(species, v, PESA).forMobile().consumeBy(oldMeta -> {

                                if (!species.isBeaver()) {
                                    model().newObservationContextSensitiveFields(species, false, PESA, MOST_RECENT);
                                }

                                onSavedAndAuthenticated(user, () -> {

                                    final MobileObservationDTO inputDto = oldMeta.dtoBuilder(params).build();
                                    final MobileObservationDTO outputDto = invokeCreateObservation(inputDto);

                                    final MobileObservationDTO expectedValues = species.isBeaver()
                                            ? updatedMeta.dtoBuilder(params).populateWith(inputDto).build()
                                            : inputDto;

                                    doCreateAssertions(outputDto.getId(), expectedValues, author);
                                });
                            });
                        });
                    });
        }));
    }

    @Test
    public void testCreateObservation_whenMooseObservedWithinHunting() {
        forEachVersion(v -> withRhy(rhy -> withPerson(author -> {

            createObservationMetaF(GameSpecies.OFFICIAL_CODE_MOOSE, v, NAKO)
                    .forMobile(true)
                    .withAmount(NO)
                    .withMooselikeAmountFieldsAs(Required.YES)
                    .consumeBy(meta -> {

                        onSavedAndAuthenticated(createUser(author), () -> {

                            final MobileObservationDTO inputDto =
                                    meta.dtoBuilder(params).mutateMooselikeAmountFields().build();
                            final MobileObservationDTO outputDto = invokeCreateObservation(inputDto);

                            if (!v.supportsMooselikeCalfAmount()) {
                                inputDto.setMooselikeCalfAmount(0);
                            }

                            // Total amount field is illegal for mooselike observations in hunting
                            // scenarios because it is automatically calculated.
                            inputDto.setAmount(inputDto.getSumOfMooselikeAmountFields());

                            doCreateAssertions(outputDto.getId(), inputDto, author);
                        });
                    });
        })));
    }

    public void testCreateObservation_forLargeCarnivoreFieldsPersisted() {
        forEachVersionStartingFrom(LOWEST_VERSION_SUPPORTING_LARGE_CARNIVORE_FIELDS, v -> {
            withRhy(rhy -> withPerson(author -> {

                model().newOccupation(rhy, author, OccupationType.PETOYHDYSHENKILO);

                createObservationMetaF(GameSpecies.OFFICIAL_CODE_WOLF, NAKO)
                        .withLargeCarnivoreFieldsAs(DynamicObservationFieldPresence.VOLUNTARY_CARNIVORE_AUTHORITY)
                        .forMobile()
                        .consumeBy(meta -> {

                            onSavedAndAuthenticated(createUser(author), () -> {

                                final MobileObservationDTO inputDto = meta.dtoBuilder(params)
                                        .mutateLargeCarnivoreFieldsAsserting(notNullValue())
                                        .withAmountAndSpecimens(5)
                                        .build();

                                final MobileObservationDTO outputDto = invokeCreateObservation(inputDto);

                                doCreateAssertions(outputDto.getId(), inputDto, author);
                            });
                        });
            }));
        });
    }

    @Test
    public void testUpdateObservation() {
        forEachVersion(v -> withRhy(rhy -> withPerson(author -> {

            final SystemUser user = createUser(author);
            final ObservationType obsType = some(ObservationType.class);

            createObservationMetaF(v, obsType).forMobile(true).withAmount(VOLUNTARY).consumeBy(m -> {

                // Add test coverage by varying the voluntary "amount" field and number of specimens.
                // Tuple consists of (total amount of specimens / number of defined specimens).

                Stream.of(Tuple.of(5, 2), Tuple.of(3, 0), Tuple.<Integer, Integer>of(null, null)).forEach(tup -> {

                    final Observation observation = model().newMobileObservation(author, m);

                    onSavedAndAuthenticated(user, () -> {

                        final MobileObservationDTO dto = m.dtoBuilder(params)
                                .populateWith(observation)
                                .mutate()
                                .withAmount(tup._1)
                                .chain(builder -> Optional.ofNullable(tup._2).ifPresent(builder::withSpecimens))
                                .build();

                        invokeUpdateObservation(dto);

                        doUpdateAssertions(dto, author, 1, o -> {
                            assertNotNull(o.getRhy());
                            assertEquals(rhy.getOfficialCode(), o.getRhy().getOfficialCode());
                        });
                    });
                });
            });
        })));
    }

    @Test
    public void testUpdateObservation_whenNothingChanged() {
        forEachVersion(v -> createObservationMetaF(v, NAKO).forMobile(true).createSpecimensF(5).consumeBy((m, f) -> {

            onSavedAndAuthenticated(createUser(f.author), () -> {

                final MobileObservationDTO dto =
                        m.dtoBuilder(params).populateWith(f.observation).populateSpecimensWith(f.specimens).build();

                invokeUpdateObservation(dto);

                doUpdateAssertions(dto, f.author, 0);
            });
        }));
    }

    @Test
    public void testUpdateObservation_whenUpdatingSpecimensOnly() {
        forEachVersion(v -> createObservationMetaF(v, NAKO).forMobile(true).createSpecimensF(5).consumeBy((m, f) -> {

            onSavedAndAuthenticated(createUser(f.author), () -> {

                final MobileObservationDTO dto = m.dtoBuilder(params)
                        .populateWith(f.observation)
                        .populateSpecimensWith(f.specimens)
                        .mutateSpecimens()
                        .build();

                invokeUpdateObservation(dto);

                doUpdateAssertions(dto, f.author, 1);
            });
        }));
    }

    @Test
    public void testUpdateObservation_forTranslationOfObsoleteBeaverObservationType() {
        forEachVersionBefore(LOWEST_VERSION_SUPPORTING_XTRA_BEAVER_TYPES, v -> withPerson(author -> {

            final SystemUser user = createUser(author);

            Stream.of(OFFICIAL_CODE_CANADIAN_BEAVER, OFFICIAL_CODE_EUROPEAN_BEAVER, OFFICIAL_CODE_BEAR)
                    .forEach(speciesCode -> {

                        final GameSpecies species = model().newGameSpecies(speciesCode);

                        createObservationMetaF(species, PESA_KEKO).forMobile().consumeBy(updatedMeta -> {

                            createObservationMetaF(species, v, PESA).forMobile().consumeBy(oldMeta -> {

                                if (!species.isBeaver()) {
                                    model().newObservationContextSensitiveFields(species, false, PESA, MOST_RECENT);
                                }

                                final Observation observation = model().newMobileObservation(author, oldMeta);

                                onSavedAndAuthenticated(user, () -> {

                                    final MobileObservationDTO inputDto =
                                            oldMeta.dtoBuilder(params).populateWith(observation).mutate().build();

                                    invokeUpdateObservation(inputDto);

                                    final MobileObservationDTO expectedValues = species.isBeaver()
                                            ? updatedMeta.dtoBuilder(params).populateWith(inputDto).build()
                                            : inputDto;

                                    doUpdateAssertions(expectedValues, author, 1);
                                });
                            });
                        });
                    });
        }));
    }

    @Test
    public void testUpdateObservation_updatedObservationTypeShouldNotBeReplacedWithDefaultTranslation() {
        forEachVersionBefore(LOWEST_VERSION_SUPPORTING_XTRA_BEAVER_TYPES, v -> withPerson(author -> {

            final SystemUser user = createUser(author);

            Stream.of(OFFICIAL_CODE_CANADIAN_BEAVER, OFFICIAL_CODE_EUROPEAN_BEAVER, OFFICIAL_CODE_BEAR)
                    .forEach(speciesCode -> {

                        final GameSpecies species = model().newGameSpecies(speciesCode);

                        createObservationMetaF(species, PESA_PENKKA).forMobile().consumeBy(currentMeta -> {

                            createObservationMetaF(species, v, PESA).forMobile().consumeBy(oldMeta -> {

                                if (!species.isBeaver()) {
                                    model().newObservationContextSensitiveFields(species, false, PESA, MOST_RECENT);
                                }

                                final Observation observation = model().newMobileObservation(author, currentMeta);

                                onSavedAndAuthenticated(user, () -> {

                                    final MobileObservationDTO inputDto =
                                            oldMeta.dtoBuilder(params).populateWith(observation).mutate().build();

                                    invokeUpdateObservation(inputDto);

                                    final MobileObservationDTO expectedValues = species.isBeaver()
                                            ? currentMeta.dtoBuilder(params).populateWith(inputDto).build()
                                            : inputDto;

                                    doUpdateAssertions(expectedValues, author, 1);
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

                final MobileObservationDTO dto = m.dtoBuilder(params)
                        .populateWith(f.observation)
                        .populateSpecimensWith(f.specimens)
                        .withAmount(f.specimens.size() + 10)
                        .build();

                invokeUpdateObservation(dto);

                doUpdateAssertions(dto, f.author, 1);
            });
        }));
    }

    @Test
    public void testUpdateObservation_forAmountChange_whenSpecimensAbsent() {
        forEachVersion(v -> withPerson(author -> {

            createObservationMetaF(v, NAKO).forMobile(true).createSpecimensF(author, 5).consumeBy((m, f) -> {

                onSavedAndAuthenticated(createUser(author), () -> {

                    final MobileObservationDTO dto =
                            m.dtoBuilder(params).populateWith(f.observation).withAmount(10).build();
                    invokeUpdateObservation(dto);

                    doUpdateAssertions(dto, author, 1);
                });
            });
        }));
    }

    @Test
    public void testUpdateObservation_whenChangingToObservationTypeNotAcceptingAmount() {
        forEachVersion(v -> createObservationMetaF(v, NAKO).forMobile(true).createSpecimensF(5).consumeBy(f -> {

            createObservationMetaF(v, AANI).forMobile(true).withAmount(NO).consumeBy(m -> {

                onSavedAndAuthenticated(createUser(f.author), () -> {

                    final MobileObservationDTO dto =
                            m.dtoBuilder(params).populateWith(f.observation).mutate().withAmount(null).build();
                    invokeUpdateObservation(dto);

                    doUpdateAssertions(dto, f.author, 1);
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

                        final MobileObservationDTO inputDto =
                                m2.dtoBuilder(params).populateWith(observation).mutate().build();
                        invokeUpdateObservation(inputDto);

                        final MobileObservationDTO expectedValues = m.dtoBuilder(params)
                                .populateWith(observation)
                                .withDescription(inputDto.getDescription())
                                .build();

                        doUpdateAssertions(expectedValues, f.groupMember, 1);
                    });
                });
            });
        }));
    }

    @Test
    public void testUpdateObservation_forLargeCarnivoreFieldsMutated() {
        forEachVersionStartingFrom(LOWEST_VERSION_SUPPORTING_LARGE_CARNIVORE_FIELDS, v -> withRhy(rhy -> {

            createObservationMetaF(GameSpecies.OFFICIAL_CODE_WOLF, NAKO)
                    .withLargeCarnivoreFieldsAs(DynamicObservationFieldPresence.VOLUNTARY_CARNIVORE_AUTHORITY)
                    .forMobile()
                    .createSpecimensF(5)
                    .withCarnivoreAuthority(true)
                    .consumeBy((meta, fixture) -> {

                        model().newOccupation(rhy, fixture.author, OccupationType.PETOYHDYSHENKILO);

                        onSavedAndAuthenticated(createUser(fixture.author), () -> {

                            final MobileObservationDTO dto = meta.dtoBuilder(params)
                                    .populateWith(fixture.observation)
                                    .populateSpecimensWith(fixture.specimens)
                                    .mutateLargeCarnivoreFieldsAsserting(notNullValue())
                                    .mutateSpecimens()
                                    .build();

                            invokeUpdateObservation(dto);

                            doUpdateAssertions(dto, fixture.author, 2);
                        });
                    });
        }));
    }

    @Test
    public void testUpdateObservation_forLargeCarnivore_carnivoreFieldsNotMutatedWhenAuthorityExpired() {
        forEachVersionStartingFrom(LOWEST_VERSION_SUPPORTING_LARGE_CARNIVORE_FIELDS, v -> {

            withRhy(rhy -> withPerson(author -> {

                model().newOccupation(rhy, author, PETOYHDYSHENKILO, today().minusYears(1), today().minusWeeks(1));

                createObservationMetaF(GameSpecies.OFFICIAL_CODE_WOLF, NAKO)
                        .withLargeCarnivoreFieldsAs(VOLUNTARY_CARNIVORE_AUTHORITY)
                        .forMobile()
                        .createSpecimensF(author, 1)
                        .withCarnivoreAuthority(true)
                        .consumeBy((m, f) -> {

                            onSavedAndAuthenticated(createUser(author), () -> {

                                final MobileObservationDTO inputDto = m.dtoBuilder(params)
                                        .populateWith(f.observation)
                                        .populateSpecimensWith(f.specimens)
                                        .withDescription("xyz" + nextPositiveInt())
                                        .mutateLargeCarnivoreFieldsAsserting(nullValue())
                                        .mutateSpecimens()
                                        .build();

                                invokeUpdateObservation(inputDto);

                                final MobileObservationDTO expectedValues = m.dtoBuilder(params)
                                        .populateWith(f.observation)
                                        .populateSpecimensWith(f.specimens)
                                        .withDescription(inputDto.getDescription())
                                        .build();

                                doUpdateAssertions(expectedValues, author, 1);
                            });
                        });
            }));
        });
    }

    @Test
    public void testUpdateObservation_forLargeCarnivoreFieldsNotNulledWhenClientDoesNotSupportThem() {
        forEachVersionBefore(LOWEST_VERSION_SUPPORTING_LARGE_CARNIVORE_FIELDS, v -> withRhy(rhy -> {

            createObservationMetaF(GameSpecies.OFFICIAL_CODE_WOLF, NAKO)
                    .withLargeCarnivoreFieldsAs(DynamicObservationFieldPresence.VOLUNTARY_CARNIVORE_AUTHORITY)
                    .forMobile()
                    .createSpecimensF(5)
                    .withCarnivoreAuthority(true)
                    .consumeBy((newMeta, fixture) -> {

                        model().newOccupation(rhy, fixture.author, OccupationType.PETOYHDYSHENKILO);

                        createObservationMetaF(newMeta.getSpecies(), v, NAKO).forMobile().consumeBy(oldMeta -> {

                            onSavedAndAuthenticated(createUser(fixture.author), () -> {

                                final Observation observation = fixture.observation;

                                final MobileObservationDTO dto = oldMeta.dtoBuilder(params)
                                        .populateWith(observation)
                                        .populateSpecimensWith(fixture.specimens)
                                        .mutate()
                                        .mutateSpecimens()
                                        .build();

                                invokeUpdateObservation(dto);

                                dto.setVerifiedByCarnivoreAuthority(observation.getVerifiedByCarnivoreAuthority());
                                dto.setObserverName(observation.getObserverName());
                                dto.setObserverPhoneNumber(observation.getObserverPhoneNumber());
                                dto.setOfficialAdditionalInfo(observation.getOfficialAdditionalInfo());

                                doUpdateAssertions(dto, fixture.author, 2, o -> {
                                    specimenRepo.findByObservation(o).forEach(specimen -> {
                                        assertNotNull(specimen.getWidthOfPaw());
                                        assertNotNull(specimen.getLengthOfPaw());
                                    });
                                });
                            });
                        });
                    });
        }));
    }

    @Test
    public void testUpdateObservation_forLargeCarnivoreFieldsNulledWhenSpeciesChangedToNonCarnivore() {
        forEachVersion(v -> withRhy(rhy -> withPerson(author -> {

            model().newOccupation(rhy, author, OccupationType.PETOYHDYSHENKILO);

            createObservationMetaF(GameSpecies.OFFICIAL_CODE_WOLF, NAKO)
                    .withLargeCarnivoreFieldsAs(DynamicObservationFieldPresence.VOLUNTARY_CARNIVORE_AUTHORITY)
                    .forMobile()
                    .createSpecimensF(author, 5)
                    .withCarnivoreAuthority(true)
                    .consumeBy((wolfMeta, fixture) -> {

                        createObservationMetaF(GameSpecies.OFFICIAL_CODE_MOOSE, v, NAKO)
                                .withLargeCarnivoreFieldsAs(DynamicObservationFieldPresence.NO)
                                .forMobile(true)
                                .consumeBy(mooseMeta -> {

                                    onSavedAndAuthenticated(createUser(author), () -> {

                                        final MobileObservationDTO dto = mooseMeta.dtoBuilder(params)
                                                .populateWith(fixture.observation)
                                                .populateSpecimensWith(fixture.specimens)
                                                .mutate()
                                                .mutateLargeCarnivoreFieldsAsserting(nullValue())
                                                .mutateSpecimens()
                                                .build();

                                        invokeUpdateObservation(dto);

                                        doUpdateAssertions(dto, author, 2);
                                    });
                                });
                    });
        })));
    }

    @Test
    public void testUpdateObservation_whenMooseObservedWithinHunting() {
        forEachVersion(v -> withRhy(rhy -> withPerson(author -> {

            createObservationMetaF(GameSpecies.OFFICIAL_CODE_MOOSE, v, NAKO)
                    .forMobile(true)
                    .withAmount(NO)
                    .withMooselikeAmountFieldsAs(Required.YES)
                    .consumeBy(meta -> {

                        final Observation observation =
                                model().newMobileObservation(author, meta.getMostRecentMetadata());

                        onSavedAndAuthenticated(createUser(author), () -> {

                            final MobileObservationDTO dto = meta.dtoBuilder(params)
                                    .populateWith(observation)
                                    .mutateMooselikeAmountFields()
                                    .build();

                            invokeUpdateObservation(dto);

                            if (!v.supportsMooselikeCalfAmount()) {
                                dto.setMooselikeCalfAmount(0);
                            }

                            // Total amount field is illegal for mooselike observations in hunting
                            // scenarios because it is automatically calculated.
                            dto.setAmount(dto.getSumOfMooselikeAmountFields());

                            doUpdateAssertions(dto, author, 1);
                        });
                    });
        })));
    }

    @Test
    public void testDeleteObservation_whenNotAttachedToHuntingDay() {
        forEachVersion(v -> createObservationMetaF(v, false, NAKO).forMobile().createSpecimensF(1).consumeBy(f -> {
            onSavedAndAuthenticated(createUser(f.author), () -> {
                final Long id = f.observation.getId();
                feature.deleteObservation(id);
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
                            feature.deleteObservation(observation.getId());
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

        doCreateAssertions(observationId, expectedValues, expectedAuthor, o -> {
        });
    }

    protected void doCreateAssertions(final long observationId,
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

    protected void doUpdateAssertions(final MobileObservationDTO expectedValues,
                                      final Person expectedAuthor,
                                      final int expectedRevision) {

        doUpdateAssertions(expectedValues, expectedAuthor, expectedRevision, o -> {
        });
    }

    protected void doUpdateAssertions(final MobileObservationDTO expectedValues,
                                      final Person expectedAuthor,
                                      final int expectedRevision,
                                      final Consumer<Observation> extraAssertions) {

        runInTransaction(() -> {
            final Observation observation = observationRepo.findOne(expectedValues.getId());
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

        assertEquals(expectedValues.getAmount(), observation.getAmount());
        assertEquals(expectedValues.getMooselikeMaleAmount(), observation.getMooselikeMaleAmount());
        assertEquals(expectedValues.getMooselikeFemaleAmount(), observation.getMooselikeFemaleAmount());
        assertEquals(expectedValues.getMooselikeCalfAmount(), observation.getMooselikeCalfAmount());
        assertEquals(expectedValues.getMooselikeFemale1CalfAmount(), observation.getMooselikeFemale1CalfAmount());
        assertEquals(expectedValues.getMooselikeFemale2CalfsAmount(), observation.getMooselikeFemale2CalfsAmount());
        assertEquals(expectedValues.getMooselikeFemale3CalfsAmount(), observation.getMooselikeFemale3CalfsAmount());
        assertEquals(expectedValues.getMooselikeFemale4CalfsAmount(), observation.getMooselikeFemale4CalfsAmount());
        assertEquals(
                expectedValues.getMooselikeUnknownSpecimenAmount(), observation.getMooselikeUnknownSpecimenAmount());

        assertEquals(expectedValues.getVerifiedByCarnivoreAuthority(), observation.getVerifiedByCarnivoreAuthority());
        assertEquals(expectedValues.getObserverName(), observation.getObserverName());
        assertEquals(expectedValues.getObserverPhoneNumber(), observation.getObserverPhoneNumber());
        assertEquals(expectedValues.getOfficialAdditionalInfo(), observation.getOfficialAdditionalInfo());

        final List<ObservationSpecimenDTO> expectedSpecimens = expectedValues.getSpecimens();
        final List<ObservationSpecimen> actualSpecimens = specimenRepo.findByObservation(observation);

        final int numSpecimenDTOs = Optional.ofNullable(expectedSpecimens).map(List::size).orElse(0);
        assertEquals(numSpecimenDTOs, actualSpecimens.size());

        if (numSpecimenDTOs > 0) {
            assertTrue(equalNotNull(actualSpecimens, expectedSpecimens, expectedValues.specimenOps()::equalContent));
        }

        assertEmpty(imageRepo.findByObservation(observation));
    }

    protected Person assertValidAuthor(final Observation observation, final long expectedAuthorId) {
        final Person author = personRepo.findOne(expectedAuthorId);
        assertNotNull(author);
        assertTrue(observation.isAuthor(author));
        return author;
    }

    protected MobileObservationDTO invokeCreateObservation(final MobileObservationDTO input) {
        return withVersionChecked(feature.createObservation(input));
    }

    protected MobileObservationDTO invokeUpdateObservation(final MobileObservationDTO input) {
        return withVersionChecked(feature.updateObservation(input));
    }

    private MobileObservationDTO withVersionChecked(final MobileObservationDTO dto) {
        return checkDtoVersionAgainstEntity(dto, Observation.class);
    }
}

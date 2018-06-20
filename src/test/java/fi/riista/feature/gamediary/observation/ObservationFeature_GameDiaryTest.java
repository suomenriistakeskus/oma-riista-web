package fi.riista.feature.gamediary.observation;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.fixture.ObservationFixtureMixin;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.F;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import static fi.riista.feature.gamediary.observation.ObservationType.AANI;
import static fi.riista.feature.gamediary.observation.ObservationType.MUUTON_AIKAINEN_LEPAILYALUE;
import static fi.riista.feature.gamediary.observation.ObservationType.NAKO;
import static fi.riista.feature.gamediary.observation.ObservationType.RIISTAKAMERA;
import static fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence.NO;
import static fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence.VOLUNTARY_CARNIVORE_AUTHORITY;
import static fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence.YES;
import static fi.riista.feature.organization.occupation.OccupationType.PETOYHDYSHENKILO;
import static fi.riista.util.DateUtil.today;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class ObservationFeature_GameDiaryTest extends ObservationFeatureTestBase
        implements HuntingGroupFixtureMixin, ObservationFixtureMixin {

    @Test
    public void testCreateObservation_withinMooseHunting() {
        doTestCreateObservation(true);
    }

    @Test
    public void testCreateObservation_notWithinMooseHunting() {
        doTestCreateObservation(null);
    }

    private void doTestCreateObservation(final Boolean withinMooseHunting) {
        withRhy(rhy -> withPerson(author -> createObservationMetaF(withinMooseHunting, NAKO).consumeBy(m -> {

            onSavedAndAuthenticated(createUser(author), () -> {
                final ObservationDTO inputDto = m.dtoBuilder(params).withAmountAndSpecimens(3).build();
                final ObservationDTO outputDto = invokeCreateObservation(inputDto);

                doCreateAssertions(outputDto.getId(), inputDto, author, author, o -> {
                    assertEquals(author.getId(), F.getId(o.getObserver()));
                    assertNotNull(o.getRhy());
                    assertEquals(rhy.getOfficialCode(), o.getRhy().getOfficialCode());
                });
            });
        })));
    }

    @Test
    public void testCreateObservation_withAmountButNoSpecimens() {
        withPerson(author -> createObservationMetaF(MUUTON_AIKAINEN_LEPAILYALUE).withAmount(YES).consumeBy(m -> {

            onSavedAndAuthenticated(createUser(author), () -> {

                final ObservationDTO inputDto = m.dtoBuilder(params).withAmount(13).build();
                final ObservationDTO outputDto = invokeCreateObservation(inputDto);

                doCreateAssertions(outputDto.getId(), inputDto, author, author);
            });
        }));
    }

    @Test
    public void testCreateObservation_forLargeCarnivore_withCarnivoreAuthority() {
        withRhy(rhy -> withPerson(author -> {

            model().newOccupation(rhy, author, PETOYHDYSHENKILO);

            createObservationMetaF(GameSpecies.OFFICIAL_CODE_WOLF, NAKO)
                    .withLargeCarnivoreFieldsAs(VOLUNTARY_CARNIVORE_AUTHORITY)
                    .consumeBy(m -> {

                        onSavedAndAuthenticated(createUser(author), () -> {

                            final ObservationDTO inputDto = m.dtoBuilder(params)
                                    .withAmountAndSpecimens(1)
                                    .mutateLargeCarnivoreFieldsAsserting(notNullValue())
                                    .build();
                            final ObservationDTO outputDto = invokeCreateObservation(inputDto);

                            doCreateAssertions(outputDto.getId(), inputDto, author, author);
                        });
                    });
        }));
    }

    @Test
    public void testCreateObservation_forLargeCarnivore_whenCarnivoreAuthorityExpired() {
        withRhy(rhy -> withPerson(author -> {

            model().newOccupation(rhy, author, PETOYHDYSHENKILO, today().minusYears(1), today().minusWeeks(1));

            createObservationMetaF(GameSpecies.OFFICIAL_CODE_WOLF, NAKO)
                    .withLargeCarnivoreFieldsAs(VOLUNTARY_CARNIVORE_AUTHORITY)
                    .consumeBy(m -> {

                        onSavedAndAuthenticated(createUser(author), () -> {

                            final ObservationDTO inputDto = m.dtoBuilder(params)
                                    .withAmountAndSpecimens(1)
                                    .build();
                            final ObservationDTO outputDto = invokeCreateObservation(inputDto);

                            doCreateAssertions(outputDto.getId(), inputDto, author, author, o -> {
                                assertNull(o.getVerifiedByCarnivoreAuthority());
                                assertNull(o.getObserverName());
                                assertNull(o.getObserverPhoneNumber());
                                assertNull(o.getOfficialAdditionalInfo());
                            });
                        });
                    });
        }));
    }

    @Test
    public void testUpdateObservation() {
        withRhy(rhy -> createObservationMetaF(false, RIISTAKAMERA).createSpecimensF(3).consumeBy(f -> {

            createObservationMetaF(true, NAKO).consumeBy(m -> onSavedAndAuthenticated(createUser(f.author), () -> {

                final ObservationDTO dto =
                        m.dtoBuilder(params).populateWith(f.observation).withAmountAndSpecimens(5).mutate().build();
                invokeUpdateObservation(dto);

                doUpdateAssertions(dto, f.author, f.author, 1, o -> {
                    assertNotNull(o.getRhy());
                    assertEquals(rhy.getOfficialCode(), o.getRhy().getOfficialCode());
                });
            }));
        }));
    }

    @Test
    public void testUpdateObservation_whenNothingChanged() {
        createObservationMetaF(NAKO).createSpecimensF(5).consumeBy((m, f) -> {

            onSavedAndAuthenticated(createUser(f.author), () -> {

                final ObservationDTO dto =
                        m.dtoBuilder(params).populateWith(f.observation).populateSpecimensWith(f.specimens).build();
                invokeUpdateObservation(dto);

                doUpdateAssertions(dto, f.author, f.author, 0);
            });
        });
    }

    @Test
    public void testUpdateObservation_whenUpdatingSpecimensOnly() {
        createObservationMetaF(NAKO).createSpecimensF(5).consumeBy((m, f) -> {

            onSavedAndAuthenticated(createUser(f.author), () -> {

                final ObservationDTO dto = m.dtoBuilder(params)
                        .populateWith(f.observation)
                        .populateSpecimensWith(f.specimens)
                        .mutateSpecimens()
                        .build();

                invokeUpdateObservation(dto);

                doUpdateAssertions(dto, f.author, f.author, 1);
            });
        });
    }

    @Test
    public void testUpdateObservation_forAmountChange_whenSpecimensPresent() {
        createObservationMetaF(NAKO).createSpecimensF(5).consumeBy((m, f) -> {

            onSavedAndAuthenticated(createUser(f.author), () -> {

                final ObservationDTO dto = m.dtoBuilder(params)
                        .populateWith(f.observation)
                        .populateSpecimensWith(f.specimens)
                        .withAmount(f.specimens.size() + 10)
                        .build();

                invokeUpdateObservation(dto);

                doUpdateAssertions(dto, f.author, f.author, 1);
            });
        });
    }

    @Test
    public void testUpdateObservation_forAmountChange_whenSpecimensAbsent() {
        withPerson(author -> createObservationMetaF(NAKO).consumeBy(m -> {

            final Observation observation = model().newObservation(author, m);

            onSavedAndAuthenticated(createUser(author), () -> {
                final ObservationDTO dto = m.dtoBuilder(params).populateWith(observation).withAmount(10).build();
                invokeUpdateObservation(dto);
                doUpdateAssertions(dto, author, author, 1);
            });
        }));
    }

    @Test
    public void testUpdateObservation_whenChangingToObservationTypeNotAcceptingAmount() {
        createObservationMetaF(NAKO).createSpecimensF(5).consumeBy(f -> {

            createObservationMetaF(AANI).withAmount(NO).consumeBy(m2 -> {

                onSavedAndAuthenticated(createUser(f.author), () -> {

                    final ObservationDTO dto =
                            m2.dtoBuilder(params).populateWith(f.observation).mutate().withAmount(null).build();
                    invokeUpdateObservation(dto);

                    doUpdateAssertions(dto, f.author, f.author, 1);
                });
            });
        });
    }

    @Test
    public void testUpdateObservation_forLargeCarnivore_withCarnivoreAuthority() {
        withRhy(rhy -> withPerson(author -> {

            model().newOccupation(rhy, author, PETOYHDYSHENKILO);

            createObservationMetaF(GameSpecies.OFFICIAL_CODE_WOLF, NAKO)
                    .withLargeCarnivoreFieldsAs(VOLUNTARY_CARNIVORE_AUTHORITY)
                    .createSpecimensF(author, 1)
                    .withCarnivoreAuthority(true)
                    .consumeBy((m, f) -> {

                        onSavedAndAuthenticated(createUser(author), () -> {

                            final ObservationDTO dto = m.dtoBuilder(params)
                                    .populateWith(f.observation)
                                    .populateSpecimensWith(f.specimens)
                                    .mutateLargeCarnivoreFieldsAsserting(notNullValue())
                                    .build();
                            invokeUpdateObservation(dto);

                            doUpdateAssertions(dto, author, author, 1);
                        });
                    });
        }));
    }

    @Test
    public void testUpdateObservation_forLargeCarnivore_carnivoreFieldsNotMutatedWhenAuthorityExpired() {
        withRhy(rhy -> withPerson(author -> {

            model().newOccupation(rhy, author, PETOYHDYSHENKILO, today().minusYears(1), today().minusWeeks(1));

            createObservationMetaF(GameSpecies.OFFICIAL_CODE_WOLF, NAKO)
                    .withLargeCarnivoreFieldsAs(VOLUNTARY_CARNIVORE_AUTHORITY)
                    .createSpecimensF(author, 1)
                    .withCarnivoreAuthority(true)
                    .consumeBy((m, f) -> {

                        onSavedAndAuthenticated(createUser(author), () -> {

                            final ObservationDTO inputDto = m.dtoBuilder(params)
                                    .populateWith(f.observation)
                                    .populateSpecimensWith(f.specimens)
                                    .withDescription("xyz" + nextPositiveInt())
                                    .mutateLargeCarnivoreFieldsAsserting(nullValue())
                                    .mutateSpecimens()
                                    .build();
                            invokeUpdateObservation(inputDto);

                            final ObservationDTO expectedValues = m.dtoBuilder(params)
                                    .populateWith(f.observation)
                                    .populateSpecimensWith(f.specimens)
                                    .withDescription(inputDto.getDescription())
                                    .build();

                            doUpdateAssertions(expectedValues, author, author, 1);
                        });
                    });
        }));
    }

    @Test
    public void testDeleteObservation() {
        withPerson(author -> createObservationMetaF(NAKO).createSpecimensF(author, 1).consumeBy((m, f) -> {

            model().newGameDiaryImage(f.observation);

            onSavedAndAuthenticated(createUser(author), () -> {
                final Long id = f.observation.getId();
                feature.deleteObservation(id);
                assertNull(observationRepo.findOne(id));
            });
        }));
    }

    @Test
    public void testDeleteObservation_whenAttachedToHuntingDay() {
        clubGroupUserFunctionsBuilder().withAdminAndModerator(true).build().forEach(userFn -> {
            withMooseHuntingGroupFixture(f -> {
                final SystemUser user = userFn.apply(f.club, f.group);
                final Person author = user.isModeratorOrAdmin() ? f.groupMember : user.getPerson();

                final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today());
                final Observation observation = model().newObservation(f.species, author, huntingDay);

                onSavedAndAuthenticated(user, () -> {
                    try {
                        feature.deleteObservation(observation.getId());
                        fail("Deletion of observation associated with a hunting day should fail");
                    } catch (final RuntimeException e) {
                        if (e instanceof AccessDeniedException) {
                            fail("Should not have failed because of insufficient permissions");
                        }
                    }
                    assertNotNull(observationRepo.findOne(observation.getId()));
                });
            });

            reset();
        });
    }
}

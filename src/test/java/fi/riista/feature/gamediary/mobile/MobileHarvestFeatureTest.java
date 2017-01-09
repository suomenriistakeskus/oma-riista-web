package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.fixture.MobileHarvestDTOBuilderFactory;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.Harvest.StateAcceptedToHarvestPermit;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.HarvestTestUtils.MooselikeFieldsPresence;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenDTO;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenRepository;
import fi.riista.feature.gis.MockGISQueryService;
import fi.riista.feature.gis.RhyNotResolvableByGeoLocationException;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFields;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.util.DateUtil;
import fi.riista.util.VersionedTestExecutionSupport;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion.LOWEST_VERSION_REQUIRING_AMOUNT;
import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion.LOWEST_VERSION_REQUIRING_LOCATION_SOURCE;
import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion.LOWEST_VERSION_SUPPORTING_PERMIT_STATE;
import static fi.riista.feature.gamediary.harvest.HarvestTestUtils.assertPresenceOfMooseFields;
import static fi.riista.util.Asserts.assertEmpty;
import static fi.riista.util.EqualityHelper.equalNotNull;
import static fi.riista.util.TestUtils.createList;
import static fi.riista.util.TestUtils.expectValidationException;
import static fi.riista.util.TestUtils.wrapExceptionExpectation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public abstract class MobileHarvestFeatureTest extends EmbeddedDatabaseTest
        implements MobileHarvestDTOBuilderFactory, VersionedTestExecutionSupport<HarvestSpecVersion> {

    @Resource
    protected HarvestRepository harvestRepo;

    @Resource
    protected HarvestSpecimenRepository specimenRepo;

    @Resource
    protected PersonRepository personRepo;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    protected abstract MobileGameDiaryFeature feature();

    @Override
    public List<HarvestSpecVersion> getTestExecutionVersions() {
        return new ArrayList<>(feature().getSupportedSpecVersions());
    }

    @Override
    public void onAfterVersionedTestExecution() {
        reset();
    }

    @Test
    public void testCreateHarvest_whenHarvestSpecVersionIsNull() {
        forEachVersion(expectValidationException(specVersion -> {

            invokeCreateHarvest(newDTOBuilderAndFixtureForCreate(specVersion).withSpecVersion(null).build());
        }));
    }

    @Test
    public void testCreateHarvest_whenAmountIsNull() {
        forEachVersionStartingFrom(LOWEST_VERSION_REQUIRING_AMOUNT, expectValidationException(specVersion -> {

            invokeCreateHarvest(newDTOBuilderAndFixtureForCreate(specVersion).withAmount(null).build());
        }));
    }

    @Test
    public void testCreateHarvest() {
        forEachVersion(specVersion -> {

            final GameSpecies species = model().newGameSpecies();

            onSavedAndAuthenticated(createUserWithPerson(), user -> {

                final MobileHarvestDTO inputDto = create(specVersion, species).build();
                final MobileHarvestDTO outputDto = invokeCreateHarvest(inputDto);

                doCreateAssertions(outputDto.getId(), inputDto, user.getPerson());
            });
        });
    }

    @Test
    public void testCreateHarvest_withSpecimens() {
        forEachVersion(specVersion -> {

            final GameSpecies species = model().newGameSpecies(true);

            onSavedAndAuthenticated(createUserWithPerson(), user -> {

                final MobileHarvestDTO inputDto = create(specVersion, species, 10).build();
                final MobileHarvestDTO outputDto = invokeCreateHarvest(inputDto);

                doCreateAssertions(outputDto.getId(), inputDto, user.getPerson());
            });
        });
    }

    @Test
    public void testCreateHarvest_whenSpecimensIsNull() {
        forEachVersion(specVersion -> {

            final GameSpecies species = model().newGameSpecies();

            onSavedAndAuthenticated(createUserWithPerson(), user -> {

                final MobileHarvestDTO inputDto = create(specVersion, species, 10).withSpecimens(null).build();
                final MobileHarvestDTO outputDto = invokeCreateHarvest(inputDto);

                doCreateAssertions(outputDto.getId(), inputDto, user.getPerson());
            });
        });
    }

    @Test
    public void testCreateHarvest_withMobileClientRefIdAssociatedWithExistingHarvest() {
        forEachVersion(specVersion -> withPerson(author -> {

            final GameSpecies species = model().newGameSpecies();
            final Harvest harvest = model().newMobileHarvest(species, author);
            final GameSpecies newSpecies = model().newGameSpecies();

            onSavedAndAuthenticated(createUser(author), () -> {

                invokeCreateHarvest(create(specVersion, harvest, newSpecies).mutate().build());

                final MobileHarvestDTO expectedValues = create(specVersion, harvest, species).build();

                doCreateAssertions(harvest.getId(), expectedValues, author);
            });
        }));
    }

    @Test
    public void testCreateHarvest_withMobileClientRefIdAssociatedWithExistingHarvestOfOtherPerson() {
        forEachVersion(specVersion -> {

            final GameSpecies species = model().newGameSpecies();
            final Harvest harvestOfOtherPerson = model().newMobileHarvest(model().newPerson());

            onSavedAndAuthenticated(createUserWithPerson(), user -> {

                final MobileHarvestDTO inputDto = create(specVersion, species)
                        .withMobileClientRefId(harvestOfOtherPerson.getMobileClientRefId())
                        .build();

                final MobileHarvestDTO outputDto = invokeCreateHarvest(inputDto);

                doCreateAssertions(outputDto.getId(), inputDto, user.getPerson());
            });
        });
    }

    @Test
    public void testCreateHarvest_whenHarvestReportRequiredByPermit() {
        forEachVersion(specVersion -> withRhy(rhy -> withPerson(person -> {

            final HarvestReportFields fields = model().newHarvestReportFields(model().newGameSpecies(), true);

            onSavedAndAuthenticated(createUser(person), () -> {

                final MobileHarvestDTO inputDto = create(specVersion, fields.getSpecies()).build();
                final MobileHarvestDTO outputDto = invokeCreateHarvest(inputDto);

                doCreateAssertions(outputDto.getId(), inputDto, person, h -> assertTrue(h.isHarvestReportRequired()));
            });
        })));
    }

    @Test
    public void testCreateHarvest_withPermit() {
        forEachVersionStartingFrom(LOWEST_VERSION_SUPPORTING_PERMIT_STATE, specVersion -> {

            final GameSpecies species = model().newGameSpecies();
            final HarvestPermit permit = model().newHarvestPermit();
            model().newHarvestPermitSpeciesAmount(permit, species);
            model().newHarvestReportFields(species, true);

            onSavedAndAuthenticated(createUserWithPerson(), user -> {

                final MobileHarvestDTO inputDto = create(specVersion, species)
                        .withPermitNumber(permit.getPermitNumber())
                        .withPermitType(permit.getPermitType())
                        .build();

                final MobileHarvestDTO outputDto = invokeCreateHarvest(inputDto);

                doCreateAssertions(outputDto.getId(), inputDto, user.getPerson(), harvest -> {
                    final HarvestPermit permit2 = harvest.getHarvestPermit();
                    assertNotNull(permit2);
                    assertEquals(permit.getPermitNumber(), permit2.getPermitNumber());
                    assertEquals(permit.getPermitType(), permit2.getPermitType());
                });
            });
        });
    }

    @Test
    public void testCreateHarvest_withPermitWhenRhyNotFound() {
        forEachVersionStartingFrom(LOWEST_VERSION_SUPPORTING_PERMIT_STATE, wrapExceptionExpectation(
                RhyNotResolvableByGeoLocationException.class, specVersion -> {

                    final GameSpecies species = model().newGameSpecies();
                    final HarvestPermit permit = model().newHarvestPermit();
                    model().newHarvestPermitSpeciesAmount(permit, species);
                    model().newHarvestReportFields(species, true);

                    persistAndAuthenticateWithNewUser(true);

                    // everything else should be fine, but RHY is not found for location
                    invokeCreateHarvest(create(specVersion, species)
                            .withPermitNumber(permit.getPermitNumber())
                            .withPermitType(permit.getPermitType())
                            .withGeoLocation(MockGISQueryService.RHY_GEOLOCATION_NOT_FOUND)
                            .build());
                }));
    }

    @Test
    public void testUpdateHarvest_whenHarvestSpecVersionIsNull() {
        forEachVersion(expectValidationException(specVersion -> {

            invokeUpdateHarvest(newDTOBuilderAndFixtureForUpdate(specVersion).withSpecVersion(null).build());
        }));
    }

    @Test
    public void testUpdateHarvest_whenGeoLocationSourceIsNull() {
        forEachVersionStartingFrom(LOWEST_VERSION_REQUIRING_LOCATION_SOURCE, expectValidationException(specVersion -> {

            final MobileHarvestDTO dto = newDTOBuilderAndFixtureForUpdate(specVersion).build();
            dto.getGeoLocation().setSource(null);

            invokeUpdateHarvest(dto);
        }));
    }

    @Test
    public void testUpdateHarvest_whenAmountIsNull() {
        forEachVersionStartingFrom(LOWEST_VERSION_REQUIRING_AMOUNT, expectValidationException(specVersion -> {

            invokeUpdateHarvest(newDTOBuilderAndFixtureForUpdate(specVersion).withAmount(null).build());
        }));
    }

    @Test
    public void testUpdateHarvest_whenNoChanges() {
        forEachVersion(specVersion -> withPerson(person -> {

            final Harvest harvest = model().newMobileHarvest(model().newGameSpecies(true), person);
            final List<HarvestSpecimen> specimens = createSpecimens(harvest, 5, specVersion);
            harvest.setAmount(specimens.size());

            onSavedAndAuthenticated(createUser(person), () -> {

                final MobileHarvestDTO inputDto = create(specVersion, harvest).populateSpecimensWith(specimens).build();
                invokeUpdateHarvest(inputDto);

                doUpdateAssertions(harvest.getId(), inputDto, person, 0);
            });
        }));
    }

    @Test
    public void testUpdateHarvest_whenHarvestReportNotDone() {
        forEachVersion(specVersion -> withPerson(person -> {

            final Harvest harvest = model().newMobileHarvest(model().newGameSpecies(true), person);
            final GameSpecies updatedSpecies = model().newGameSpecies(true);

            onSavedAndAuthenticated(createUser(person), () -> {

                final MobileHarvestDTO inputDto = create(specVersion, harvest, updatedSpecies)
                        .mutate()
                        .withSpecimens(5)
                        .withAmount(7) // Greater than number of specimens
                        .build();

                invokeUpdateHarvest(inputDto);

                doUpdateAssertions(harvest.getId(), inputDto, person, 1);
            });
        }));
    }

    @Test
    public void testUpdateHarvest_whenHarvestReportDone() {
        forEachVersion(specVersion -> withPerson(person -> {

            final GameSpecies species = model().newGameSpecies(true);
            final Harvest harvest = model().newMobileHarvest(species, person);
            final List<HarvestSpecimen> specimens = createSpecimens(harvest, 5, specVersion);

            model().newHarvestReport(harvest, HarvestReport.State.PROPOSED);

            final GameSpecies updatedSpecies = model().newGameSpecies();

            onSavedAndAuthenticated(createUser(person), () -> {

                final MobileHarvestDTO inputDto = create(specVersion, harvest, updatedSpecies)
                        .mutate()
                        .withSpecimens(Collections.emptyList())
                        .withAmount(1)
                        .build();

                invokeUpdateHarvest(inputDto);

                final MobileHarvestDTO expectedValues = create(specVersion, harvest, species)
                        .withHarvestReportDone(true)
                        .populateSpecimensWith(specimens)
                        .withDescription(inputDto.getDescription())
                        .build();

                doUpdateAssertions(harvest.getId(), expectedValues, person, 2);
            });
        }));
    }

    @Test
    public void testUpdateHarvest_mobileClientRefIdShouldNotBeUpdated() {
        forEachVersion(specVersion -> withPerson(person -> {

            final Harvest harvest = model().newMobileHarvest(model().newGameSpecies(true), person);
            final GameSpecies updatedSpecies = model().newGameSpecies(true);

            onSavedAndAuthenticated(createUser(person), () -> {

                final Long originalMobileClientRefId = harvest.getMobileClientRefId();
                final MobileHarvestDTO inputDto = create(specVersion, harvest, updatedSpecies)
                        .mutate()
                        .withMobileClientRefId(originalMobileClientRefId + 1)
                        .build();

                invokeUpdateHarvest(inputDto);

                inputDto.setMobileClientRefId(originalMobileClientRefId);

                doUpdateAssertions(harvest.getId(), inputDto, person, 1);
            });
        }));
    }

    @Test
    public void testUpdateHarvest_forSpecimensBeingPreserved() {
        forEachVersion(specVersion -> withPerson(person -> {

            final Harvest harvest = model().newMobileHarvest(model().newGameSpecies(true), person);
            final GameSpecies updatedSpecies = model().newGameSpecies(true);
            final List<HarvestSpecimen> specimens = createSpecimens(harvest, 5, specVersion);

            onSavedAndAuthenticated(createUser(person), () -> {

                final MobileHarvestDTO inputDto = create(specVersion, harvest, updatedSpecies)
                        .mutate()
                        .populateSpecimensWith(specimens)
                        .withAmount(specimens.size())
                        .build();

                invokeUpdateHarvest(inputDto);

                doUpdateAssertions(harvest.getId(), inputDto, person, 1);
            });
        }));
    }

    @Test
    public void testUpdateHarvest_whenUpdatingSpecimensOnly() {
        forEachVersion(specVersion -> withPerson(person -> {

            final Harvest harvest = model().newMobileHarvest(model().newGameSpecies(true), person);
            final List<HarvestSpecimen> specimens = createSpecimens(harvest, 5, specVersion);
            harvest.setAmount(specimens.size());

            onSavedAndAuthenticated(createUser(person), () -> {

                final MobileHarvestDTO inputDto = create(specVersion, harvest)
                        .populateSpecimensWith(specimens)
                        .mutateSpecimens()
                        .build();

                invokeUpdateHarvest(inputDto);

                doUpdateAssertions(harvest.getId(), inputDto, person, 1);
            });
        }));
    }

    @Test
    public void testUpdateHarvest_forAmountChange_whenSpecimensPresent() {
        forEachVersion(specVersion -> withPerson(person -> {

            final Harvest harvest = model().newMobileHarvest(model().newGameSpecies(true), person);
            final List<HarvestSpecimen> specimens = createSpecimens(harvest, 5, specVersion);

            onSavedAndAuthenticated(createUser(person), () -> {

                final MobileHarvestDTO inputDto = create(specVersion, harvest)
                        .populateSpecimensWith(specimens)
                        .withAmount(specimens.size() + 10)
                        .build();

                invokeUpdateHarvest(inputDto);

                doUpdateAssertions(harvest.getId(), inputDto, person, 1);
            });
        }));
    }

    @Test
    public void testUpdateHarvest_forAmountChange_whenSpecimensNotPresent() {
        forEachVersion(specVersion -> withPerson(person -> {

            final Harvest harvest = model().newMobileHarvest(model().newGameSpecies(true), person);

            onSavedAndAuthenticated(createUser(person), () -> {

                final MobileHarvestDTO inputDto = create(specVersion, harvest).withAmount(10).build();
                invokeUpdateHarvest(inputDto);

                doUpdateAssertions(harvest.getId(), inputDto, person, 1);
            });
        }));
    }

    @Test
    public void testUpdateHarvest_forAddingPermit() {
        forEachVersionStartingFrom(LOWEST_VERSION_SUPPORTING_PERMIT_STATE, specVersion -> withPerson(person -> {

            final GameSpecies species = model().newGameSpecies();

            final HarvestPermit originalPermit = model().newHarvestPermit();
            model().newHarvestPermitSpeciesAmount(originalPermit, species);
            model().newHarvestReportFields(species, true);

            final Harvest harvest = model().newMobileHarvest(species, person);

            onSavedAndAuthenticated(createUser(person), () -> {

                final MobileHarvestDTO inputDto = create(specVersion, harvest)
                        .withPermitNumber(originalPermit.getPermitNumber())
                        .build();

                invokeUpdateHarvest(inputDto);

                doUpdateAssertions(harvest.getId(), inputDto, person, 1, h -> {
                    final HarvestPermit permit = h.getHarvestPermit();
                    assertNotNull(permit);
                    assertEquals(originalPermit.getPermitNumber(), permit.getPermitNumber());
                    assertEquals(originalPermit.getPermitType(), permit.getPermitType());
                });
            });
        }));
    }

    @Test
    public void testUpdateHarvest_withPermitWhenRhyNotFound() {
        forEachVersionStartingFrom(LOWEST_VERSION_SUPPORTING_PERMIT_STATE, wrapExceptionExpectation(
                RhyNotResolvableByGeoLocationException.class, specVersion -> withPerson(person -> {

                    final GameSpecies species = model().newGameSpecies();

                    final HarvestPermit permit = model().newHarvestPermit();
                    model().newHarvestPermitSpeciesAmount(permit, species);
                    model().newHarvestReportFields(species, true);

                    final Harvest harvest = model().newMobileHarvest(species, person);

                    // Everything else should be fine, but rhy is not found for location
                    onSavedAndAuthenticated(createUser(person), () -> invokeUpdateHarvest(
                            create(specVersion, harvest)
                                    .withPermitNumber(permit.getPermitNumber())
                                    .withGeoLocation(MockGISQueryService.RHY_GEOLOCATION_NOT_FOUND)
                                    .build()));
                })));
    }

    @Test
    public void testUpdateHarvest_forRemovingPermit() {
        forEachVersionStartingFrom(LOWEST_VERSION_SUPPORTING_PERMIT_STATE, specVersion -> withPerson(person -> {

            final GameSpecies species = model().newGameSpecies();

            final HarvestPermit permit = model().newHarvestPermit();
            model().newHarvestPermitSpeciesAmount(permit, species);

            final Harvest harvest = model().newMobileHarvest(species, person);
            harvest.setHarvestPermit(permit);
            harvest.setRhy(permit.getRhy());

            onSavedAndAuthenticated(createUser(person), () -> {

                final MobileHarvestDTO inputDto = create(specVersion, harvest)
                        .withPointOfTime(Optional
                                .ofNullable(DateUtil.toLocalDateTimeNullSafe(harvest.getPointOfTime()))
                                .map(ldt -> ldt.plusMinutes(1))
                                .orElse(null))
                        .withPermitNumber(null)
                        .build();

                invokeUpdateHarvest(inputDto);

                doUpdateAssertions(harvest.getId(), inputDto, person, 1, h -> assertNull(h.getHarvestPermit()));
            });
        }));
    }

    // Test that harvest is not mutated (except for description/images) when group is created
    // within moose data card import.
    @Test
    public void testUpdateHarvest_whenGroupOriginatingFromMooseDataCard() {
        forEachVersion(specVersion -> withMooseHuntingGroupFixture(f -> {
            f.group.setFromMooseDataCard(true);

            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, DateUtil.today());
            final Harvest harvest = model().newMobileHarvest(f.species, f.groupMember, huntingDay);

            final GameSpecies newSpecies = model().newGameSpecies();

            onSavedAndAuthenticated(createUser(f.groupMember), () -> {

                final MobileHarvestDTO inputDto = create(specVersion, harvest, newSpecies).mutate().build();
                invokeUpdateHarvest(inputDto);

                final MobileHarvestDTO expectedValues = create(specVersion, harvest, f.species)
                        .withDescription(inputDto.getDescription())
                        .build();

                doUpdateAssertions(harvest.getId(), expectedValues, f.groupMember, 1);
            });
        }));
    }

    @Test
    public void testUpdateHarvest_whenHarvestChangedToRequired() {
        forEachVersion(specVersion -> withRhy(rhy -> withPerson(person -> {

            final HarvestReportFields fields = model().newHarvestReportFields(model().newGameSpecies(), true);
            final Harvest harvest = model().newMobileHarvest(person);

            onSavedAndAuthenticated(createUser(person), () -> {
                // Check invariant
                assertFalse(harvest.isHarvestReportRequired());

                final MobileHarvestDTO inputDto = create(specVersion, harvest, fields.getSpecies()).build();
                invokeUpdateHarvest(inputDto);

                doUpdateAssertions(harvest.getId(), inputDto, person, 1, h -> assertTrue(h.isHarvestReportRequired()));
            });
        })));
    }

    @Test
    public void testUpdateHarvest_withHarvestAcceptedToPermit() {
        forEachVersionStartingFrom(LOWEST_VERSION_SUPPORTING_PERMIT_STATE, specVersion -> {

            doTestUpdateHarvestWithPermit(specVersion, false, false, StateAcceptedToHarvestPermit.ACCEPTED);
        });
    }

    @Test
    public void testUpdateHarvest_withHarvestAcceptedToPermit_asContactPerson() {
        forEachVersionStartingFrom(LOWEST_VERSION_SUPPORTING_PERMIT_STATE, specVersion -> {

            doTestUpdateHarvestWithPermit(specVersion, true, true, StateAcceptedToHarvestPermit.ACCEPTED);
        });
    }

    @Test
    public void testUpdateHarvest_withHarvestProposedToPermit() {
        forEachVersionStartingFrom(LOWEST_VERSION_SUPPORTING_PERMIT_STATE, specVersion -> {

            doTestUpdateHarvestWithPermit(specVersion, false, true, StateAcceptedToHarvestPermit.PROPOSED);
        });
    }

    @Test
    public void testUpdateHarvest_withHarvestRejectedToPermit() {
        forEachVersionStartingFrom(LOWEST_VERSION_SUPPORTING_PERMIT_STATE, specVersion -> {

            doTestUpdateHarvestWithPermit(specVersion, false, true, StateAcceptedToHarvestPermit.REJECTED);
        });
    }

    private void doTestUpdateHarvestWithPermit(
            final HarvestSpecVersion specVersion,
            final boolean setAsOriginalContactPerson,
            final boolean businessFieldsUpdateable,
            final StateAcceptedToHarvestPermit stateAcceptedToHarvestPermit) {

        withHarvestHavingPermitState(stateAcceptedToHarvestPermit, harvest -> {

            if (setAsOriginalContactPerson) {
                harvest.getHarvestPermit().setOriginalContactPerson(harvest.getAuthor());
            }

            final String permitNumber = harvest.getHarvestPermit().getPermitNumber();

            onSavedAndAuthenticated(createUser(harvest.getAuthor()), () -> {

                // Do not change species because test fixture has been setup for original species.
                final MobileHarvestDTO inputDto = create(specVersion, harvest)
                        .mutate()
                        .withPermitNumber(permitNumber)
                        .build();

                invokeUpdateHarvest(inputDto);

                final MobileHarvestDTO expectedValues = businessFieldsUpdateable
                        ? inputDto
                        : create(specVersion, harvest, harvest.getSpecies())
                                .withDescription(inputDto.getDescription())
                                .build();

                final StateAcceptedToHarvestPermit expectedStateAfterUpdate =
                        stateAcceptedToHarvestPermit == StateAcceptedToHarvestPermit.REJECTED
                            ? StateAcceptedToHarvestPermit.PROPOSED
                            : stateAcceptedToHarvestPermit;

                doUpdateAssertions(harvest.getId(), expectedValues, harvest.getAuthor(), 1, h -> {
                    assertEquals(permitNumber, h.getHarvestPermit().getPermitNumber());
                    assertEquals(expectedStateAfterUpdate, h.getStateAcceptedToHarvestPermit());
                });
            });
        });
    }

    private void withHarvestHavingPermitState(
            final StateAcceptedToHarvestPermit state, final Consumer<Harvest> consumer) {

        withRhy(rhy -> withPerson(author -> {
            final GameSpecies species = model().newGameSpecies(true);
            final HarvestPermit permit = model().newHarvestPermit(rhy, true);

            final Harvest harvest = model().newMobileHarvest(species, author);
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(state);
            harvest.setRhy(rhy);

            model().newHarvestPermitSpeciesAmount(permit, species);
            model().newHarvestReportFields(species, true);

            consumer.accept(harvest);
        }));
    }

    @Test
    public void testUpdateHarvest_forIntegrationWithHarvestSpecimenOps() {
        forEachVersion(specVersion -> withPerson(person -> {

            final Harvest harvest = model().newMobileHarvest(model().newGameSpeciesMoose(), person);

            onSavedAndAuthenticated(createUser(person), () -> {

                final MobileHarvestDTO inputDto = create(specVersion, harvest)
                        .mutate()
                        .withSpecimens(1)
                        .build();

                invokeUpdateHarvest(inputDto);

                doUpdateAssertions(harvest.getId(), inputDto, person, 1, h -> {
                    final MooselikeFieldsPresence p = inputDto.specimenOps().supportsExtendedMooseFields()
                            ? MooselikeFieldsPresence.ALL
                            : MooselikeFieldsPresence.ESTIMATED_WEIGHT;

                    assertPresenceOfMooseFields(specimenRepo.findByHarvest(h), p);
                });
            });
        }));
    }

    @Test
    public void testDeleteHarvest_whenHarvestReportNotDone() {
        withPerson(author -> {
            final Harvest harvest = model().newMobileHarvest(author);
            model().newHarvestSpecimen(harvest);

            // HarvestReport with DELETED state means harvest report is not done.
            model().newHarvestReport(harvest, HarvestReport.State.DELETED);

            onSavedAndAuthenticated(createUser(author), () -> {
                feature().deleteHarvest(harvest.getId());
                assertNull(harvestRepo.findOne(harvest.getId()));
            });
        });
    }

    @Test
    public void testDeleteHarvest_whenHarvestReportDone() {
        withPerson(author -> {
            final Harvest harvest = model().newMobileHarvest(author);
            model().newHarvestSpecimen(harvest);
            model().newHarvestReport(harvest, HarvestReport.State.PROPOSED);

            thrown.expect(RuntimeException.class);
            thrown.expectMessage("Cannot delete harvest with an associated harvest report.");

            onSavedAndAuthenticated(createUser(author), () -> {
                feature().deleteHarvest(harvest.getId());
                assertNotNull(harvestRepo.findOne(harvest.getId()));
            });
        });
    }

    @Test
    public void testDeleteHarvest_whenAttachedToHuntingDay() {
        withMooseHuntingGroupFixture(f -> {
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, DateUtil.today());
            final Harvest harvest = model().newMobileHarvest(f.species, f.groupMember, huntingDay);

            onSavedAndAuthenticated(createUser(f.groupMember), () -> {
                try {
                    feature().deleteHarvest(harvest.getId());
                    fail("Deletion of harvest associated with a hunting day should fail");
                } catch (final RuntimeException e) {
                    // Expected
                }
                assertNotNull(harvestRepo.findOne(harvest.getId()));
            });
        });
    }

    @Test
    public void testDeleteHarvest_withHarvestAcceptedToPermit_asContactPerson() {
        withHarvestHavingPermitState(StateAcceptedToHarvestPermit.ACCEPTED, harvest -> {
            harvest.getHarvestPermit().setOriginalContactPerson(harvest.getAuthor());
            doTestDeleteHarvest(true, harvest);
        });
    }

    @Test
    public void testDeleteHarvest_withHarvestAcceptedToPermit() {
        withHarvestHavingPermitState(StateAcceptedToHarvestPermit.ACCEPTED, harvest -> {
            doTestDeleteHarvest(false, harvest);
        });
    }

    @Test
    public void testDeleteHarvest_withHarvestProposedToPermit() {
        withHarvestHavingPermitState(StateAcceptedToHarvestPermit.PROPOSED, harvest -> {
            doTestDeleteHarvest(true, harvest);
        });
    }

    @Test
    public void testDeleteHarvest_withHarvestRejectedToPermit() {
        withHarvestHavingPermitState(StateAcceptedToHarvestPermit.REJECTED, harvest -> {
            doTestDeleteHarvest(true, harvest);
        });
    }

    private void doTestDeleteHarvest(final boolean shouldBeDeleted, final Harvest harvest) {
        doTestDeleteHarvest(shouldBeDeleted, harvest, createUser(harvest.getAuthor()));
    }

    private void doTestDeleteHarvest(final boolean shouldBeDeleted, final Harvest harvest, final SystemUser user) {
        onSavedAndAuthenticated(user, () -> {
            if (!shouldBeDeleted) {
                thrown.expect(RuntimeException.class);
                thrown.expectMessage("Cannot delete harvest which is accepted to permit");
            }
            feature().deleteHarvest(harvest.getId());
        });
    }

    protected MobileHarvestDTOBuilderFactory.Builder newDTOBuilderAndFixtureForCreate(
            final HarvestSpecVersion specVersion) {

        final GameSpecies species = model().newGameSpecies();
        persistAndAuthenticateWithNewUser(true);
        return create(specVersion, species);
    }

    protected MobileHarvestDTOBuilderFactory.Builder newDTOBuilderAndFixtureForUpdate(
            final HarvestSpecVersion specVersion) {

        final SystemUser user = createUserWithPerson();
        final Harvest harvest = model().newMobileHarvest(user.getPerson());
        final GameSpecies newSpecies = model().newGameSpecies(true);

        persistInNewTransaction();
        authenticate(user);

        return create(specVersion, harvest, newSpecies);
    }

    protected List<HarvestSpecimen> createSpecimens(
            final Harvest harvest, final int numSpecimens, final HarvestSpecVersion specVersion) {

        return createList(numSpecimens, () -> model().newHarvestSpecimen(harvest, specVersion));
    }

    protected void doCreateAssertions(
            final long harvestId, final MobileHarvestDTO expectedValues, final Person expectedAuthor) {

        doCreateAssertions(harvestId, expectedValues, expectedAuthor, h -> {});
    }

    protected void doCreateAssertions(
            final long harvestId,
            final MobileHarvestDTO expectedValues,
            final Person expectedAuthor,
            final Consumer<Harvest> additionalAssertions) {

        runInTransaction(() -> {
            final Harvest harvest = harvestRepo.findOne(harvestId);
            assertCommonExpectations(harvest, expectedValues);
            assertVersion(harvest, 0);

            final Person author = assertValidAuthor(harvest, expectedAuthor.getId());
            assertTrue(harvest.isActor(author));

            assertNull(harvest.getHarvestReport());
            assertFalse(harvest.isHarvestReportDone());

            additionalAssertions.accept(harvest);
        });
    }

    protected void doUpdateAssertions(
            final long harvestId,
            final MobileHarvestDTO expectedValues,
            final Person expectedAuthor,
            final int expectedRevision) {

        doUpdateAssertions(harvestId, expectedValues, expectedAuthor, expectedRevision, h -> {});
    }

    protected void doUpdateAssertions(
            final long harvestId,
            final MobileHarvestDTO expectedValues,
            final Person expectedAuthor,
            final int expectedRevision,
            final Consumer<Harvest> additionalAssertions) {

        runInTransaction(() -> {
            final Harvest harvest = harvestRepo.findOne(harvestId);
            assertCommonExpectations(harvest, expectedValues);
            assertVersion(harvest, expectedRevision);

            assertValidAuthor(harvest, expectedAuthor.getId());
            assertEquals(expectedValues.isHarvestReportDone(), harvest.isHarvestReportDone());

            additionalAssertions.accept(harvest);
        });
    }

    protected void assertCommonExpectations(final Harvest harvest, final MobileHarvestDTO expectedValues) {
        assertNotNull(harvest);

        assertEquals(GeoLocation.Source.GPS_DEVICE, harvest.getGeoLocation().getSource());
        assertEquals(Boolean.TRUE, harvest.getFromMobile());

        assertTrue(Objects.equals(expectedValues.getMobileClientRefId(), harvest.getMobileClientRefId()));
        assertEquals(expectedValues.getGameSpeciesCode(), harvest.getSpecies().getOfficialCode());
        assertEquals(DateUtil.toDateNullSafe(expectedValues.getPointOfTime()), harvest.getPointOfTime());
        assertEquals(expectedValues.getGeoLocation(), harvest.getGeoLocation());
        assertEquals(expectedValues.getDescription(), harvest.getDescription());

        final int expectedAmount = Optional.ofNullable(expectedValues.getAmount()).orElse(1);
        assertEquals(expectedAmount, harvest.getAmount());

        assertSpecimens(
                specimenRepo.findByHarvest(harvest),
                expectedValues.getSpecimens(),
                expectedValues.specimenOps()::equalContent);

        assertEmpty(harvest.getImages());
    }

    protected Person assertValidAuthor(final Harvest harvest, final long expectedAuthorId) {
        final Person author = personRepo.findOne(expectedAuthorId);
        assertNotNull(author);
        assertTrue(harvest.isAuthor(author));
        return author;
    }

    protected void assertSpecimens(
            final List<HarvestSpecimen> specimens,
            final List<HarvestSpecimenDTO> expectedSpecimens,
            final BiFunction<HarvestSpecimen, HarvestSpecimenDTO, Boolean> compareFn) {

        final int numSpecimenDTOs = Optional.ofNullable(expectedSpecimens).map(List::size).orElse(0);
        assertEquals(numSpecimenDTOs, specimens.size());

        if (numSpecimenDTOs > 0) {
            assertTrue(equalNotNull(specimens, expectedSpecimens, compareFn));
        }
    }

    protected MobileHarvestDTO invokeCreateHarvest(final MobileHarvestDTO input) {
        return withVersionChecked(feature().createHarvest(input));
    }

    protected MobileHarvestDTO invokeUpdateHarvest(final MobileHarvestDTO input) {
        return withVersionChecked(feature().updateHarvest(input));
    }

    private MobileHarvestDTO withVersionChecked(final MobileHarvestDTO dto) {
        return checkDtoVersionAgainstEntity(dto, Harvest.class);
    }

}

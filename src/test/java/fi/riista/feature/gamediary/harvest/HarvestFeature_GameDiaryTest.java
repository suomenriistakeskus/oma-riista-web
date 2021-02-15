package fi.riista.feature.gamediary.harvest;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.HarvestChangeHistory;
import fi.riista.feature.gamediary.HarvestChangeHistoryRepository;
import fi.riista.feature.gamediary.fixture.HarvestDTOBuilderFactory;
import fi.riista.feature.gamediary.harvest.Harvest.StateAcceptedToHarvestPermit;
import fi.riista.feature.gamediary.harvest.mutation.exception.HarvestReportingTypeChangeForbiddenException;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenAssertionBuilder;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenValidationException;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gis.MockGISQueryService;
import fi.riista.feature.gis.RhyNotResolvableByGeoLocationException;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitNotFoundException;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountNotFound;
import fi.riista.feature.harvestpermit.endofhunting.EndOfHuntingReportExistsException;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.harvestpermit.season.HarvestSeason;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_GREY_SEAL;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_MALE;
import static fi.riista.feature.gamediary.harvest.Harvest.StateAcceptedToHarvestPermit.ACCEPTED;
import static fi.riista.feature.gamediary.harvest.Harvest.StateAcceptedToHarvestPermit.PROPOSED;
import static fi.riista.feature.gamediary.harvest.Harvest.StateAcceptedToHarvestPermit.REJECTED;
import static fi.riista.util.DateUtil.huntingYearBeginDate;
import static fi.riista.util.DateUtil.today;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThrows;

public class HarvestFeature_GameDiaryTest extends HarvestFeatureTestBase implements HarvestDTOBuilderFactory {

    @Resource
    private HarvestChangeHistoryRepository changeEventHistoryRepo;

    @Test
    public void testCreateHarvest() {
        withRhy(rhy -> withPerson(author -> {

            final GameSpecies species = model().newGameSpecies(true);

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO inputDto = create(species, 5).withAuthorInfo(author).build();
                final HarvestDTO outputDto = invokeCreateHarvest(inputDto);

                runInTransaction(() -> {
                    final Harvest harvest = assertHarvestCreated(outputDto.getId());

                    assertThat(harvest.getFromMobile(), is(Boolean.FALSE));
                    assertThat(harvest.getMobileClientRefId(), is(nullValue()));

                    final GeoLocation geoLocation = harvest.getGeoLocation();
                    assertThat(geoLocation, is(inputDto.getGeoLocation()));
                    assertThat(geoLocation.getSource(), is(GeoLocation.Source.MANUAL));

                    assertThat(harvest.getPointOfTime(), is(DateUtil.toDateTimeNullSafe(inputDto.getPointOfTime())));

                    assertThat(harvest.getSpecies().getOfficialCode(), is(species.getOfficialCode()));
                    assertThat(harvest.getTaigaBeanGoose(), is(nullValue()));
                    assertThat(harvest.getSubSpeciesCode(), is(nullValue()));

                    assertThat(harvest.getDeerHuntingType(), is(nullValue()));
                    assertThat(harvest.getDeerHuntingOtherTypeDescription(), is(nullValue()));

                    assertThat(harvest.getAmount(), is(5));

                    final Long authorId = F.getId(author);
                    assertAuthorAndActor(harvest, authorId, authorId);

                    final String description = harvest.getDescription();
                    assertThat(description, is(notNullValue()));
                    assertThat(description, is(inputDto.getDescription()));

                    assertThat(harvest.getRhy(), is(rhy));

                    assertEmptyHarvestReportState(harvest);
                    assertEmptyHarvestPermitState(harvest);

                    assertThat(harvest.getHarvestSeason(), is(nullValue()));
                    assertThat(harvest.getHarvestQuota(), is(nullValue()));

                    assertThat(harvest.getHuntingDayOfGroup(), is(nullValue()));
                    assertThat(harvest.getApproverToHuntingDay(), is(nullValue()));
                    assertThat(harvest.getPointOfTimeApprovedToHuntingDay(), is(nullValue()));
                    assertThat(harvest.isModeratorOverride(), is(false));

                    assertThat(harvest.getHuntingAreaType(), is(nullValue()));
                    assertThat(harvest.getHuntingAreaSize(), is(nullValue()));
                    assertThat(harvest.getHuntingParty(), is(nullValue()));

                    assertThat(harvest.getHuntingMethod(), is(nullValue()));
                    assertThat(harvest.getPermittedMethod(), is(nullValue()));
                    assertThat(harvest.getReportedWithPhoneCall(), is(nullValue()));

                    assertThat(harvest.getFeedingPlace(), is(nullValue()));
                    assertThat(harvest.getLukeStatus(), is(nullValue()));

                    final List<HarvestSpecimen> specimens = findSpecimens(harvest);
                    assertThat(specimens, hasSize(5));
                    assertSpecimens(specimens, inputDto.getSpecimens(), inputDto.specimenOps()::equalContent);
                });
            });
        }));
    }

    @Test
    public void testCreateHarvest_asModerator() {
        withPerson(author -> {

            final GameSpecies species = model().newGameSpecies(true);

            onSavedAndAuthenticated(createNewModerator(), () -> {

                final HarvestDTO dto = create(species)
                        .withAuthorInfo(author)
                        .withActorInfo(author)
                        .build();

                assertCreateThrows(HarvestReportingTypeChangeForbiddenException.class, dto);
            });
        });
    }

    @Test
    public void testCreateHarvest_whenActorIsHunter() {
        withPerson(author -> withPerson(actor -> {

            actor.setHunterNumber(hunterNumber()); // set explicitly

            final GameSpecies species = model().newGameSpecies(true);

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO inputDto = create(species, 5)
                        .withAuthorInfo(author)
                        .withActorInfo(actor)
                        .build();

                final HarvestDTO outputDto = invokeCreateHarvest(inputDto);

                runInTransaction(() -> {
                    final Harvest harvest = assertHarvestCreated(outputDto.getId());

                    assertAuthorAndActor(harvest, F.getId(author), F.getId(actor));
                });
            });
        }));
    }

    @Test
    public void testCreateHarvest_withinSeason() {
        withRhy(rhy -> withPerson(author -> {

            final GameSpecies species = model().newGameSpecies(OFFICIAL_CODE_BEAR);

            final LocalDate seasonBegin = today();
            final LocalDate seasonEnd = seasonBegin.plusDays(1);
            final HarvestSeason season = model().newHarvestSeason(species, seasonBegin, seasonEnd, seasonEnd);

            onSavedAndAuthenticated(createUser(author), user -> {

                final HarvestDTO inputDto = create(species, 1)
                        .withPointOfTime(seasonBegin.toDateTimeAtStartOfDay().toLocalDateTime())
                        .withAuthorInfo(author)
                        .build();

                final HarvestDTO outputDto = invokeCreateHarvest(inputDto);

                runInTransaction(() -> {
                    final Harvest harvest = assertHarvestCreated(outputDto.getId());

                    assertThat(harvest.getHarvestSeason(), is(season));

                    assertHarvestReportState(harvest, HarvestReportState.SENT_FOR_APPROVAL, author);
                    assertChangeHistoryEventExists(harvest, user, HarvestReportState.SENT_FOR_APPROVAL);

                    assertEmptyHarvestPermitState(harvest);

                    assertThat(harvest.getHarvestQuota(), is(nullValue()));
                });
            });
        }));
    }

    @Test
    public void testCreateHarvest_whenSpecimenHasRequiredFields_noSpecimen() {
        withRhy(rhy -> withPerson(author -> {

            final GameSpecies species = model().newGameSpecies(OFFICIAL_CODE_BEAR);

            final LocalDate seasonBegin = today();
            final LocalDate seasonEnd = seasonBegin.plusDays(1);
            model().newHarvestSeason(species, seasonBegin, seasonEnd, seasonEnd);

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO dto = create(species)
                        .withPointOfTime(seasonBegin.toDateTimeAtStartOfDay().toLocalDateTime())
                        .withAuthorInfo(author)
                        .build();

                assertCreateThrows(HarvestSpecimenValidationException.class, dto);
            });
        }));
    }

    @Test
    public void testCreateHarvest_forPermit() {
        withRhy(rhy -> withPerson(author -> {

            final GameSpecies species = model().newGameSpecies();

            final HarvestPermit permit = model().newHarvestPermit(rhy);
            model().newHarvestPermitSpeciesAmount(permit, species);

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO inputDto = create(species, 1)
                        .withPermitNumber(permit.getPermitNumber())
                        .withAuthorInfo(author)
                        .build();

                final HarvestDTO outputDto = invokeCreateHarvest(inputDto);

                runInTransaction(() -> {
                    final Harvest harvest = assertHarvestCreated(outputDto.getId());

                    assertHarvestPermitState(harvest, PROPOSED, permit);

                    assertEmptyHarvestReportState(harvest);
                });
            });
        }));
    }

    @Test
    public void testCreateHarvest_forPermit_asContactPerson() {
        withPerson(originalContactPerson -> withPerson(anotherContactPerson -> {

            final GameSpecies species = model().newGameSpecies();

            final HarvestPermit permit = model().newHarvestPermit(anotherContactPerson);
            permit.setOriginalContactPerson(originalContactPerson);
            model().newHarvestPermitSpeciesAmount(permit, species);

            onSavedAndAuthenticated(createUser(anotherContactPerson), user -> {

                final HarvestDTO inputDto = create(species, 1)
                        .withPermitNumber(permit.getPermitNumber())
                        .withAuthorInfo(anotherContactPerson)
                        .build();

                final HarvestDTO outputDto = invokeCreateHarvest(inputDto);

                runInTransaction(() -> {
                    final Harvest harvest = assertHarvestCreated(outputDto.getId());

                    assertHarvestPermitState(harvest, ACCEPTED, permit);
                    assertHarvestReportState(harvest, HarvestReportState.SENT_FOR_APPROVAL, anotherContactPerson);

                    assertChangeHistoryEventExists(harvest, user, HarvestReportState.SENT_FOR_APPROVAL);
                });
            });
        }));
    }

    @Test
    public void testCreateHarvest_forPermit_asModerator() {
        withPerson(originalContactPerson -> withPerson(anotherContact -> withPerson(author -> withPerson(actor -> {

            final GameSpecies species = model().newGameSpecies(true);

            final HarvestPermit permit = model().newHarvestPermit(anotherContact);
            permit.setOriginalContactPerson(originalContactPerson);
            model().newHarvestPermitSpeciesAmount(permit, species);

            onSavedAndAuthenticated(createNewModerator(), moderator -> {

                final HarvestDTO inputDto = create(species, 5)
                        .withPermitNumber(permit.getPermitNumber())
                        .withAuthorInfo(author)
                        .withActorInfo(actor)
                        .withModeratorChangeReason("reason")
                        .build();

                final HarvestDTO outputDto = invokeCreateHarvest(inputDto);

                runInTransaction(() -> {
                    final Harvest harvest = assertHarvestCreated(outputDto.getId());

                    assertThat(harvest.getDescription(), is(nullValue()));

                    assertAuthorAndActor(harvest, F.getId(author), F.getId(actor));

                    assertHarvestPermitState(harvest, ACCEPTED, permit);
                    assertHarvestReportState(harvest, HarvestReportState.SENT_FOR_APPROVAL, originalContactPerson);

                    final HarvestChangeHistory historyEvent =
                            assertChangeHistoryEventExists(harvest, moderator, HarvestReportState.SENT_FOR_APPROVAL);
                    assertThat(historyEvent.getReasonForChange(), is("reason"));
                });
            });
        }))));
    }

    @Test
    public void testCreateGreySealForPermit_asModerator() {
        withPerson(originalContactPerson -> withPerson(anotherContact -> withPerson(author -> withPerson(actor -> {

            final GameSpecies species = model().newGameSpecies(OFFICIAL_CODE_GREY_SEAL);

            final HarvestPermit permit = model().newHarvestPermit(anotherContact);
            permit.setOriginalContactPerson(originalContactPerson);
            model().newHarvestPermitSpeciesAmount(permit, species);

            onSavedAndAuthenticated(createNewModerator(), moderator -> {

                final HarvestDTO inputDto = create(species, 1)
                        .withPermitNumber(permit.getPermitNumber())
                        .withAuthorInfo(author)
                        .withActorInfo(actor)
                        .withModeratorChangeReason("reason")
                        .withHuntingMethod(HuntingMethod.SHOT)
                        .build();

                final HarvestDTO outputDto = invokeCreateHarvest(inputDto);

                runInTransaction(() -> {
                    final Harvest harvest = assertHarvestCreated(outputDto.getId());

                    assertThat(harvest.getHuntingMethod(), is(HuntingMethod.SHOT));
                    assertThat(harvest.getDescription(), is(nullValue()));

                    assertAuthorAndActor(harvest, F.getId(author), F.getId(actor));

                    assertHarvestPermitState(harvest, ACCEPTED, permit);
                    assertHarvestReportState(harvest, HarvestReportState.SENT_FOR_APPROVAL, originalContactPerson);

                    final HarvestChangeHistory historyEvent =
                            assertChangeHistoryEventExists(harvest, moderator, HarvestReportState.SENT_FOR_APPROVAL);
                    assertThat(historyEvent.getReasonForChange(), is("reason"));
                });
            });
        }))));
    }

    @Test
    public void testCreateHarvest_whenPermitNotFound() {
        withRhy(rhy -> withPerson(author -> {

            final GameSpecies species = model().newGameSpecies();

            // Create some permit for test coverage.
            model().newHarvestPermitSpeciesAmount(model().newHarvestPermit(rhy), species);

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO dto = create(species, 1)
                        .withAuthorInfo(author)
                        .withPermitNumber(permitNumber()) // Permit does not exist for this fabricated number.
                        .build();

                assertCreateThrows(HarvestPermitNotFoundException.class, dto);
            });
        }));
    }

    @Test
    public void testCreateHarvest_ignoreMoosePermit() {
        withRhy(rhy -> withPerson(author -> {

            final GameSpecies moose = model().newGameSpeciesMoose();

            final HarvestPermit permit = model().newMooselikePermit(rhy);
            model().newHarvestPermitSpeciesAmount(permit, moose);

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO inputDto = create(moose, 1)
                        .withPermitNumber(permit.getPermitNumber())
                        .withAuthorInfo(author)
                        .build();

                final HarvestDTO outputDto = invokeCreateHarvest(inputDto);

                runInTransaction(() -> {
                    final Harvest harvest = assertHarvestCreated(outputDto.getId());

                    assertEmptyHarvestReportState(harvest);
                    assertEmptyHarvestPermitState(harvest);

                    assertAuthorAndActor(harvest, F.getId(author), F.getId(author));
                });
            });
        }));
    }

    @Test
    public void testCreateHarvest_whenPermitEndOfHuntingReportDone() {
        withRhy(rhy -> withPerson(permitContactPerson -> {

            final GameSpecies species = model().newGameSpecies();

            final HarvestPermit permit = model().newHarvestPermit(rhy);
            permit.setOriginalContactPerson(permitContactPerson);
            permit.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
            permit.setHarvestReportAuthor(permitContactPerson);
            permit.setHarvestReportDate(DateUtil.now());
            permit.setHarvestReportModeratorOverride(false);

            model().newHarvestPermitSpeciesAmount(permit, species);

            onSavedAndAuthenticated(createUser(permitContactPerson), () -> {

                final HarvestDTO dto = create(species, 1)
                        .withPermitNumber(permit.getPermitNumber())
                        .withAuthorInfo(permitContactPerson)
                        .build();

                assertCreateThrows(EndOfHuntingReportExistsException.class, dto);
            });
        }));
    }

    @Test
    public void testCreateHarvestForPermit_whenSpeciesIsNotValid() {
        final GameSpecies species = model().newGameSpecies(true);

        final HarvestPermit permit = model().newHarvestPermit(true);
        final HarvestPermitSpeciesAmount amount = model().newHarvestPermitSpeciesAmount(permit, species);

        final SystemUser user = persistAndAuthenticateWithNewUser(true);

        final HarvestDTO dto = create(species, 1)
                .withPointOfTime(amount.getBeginDate().minusDays(1).toLocalDateTime(LocalTime.MIDNIGHT))
                .withPermitNumber(permit.getPermitNumber())
                .withAuthorInfo(user.getPerson())
                .build();

        assertCreateThrows(HarvestPermitSpeciesAmountNotFound.class, dto);
    }

    @Test
    public void testCreateHarvestForPermit_whenRhyNotFound() {
        final GameSpecies species = model().newGameSpecies(true);

        final HarvestPermit permit = model().newHarvestPermit(true);
        final HarvestPermitSpeciesAmount amount = model().newHarvestPermitSpeciesAmount(permit, species);

        final SystemUser user = persistAndAuthenticateWithNewUser(true);

        final HarvestDTO dto = create(species, 1)
                .withGeoLocation(MockGISQueryService.RHY_GEOLOCATION_NOT_FOUND)
                .withPermitNumber(permit.getPermitNumber())
                .withPointOfTime(amount.getBeginDate().toLocalDateTime(LocalTime.MIDNIGHT))
                .withAuthorInfo(user.getPerson())
                .build();

        // everything else should be fine, but RHY is not found for location
        assertCreateThrows(RhyNotResolvableByGeoLocationException.class, dto);
    }

    // Test integration to HarvestSpecimenOps.
    @Ignore("Does not work during deer pilot 2020. Re-enable when deer pilot is over")
    @Test
    public void testCreate_antlerFields_startingFrom2020() {
        final GameSpecies species = model().newGameSpeciesMoose();

        onSavedAndAuthenticated(createUserWithPerson(), () -> {

            final HarvestDTO inputDto = create(species)
                    .withSpecimen(ADULT_MALE)
                    .build();

            final HarvestDTO outputDto = invokeCreateHarvest(inputDto);

            runInTransaction(() -> {
                final Harvest harvest = assertHarvestCreated(outputDto.getId());

                final List<HarvestSpecimen> specimens = findSpecimens(harvest);
                assertThat(specimens, hasSize(1));

                HarvestSpecimenAssertionBuilder.builder()
                        .mooseAdultMaleFields2020Present()
                        .mooseFields2020EqualTo(inputDto.getSpecimens().get(0))
                        .verify(specimens.get(0));
            });
        });
    }

    // Test integration to HarvestSpecimenOps.
    @Test
    public void testCreate_antlerFields_before2020() {
        final GameSpecies species = model().newGameSpeciesWhiteTailedDeer();

        onSavedAndAuthenticated(createUserWithPerson(), () -> {

            final HarvestDTO inputDto = create(species)
                    .withPointOfTime(huntingYearBeginDate(2019).toLocalDateTime(LocalTime.now()))
                    .withSpecimen(ADULT_MALE)
                    .build();

            final HarvestDTO outputDto = invokeCreateHarvest(inputDto);

            runInTransaction(() -> {
                final Harvest harvest = assertHarvestCreated(outputDto.getId());

                final List<HarvestSpecimen> specimens = findSpecimens(harvest);
                assertThat(specimens, hasSize(1));

                HarvestSpecimenAssertionBuilder.builder()
                        .permitBasedDeerAdultMaleFields2016Present()
                        .permitBasedDeerFields2016EqualTo(inputDto.getSpecimens().get(0))
                        .verify(specimens.get(0));
            });
        });
    }

    @Test
    public void testUpdateHarvest_whenNoChanges() {
        withPerson(author -> {

            final Harvest harvest = model().newHarvest(model().newGameSpecies(true), author);

            final List<HarvestSpecimen> specimens = createSpecimens(harvest, 5);
            harvest.setAmount(specimens.size());

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO dto = create(harvest)
                        .withSpecimensMappedFrom(specimens)
                        .build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest refreshed = harvestRepo.getOne(harvest.getId());
                    assertVersion(refreshed, 0);
                });
            });
        });
    }

    @Test
    public void testUpdateHarvest_whenActorChanged() {
        withPerson(author -> withPerson(actor -> {

            final Harvest harvest = model().newHarvest(model().newGameSpecies(true), author);

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO dto = create(harvest)
                        .withActorInfo(actor)
                        .build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest updated = harvestRepo.getOne(harvest.getId());
                    assertVersion(updated, 1);

                    assertAuthorAndActor(updated, F.getId(author), F.getId(actor));
                });
            });
        }));
    }

    @Test
    public void testUpdateHarvest_whenHarvestReportNotDone() {
        withRhy(rhy -> withPerson(author -> {

            final Harvest harvest = model().newHarvest(model().newGameSpecies(true), author);
            final GameSpecies newSpecies = model().newGameSpecies(true);

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO dto = create(harvest, newSpecies, 10)
                        .mutate()
                        .build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest updated = harvestRepo.getOne(harvest.getId());
                    assertVersion(updated, 1);

                    // Assert mutations.

                    assertThat(updated.getSpecies(), is(newSpecies));
                    assertThat(updated.getGeoLocation(), is(dto.getGeoLocation()));
                    assertThat(updated.getPointOfTime(), is(DateUtil.toDateTimeNullSafe(dto.getPointOfTime())));

                    final List<HarvestSpecimen> specimens = findSpecimens(updated);
                    assertThat(specimens, hasSize(10));
                    assertSpecimens(specimens, dto.getSpecimens(), dto.specimenOps()::equalContent);
                });
            });
        }));
    }

    @Test
    public void testUpdateHarvest_whenSwitchingToSpeciesRequiringHarvestReport() {
        withRhy(rhy -> withPerson(author -> {

            final GameSpecies species = model().newGameSpecies();
            final Harvest harvest = model().newHarvest(species, author);

            final LocalDate seasonBegin = today();
            final LocalDate seasonEnd = seasonBegin.plusDays(1);

            final GameSpecies newSpecies = model().newGameSpecies(OFFICIAL_CODE_BEAR);
            final HarvestSeason season = model().newHarvestSeason(newSpecies, seasonBegin, seasonEnd, seasonEnd);

            onSavedAndAuthenticated(createUser(author), user -> {

                final HarvestDTO dto = create(harvest, newSpecies, 1)
                        .withPointOfTime(seasonBegin.toDateTimeAtStartOfDay().toLocalDateTime())
                        .build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest updated = harvestRepo.getOne(harvest.getId());
                    assertVersion(updated, 1);

                    // Assert mutations.

                    assertThat(updated.getSpecies(), is(newSpecies));
                    assertThat(updated.getGeoLocation(), is(dto.getGeoLocation()));
                    assertThat(updated.getPointOfTime(), is(DateUtil.toDateTimeNullSafe(dto.getPointOfTime())));

                    final List<HarvestSpecimen> specimens = findSpecimens(updated);
                    assertThat(specimens, hasSize(1));
                    assertSpecimens(specimens, dto.getSpecimens(), dto.specimenOps()::equalContent);

                    // Assert harvest report state

                    assertThat(updated.getHarvestSeason(), is(season));

                    assertHarvestReportState(updated, HarvestReportState.SENT_FOR_APPROVAL, author);
                    assertChangeHistoryEventExists(updated, user, HarvestReportState.SENT_FOR_APPROVAL);

                    assertEmptyHarvestPermitState(updated);

                    assertThat(updated.getHarvestQuota(), is(nullValue()));
                });
            });
        }));
    }

    @Test
    public void testUpdateHarvest_whenHarvestReportDone_changeDateOutsideSeason_asModerator() {
        withRhy(rhy -> withPerson(author -> {

            final GameSpecies species = model().newGameSpecies(true);

            final LocalDate seasonBegin = today();
            final LocalDate seasonEnd = seasonBegin.plusDays(1);
            final HarvestSeason season = model().newHarvestSeason(species, seasonBegin, seasonEnd, seasonEnd);

            final Harvest harvest = model().newHarvest(species, author);
            harvest.setPointOfTime(seasonBegin.toDateTimeAtStartOfDay());
            harvest.setHarvestSeason(season);
            harvest.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
            harvest.setHarvestReportAuthor(author);
            harvest.setHarvestReportDate(DateUtil.now());

            final List<HarvestSpecimen> specimens = createSpecimens(harvest, 5);
            harvest.setAmount(specimens.size());

            onSavedAndAuthenticated(createNewModerator(), () -> {

                final HarvestDTO dto = create(harvest, 10)
                        .withPointOfTime(seasonBegin.minusDays(1).toDateTimeAtStartOfDay().toLocalDateTime())
                        .build();

                assertUpdateThrows(HarvestReportingTypeChangeForbiddenException.class, dto);
            });
        }));
    }

    @Test
    public void testUpdateHarvest_notUpdatedWhenHarvestReportDone() {
        withRhy(rhy -> withPerson(author -> {

            final GameSpecies originalSpecies = model().newGameSpecies(true);

            final Harvest harvest = model().newHarvest(originalSpecies, author);
            harvest.setHarvestReportState(HarvestReportState.APPROVED);
            harvest.setHarvestReportAuthor(author);
            harvest.setHarvestReportDate(DateUtil.now());

            final List<HarvestSpecimen> originalSpecimens = createSpecimens(harvest, 5);
            harvest.setAmount(originalSpecimens.size());

            final GameSpecies newSpecies = model().newGameSpecies(true);

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO dto = create(harvest, newSpecies, 10).mutate().build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest updated = harvestRepo.getOne(harvest.getId());
                    assertVersion(updated, 1);

                    // Assert that description is changed.
                    assertThat(updated.getDescription(), is(dto.getDescription()));

                    // Assert that other harvest fields are NOT changed.

                    assertThat(updated.getSpecies(), is(originalSpecies));
                    assertThat(updated.getGeoLocation(), is(harvest.getGeoLocation()));
                    assertThat(updated.getPointOfTime(), is(harvest.getPointOfTime()));

                    assertThat(updated.getHarvestReportState(), is(HarvestReportState.APPROVED));

                    assertThat(findSpecimens(updated), hasSize(5)); // number of specimens should be unchanged
                });
            });
        }));
    }

    @Test
    public void testUpdateHarvest_whenUpdatingSpecimensOnly() {
        withPerson(author -> {

            final Harvest harvest = model().newHarvest(model().newGameSpecies(true), author);

            final List<HarvestSpecimen> specimens = createSpecimens(harvest, 5);
            harvest.setAmount(specimens.size());

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO dto = create(harvest)
                        .withSpecimensMappedFrom(specimens)
                        .mutateSpecimens()
                        .build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest updated = harvestRepo.getOne(harvest.getId());
                    assertVersion(updated, 1);

                    final List<HarvestSpecimen> updatedSpecimens = findSpecimens(updated);
                    assertSpecimens(updatedSpecimens, dto.getSpecimens(), dto.specimenOps()::equalContent);
                });
            });
        });
    }

    @Test
    public void testUpdateHarvest_forAmountChange_whenSpecimensPresent() {
        withPerson(author -> {

            final Harvest harvest = model().newHarvest(model().newGameSpecies(true), author);

            final List<HarvestSpecimen> specimens = createSpecimens(harvest, 5);
            harvest.setAmount(specimens.size());

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO dto = create(harvest)
                        .withSpecimensMappedFrom(specimens)
                        .withAmount(15)
                        .build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest updated = harvestRepo.getOne(harvest.getId());
                    assertVersion(updated, 1);

                    assertThat(updated.getAmount(), is(15));
                    assertThat(findSpecimens(updated), hasSize(5));
                });
            });
        });
    }

    @Test
    public void testUpdateHarvest_forAmountChange_whenSpecimensNotPresent() {
        withPerson(author -> {

            final Harvest harvest = model().newHarvest(model().newGameSpecies(true), author);

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO dto = create(harvest).withAmount(10).build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest updated = harvestRepo.getOne(harvest.getId());
                    assertVersion(updated, 1);

                    assertThat(updated.getAmount(), is(10));
                    assertThat(findSpecimens(updated), is(empty()));
                });
            });
        });
    }

    @Test
    public void testUpdateHarvest_whenProposedForPermit_asAuthor() {
        withRhy(rhy -> withPerson(author -> {

            final GameSpecies species = model().newGameSpecies();

            final HarvestPermit permit = model().newHarvestPermit(rhy);
            model().newHarvestPermitSpeciesAmount(permit, species);

            final Harvest harvest = model().newHarvest(species, author);
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(PROPOSED);
            harvest.setRhy(rhy);

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO dto = create(harvest).mutate().build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest updated = harvestRepo.getOne(harvest.getId());
                    assertVersion(updated, 1);

                    assertHarvestPermitState(updated, PROPOSED, permit);

                    assertEmptyHarvestReportState(updated);
                });
            });
        }));
    }

    @Test
    public void testUpdateHarvest_whenProposedForPermit_asContactPerson() {
        withRhy(rhy -> withPerson(originalContactPerson -> withPerson(anotherContactPerson -> {

            final GameSpecies species = model().newGameSpecies();

            final HarvestPermit permit = model().newHarvestPermit(rhy, anotherContactPerson);
            permit.setOriginalContactPerson(originalContactPerson);
            model().newHarvestPermitSpeciesAmount(permit, species);

            final Harvest harvest = model().newHarvest(species, anotherContactPerson);
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(PROPOSED);
            harvest.setRhy(rhy);

            onSavedAndAuthenticated(createUser(anotherContactPerson), user -> {

                final HarvestDTO dto = create(harvest).mutate().build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest updated = harvestRepo.getOne(harvest.getId());
                    assertVersion(updated, 1);

                    assertHarvestPermitState(updated, ACCEPTED, permit);
                    assertHarvestReportState(updated, HarvestReportState.SENT_FOR_APPROVAL, anotherContactPerson);

                    assertChangeHistoryEventExists(updated, user, HarvestReportState.SENT_FOR_APPROVAL);
                });
            });
        })));
    }

    @Test
    public void testUpdateHarvest_whenProposedForPermit_asModerator() {
        withPerson(originalContact -> withPerson(anotherContact -> withPerson(newAuthor -> withPerson(newActor -> {

            final GameSpecies species = model().newGameSpecies();

            final HarvestPermit permit = model().newHarvestPermit(anotherContact);
            permit.setOriginalContactPerson(originalContact);
            model().newHarvestPermitSpeciesAmount(permit, species);

            final Harvest harvest = model().newHarvest(species, anotherContact);
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(PROPOSED);
            harvest.setRhy(permit.getRhy());

            final String originalDescription = harvest.getDescription();

            onSavedAndAuthenticated(createNewModerator(), moderator -> {

                final HarvestDTO dto = create(harvest)
                        .withAuthorInfo(newAuthor)
                        .withActorInfo(newActor)
                        .withModeratorChangeReason("reason")
                        .mutate()
                        .build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest updated = harvestRepo.getOne(harvest.getId());
                    assertVersion(updated, 1);

                    // Assert that description is NOT changed.
                    assertThat(updated.getDescription(), is(originalDescription));

                    // Assert that other mutations take effect.
                    assertThat(updated.getGeoLocation(), is(dto.getGeoLocation()));
                    assertThat(updated.getPointOfTime(), is(DateUtil.toDateTimeNullSafe(dto.getPointOfTime())));

                    assertAuthorAndActor(updated, F.getId(newAuthor), F.getId(newActor));

                    // Assert permit/report state.

                    assertHarvestPermitState(updated, ACCEPTED, permit);
                    assertHarvestReportState(updated, HarvestReportState.SENT_FOR_APPROVAL, originalContact);

                    final HarvestChangeHistory historyEvent =
                            assertChangeHistoryEventExists(updated, moderator, HarvestReportState.SENT_FOR_APPROVAL);
                    assertThat(historyEvent.getReasonForChange(), is("reason"));
                });
            });
        }))));
    }

    @Test
    public void testUpdateHarvest_whenAcceptedForPermit_asAuthor() {
        withRhy(rhy -> withPerson(author -> {

            final GameSpecies species = model().newGameSpecies();
            final GameSpecies newSpecies = model().newGameSpecies();

            final HarvestPermit permit = model().newHarvestPermit(rhy);
            model().newHarvestPermitSpeciesAmount(permit, species);

            final Harvest harvest = model().newHarvest(species, author);
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(ACCEPTED);
            harvest.setRhy(rhy);

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO dto = create(harvest, newSpecies, 5).mutate().build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest updated = harvestRepo.getOne(harvest.getId());
                    assertVersion(updated, 1);

                    // Assert that description is changed.
                    assertThat(updated.getDescription(), is(dto.getDescription()));

                    // Assert that other harvest fields are NOT changed.

                    assertThat(updated.getSpecies(), is(species));
                    assertThat(updated.getGeoLocation(), is(harvest.getGeoLocation()));
                    assertThat(updated.getPointOfTime(), is(harvest.getPointOfTime()));
                    assertThat(updated.getAmount(), is(1)); // same as originally

                    assertThat(findSpecimens(updated), is(empty())); // specimens should not be added

                    assertHarvestPermitState(updated, ACCEPTED, permit);

                    assertEmptyHarvestReportState(updated);
                });
            });
        }));
    }

    @Test
    public void testUpdateHarvest_whenAcceptedForPermit_asContactPerson() {
        withRhy(rhy -> withPerson(originalContactPerson -> withPerson(anotherContactPerson -> {
            final GameSpecies species = model().newGameSpecies();

            final HarvestPermit permit = model().newHarvestPermit(rhy, anotherContactPerson);
            permit.setOriginalContactPerson(originalContactPerson);
            model().newHarvestPermitSpeciesAmount(permit, species);

            final Harvest harvest = model().newHarvest(species, anotherContactPerson);
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(ACCEPTED);
            harvest.setRhy(rhy);

            onSavedAndAuthenticated(createUser(anotherContactPerson), user -> {

                final HarvestDTO dto = create(harvest, 1).mutate().build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest updated = harvestRepo.getOne(harvest.getId());
                    assertVersion(updated, 1);

                    assertThat(updated.getGeoLocation(), is(dto.getGeoLocation()));
                    assertThat(updated.getPointOfTime(), is(DateUtil.toDateTimeNullSafe(dto.getPointOfTime())));
                    assertThat(updated.getDescription(), is(dto.getDescription()));

                    final List<HarvestSpecimen> updatedSpecimens = findSpecimens(updated);
                    assertThat(updatedSpecimens, hasSize(1));
                    assertSpecimens(updatedSpecimens, dto.getSpecimens(), dto.specimenOps()::equalContent);

                    assertHarvestPermitState(updated, ACCEPTED, permit);
                    assertHarvestReportState(updated, HarvestReportState.SENT_FOR_APPROVAL, anotherContactPerson);

                    assertChangeHistoryEventExists(updated, user, HarvestReportState.SENT_FOR_APPROVAL);
                });
            });
        })));
    }

    @Test
    public void testUpdateHarvest_whenAcceptedForPermit_asModerator() {
        withPerson(originalContact -> withPerson(anotherContact -> withPerson(newAuthor -> withPerson(newActor -> {

            final GameSpecies species = model().newGameSpecies();

            final HarvestPermit permit = model().newHarvestPermit(anotherContact);
            permit.setOriginalContactPerson(originalContact);
            model().newHarvestPermitSpeciesAmount(permit, species);

            final Harvest harvest = model().newHarvest(species, anotherContact);
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(ACCEPTED);
            harvest.setRhy(permit.getRhy());

            final String originalDescription = harvest.getDescription();

            onSavedAndAuthenticated(createNewModerator(), moderator -> {

                final HarvestDTO dto = create(harvest)
                        .withAuthorInfo(newAuthor)
                        .withActorInfo(newActor)
                        .withModeratorChangeReason("reason")
                        .mutate()
                        .build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest updated = harvestRepo.getOne(harvest.getId());
                    assertVersion(updated, 1);

                    // Assert that description is NOT changed.
                    assertThat(updated.getDescription(), is(originalDescription));

                    // Assert that other mutations take effect.

                    assertThat(updated.getGeoLocation(), is(dto.getGeoLocation()));
                    assertThat(updated.getPointOfTime(), is(DateUtil.toDateTimeNullSafe(dto.getPointOfTime())));

                    assertAuthorAndActor(updated, F.getId(newAuthor), F.getId(newActor));

                    // Assert permit/report state.

                    assertHarvestPermitState(updated, ACCEPTED, permit);
                    assertHarvestReportState(updated, HarvestReportState.SENT_FOR_APPROVAL, originalContact);

                    final HarvestChangeHistory historyEvent =
                            assertChangeHistoryEventExists(updated, moderator, HarvestReportState.SENT_FOR_APPROVAL);
                    assertThat(historyEvent.getReasonForChange(), is("reason"));
                });
            });
        }))));
    }

    @Test
    public void testUpdateHarvest_addPermit_asAuthor() {
        withPerson(author -> {

            final GameSpecies species = model().newGameSpecies();
            final Harvest harvest = model().newHarvest(species, author);

            final HarvestPermit permit = model().newHarvestPermit();
            model().newHarvestPermitSpeciesAmount(permit, species);

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO dto = create(harvest)
                        .withPermitNumber(permit.getPermitNumber())
                        .build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest updated = harvestRepo.getOne(harvest.getId());
                    assertVersion(updated, 1);

                    assertHarvestPermitState(updated, PROPOSED, permit);

                    assertEmptyHarvestReportState(updated);
                });
            });
        });
    }

    @Test
    public void testUpdateHarvest_addPermit_asContactPerson() {
        withPerson(originalContactPerson -> withPerson(anotherContactPerson -> {

            final GameSpecies species = model().newGameSpecies();
            final Harvest harvest = model().newHarvest(species, anotherContactPerson);

            final HarvestPermit permit = model().newHarvestPermit(anotherContactPerson);
            permit.setOriginalContactPerson(originalContactPerson);
            model().newHarvestPermitSpeciesAmount(permit, species);

            onSavedAndAuthenticated(createUser(anotherContactPerson), user -> {

                final HarvestDTO dto = create(harvest)
                        .withPermitNumber(permit.getPermitNumber())
                        .build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest updated = harvestRepo.getOne(harvest.getId());
                    assertVersion(updated, 1);

                    assertHarvestPermitState(updated, ACCEPTED, permit);
                    assertHarvestReportState(updated, HarvestReportState.SENT_FOR_APPROVAL, anotherContactPerson);

                    assertChangeHistoryEventExists(updated, user, HarvestReportState.SENT_FOR_APPROVAL);
                });
            });
        }));
    }

    @Test
    public void testUpdateHarvest_addPermit_whenPermitNotFound() {
        withRhy(rhy -> withPerson(author -> {

            final Harvest harvest = model().newHarvest(author);

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO dto = create(harvest)
                        .withPermitNumber(permitNumber())
                        .build();

                assertUpdateThrows(HarvestPermitNotFoundException.class, dto);
            });
        }));
    }

    @Test
    public void testUpdateHarvest_addPermit_ignoreMoosePermit() {
        withRhy(rhy -> withPerson(author -> {

            final GameSpecies moose = model().newGameSpeciesMoose();
            final Harvest harvest = model().newHarvest(moose, author);

            final HarvestPermit permit = model().newMooselikePermit(rhy);
            model().newHarvestPermitSpeciesAmount(permit, moose);

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO dto = create(harvest)
                        .withPermitNumber(permit.getPermitNumber())
                        .build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest updated = harvestRepo.getOne(harvest.getId());
                    assertVersion(updated, 1);

                    assertEmptyHarvestPermitState(updated);
                    assertEmptyHarvestReportState(updated);
                });
            });
        }));
    }

    @Test
    public void testUpdateHarvest_addPermit_whenRhyNotFound() {
        withPerson(author -> {

            final GameSpecies species = model().newGameSpecies();
            final Harvest harvest = model().newHarvest(species, author);

            final HarvestPermit permit = model().newHarvestPermit();
            model().newHarvestPermitSpeciesAmount(permit, species);

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO dto = create(harvest)
                        .withGeoLocation(MockGISQueryService.RHY_GEOLOCATION_NOT_FOUND)
                        .withPermitNumber(permit.getPermitNumber())
                        .build();

                // everything else should be fine, but rhy is not found for location
                assertUpdateThrows(RhyNotResolvableByGeoLocationException.class, dto);
            });
        });
    }

    @Test
    public void testUpdateHarvest_addPermit_whenSpeciesIsNotValid() {
        withPerson(author -> {

            final GameSpecies species = model().newGameSpecies();
            final Harvest harvest = model().newHarvest(species, author);

            final HarvestPermit permit = model().newHarvestPermit();
            final HarvestPermitSpeciesAmount amount = model().newHarvestPermitSpeciesAmount(permit, species);

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO dto = create(harvest)
                        .withPermitNumber(permit.getPermitNumber())
                        .withPointOfTime(amount.getBeginDate().minusDays(1).toLocalDateTime(LocalTime.MIDNIGHT))
                        .build();

                assertUpdateThrows(HarvestPermitSpeciesAmountNotFound.class, dto);
            });
        });
    }

    @Test
    public void testUpdateHarvest_addPermit_whenPermitEndOfHuntingReportDone() {
        withRhy(rhy -> withPerson(author -> withPerson(permitContactPerson -> {

            final GameSpecies species = model().newGameSpecies();
            final Harvest harvest = model().newHarvest(species, author);

            final HarvestPermit permit = model().newHarvestPermit(rhy);
            permit.setOriginalContactPerson(permitContactPerson);
            permit.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
            permit.setHarvestReportAuthor(permitContactPerson);
            permit.setHarvestReportDate(DateUtil.now());
            permit.setHarvestReportModeratorOverride(false);

            model().newHarvestPermitSpeciesAmount(permit, species);

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO dto = create(harvest)
                        .withPermitNumber(permit.getPermitNumber())
                        .build();

                assertUpdateThrows(EndOfHuntingReportExistsException.class, dto);
            });
        })));
    }

    @Test
    public void testUpdateHarvest_removePermit_whenRejectedForPermit() {
        withRhy(rhy -> withPerson(author -> {

            final GameSpecies species = model().newGameSpecies();

            final HarvestPermit permit = model().newHarvestPermit(rhy);
            model().newHarvestPermitSpeciesAmount(permit, species);

            final Harvest harvest = model().newHarvest(species, author);
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(REJECTED);
            harvest.setRhy(rhy);

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO dto = create(harvest).withPermitNumber(null).build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest updated = harvestRepo.getOne(harvest.getId());
                    assertVersion(updated, 1);

                    assertEmptyHarvestPermitState(updated);
                    assertEmptyHarvestReportState(updated);
                });
            });
        }));
    }

    @Test
    public void testUpdateHarvest_removePermit_whenAcceptedToPermit() {
        withRhy(rhy -> withPerson(author -> {

            final GameSpecies species = model().newGameSpecies();

            final HarvestPermit permit = model().newHarvestPermit(rhy);
            model().newHarvestPermitSpeciesAmount(permit, species);

            final Harvest harvest = model().newHarvest(species, author);
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(ACCEPTED);
            harvest.setRhy(rhy);
            harvest.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
            harvest.setHarvestReportDate(DateUtil.now());
            harvest.setHarvestReportAuthor(author);
            harvest.setHarvestReportRequired(false);

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO dto = create(harvest).withPermitNumber(null).build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest refreshed = harvestRepo.getOne(harvest.getId());
                    assertVersion(refreshed, 0); // should not have changed
                });
            });
        }));
    }

    @Test
    public void testUpdateHarvest_changePermit_whenProposedForPermit_asAuthor() {
        withRhy(rhy -> withPerson(author -> {

            final GameSpecies species = model().newGameSpecies();

            final HarvestPermit permit = model().newHarvestPermit(rhy);
            model().newHarvestPermitSpeciesAmount(permit, species);

            final HarvestPermit otherPermit = model().newHarvestPermit(rhy);
            model().newHarvestPermitSpeciesAmount(otherPermit, species);

            final Harvest harvest = model().newHarvest(species, author);
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(PROPOSED);
            harvest.setRhy(rhy);

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO dto = create(harvest)
                        .withPermitNumber(otherPermit.getPermitNumber())
                        .build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest updated = harvestRepo.getOne(harvest.getId());
                    assertVersion(updated, 1);

                    assertHarvestPermitState(updated, PROPOSED, otherPermit);

                    assertEmptyHarvestReportState(updated);
                });
            });
        }));
    }

    @Test
    public void testUpdateHarvest_changePermit_whenProposedForPermit_asContactPerson() {
        withRhy(rhy -> withPerson(originalContactPerson -> withPerson(anotherContactPerson -> {

            final GameSpecies species = model().newGameSpecies();

            final HarvestPermit permit = model().newHarvestPermit(rhy);
            model().newHarvestPermitSpeciesAmount(permit, species);

            final HarvestPermit otherPermit = model().newHarvestPermit(rhy, anotherContactPerson);
            otherPermit.setOriginalContactPerson(originalContactPerson);
            model().newHarvestPermitSpeciesAmount(otherPermit, species);

            final Harvest harvest = model().newHarvest(species, anotherContactPerson);
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(PROPOSED);
            harvest.setRhy(rhy);

            onSavedAndAuthenticated(createUser(anotherContactPerson), user -> {

                final HarvestDTO dto = create(harvest)
                        .withPermitNumber(otherPermit.getPermitNumber())
                        .build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest updated = harvestRepo.getOne(harvest.getId());
                    assertVersion(updated, 1);

                    assertHarvestPermitState(updated, ACCEPTED, otherPermit);
                    assertHarvestReportState(updated, HarvestReportState.SENT_FOR_APPROVAL, anotherContactPerson);

                    assertChangeHistoryEventExists(updated, user, HarvestReportState.SENT_FOR_APPROVAL);
                });
            });
        })));
    }

    @Test
    public void testUpdateHarvest_changePermit_whenPermitEndOfHuntingReportDone_asContactPerson() {
        withRhy(rhy -> withPerson(originalContactPerson -> withPerson(anotherContactPerson -> {

            final GameSpecies species = model().newGameSpecies();

            final HarvestPermit permit = model().newHarvestPermit(rhy, anotherContactPerson);
            permit.setOriginalContactPerson(originalContactPerson);
            model().newHarvestPermitSpeciesAmount(permit, species);

            final HarvestPermit otherPermit = model().newHarvestPermit(rhy, anotherContactPerson);
            otherPermit.setOriginalContactPerson(originalContactPerson);
            permit.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
            permit.setHarvestReportAuthor(originalContactPerson);
            permit.setHarvestReportDate(DateUtil.now());
            permit.setHarvestReportModeratorOverride(false);
            model().newHarvestPermitSpeciesAmount(otherPermit, species);

            final Harvest harvest = model().newHarvest(species, anotherContactPerson);
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(ACCEPTED);
            harvest.setRhy(rhy);
            harvest.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
            harvest.setHarvestReportAuthor(anotherContactPerson);
            harvest.setHarvestReportDate(DateUtil.now());
            harvest.setHarvestReportRequired(true);

            onSavedAndAuthenticated(createUser(anotherContactPerson), () -> {

                final HarvestDTO dto = create(harvest)
                        .withPermitNumber(otherPermit.getPermitNumber())
                        .build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest updated = harvestRepo.getOne(harvest.getId());
                    assertVersion(updated, 0); // nothing expected to change

                    // Assert permit/report state.

                    assertHarvestPermitState(updated, ACCEPTED, permit);
                    assertHarvestReportState(updated, HarvestReportState.SENT_FOR_APPROVAL, anotherContactPerson);

                    assertThat(changeEventHistoryRepo.findByHarvest(updated), is(empty()));
                });
            });
        })));
    }

    @Test
    public void testUpdateHarvest_withHarvestAcceptedToPermit_asContactPerson() {
        withHarvestHavingPermitState(ACCEPTED, harvest -> {
            harvest.getHarvestPermit().setOriginalContactPerson(harvest.getAuthor());
            doTestUpdateHarvestWithPermit(true, true, ACCEPTED, harvest);
        });
    }

    @Test
    public void testUpdateHarvest_withHarvestAcceptedToPermit() {
        withHarvestHavingPermitState(ACCEPTED, harvest -> {
            doTestUpdateHarvestWithPermit(false, true, ACCEPTED, harvest);
        });
    }

    @Test
    public void testUpdateHarvest_withHarvestProposedToPermit() {
        withHarvestHavingPermitState(PROPOSED, harvest -> {
            doTestUpdateHarvestWithPermit(true, false, PROPOSED, harvest);
        });
    }

    @Test
    public void testUpdateHarvest_withHarvestRejectedToPermit() {
        withHarvestHavingPermitState(REJECTED, harvest -> {
            doTestUpdateHarvestWithPermit(true, false, PROPOSED, harvest);
        });
    }

    private void doTestUpdateHarvestWithPermit(final boolean expectBusinessFieldsUpdated,
                                               final boolean expectedHarvestReportRequired,
                                               final StateAcceptedToHarvestPermit expectedStateAfterUpdate,
                                               final Harvest harvest) {

        final HarvestPermit permit = harvest.getHarvestPermit();

        onSavedAndAuthenticated(createUser(harvest.getAuthor()), () -> {

            final HarvestDTO dto = create(harvest, 5)
                    .mutate()
                    .withPermitNumber(permit.getPermitNumber())
                    .build();

            invokeUpdateHarvest(dto);

            runInTransaction(() -> {
                final Harvest updated = harvestRepo.getOne(harvest.getId());
                assertVersion(updated, 1);

                assertHarvestPermitState(updated, expectedStateAfterUpdate, permit);

                if (expectBusinessFieldsUpdated) {
                    assertThat(updated.getGeoLocation(), is(dto.getGeoLocation()));
                    assertThat(updated.getPointOfTime(), is(DateUtil.toDateTimeNullSafe(dto.getPointOfTime())));

                    assertThat(updated.isHarvestReportRequired(), is(expectedHarvestReportRequired));

                    if (expectedHarvestReportRequired) {
                        assertThat(updated.getHarvestReportAuthor(), is(notNullValue()));
                        assertThat(updated.getHarvestReportDate(), is(notNullValue()));
                    } else {
                        assertThat(updated.getHarvestReportAuthor(), is(nullValue()));
                        assertThat(updated.getHarvestReportDate(), is(nullValue()));
                    }

                } else {
                    assertThat(updated.getGeoLocation(), is(harvest.getGeoLocation()));
                    assertThat(updated.getPointOfTime(), is(harvest.getPointOfTime()));
                }
            });
        });
    }

    @Test
    public void testUpdateHarvest_forAuthorChangedWhenNormalUser() {
        withPerson(author -> withPerson(newAuthor -> {

            final Harvest harvest = model().newHarvest(author);

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO dto = create(harvest)
                        .mutate()
                        .withAuthorInfo(newAuthor)
                        .build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest updated = harvestRepo.getOne(harvest.getId());
                    assertVersion(updated, 1);

                    // author change not effective
                    assertAuthorAndActor(updated, F.getId(author), F.getId(author));
                });
            });
        }));
    }

    @Test
    public void testUpdateHarvest_descriptionAndImagesNotChangedIfNeitherAuthorNorShooter() {
        final String description = "Author wrote description of harvest.";

        final Harvest harvest = model().newHarvest(model().newPerson());
        harvest.setDescription(description);

        final GameDiaryImage image = model().newGameDiaryImage(harvest);

        onSavedAndAuthenticated(createNewModerator(), () -> {

            final HarvestDTO dto = create(harvest)
                    .withAuthorInfo(harvest.getAuthor())
                    .withActorInfo(harvest.getActor())
                    .withDescription("moderator changed this")
                    .withImageIds(Collections.emptyList())
                    .withModeratorChangeReason("foobar")
                    .build();

            invokeUpdateHarvest(dto);

            runInTransaction(() -> {
                final Harvest updated = harvestRepo.getOne(harvest.getId());
                assertThat(updated.getDescription(), is(description));

                assertThat(updated.getImages(), hasSize(1));
                final GameDiaryImage updatedImage = updated.getImages().iterator().next();
                assertThat(updatedImage.getFileMetadata(), is(image.getFileMetadata()));
            });
        });
    }

    @Test
    public void testDeleteHarvest_whenHarvestReportNotDone() {
        withPerson(author -> {

            final Harvest harvest = model().newHarvest(author);

            model().newHarvestSpecimen(harvest);
            model().newGameDiaryImage(harvest);

            onSavedAndAuthenticated(createUser(author), () -> {
                final long harvestId = harvest.getId();
                feature.deleteHarvest(harvestId);
                assertThat(harvestRepo.existsById(harvestId), is(false));
            });
        });
    }

    @Test
    public void testDeleteHarvest_whenHarvestReportApproved() {
        withPerson(author -> {

            final Harvest harvest = model().newHarvest(author);
            harvest.setHarvestReportState(HarvestReportState.APPROVED);
            harvest.setHarvestReportAuthor(author);
            harvest.setHarvestReportDate(DateUtil.now());

            model().newHarvestSpecimen(harvest);

            onSavedAndAuthenticated(createUser(author), () -> {
                final Throwable exception =
                        assertThrows(RuntimeException.class, () -> feature.deleteHarvest(harvest.getId()));

                assertThat(exception.getMessage(), is("Cannot delete harvest with an associated harvest report."));
            });
        });
    }

    @Test
    public void testDeleteHarvest_withHarvestAcceptedToPermit_asContactPerson() {
        testDeleteHarvest_whenHarvestHasPermitProcessingState(true, ACCEPTED, harvest -> {
            harvest.getHarvestPermit().setOriginalContactPerson(harvest.getAuthor());
        });
    }

    @Test
    public void testDeleteHarvest_withHarvestAcceptedToPermit() {
        testDeleteHarvest_whenHarvestHasPermitProcessingState(false, ACCEPTED);
    }

    @Test
    public void testDeleteHarvest_withHarvestProposedToPermit() {
        testDeleteHarvest_whenHarvestHasPermitProcessingState(true, PROPOSED);
    }

    @Test
    public void testDeleteHarvest_withHarvestRejectedToPermit() {
        testDeleteHarvest_whenHarvestHasPermitProcessingState(true, REJECTED);
    }

    private void testDeleteHarvest_whenHarvestHasPermitProcessingState(final boolean shouldBeDeleted,
                                                                       final StateAcceptedToHarvestPermit state) {

        testDeleteHarvest_whenHarvestHasPermitProcessingState(shouldBeDeleted, state, harvest -> {});
    }

    private void testDeleteHarvest_whenHarvestHasPermitProcessingState(final boolean shouldBeDeleted,
                                                                       final StateAcceptedToHarvestPermit state,
                                                                       final Consumer<Harvest> consumer) {
        withHarvestHavingPermitState(state, harvest -> {
            consumer.accept(harvest);

            onSavedAndAuthenticated(createUser(harvest.getAuthor()), () -> {
                final long harvestId = harvest.getId();

                if (shouldBeDeleted) {
                    feature.deleteHarvest(harvestId);
                    assertThat(harvestRepo.existsById(harvestId), is(false));
                } else {
                    final Throwable exception =
                            assertThrows(RuntimeException.class, () -> feature.deleteHarvest(harvestId));

                    assertThat(exception.getMessage(), is("Cannot delete harvest which is accepted to permit."));
                }
            });
        });
    }

    private void withHarvestHavingPermitState(final StateAcceptedToHarvestPermit state,
                                              final Consumer<Harvest> consumer) {

        withRhy(rhy -> withPerson(author -> {

            final GameSpecies species = model().newGameSpecies(true);

            final HarvestPermit permit = model().newHarvestPermit(rhy, true);
            model().newHarvestPermitSpeciesAmount(permit, species);

            final Harvest harvest = model().newHarvest(species, author);
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(state);
            harvest.setRhy(rhy);

            consumer.accept(harvest);
        }));
    }

    private HarvestChangeHistory assertChangeHistoryEventExists(final Harvest harvest,
                                                                final SystemUser expectedUser,
                                                                final HarvestReportState expectedState) {

        final List<HarvestChangeHistory> historyEvents = changeEventHistoryRepo.findByHarvest(harvest);
        assertThat(historyEvents, hasSize(1));

        final HarvestChangeHistory historyEvent = historyEvents.get(0);
        assertThat(historyEvent.getHarvestReportState(), is(expectedState));
        assertThat(historyEvent.getUserId(), is(expectedUser.getId()));
        return historyEvent;
    }

    private static void assertHarvestReportState(final Harvest harvest,
                                                 final HarvestReportState expectedState,
                                                 final Person expectedAuthor) {

        assertThat(harvest.getHarvestReportState(), is(expectedState));
        assertThat(harvest.isHarvestReportRequired(), is(true));
        assertThat(harvest.getHarvestReportAuthor(), is(expectedAuthor));
        assertThat(harvest.getHarvestReportDate(), is(notNullValue()));
    }

    private void assertEmptyHarvestReportState(final Harvest harvest) {
        assertThat(harvest.getHarvestReportState(), is(nullValue()));
        assertThat(harvest.isHarvestReportRequired(), is(false));
        assertThat(harvest.getHarvestReportAuthor(), is(nullValue()));
        assertThat(harvest.getHarvestReportDate(), is(nullValue()));

        assertThat(changeEventHistoryRepo.findByHarvest(harvest), is(empty()));
    }

    private static void assertHarvestPermitState(final Harvest harvest,
                                                 final StateAcceptedToHarvestPermit expectedPermitState,
                                                 final HarvestPermit expectedPermit) {

        assertThat(harvest.getStateAcceptedToHarvestPermit(), is(expectedPermitState));
        assertThat(harvest.getHarvestPermit(), is(expectedPermit));
    }

    private static void assertEmptyHarvestPermitState(final Harvest harvest) {
        assertThat(harvest.getHarvestPermit(), is(nullValue()));
        assertThat(harvest.getStateAcceptedToHarvestPermit(), is(nullValue()));
    }
}

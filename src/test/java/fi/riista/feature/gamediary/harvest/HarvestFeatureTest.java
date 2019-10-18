package fi.riista.feature.gamediary.harvest;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameDiaryEntryFeatureTest;
import fi.riista.feature.gamediary.GameDiaryFeature;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.fixture.HarvestDTOBuilderFactory;
import fi.riista.feature.gamediary.harvest.Harvest.StateAcceptedToHarvestPermit;
import fi.riista.feature.gamediary.harvest.mutation.exception.HarvestReportingTypeChangeForbiddenException;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenRepository;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenValidationException;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen_;
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
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.hunting.ClubHuntingFinishedException;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.hunting.day.PointOfTimeOutsideOfHuntingDayException;
import fi.riista.feature.huntingclub.hunting.day.PointOfTimeOutsideOfPermittedDatesException;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static fi.riista.test.TestUtils.createList;
import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class HarvestFeatureTest extends GameDiaryEntryFeatureTest
        implements HarvestDTOBuilderFactory, HuntingGroupFixtureMixin {

    @Resource
    private GameDiaryFeature feature;

    @Resource
    private HarvestRepository harvestRepo;

    @Resource
    private HarvestSpecimenRepository harvestSpecimenRepo;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testCreateHarvest() {
        withRhy(rhy -> withPerson(author -> {

            final GameSpecies species = model().newGameSpecies(true);

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO inputDto = create(species, 5).build();
                final HarvestDTO outputDto = invokeCreateHarvest(inputDto);

                doCreateAssertions(outputDto.getId(), inputDto, author, author, h -> {
                    assertNotNull(h.getRhy());
                    assertEquals(rhy.getOfficialCode(), h.getRhy().getOfficialCode());
                });
            });
        }));
    }

    @Test(expected = HarvestReportingTypeChangeForbiddenException.class)
    public void testCreateHarvest_asModerator() {
        withRhy(rhy -> withPerson(author -> {
            final GameSpecies species = model().newGameSpecies(true);

            onSavedAndAuthenticated(createNewModerator(), () -> {
                final HarvestDTO inputDto = create(species)
                        .withAuthorInfo(author)
                        .withActorInfo(author)
                        .build();

                invokeCreateHarvest(inputDto);
            });
        }));
    }

    @Test
    public void testCreateHarvest_linkToHuntingDay() {
        withMooseHuntingGroupFixture(f -> {

            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today());
            final Person author = f.groupLeader;

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO inputDto = create(f.species, 1).linkToHuntingDay(huntingDay).build();
                final HarvestDTO outputDto = invokeCreateHarvest(inputDto);

                doCreateAssertions(
                        outputDto.getId(), inputDto, author, author, assertAcceptedToHuntingDay(author, huntingDay));
            });
        });
    }

    @Test
    public void testCreateHarvest_whenActorIsHunter() {
        withRhy(rhy -> withPerson(author -> withPerson(actor -> {

            final GameSpecies species = model().newGameSpecies(true);
            actor.setHunterNumber(model().hunterNumber());

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO inputDto = create(species, 5).withActorInfo(actor).build();
                final HarvestDTO outputDto = invokeCreateHarvest(inputDto);

                doCreateAssertions(outputDto.getId(), inputDto, author, actor);
            });
        })));
    }

    @Test
    public void testCreateHarvest_whenHarvestReportRequired() {
        withRhy(rhy -> withPerson(author -> {
            final GameSpecies species = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_BEAR);
            final LocalDate seasonBegin = DateUtil.today();
            final LocalDate seasonEnd = DateUtil.today().plusDays(1);
            model().newHarvestSeason(species, seasonBegin, seasonEnd, seasonEnd);

            onSavedAndAuthenticated(createUser(author), () -> {
                final HarvestDTO inputDto = create(species, 1)
                        .withPointOfTime(seasonBegin.toDateTimeAtStartOfDay().toLocalDateTime())
                        .build();
                final HarvestDTO outputDto = invokeCreateHarvest(inputDto);

                inputDto.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
                inputDto.setHarvestReportRequired(true);

                doCreateAssertions(outputDto.getId(), inputDto, author, author);
            });
        }));
    }

    @Test(expected = HarvestSpecimenValidationException.class)
    public void testCreateHarvest_whenSpecimenHasRequiredFields_noSpecimen() {
        withRhy(rhy -> withPerson(author -> {
            final GameSpecies species = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_BEAR);
            final LocalDate seasonBegin = DateUtil.today();
            final LocalDate seasonEnd = DateUtil.today().plusDays(1);
            model().newHarvestSeason(species, seasonBegin, seasonEnd, seasonEnd);

            onSavedAndAuthenticated(createUser(author), () -> {
                final HarvestDTO inputDto = create(species)
                        .withPointOfTime(seasonBegin.toDateTimeAtStartOfDay().toLocalDateTime())
                        .build();

                invokeCreateHarvest(inputDto);
            });
        }));
    }

    @Test
    public void testCreateHarvest_forPermit() {
        withPerson(author -> {
            final GameSpecies species = model().newGameSpecies();
            final HarvestPermit permit = model().newHarvestPermit();
            model().newHarvestPermitSpeciesAmount(permit, species);

            onSavedAndAuthenticated(createUser(author), () -> {
                final HarvestDTO inputDto = create(species, 1)
                        .withPermitNumber(permit.getPermitNumber())
                        .build();
                final HarvestDTO outputDto = invokeCreateHarvest(inputDto);

                inputDto.setStateAcceptedToHarvestPermit(StateAcceptedToHarvestPermit.PROPOSED);
                inputDto.setHarvestReportState(null);
                inputDto.setHarvestReportRequired(false);

                doCreateAssertions(outputDto.getId(), inputDto, author, author);
            });
        });
    }

    @Test
    public void testCreateHarvest_forPermit_asContactPerson() {
        withPerson(contactPerson -> {
            final GameSpecies species = model().newGameSpecies();
            final HarvestPermit permit = model().newHarvestPermit(contactPerson);
            model().newHarvestPermitSpeciesAmount(permit, species);

            onSavedAndAuthenticated(createUser(contactPerson), () -> {
                final HarvestDTO inputDto = create(species, 1)
                        .withPermitNumber(permit.getPermitNumber())
                        .build();
                final HarvestDTO outputDto = invokeCreateHarvest(inputDto);

                inputDto.setStateAcceptedToHarvestPermit(StateAcceptedToHarvestPermit.ACCEPTED);
                inputDto.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
                inputDto.setHarvestReportRequired(true);

                doCreateAssertions(outputDto.getId(), inputDto, contactPerson, contactPerson);
            });
        });
    }

    @Test
    public void testCreateHarvest_forPermit_asModerator() {
        withPerson(author -> withPerson(actor -> {
            final GameSpecies species = model().newGameSpecies(true);
            final HarvestPermit permit = model().newHarvestPermit();
            model().newHarvestPermitSpeciesAmount(permit, species);

            onSavedAndAuthenticated(createNewModerator(), () -> {

                final HarvestDTO inputDto = create(species, 5)
                        .withPermitNumber(permit.getPermitNumber())
                        .withAuthorInfo(author)
                        .withActorInfo(actor)
                        .withModeratorChangeReason("reason")
                        .build();
                final HarvestDTO outputDto = invokeCreateHarvest(inputDto);

                inputDto.setDescription(null);
                inputDto.setStateAcceptedToHarvestPermit(StateAcceptedToHarvestPermit.ACCEPTED);
                inputDto.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
                inputDto.setHarvestReportRequired(true);

                doCreateAssertions(outputDto.getId(), inputDto, author, actor);
            });
        }));
    }

    @Test
    public void testCreateGreySealForPermit() {
        withPerson(author -> withPerson(actor -> {
            final GameSpecies species = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_GREY_SEAL);
            final HarvestPermit permit = model().newHarvestPermit();
            model().newHarvestPermitSpeciesAmount(permit, species);

            onSavedAndAuthenticated(createNewModerator(), () -> {

                final HarvestDTO inputDto = create(species, 1)
                        .withPermitNumber(permit.getPermitNumber())
                        .withAuthorInfo(author)
                        .withActorInfo(actor)
                        .withModeratorChangeReason("reason")
                        .withHuntingMethod(HuntingMethod.SHOT)
                        .build();
                final HarvestDTO outputDto = invokeCreateHarvest(inputDto);

                inputDto.setDescription(null);
                inputDto.setStateAcceptedToHarvestPermit(StateAcceptedToHarvestPermit.ACCEPTED);
                inputDto.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
                inputDto.setHarvestReportRequired(true);

                doCreateAssertions(outputDto.getId(), inputDto, author, actor);
            });
        }));
    }

    @Test(expected = HarvestPermitNotFoundException.class)
    public void testCreateHarvest_whenPermitNotFound() {
        model().newRiistanhoitoyhdistys();
        final GameSpecies species = model().newGameSpecies();
        final String invalidPermitNumber = model().permitNumber();

        onSavedAndAuthenticated(createUserWithPerson(), () -> {
            invokeCreateHarvest(create(species, 1).withPermitNumber(invalidPermitNumber).build());
        });
    }

    @Test
    public void testCreateHarvest_ignoreMoosePermit() {
        withPerson(author -> {
            final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
            final GameSpecies moose = model().newGameSpeciesMoose();
            final HarvestPermit permit = model().newMooselikePermit(rhy);
            model().newHarvestPermitSpeciesAmount(permit, moose);

            onSavedAndAuthenticated(createUser(author), () -> {
                final HarvestDTO inputDto = create(moose, 1)
                        .withPermitNumber(permit.getPermitNumber())
                        .build();
                final HarvestDTO outputDto = invokeCreateHarvest(inputDto);

                inputDto.setPermitNumber(null);
                inputDto.setStateAcceptedToHarvestPermit(null);
                inputDto.setHarvestReportState(null);
                inputDto.setHarvestReportRequired(false);

                doCreateAssertions(outputDto.getId(), inputDto, author, author);
            });
        });
    }

    @Test(expected = EndOfHuntingReportExistsException.class)
    public void testCreateHarvest_whenPermitEndOfHuntingReportDone() {
        final GameSpecies species = model().newGameSpecies();
        final HarvestPermit permit = model().newHarvestPermit();
        model().newHarvestPermitSpeciesAmount(permit, species);

        permit.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
        permit.setHarvestReportAuthor(permit.getOriginalContactPerson());
        permit.setHarvestReportDate(DateUtil.now());
        permit.setHarvestReportModeratorOverride(false);

        persistAndAuthenticateWithNewUser(true);

        invokeCreateHarvest(create(species, 1)
                .withPermitNumber(permit.getPermitNumber())
                .build());
    }

    @Test(expected = HarvestPermitSpeciesAmountNotFound.class)
    public void testCreateHarvestForPermit_whenSpeciesIsNotValid() {
        final GameSpecies species = model().newGameSpecies(true);

        final HarvestPermit permit = model().newHarvestPermit(true);
        final HarvestPermitSpeciesAmount amount = model().newHarvestPermitSpeciesAmount(permit, species);

        persistAndAuthenticateWithNewUser(true);

        invokeCreateHarvest(create(species, 1)
                .withPointOfTime(amount.getBeginDate().minusDays(1).toLocalDateTime(LocalTime.MIDNIGHT))
                .withPermitNumber(permit.getPermitNumber())
                .build());
    }

    @Test(expected = RhyNotResolvableByGeoLocationException.class)
    public void testCreateHarvestForPermit_whenRhyNotFound() {
        final GameSpecies species = model().newGameSpecies(true);

        final HarvestPermit permit = model().newHarvestPermit(true);
        final HarvestPermitSpeciesAmount amount = model().newHarvestPermitSpeciesAmount(permit, species);

        persistAndAuthenticateWithNewUser(true);

        // everything else should be fine, but rhy is not found for location
        invokeCreateHarvest(create(species, 1)
                .withGeoLocation(MockGISQueryService.RHY_GEOLOCATION_NOT_FOUND)
                .withPermitNumber(permit.getPermitNumber())
                .withPointOfTime(amount.getBeginDate().toLocalDateTime(LocalTime.MIDNIGHT))
                .build());
    }

    @Test(expected = ClubHuntingFinishedException.class)
    public void testCreateHarvestForHuntingDay_whenHuntingFinished() {
        withMooseHuntingGroupFixture(f -> {

            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today());

            // Intermediary flush needed before persisting MooseHuntingSummary in order to have
            // harvest_permit_partners table populated required for foreign key constraint.
            persistInNewTransaction();

            // Set club hunting finished.
            model().newMooseHuntingSummary(f.permit, f.club, true);

            onSavedAndAuthenticated(createUser(f.clubContact), () -> {
                invokeCreateHarvest(create(f.species, 1).linkToHuntingDay(huntingDay).build());
            });
        });
    }

    @Test(expected = PointOfTimeOutsideOfHuntingDayException.class)
    public void testCreateHarvestForHuntingDay_whenPointOfTimeOutsideOfHuntingDay() {
        withMooseHuntingGroupFixture(f -> {

            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today());

            onSavedAndAuthenticated(createUser(f.clubContact), () -> {
                invokeCreateHarvest(create(f.species, 1)
                        .linkToHuntingDay(huntingDay)
                        .withPointOfTime(huntingDay.getStartAsLocalDateTime().minusMinutes(1))
                        .build());
            });
        });
    }

    @Test(expected = PointOfTimeOutsideOfPermittedDatesException.class)
    public void testCreateHarvestForHuntingDay_whenPointOfTimeOutsideOfPermitted() {
        withMooseHuntingGroupFixture(f -> {

            final LocalDate date = f.speciesAmount.getBeginDate().minusDays(1);
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, date);

            onSavedAndAuthenticated(createUser(f.clubContact), () -> {
                invokeCreateHarvest(create(f.species, 1)
                        .linkToHuntingDay(huntingDay)
                        .withPointOfTime(date.toLocalDateTime(new LocalTime(12, 02)))
                        .build());
            });
        });
    }

    @Test
    public void testCreateHarvest_linkToHuntingDay_asModerator() {
        withMooseHuntingGroupFixture(fixture -> {
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(fixture.group, today());
            final Person author = fixture.groupMember;

            onSavedAndAuthenticated(createNewModerator(), () -> {

                final HarvestDTO inputDto = create(fixture.species, 1)
                        .withAuthorInfo(author)
                        .withActorInfo(author)
                        .linkToHuntingDay(huntingDay)
                        .withModeratorChangeReason("reason")
                        .build();
                final HarvestDTO outputDto = invokeCreateHarvest(inputDto);

                inputDto.setDescription(null);

                doCreateAssertions(
                        outputDto.getId(), inputDto, author, author, assertAcceptedToHuntingDay(null, huntingDay));
            });
        });
    }

    @Test
    public void testUpdateHarvest_linkToHuntingDay() {
        withMooseHuntingGroupFixture(fixture -> {
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(fixture.group, today());

            final Person author = fixture.groupMember;
            final Person acceptor = fixture.groupLeader;

            final Harvest harvest = model().newHarvest(fixture.species, author);
            harvest.setGeoLocation(fixture.zoneCentroid);

            final List<HarvestSpecimen> specimens = createSpecimens(harvest, 1);
            harvest.setAmount(specimens.size());

            onSavedAndAuthenticated(createUser(acceptor), () -> {

                final HarvestDTO dto = create(harvest)
                        .populateSpecimensWith(specimens)
                        .linkToHuntingDay(huntingDay)
                        .build();
                invokeUpdateHarvest(dto);

                doUpdateAssertions(dto, author, author, 1,
                        assertAcceptedToHuntingDay(acceptor, huntingDay));
            });
        });
    }

    @Test
    public void testUpdateHarvest_linkToHuntingDay_asModerator() {
        withMooseHuntingGroupFixture(fixture -> {
            final Person author = fixture.groupMember;

            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(fixture.group, today());

            final Harvest harvest = model().newHarvest(fixture.species, author, huntingDay.getStartDate());

            onSavedAndAuthenticated(createNewModerator(), () -> {

                final HarvestDTO dto = create(harvest, 1)
                        .withAuthorInfo(harvest.getAuthor())
                        .withActorInfo(harvest.getActor())
                        .mutate().linkToHuntingDay(huntingDay).build();
                invokeUpdateHarvest(dto);

                dto.setDescription(harvest.getDescription());

                doUpdateAssertions(dto, author, author, 1, assertAcceptedToHuntingDay(null, huntingDay));
            });
        });
    }

    @Test
    public void testUpdateHarvest_whenAssociatedWithHuntingDay_asModerator() {
        withMooseHuntingGroupFixture(f -> {
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today());

            final Harvest harvest = model().newHarvest(f.species, f.clubContact, huntingDay.getStartDate());
            harvest.updateHuntingDayOfGroup(huntingDay, null);

            onSavedAndAuthenticated(createNewModerator(), () -> {

                final HarvestDTO dto = create(harvest, 1)
                        .withAuthorInfo(harvest.getAuthor())
                        .withActorInfo(harvest.getActor())
                        .mutate().linkToHuntingDay(huntingDay).build();
                invokeUpdateHarvest(dto);

                dto.setDescription(harvest.getDescription());

                doUpdateAssertions(dto, f.clubContact, f.clubContact, 1);
            });
        });
    }

    @Test
    public void testUpdateHarvest_linkToHuntingDay_acceptorNotChangedOnUpdate() {
        withMooseHuntingGroupFixture(fixture -> {
            final Person author = fixture.groupLeader;
            final Person acceptor = fixture.groupLeader;

            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(fixture.group, today());

            final Harvest harvest = model().newHarvest(fixture.species, author);
            final List<HarvestSpecimen> specimens = createSpecimens(harvest, 1);
            harvest.setAmount(specimens.size());

            final AtomicReference<HarvestDTO> updatedDto = new AtomicReference<>();

            onSavedAndAuthenticated(createUser(acceptor), () -> {
                final HarvestDTO dto = create(harvest)
                        .populateSpecimensWith(specimens)
                        .linkToHuntingDay(huntingDay)
                        .build();

                updatedDto.set(invokeUpdateHarvest(dto));

                doUpdateAssertions(dto, author, author, 1, assertAcceptedToHuntingDay(acceptor, huntingDay));
            });

            onSavedAndAuthenticated(createNewModerator(), () -> {
                final HarvestDTO dto = updatedDto.get();
                dto.setRev(1);

                invokeUpdateHarvest(dto);
                doUpdateAssertions(dto, author, author, 2, assertAcceptedToHuntingDay(acceptor, huntingDay));
            });
        });
    }

    @Test
    public void testUpdateHarvest_whenNoChanges() {
        withPerson(person -> {

            final Harvest harvest = model().newHarvest(model().newGameSpecies(true), person);
            final List<HarvestSpecimen> specimens = createSpecimens(harvest, 5);
            harvest.setAmount(specimens.size());

            onSavedAndAuthenticated(createUser(person), () -> {

                final HarvestDTO dto = create(harvest).populateSpecimensWith(specimens).build();
                invokeUpdateHarvest(dto);

                doUpdateAssertions(dto, person, person, 0);
            });
        });
    }

    @Test
    public void testUpdateHarvest_whenActorChanged() {
        withPerson(author -> withPerson(actor -> {

            final Harvest harvest = model().newHarvest(model().newGameSpecies(true), author);
            final List<HarvestSpecimen> specimens = createSpecimens(harvest, 5);
            harvest.setAmount(specimens.size());

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO dto = create(harvest).withActorInfo(actor).populateSpecimensWith(specimens).build();
                invokeUpdateHarvest(dto);

                doUpdateAssertions(dto, author, actor, 1);
            });
        }));
    }

    @Test
    public void testUpdateHarvest_whenHarvestReportNotDone() {
        withRhy(rhy -> withPerson(person -> {
            final Harvest harvest = model().newHarvest(model().newGameSpecies(true), person);
            final GameSpecies newSpecies = model().newGameSpecies(true);

            onSavedAndAuthenticated(createUser(person), () -> {
                final HarvestDTO dto = create(harvest, newSpecies, 10).mutate().build();
                invokeUpdateHarvest(dto);
                doUpdateAssertions(dto, person, person, 1, h -> assertNotNull(h.getRhy()));
            });
        }));
    }

    @Test
    public void testUpdateHarvest_whenSwitchingToSpeciesRequiringHarvestReport() {
        withRhy(rhy -> withPerson(person -> {
            final GameSpecies oldSpecies = model().newGameSpecies();
            final GameSpecies newSpecies = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_BEAR);
            final LocalDate seasonBegin = DateUtil.today();
            final LocalDate seasonEnd = DateUtil.today().plusDays(1);
            model().newHarvestSeason(newSpecies, seasonBegin, seasonEnd, seasonEnd);
            final Harvest harvest = model().newHarvest(oldSpecies, person);

            onSavedAndAuthenticated(createUser(person), () -> {
                final HarvestDTO dto = create(harvest, 1)
                        .populateWith(newSpecies)
                        .withPointOfTime(seasonBegin.toDateTimeAtStartOfDay().toLocalDateTime())
                        .build();
                invokeUpdateHarvest(dto);

                dto.setHarvestReportRequired(true);
                dto.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);

                doUpdateAssertions(dto, person, person, 1);
            });
        }));
    }

    @Test(expected = HarvestReportingTypeChangeForbiddenException.class)
    public void testUpdateHarvest_whenHarvestReportDone_changeDateOutsideSeason_asModerator() {
        withRhy(rhy -> withPerson(author -> {
            final GameSpecies species = model().newGameSpecies(true);
            final LocalDate seasonBegin = DateUtil.today();
            final LocalDate seasonEnd = DateUtil.today().plusDays(1);
            final HarvestSeason season = model().newHarvestSeason(species, seasonBegin, seasonEnd, seasonEnd);

            final Harvest harvest = model().newHarvest(species, author);
            harvest.setPointOfTime(seasonBegin.toDateTimeAtStartOfDay().toDate());
            harvest.setHarvestSeason(season);
            harvest.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
            harvest.setHarvestReportAuthor(harvest.getAuthor());
            harvest.setHarvestReportDate(DateUtil.now());

            final List<HarvestSpecimen> specimens = createSpecimens(harvest, 5);
            harvest.setAmount(specimens.size());

            onSavedAndAuthenticated(createNewModerator(), () -> {
                final HarvestDTO dto = create(harvest, 10)
                        .withAuthorInfo(harvest.getAuthor())
                        .withActorInfo(harvest.getActor())
                        .withPointOfTime(seasonBegin.minusDays(1).toDateTimeAtStartOfDay().toLocalDateTime())
                        .build();

                invokeUpdateHarvest(dto);
            });
        }));
    }

    @Test
    public void testUpdateHarvest_whenHarvestReportDone() {
        withRhy(rhy -> withPerson(person -> {

            final Harvest harvest = model().newHarvest(model().newGameSpecies(true), person);
            final List<HarvestSpecimen> specimens = createSpecimens(harvest, 5);
            harvest.setAmount(specimens.size());

            harvest.setHarvestReportState(HarvestReportState.APPROVED);
            harvest.setHarvestReportAuthor(harvest.getAuthor());
            harvest.setHarvestReportDate(DateUtil.now());

            final GameSpecies newSpecies = model().newGameSpecies(true);

            onSavedAndAuthenticated(createUser(person), () -> {

                final HarvestDTO dto = create(harvest, newSpecies, 10).mutate().build();

                invokeUpdateHarvest(dto);

                final HarvestDTO expectedValues = create(harvest)
                        .withDescription(dto.getDescription())
                        .populateSpecimensWith(specimens)
                        .build();

                doUpdateAssertions(expectedValues, person, person, 1, h -> assertNull(h.getRhy()));
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

                final HarvestDTO dto = create(harvest).populateSpecimensWith(specimens).mutateSpecimens().build();
                invokeUpdateHarvest(dto);

                doUpdateAssertions(dto, author, author, 1);
            });
        });
    }

    @Test
    public void testUpdateHarvest_forAmountChange_whenSpecimensPresent() {
        withPerson(person -> {

            final Harvest harvest = model().newHarvest(model().newGameSpecies(true), person);
            final List<HarvestSpecimen> specimens = createSpecimens(harvest, 5);
            harvest.setAmount(specimens.size());

            onSavedAndAuthenticated(createUser(person), () -> {

                final HarvestDTO dto = create(harvest)
                        .populateSpecimensWith(specimens)
                        .withAmount(specimens.size() + 10)
                        .build();

                invokeUpdateHarvest(dto);

                doUpdateAssertions(dto, person, person, 1);
            });
        });
    }

    @Test
    public void testUpdateHarvest_forAmountChange_whenSpecimensNotPresent() {
        withPerson(person -> {

            final Harvest harvest = model().newHarvest(model().newGameSpecies(true), person);

            onSavedAndAuthenticated(createUser(person), () -> {

                final HarvestDTO dto = create(harvest).withAmount(10).build();
                invokeUpdateHarvest(dto);

                doUpdateAssertions(dto, person, person, 1);
            });
        });
    }

    // Test that harvest is not mutated (except for description/images) when hunting is finished.
    @Test
    public void testUpdateHarvest_whenHuntingFinished() {
        withMooseHuntingGroupFixture(f -> {

            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today());

            final Harvest harvest = model().newHarvest(f.species, f.clubContact, huntingDay.getStartDate());
            harvest.updateHuntingDayOfGroup(huntingDay, null);

            final GroupHuntingDay huntingDay2 = model().newGroupHuntingDay(f.group, today().minusDays(1));

            // Intermediary flush needed before persisting MooseHuntingSummary in order to have
            // harvest_permit_partners table populated required for foreign key constraint.
            persistInNewTransaction();

            // Set club hunting finished.
            model().newMooseHuntingSummary(f.permit, f.club, true);

            final GameSpecies newSpecies = model().newDeerSubjectToClubHunting();

            onSavedAndAuthenticated(createUser(f.clubContact), () -> {

                final HarvestDTO dto = create(harvest, newSpecies, 1)
                        .mutate()
                        .linkToHuntingDay(huntingDay2)
                        .build();

                invokeUpdateHarvest(dto);

                final HarvestDTO expectedValues = create(harvest).withDescription(dto.getDescription()).build();

                doUpdateAssertions(expectedValues, f.clubContact, f.clubContact, 1, h -> assertNull(h.getRhy()));
            });
        });
    }

    // Test that harvest is not mutated (except for description/images) when group is created
    // within moose data card import.
    @Test
    public void testUpdateHarvest_whenGroupOriginatingFromMooseDataCard_asClubContact() {
        testUpdateHarvest_whenGroupOriginatingFromMooseDataCard(false);
    }

    @Test
    public void testUpdateHarvest_whenGroupOriginatingFromMooseDataCard_asModerator() {
        testUpdateHarvest_whenGroupOriginatingFromMooseDataCard(true);
    }

    private void testUpdateHarvest_whenGroupOriginatingFromMooseDataCard(final boolean moderator) {
        withMooseHuntingGroupFixture(f -> {
            f.group.setFromMooseDataCard(true);

            final LocalDate today = today();
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today);

            final Harvest harvest = model().newHarvest(f.species, f.clubContact, huntingDay);

            onSavedAndAuthenticated(moderator ? createNewModerator() : createUser(f.clubContact), () -> {

                final HarvestDTO dto = create(harvest, 1)
                        .mutate(huntingDay)
                        .populateWith(f.species)
                        .withAuthorInfo(f.groupMember)
                        .withActorInfo(f.groupMember)
                        .build();

                invokeUpdateHarvest(dto);

                final HarvestDTO expectedValues =
                        moderator ? create(dto).withDescription(harvest.getDescription()).build() : create(harvest)
                                .withDescription(dto.getDescription()).build();

                final Person expectedAuthor = moderator ? f.groupMember : f.clubContact;

                doUpdateAssertions(expectedValues, expectedAuthor, expectedAuthor, 1);
            });
        });
    }

    @Test
    public void testUpdateHarvest_whenProposedForPermit_asAuthor() {
        withPerson(author -> {
            final GameSpecies species = model().newGameSpecies();
            final HarvestPermit permit = model().newHarvestPermit();
            model().newHarvestPermitSpeciesAmount(permit, species);

            final Harvest harvest = model().newHarvest(species, author);
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(StateAcceptedToHarvestPermit.PROPOSED);
            harvest.setRhy(permit.getRhy());

            onSavedAndAuthenticated(createUser(author), () -> {
                final HarvestDTO dto = create(harvest).mutate().build();
                invokeUpdateHarvest(dto);

                dto.setStateAcceptedToHarvestPermit(StateAcceptedToHarvestPermit.PROPOSED);
                dto.setHarvestReportState(null);
                dto.setHarvestReportRequired(false);

                doUpdateAssertions(dto, author, author, 1);
            });
        });
    }

    @Test
    public void testUpdateHarvest_whenProposedForPermit_asContactPerson() {
        withPerson(contactPerson -> {
            final GameSpecies species = model().newGameSpecies();
            final HarvestPermit permit = model().newHarvestPermit(contactPerson);
            model().newHarvestPermitSpeciesAmount(permit, species);

            final Harvest harvest = model().newHarvest(species, contactPerson);
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(StateAcceptedToHarvestPermit.PROPOSED);
            harvest.setRhy(permit.getRhy());

            onSavedAndAuthenticated(createUser(contactPerson), () -> {
                final HarvestDTO dto = create(harvest).mutate().build();
                invokeUpdateHarvest(dto);

                dto.setStateAcceptedToHarvestPermit(StateAcceptedToHarvestPermit.ACCEPTED);
                dto.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
                dto.setHarvestReportRequired(true);

                doUpdateAssertions(dto, contactPerson, contactPerson, 1);
            });
        });
    }


    @Test
    public void testUpdateHarvest_whenProposedForPermit_asModerator() {
        withPerson(contactPerson -> {
            final GameSpecies species = model().newGameSpecies();
            final HarvestPermit permit = model().newHarvestPermit(contactPerson);
            model().newHarvestPermitSpeciesAmount(permit, species);

            final Harvest harvest = model().newHarvest(species, contactPerson);
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(StateAcceptedToHarvestPermit.PROPOSED);
            harvest.setRhy(permit.getRhy());

            final Person newAuthor = model().newPerson();
            final Person newActor = model().newPerson();

            onSavedAndAuthenticated(createNewModerator(), () -> {
                final HarvestDTO dto = create(harvest)
                        .withAuthorInfo(newAuthor)
                        .withActorInfo(newActor)
                        .withModeratorChangeReason("reason")
                        .mutateSpecimens()
                        .build();
                invokeUpdateHarvest(dto);

                dto.setStateAcceptedToHarvestPermit(StateAcceptedToHarvestPermit.ACCEPTED);
                dto.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
                dto.setHarvestReportRequired(true);

                doUpdateAssertions(dto, newAuthor, newActor, 1);
            });
        });
    }

    // FIXME: Should not fail silently?
    @Test
    public void testUpdateHarvest_whenAcceptedForPermit_asAuthor() {
        withPerson(author -> {
            final GameSpecies species = model().newGameSpecies();
            final HarvestPermit permit = model().newHarvestPermit();
            model().newHarvestPermitSpeciesAmount(permit, species);

            final Harvest harvest = model().newHarvest(species, author);
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(StateAcceptedToHarvestPermit.ACCEPTED);
            harvest.setRhy(permit.getRhy());

            onSavedAndAuthenticated(createUser(author), () -> {
                final HarvestDTO dto = create(harvest).mutateSpecimens().build();
                invokeUpdateHarvest(dto);

                doUpdateAssertions(dto, author, author, 0);
            });
        });
    }

    @Test
    public void testUpdateHarvest_whenAcceptedForPermit_asContactPerson() {
        withPerson(contactPerson -> {
            final GameSpecies species = model().newGameSpecies();
            final HarvestPermit permit = model().newHarvestPermit(contactPerson);
            model().newHarvestPermitSpeciesAmount(permit, species);

            final Harvest harvest = model().newHarvest(species, contactPerson);
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(StateAcceptedToHarvestPermit.ACCEPTED);
            harvest.setRhy(permit.getRhy());

            onSavedAndAuthenticated(createUser(contactPerson), () -> {
                final HarvestDTO dto = create(harvest).mutateSpecimens().build();
                invokeUpdateHarvest(dto);

                dto.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
                dto.setHarvestReportRequired(true);

                doUpdateAssertions(dto, contactPerson, contactPerson, 1);
            });
        });
    }

    @Test
    public void testUpdateHarvest_whenAcceptedForPermit_asModerator() {
        withPerson(contactPerson -> {
            final GameSpecies species = model().newGameSpecies();
            final HarvestPermit permit = model().newHarvestPermit(contactPerson);
            model().newHarvestPermitSpeciesAmount(permit, species);

            final Harvest harvest = model().newHarvest(species, contactPerson);
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(StateAcceptedToHarvestPermit.ACCEPTED);
            harvest.setRhy(permit.getRhy());

            final Person newAuthor = model().newPerson();
            final Person newActor = model().newPerson();

            onSavedAndAuthenticated(createNewModerator(), () -> {
                final HarvestDTO dto = create(harvest)
                        .withAuthorInfo(newAuthor)
                        .withActorInfo(newActor)
                        .withModeratorChangeReason("reason")
                        .mutateSpecimens()
                        .build();
                invokeUpdateHarvest(dto);

                dto.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
                dto.setHarvestReportRequired(true);

                doUpdateAssertions(dto, newAuthor, newActor, 1);
            });
        });
    }

    @Test
    public void testUpdateHarvest_addPermit_asAuthor() {
        withPerson(author -> {
            final GameSpecies species = model().newGameSpecies();
            final HarvestPermit permit = model().newHarvestPermit();
            model().newHarvestPermitSpeciesAmount(permit, species);

            final Harvest harvest = model().newHarvest(species, author);

            onSavedAndAuthenticated(createUser(author), () -> {
                final HarvestDTO dto = create(harvest)
                        .withPermitNumber(permit.getPermitNumber())
                        .build();

                invokeUpdateHarvest(dto);
                dto.setStateAcceptedToHarvestPermit(StateAcceptedToHarvestPermit.PROPOSED);
                dto.setHarvestReportState(null);

                doUpdateAssertions(dto, author, author, 1);
            });
        });
    }

    @Test
    public void testUpdateHarvest_addPermit_asContactPerson() {
        withPerson(contactPerson -> {
            final GameSpecies species = model().newGameSpecies();
            final HarvestPermit permit = model().newHarvestPermit(contactPerson);
            model().newHarvestPermitSpeciesAmount(permit, species);

            final Harvest harvest = model().newHarvest(species, contactPerson);

            onSavedAndAuthenticated(createUser(contactPerson), () -> {
                final HarvestDTO dto = create(harvest).withPermitNumber(permit.getPermitNumber()).build();

                invokeUpdateHarvest(dto);

                dto.setStateAcceptedToHarvestPermit(StateAcceptedToHarvestPermit.ACCEPTED);
                dto.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
                dto.setHarvestReportRequired(true);

                doUpdateAssertions(dto, contactPerson, contactPerson, 1);
            });
        });
    }

    @Test(expected = HarvestPermitNotFoundException.class)
    public void testUpdateHarvest_addPermit_whenPermitNotFound() {
        withPerson(author -> {
            model().newRiistanhoitoyhdistys();
            final GameSpecies species = model().newGameSpecies();
            final String permitNumber = model().permitNumber();
            final Harvest harvest = model().newHarvest(species, author);

            onSavedAndAuthenticated(createUser(author), () -> {
                invokeUpdateHarvest(create(harvest)
                        .withPermitNumber(permitNumber)
                        .build());
            });
        });
    }

    @Test
    public void testUpdateHarvest_addPermit_ignoreMoosePermit() {
        withPerson(author -> {
            final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
            final GameSpecies moose = model().newGameSpeciesMoose();
            final HarvestPermit permit = model().newMooselikePermit(rhy);
            model().newHarvestPermitSpeciesAmount(permit, moose);

            final Harvest harvest = model().newHarvest(moose, author);

            onSavedAndAuthenticated(createUser(author), () -> {
                final HarvestDTO dto = create(harvest, 1)
                        .withPermitNumber(permit.getPermitNumber())
                        .build();
                invokeUpdateHarvest(dto);

                dto.setPermitNumber(null);
                dto.setStateAcceptedToHarvestPermit(null);
                dto.setHarvestReportState(null);
                dto.setHarvestReportRequired(false);

                doUpdateAssertions(dto, author, author, 1);
            });
        });
    }

    @Test(expected = RhyNotResolvableByGeoLocationException.class)
    public void testUpdateHarvest_addPermit_whenRhyNotFound() {
        withPerson(author -> {
            final GameSpecies species = model().newGameSpecies();
            final HarvestPermit permit = model().newHarvestPermit();
            model().newHarvestPermitSpeciesAmount(permit, species);

            final Harvest harvest = model().newHarvest(species, author);

            onSavedAndAuthenticated(createUser(author), () -> {

                // everything else should be fine, but rhy is not found for location
                invokeUpdateHarvest(create(harvest)
                        .withGeoLocation(MockGISQueryService.RHY_GEOLOCATION_NOT_FOUND)
                        .withPermitNumber(permit.getPermitNumber())
                        .build());
            });
        });
    }

    @Test(expected = HarvestPermitSpeciesAmountNotFound.class)
    public void testUpdateHarvest_addPermit_whenSpeciesIsNotValid() {
        withPerson(author -> {
            final GameSpecies species = model().newGameSpecies();
            final HarvestPermit permit = model().newHarvestPermit();
            final HarvestPermitSpeciesAmount amount = model().newHarvestPermitSpeciesAmount(permit, species);
            final Harvest harvest = model().newHarvest(species, author);

            onSavedAndAuthenticated(createUser(author), () -> {

                invokeUpdateHarvest(create(harvest)
                        .withPermitNumber(permit.getPermitNumber())
                        .withPointOfTime(amount.getBeginDate().minusDays(1).toLocalDateTime(LocalTime.MIDNIGHT))
                        .build());
            });
        });
    }

    @Test(expected = EndOfHuntingReportExistsException.class)
    public void testUpdateHarvest_addPermit_whenPermitEndOfHuntingReportDone() {
        withPerson(author -> {
            final GameSpecies species = model().newGameSpecies();
            final HarvestPermit permit = model().newHarvestPermit();
            model().newHarvestPermitSpeciesAmount(permit, species);
            permit.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
            permit.setHarvestReportAuthor(permit.getOriginalContactPerson());
            permit.setHarvestReportDate(DateUtil.now());
            permit.setHarvestReportModeratorOverride(false);

            final Harvest harvest = model().newHarvest(species, author);

            onSavedAndAuthenticated(createUser(author), () -> {
                final HarvestDTO dto = create(harvest)
                        .withPermitNumber(permit.getPermitNumber())
                        .build();

                invokeUpdateHarvest(dto);
            });
        });
    }

    @Test
    public void testUpdateHarvest_removePermit_whenRejectedForPermit() {
        withPerson(author -> {
            final GameSpecies species = model().newGameSpecies();
            final HarvestPermit permit = model().newHarvestPermit();
            model().newHarvestPermitSpeciesAmount(permit, species);

            final Harvest harvest = model().newHarvest(species, author);
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(StateAcceptedToHarvestPermit.REJECTED);
            harvest.setRhy(permit.getRhy());

            onSavedAndAuthenticated(createUser(author), () -> {
                final HarvestDTO dto = create(harvest).withPermitNumber(null).build();
                invokeUpdateHarvest(dto);

                dto.setStateAcceptedToHarvestPermit(null);
                dto.setHarvestReportRequired(false);

                doUpdateAssertions(dto, author, author, 1);
            });
        });
    }

    @Test
    public void testUpdateHarvest_removePermit_whenAcceptedForPermit() {
        withPerson(author -> {
            final GameSpecies species = model().newGameSpecies();
            final HarvestPermit permit = model().newHarvestPermit();
            model().newHarvestPermitSpeciesAmount(permit, species);

            final Harvest harvest = model().newHarvest(species, author);
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(StateAcceptedToHarvestPermit.ACCEPTED);
            harvest.setRhy(permit.getRhy());
            harvest.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
            harvest.setHarvestReportDate(DateUtil.now());
            harvest.setHarvestReportAuthor(author);
            harvest.setHarvestReportRequired(false);

            onSavedAndAuthenticated(createUser(author), () -> {
                final HarvestDTO dto = create(harvest).withPermitNumber(null).build();
                invokeUpdateHarvest(dto);

                dto.setPermitNumber(permit.getPermitNumber());

                doUpdateAssertions(dto, author, author, 0);
            });
        });
    }

    @Test
    public void testUpdateHarvest_changePermit_whenProposedForPermit_asAuthor() {
        withPerson(author -> {
            final GameSpecies species = model().newGameSpecies();
            final HarvestPermit permit = model().newHarvestPermit();
            model().newHarvestPermitSpeciesAmount(permit, species);

            final HarvestPermit otherPermit = model().newHarvestPermit();
            model().newHarvestPermitSpeciesAmount(otherPermit, species);

            final Harvest harvest = model().newHarvest(species, author);
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(StateAcceptedToHarvestPermit.PROPOSED);
            harvest.setRhy(permit.getRhy());

            onSavedAndAuthenticated(createUser(author), () -> {
                final HarvestDTO dto = create(harvest).withPermitNumber(otherPermit.getPermitNumber()).build();

                invokeUpdateHarvest(dto);

                dto.setStateAcceptedToHarvestPermit(StateAcceptedToHarvestPermit.PROPOSED);

                doUpdateAssertions(dto, author, author, 1);
            });
        });
    }

    @Test
    public void testUpdateHarvest_changePermit_whenProposedForPermit_asContactPerson() {
        withPerson(contactPerson -> {
            final GameSpecies species = model().newGameSpecies();
            final HarvestPermit permit = model().newHarvestPermit();
            model().newHarvestPermitSpeciesAmount(permit, species);

            final HarvestPermit otherPermit = model().newHarvestPermit(contactPerson);
            model().newHarvestPermitSpeciesAmount(otherPermit, species);

            final Harvest harvest = model().newHarvest(species, contactPerson);
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(StateAcceptedToHarvestPermit.PROPOSED);
            harvest.setRhy(permit.getRhy());

            onSavedAndAuthenticated(createUser(contactPerson), () -> {
                final HarvestDTO dto = create(harvest).withPermitNumber(otherPermit.getPermitNumber()).build();

                invokeUpdateHarvest(dto);

                dto.setStateAcceptedToHarvestPermit(StateAcceptedToHarvestPermit.ACCEPTED);
                dto.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
                dto.setHarvestReportRequired(true);

                doUpdateAssertions(dto, contactPerson, contactPerson, 1);
            });
        });
    }

    // FIXME: Should not fail silently?
    @Test
    public void testUpdateHarvest_changePermit_whenPermitEndOfHuntingReportDone_asContactPerson() {
        withPerson(contactPerson -> {
            final GameSpecies species = model().newGameSpecies();
            final HarvestPermit permit = model().newHarvestPermit(contactPerson);
            model().newHarvestPermitSpeciesAmount(permit, species);

            final HarvestPermit otherPermit = model().newHarvestPermit(contactPerson);
            model().newHarvestPermitSpeciesAmount(otherPermit, species);

            permit.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
            permit.setHarvestReportAuthor(contactPerson);
            permit.setHarvestReportDate(DateUtil.now());
            permit.setHarvestReportModeratorOverride(false);

            final Harvest harvest = model().newHarvest(species, contactPerson);
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(StateAcceptedToHarvestPermit.ACCEPTED);
            harvest.setRhy(permit.getRhy());
            harvest.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
            harvest.setHarvestReportAuthor(contactPerson);
            harvest.setHarvestReportDate(DateUtil.now());
            harvest.setHarvestReportRequired(true);

            onSavedAndAuthenticated(createUser(contactPerson), () -> {
                final HarvestDTO dto = create(harvest).withPermitNumber(otherPermit.getPermitNumber()).build();

                invokeUpdateHarvest(dto);

                dto.setPermitNumber(permit.getPermitNumber());

                doUpdateAssertions(dto, contactPerson, contactPerson, 0);
            });
        });
    }

    @Test
    public void testUpdateHarvest_whenNonHunters() {
        withPerson(person -> withPerson(person1 -> withPerson(person2 -> {
            person.setHunterNumber(null);
            person1.setHunterNumber(null);
            person2.setHunterNumber(null);

            final Harvest harvest = model().newHarvest(model().newGameSpecies(true), person);

            onSavedAndAuthenticated(createUser(person), () -> {

                // Usually actual shooter always has hunter number, but does not when you are non-hunter who added
                // harvest to yourself
                final HarvestDTO dto = create(harvest, 5).mutate().withActorInfo(person).build();
                invokeUpdateHarvest(dto);

                doUpdateAssertions(dto, person, person, 1);
            });
        })));
    }

    @Test
    public void testUpdateHarvest_withHarvestAcceptedToPermit_asContactPerson() {
        withHarvestHavingPermitState(StateAcceptedToHarvestPermit.ACCEPTED, harvest -> {
            harvest.getHarvestPermit().setOriginalContactPerson(harvest.getAuthor());
            doTestUpdateHarvestWithPermit(true, true, StateAcceptedToHarvestPermit.ACCEPTED, harvest);
        });
    }

    @Test
    public void testUpdateHarvest_withHarvestAcceptedToPermit() {
        withHarvestHavingPermitState(StateAcceptedToHarvestPermit.ACCEPTED, harvest -> {
            doTestUpdateHarvestWithPermit(false, true, StateAcceptedToHarvestPermit.ACCEPTED, harvest);
        });
    }

    @Test
    public void testUpdateHarvest_withHarvestProposedToPermit() {
        withHarvestHavingPermitState(StateAcceptedToHarvestPermit.PROPOSED, harvest -> {
            doTestUpdateHarvestWithPermit(true, false, StateAcceptedToHarvestPermit.PROPOSED, harvest);
        });
    }

    @Test
    public void testUpdateHarvest_withHarvestRejectedToPermit() {
        withHarvestHavingPermitState(StateAcceptedToHarvestPermit.REJECTED, harvest -> {
            doTestUpdateHarvestWithPermit(true, false, StateAcceptedToHarvestPermit.PROPOSED, harvest);
        });
    }

    private void doTestUpdateHarvestWithPermit(final boolean expectBusinessFieldsUpdated,
                                               final boolean expectedHarvestReportRequired,
                                               final StateAcceptedToHarvestPermit expectedStateAfterUpdate,
                                               final Harvest harvest) {

        onSavedAndAuthenticated(createUser(harvest.getAuthor()), () -> {
            final String permitNumber = harvest.getHarvestPermit().getPermitNumber();

            final HarvestDTO dto = create(harvest, 5).mutate().withPermitNumber(permitNumber).build();
            invokeUpdateHarvest(dto);

            final HarvestDTO expectedValues = expectBusinessFieldsUpdated
                    ? create(dto)
                    .withStateAcceptedToHarvestPermit(expectedStateAfterUpdate)
                    .withHarvestReportRequired(expectedHarvestReportRequired)
                    .build()
                    : create(harvest)
                    .withDescription(dto.getDescription())
                    .withPermitNumber(permitNumber)
                    .withStateAcceptedToHarvestPermit(expectedStateAfterUpdate)
                    .build();

            doUpdateAssertions(expectedValues, harvest.getAuthor(), harvest.getActor(), 1);
        });
    }

    @Test
    public void testUpdateHarvest_forAuthorChangedWhenNormalUser() {
        withPerson(author -> withPerson(newAuthor -> {
            final Harvest harvest = model().newHarvest(author);

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO dto = create(harvest, 1)
                        .mutate()
                        .withGeoLocation(harvest.getGeoLocation())
                        .withAuthorInfo(newAuthor)
                        .build();

                invokeUpdateHarvest(dto);

                doUpdateAssertions(dto, author, author, 1);
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
                final Harvest updatedHarvest = harvestRepo.getOne(harvest.getId());
                assertEquals(description, updatedHarvest.getDescription());

                assertEquals(1, updatedHarvest.getImages().size());
                final GameDiaryImage updatedImage = updatedHarvest.getImages().iterator().next();
                assertEquals(image.getFileMetadata().getId(), updatedImage.getFileMetadata().getId());
            });
        });
    }

    @Test
    public void testDeleteHarvest_whenHarvestReportNotDone() {
        withPerson(author -> {
            final Harvest harvest = model().newHarvest(author);

            model().newHarvestSpecimen(harvest);
            model().newGameDiaryImage(harvest);

            doTestDeleteHarvest(true, harvest, createUser(author));
        });
    }

    @Test
    public void testDeleteHarvest_whenHarvestReportApproved() {
        withPerson(author -> {
            final Harvest harvest = model().newHarvest(author);
            harvest.setHarvestReportState(HarvestReportState.APPROVED);
            harvest.setHarvestReportAuthor(harvest.getAuthor());
            harvest.setHarvestReportDate(DateUtil.now());

            model().newHarvestSpecimen(harvest);

            thrown.expectMessage("Cannot delete harvest with an associated harvest report.");
            doTestDeleteHarvest(false, harvest, createUser(author));
        });
    }

    @Test
    public void testDeleteHarvest_withHarvestAcceptedToPermit_asContactPerson() {
        testDeleteHarvest_whenHarvestHasPermitProcessingState(true, StateAcceptedToHarvestPermit.ACCEPTED, harvest -> {
            harvest.getHarvestPermit().setOriginalContactPerson(harvest.getAuthor());
        });
    }

    @Test
    public void testDeleteHarvest_withHarvestAcceptedToPermit() {
        testDeleteHarvest_whenHarvestHasPermitProcessingState(false, StateAcceptedToHarvestPermit.ACCEPTED);
    }

    @Test
    public void testDeleteHarvest_withHarvestProposedToPermit() {
        testDeleteHarvest_whenHarvestHasPermitProcessingState(true, StateAcceptedToHarvestPermit.PROPOSED);
    }

    @Test
    public void testDeleteHarvest_withHarvestRejectedToPermit() {
        testDeleteHarvest_whenHarvestHasPermitProcessingState(true, StateAcceptedToHarvestPermit.REJECTED);
    }

    private void testDeleteHarvest_whenHarvestHasPermitProcessingState(final boolean shouldBeDeleted,
                                                                       final StateAcceptedToHarvestPermit state) {

        testDeleteHarvest_whenHarvestHasPermitProcessingState(shouldBeDeleted, state, harvest -> {
        });
    }

    private void testDeleteHarvest_whenHarvestHasPermitProcessingState(final boolean shouldBeDeleted,
                                                                       final StateAcceptedToHarvestPermit state,
                                                                       final Consumer<Harvest> consumer) {
        withHarvestHavingPermitState(state, harvest -> {
            consumer.accept(harvest);

            if (!shouldBeDeleted) {
                thrown.expectMessage("Cannot delete harvest which is accepted to permit");
            }

            doTestDeleteHarvest(shouldBeDeleted, harvest, createUser(harvest.getAuthor()));
        });
    }

    @Test
    public void testDeleteHarvest_whenAttachedToHuntingDay() {
        clubGroupUserFunctionsBuilder().withAdminAndModerator(true).build().forEach(userFn -> {
            withMooseHuntingGroupFixture(f -> {
                final SystemUser user = userFn.apply(f.club, f.group);
                final Person author = user.isModeratorOrAdmin() ? f.groupMember : user.getPerson();

                final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today());
                final Harvest harvest = model().newHarvest(f.species, author, huntingDay);

                onSavedAndAuthenticated(user, () -> {
                    try {
                        feature.deleteHarvest(harvest.getId());
                        fail("Deletion of harvest associated with a hunting day should fail");
                    } catch (final RuntimeException e) {
                        if (!user.isModeratorOrAdmin() && e instanceof AccessDeniedException) {
                            fail("Should not have failed because of insufficient permissions");
                        }
                    }
                    assertNotNull(harvestRepo.findOne(harvest.getId()));
                });
            });

            reset();
        });
    }

    private void doTestDeleteHarvest(final boolean shouldBeDeleted, final Harvest harvest, final SystemUser user) {
        onSavedAndAuthenticated(user, () -> {
            if (!shouldBeDeleted) {
                thrown.expect(RuntimeException.class);
            }

            feature.deleteHarvest(harvest.getId());
            assertEquals(shouldBeDeleted, harvestRepo.findOne(harvest.getId()) == null);
        });
    }

    private HarvestDTO invokeCreateHarvest(final HarvestDTO input) {
        return withVersionChecked(feature.createHarvest(input));
    }

    private HarvestDTO invokeUpdateHarvest(final HarvestDTO input) {
        return withVersionChecked(feature.updateHarvest(input));
    }

    private HarvestDTO withVersionChecked(final HarvestDTO dto) {
        return checkDtoVersionAgainstEntity(dto, Harvest.class);
    }

    private List<HarvestSpecimen> createSpecimens(final Harvest harvest, final int numSpecimens) {
        return createList(numSpecimens, () -> model().newHarvestSpecimen(harvest));
    }

    private void withHarvestHavingPermitState(final StateAcceptedToHarvestPermit state,
                                              final Consumer<Harvest> consumer) {

        withRhy(rhy -> withPerson(author -> {
            final GameSpecies species = model().newGameSpecies(true);
            final HarvestPermit permit = model().newHarvestPermit(rhy, true);

            final Harvest harvest = model().newHarvest(species, author);
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(state);
            harvest.setRhy(rhy);

            model().newHarvestPermitSpeciesAmount(permit, species);

            consumer.accept(harvest);
        }));
    }

    protected void doCreateAssertions(final long harvestId,
                                      final HarvestDTO expectedValues,
                                      final Person expectedAuthor,
                                      final Person expectedActor) {

        doCreateAssertions(harvestId, expectedValues, expectedAuthor, expectedActor, h -> {
        });
    }

    protected void doCreateAssertions(final long harvestId,
                                      final HarvestDTO expectedValues,
                                      final Person expectedAuthor,
                                      final Person expectedActor,
                                      final Consumer<Harvest> additionalAssertions) {

        runInTransaction(() -> {
            final Harvest harvest = harvestRepo.findOne(harvestId);
            assertHarvestExpectations(harvest, expectedValues);

            validateAuthorAndActor(harvest, F.getId(expectedAuthor), F.getId(expectedActor));

            assertVersion(harvest, 0);
            assertEquals(expectedValues.getHarvestReportState(), harvest.getHarvestReportState());
            assertEquals(expectedValues.isHarvestReportRequired(), harvest.isHarvestReportRequired());

            additionalAssertions.accept(harvest);
        });
    }

    private void doUpdateAssertions(final HarvestDTO expectedValues,
                                    final Person expectedAuthor,
                                    final Person expectedActor,
                                    final int expectedRevision) {

        doUpdateAssertions(expectedValues, expectedAuthor, expectedActor, expectedRevision, h -> {
        });
    }

    private void doUpdateAssertions(final HarvestDTO expectedValues,
                                    final Person expectedAuthor,
                                    final Person expectedActor,
                                    final int expectedRevision,
                                    final Consumer<Harvest> additionalAssertions) {

        runInTransaction(() -> {
            final Harvest harvest = harvestRepo.findOne(expectedValues.getId());
            assertVersion(harvest, expectedRevision);
            assertHarvestExpectations(harvest, expectedValues);

            validateAuthorAndActor(harvest, F.getId(expectedAuthor), F.getId(expectedActor));

            additionalAssertions.accept(harvest);
        });
    }

    private void assertHarvestExpectations(final Harvest harvest, final HarvestDTO expectedValues) {
        assertCommonExpectations(harvest, expectedValues);
        assertEquals(Boolean.FALSE, harvest.getFromMobile());
        assertEquals(expectedValues.getAmount(), harvest.getAmount());

        final String actualPermitNumber =
                Optional.ofNullable(harvest.getHarvestPermit()).map(HarvestPermit::getPermitNumber).orElse(null);

        assertEquals(expectedValues.getPermitNumber(), actualPermitNumber);
        assertEquals(expectedValues.getStateAcceptedToHarvestPermit(), harvest.getStateAcceptedToHarvestPermit());

        assertEquals(expectedValues.isHarvestReportRequired(), harvest.isHarvestReportRequired());
        assertEquals(expectedValues.getHuntingAreaType(), harvest.getHuntingAreaType());
        assertEquals(expectedValues.getHuntingAreaSize(), harvest.getHuntingAreaSize());
        assertEquals(expectedValues.getHuntingParty(), harvest.getHuntingParty());
        assertEquals(expectedValues.getHuntingMethod(), harvest.getHuntingMethod());
        assertEquals(expectedValues.getReportedWithPhoneCall(), harvest.getReportedWithPhoneCall());
        assertEquals(expectedValues.getPermittedMethod(), harvest.getPermittedMethod());
        assertEquals(expectedValues.getFeedingPlace(), harvest.getFeedingPlace());
        assertEquals(expectedValues.getTaigaBeanGoose(), harvest.getTaigaBeanGoose());
        assertEquals(expectedValues.getLukeStatus(), harvest.getLukeStatus());

        Optional.ofNullable(expectedValues.getActorInfo())
                .ifPresent(actor -> assertEquals(actor.getId(), harvest.getActualShooter().getId()));

        assertEquals(expectedValues.getHuntingDayId(), F.getId(harvest.getHuntingDayOfGroup()));

        assertSpecimens(
                harvestSpecimenRepo.findByHarvest(harvest, new JpaSort(HarvestSpecimen_.id)),
                expectedValues.getSpecimens(),
                expectedValues.specimenOps()::equalContent);
    }
}

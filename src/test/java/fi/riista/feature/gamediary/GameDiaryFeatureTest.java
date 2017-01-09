package fi.riista.feature.gamediary;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.gamediary.fixture.HarvestDTOBuilderFactory;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.Harvest.StateAcceptedToHarvestPermit;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenRepository;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen_;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationCanBeLinkedToHuntingDayOnlyWithinMooseHuntingException;
import fi.riista.feature.gamediary.observation.ObservationDTO;
import fi.riista.feature.gamediary.observation.ObservationRepository;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenDTO;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenRepository;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen_;
import fi.riista.feature.gis.MockGISQueryService;
import fi.riista.feature.gis.RhyNotResolvableByGeoLocationException;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountNotFound;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.ClubHuntingFinishedException;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.hunting.day.PointOfTimeOutsideOfHuntingDayException;
import fi.riista.feature.huntingclub.hunting.day.PointOfTimeOutsideOfPermittedDatesException;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import javaslang.Tuple;
import javaslang.Tuple2;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static fi.riista.feature.common.entity.Required.NO;
import static fi.riista.feature.common.entity.Required.YES;
import static fi.riista.feature.gamediary.observation.ObservationType.AANI;
import static fi.riista.feature.gamediary.observation.ObservationType.JALKI;
import static fi.riista.feature.gamediary.observation.ObservationType.MUUTON_AIKAINEN_LEPAILYALUE;
import static fi.riista.feature.gamediary.observation.ObservationType.NAKO;
import static fi.riista.feature.gamediary.observation.ObservationType.RIISTAKAMERA;
import static fi.riista.util.Asserts.assertEmpty;
import static fi.riista.util.DateUtil.today;
import static fi.riista.util.EqualityHelper.equalIdAndContent;
import static fi.riista.util.EqualityHelper.equalNotNull;
import static fi.riista.util.Filters.hasAnyIdOf;
import static fi.riista.util.TestUtils.createList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GameDiaryFeatureTest extends EmbeddedDatabaseTest implements HarvestDTOBuilderFactory {

    private static final HarvestSpecVersion HARVEST_SPEC_VERSION = HarvestSpecVersion.MOST_RECENT;

    @Resource
    private GameDiaryFeature feature;

    @Resource
    private HarvestRepository harvestRepo;

    @Resource
    private HarvestSpecimenRepository harvestSpecimenRepo;

    @Resource
    private ObservationRepository observationRepo;

    @Resource
    private ObservationSpecimenRepository observationSpecimenRepo;

    @Resource
    protected PersonRepository personRepo;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testListDiaryEntriesForActiveUser() {
        final Interval interval = DateUtil.createDateInterval(today().minusDays(1), today().plusDays(1));
        final GameSpecies species = model().newGameSpecies();
        withPerson(me -> withPerson(other -> {
            final Harvest myHarvest = model().newHarvest(species, me, me);
            final Harvest myAuthoredHarvest = model().newHarvest(species, me, other);
            final Harvest notMyHarvest = model().newHarvest(species, other, other);
            final Observation myObservation = model().newObservation(species, me, me);
            final Observation myAuthoredObservation = model().newObservation(species, me, other);
            final Observation notMyObservation = model().newObservation(species, other, other);

            onSavedAndAuthenticated(createUser(me), () -> {
                assertEquals(
                        F.getUniqueIds(myHarvest, myObservation),
                        F.getUniqueIds(feature.listDiaryEntriesForActiveUser(interval, false)));
                assertEquals(
                        F.getUniqueIds(myAuthoredHarvest, myAuthoredObservation),
                        F.getUniqueIds(feature.listDiaryEntriesForActiveUser(interval, true)));
            });
        }));
    }

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
            final GameSpecies species = model().newGameSpecies(true);
            model().newHarvestReportFields(species, true);

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO inputDto = create(species, 1).build();
                final HarvestDTO outputDto = invokeCreateHarvest(inputDto);

                inputDto.setHarvestReportRequired(true);

                doCreateAssertions(outputDto.getId(), inputDto, author, author);
            });
        }));
    }

    @Test
    public void testCreateHarvest_forPermitChangeWhenPermitBasedHarvestReportDone() {
        withPerson(author -> {
            final GameSpecies species = model().newGameSpecies(true);
            model().newHarvestReportFields(species, true);

            final HarvestPermit permit = model().newHarvestPermit(true);
            model().newHarvestPermitSpeciesAmount(permit, species);

            model().newHarvestReport(model().newHarvest(permit), HarvestReport.State.APPROVED);

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO inputDto = create(species, 1).withPermitNumber(permit.getPermitNumber()).build();
                final HarvestDTO outputDto = invokeCreateHarvest(inputDto);

                inputDto.setStateAcceptedToHarvestPermit(StateAcceptedToHarvestPermit.REJECTED);

                doCreateAssertions(outputDto.getId(), inputDto, author, author);
            });
        });
    }

    @Test(expected = HarvestPermitSpeciesAmountNotFound.class)
    public void testCreateHarvestForPermit_WhenSpeciesIsNotValid() {
        final GameSpecies species = model().newGameSpecies(true);
        model().newHarvestReportFields(species, true);

        final HarvestPermit permit = model().newHarvestPermit(true);
        final HarvestPermitSpeciesAmount amount = model().newHarvestPermitSpeciesAmount(permit, species);

        persistAndAuthenticateWithNewUser(true);

        invokeCreateHarvest(create(species, 1)
                .withPointOfTime(amount.getBeginDate().minusDays(1).toLocalDateTime(LocalTime.MIDNIGHT))
                .withPermitNumber(permit.getPermitNumber())
                .build());
    }

    @Test(expected = RhyNotResolvableByGeoLocationException.class)
    public void testCreateHarvestForPermit_WhenRhyNotFound() {
        final GameSpecies species = model().newGameSpecies(true);
        model().newHarvestReportFields(species, true);

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
    public void testCreateHarvest_asModerator() {
        withPerson(author -> withPerson(actor -> {
            final GameSpecies species = model().newGameSpecies(true);

            onSavedAndAuthenticated(createNewModerator(), () -> {

                final HarvestDTO inputDto = create(species, 5).withAuthorInfo(author).withActorInfo(actor).build();
                final HarvestDTO outputDto = invokeCreateHarvest(inputDto);

                doCreateAssertions(outputDto.getId(), inputDto, author, actor);
            });
        }));
    }

    @Test
    public void testCreateHarvest_asModerator_linkToHuntingDay() {
        withMooseHuntingGroupFixture(fixture -> {
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(fixture.group, today());
            final Person author = fixture.groupMember;

            onSavedAndAuthenticated(createNewModerator(), () -> {

                final HarvestDTO inputDto = create(fixture.species, 1)
                        .withAuthorInfo(author)
                        .withActorInfo(author)
                        .linkToHuntingDay(huntingDay)
                        .build();
                final HarvestDTO outputDto = invokeCreateHarvest(inputDto);

                doCreateAssertions(
                        outputDto.getId(), inputDto, author, author, assertAcceptedToHuntingDay(null, huntingDay));
            });
        });
    }

    @Test
    public void testCreateObservation_asModerator() {
        withPerson(author -> withPerson(actor -> createObservationMetaF(true, NAKO).consumeBy(m -> {

            onSavedAndAuthenticated(createNewModerator(), () -> {

                final ObservationDTO inputDto = m.dtoBuilder(5).withAuthorInfo(author).withActorInfo(actor).build();
                final ObservationDTO outputDto = invokeCreateObservation(inputDto);

                doCreateAssertions(outputDto.getId(), inputDto, author, actor);
            });
        })));
    }

    @Test
    public void testCreateObservation_asModerator_linkToHuntingDay() {
        createObservationMetaF(true, NAKO).consumeBy(m -> withHuntingGroupFixture(m.getSpecies(), f -> {

            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today());
            final Person author = f.groupMember;

            onSavedAndAuthenticated(createNewModerator(), () -> {

                final ObservationDTO inputDto = m.dtoBuilder(5)
                        .withAuthorInfo(author)
                        .withActorInfo(author)
                        .linkToHuntingDay(huntingDay)
                        .build();

                final ObservationDTO outputDto = invokeCreateObservation(inputDto);

                doCreateAssertions(
                        outputDto.getId(), inputDto, author, author, assertAcceptedToHuntingDay(null, huntingDay));
            });
        }));
    }

    @Test
    public void testCreateObservation_withinMooseHunting() {
        doTestCreateObservation(Boolean.TRUE);
    }

    @Test
    public void testCreateObservation_notWithinMooseHunting() {
        doTestCreateObservation(null);
    }

    private void doTestCreateObservation(final Boolean withinMooseHunting) {
        withRhy(rhy -> withPerson(author -> createObservationMetaF(withinMooseHunting, NAKO).consumeBy(m -> {

            onSavedAndAuthenticated(createUser(author), () -> {
                final ObservationDTO inputDto = m.dtoBuilder(3).build();
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
    public void testCreateObservation_withActor() {
        withPerson(author -> withPerson(actor -> createObservationMetaF(true, NAKO).consumeBy(m -> {

            onSavedAndAuthenticated(createUser(author), () -> {

                final ObservationDTO inputDto = m.dtoBuilder(5).withActorInfo(actor).build();
                final ObservationDTO outputDto = invokeCreateObservation(inputDto);

                doCreateAssertions(outputDto.getId(), inputDto, author, actor);
            });
        })));
    }

    @Test
    public void testCreateObservation_withAmountField() {
        withPerson(author -> createObservationMetaF(MUUTON_AIKAINEN_LEPAILYALUE).withAmount(YES).consumeBy(m -> {

            onSavedAndAuthenticated(createUser(author), () -> {

                final ObservationDTO inputDto = m.dtoBuilder().withAmount(13).build();
                final ObservationDTO outputDto = invokeCreateObservation(inputDto);

                doCreateAssertions(outputDto.getId(), inputDto, author, author);
            });
        }));
    }

    @Test
    public void testCreateObservation_linkToHuntingDay() {
        createObservationMetaF(true, NAKO).consumeBy(m -> withHuntingGroupFixture(m.getSpecies(), f -> {

            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today());
            final Person author = f.groupLeader;

            onSavedAndAuthenticated(createUser(author), () -> {

                final ObservationDTO inputDto = m.dtoBuilder(5)
                        .withAuthorInfo(author)
                        .withActorInfo(author)
                        .linkToHuntingDay(huntingDay)
                        .build();

                final ObservationDTO outputDto = invokeCreateObservation(inputDto);

                doCreateAssertions(
                        outputDto.getId(), inputDto, author, author, assertAcceptedToHuntingDay(author, huntingDay));
            });
        }));
    }

    @Test(expected = ClubHuntingFinishedException.class)
    public void testCreateObservationForHuntingDay_whenHuntingFinished() {
        createObservationMetaF(true, NAKO).consumeBy(m -> withHuntingGroupFixture(m.getSpecies(), f -> {

            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today());

            // Set club hunting finished.
            model().newModeratedBasicHuntingSummary(f.speciesAmount, f.club);

            onSavedAndAuthenticated(
                    createUser(f.clubContact),
                    () -> invokeCreateObservation(m.dtoBuilder(1).linkToHuntingDay(huntingDay).build()));
        }));
    }

    @Test(expected = PointOfTimeOutsideOfHuntingDayException.class)
    public void testCreateObservationForHuntingDay_whenPointOfTimeOutsideOfHuntingDay() {
        createObservationMetaF(true, NAKO).consumeBy(m -> withHuntingGroupFixture(m.getSpecies(), f -> {

            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today());

            onSavedAndAuthenticated(createUser(f.clubContact), () -> {
                invokeCreateObservation(m.dtoBuilder(1)
                        .linkToHuntingDay(huntingDay)
                        .withPointOfTime(huntingDay.getStartAsLocalDateTime().minusMinutes(1))
                        .build());
            });
        }));
    }


    @Test(expected = PointOfTimeOutsideOfPermittedDatesException.class)
    public void testCreateObservationForHuntingDay_whenPointOfTimeOutsideOfPermitted() {
        createObservationMetaF(true, NAKO).consumeBy(m -> withHuntingGroupFixture(m.getSpecies(), f -> {

            final LocalDate date = f.speciesAmount.getBeginDate().minusDays(1);
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, date);

            onSavedAndAuthenticated(createUser(f.clubContact), () -> {
                invokeCreateObservation(m.dtoBuilder(1)
                        .linkToHuntingDay(huntingDay)
                        .withPointOfTime(date.toLocalDateTime(new LocalTime(12, 02)))
                        .build());
            });
        }));
    }

    @Test
    public void testUpdateHarvest_linkToHuntingDay() {
        withMooseHuntingGroupFixture(fixture -> {

            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(fixture.group, today());
            final Person author = fixture.groupLeader;

            final Harvest harvest = model().newHarvest(fixture.species, author);
            final List<HarvestSpecimen> specimens = createSpecimens(harvest, 1);
            harvest.setAmount(specimens.size());

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO inputDto = create(harvest).linkToHuntingDay(huntingDay).build();
                invokeUpdateHarvest(inputDto);

                doUpdateAssertions(
                        harvest.getId(), inputDto, author, author, 1, assertAcceptedToHuntingDay(author, huntingDay));
            });
        });
    }

    @Test
    public void testUpdateHarvest_linkToHuntingDay_acceptorNotChangedOnUpdate() {
        withMooseHuntingGroupFixture(fixture -> {
            final Person author = fixture.groupLeader;

            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(fixture.group, today());

            final Harvest harvest = model().newHarvest(fixture.species, author);
            final List<HarvestSpecimen> specimens = createSpecimens(harvest, 1);
            harvest.setAmount(specimens.size());

            final AtomicReference<HarvestDTO> updatedDto = new AtomicReference<>();

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO inputDto = create(harvest).linkToHuntingDay(huntingDay).build();

                updatedDto.set(invokeUpdateHarvest(inputDto));

                doUpdateAssertions(
                        harvest.getId(), inputDto, author, author, 1, assertAcceptedToHuntingDay(author, huntingDay));
            });

            onSavedAndAuthenticated(createNewModerator(), () -> {
                final HarvestDTO inputDto = updatedDto.get();
                inputDto.setRev(1);

                invokeUpdateHarvest(inputDto);
                doUpdateAssertions(
                        harvest.getId(), inputDto, author, author, 2, assertAcceptedToHuntingDay(author, huntingDay));
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

                final HarvestDTO inputDto = create(harvest).populateSpecimensWith(specimens).build();
                invokeUpdateHarvest(inputDto);

                doUpdateAssertions(harvest.getId(), inputDto, person, person, 0);
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

                final HarvestDTO inputDto = create(harvest)
                        .withActorInfo(actor)
                        .populateSpecimensWith(specimens)
                        .build();

                invokeUpdateHarvest(inputDto);

                doUpdateAssertions(harvest.getId(), inputDto, author, actor, 1);
            });
        }));
    }

    @Test
    public void testUpdateHarvest_whenHarvestReportNotDone() {
        withRhy(rhy -> withPerson(person -> {

            final Harvest harvest = model().newHarvest(model().newGameSpecies(true), person);
            final GameSpecies newSpecies = model().newGameSpecies(true);

            onSavedAndAuthenticated(createUser(person), () -> {

                final HarvestDTO inputDto = create(harvest, newSpecies, 10).mutate().build();
                invokeUpdateHarvest(inputDto);
                doUpdateAssertions(harvest.getId(), inputDto, person, person, 1, h -> assertNotNull(h.getRhy()));
            });
        }));
    }

    @Test
    public void testUpdateHarvest_whenSwitchingToSpeciesRequiringHarvestReport() {
        withRhy(rhy -> withPerson(person -> {
            final Harvest harvest = model().newHarvest(model().newGameSpecies(true), person);

            final GameSpecies newSpecies = model().newGameSpecies(true);
            model().newHarvestReportFields(newSpecies, true);

            onSavedAndAuthenticated(createUser(person), () -> {

                final HarvestDTO inputDto = create(harvest, newSpecies).mutate().build();
                invokeUpdateHarvest(inputDto);

                inputDto.setHarvestReportRequired(true);

                doUpdateAssertions(harvest.getId(), inputDto, person, person, 1);
            });
        }));
    }

    @Test
    public void testUpdateHarvest_whenHarvestReportDone() {
        withRhy(rhy -> withPerson(person -> {

            final Harvest harvest = model().newHarvest(model().newGameSpecies(true), person);
            final List<HarvestSpecimen> specimens = createSpecimens(harvest, 5);
            harvest.setAmount(specimens.size());

            model().newHarvestReport(harvest, HarvestReport.State.PROPOSED);

            final GameSpecies newSpecies = model().newGameSpecies(true);

            onSavedAndAuthenticated(createUser(person), () -> {

                final HarvestDTO inputDto = create(harvest, newSpecies, 10).mutate().build();

                invokeUpdateHarvest(inputDto);

                final HarvestDTO expectedValues = create(harvest)
                        .withDescription(inputDto.getDescription())
                        .populateSpecimensWith(specimens)
                        .build();

                doUpdateAssertions(harvest.getId(), expectedValues, person, person, 2, h -> assertNull(h.getRhy()));
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

                final HarvestDTO inputDto = create(harvest).populateSpecimensWith(specimens).mutateSpecimens().build();
                invokeUpdateHarvest(inputDto);

                doUpdateAssertions(harvest.getId(), inputDto, author, author, 1);
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

                final HarvestDTO inputDto = create(harvest)
                        .populateSpecimensWith(specimens)
                        .withAmount(specimens.size() + 10)
                        .build();

                invokeUpdateHarvest(inputDto);

                doUpdateAssertions(harvest.getId(), inputDto, person, person, 1);
            });
        });
    }

    @Test
    public void testUpdateHarvest_forAmountChange_whenSpecimensNotPresent() {
        withPerson(person -> {

            final Harvest harvest = model().newHarvest(model().newGameSpecies(true), person);

            onSavedAndAuthenticated(createUser(person), () -> {

                final HarvestDTO inputDto = create(harvest).withAmount(10).build();
                invokeUpdateHarvest(inputDto);

                doUpdateAssertions(harvest.getId(), inputDto, person, person, 1);
            });
        });
    }

    @Test
    public void testUpdateHarvest_whenPermitBasedHarvestReportDone() {
        withPerson(person -> {

            final GameSpecies species = model().newGameSpecies(true);
            final Harvest harvest = model().newHarvest(species, person);

            final HarvestPermit permit = model().newHarvestPermit(true);
            model().newHarvestPermitSpeciesAmount(permit, species);
            model().newHarvestReportFields(species, true);

            model().newHarvestReport(model().newHarvest(permit), HarvestReport.State.SENT_FOR_APPROVAL);

            onSavedAndAuthenticated(createUser(person), () -> {

                final HarvestDTO inputDto = create(harvest, 5)
                        .mutate()
                        .withPermitNumber(permit.getPermitNumber())
                        .build();

                invokeUpdateHarvest(inputDto);

                inputDto.setStateAcceptedToHarvestPermit(StateAcceptedToHarvestPermit.REJECTED);

                doUpdateAssertions(harvest.getId(), inputDto, person, person, 1);
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

                final HarvestDTO inputDto = create(harvest, 1).mutate().linkToHuntingDay(huntingDay).build();
                invokeUpdateHarvest(inputDto);

                inputDto.setDescription(harvest.getDescription());

                doUpdateAssertions(
                        harvest.getId(), inputDto, author, author, 1, assertAcceptedToHuntingDay(null, huntingDay));
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

                final HarvestDTO inputDto = create(harvest, 1).mutate().linkToHuntingDay(huntingDay).build();
                invokeUpdateHarvest(inputDto);

                inputDto.setDescription(harvest.getDescription());

                doUpdateAssertions(harvest.getId(), inputDto, f.clubContact, f.clubContact, 1);
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

            final GameSpecies newSpecies = model().newGameSpecies();

            onSavedAndAuthenticated(createUser(f.clubContact), () -> {

                final HarvestDTO inputDto = create(harvest, newSpecies, 1)
                        .mutate()
                        .linkToHuntingDay(huntingDay2)
                        .build();

                invokeUpdateHarvest(inputDto);

                final HarvestDTO expectedValues = create(harvest).withDescription(inputDto.getDescription()).build();

                doUpdateAssertions(harvest.getId(), expectedValues, f.clubContact, f.clubContact, 1, h -> {
                    assertNull(h.getRhy());
                });
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
            final GroupHuntingDay huntingDay2 = model().newGroupHuntingDay(f.group, today.minusDays(1));

            final Harvest harvest = model().newHarvest(f.species, f.clubContact, huntingDay);

            final GameSpecies newSpecies = model().newGameSpecies();

            onSavedAndAuthenticated(moderator ? createNewModerator() : createUser(f.clubContact), () -> {

                final HarvestDTO inputDto = create(harvest, 1)
                        .mutate()
                        .populateWith(newSpecies)
                        .linkToHuntingDay(huntingDay2)
                        .withAuthorInfo(f.groupMember)
                        .withActorInfo(f.groupMember)
                        .build();

                invokeUpdateHarvest(inputDto);

                final HarvestDTO expectedValues = moderator
                        ? create(inputDto).withDescription(harvest.getDescription()).build()
                        : create(harvest).withDescription(inputDto.getDescription()).build();

                final Person expectedAuthor = moderator ? f.groupMember : f.clubContact;

                doUpdateAssertions(harvest.getId(), expectedValues, expectedAuthor, expectedAuthor, 1);
            });
        });
    }

    @Test(expected = RhyNotResolvableByGeoLocationException.class)
    public void testUpdateHarvest_withPermitWhenRhyNotFound() {
        withPerson(author -> {
            final GameSpecies species = model().newGameSpecies(true);
            final Harvest harvest = model().newHarvest(species, author);

            final HarvestPermit permit = model().newHarvestPermit(true);
            model().newHarvestPermitSpeciesAmount(permit, species);
            model().newHarvestReportFields(species, true);

            onSavedAndAuthenticated(createUser(author), () -> {

                // everything else should be fine, but rhy is not found for location
                invokeUpdateHarvest(create(harvest, 5)
                        .mutate()
                        .withGeoLocation(MockGISQueryService.RHY_GEOLOCATION_NOT_FOUND)
                        .withPermitNumber(permit.getPermitNumber())
                        .build());
            });
        });
    }

    @Test(expected = HarvestPermitSpeciesAmountNotFound.class)
    public void testUpdateHarvest_whenSpeciesIsNotValid() {
        withPerson(author -> {
            final GameSpecies species = model().newGameSpecies(true);
            final Harvest harvest = model().newHarvest(species, author);
            final HarvestPermit permit = model().newHarvestPermit(true);
            final HarvestPermitSpeciesAmount amount = model().newHarvestPermitSpeciesAmount(permit, species);
            model().newHarvestReportFields(species, true);

            onSavedAndAuthenticated(createUser(author), () -> {

                invokeUpdateHarvest(create(harvest, 5)
                        .mutate()
                        .withPermitNumber(permit.getPermitNumber())
                        .withPointOfTime(amount.getBeginDate().minusDays(1).toLocalDateTime(LocalTime.MIDNIGHT))
                        .build());
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
                final HarvestDTO inputDto = create(harvest, 5).mutate().withActorInfo(person).build();
                invokeUpdateHarvest(inputDto);

                doUpdateAssertions(harvest.getId(), inputDto, person, person, 1);
            });
        })));
    }

    @Test
    public void testUpdateHarvest_withHarvestAcceptedToPermit_asContactPerson() {
        withHarvestHavingPermitState(StateAcceptedToHarvestPermit.ACCEPTED, harvest -> {
            harvest.getHarvestPermit().setOriginalContactPerson(harvest.getAuthor());
            doTestUpdateHarvestWithPermit(true, StateAcceptedToHarvestPermit.ACCEPTED, harvest);
        });
    }

    @Test
    public void testUpdateHarvest_withHarvestAcceptedToPermit() {
        withHarvestHavingPermitState(StateAcceptedToHarvestPermit.ACCEPTED, harvest -> {
            doTestUpdateHarvestWithPermit(false, StateAcceptedToHarvestPermit.ACCEPTED, harvest);
        });
    }

    @Test
    public void testUpdateHarvest_withHarvestProposedToPermit() {
        withHarvestHavingPermitState(StateAcceptedToHarvestPermit.PROPOSED, harvest -> {
            doTestUpdateHarvestWithPermit(true, StateAcceptedToHarvestPermit.PROPOSED, harvest);
        });
    }

    @Test
    public void testUpdateHarvest_withHarvestRejectedToPermit() {
        withHarvestHavingPermitState(StateAcceptedToHarvestPermit.REJECTED, harvest -> {
            doTestUpdateHarvestWithPermit(true, StateAcceptedToHarvestPermit.PROPOSED, harvest);
        });
    }

    private void doTestUpdateHarvestWithPermit(
            final boolean businessFieldsUpdated,
            final StateAcceptedToHarvestPermit expectedStateAfterUpdate,
            final Harvest harvest) {

        onSavedAndAuthenticated(createUser(harvest.getAuthor()), () -> {
            final String permitNumber = harvest.getHarvestPermit().getPermitNumber();

            final HarvestDTO inputDto = create(harvest, 5).mutate().withPermitNumber(permitNumber).build();

            invokeUpdateHarvest(inputDto);

            final HarvestDTO expectedValues = businessFieldsUpdated
                    ? create(inputDto).withStateAcceptedToHarvestPermit(expectedStateAfterUpdate).build()
                    : create(harvest)
                            .withDescription(inputDto.getDescription())
                            .withPermitNumber(permitNumber)
                            .withStateAcceptedToHarvestPermit(expectedStateAfterUpdate)
                            .build();

            doUpdateAssertions(harvest.getId(), expectedValues, harvest.getAuthor(), harvest.getActor(), 1);
        });
    }

    @Test
    public void testUpdateHarvest_forAuthorChangeWhenModerator() {
        final Harvest harvest = model().newHarvest();
        final Person newAuthor = model().newPerson();

        onSavedAndAuthenticated(createNewModerator(), () -> {

            final HarvestDTO inputDto = create(harvest, 1).mutate().withAuthorInfo(newAuthor).build();
            invokeUpdateHarvest(inputDto);

            inputDto.setDescription(harvest.getDescription());

            doUpdateAssertions(harvest.getId(), inputDto, newAuthor, harvest.getActor(), 1);
        });
    }

    @Test
    public void testUpdateHarvest_forAuthorChangedWhenNormalUser() {
        withPerson(author -> withPerson(newAuthor -> {
            final Harvest harvest = model().newHarvest(author);

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO inputDto = create(harvest, 1)
                        .mutate()
                        .withGeoLocation(harvest.getGeoLocation())
                        .withAuthorInfo(newAuthor)
                        .build();

                invokeUpdateHarvest(inputDto);

                doUpdateAssertions(harvest.getId(), inputDto, author, author, 1);
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
                    .withDescription("moderator changed this")
                    .withImageIds(Collections.emptyList())
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
    public void testUpdateObservation() {
        withRhy(rhy -> createObservationMetaF(false, RIISTAKAMERA).createSpecimensF(3).consumeBy(f -> {

            createObservationMetaF(true, NAKO).consumeBy(m -> onSavedAndAuthenticated(createUser(f.author), () -> {

                final ObservationDTO inputDto = m.dtoBuilder(f.observation, 5).mutate().build();
                invokeUpdateObservation(inputDto);

                doUpdateAssertions(f.observation.getId(), inputDto, f.author, f.author, 1, o -> {
                    assertNotNull(o.getRhy());
                    assertEquals(rhy.getOfficialCode(), o.getRhy().getOfficialCode());
                });
            }));
        }));
    }

    @Test
    public void testUpdateObservation_whenNoChanges() {
        createObservationMetaF(NAKO).createSpecimensF(5).consumeBy((m, f) -> {

            onSavedAndAuthenticated(createUser(f.author), () -> {

                final ObservationDTO inputDto = m.dtoBuilder(f.observation).populateSpecimensWith(f.specimens).build();
                invokeUpdateObservation(inputDto);

                doUpdateAssertions(f.observation.getId(), inputDto, f.author, f.author, 0);
            });
        });
    }

    @Test(expected = ObservationCanBeLinkedToHuntingDayOnlyWithinMooseHuntingException.class)
    public void testUpdateObservation_linkToHuntingDay_possibleOnlyIfWithinMooseHunting() {
        new UpdateObservationLinkToHuntingDayTestRunner(model().newPerson(), null, null).runTest();
    }

    @Test
    public void testUpdateObservation_linkToHuntingDay() {
        new UpdateObservationLinkToHuntingDayTestRunner(model().newPerson(), true, true).runTest();
    }

    @Test
    public void testUpdateObservation_linkToHuntingDay_acceptorNotChangedOnUpdate() {
        withPerson(author -> new UpdateObservationLinkToHuntingDayTestRunner(author, true, true).runTest((fxt, day) -> {

            onSavedAndAuthenticated(createNewModerator(), () -> {

                final Long id = fxt._2.observation.getId();
                final ObservationDTO dto = fxt._1.dtoBuilder(observationRepo.findOne(id)).build();
                invokeUpdateObservation(dto);

                doUpdateAssertions(id, dto, author, author, 2, assertAcceptedToHuntingDay(author, day));
            });
        }));
    }

    @Test
    public void testUpdateObservation_whenActorChanged() {
        withPerson(actor -> createObservationMetaF(NAKO).createSpecimensF(5).consumeBy((m, f) -> {

            onSavedAndAuthenticated(createUser(f.author), () -> {

                final ObservationDTO inputDto = m.dtoBuilder(f.observation)
                        .withActorInfo(actor)
                        .populateSpecimensWith(f.specimens)
                        .build();

                invokeUpdateObservation(inputDto);

                doUpdateAssertions(f.observation.getId(), inputDto, f.author, actor, 1);
            });
        }));
    }

    @Test
    public void testUpdateObservation_whenUpdatingSpecimensOnly() {
        createObservationMetaF(NAKO).createSpecimensF(5).consumeBy((m, f) -> {

            onSavedAndAuthenticated(createUser(f.author), () -> {

                final ObservationDTO inputDto = m.dtoBuilder(f.observation)
                        .populateSpecimensWith(f.specimens)
                        .mutateSpecimens()
                        .build();

                invokeUpdateObservation(inputDto);

                doUpdateAssertions(f.observation.getId(), inputDto, f.author, f.author, 1);
            });
        });
    }

    @Test
    public void testUpdateObservation_forAmountChange_whenSpecimensPresent() {
        createObservationMetaF(NAKO).createSpecimensF(5).consumeBy((m, f) -> {

            onSavedAndAuthenticated(createUser(f.author), () -> {

                final ObservationDTO inputDto = m.dtoBuilder(f.observation)
                        .populateSpecimensWith(f.specimens)
                        .withAmount(f.specimens.size() + 10)
                        .build();

                invokeUpdateObservation(inputDto);

                doUpdateAssertions(f.observation.getId(), inputDto, f.author, f.author, 1);
            });
        });
    }

    @Test
    public void testUpdateObservation_forAmountChange_whenSpecimensNotPresent() {
        withPerson(author -> createObservationMetaF(NAKO).consumeBy(m -> {

            final Observation observation = model().newObservation(author, m);

            onSavedAndAuthenticated(createUser(author), () -> {
                final ObservationDTO inputDto = m.dtoBuilder(observation).withAmount(10).build();
                invokeUpdateObservation(inputDto);
                doUpdateAssertions(observation.getId(), inputDto, author, author, 1);
            });
        }));
    }

    @Test
    public void testUpdateObservation_whenChangingToObservationTypeNotAcceptingAmount() {
        createObservationMetaF(NAKO).createSpecimensF(5).consumeBy(f -> {

            createObservationMetaF(AANI).withAmount(NO).consumeBy(m2 -> {

                onSavedAndAuthenticated(createUser(f.author), () -> {

                    final ObservationDTO inputDto = m2.dtoBuilder(f.observation).mutate().withAmount(null).build();
                    invokeUpdateObservation(inputDto);

                    doUpdateAssertions(f.observation.getId(), inputDto, f.author, f.author, 1);
                });
            });
        });
    }

    @Test
    public void testUpdateObservation_linkToHuntingDay_asModerator() {
        withMooseHuntingGroupFixture(f -> createObservationMetaF(true, NAKO).consumeBy(m -> {

            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today());

            // Different species than what is assigned for the group but doesn't matter.
            final Observation observation = model().newObservation(f.groupMember, m);

            onSavedAndAuthenticated(createNewModerator(), () -> {

                final ObservationDTO inputDto = m.dtoBuilder(observation, 1)
                        .mutate()
                        .linkToHuntingDay(huntingDay)
                        .build();

                invokeUpdateObservation(inputDto);

                inputDto.setDescription(observation.getDescription());

                doUpdateAssertions(observation.getId(), inputDto, f.groupMember, f.groupMember, 1,
                        assertAcceptedToHuntingDay(null, huntingDay));
            });
        }));
    }

    @Test
    public void testUpdateObservation_whenAssociatedWithHuntingDay_asModerator() {
        withMooseHuntingGroupFixture(f -> createObservationMetaF(true, NAKO).consumeBy(m -> {

            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today());

            // Different species than what is assigned for the group but doesn't matter.
            final Observation observation = model().newObservation(m.getSpecies(), f.groupMember, huntingDay);

            onSavedAndAuthenticated(createNewModerator(), () -> {

                final ObservationDTO inputDto = m.dtoBuilder(observation, 1)
                        .mutate()
                        .linkToHuntingDay(huntingDay)
                        .build();

                invokeUpdateObservation(inputDto);

                inputDto.setDescription(observation.getDescription());

                doUpdateAssertions(observation.getId(), inputDto, f.groupMember, f.groupMember, 1, o -> {
                    assertNotNull(o.getRhy());
                    assertEquals(f.rhy.getOfficialCode(), o.getRhy().getOfficialCode());
                });
            });
        }));
    }

    // Test that observation is not mutated (except for description/images) when hunting is finished.
    @Test
    public void testUpdateObservation_whenHuntingFinished() {
        createObservationMetaF(true, NAKO).consumeBy(m -> withHuntingGroupFixture(m.getSpecies(), f -> {

            final LocalDate today = today();
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today);
            final GroupHuntingDay huntingDay2 = model().newGroupHuntingDay(f.group, today.minusDays(1));

            // Set club hunting finished.
            model().newBasicHuntingSummary(f.speciesAmount, f.club, true);

            final Observation observation = model().newObservation(m.getSpecies(), f.clubContact, huntingDay);

            createObservationMetaF(true, JALKI).consumeBy(m2 -> {

                onSavedAndAuthenticated(createUser(f.clubContact), () -> {

                    final ObservationDTO inputDto = m2.dtoBuilder(observation, 5)
                            .mutate()
                            .linkToHuntingDay(huntingDay2)
                            .build();

                    invokeUpdateObservation(inputDto);

                    final ObservationDTO expectedValues = m.dtoBuilder(observation)
                            .withDescription(inputDto.getDescription())
                            .build();

                    doUpdateAssertions(observation.getId(), expectedValues, f.clubContact, f.clubContact, 1, o -> {
                        assertNull(o.getRhy());
                    });
                });
            });
        }));
    }

    // Test that observation is not mutated (except for description/images) when group is created
    // within moose data card import.
    @Test
    public void testUpdateObservation_whenGroupOriginatingFromMooseDataCard_asClubContact() {
        testUpdateObservation_whenGroupOriginatingFromMooseDataCard(false);
    }

    @Test
    public void testUpdateObservation_whenGroupOriginatingFromMooseDataCard_asModerator() {
        testUpdateObservation_whenGroupOriginatingFromMooseDataCard(true);
    }

    private void testUpdateObservation_whenGroupOriginatingFromMooseDataCard(final boolean moderator) {
        createObservationMetaF(true, NAKO).consumeBy(m -> withHuntingGroupFixture(m.getSpecies(), f -> {
            f.group.setFromMooseDataCard(true);

            final LocalDate today = today();
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today);
            final GroupHuntingDay huntingDay2 = model().newGroupHuntingDay(f.group, today.minusDays(1));

            final Observation observation = model().newObservation(m.getSpecies(), f.clubContact, huntingDay);

            createObservationMetaF(true, JALKI).consumeBy(m2 -> {

                onSavedAndAuthenticated(moderator ? createNewModerator() : createUser(f.clubContact), () -> {

                    final ObservationDTO inputDto = m2.dtoBuilder(observation, 1)
                            .mutate()
                            .withAuthorInfo(f.groupMember)
                            .withActorInfo(f.groupMember)
                            .linkToHuntingDay(huntingDay2)
                            .build();

                    invokeUpdateObservation(inputDto);

                    final ObservationDTO expectedValues = moderator
                            ? m2.dtoBuilder(inputDto).withDescription(observation.getDescription()).build()
                            : m.dtoBuilder(observation).withDescription(inputDto.getDescription()).build();

                    final Person expectedAuthor = moderator ? f.groupMember : f.clubContact;

                    doUpdateAssertions(observation.getId(), expectedValues, expectedAuthor, expectedAuthor, 1);
                });
            });
        }));
    }

    @Test
    public void testDeleteHarvest_whenHarvestReportNotDone() {
        withPerson(author -> {
            final Harvest harvest = model().newHarvest(author);

            model().newHarvestSpecimen(harvest);
            model().newGameDiaryImage(harvest);

            // HarvestReport with DELETED state means harvest report is not done.
            model().newHarvestReport(harvest, HarvestReport.State.DELETED);

            doTestDeleteHarvest(true, harvest, createUser(author));
        });
    }

    @Test
    public void testDeleteHarvest_whenHarvestReportDone() {
        withPerson(author -> {
            final Harvest harvest = model().newHarvest(author);

            model().newHarvestSpecimen(harvest);
            model().newHarvestReport(harvest, HarvestReport.State.PROPOSED);

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

    private void testDeleteHarvest_whenHarvestHasPermitProcessingState(
            final boolean shouldBeDeleted, final StateAcceptedToHarvestPermit state) {

        testDeleteHarvest_whenHarvestHasPermitProcessingState(shouldBeDeleted, state, harvest -> {});
    }

    private void testDeleteHarvest_whenHarvestHasPermitProcessingState(
            final boolean shouldBeDeleted, final StateAcceptedToHarvestPermit state, final Consumer<Harvest> consumer) {

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
                        if (e instanceof AccessDeniedException) {
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

    @Test
    public void testDeleteObservation() {
        withPerson(author -> createObservationMetaF(NAKO).consumeBy(m -> {

            final Observation observation = model().newObservation(author, m);
            model().newObservationSpecimen(observation);
            model().newGameDiaryImage(observation);

            onSavedAndAuthenticated(createUser(author), () -> {
                feature.deleteObservation(observation.getId());
                assertNull(observationRepo.findOne(observation.getId()));
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

    @Test
    public void testSaveImageTwice() throws IOException {
        final SystemUser user = createUserWithPerson();
        final Harvest harvest = model().newHarvest(user.getPerson());

        persistInNewTransaction();

        authenticate(user);

        final long harvestId = harvest.getId();
        final UUID imageId = UUID.randomUUID();

        final byte[] imageData = Files.readAllBytes(new File("frontend/app/assets/images/select2.png").toPath());
        final MultipartFile file = new MockMultipartFile("test.png", "//test/test.png", "image/png", imageData);

        feature.addGameDiaryImageForDiaryEntry(harvestId, GameDiaryEntryType.HARVEST, imageId, file);
        feature.addGameDiaryImageForDiaryEntry(harvestId, GameDiaryEntryType.HARVEST, imageId, file);

        assertNotNull(feature.getGameDiaryImageBytes(imageId, false));
    }

    private void withHarvestHavingPermitState(
            final StateAcceptedToHarvestPermit state, final Consumer<Harvest> consumer) {

        withRhy(rhy -> withPerson(author -> {
            final GameSpecies species = model().newGameSpecies(true);
            final HarvestPermit permit = model().newHarvestPermit(rhy, true);

            final Harvest harvest = model().newHarvest(species, author);
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(state);
            harvest.setRhy(rhy);

            model().newHarvestPermitSpeciesAmount(permit, species);
            model().newHarvestReportFields(species, true);

            consumer.accept(harvest);
        }));
    }

    private List<HarvestSpecimen> createSpecimens(final Harvest harvest, final int numSpecimens) {
        return createList(numSpecimens, () -> model().newHarvestSpecimen(harvest, HARVEST_SPEC_VERSION));
    }

    protected void doCreateAssertions(
            final long harvestId, final HarvestDTO expectedValues, final Person expectedAuthor, final Person expectedActor) {

        doCreateAssertions(harvestId, expectedValues, expectedAuthor, expectedActor, h -> {});
    }

    protected void doCreateAssertions(
            final long harvestId,
            final HarvestDTO expectedValues,
            final Person expectedAuthor,
            final Person expectedActor,
            final Consumer<Harvest> additionalAssertions) {

        runInTransaction(() -> {
            final Harvest harvest = harvestRepo.findOne(harvestId);
            assertHarvestExpectations(harvest, expectedValues);
            assertVersion(harvest, 0);

            validateAuthorAndActor(harvest, F.getId(expectedAuthor), F.getId(expectedActor));

            assertNull(harvest.getHarvestReport());
            assertFalse(harvest.isHarvestReportDone());

            additionalAssertions.accept(harvest);
        });
    }

    protected void doCreateAssertions(
            final long observationId, final ObservationDTO expectedValues,
            final Person expectedAuthor, final Person expectedActor) {

        doCreateAssertions(observationId, expectedValues, expectedAuthor, expectedActor, h -> {});
    }

    protected void doCreateAssertions(
            final long observationId,
            final ObservationDTO expectedValues,
            final Person expectedAuthor,
            final Person expectedActor,
            final Consumer<Observation> additionalAssertions) {

        runInTransaction(() -> {
            final Observation observation = observationRepo.findOne(observationId);
            assertObservationExpectations(observation, expectedValues);
            assertVersion(observation, 0);

            validateAuthorAndActor(observation, F.getId(expectedAuthor), F.getId(expectedActor));

            additionalAssertions.accept(observation);
        });
    }

    private void doUpdateAssertions(
            final long harvestId,
            final HarvestDTO expectedValues,
            final Person expectedAuthor,
            final Person expectedActor,
            final int expectedRevision) {

        doUpdateAssertions(harvestId, expectedValues, expectedAuthor, expectedActor, expectedRevision, h -> {});
    }

    private void doUpdateAssertions(
            final long harvestId,
            final HarvestDTO expectedValues,
            final Person expectedAuthor,
            final Person expectedActor,
            final int expectedRevision,
            final Consumer<Harvest> additionalAssertions) {

        runInTransaction(() -> {
            final Harvest harvest = harvestRepo.findOne(harvestId);
            assertHarvestExpectations(harvest, expectedValues);
            assertVersion(harvest, expectedRevision);

            validateAuthorAndActor(harvest, F.getId(expectedAuthor), F.getId(expectedActor));

            additionalAssertions.accept(harvest);
        });
    }

    private void doUpdateAssertions(
            final long observationId,
            final ObservationDTO expectedValues,
            final Person expectedAuthor,
            final Person expectedActor,
            final int expectedRevision) {

        doUpdateAssertions(observationId, expectedValues, expectedAuthor, expectedActor, expectedRevision, h -> {});
    }

    private void doUpdateAssertions(
            final long observationId,
            final ObservationDTO expectedValues,
            final Person expectedAuthor,
            final Person expectedActor,
            final int expectedRevision,
            final Consumer<Observation> additionalAssertions) {

        runInTransaction(() -> {
            final Observation observation = observationRepo.findOne(observationId);
            assertObservationExpectations(observation, expectedValues);
            assertVersion(observation, expectedRevision);

            validateAuthorAndActor(observation, F.getId(expectedAuthor), F.getId(expectedActor));

            additionalAssertions.accept(observation);
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

        Optional.ofNullable(expectedValues.getActorInfo()).ifPresent(actor -> {
            assertEquals(actor.getId(), harvest.getActualShooter().getId());
        });

        assertEquals(expectedValues.getHuntingDayId(), F.getId(harvest.getHuntingDayOfGroup()));

        assertSpecimens(
                harvestSpecimenRepo.findByHarvest(harvest, new JpaSort(HarvestSpecimen_.id)),
                expectedValues.getSpecimens(),
                expectedValues.specimenOps()::equalContent);
    }

    private void assertObservationExpectations(final Observation observation, final ObservationDTO expectedValues) {
        assertCommonExpectations(observation, expectedValues);
        assertFalse(observation.isFromMobile());

        assertEquals(expectedValues.getWithinMooseHunting(), observation.getWithinMooseHunting());
        assertEquals(expectedValues.getObservationType(), observation.getObservationType());

        assertTrue(observation.isAmountEqualTo(expectedValues.getAmount()));
        assertEquals(expectedValues.getMooselikeMaleAmount(), observation.getMooselikeMaleAmount());
        assertEquals(expectedValues.getMooselikeFemaleAmount(), observation.getMooselikeFemaleAmount());
        assertEquals(expectedValues.getMooselikeFemale1CalfAmount(), observation.getMooselikeFemale1CalfAmount());
        assertEquals(expectedValues.getMooselikeFemale2CalfsAmount(), observation.getMooselikeFemale2CalfsAmount());
        assertEquals(expectedValues.getMooselikeFemale3CalfsAmount(), observation.getMooselikeFemale3CalfsAmount());
        assertEquals(expectedValues.getMooselikeFemale4CalfsAmount(), observation.getMooselikeFemale4CalfsAmount());
        assertEquals(
                expectedValues.getMooselikeUnknownSpecimenAmount(), observation.getMooselikeUnknownSpecimenAmount());

        Optional.ofNullable(expectedValues.getActorInfo()).ifPresent(actor -> {
            assertEquals(actor.getId(), observation.getObserver().getId());
        });

        assertEquals(expectedValues.getHuntingDayId(), F.getId(observation.getHuntingDayOfGroup()));

        assertSpecimens(observation, expectedValues.getSpecimens());
    }

    private static void assertCommonExpectations(
            final GameDiaryEntry entry, final HuntingDiaryEntryDTO expectedValues) {

        assertNotNull(entry);

        assertEquals(GeoLocation.Source.MANUAL, entry.getGeoLocation().getSource());
        assertNull(entry.getMobileClientRefId());

        assertEquals(expectedValues.getGameSpeciesCode(), entry.getSpecies().getOfficialCode());
        assertEquals(DateUtil.toDateNullSafe(expectedValues.getPointOfTime()), entry.getPointOfTime());
        assertEquals(expectedValues.getGeoLocation(), entry.getGeoLocation());
        assertEquals(expectedValues.getDescription(), entry.getDescription());
    }

    private void validateAuthorAndActor(final GameDiaryEntry diaryEntry,
                                        final Long expectedAuthorId,
                                        final Long expectedActorId) {
        final Person author = personRepo.getOne(expectedAuthorId);
        final Person actor = expectedActorId != null ? personRepo.getOne(expectedActorId) : author;

        assertEquals("wrong author", author, diaryEntry.getAuthor());
        assertEquals("wrong actor", actor, diaryEntry.getActor());
    }

    private void assertSpecimens(
            final Observation observation, final List<ObservationSpecimenDTO> expectedSpecimens) {

        final List<ObservationSpecimen> specimens =
                observationSpecimenRepo.findByObservation(observation, new JpaSort(ObservationSpecimen_.id));

        if (expectedSpecimens == null) {
            assertEmpty(specimens);
        } else {
            assertSpecimens(specimens, expectedSpecimens, ObservationSpecimenDTO.EQUAL_TO_ENTITY);
        }
    }

    private static <ENTITY extends HasID<Long>, DTO extends HasID<Long>> void assertSpecimens(
            final List<ENTITY> specimens,
            final List<DTO> expectedSpecimens,
            final BiFunction<ENTITY, DTO, Boolean> compareFn) {

        assertEquals(expectedSpecimens.size(), specimens.size());

        final Map<Boolean, List<DTO>> dtoPartitionByIdExistence = F.partition(expectedSpecimens, F::hasId);
        final List<DTO> expectedUpdatedSpecimens = dtoPartitionByIdExistence.get(true);
        final Map<Boolean, List<ENTITY>> entityPartition = F.partition(specimens, hasAnyIdOf(expectedUpdatedSpecimens));

        assertTrue(equalIdAndContent(entityPartition.get(true), expectedUpdatedSpecimens, compareFn));
        assertTrue(equalNotNull(entityPartition.get(false), dtoPartitionByIdExistence.get(false), compareFn));
    }

    private static <T extends GameDiaryEntry> Consumer<T> assertAcceptedToHuntingDay(
            final Person acceptor, final GroupHuntingDay huntingDay) {

        return e -> {
            assertEquals(huntingDay.getId(), e.getHuntingDayOfGroup().getId());
            assertEquals(acceptor, e.getApproverToHuntingDay());
            assertNotNull(e.getPointOfTimeApprovedToHuntingDay());
        };
    }

    private HarvestDTO invokeCreateHarvest(final HarvestDTO input) {
        return withVersionChecked(feature.createHarvest(input));
    }

    private ObservationDTO invokeCreateObservation(final ObservationDTO input) {
        return withVersionChecked(feature.createObservation(input));
    }

    private HarvestDTO invokeUpdateHarvest(final HarvestDTO input) {
        return withVersionChecked(feature.updateHarvest(input));
    }

    private ObservationDTO invokeUpdateObservation(final ObservationDTO input) {
        return withVersionChecked(feature.updateObservation(input));
    }

    private HarvestDTO withVersionChecked(final HarvestDTO dto) {
        return checkDtoVersionAgainstEntity(dto, Harvest.class);
    }

    private ObservationDTO withVersionChecked(final ObservationDTO dto) {
        return checkDtoVersionAgainstEntity(dto, Observation.class);
    }

    private class UpdateObservationLinkToHuntingDayTestRunner {
        private Person author;
        private Boolean metadataWithinMooseHunting;
        private Boolean observationWithinMooseHunting;

        public UpdateObservationLinkToHuntingDayTestRunner(
                final Person author,
                final Boolean metadataWithinMooseHunting,
                final Boolean observationWithinMooseHunting) {

            this.author = author;
            this.metadataWithinMooseHunting = metadataWithinMooseHunting;
            this.observationWithinMooseHunting = observationWithinMooseHunting;
        }

        public void runTest() {
            runTest(null);
        }

        public void runTest(@Nullable final BiConsumer<Tuple2<ObservationMetaFixture, ObservationSpecimenFixture>, GroupHuntingDay> fixtureConsumer) {
            createObservationMetaF(metadataWithinMooseHunting, NAKO).createSpecimensF(author, 5).consumeBy((m, f) -> {

                final HuntingClubGroup group = model().newHuntingClubGroup(model().newHuntingClub(), m.getSpecies());
                group.updateHarvestPermit(model().newHarvestPermit());
                model().newHarvestPermitSpeciesAmount(group.getHarvestPermit(), m.getSpecies());
                final GroupHuntingDay huntingDay = model().newGroupHuntingDay(group, today());

                model().newHuntingClubGroupMember(author, group, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

                onSavedAndAuthenticated(createUser(author), () -> {

                    final ObservationDTO inputDto = m.dtoBuilder(f.observation)
                            .withWithinMooseHunting(observationWithinMooseHunting)
                            .linkToHuntingDay(huntingDay)
                            .populateSpecimensWith(f.specimens)
                            .build();

                    invokeUpdateObservation(inputDto);

                    final Long id = f.observation.getId();
                    doUpdateAssertions(id, inputDto, author, author, 1, assertAcceptedToHuntingDay(author, huntingDay));
                });

                if (fixtureConsumer != null) {
                    fixtureConsumer.accept(Tuple.of(m, f), huntingDay);
                }
            });
        }
    }

}

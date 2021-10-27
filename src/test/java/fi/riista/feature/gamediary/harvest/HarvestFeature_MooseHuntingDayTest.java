package fi.riista.feature.gamediary.harvest;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.fixture.HarvestDTOBuilderFactory;
import fi.riista.feature.gamediary.fixture.HarvestSpecimenType;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.hunting.ClubHuntingFinishedException;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.hunting.day.PointOfTimeOutsideOfHuntingDayException;
import fi.riista.feature.huntingclub.hunting.day.PointOfTimeOutsideOfPermittedDatesException;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;

import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_FEMALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_MALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ANTLERS_LOST;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.YOUNG_FEMALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.YOUNG_MALE;
import static fi.riista.test.TestUtils.ld;
import static fi.riista.util.DateUtil.huntingYear;
import static fi.riista.util.DateUtil.today;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

@RunWith(Theories.class)
public class HarvestFeature_MooseHuntingDayTest extends HarvestFeatureTestBase
        implements HarvestDTOBuilderFactory, HuntingGroupFixtureMixin {

    @DataPoints("specimenTypes")
    public static final HarvestSpecimenType[] SPECIMEN_TYPES = {
            ADULT_MALE, ADULT_FEMALE, ANTLERS_LOST, YOUNG_MALE, YOUNG_FEMALE
    };

    @Theory
    public void testCreateHarvest_linkToHuntingDay(final HarvestSpecimenType specimenType) {
        withMooseHuntingGroupFixture(f -> {

            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today());
            final Person author = f.groupLeader;
            final Person actor = f.groupMember;

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO inputDto = create(f.species)
                        .withSpecimen(specimenType)
                        .withAuthorInfo(author)
                        .withActorInfo(actor)
                        .linkToHuntingDay(huntingDay)
                        .build();

                final HarvestDTO outputDto = invokeCreateHarvest(inputDto);

                runInTransaction(() -> {
                    final Harvest harvest = assertHarvestCreated(outputDto.getId());

                    assertThat(harvest.getDescription(), is(notNullValue()));

                    assertAuthorAndActor(harvest, F.getId(author), F.getId(actor));

                    assertAcceptanceToHuntingDay(harvest, huntingDay, author);
                });
            });
        });
    }

    @Theory
    public void testCreateHarvestForHuntingDay_whenHuntingFinished(final HarvestSpecimenType specimenType) {
        withMooseHuntingGroupFixture(f -> {

            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today());

            // Intermediary flush needed before persisting MooseHuntingSummary in order to have
            // harvest_permit_partners table populated required for foreign key constraint.
            persistInNewTransaction();

            // Set club hunting finished.
            model().newMooseHuntingSummary(f.permit, f.club, true);

            final Person author = f.clubContact;

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO dto = create(f.species)
                        .withSpecimen(specimenType)
                        .withAuthorInfo(author)
                        .linkToHuntingDay(huntingDay)
                        .build();

                assertCreateThrows(ClubHuntingFinishedException.class, dto);
            });
        });
    }

    @Theory
    public void testCreateHarvestForHuntingDay_whenPointOfTimeOutsideOfHuntingDay(final HarvestSpecimenType specimenType) {
        // Month check done in order to have this test working in August generally because beginDate of speciesAmount
        // is set to 1.9.
        final int currentMonth = today().getMonthOfYear();
        final int huntingYear = currentMonth >= 9 ? huntingYear() : huntingYear() - 1;

        final HarvestPermitSpeciesAmount speciesAmount =
                model().newHarvestPermitSpeciesAmount(
                        model().newMooselikePermit(model().newRiistanhoitoyhdistys()),
                        model().newGameSpeciesMoose());
        speciesAmount.setBeginDate(ld(huntingYear, 9, 1));
        speciesAmount.setEndDate(ld(huntingYear, 12, 31));

        withHuntingGroupFixture(speciesAmount, f -> {

            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, speciesAmount.getBeginDate());
            final Person author = f.clubContact;

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO dto = create(f.species)
                        .withSpecimen(specimenType)
                        .withAuthorInfo(author)
                        .linkToHuntingDay(huntingDay)
                        .withPointOfTime(huntingDay.getStartAsLocalDateTime().minusHours(4))
                        .build();

                assertCreateThrows(PointOfTimeOutsideOfHuntingDayException.class, dto);
            });
        });
    }

    @Theory
    public void testCreateHarvestForHuntingDay_whenPointOfTimeOutsideOfPermitted(final HarvestSpecimenType specimenType) {
        // Month check done in order to have this test working in August generally because beginDate of speciesAmount
        // is set to 1.9.
        final int currentMonth = today().getMonthOfYear();
        final int huntingYear = currentMonth >= 9 ? huntingYear() : huntingYear() - 1;

        final HarvestPermitSpeciesAmount speciesAmount =
                model().newHarvestPermitSpeciesAmount(
                        model().newMooselikePermit(model().newRiistanhoitoyhdistys()),
                        model().newGameSpeciesMoose());
        speciesAmount.setBeginDate(ld(huntingYear, 9, 1));
        speciesAmount.setEndDate(ld(huntingYear, 12, 31));

        withHuntingGroupFixture(speciesAmount, f -> {

            final LocalDate date = f.speciesAmount.getBeginDate().minusDays(1);
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, date);

            onSavedAndAuthenticated(createUser(f.clubContact), () -> {

                final HarvestDTO dto = create(f.species)
                        .withSpecimen(specimenType)
                        .withAuthorInfo(f.groupMember)
                        .linkToHuntingDay(huntingDay)
                        .build();

                assertCreateThrows(PointOfTimeOutsideOfPermittedDatesException.class, dto);
            });
        });
    }

    @Theory
    public void testCreateHarvest_linkToHuntingDay_asModerator(final HarvestSpecimenType specimenType) {
        withMooseHuntingGroupFixture(fixture -> {
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(fixture.group, today());
            final Person author = fixture.groupLeader;
            final Person actor = fixture.groupMember;

            onSavedAndAuthenticated(createNewModerator(), () -> {

                final HarvestDTO inputDto = create(fixture.species)
                        .withSpecimen(specimenType)
                        .withAuthorInfo(author)
                        .withActorInfo(actor)
                        .withModeratorChangeReason("reason")
                        .linkToHuntingDay(huntingDay)
                        .build();

                final HarvestDTO outputDto = invokeCreateHarvest(inputDto);

                runInTransaction(() -> {
                    final Harvest harvest = assertHarvestCreated(outputDto.getId());

                    // Description cannot be set by moderator.
                    assertThat(harvest.getDescription(), is(nullValue()));

                    assertAuthorAndActor(harvest, F.getId(author), F.getId(actor));

                    assertAcceptanceToHuntingDay(harvest, huntingDay, null);
                });
            });
        });
    }

    @Theory
    public void testUpdateHarvest_linkToHuntingDay(final HarvestSpecimenType specimenType) {
        withMooseHuntingGroupFixture(fixture -> {
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(fixture.group, today());

            final Person author = fixture.groupMember;
            final Person acceptor = fixture.groupLeader;

            final Harvest harvest = model().newHarvest(fixture.species, author);
            harvest.setGeoLocation(fixture.zoneCentroid);
            harvest.setAmount(1);

            final HarvestSpecimen specimen = model().newHarvestSpecimen(harvest, specimenType, getDefaultSpecVersion());

            onSavedAndAuthenticated(createUser(acceptor), () -> {

                final HarvestDTO dto = create(harvest)
                        .withSpecimensMappedFrom(asList(specimen))
                        .linkToHuntingDay(huntingDay)
                        .build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest updated = harvestRepo.getOne(dto.getId());
                    assertVersion(updated, 1);

                    assertAuthorAndActor(updated, F.getId(author), F.getId(author));

                    assertAcceptanceToHuntingDay(updated, huntingDay, acceptor);
                });
            });
        });
    }

    @Theory
    public void testUpdateHarvest_linkToHuntingDay_asModerator(final HarvestSpecimenType specimenType) {
        withMooseHuntingGroupFixture(fixture -> {
            final Person originalAuthor = fixture.clubContact;

            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(fixture.group, today());

            final Harvest harvest = model().newHarvest(fixture.species, originalAuthor, huntingDay.getStartDate());

            onSavedAndAuthenticated(createNewModerator(), () -> {

                final Person newAuthor = fixture.groupLeader;
                final Person newActor = fixture.groupMember;

                final String originalDescription = harvest.getDescription();

                final HarvestDTO dto = create(harvest)
                        .withSpecimen(specimenType)
                        .mutate()
                        .withDescription(null)
                        .withAuthorInfo(newAuthor)
                        .withActorInfo(newActor)
                        .linkToHuntingDay(huntingDay)
                        .build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest updated = harvestRepo.getOne(harvest.getId());
                    assertVersion(updated, 1);

                    // Assert mutations.
                    assertThat(updated.getGeoLocation(), is(dto.getGeoLocation()));
                    assertThat(updated.getPointOfTime(), is(DateUtil.toDateTimeNullSafe(dto.getPointOfTime())));

                    // Description should be unchanged.
                    assertThat(updated.getDescription(), is(originalDescription));

                    assertAuthorAndActor(updated, F.getId(newAuthor), F.getId(newActor));

                    assertAcceptanceToHuntingDay(updated, huntingDay, null);
                });
            });
        });
    }

    @Theory
    public void testUpdateHarvest_whenAlreadyAssociatedWithHuntingDay_asModerator(final HarvestSpecimenType specimenType) {
        withMooseHuntingGroupFixture(f -> {
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today());

            final Person author = f.clubContact;

            final Harvest harvest = model().newHarvest(f.species, author, huntingDay.getStartDate());
            final HarvestSpecimen specimen = model().newHarvestSpecimen(harvest, specimenType, getDefaultSpecVersion());

            harvest.updateHuntingDayOfGroup(huntingDay, null);

            onSavedAndAuthenticated(createNewModerator(), () -> {

                final HarvestDTO dto = create(harvest)
                        .withSpecimensMappedFrom(asList(specimen))
                        .mutate()
                        .withAuthorInfo(author)
                        .withActorInfo(author)
                        .linkToHuntingDay(huntingDay)
                        .build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest updated = harvestRepo.getOne(harvest.getId());
                    assertVersion(updated, 1);

                    // Assert mutations.
                    assertThat(updated.getGeoLocation(), is(dto.getGeoLocation()));
                    assertThat(updated.getPointOfTime(), is(DateUtil.toDateTimeNullSafe(dto.getPointOfTime())));

                    assertAuthorAndActor(updated, F.getId(author), F.getId(author));

                    // Assert that hunting day is unchanged.
                    assertAcceptanceToHuntingDay(updated, huntingDay, null);
                });
            });
        });
    }

    @Theory
    public void testUpdateHarvest_linkToHuntingDay_acceptorNotChangedOnModeatorUpdate(final HarvestSpecimenType specimenType) {
        withMooseHuntingGroupFixture(fixture -> {
            final Person author = fixture.groupLeader;
            final Person acceptor = fixture.groupLeader;

            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(fixture.group, today());

            final Harvest harvest = model().newHarvest(fixture.species, author);
            harvest.setAmount(1);

            final HarvestSpecimen specimen = model().newHarvestSpecimen(harvest, specimenType, getDefaultSpecVersion());

            onSavedAndAuthenticated(createUser(acceptor), () -> {
                final HarvestDTO inputDto = create(harvest)
                        .withSpecimensMappedFrom(asList(specimen))
                        .linkToHuntingDay(huntingDay)
                        .build();

                final HarvestDTO outputDto = invokeUpdateHarvest(inputDto);

                runInTransaction(() -> {
                    final Harvest updated = harvestRepo.getOne(harvest.getId());
                    assertVersion(updated, 1);

                    assertAuthorAndActor(updated, F.getId(author), F.getId(author));

                    assertAcceptanceToHuntingDay(updated, huntingDay, acceptor);
                });

                onSavedAndAuthenticated(createNewModerator(), () -> {
                    // Use output of first update as input of second update.
                    outputDto.setRev(1);
                    invokeUpdateHarvest(outputDto);

                    runInTransaction(() -> {
                        final Harvest updated2 = harvestRepo.getOne(harvest.getId());
                        assertVersion(updated2, 2);

                        assertAuthorAndActor(updated2, F.getId(author), F.getId(author));

                        // Assert that acceptor is unchanged.
                        assertAcceptanceToHuntingDay(updated2, huntingDay, acceptor);
                    });
                });
            });
        });
    }

    // Test that harvest is not mutated (except for description/images) when hunting is finished.
    @Theory
    public void testUpdateHarvest_whenHuntingFinished(final HarvestSpecimenType specimenType) {
        withMooseHuntingGroupFixture(f -> {

            final LocalDate today = today();
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today);
            final GroupHuntingDay huntingDay2 = model().newGroupHuntingDay(f.group, today.minusDays(1));

            final Person originalAuthor = f.clubContact;
            final Person newAuthor = f.groupMember;

            final GameSpecies originalSpecies = f.species;
            final Harvest harvest = model().newHarvest(originalSpecies, originalAuthor, huntingDay);
            final HarvestSpecimen specimen = model().newHarvestSpecimen(harvest, specimenType, getDefaultSpecVersion());

            // Intermediary flush needed before persisting MooseHuntingSummary in order to have
            // harvest_permit_partners table populated required for foreign key constraint.
            persistInNewTransaction();

            // Set club hunting finished.
            model().newMooseHuntingSummary(f.permit, f.club, true);

            final GameSpecies newSpecies = model().newDeerSubjectToClubHunting();

            onSavedAndAuthenticated(createUser(originalAuthor), () -> {

                final HarvestDTO dto = create(harvest, newSpecies)
                        .withSpecimensMappedFrom(asList(specimen))
                        .mutate()
                        .withAuthorInfo(newAuthor)
                        .withActorInfo(newAuthor)
                        .linkToHuntingDay(huntingDay2)
                        .build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest updated = harvestRepo.getOne(dto.getId());
                    assertVersion(updated, 1);

                    // Assert that description is changed.
                    assertThat(updated.getDescription(), is(dto.getDescription()));

                    // Assert that other harvest fields are NOT changed.

                    assertThat(updated.getSpecies().getOfficialCode(), is(originalSpecies.getOfficialCode()));
                    assertThat(updated.getGeoLocation(), is(harvest.getGeoLocation()));
                    assertThat(updated.getPointOfTime(), is(harvest.getPointOfTime()));

                    assertAuthorAndActor(updated, F.getId(originalAuthor), F.getId(originalAuthor));

                    // Hunting day should be unchanged.
                    assertAcceptanceToHuntingDay(updated, huntingDay, null);
                });
            });
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
                model().newHarvestSpecimen(harvest, ADULT_FEMALE);

                onSavedAndAuthenticated(user, () -> {
                    final Long harvestId = harvest.getId();

                    try {
                        feature.deleteHarvest(harvestId);
                        fail("Deletion of harvest associated with a hunting day should fail");
                    } catch (final RuntimeException e) {
                        if (!user.isModeratorOrAdmin() && e instanceof AccessDeniedException) {
                            fail("Should not have failed because of insufficient permissions");
                        }
                    }
                    assertThat(harvestRepo.findById(harvestId).isPresent(), is(true));
                });
            });

            reset();
        });
    }
}

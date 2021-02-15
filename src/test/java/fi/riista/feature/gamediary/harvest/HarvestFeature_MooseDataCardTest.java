package fi.riista.feature.gamediary.harvest;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.fixture.HarvestDTOBuilderFactory;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.LocalDate;
import org.junit.Test;

import static fi.riista.util.DateUtil.today;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class HarvestFeature_MooseDataCardTest extends HarvestFeatureTestBase
        implements HarvestDTOBuilderFactory, HuntingGroupFixtureMixin {

    // Test that harvest is not mutated (except for description/images) when group is created
    // within moose data card import.
    @Test
    public void testUpdateHarvest_whenGroupOriginatingFromMooseDataCard_asClubContact() {
        withMooseHuntingGroupFixture(f -> {
            f.group.setFromMooseDataCard(true);

            final LocalDate today = today();
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today);
            final GroupHuntingDay huntingDay2 = model().newGroupHuntingDay(f.group, today.minusDays(1));

            final Person originalAuthor = f.clubContact;
            final Person acceptor = f.groupLeader;
            final Person newAuthor = f.groupMember;

            final GameSpecies originalSpecies = f.species;
            final Harvest original = model().newHarvest(originalSpecies, originalAuthor, huntingDay, acceptor);

            final GameSpecies newSpecies = model().newDeerSubjectToClubHunting();

            onSavedAndAuthenticated(createUser(originalAuthor), () -> {

                final HarvestDTO dto = create(original, newSpecies, 1)
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
                    assertThat(updated.getGeoLocation(), is(original.getGeoLocation()));
                    assertThat(updated.getPointOfTime(), is(original.getPointOfTime()));

                    assertAuthorAndActor(updated, F.getId(originalAuthor), F.getId(originalAuthor));

                    // Hunting day should be unchanged.
                    assertAcceptanceToHuntingDay(updated, huntingDay, acceptor);
                });
            });
        });
    }

    @Test
    public void testUpdateHarvest_whenGroupOriginatingFromMooseDataCard_asModerator() {
        withMooseHuntingGroupFixture(f -> {
            f.group.setFromMooseDataCard(true);

            final LocalDate today = today();
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today);
            final GroupHuntingDay huntingDay2 = model().newGroupHuntingDay(f.group, today.minusDays(1));

            final Person originalAuthor = f.clubContact;
            final Person newAuthor = f.groupMember;

            final Harvest original = model().newHarvest(f.species, originalAuthor, huntingDay);

            final String originalDescription = original.getDescription();

            onSavedAndAuthenticated(createNewModerator(), () -> {

                final HarvestDTO dto = create(original, 1)
                        .mutate()
                        .withAuthorInfo(newAuthor)
                        .withActorInfo(newAuthor)
                        .linkToHuntingDay(huntingDay2)
                        .build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest updated = harvestRepo.getOne(dto.getId());
                    assertVersion(updated, 1);

                    // Assert that description is unchanged.
                    assertThat(updated.getDescription(), is(originalDescription));

                    // Assert that other harvest fields are updated.

                    assertThat(updated.getGeoLocation(), is(dto.getGeoLocation()));
                    assertThat(updated.getPointOfTime(), is(DateUtil.toDateTimeNullSafe(dto.getPointOfTime())));

                    assertAuthorAndActor(updated, F.getId(newAuthor), F.getId(newAuthor));

                    // Hunting day should be updated.
                    assertAcceptanceToHuntingDay(updated, huntingDay2, null);
                });
            });
        });
    }
}

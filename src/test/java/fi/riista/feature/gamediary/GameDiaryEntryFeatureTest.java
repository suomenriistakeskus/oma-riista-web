package fi.riista.feature.gamediary;

import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationRepository;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import static fi.riista.test.Asserts.assertThat;
import static fi.riista.util.EqualityHelper.equalIdAndContent;
import static fi.riista.util.EqualityHelper.equalNotNull;
import static fi.riista.util.Filters.hasAnyIdOf;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public abstract class GameDiaryEntryFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    protected HarvestRepository harvestRepo;

    @Resource
    protected ObservationRepository observationRepo;

    protected Harvest getHarvest(final Long id) {
        final Optional<Harvest> harvestOpt = harvestRepo.findById(id);

        assertThat(harvestOpt.isPresent(), is(true),
                format("Harvest with ID=%d not found", id));

        return harvestOpt.get();
    }

    protected Observation getObservation(final Long id) {
        final Optional<Observation> observationOpt = observationRepo.findById(id);

        assertThat(observationOpt.isPresent(), is(true),
                format("Observation with ID=%d not found", id));

        return observationOpt.get();
    }

    protected Harvest assertHarvestCreated(final Long id) {
        final Harvest harvest = getHarvest(id);
        assertVersion(harvest, 0);
        return harvest;
    }

    protected Observation assertObservationCreated(final Long id) {
        final Observation observation = getObservation(id);
        assertVersion(observation, 0);
        return observation;
    }

    protected static <T extends GameDiaryEntry> void assertAcceptanceToHuntingDay(
            @Nonnull final T diaryEntry,
            @Nonnull final GroupHuntingDay expectedHuntingDay,
            @Nullable final Person acceptor) {

        requireNonNull(diaryEntry);
        requireNonNull(expectedHuntingDay);

        assertThat(diaryEntry.getHuntingDayOfGroup(), is(equalTo(expectedHuntingDay)));
        assertThat(diaryEntry.getPointOfTimeApprovedToHuntingDay(), is(notNullValue()));
        assertThat(diaryEntry.getApproverToHuntingDay(), is(equalTo(acceptor)));
    }

    protected void assertAuthorAndActor(@Nonnull final GameDiaryEntry diaryEntry,
                                        @Nonnull final Long expectedAuthorId,
                                        @Nullable final Long expectedActorId) {
        requireNonNull(diaryEntry);
        requireNonNull(expectedAuthorId);

        final Long nonNullActorId = Optional.ofNullable(expectedActorId).orElse(expectedAuthorId);

        assertThat(diaryEntry.getAuthor().getId(), is(equalTo(expectedAuthorId)), "Wrong author");
        assertThat(diaryEntry.getActor().getId(), is(equalTo(nonNullActorId)), "Wrong actor");
    }

    protected static <ENTITY extends HasID<Long>, DTO extends HasID<Long>> void assertSpecimens(
            @Nonnull final List<ENTITY> specimens,
            @Nonnull final List<DTO> expectedSpecimens,
            @Nonnull final BiFunction<ENTITY, DTO, Boolean> compareFn) {

        assertThat(specimens, hasSize(expectedSpecimens.size()));

        final Map<Boolean, List<DTO>> dtoPartitionByIdExistence = F.partition(expectedSpecimens, F::hasId);
        final List<DTO> expectedUpdatedSpecimens = dtoPartitionByIdExistence.get(true);
        final Map<Boolean, List<ENTITY>> entityPartition = F.partition(specimens, hasAnyIdOf(expectedUpdatedSpecimens));

        assertThat(equalIdAndContent(entityPartition.get(true), expectedUpdatedSpecimens, compareFn), is(true));
        assertThat(equalNotNull(entityPartition.get(false), dtoPartitionByIdExistence.get(false), compareFn), is(true));
    }
}

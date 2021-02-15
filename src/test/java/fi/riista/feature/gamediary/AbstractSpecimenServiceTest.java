package fi.riista.feature.gamediary;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.test.TestUtils;
import fi.riista.util.EqualityHelper;
import fi.riista.util.F;
import fi.riista.util.TransactionalTaskExecutor;
import fi.riista.util.TransactionalVersionedTestExecutionSupport;
import io.vavr.Tuple2;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.google.common.collect.Lists.partition;
import static fi.riista.test.Asserts.assertThat;
import static fi.riista.test.TestUtils.expectIllegalArgumentException;
import static fi.riista.test.TestUtils.expectNPE;
import static fi.riista.test.TestUtils.expectOutOfBoundsSpecimenAmountException;
import static fi.riista.test.TestUtils.expectRevisionConflictException;
import static fi.riista.test.TestUtils.times;
import static fi.riista.util.Filters.hasAnyIdOf;
import static fi.riista.util.Filters.idNotAnyOf;
import static fi.riista.util.Functions.idAndVersion;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

public abstract class AbstractSpecimenServiceTest<PARENT extends GameDiaryEntry, ENTITY extends BaseEntity<Long>, DTO extends BaseEntityDTO<Long>, VERSION extends GameDiaryEntitySpecVersion>
        extends EmbeddedDatabaseTest implements TransactionalVersionedTestExecutionSupport<VERSION> {

    private static <T> Map<Boolean, List<T>> oddEvenPartition(final Iterable<? extends T> objects) {
        final AtomicInteger i = new AtomicInteger(0);
        return F.partition(objects, object -> i.getAndIncrement() % 2 == 0);
    }

    @Override
    public TransactionalTaskExecutor getTransactionalExecutor() {
        return super.getTransactionalExecutor();
    }

    // Exposed public because implementing TransactionalVersionedTestExecutionSupport.
    @Override
    public void reset() {
        super.reset();
    }

    protected abstract AbstractSpecimenService<PARENT, ENTITY, DTO, VERSION> getService();

    protected abstract SpecimenTestOps<PARENT, ENTITY, DTO> getSpecimenTestOps(PARENT parent, VERSION version);

    protected abstract List<ENTITY> findSpecimensInInsertionOrder(PARENT diaryEntry);

    protected abstract PARENT newParent();

    protected PARENT newPersistentParent() {
        final PARENT parent = newParent();
        persistInCurrentlyOpenTransaction();
        return parent;
    }

    @Test
    public void testAddSpecimens_withNullParent() {
        forEachVersion(expectNPE(version -> invokeAdd(null, 1, emptyList(), version)));
    }

    @Test
    public void testAddSpecimens_withNullDtoList() {
        testAllVersions(expectNPE(ctx -> {

            ctx.invokeAdd(1, null);

        })).accept(this::newPersistentParent);
    }

    @Test
    public void testAddSpecimens_withTransientParentEntity() {
        testAllVersions(expectIllegalArgumentException(ctx -> {

            ctx.invokeAdd(1, ctx.createDTOs(1));

        })).accept(/* transient */this::newParent);
    }

    @Test
    public void testAddSpecimens_whenTotalAmountTooLow() {
        testAllVersions(expectOutOfBoundsSpecimenAmountException(ctx -> {

            final int minAmount = ctx.getMinAmount();
            ctx.invokeAdd(minAmount - 1, ctx.createDTOs(minAmount));

        })).accept(this::newPersistentParent);
    }

    @Test
    public void testAddSpecimens_whenTotalAmountTooHigh() {
        testAllVersions(expectOutOfBoundsSpecimenAmountException(ctx -> {

            ctx.invokeAdd(ctx.getMaxAmount() + 1, ctx.createDTOs(1));

        })).accept(this::newPersistentParent);
    }

    @Test
    public void testAddSpecimens_whenTotalAmountLessThanNumberOfDTOs() {
        testAllVersions(expectOutOfBoundsSpecimenAmountException(ctx -> {

            final int minAmount = ctx.getMinAmount();
            ctx.invokeAdd(minAmount, ctx.createDTOs(minAmount + 1));

        })).accept(this::newPersistentParent);
    }

    @Test
    public void testAddSpecimens() {
        testAllVersions(ctx -> {

            final List<DTO> dtos = ctx.createDTOs(5);
            final List<ENTITY> persistedSpecimens = ctx.invokeAdd(dtos.size(), dtos);
            assertThat(ctx.equalContent(persistedSpecimens, dtos), is(true));

        }).accept(this::newPersistentParent);
    }

    @Test
    public void testAddSpecimens_whenSomeSpecimensNotHavingContent() {
        testAllVersions(ctx -> {

            final List<DTO> allDtos = new ArrayList<>();
            final List<DTO> dtosWithContent = new ArrayList<>();

            for (int i = 0; i < 10; i++) {
                final DTO dtoWithContent = ctx.createDTO();
                dtosWithContent.add(dtoWithContent);
                allDtos.add(dtoWithContent);

                final DTO dtoWithNoContent = ctx.createDTO();
                ctx.clearContent(dtoWithNoContent);
                allDtos.add(dtoWithNoContent);
            }

            final List<ENTITY> persistedSpecimens = ctx.invokeAdd(allDtos.size(), allDtos);

            assertThat(persistedSpecimens, hasSize(dtosWithContent.size()));
            assertThat(ctx.equalContent(persistedSpecimens, dtosWithContent), is(true));

        }).accept(this::newPersistentParent);
    }

    @Test
    public void testSetSpecimens_withNullParent() {
        forEachVersion(expectNPE(version -> invokeSet(null, 1, emptyList(), version)));
    }

    @Test
    public void testSetSpecimens_withNullDtoList() {
        testAllVersions(expectNPE(ctx -> {

            ctx.invokeSet(1, null);

        })).accept(this::newPersistentParent);
    }

    @Test
    public void testSetSpecimens_withTransientParentEntity() {
        testAllVersions(expectIllegalArgumentException(ctx -> {

            ctx.invokeSet(1, ctx.createDTOs(1));

        })).accept(/* transient */this::newParent);
    }

    @Test
    public void testSetSpecimens_whenTotalAmountTooLow() {
        testAllVersions(expectOutOfBoundsSpecimenAmountException(ctx -> {

            final int minAmount = ctx.getMinAmount();
            ctx.invokeSet(minAmount - 1, ctx.createDTOs(minAmount));

        })).accept(this::newPersistentParent);
    }

    @Test
    public void testSetSpecimens_whenTotalAmountTooHigh() {
        testAllVersions(expectOutOfBoundsSpecimenAmountException(ctx -> {

            ctx.invokeSet(ctx.getMaxAmount() + 1, ctx.createDTOs(1));

        })).accept(this::newPersistentParent);
    }

    @Test
    public void testSetSpecimens_whenTotalAmountLessThanNumberOfDTOs() {
        testAllVersions(expectOutOfBoundsSpecimenAmountException(ctx -> {

            final int minAmount = ctx.getMinAmount();
            ctx.invokeSet(minAmount, ctx.createDTOs(minAmount + 1));

        })).accept(this::newPersistentParent);
    }

    @Test
    public void testSetSpecimens_withRevisionConflict() {
        testAllVersions(expectRevisionConflictException(ctx -> {

            final ENTITY specimen = ctx.createSpecimen();
            persistInCurrentlyOpenTransaction();

            final DTO dto = ctx.transform(specimen);
            ctx.mutateContent(dto);
            ctx.invokeSet(1, singletonList(dto));

            ctx.mutateContent(dto);
            dto.setRev(0);
            ctx.invokeSet(1, singletonList(dto));

        })).accept(this::newParent);
    }

    @Test
    public void testSetSpecimens_whenAllDtosHaveId() {
        testAllVersions(ctx -> {

            ctx.createSpecimens(2);
            persistInCurrentlyOpenTransaction();

            final List<ENTITY> specimens = ctx.findExistingSpecimensForParentInInsertionOrder();
            final List<DTO> updatedDtos = ctx.transform(specimens);
            ctx.mutateContent(updatedDtos);

            final List<ENTITY> updatedEntities = ctx.invokeSet(updatedDtos.size(), updatedDtos);
            assertThat(ctx.equalIdAndContent(updatedEntities, updatedDtos), is(true));

        }).accept(this::newParent);
    }

    @Test
    public void testSetSpecimens_addRemoveAndPreserveExisting() {
        testAllVersions(ctx -> {

            final int numInitialSpecimens = 5;

            ctx.createSpecimens(numInitialSpecimens);
            persistInCurrentlyOpenTransaction();

            final List<List<ENTITY>> partitioned =
                    partition(ctx.findExistingSpecimensForParentInInsertionOrder(), numInitialSpecimens / 2 + 1);

            final List<ENTITY> initialEntitiesToBeUpdated = partitioned.get(0);
            final List<ENTITY> initialEntitiesToBeRemoved = partitioned.get(1);

            final List<DTO> updatedDtos = ctx.transform(initialEntitiesToBeUpdated);
            ctx.mutateContent(updatedDtos);

            final List<DTO> newDtos = ctx.createDTOs(2);
            final List<DTO> newAndToBeUpdatedDtos = F.concat(updatedDtos, newDtos);

            final List<ENTITY> specimensAfterUpdate =
                    ctx.invokeSet(newAndToBeUpdatedDtos.size(), newAndToBeUpdatedDtos);

            // Assert removed
            assertThat(specimensAfterUpdate.stream().anyMatch(hasAnyIdOf(initialEntitiesToBeRemoved)), is(false));

            // Assert updated
            final List<ENTITY> updatedEntities =
                    F.filterToList(specimensAfterUpdate, hasAnyIdOf(initialEntitiesToBeUpdated));
            assertThat(ctx.equalIdAndContent(updatedEntities, updatedDtos), is(true));
            updatedEntities.forEach(e -> assertVersion(e, 1));

            // Assert newly-added
            final List<ENTITY> newEntities =
                    F.filterToList(specimensAfterUpdate, idNotAnyOf(initialEntitiesToBeUpdated));
            assertThat(ctx.equalContent(newEntities, newDtos), is(true));
            newEntities.forEach(e -> assertVersion(e, 0));

        }).accept(this::newParent);
    }

    @Test
    public void testSetSpecimens_assertCrossParentReferencesNotAllowed() {
        testAllVersions(ctx -> {

            final PARENT anotherParent = newParent();
            anotherParent.setSpecies(ctx.getParent().getSpecies());

            final ENTITY spe1 = ctx.createSpecimen();
            final ENTITY spe2 = ctx.createSpecimen(anotherParent);

            persistInCurrentlyOpenTransaction();

            final List<DTO> dtos = ctx.transform(asList(spe1, spe2));

            try {
                ctx.invokeSet(dtos.size(), dtos);
                fail(appendVersion(
                        "Specimens cross-referencing multiple parents should not be allowed", ctx.getVersion()));
            } catch (final IllegalArgumentException e) {
                // Success
            }

            final List<ENTITY> specimensAfter = ctx.findExistingSpecimensForParentInInsertionOrder();

            assertThat(specimensAfter, hasSize(1));
            assertThat(ctx.equalIdAndContent(specimensAfter, singletonList(ctx.transform(spe1))), is(true));

        }).accept(this::newParent);
    }

    @Test
    public void testSetSpecimens_whenSomeOfExistingSpecimensAreDepleted() {
        testAllVersions(ctx -> {

            ctx.createSpecimens(10);
            persistInCurrentlyOpenTransaction();

            final List<DTO> allDtos = ctx.transform(ctx.findExistingSpecimensForParentInInsertionOrder());

            final Map<Boolean, List<DTO>> oddEvenPartitioning = oddEvenPartition(allDtos);

            oddEvenPartitioning.get(true).forEach(ctx::clearContent);

            final List<ENTITY> updatedSpecimens = ctx.invokeSet(allDtos.size(), allDtos);

            final List<DTO> dtosWithContent = oddEvenPartitioning.get(false);

            assertThat(updatedSpecimens, hasSize(dtosWithContent.size()));
            assertThat(ctx.equalIdAndContent(updatedSpecimens, dtosWithContent), is(true));

        }).accept(this::newParent);
    }

    @Test
    public void testSetSpecimens_whenSomeOfNewSpecimensNotHavingContent() {
        testAllVersions(ctx -> {

            ctx.createSpecimens(10);
            persistInCurrentlyOpenTransaction();

            final List<DTO> newDtos = ctx.createDTOs(20);
            final Map<Boolean, List<DTO>> oddEvenPartitioning = oddEvenPartition(newDtos);

            oddEvenPartitioning.get(true).forEach(ctx::clearContent);

            final List<ENTITY> persistedSpecimens = ctx.invokeSet(newDtos.size(), newDtos);

            final List<DTO> dtosWithContent = oddEvenPartitioning.get(false);

            assertThat(persistedSpecimens, hasSize(dtosWithContent.size()));
            assertThat(ctx.equalContent(persistedSpecimens, dtosWithContent), is(true));

        }).accept(this::newParent);
    }

    @Test
    public void testSetSpecimens_whenSpecimensNotAltered() {
        testAllVersions(ctx -> {

            ctx.createSpecimens(10);
            persistInCurrentlyOpenTransaction();

            final List<DTO> allDtos = ctx.transform(ctx.findExistingSpecimensForParentInInsertionOrder());

            final List<ENTITY> persistedSpecimens = ctx.invokeSet_expectNoneChanged(allDtos.size(), allDtos);

            assertThat(persistedSpecimens, hasSize(allDtos.size()));
            assertThat(ctx.equalContent(persistedSpecimens, allDtos), is(true));

        }).accept(this::newParent);
    }

    @Test
    public void testSetSpecimens_whenSpecimensListIsEmptyAllAreDeleted() {
        testAllVersions(ctx -> {

            ctx.createSpecimens(3);
            persistInCurrentlyOpenTransaction();

            assertThat(ctx.invokeSet(3, emptyList()), is(empty()));

        }).accept(this::newParent);
    }

    @Test(expected = NullPointerException.class)
    public void testDeleteAllSpecimens_withNull() {
        runInTransaction(() -> getService().deleteAllSpecimens(null));
    }

    @Test
    public void testDeleteAllSpecimens() {
        testAllVersions(ctx -> {

            ctx.createSpecimens(10);
            persistInCurrentlyOpenTransaction();
            getService().deleteAllSpecimens(ctx.getParent());
            assertThat(ctx.findExistingSpecimensForParentInInsertionOrder(), is(empty()));

        }).accept(this::newParent);
    }

    protected List<ENTITY> invokeAdd(final PARENT diaryEntry,
                                     final int totalAmount,
                                     final List<DTO> dtoList,
                                     final VERSION version) {

        final List<ENTITY> result = getService().addSpecimens(diaryEntry, totalAmount, dtoList, version);
        result.forEach(s -> assertVersion(s, 0));

        // Do this result transformation before the following query (that triggers flush).
        final List<Tuple2<Long, Integer>> resultIdsAndVersions = F.mapNonNullsToList(result, idAndVersion());

        final List<ENTITY> persistentSpecimens = findSpecimensInInsertionOrder(diaryEntry);

        // Assert that returned list is same than is persisted and that the specimens are not in
        // dirty state (previous query triggered flush causing dirty state to be persisted).
        assertThat(resultIdsAndVersions, equalTo(F.mapNonNullsToList(persistentSpecimens, idAndVersion())));

        return persistentSpecimens;
    }

    protected List<ENTITY> invokeSet(final PARENT diaryEntry,
                                     final int totalAmount,
                                     final List<DTO> dtoList,
                                     final VERSION version) {

        return invokeSet(diaryEntry, totalAmount, dtoList, version, true);
    }

    protected List<ENTITY> invokeSet(final PARENT diaryEntry,
                                     final int totalAmount,
                                     final List<DTO> dtoList,
                                     final VERSION version,
                                     final boolean changesExpected) {

        final Tuple2<List<ENTITY>, Boolean> result =
                getService().setSpecimens(diaryEntry, totalAmount, dtoList, version);
        assertThat(result._2, is(changesExpected));

        // Do this result transformation before the following query that triggers flush.
        final List<Tuple2<Long, Integer>> resultIdsAndVersions = F.mapNonNullsToList(result._1, idAndVersion());

        final List<ENTITY> persistentSpecimens = findSpecimensInInsertionOrder(diaryEntry);

        // Assert that returned list is same than is persisted and that the specimens are not in
        // dirty state (previous query triggered flush causing dirty state to be persisted).
        assertThat(resultIdsAndVersions, equalTo(F.mapNonNullsToList(persistentSpecimens, idAndVersion())));

        return persistentSpecimens;
    }

    protected final BiConsumer<PARENT, VERSION> with(final Consumer<Context> consumer) {
        return (parent, version) -> consumer.accept(new Context(parent, version));
    }

    protected Consumer<Supplier<PARENT>> testAllVersions(final Consumer<Context> execution) {
        return parentSupplier -> forEachVersion(version -> with(execution).accept(parentSupplier.get(), version));
    }

    protected Consumer<Supplier<PARENT>> testAllVersionsBefore(final VERSION versionUpperBound,
                                                               final Consumer<Context> execution) {
        return parentSupplier -> {
            forEachVersionBefore(versionUpperBound, version -> with(execution).accept(parentSupplier.get(), version));
        };
    }

    protected Consumer<Supplier<PARENT>> testAllVersionsStartingFrom(final VERSION minVersion,
                                                                     final Consumer<Context> execution) {
        return parentSupplier -> {
            forEachVersionStartingFrom(minVersion, version -> with(execution).accept(parentSupplier.get(), version));
        };
    }

    protected String appendVersion(final String s, final VERSION version) {
        final String versionQualifier = "[version: " + (version != null ? version.toString() : "<N/A>") + "]";
        return s == null ? versionQualifier : s + " " + versionQualifier;
    }

    protected interface SpecimenTestOps<PARENT, ENTITY, DTO> {

        int getMinAmount();

        int getMaxAmount();

        ENTITY createSpecimen(@Nullable PARENT diaryEntry);

        DTO createDTO();

        DTO transform(@Nonnull ENTITY entity);

        // Mutates business fields but not ID and rev.
        void mutateContent(@Nonnull DTO dto);

        // Clears all business fields except ID and rev.
        void clearContent(@Nonnull DTO dto);

        boolean equalContent(@Nonnull ENTITY entity, @Nonnull DTO dto);
    }

    protected class Context {

        private final PARENT diaryEntry;
        private final VERSION version;
        private final SpecimenTestOps<PARENT, ENTITY, DTO> ops;

        public Context(@Nonnull final PARENT diaryEntry, @Nonnull final VERSION version) {
            this.diaryEntry = requireNonNull(diaryEntry, "diaryEntry is null");
            this.version = requireNonNull(version, "version is null");
            this.ops = getSpecimenTestOps(diaryEntry, version);
        }

        public PARENT getParent() {
            return diaryEntry;
        }

        public VERSION getVersion() {
            return version;
        }

        public int getMinAmount() {
            return ops.getMinAmount();
        }

        public int getMaxAmount() {
            return ops.getMaxAmount();
        }

        public ENTITY createSpecimen() {
            return ops.createSpecimen(diaryEntry);
        }

        public ENTITY createSpecimen(final PARENT differentParent) {
            return ops.createSpecimen(differentParent);
        }

        public void createSpecimens(final int numSpecimens) {
            times(numSpecimens).run(this::createSpecimen);
        }

        public DTO createDTO() {
            return ops.createDTO();
        }

        public List<DTO> createDTOs(final int numberOfElements) {
            return TestUtils.createList(numberOfElements, this::createDTO);
        }

        public DTO transform(@Nonnull final ENTITY entity) {
            return ops.transform(entity);
        }

        public List<DTO> transform(@Nonnull final Collection<ENTITY> entities) {
            requireNonNull(entities);
            return entities.stream().map(this::transform).collect(toList());
        }

        public void mutateContent(@Nonnull final DTO dto) {
            ops.mutateContent(dto);
        }

        public void mutateContent(@Nonnull final Collection<? extends DTO> dtos) {
            requireNonNull(dtos);
            dtos.forEach(this::mutateContent);
        }

        public void clearContent(@Nonnull final DTO dto) {
            ops.clearContent(dto);
        }

        public boolean equalContent(@Nonnull final Iterable<ENTITY> entities, @Nonnull final Iterable<DTO> dtos) {
            return EqualityHelper.equalNotNull(entities, dtos, ops::equalContent);
        }

        public boolean equalIdAndContent(@Nonnull final Iterable<ENTITY> entities, @Nonnull final Iterable<DTO> dtos) {
            return EqualityHelper.equalIdAndContent(entities, dtos, ops::equalContent);
        }

        public List<ENTITY> findExistingSpecimensForParentInInsertionOrder() {
            return AbstractSpecimenServiceTest.this.findSpecimensInInsertionOrder(diaryEntry);
        }

        public List<ENTITY> invokeAdd(final int totalAmount, final List<DTO> dtoList) {
            return AbstractSpecimenServiceTest.this.invokeAdd(requireParent(), totalAmount, dtoList, version);
        }

        public List<ENTITY> invokeSet(final int totalAmount, final List<DTO> dtoList) {
            return AbstractSpecimenServiceTest.this.invokeSet(requireParent(), totalAmount, dtoList, version, true);
        }

        public List<ENTITY> invokeSet_expectNoneChanged(final int totalAmount, final List<DTO> dtoList) {
            return AbstractSpecimenServiceTest.this.invokeSet(requireParent(), totalAmount, dtoList, version, false);
        }

        private PARENT requireParent() {
            return requireNonNull(diaryEntry, "parent must not be null");
        }
    }
}

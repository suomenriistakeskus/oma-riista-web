package fi.riista.feature.gamediary;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.partition;
import static fi.riista.util.Asserts.assertEmpty;
import static fi.riista.util.Filters.hasAnyIdOf;
import static fi.riista.util.Filters.idNotAnyOf;
import static fi.riista.util.Functions.idAndVersion;
import static fi.riista.util.TestUtils.expectIllegalArgumentException;
import static fi.riista.util.TestUtils.expectNPE;
import static fi.riista.util.TestUtils.expectRevisionConflictException;
import static fi.riista.util.TestUtils.times;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.test.TransactionalTaskExecutor;
import fi.riista.util.EqualityHelper;
import fi.riista.util.F;
import fi.riista.util.TestUtils;
import fi.riista.util.TransactionalVersionedTestExecutionSupport;

import javaslang.Tuple2;

import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

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

    protected abstract SpecimenTestOps<PARENT, ENTITY, DTO> getSpecimenTestOps(GameSpecies species, VERSION version);

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
        testAllVersions(expectIllegalArgumentException(ctx -> {

            final int minAmount = ctx.getMinAmount();
            ctx.invokeAdd(minAmount - 1, ctx.createDTOs(minAmount));

        })).accept(this::newPersistentParent);
    }

    @Test
    public void testAddSpecimens_whenTotalAmountTooHigh() {
        testAllVersions(expectIllegalArgumentException(ctx -> {

            ctx.invokeAdd(ctx.getMaxAmount() + 1, ctx.createDTOs(1));

        })).accept(this::newPersistentParent);
    }

    @Test
    public void testAddSpecimens_whenTotalAmountLessThanNumberOfDTOs() {
        testAllVersions(expectIllegalArgumentException(ctx -> {

            final int minAmount = ctx.getMinAmount();
            ctx.invokeAdd(minAmount, ctx.createDTOs(minAmount + 1));

        })).accept(this::newPersistentParent);
    }

    @Test
    public void testAddSpecimens() {
        testAllVersions(ctx -> {

            final List<DTO> dtos = ctx.createDTOs(5);
            final List<ENTITY> persistedSpecimens = ctx.invokeAdd(dtos.size(), dtos);
            assertTrue(ctx.equalContent(persistedSpecimens, dtos));

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

            assertEquals(dtosWithContent.size(), persistedSpecimens.size());
            assertTrue(ctx.equalContent(persistedSpecimens, dtosWithContent));

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
        testAllVersions(expectIllegalArgumentException(ctx -> {

            final int minAmount = ctx.getMinAmount();
            ctx.invokeSet(minAmount - 1, ctx.createDTOs(minAmount));

        })).accept(this::newPersistentParent);
    }

    @Test
    public void testSetSpecimens_whenTotalAmountTooHigh() {
        testAllVersions(expectIllegalArgumentException(ctx -> {

            ctx.invokeSet(ctx.getMaxAmount() + 1, ctx.createDTOs(1));

        })).accept(this::newPersistentParent);
    }

    @Test
    public void testSetSpecimens_whenTotalAmountLessThanNumberOfDTOs() {
        testAllVersions(expectIllegalArgumentException(ctx -> {

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
            assertTrue(ctx.equalIdAndContent(updatedEntities, updatedDtos));

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
            final List<DTO> newAndToBeUpdatedDtos = newArrayList(concat(updatedDtos, newDtos));

            final List<ENTITY> specimensAfterUpdate =
                    ctx.invokeSet(newAndToBeUpdatedDtos.size(), newAndToBeUpdatedDtos);

            // Assert removed
            assertFalse(specimensAfterUpdate.stream().anyMatch(hasAnyIdOf(initialEntitiesToBeRemoved)));

            // Assert updated
            final List<ENTITY> updatedEntities =
                    F.filterToList(specimensAfterUpdate, hasAnyIdOf(initialEntitiesToBeUpdated));
            assertTrue(ctx.equalIdAndContent(updatedEntities, updatedDtos));
            updatedEntities.forEach(e -> assertVersion(e, 1));

            // Assert newly-added
            final List<ENTITY> newEntities =
                    F.filterToList(specimensAfterUpdate, idNotAnyOf(initialEntitiesToBeUpdated));
            assertTrue(ctx.equalContent(newEntities, newDtos));
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

            assertEquals(1, specimensAfter.size());
            assertTrue(ctx.equalIdAndContent(specimensAfter, singletonList(ctx.transform(spe1))));

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

            assertEquals(dtosWithContent.size(), updatedSpecimens.size());
            assertTrue(ctx.equalIdAndContent(updatedSpecimens, dtosWithContent));

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

            assertEquals(dtosWithContent.size(), persistedSpecimens.size());
            assertTrue(ctx.equalContent(persistedSpecimens, dtosWithContent));

        }).accept(this::newParent);
    }

    @Test
    public void testSetSpecimens_whenSpecimensNotAltered() {
        testAllVersions(ctx -> {

            ctx.createSpecimens(10);
            persistInCurrentlyOpenTransaction();

            final List<DTO> allDtos = ctx.transform(ctx.findExistingSpecimensForParentInInsertionOrder());

            final List<ENTITY> persistedSpecimens = ctx.invokeSet_assertNoChanges(allDtos.size(), allDtos);

            assertEquals(allDtos.size(), persistedSpecimens.size());
            assertTrue(ctx.equalContent(persistedSpecimens, allDtos));

        }).accept(this::newParent);
    }

    @Test
    public void testSetSpecimens_whenSpecimensListIsEmptyAllAreDeleted() {
        testAllVersions(ctx -> {

            ctx.createSpecimens(3);
            persistInCurrentlyOpenTransaction();

            assertEmpty(ctx.invokeSet(3, emptyList()));

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
            assertEmpty(ctx.findExistingSpecimensForParentInInsertionOrder());

        }).accept(this::newParent);
    }

    protected List<ENTITY> invokeAdd(
            final PARENT diaryEntry, final int totalAmount, final List<DTO> dtoList, final VERSION version) {

        final List<ENTITY> result = getService().addSpecimens(diaryEntry, totalAmount, dtoList, version);
        result.forEach(s -> assertVersion(s, 0));

        // Do this result transformation before the following query (that triggers flush).
        final List<Tuple2<Long, Integer>> resultIdsAndVersions = result.stream().map(idAndVersion()).collect(toList());

        final List<ENTITY> persistentSpecimens = findSpecimensInInsertionOrder(diaryEntry);

        // Assert that returned list is same than is persisted and that the specimens are not in
        // dirty state (previous query triggered flush causing dirty state to be persisted).
        assertEquals(persistentSpecimens.stream().map(idAndVersion()).collect(toList()), resultIdsAndVersions);

        return persistentSpecimens;
    }

    protected List<ENTITY> invokeSet(
            final PARENT diaryEntry, final int totalAmount, final List<DTO> dtoList, final VERSION version) {

        return invokeSet(diaryEntry, totalAmount, dtoList, version, true);
    }

    protected List<ENTITY> invokeSet(
            final PARENT diaryEntry,
            final int totalAmount,
            final List<DTO> dtoList,
            final VERSION version,
            final boolean changesExpected) {

        final Tuple2<List<ENTITY>, Boolean> result =
                getService().setSpecimens(diaryEntry, totalAmount, dtoList, version);
        assertEquals(changesExpected, result._2);

        // Do this result transformation before the following query that triggers flush.
        final List<Tuple2<Long, Integer>> resultIdsAndVersions =
                result._1.stream().map(idAndVersion()).collect(toList());

        final List<ENTITY> persistentSpecimens = findSpecimensInInsertionOrder(diaryEntry);

        // Assert that returned list is same than is persisted and that the specimens are not in
        // dirty state (previous query triggered flush causing dirty state to be persisted).
        assertEquals(persistentSpecimens.stream().map(idAndVersion()).collect(toList()), resultIdsAndVersions);

        return persistentSpecimens;
    }

    protected final BiConsumer<PARENT, VERSION> with(final Consumer<Context> consumer) {
        return (parent, version) -> consumer.accept(new Context(parent, version));
    }

    protected Consumer<Supplier<PARENT>> testAllVersions(final Consumer<Context> execution) {
        return parentSupplier -> forEachVersion(version -> with(execution).accept(parentSupplier.get(), version));
    }

    protected Consumer<Supplier<PARENT>> testAllVersionsBefore(
            final VERSION versionUpperBound, final Consumer<Context> execution) {

        return parentSupplier -> {
            forEachVersionBefore(versionUpperBound, version -> with(execution).accept(parentSupplier.get(), version));
        };
    }

    protected Consumer<Supplier<PARENT>> testAllVersionsStartingFrom(
            final VERSION minVersion, final Consumer<Context> execution) {

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

        // Mutates all fields except ID and rev.
        void mutateContent(@Nonnull DTO dto);

        // Clears all fields except ID and rev.
        void clearContent(@Nonnull DTO dto);

        boolean equalContent(@Nonnull ENTITY entity, @Nonnull DTO dto);
    }

    protected class Context {

        private final PARENT diaryEntry;
        private final VERSION version;
        private final SpecimenTestOps<PARENT, ENTITY, DTO> ops;

        public Context(@Nonnull final PARENT diaryEntry, @Nonnull final VERSION version) {
            this(Objects.requireNonNull(diaryEntry, "diaryEntry is null"), diaryEntry.getSpecies(), version);
        }

        public Context(@Nonnull final GameSpecies species, @Nonnull final VERSION version) {
            this(null, species, version);
        }

        private Context(
                @Nullable final PARENT diaryEntry, @Nonnull final GameSpecies species, @Nonnull final VERSION version) {

            this.diaryEntry = diaryEntry;
            Objects.requireNonNull(species, "species is null");
            this.version = Objects.requireNonNull(version, "version is null");
            this.ops = getSpecimenTestOps(species, version);
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
            Objects.requireNonNull(entities);
            return entities.stream().map(this::transform).collect(toList());
        }

        public void mutateContent(@Nonnull final DTO dto) {
            ops.mutateContent(dto);
        }

        public void mutateContent(@Nonnull final Collection<? extends DTO> dtos) {
            Objects.requireNonNull(dtos);
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

        public List<ENTITY> invokeSet_assertNoChanges(final int totalAmount, final List<DTO> dtoList) {
            return AbstractSpecimenServiceTest.this.invokeSet(requireParent(), totalAmount, dtoList, version, false);
        }

        private PARENT requireParent() {
            return Objects.requireNonNull(diaryEntry, "parent must not be null");
        }
    }

}

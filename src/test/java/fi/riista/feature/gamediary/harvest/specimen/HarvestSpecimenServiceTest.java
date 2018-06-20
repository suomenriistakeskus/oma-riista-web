package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.AbstractSpecimenServiceTest;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.OutOfBoundsSpecimenAmountException;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFields;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import io.vavr.Lazy;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.junit.After;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion.LOWEST_VERSION_SUPPORTING_EXTENDED_MOOSE_FIELDS;
import static fi.riista.test.TestUtils.expectMultipleSpecimenNotAllowedException;
import static fi.riista.test.TestUtils.wrapExceptionExpectation;
import static fi.riista.util.DateUtil.today;
import static fi.riista.util.EqualityHelper.equalIdAndContent;
import static fi.riista.util.jpa.JpaSpecs.equal;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class HarvestSpecimenServiceTest
        extends AbstractSpecimenServiceTest<Harvest, HarvestSpecimen, HarvestSpecimenDTO, HarvestSpecVersion>
        implements HuntingGroupFixtureMixin {

    @Resource
    private HarvestSpecimenRepository repository;

    // Spied instances of HarvestSpecimenOps are cached for a duration of version-specific
    // test execution.
    private final HashMap<Tuple2<Integer, HarvestSpecVersion>, HarvestSpecimenOps> cachedSpecimenOps = new HashMap<>();

    // Invocation times of getSpecimenFieldCopier are tracked for the purpose of ensuring that
    // each such invocation is succeeded by a call to HarvestSpecimenOps.copyContentToEntity.
    private final HashMap<Tuple2<Integer, HarvestSpecVersion>, Integer> invocationTimes = new HashMap<>();

    private Supplier<HarvestSpecimenService> serviceSupplier = Lazy.of(() -> {
        // HarvestSpecimenService and HarvestSpecimenOps are mocked for the purpose of ensuring that
        // HarvestSpecimenOps is integrated properly into HarvestSpecimenService.

        final HarvestSpecimenService service = spy(new HarvestSpecimenService(repository));

        final Function<InvocationOnMock, Tuple2<Integer, HarvestSpecVersion>> keyFn = invocation -> {
            final Object[] arguments = invocation.getArguments();

            final Harvest harvest = Objects.requireNonNull((Harvest) arguments[0]);
            final GameSpecies species = Objects.requireNonNull(harvest.getSpecies());

            return Tuple.of(species.getOfficialCode(), (HarvestSpecVersion) arguments[1]);
        };

        doAnswer(invocation -> {

            final Tuple2<Integer, HarvestSpecVersion> key = keyFn.apply(invocation);
            HarvestSpecimenOps specimenOps = cachedSpecimenOps.get(key);

            if (specimenOps != null) {
                return specimenOps;
            }

            specimenOps = spy((HarvestSpecimenOps) invocation.callRealMethod());
            cachedSpecimenOps.put(key, specimenOps);
            return specimenOps;

        }).when(service).getSpecimenOps(any(Harvest.class), any(HarvestSpecVersion.class));

        doAnswer(invocation -> {

            invocationTimes.merge(keyFn.apply(invocation), 1, (oldVal, newVal) -> oldVal + 1);
            return invocation.callRealMethod();

        }).when(service).getSpecimenFieldCopier(any(Harvest.class), any(HarvestSpecVersion.class));

        return service;
    });

    @Override
    public List<HarvestSpecVersion> getTestExecutionVersions() {
        return new ArrayList<>(EnumSet.allOf(HarvestSpecVersion.class));
    }

    @After
    public void clearMaps() {
        // Reset state.
        cachedSpecimenOps.clear();
        invocationTimes.clear();
    }

    @Override
    public void onAfterTestExecutionWithinSameTransaction() {
        // HarvestSpecimenOps contains much of the business logic related to handling of harvest
        // specimens making it is essential to test that it is properly integrated into
        // HarvestSpecimenService.

        assertEquals("Test-internal state corrupted,", cachedSpecimenOps.keySet(), invocationTimes.keySet());

        invocationTimes.forEach((key, invocationCount) -> {
            assertTrue(invocationCount > 0);

            final HarvestSpecimenOps specimenOps = cachedSpecimenOps.get(key);
            assertNotNull("Test-internal state corrupted", specimenOps);

            verify(specimenOps, times(invocationCount))
                    .copyContentToEntity(any(HarvestSpecimenDTO.class), any(HarvestSpecimen.class));
        });

        clearMaps();
    }

    @Override
    protected HarvestSpecimenService getService() {
        return serviceSupplier.get();
    }

    @Override
    protected SpecimenTestOps<Harvest, HarvestSpecimen, HarvestSpecimenDTO> getSpecimenTestOps(
            @Nonnull final GameSpecies species, @Nonnull final HarvestSpecVersion version) {

        return new CustomHarvestSpecimenOps(species, version);
    }

    @Override
    protected Harvest newParent() {
        return model().newHarvest(model().newGameSpecies(true));
    }

    private Harvest newPersistentMooseHarvest() {
        return newPersistentHarvest(GameSpecies.OFFICIAL_CODE_MOOSE, false);
    }

    private Harvest newPersistentHarvest(final int gameSpeciesCode, final boolean multipleSpecimensAllowed) {
        final GameSpecies species = model().newGameSpecies(gameSpeciesCode);
        species.setMultipleSpecimenAllowedOnHarvest(multipleSpecimensAllowed);
        final Harvest harvest = model().newHarvest(species);
        persistInCurrentlyOpenTransaction();
        return harvest;
    }

    @Override
    protected List<HarvestSpecimen> findSpecimensInInsertionOrder(final Harvest harvest) {
        return repository.findAll(equal(HarvestSpecimen_.harvest, harvest), new JpaSort(HarvestSpecimen_.id));
    }

    @Test
    public void testAddSpecimens_whenSpeciesForbidsMultipleSpecimens() {
        testAllVersions(expectMultipleSpecimenNotAllowedException(ctx -> {

            ctx.invokeAdd(2, ctx.createDTOs(2));

        })).accept(this::newPersistentMooseHarvest);
    }

    @Test
    public void testAddSpecimens_whenMandatorySpecimenFieldsMissingWithinClubHunting() {
        testAllVersions(wrapExceptionExpectation(HarvestSpecimenValidationException.class,
                ctx -> {

                    final HarvestSpecimenDTO dto = ctx.createDTO();

                    // Failure expected to be caused by (at least of) missing antler points.
                    dto.setAge(GameAge.ADULT);
                    dto.setGender(GameGender.MALE);
                    dto.setAntlerPointsRight(null);

                    ctx.invokeAdd(1, Collections.singletonList(dto));

                })).accept(() -> {
            final HuntingGroupFixture f = new HuntingGroupFixture(model());
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today());
            final Harvest harvest = model().newHarvest(f.species, f.clubContact, huntingDay.getStartDate());
            harvest.updateHuntingDayOfGroup(huntingDay, null);

            persistInCurrentlyOpenTransaction();
            return harvest;
        });
    }

    @Test
    public void testSetSpecimens_whenSpeciesForbidsMultipleSpecimens() {
        testAllVersions(expectMultipleSpecimenNotAllowedException(ctx -> {

            ctx.invokeSet(2, ctx.createDTOs(2));

        })).accept(this::newPersistentMooseHarvest);
    }

    @Test
    public void testSetSpecimens_whenMandatorySpecimenFieldsMissingWithinClubHunting() {
        testAllVersionsStartingFrom(LOWEST_VERSION_SUPPORTING_EXTENDED_MOOSE_FIELDS,
                wrapExceptionExpectation(HarvestSpecimenValidationException.class, ctx -> {

                    final HarvestSpecimen specimen = ctx.createSpecimen();
                    persistInCurrentlyOpenTransaction();

                    final HarvestSpecimenDTO dto = ctx.transform(specimen);

                    // Failure expected to be caused by missing fitness class.
                    dto.setFitnessClass(null);

                    ctx.invokeSet(1, Collections.singletonList(dto));

                })).accept(() -> {
            final HuntingGroupFixture f = new HuntingGroupFixture(model());
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today());

            final Harvest harvest = model().newHarvest(f.species, f.clubContact, huntingDay.getStartDate());
            harvest.updateHuntingDayOfGroup(huntingDay, null);

            return harvest;
        });
    }

    @Test
    public void testSetSpecimens_whenExtendedMooseFieldsNotSupported() {
        testAllVersionsBefore(LOWEST_VERSION_SUPPORTING_EXTENDED_MOOSE_FIELDS, ctx -> {

            final HarvestSpecimen specimen = ctx.createSpecimen();
            persistInCurrentlyOpenTransaction();

            final HarvestSpecimenDTO dto = ctx.transform(specimen);

            // Actually following two assignments are NOOP but are anyway left here to make it
            // clear that DTO fields are not in sync with entity fields.
            dto.setWeightEstimated(null);
            dto.setWeightMeasured(null);

            ctx.invokeSet_expectNoneChanged(1, Collections.singletonList(dto));

        }).accept(() -> {
            final HuntingGroupFixture f = new HuntingGroupFixture(model());
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today());

            final Harvest harvest = model().newHarvest(f.species, f.clubContact, huntingDay.getStartDate());
            harvest.updateHuntingDayOfGroup(huntingDay, null);

            return harvest;
        });
    }

    @Test(expected = IllegalArgumentException.class)
    @Transactional
    public void testLimitSpecimens_withTransientParentEntity() {
        getService().limitSpecimens(newParent(), 1);
    }

    @Test(expected = OutOfBoundsSpecimenAmountException.class)
    @Transactional
    public void testLimitSpecimens_whenGivenLimitIsLowerThanMinimumAllowedSpecimenAmount() {

        withLatestVersion(ctx -> getService().limitSpecimens(ctx.getParent(), ctx.getMinAmount() - 1))
                .accept(newPersistentParent());

    }

    @Test(expected = OutOfBoundsSpecimenAmountException.class)
    @Transactional
    public void testLimitSpecimens_whenGivenLimitIsHigherThanMaximumAllowedSpecimenAmount() {

        withLatestVersion(ctx -> getService().limitSpecimens(ctx.getParent(), ctx.getMaxAmount() + 1))
                .accept(newPersistentParent());

    }

    @Test(expected = MultipleSpecimenNotAllowedException.class)
    @Transactional
    public void testLimitSpecimens_whenSpeciesForbidsMultipleSpecimens() {
        withLatestVersion(ctx -> {

            getService().limitSpecimens(ctx.getParent(), 2);

        }).accept(newPersistentMooseHarvest());
    }

    @Test
    @Transactional
    public void testLimitSpecimens_whenGivenLimitIsGreaterThanCurrentNumberOfSpecimens() {
        withLatestVersion(ctx -> {

            ctx.createSpecimens(10);
            persistInCurrentlyOpenTransaction();

            final List<HarvestSpecimen> specimensBefore = ctx.findExistingSpecimensForParentInInsertionOrder();
            getService().limitSpecimens(ctx.getParent(), specimensBefore.size() + 15);
            final List<HarvestSpecimen> specimensAfter = ctx.findExistingSpecimensForParentInInsertionOrder();

            assertEquals(specimensBefore.size(), specimensAfter.size());
            assertTrue(equalIdAndContent(specimensBefore, specimensAfter, HarvestSpecimen::hasEqualBusinessFields));

        }).accept(newParent());
    }

    @Test
    @Transactional
    public void testLimitSpecimens_whenGivenLimitIsLowerThanCurrentNumberOfSpecimens() {
        withLatestVersion(ctx -> {

            ctx.createSpecimens(10);
            persistInCurrentlyOpenTransaction();

            final List<HarvestSpecimen> specimensBefore = ctx.findExistingSpecimensForParentInInsertionOrder();

            final int numNewSpecimens = 6;
            getService().limitSpecimens(ctx.getParent(), numNewSpecimens);

            final List<HarvestSpecimen> specimensAfter = ctx.findExistingSpecimensForParentInInsertionOrder();

            assertEquals(numNewSpecimens, specimensAfter.size());

            assertTrue(equalIdAndContent(
                    specimensBefore.stream().limit(numNewSpecimens).collect(toList()),
                    specimensAfter,
                    HarvestSpecimen::hasEqualBusinessFields));

        }).accept(newParent());
    }

    private final Consumer<Harvest> withLatestVersion(Consumer<Context> consumer) {
        return parent -> consumer.accept(new Context(parent, HarvestSpecVersion.MOST_RECENT));
    }

    private class CustomHarvestSpecimenOps extends HarvestSpecimenOpsForTest
            implements SpecimenTestOps<Harvest, HarvestSpecimen, HarvestSpecimenDTO> {

        public CustomHarvestSpecimenOps(@Nonnull final GameSpecies species,
                                        @Nonnull final HarvestSpecVersion specVersion) {

            super(species.getOfficialCode(), specVersion, HarvestSpecimenServiceTest.this.getNumberGenerator());
        }

        @Override
        public HarvestSpecimen createSpecimen(@Nullable final Harvest harvest) {
            return model().newHarvestSpecimen(harvest);
        }

        @Override
        public void mutateContent(@Nonnull final HarvestSpecimenDTO dto) {
            super.mutateContent(dto);
        }

        @Override
        public void clearContent(@Nonnull final HarvestSpecimenDTO dto) {
            Objects.requireNonNull(dto);
            dto.clearBusinessFields();
        }
    }
}

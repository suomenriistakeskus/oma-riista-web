package fi.riista.feature.gamediary.harvest.specimen;

import com.google.common.base.Throwables;
import fi.riista.feature.gamediary.AbstractSpecimenServiceTest;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.util.DateUtil;
import fi.riista.util.NumberGenerator;
import io.vavr.Lazy;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.data.jpa.domain.JpaSort;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static fi.riista.test.Asserts.assertThat;
import static fi.riista.test.TestUtils.expectMultipleSpecimenNotAllowedException;
import static fi.riista.test.TestUtils.wrapExceptionExpectation;
import static fi.riista.util.DateUtil.today;
import static fi.riista.util.jpa.JpaSpecs.equal;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
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

    private final Supplier<HarvestSpecimenService> serviceSupplier = Lazy.of(() -> {
        // HarvestSpecimenService and HarvestSpecimenOps are mocked for the purpose of ensuring that
        // HarvestSpecimenOps is integrated properly into HarvestSpecimenService.

        final HarvestSpecimenService service = spy(new HarvestSpecimenService(repository));

        final Function<InvocationOnMock, Tuple2<Integer, HarvestSpecVersion>> keyFn = invocation -> {
            final Object[] arguments = invocation.getArguments();

            final Harvest harvest = requireNonNull((Harvest) arguments[0]);
            final GameSpecies species = requireNonNull(harvest.getSpecies());

            return Tuple.of(species.getOfficialCode(), (HarvestSpecVersion) arguments[1]);
        };

        doAnswer(invocation -> {

            final Tuple2<Integer, HarvestSpecVersion> key = keyFn.apply(invocation);

            return Optional
                    .ofNullable(cachedSpecimenOps.get(key))
                    .orElseGet(() -> {
                        try {
                            final HarvestSpecimenOps specimenOps = spy((HarvestSpecimenOps) invocation.callRealMethod());
                            cachedSpecimenOps.put(key, specimenOps);
                            return specimenOps;
                        } catch (final Throwable t) {
                            Throwables.throwIfUnchecked(t);
                            throw new RuntimeException(t);
                        }
                    });

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

        assertThat(invocationTimes.keySet(), equalTo(cachedSpecimenOps.keySet()),
                "Test-internal state corrupted,");

        invocationTimes.forEach((key, invocationCount) -> {
            assertThat(invocationCount, greaterThan(0));

            final HarvestSpecimenOps specimenOps = cachedSpecimenOps.get(key);
            assertThat(specimenOps, is(notNullValue()), "Test-internal state corrupted");

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
            @Nonnull final Harvest harvest, @Nonnull final HarvestSpecVersion version) {

        final GameSpecies species = requireNonNull(harvest.getSpecies(), "species is null");
        final DateTime pointOfTime = requireNonNull(harvest.getPointOfTime(), "pointOfTime is null");

        return new HarvestSpecimenOpsForTest(
                species.getOfficialCode(),
                version,
                DateUtil.huntingYearContaining(pointOfTime.toLocalDate()),
                getNumberGenerator());
    }

    @Override
    protected Harvest newParent() {
        return model().newHarvest(model().newGameSpecies(true));
    }

    private Harvest newPersistentMooseHarvest() {
        return newPersistentHarvest(OFFICIAL_CODE_MOOSE, false);
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
        return repository.findAll(equal(HarvestSpecimen_.harvest, harvest), JpaSort.of(HarvestSpecimen_.id));
    }

    @Test
    public void testAddSpecimens_whenSpeciesForbidsMultipleSpecimens() {
        testAllVersions(expectMultipleSpecimenNotAllowedException(ctx -> {

            ctx.invokeAdd(2, ctx.createDTOs(2));

        })).accept(this::newPersistentMooseHarvest);
    }

    @Test
    public void testSetSpecimens_whenSpeciesForbidsMultipleSpecimens() {
        testAllVersions(expectMultipleSpecimenNotAllowedException(ctx -> {

            ctx.invokeSet(2, ctx.createDTOs(2));

        })).accept(this::newPersistentMooseHarvest);
    }

    private class HarvestSpecimenOpsForTest extends HarvestSpecimenPopulator
            implements SpecimenTestOps<Harvest, HarvestSpecimen, HarvestSpecimenDTO> {

        public HarvestSpecimenOpsForTest(final int gameSpeciesCode,
                                         @Nonnull final HarvestSpecVersion specVersion,
                                         final int huntingYear,
                                         @Nonnull final NumberGenerator numberGenerator) {

            super(gameSpeciesCode, specVersion, huntingYear, numberGenerator);
        }

        @Override
        public HarvestSpecimen createSpecimen(final Harvest harvest) {
            return model().newHarvestSpecimen(harvest);
        }

        @Override
        public HarvestSpecimenDTO createDTO() {
            final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();
            mutateContent(dto);
            return dto;
        }

        @Override
        public void mutateContent(@Nonnull final HarvestSpecimenDTO dto) {
            super.mutateContent(dto);
        }

        @Override
        public void clearContent(@Nonnull final HarvestSpecimenDTO dto) {
            requireNonNull(dto);
            dto.clearBusinessFields();
        }

        @Override
        public boolean equalContent(@Nonnull final HarvestSpecimen entity, @Nonnull final HarvestSpecimenDTO dto) {
            return getSpecimenOps().equalContent(entity, dto);
        }

        @Override
        public HarvestSpecimenDTO transform(@Nonnull final HarvestSpecimen entity) {
            return getSpecimenOps().transform(entity);
        }

        public int getMinAmount() {
            return Harvest.MIN_AMOUNT;
        }

        public int getMaxAmount() {
            return Harvest.MAX_AMOUNT;
        }
    }
}

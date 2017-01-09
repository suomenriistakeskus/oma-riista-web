package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.AbstractSpecimenService;
import fi.riista.util.F;

import javaslang.Tuple2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

@Component
public class HarvestSpecimenService
        extends AbstractSpecimenService<Harvest, HarvestSpecimen, HarvestSpecimenDTO, HarvestSpecVersion> {

    private static final Logger LOG = LoggerFactory.getLogger(HarvestSpecimenService.class);

    private HarvestSpecimenRepository repository;

    public HarvestSpecimenService(@Autowired @Nonnull final HarvestSpecimenRepository repository) {
        super(HarvestSpecimen_.harvest);
        this.repository = Objects.requireNonNull(repository);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected HarvestSpecimenRepository getSpecimenRepository() {
        return repository;
    }

    @Override
    protected HarvestSpecimen createSpecimen(@Nonnull final Harvest harvest) {
        Objects.requireNonNull(harvest);
        return new HarvestSpecimen(harvest);
    }

    @Override
    protected boolean hasContent(final HarvestSpecimenDTO dto) {
        return !dto.allBusinessFieldsNull();
    }

    // Used in tests for spying/mocking.
    protected HarvestSpecimenOps getSpecimenOps(@Nonnull final Harvest harvest,
                                                @Nonnull final HarvestSpecVersion version) {

        Objects.requireNonNull(harvest, "harvest is null");
        return new HarvestSpecimenOps(harvest.getSpecies(), version);
    }

    @Override
    protected BiConsumer<HarvestSpecimenDTO, HarvestSpecimen> getSpecimenFieldCopier(
            @Nonnull final Harvest harvest, @Nonnull final HarvestSpecVersion version) {

        final BiConsumer<HarvestSpecimenDTO, HarvestSpecimen> delegate =
                getSpecimenOps(harvest, version)::copyContentToEntity;

        return delegate.andThen((dto, entity) -> {
            if (harvest.getHuntingDayOfGroup() != null) {
                entity.checkAllMandatoryFieldsPresentWithinClubHunting(harvest.getSpecies().getOfficialCode());
            }
        });
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<HarvestSpecimen> limitSpecimens(@Nonnull final Harvest harvest, final int limit) {

        // limitSpecimens is not actually dependent on spec-version so use the most recent value always.
        checkParameters(harvest, limit, Collections.emptyList(), HarvestSpecVersion.MOST_RECENT);

        final List<HarvestSpecimen> existingSpecimens = findExistingSpecimensInInsertionOrder(harvest);

        int numRemoved = 0;
        List<HarvestSpecimen> ret = existingSpecimens;

        if (limit < existingSpecimens.size()) {
            final Tuple2<List<HarvestSpecimen>, List<HarvestSpecimen>> pair = F.split(existingSpecimens, limit);
            final List<HarvestSpecimen> removedSpecimens = pair._2;

            getSpecimenRepository().deleteInBatch(removedSpecimens);
            numRemoved = removedSpecimens.size();
            ret = pair._1;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Harvest(id={}) specimen limit: removed {}, total amount set to {}",
                    harvest.getId(), numRemoved, limit);
        }

        return ret;
    }

    @Override
    protected void checkParameters(final Harvest harvest,
                                   final int totalAmount,
                                   final List<HarvestSpecimenDTO> dtos,
                                   final HarvestSpecVersion specVersion) {

        super.checkParameters(harvest, totalAmount, dtos, specVersion);
        assertSpecimenAmountWithinBounds(totalAmount);
        assertMultipleSpecimenConstraint(harvest, totalAmount);
    }

    private static void assertSpecimenAmountWithinBounds(final int totalAmount) {
        if (totalAmount < Harvest.MIN_AMOUNT || totalAmount > Harvest.MAX_AMOUNT) {
            throw new IllegalArgumentException(String.format(
                    "Total amount of harvest specimens must be between %d and %d",
                    Harvest.MIN_AMOUNT, Harvest.MAX_AMOUNT));
        }
    }

    private static void assertMultipleSpecimenConstraint(final Harvest harvest, final int totalAmount) {
        if (totalAmount > 1 && !harvest.getSpecies().isMultipleSpecimenAllowedOnHarvest()) {
            final GameSpecies species = harvest.getSpecies();
            final String errMsg = String.format(
                    "Multiple harvest specimens not allowed for species: %s (%s)",
                    species.getNameFinnish(),
                    species.getOfficialCode());
            throw new IllegalArgumentException(errMsg);
        }
    }

}

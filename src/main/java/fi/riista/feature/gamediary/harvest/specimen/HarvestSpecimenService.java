package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.AbstractSpecimenService;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.OutOfBoundsSpecimenAmountException;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFields;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import io.vavr.Tuple2;
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
        final GameSpecies gameSpecies = Objects.requireNonNull(harvest.getSpecies(), "species is null");
        return new HarvestSpecimenOps(gameSpecies.getOfficialCode(), version);
    }

    @Override
    protected BiConsumer<HarvestSpecimenDTO, HarvestSpecimen> getSpecimenFieldCopier(
            @Nonnull final Harvest harvest, @Nonnull final HarvestSpecVersion version) {
        return getSpecimenOps(harvest, version)::copyContentToEntity;
    }

    @Override
    protected void validateSpecimen(@Nonnull final Harvest harvest, @Nonnull final List<HarvestSpecimen> specimenList) {
        final int huntingYear = DateUtil.huntingYearContaining(harvest.getPointOfTimeAsLocalDate());
        final int speciesCode = harvest.getSpecies().getOfficialCode();
        final boolean associatedWithHuntingDay = harvest.getHuntingDayOfGroup() != null;
        final RequiredHarvestFields.Specimen specimenFieldRequirements = RequiredHarvestFields.getSpecimenFields(
                huntingYear, speciesCode, harvest.getHuntingMethod(), harvest.resolveReportingType());

        for (final HarvestSpecimen specimen : specimenList) {
            new HarvestSpecimenValidator(specimenFieldRequirements, specimen, speciesCode, associatedWithHuntingDay)
                    .validateAll()
                    .throwOnErrors();
        }

        // Can not be empty if any specimen fields is required
        if (specimenList.isEmpty()) {
            final HarvestSpecimen emptySpecimen = new HarvestSpecimen();

            new HarvestSpecimenValidator(specimenFieldRequirements, emptySpecimen, speciesCode, associatedWithHuntingDay)
                    .validateAll()
                    .throwOnErrors();
        }
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<HarvestSpecimen> limitSpecimens(@Nonnull final Harvest harvest, final int limit) {

        // limitSpecimens is actually not dependent on spec-version so use the most recent value always.
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

        OutOfBoundsSpecimenAmountException.assertHarvestSpecimenAmountWithinBounds(totalAmount);
        MultipleSpecimenNotAllowedException.assertHarvestMultipleSpecimenConstraint(harvest.getSpecies(), totalAmount);
    }
}

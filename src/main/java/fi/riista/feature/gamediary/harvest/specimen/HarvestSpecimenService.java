package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.AbstractSpecimenService;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.OutOfBoundsSpecimenAmountException;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFields;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.BiConsumer;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

@Component
public class HarvestSpecimenService
        extends AbstractSpecimenService<Harvest, HarvestSpecimen, HarvestSpecimenDTO, HarvestSpecVersion> {

    private final HarvestSpecimenRepository repository;

    public HarvestSpecimenService(@Autowired @Nonnull final HarvestSpecimenRepository repository) {
        super(HarvestSpecimen_.harvest);

        this.repository = requireNonNull(repository);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected HarvestSpecimenRepository getSpecimenRepository() {
        return repository;
    }

    @Override
    protected HarvestSpecimen createSpecimen(@Nonnull final Harvest harvest) {
        requireNonNull(harvest);
        return new HarvestSpecimen(harvest);
    }

    @Override
    protected boolean hasContent(final HarvestSpecimenDTO dto) {
        return !dto.allBusinessFieldsNull();
    }

    // Used in tests for spying/mocking.
    protected HarvestSpecimenOps getSpecimenOps(@Nonnull final Harvest harvest,
                                                @Nonnull final HarvestSpecVersion version) {

        requireNonNull(harvest, "harvest is null");

        final GameSpecies gameSpecies = requireNonNull(harvest.getSpecies(), "species is null");
        final DateTime pointOfTime = requireNonNull(harvest.getPointOfTime(), "pointOfTime is null");

        return new HarvestSpecimenOps(
                gameSpecies.getOfficialCode(),
                version,
                DateUtil.huntingYearContaining(pointOfTime.toLocalDate()));
    }

    @Override
    protected BiConsumer<HarvestSpecimenDTO, HarvestSpecimen> getSpecimenFieldCopier(
            @Nonnull final Harvest harvest, @Nonnull final HarvestSpecVersion version) {

        return getSpecimenOps(harvest, version)::copyContentToEntity;
    }

    @Override
    protected void validateInputSpecimens(@Nonnull final Harvest harvest,
                                          @Nonnull final List<HarvestSpecimenDTO> inputSpecimens,
                                          @Nonnull final HarvestSpecVersion version) {
        validateSpecimens(
                harvest,
                // At least one specimen is needed for validation in order to verify that required fields are present.
                !inputSpecimens.isEmpty() ? inputSpecimens : singletonList(new HarvestSpecimenDTO()),
                version,
                /* skipNewAntlerFields */false);
    }

    @Override
    protected void validateResultSpecimens(@Nonnull final Harvest harvest,
                                           @Nonnull final List<HarvestSpecimen> resultSpecimens) {
        validateSpecimens(
                harvest,
                // At least one specimen is needed for validation in order to verify that required fields are present.
                !resultSpecimens.isEmpty() ? resultSpecimens : singletonList(new HarvestSpecimen()),
                HarvestSpecVersion.CURRENTLY_SUPPORTED,
                /* skipNewAntlerFields */true);
    }

    // TODO `skipNewAntlerFields` parameter is provided for the duration of deer pilot 2020.
    //  Because presence of the new antler fields introduced in the deer pilot is dependent on whether the author
    //  of a harvest is a pilot user it is not feasible to validate resulting specimens for the new antler fields.
    //  We need to trust that HarvestSpecimenOps does handling of those fields properly.
    private void validateSpecimens(@Nonnull final Harvest harvest,
                                   @Nonnull final List<? extends HarvestSpecimenBusinessFields> specimens,
                                   @Nonnull final HarvestSpecVersion version,
                                   final boolean skipNewAntlerFields) {

        final int huntingYear = DateUtil.huntingYearContaining(harvest.getPointOfTimeAsLocalDate());
        final int speciesCode = harvest.getSpecies().getOfficialCode();

        final GroupHuntingDay huntingDayOfGroup = harvest.getHuntingDayOfGroup();
        final boolean associatedWithHuntingDay = huntingDayOfGroup != null;
        final boolean legallyMandatoryFieldsOnly = associatedWithHuntingDay && huntingDayOfGroup.isCreatedBySystem();

        final RequiredHarvestFields.Specimen fieldRequirements = RequiredHarvestFields.getSpecimenFields(
                huntingYear, speciesCode, harvest.getHuntingMethod(), harvest.resolveReportingType(),
                legallyMandatoryFieldsOnly, version);

        // It turned out that skipping new antlers fields causes problems only when harvest is associated
        // with a hunting day.
        final boolean skipNewAntlerFieldsWhenAssociatedWithHuntingDay = skipNewAntlerFields && associatedWithHuntingDay;

        for (final HarvestSpecimenBusinessFields specimen : specimens) {
            executeValidation(
                    new HarvestSpecimenValidator(
                            fieldRequirements, specimen, speciesCode, associatedWithHuntingDay),
                    skipNewAntlerFieldsWhenAssociatedWithHuntingDay);
        }
    }

    private void executeValidation(final HarvestSpecimenValidator validator, final boolean skipAntlerFields2020) {
        if (skipAntlerFields2020) {
            validator.validateAge()
                    .validateGender()
                    .validateWeight()
                    .validateMooselikeWeight()
                    .validateNotEdible()
                    .validateFitnessClass()
                    .validateAntlersType()
                    .validateAntlersWidth()
                    .validateAntlerPointsLeft()
                    .validateAntlerPointsRight()
                    .validateAlone()
                    .throwOnErrors();
        } else {
            validator.validateAll().throwOnErrors();
        }
    }

    @Override
    protected void checkParameters(@Nonnull final Harvest harvest,
                                   final int totalAmount,
                                   @Nonnull final List<HarvestSpecimenDTO> dtoList,
                                   @Nonnull final HarvestSpecVersion specVersion) {

        super.checkParameters(harvest, totalAmount, dtoList, specVersion);

        OutOfBoundsSpecimenAmountException.assertHarvestSpecimenAmountWithinBounds(totalAmount);
        MultipleSpecimenNotAllowedException.assertHarvestMultipleSpecimenConstraint(harvest.getSpecies(), totalAmount);
    }
}

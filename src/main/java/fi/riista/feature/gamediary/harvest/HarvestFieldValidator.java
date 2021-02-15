package fi.riista.feature.gamediary.harvest;

import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestField;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFields;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.Set;

import static fi.riista.feature.gamediary.harvest.fields.RequiredHarvestField.NO;
import static fi.riista.feature.gamediary.harvest.fields.RequiredHarvestField.YES;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

public class HarvestFieldValidator {

    private final RequiredHarvestFields.Report requirements;

    private final Harvest harvest;

    private final EnumSet<HarvestFieldName> missingFields = EnumSet.noneOf(HarvestFieldName.class);
    private final EnumSet<HarvestFieldName> illegalFields = EnumSet.noneOf(HarvestFieldName.class);

    public HarvestFieldValidator(@Nonnull final RequiredHarvestFields.Report requirements,
                                 @Nonnull final Harvest harvest) {

        this.requirements = requireNonNull(requirements);
        this.harvest = requireNonNull(harvest);
    }

    public HarvestFieldValidator validateAll() {
        return validateHuntingMethod()
                .validateFeedingPlace()
                .validateTaigaBeanGoose()
                .validateHuntingAreaType()
                .validateHuntingAreaSize()
                .validateReportedWithPhoneCall()
                .validateHuntingParty()
                .validateDeerHuntingType();
    }

    public HarvestFieldValidator validateHuntingMethod() {
        validateField(HarvestFieldName.HUNTING_METHOD, requirements.getHuntingMethod(), harvest.getHuntingMethod());
        return this;
    }

    public HarvestFieldValidator validateFeedingPlace() {
        validateField(HarvestFieldName.FEEDING_PLACE, requirements.getFeedingPlace(), harvest.getFeedingPlace());
        return this;
    }

    public HarvestFieldValidator validateTaigaBeanGoose() {
        final boolean valueDefined = harvest.isTaigaBeanGoose() || harvest.isTundraBeanGoose();
        validateField(HarvestFieldName.TAIGA_BEAN_GOOSE, requirements.getTaigaBeanGoose(), valueDefined ? true : null);
        return this;
    }

    public HarvestFieldValidator validateHuntingAreaType() {
        validateField(HarvestFieldName.HUNTING_AREA_TYPE, requirements.getHuntingAreaType(), harvest.getHuntingAreaType());
        return this;
    }

    public HarvestFieldValidator validateHuntingAreaSize() {
        validateField(HarvestFieldName.HUNTING_AREA_SIZE, requirements.getHuntingAreaSize(), harvest.getHuntingAreaSize());
        return this;
    }

    public HarvestFieldValidator validateReportedWithPhoneCall() {
        validateField(HarvestFieldName.REPORTED_WITH_PHONE_CALL, requirements.getReportedWithPhoneCall(), harvest.getReportedWithPhoneCall());
        return this;
    }

    public HarvestFieldValidator validateDeerHuntingType() {
        validateField(HarvestFieldName.DEER_HUNTING_TYPE, requirements.getDeerHuntingType(), harvest.getDeerHuntingType());
        return this;
    }

    public HarvestFieldValidator validateHuntingParty() {
        final String huntingParty = harvest.getHuntingParty();
        final boolean emptyOrNullValue = StringUtils.isBlank(huntingParty);

        if (harvest.getHuntingAreaType() != HuntingAreaType.HUNTING_SOCIETY) {
            if (!emptyOrNullValue) {
                illegal(HarvestFieldName.HUNTING_PARTY);
            }
            return this;
        }

        if (requirements.getHuntingParty() == NO) {
            if (!emptyOrNullValue) {
                illegal(HarvestFieldName.HUNTING_PARTY);
            }
        } else if (requirements.getHuntingParty() == YES) {
            if (emptyOrNullValue) {
                missing(HarvestFieldName.HUNTING_PARTY);
            }
        }
        return this;
    }

    private void validateField(final HarvestFieldName fieldName,
                               final RequiredHarvestField requirement,
                               final Object fieldValue) {
        switch (requirement) {
            case YES:
                mustNotBeNull(fieldName, fieldValue);
                break;
            case VOLUNTARY:
                // Either null or non-null value will pass.
                break;
            case NO:
                mustBeNull(fieldName, fieldValue);
                break;
            default:
                throw new IllegalArgumentException("Unsupported RequiredHarvestField: " + requirement);
        }
    }

    public Set<HarvestFieldName> getMissingFields() {
        return unmodifiableSet(missingFields);
    }

    public Set<HarvestFieldName> getIllegalFields() {
        return unmodifiableSet(illegalFields);
    }

    public boolean hasErrors() {
        return !missingFields.isEmpty() || !illegalFields.isEmpty();
    }

    public void throwOnErrors() {
        if (hasErrors()) {
            throw new HarvestFieldValidationException(illegalFields, missingFields);
        }
    }

    private void mustBeNull(final HarvestFieldName fieldName, final Object value) {
        if (value != null) {
            illegal(fieldName);
        }
    }

    private void mustNotBeNull(final HarvestFieldName fieldName, final Object value) {
        if (value == null) {
            missing(fieldName);
        }
    }

    private void illegal(final HarvestFieldName fieldName) {
        illegalFields.add(requireNonNull(fieldName));
    }

    private void missing(final HarvestFieldName fieldName) {
        missingFields.add(requireNonNull(fieldName));
    }

}

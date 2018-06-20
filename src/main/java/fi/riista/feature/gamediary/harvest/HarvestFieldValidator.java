package fi.riista.feature.gamediary.harvest;

import fi.riista.feature.common.entity.FieldPresence;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFields;
import org.apache.commons.lang.StringUtils;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

public class HarvestFieldValidator {
    private final RequiredHarvestFields.Report requirements;
    private final EnumSet<HarvestFieldName> missingFields = EnumSet.noneOf(HarvestFieldName.class);
    private final EnumSet<HarvestFieldName> illegalFields = EnumSet.noneOf(HarvestFieldName.class);
    private final Harvest harvest;

    public HarvestFieldValidator(final RequiredHarvestFields.Report requirements,
                                 final Harvest harvest) {
        this.requirements = requirements;
        this.harvest = harvest;
    }

    public HarvestFieldValidator validateAll() {
        return validateHuntingMethod()
                .validateFeedingPlace()
                .validateTaigaBeanGoose()
                .validateHuntingAreaType()
                .validateHuntingAreaSize()
                .validateReportedWithPhoneCall()
                .validateHuntingParty();
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

    public HarvestFieldValidator validateHuntingParty() {
        final String huntingParty = harvest.getHuntingParty();
        final boolean emptyOrNullValue = StringUtils.isBlank(huntingParty);

        if (harvest.getHuntingAreaType() != HuntingAreaType.HUNTING_SOCIETY) {
            if (!emptyOrNullValue) {
                illegal(HarvestFieldName.HUNTING_PARTY);
            }
            return this;
        }

        if (requirements.getHuntingParty().nullValueRequired()) {
            if (!emptyOrNullValue) {
                illegal(HarvestFieldName.HUNTING_PARTY);
            }
        } else if (requirements.getHuntingParty().nonNullValueRequired()) {
            if (emptyOrNullValue) {
                missing(HarvestFieldName.HUNTING_PARTY);
            }
        }
        return this;
    }

    private void validateField(HarvestFieldName fieldName,
                               FieldPresence required,
                               Object fieldValue) {
        if (required.nonNullValueRequired()) {
            mustNotBeNull(fieldName, fieldValue);
        } else if (required.nullValueRequired()) {
            mustBeNull(fieldName, fieldValue);
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
        illegalFields.add(Objects.requireNonNull(fieldName));
    }

    private void missing(final HarvestFieldName fieldName) {
        missingFields.add(Objects.requireNonNull(fieldName));
    }

}

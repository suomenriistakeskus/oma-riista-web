package fi.riista.feature.gamediary.harvest.fields;

import fi.riista.feature.gamediary.harvest.HarvestReportingType;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.HuntingMethod;

public class RequiredHarvestFields {

    public static RequiredHarvestFields.Report getFormFields(final int huntingYear,
                                                             final int gameSpeciesCode,
                                                             final HarvestReportingType reportingType,
                                                             final boolean legallyMandatoryOnly,
                                                             final boolean isDeerPilotEnabled) {
        if (legallyMandatoryOnly) {
            return LegallyMandatoryFieldsMooselike.getFormFields(gameSpeciesCode, isDeerPilotEnabled);
        }

        return RequiredHarvestFieldsImpl.getFormFields(huntingYear, gameSpeciesCode, reportingType, isDeerPilotEnabled);
    }

    public static RequiredHarvestFields.Specimen getSpecimenFields(final int huntingYear,
                                                                   final int gameSpeciesCode,
                                                                   final HuntingMethod huntingMethod,
                                                                   final HarvestReportingType reportingType,
                                                                   final boolean legallyMandatoryOnly,
                                                                   final HarvestSpecVersion specVersion) {

        final boolean isClientSupportFor2020Fields = specVersion.supportsAntlerFields2020();

        if (legallyMandatoryOnly) {
            return LegallyMandatoryFieldsMooselike
                    .getSpecimenFields(huntingYear, gameSpeciesCode, isClientSupportFor2020Fields);
        }

        return RequiredHarvestFieldsImpl.getSpecimenFields(
                huntingYear, gameSpeciesCode, huntingMethod, reportingType, isClientSupportFor2020Fields);
    }

    public interface Report {

        RequiredHarvestField getPermitNumber();

        RequiredHarvestField getHarvestArea();

        RequiredHarvestField getHuntingMethod();

        RequiredHarvestField getFeedingPlace();

        RequiredHarvestField getTaigaBeanGoose();

        RequiredHarvestField getLukeStatus();

        RequiredHarvestField getHuntingAreaType();

        RequiredHarvestField getHuntingParty();

        RequiredHarvestField getHuntingAreaSize();

        RequiredHarvestField getReportedWithPhoneCall();

        RequiredHarvestField getDeerHuntingType();
    }

    public interface Specimen {

        RequiredHarvestSpecimenField getAge();

        RequiredHarvestSpecimenField getGender();

        RequiredHarvestSpecimenField getWeight();

        RequiredHarvestSpecimenField getWeightEstimated();

        RequiredHarvestSpecimenField getWeightMeasured();

        RequiredHarvestSpecimenField getAdditionalInfo();

        RequiredHarvestSpecimenField getNotEdible();

        RequiredHarvestSpecimenField getFitnessClass();

        RequiredHarvestSpecimenField getAntlersLost();

        RequiredHarvestSpecimenField getAntlersType();

        RequiredHarvestSpecimenField getAntlersWidth();

        RequiredHarvestSpecimenField getAntlerPoints();

        RequiredHarvestSpecimenField getAntlersGirth();

        RequiredHarvestSpecimenField getAntlersLength();

        RequiredHarvestSpecimenField getAntlersInnerWidth();

        RequiredHarvestSpecimenField getAntlerShaftWidth();

        RequiredHarvestSpecimenField getAlone();
    }
}

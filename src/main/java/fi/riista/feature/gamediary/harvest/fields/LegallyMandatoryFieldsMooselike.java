package fi.riista.feature.gamediary.harvest.fields;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.HasGameSpeciesCode;

import static com.google.common.base.Preconditions.checkArgument;

public class LegallyMandatoryFieldsMooselike {

    public static Report getFormFields(final int gameSpeciesCode, final boolean isDeerPilotEnabled) {
        return new Report(gameSpeciesCode, isDeerPilotEnabled);
    }

    public static Specimen getSpecimenFields(final int huntingYear,
                                             final int gameSpeciesCode,
                                             final boolean isClientSupportFor2020Fields) {

        return new Specimen(huntingYear, gameSpeciesCode, isClientSupportFor2020Fields);
    }

    public static class Report implements RequiredHarvestFields.Report {

        private final boolean isDeerPilotEnabled;

        protected Report(final int gameSpeciesCode, final boolean isDeerPilotEnabled) {
            checkArgument(GameSpecies.isMooseOrDeerRequiringPermitForHunting(gameSpeciesCode));
            this.isDeerPilotEnabled = isDeerPilotEnabled;
        }

        @Override
        public RequiredHarvestField getPermitNumber() {
            return RequiredHarvestField.NO;
        }

        @Override
        public RequiredHarvestField getHarvestArea() {
            return RequiredHarvestField.NO;
        }

        @Override
        public RequiredHarvestField getHuntingMethod() {
            return RequiredHarvestField.NO;
        }

        @Override
        public RequiredHarvestField getFeedingPlace() {
            return RequiredHarvestField.NO;
        }

        @Override
        public RequiredHarvestField getTaigaBeanGoose() {
            return RequiredHarvestField.NO;
        }

        @Override
        public RequiredHarvestField getLukeStatus() {
            return RequiredHarvestField.NO;
        }

        @Override
        public RequiredHarvestField getDeerHuntingType() {
            return isDeerPilotEnabled ? RequiredHarvestField.VOLUNTARY : RequiredHarvestField.NO;
        }

        // legacy for metsäkauris, was mandatory for season before 2017
        @Override
        @Deprecated
        public RequiredHarvestField getHuntingAreaType() {
            return RequiredHarvestField.NO;
        }

        // legacy for metsäkauris, was voluntary for season before 2017
        @Override
        @Deprecated
        public RequiredHarvestField getHuntingParty() {
            return RequiredHarvestField.NO;
        }

        // legacy for metsäkauris, was mandatory for season before 2017
        @Override
        @Deprecated
        public RequiredHarvestField getHuntingAreaSize() {
            return RequiredHarvestField.NO;
        }

        // legacy for bear before 2015
        @Override
        @Deprecated
        public RequiredHarvestField getReportedWithPhoneCall() {
            return RequiredHarvestField.NO;
        }
    }

    public static class Specimen implements RequiredHarvestFields.Specimen, HasGameSpeciesCode {

        private final int gameSpeciesCode;
        private final boolean fields2020Enabled;

        protected Specimen(final int huntingYear,
                           final int gameSpeciesCode,
                           final boolean isClientSupportFor2020Fields) {

            this.gameSpeciesCode = gameSpeciesCode;
            this.fields2020Enabled = isClientSupportFor2020Fields && huntingYear >= 2020;

            checkArgument(isMooseOrDeerRequiringPermitForHunting());
        }

        @Override
        public int getGameSpeciesCode() {
            return gameSpeciesCode;
        }

        @Override
        public RequiredHarvestSpecimenField getAge() {
            return RequiredHarvestSpecimenField.YES;
        }

        @Override
        public RequiredHarvestSpecimenField getGender() {
            return RequiredHarvestSpecimenField.YES;
        }

        @Override
        public RequiredHarvestSpecimenField getWeight() {
            return RequiredHarvestSpecimenField.VOLUNTARY;
        }

        @Override
        public RequiredHarvestSpecimenField getWeightEstimated() {
            return RequiredHarvestSpecimenField.VOLUNTARY;
        }

        @Override
        public RequiredHarvestSpecimenField getWeightMeasured() {
            return RequiredHarvestSpecimenField.VOLUNTARY;
        }

        @Override
        public RequiredHarvestSpecimenField getAdditionalInfo() {
            return RequiredHarvestSpecimenField.VOLUNTARY;
        }

        @Override
        public RequiredHarvestSpecimenField getNotEdible() {
            return RequiredHarvestSpecimenField.VOLUNTARY;
        }

        @Override
        public RequiredHarvestSpecimenField getFitnessClass() {
            return isMoose() ? RequiredHarvestSpecimenField.VOLUNTARY : RequiredHarvestSpecimenField.NO;
        }

        @Override
        public RequiredHarvestSpecimenField getAntlersLost() {
            return fields2020Enabled
                    ? RequiredHarvestSpecimenField.YES_IF_ADULT_MALE
                    : RequiredHarvestSpecimenField.NO;
        }

        @Override
        public RequiredHarvestSpecimenField getAntlersType() {
            return fields2020Enabled
                    ? voluntaryIfAntlersPresentAnd(isMoose())
                    : voluntaryIfAdultMaleAnd(isMoose());
        }

        @Override
        public RequiredHarvestSpecimenField getAntlersWidth() {
            if (!fields2020Enabled) {
                return voluntaryIfAdultMaleAnd(isMooseOrDeerRequiringPermitForHunting());
            }

            if (isWhiteTailedDeer()) {
                return RequiredHarvestSpecimenField.DEPRECATED_ANTLER_DETAIL;
            }

            return voluntaryIfAntlersPresentAnd(isFallowDeer() || isMoose() || isWildForestReindeer());
        }

        @Override
        public RequiredHarvestSpecimenField getAntlerPoints() {
            return fields2020Enabled
                    ? voluntaryIfAntlersPresentAnd(true)
                    : voluntaryIfAdultMaleAnd(true);
        }

        @Override
        public RequiredHarvestSpecimenField getAntlersGirth() {
            return voluntaryIfAntlersPresentAnd(fields2020Enabled && (isMoose() || isWhiteTailedDeer()));
        }

        @Override
        public RequiredHarvestSpecimenField getAntlersLength() {
            return voluntaryIfAntlersPresentAnd(fields2020Enabled && (isRoeDeer() || isWhiteTailedDeer()));
        }

        @Override
        public RequiredHarvestSpecimenField getAntlersInnerWidth() {
            return voluntaryIfAntlersPresentAnd(fields2020Enabled && isWhiteTailedDeer());
        }

        @Override
        public RequiredHarvestSpecimenField getAntlerShaftWidth() {
            return voluntaryIfAntlersPresentAnd(fields2020Enabled && isRoeDeer());
        }

        @Override
        public RequiredHarvestSpecimenField getAlone() {
            return isMoose() ? RequiredHarvestSpecimenField.VOLUNTARY_IF_YOUNG : RequiredHarvestSpecimenField.NO;
        }

        private static RequiredHarvestSpecimenField voluntaryIfAdultMaleAnd(final boolean conditionMatched) {
            return conditionMatched ? RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE : RequiredHarvestSpecimenField.NO;
        }

        private static RequiredHarvestSpecimenField voluntaryIfAntlersPresentAnd(final boolean conditionMatched) {
            return conditionMatched
                    ? RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT
                    : RequiredHarvestSpecimenField.NO;
        }
    }
}

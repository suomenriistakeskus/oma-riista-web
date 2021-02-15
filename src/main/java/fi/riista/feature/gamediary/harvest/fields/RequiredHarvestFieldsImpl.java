package fi.riista.feature.gamediary.harvest.fields;

import com.google.common.collect.ImmutableSet;
import fi.riista.feature.gamediary.HasGameSpeciesCode;
import fi.riista.feature.gamediary.harvest.HarvestReportingType;
import fi.riista.feature.gamediary.harvest.HuntingMethod;

import static fi.riista.feature.gamediary.harvest.HarvestReportingType.BASIC;
import static fi.riista.feature.gamediary.harvest.HarvestReportingType.HUNTING_DAY;
import static fi.riista.feature.gamediary.harvest.HarvestReportingType.PERMIT;
import static fi.riista.feature.gamediary.harvest.HarvestReportingType.SEASON;

public class RequiredHarvestFieldsImpl {

    public static ReportImpl getFormFields(final int huntingYear,
                                           final int gameSpeciesCode,
                                           final HarvestReportingType reportingType,
                                           final boolean isDeerPilotEnabled) {

        return new ReportImpl(huntingYear, gameSpeciesCode, reportingType, isDeerPilotEnabled);
    }

    public static SpecimenImpl getSpecimenFields(final int huntingYear,
                                                 final int gameSpeciesCode,
                                                 final HuntingMethod huntingMethod,
                                                 final HarvestReportingType reportingType,
                                                 final boolean isClientSupportFor2020Fields) {

        return new SpecimenImpl(huntingYear, gameSpeciesCode, huntingMethod, reportingType, isClientSupportFor2020Fields);
    }

    public static class ReportImpl implements RequiredHarvestFields.Report, HasGameSpeciesCode {

        private final int huntingYear;
        private final int gameSpeciesCode;
        private final HarvestReportingType reportingType;
        private final boolean isDeerPilotEnabled;

        protected ReportImpl(final int huntingYear,
                             final int gameSpeciesCode,
                             final HarvestReportingType reportingType,
                             final boolean isDeerPilotEnabled) {

            this.huntingYear = huntingYear;
            this.gameSpeciesCode = gameSpeciesCode;
            this.reportingType = reportingType;
            this.isDeerPilotEnabled = isDeerPilotEnabled;
        }

        @Override
        public int getGameSpeciesCode() {
            return gameSpeciesCode;
        }

        @Override
        public RequiredHarvestField getPermitNumber() {
            return requiredIf(reportingType == PERMIT);
        }

        @Override
        public RequiredHarvestField getHarvestArea() {
            return requiredIf(reportingType == SEASON && (isBear() || isGreySeal()));
        }

        @Override
        public RequiredHarvestField getHuntingMethod() {
            return requiredIf(isGreySeal() && reportingType != BASIC);
        }

        @Override
        public RequiredHarvestField getFeedingPlace() {
            return voluntaryIf(isWildBoar() && reportingType != BASIC);
        }

        @Override
        public RequiredHarvestField getTaigaBeanGoose() {
            return voluntaryIf(isBeanGoose() && reportingType != BASIC);
        }

        @Override
        public RequiredHarvestField getLukeStatus() {
            return voluntaryIf(isWolf() && reportingType == SEASON);
        }

        @Override
        public RequiredHarvestField getDeerHuntingType() {
            // TODO Remove deer pilot condition when pilot is over.
            return isDeerPilotEnabled && huntingYear >= 2020 && isWhiteTailedDeer() && reportingType != PERMIT
                    ? RequiredHarvestField.VOLUNTARY
                    : RequiredHarvestField.NO;
        }

        // legacy for metsäkauris, was mandatory for season before 2017
        @Override
        @Deprecated
        public RequiredHarvestField getHuntingAreaType() {
            if (isRoeDeer() && reportingType == SEASON) {
                return huntingYear < 2017 ? RequiredHarvestField.YES : RequiredHarvestField.VOLUNTARY;
            }

            return RequiredHarvestField.NO;
        }

        // legacy for metsäkauris, was voluntary for season before 2017
        @Override
        @Deprecated
        public RequiredHarvestField getHuntingParty() {
            // reportingType=SEASON speciesCode=47507 huntingYear=2017 huntingDay=false has missing fields
            // [HUNTING_PARTY] and invalid fields []
            return voluntaryIf(huntingYear < 2017 && isRoeDeer() && reportingType == SEASON);
        }

        // legacy for metsäkauris, was mandatory for season before 2017
        @Override
        @Deprecated
        public RequiredHarvestField getHuntingAreaSize() {
            return requiredIf(huntingYear < 2017 && isRoeDeer() && reportingType == SEASON);
        }

        // legacy for bear before 2015
        @Override
        @Deprecated
        public RequiredHarvestField getReportedWithPhoneCall() {
            return requiredIf(huntingYear == 2014 && isBear() && reportingType == SEASON);
        }

        private static RequiredHarvestField requiredIf(final boolean condition) {
            return condition ? RequiredHarvestField.YES : RequiredHarvestField.NO;
        }

        private static RequiredHarvestField voluntaryIf(final boolean condition) {
            return condition ? RequiredHarvestField.VOLUNTARY : RequiredHarvestField.NO;
        }
    }

    public static class SpecimenImpl implements RequiredHarvestFields.Specimen, HasGameSpeciesCode {

        // {mufloni,saksanhirvi,japaninpeura,halli,susi,ahma,karhu,hirvi,kuusipeura,valkohäntäpeura,metsäpeura,
        // villisika,saukko,ilves}
        static final ImmutableSet<Integer> PERMIT_MANDATORY_AGE = ImmutableSet
                .of(47774, 47476, 47479, 47282, 46549, 47212, 47348, 47503, 47484, 47629, 200556, 47926, 47169, 46615);

        // {villisika,saukko,ilves,piisami,rämemajava,"tarhattu naali",pesukarhu,hilleri,kirjohylje,mufloni,
        // saksanhirvi,japaninpeura ,halli,susi,"villiintynyt kissa",metsäjänis,rusakko,orava,kanadanmajava,kettu,
        // kärppä,näätä,minkki,villikani,supikoira,mäyrä,itämerennorppa,euroopanmajava,ahma,karhu,metsäkauris,hirvi,
        // kuusipeura,valkohäntäpeura,metsäpeura}
        static final ImmutableSet<Integer> PERMIT_MANDATORY_GENDER = ImmutableSet
                .of(47926, 47169, 46615, 48537, 50336, 46542, 47329, 47240, 47305, 47774, 47476, 47479, 47282, 46549,
                        53004, 50106, 50386, 48089, 48250, 46587, 47230, 47223, 47243, 50114, 46564, 47180, 200555,
                        48251, 47212, 47348, 47507, 47503, 47484, 47629, 200556);

        // {halli,susi,saukko,ilves,ahma,karhu}
        static final ImmutableSet<Integer> PERMIT_MANDATORY_WEIGHT = ImmutableSet
                .of(47282, 46549, 47169, 46615, 47212, 47348);

        // {karhu,metsäkauris,halli,villisika}
        private static final ImmutableSet<Integer> SEASON_COMMON_MANDATORY = ImmutableSet
                .of(47348, 47507, 47282, 47926);

        private final int huntingYear;
        private final int gameSpeciesCode;
        private final HarvestReportingType reportingType;
        private final HuntingMethod huntingMethod;
        private final boolean fields2020Enabled;

        private final boolean associatedToHuntingDay;

        /*package*/ SpecimenImpl(final int huntingYear,
                                 final int gameSpeciesCode,
                                 final HuntingMethod huntingMethod,
                                 final HarvestReportingType reportingType,
                                 final boolean isClientSupportFor2020Fields) {

            this.huntingYear = huntingYear;
            this.gameSpeciesCode = gameSpeciesCode;
            this.reportingType = reportingType;
            this.huntingMethod = huntingMethod;
            this.fields2020Enabled = isClientSupportFor2020Fields && huntingYear >= 2020;

            this.associatedToHuntingDay = reportingType == HUNTING_DAY;
        }

        @Override
        public int getGameSpeciesCode() {
            return gameSpeciesCode;
        }

        @Override
        public RequiredHarvestSpecimenField getAge() {
            if (isMooseOrDeerRequiringPermitForHunting()) {
                return associatedToHuntingDay ? RequiredHarvestSpecimenField.YES : RequiredHarvestSpecimenField.VOLUNTARY;
            }

            return getRequirement(PERMIT_MANDATORY_AGE, gameSpeciesCode);
        }

        @Override
        public RequiredHarvestSpecimenField getGender() {
            if (isMooseOrDeerRequiringPermitForHunting()) {
                return associatedToHuntingDay ? RequiredHarvestSpecimenField.YES : RequiredHarvestSpecimenField.VOLUNTARY;
            }

            return getRequirement(PERMIT_MANDATORY_GENDER, gameSpeciesCode);
        }

        @Override
        public RequiredHarvestSpecimenField getWeight() {
            if (isMooseOrDeerRequiringPermitForHunting()) {
                return voluntaryIf(huntingYear < 2016);

            } else if (isGreySeal() && huntingMethod == HuntingMethod.SHOT_BUT_LOST) {
                return voluntaryIf(huntingYear < 2015);

            } else if (isRoeDeer() || isWildBoar()) {
                return voluntaryIf(!fields2020Enabled && reportingType == SEASON);
            }

            return getRequirement(PERMIT_MANDATORY_WEIGHT, gameSpeciesCode);
        }

        private RequiredHarvestSpecimenField getRequirement(final ImmutableSet<Integer> permitMandatorySpecies,
                                                            final int gameSpeciesCode) {

            return reportingType == PERMIT && permitMandatorySpecies.contains(gameSpeciesCode) ||
                    reportingType == SEASON && SEASON_COMMON_MANDATORY.contains(gameSpeciesCode) ||
                    reportingType == HUNTING_DAY
                    ? RequiredHarvestSpecimenField.YES : RequiredHarvestSpecimenField.VOLUNTARY;
        }

        @Override
        public RequiredHarvestSpecimenField getWeightEstimated() {
            if (!fields2020Enabled) {
                if (isMooseOrDeerRequiringPermitForHunting()) {
                    return RequiredHarvestSpecimenField.VOLUNTARY;
                }

                // TODO Can be removed when deer pilot 2020 is over.
                if (huntingYear >= 2020 && (isRoeDeer() || isWildBoar())) {
                    return RequiredHarvestSpecimenField.ALLOWED_BUT_HIDDEN;
                }

                return RequiredHarvestSpecimenField.NO;
            }

            return voluntaryIf(isMooselike() || isWildBoar());
        }

        @Override
        public RequiredHarvestSpecimenField getWeightMeasured() {
            return voluntaryIf(
                    !fields2020Enabled && isMooseOrDeerRequiringPermitForHunting()
                            || fields2020Enabled && (isMooselike() || isWildBoar()));
        }

        @Override
        public RequiredHarvestSpecimenField getAdditionalInfo() {
            return voluntaryIf(isMooseOrDeerRequiringPermitForHunting());
        }

        @Override
        public RequiredHarvestSpecimenField getNotEdible() {
            if (isMooseOrDeerRequiringPermitForHunting()) {
                return huntingYear >= 2016 && isMoose() && associatedToHuntingDay
                        ? RequiredHarvestSpecimenField.YES
                        : RequiredHarvestSpecimenField.VOLUNTARY;
            }

            return RequiredHarvestSpecimenField.NO;
        }

        @Override
        public RequiredHarvestSpecimenField getFitnessClass() {
            if (isMoose()) {
                return huntingYear >= 2016 && associatedToHuntingDay
                        ? RequiredHarvestSpecimenField.YES
                        : RequiredHarvestSpecimenField.VOLUNTARY;
            }

            return RequiredHarvestSpecimenField.NO;
        }

        @Override
        public RequiredHarvestSpecimenField getAntlersLost() {
            if (!fields2020Enabled || !isMooselike()) {
                return RequiredHarvestSpecimenField.NO;
            }

            return associatedToHuntingDay
                    ? RequiredHarvestSpecimenField.YES_IF_ADULT_MALE
                    : RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE;
        }

        @Override
        public RequiredHarvestSpecimenField getAntlersType() {
            if (!isMoose()) {
                return RequiredHarvestSpecimenField.NO;
            }

            if (!fields2020Enabled) {
                return huntingYear >= 2016 && associatedToHuntingDay
                        ? RequiredHarvestSpecimenField.YES_IF_ADULT_MALE
                        : RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE;
            }

            return associatedToHuntingDay
                    ? RequiredHarvestSpecimenField.YES_IF_ANTLERS_PRESENT
                    : RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT;
        }

        @Override
        public RequiredHarvestSpecimenField getAntlersWidth() {
            if (!isMooseOrDeerRequiringPermitForHunting()) {
                return RequiredHarvestSpecimenField.NO;
            }

            if (!fields2020Enabled) {
                return huntingYear >= 2016 && isMoose() && associatedToHuntingDay
                        ? RequiredHarvestSpecimenField.YES_IF_ADULT_MALE
                        : RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE;
            }

            if (isWhiteTailedDeer()) {
                return RequiredHarvestSpecimenField.DEPRECATED_ANTLER_DETAIL;
            }

            return isMoose() && associatedToHuntingDay
                    ? RequiredHarvestSpecimenField.YES_IF_ANTLERS_PRESENT
                    : RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT;
        }

        @Override
        public RequiredHarvestSpecimenField getAntlerPoints() {
            if (!isMooselike()) {
                return RequiredHarvestSpecimenField.NO;
            }

            if (!fields2020Enabled) {
                if (isRoeDeer()) {
                    return RequiredHarvestSpecimenField.NO;
                }

                return huntingYear >= 2016 && isMoose() && associatedToHuntingDay
                        ? RequiredHarvestSpecimenField.YES_IF_ADULT_MALE
                        : RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE;
            }

            return isMoose() && associatedToHuntingDay
                    ? RequiredHarvestSpecimenField.YES_IF_ANTLERS_PRESENT
                    : RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT;
        }

        @Override
        public RequiredHarvestSpecimenField getAntlersGirth() {
            if (!fields2020Enabled || !(isMoose() || isWhiteTailedDeer())) {
                return RequiredHarvestSpecimenField.NO;
            }

            return isMoose() && associatedToHuntingDay
                    ? RequiredHarvestSpecimenField.YES_IF_ANTLERS_PRESENT
                    : RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT;
        }

        @Override
        public RequiredHarvestSpecimenField getAntlersLength() {
            return fields2020Enabled && (isRoeDeer() || isWhiteTailedDeer())
                    ? RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT
                    : RequiredHarvestSpecimenField.NO;
        }

        @Override
        public RequiredHarvestSpecimenField getAntlersInnerWidth() {
            return fields2020Enabled && isWhiteTailedDeer()
                    ? RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT
                    : RequiredHarvestSpecimenField.NO;
        }

        @Override
        public RequiredHarvestSpecimenField getAntlerShaftWidth() {
            return fields2020Enabled && isRoeDeer()
                    ? RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT
                    : RequiredHarvestSpecimenField.NO;
        }

        @Override
        public RequiredHarvestSpecimenField getAlone() {
            if (isMoose()) {
                return associatedToHuntingDay
                        ? RequiredHarvestSpecimenField.YES_IF_YOUNG
                        : RequiredHarvestSpecimenField.VOLUNTARY_IF_YOUNG;
            }

            return RequiredHarvestSpecimenField.NO;
        }

        private static RequiredHarvestSpecimenField voluntaryIf(final boolean condition) {
            return condition ? RequiredHarvestSpecimenField.VOLUNTARY : RequiredHarvestSpecimenField.NO;
        }
    }
}

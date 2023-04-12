package fi.riista.feature.gamediary.harvest.fields;

import com.google.common.collect.ImmutableSet;
import fi.riista.feature.gamediary.HasGameSpeciesCode;
import fi.riista.feature.gamediary.harvest.HarvestReportingType;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.HuntingMethod;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_AMERICAN_MINK;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BADGER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BLUE_FOX;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BROWN_HARE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_CANADIAN_BEAVER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_DOMESTICATED_CAT;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_ERMINE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_EUROPEAN_BEAVER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_EUROPEAN_POLECAT;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_FALLOW_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_GREY_SEAL;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_HARBOUR_SEAL;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_LYNX;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOUNTAIN_HARE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MUFFLON;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MUSKRAT;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_NUTRIA;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_OTTER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_PINE_MARTEN;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RABBIT;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RACCOON;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RACCOON_DOG;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RED_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RED_FOX;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RED_SQUIRREL;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RINGED_SEAL;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_ROE_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_SIKA_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_BOAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_FOREST_REINDEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLF;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLVERINE;
import static fi.riista.feature.gamediary.harvest.HarvestReportingType.BASIC;
import static fi.riista.feature.gamediary.harvest.HarvestReportingType.HUNTING_DAY;
import static fi.riista.feature.gamediary.harvest.HarvestReportingType.PERMIT;
import static fi.riista.feature.gamediary.harvest.HarvestReportingType.SEASON;

public class RequiredHarvestFieldsImpl {

    public static ReportImpl getFormFields(final int huntingYear,
                                           final int gameSpeciesCode,
                                           final HarvestReportingType reportingType) {

        return new ReportImpl(huntingYear, gameSpeciesCode, reportingType);
    }

    public static SpecimenImpl getSpecimenFields(final int huntingYear,
                                                 final int gameSpeciesCode,
                                                 final HuntingMethod huntingMethod,
                                                 final HarvestReportingType reportingType,
                                                 final HarvestSpecVersion specVersion,
                                                 final boolean withPermit) {

        return new SpecimenImpl(huntingYear, gameSpeciesCode, huntingMethod, reportingType, specVersion, withPermit);
    }

    public static class ReportImpl implements RequiredHarvestFields.Report, HasGameSpeciesCode {

        private final int huntingYear;
        private final int gameSpeciesCode;
        private final HarvestReportingType reportingType;

        protected ReportImpl(final int huntingYear,
                             final int gameSpeciesCode,
                             final HarvestReportingType reportingType) {

            this.huntingYear = huntingYear;
            this.gameSpeciesCode = gameSpeciesCode;
            this.reportingType = reportingType;
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
            return huntingYear >= 2020 && isWhiteTailedDeer() && reportingType != PERMIT
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
                .of(
                        OFFICIAL_CODE_MUFFLON,
                        OFFICIAL_CODE_RED_DEER,
                        OFFICIAL_CODE_SIKA_DEER,
                        OFFICIAL_CODE_GREY_SEAL,
                        OFFICIAL_CODE_WOLF,
                        OFFICIAL_CODE_WOLVERINE,
                        OFFICIAL_CODE_BEAR,
                        OFFICIAL_CODE_MOOSE,
                        OFFICIAL_CODE_FALLOW_DEER,
                        OFFICIAL_CODE_WHITE_TAILED_DEER,
                        OFFICIAL_CODE_WILD_FOREST_REINDEER,
                        OFFICIAL_CODE_WILD_BOAR,
                        OFFICIAL_CODE_OTTER,
                        OFFICIAL_CODE_LYNX);


        // {villisika,saukko,ilves,piisami,rämemajava,"tarhattu naali",pesukarhu,hilleri,kirjohylje,mufloni,
        // saksanhirvi,japaninpeura,halli,susi,"villiintynyt kissa",metsäjänis,rusakko,orava,kanadanmajava,kettu,
        // kärppä,näätä,minkki,villikani,supikoira,mäyrä,itämerennorppa,euroopanmajava,ahma,karhu,metsäkauris,hirvi,
        // kuusipeura,valkohäntäpeura,metsäpeura}
        static final ImmutableSet<Integer> PERMIT_MANDATORY_GENDER = ImmutableSet
                .of(
                        OFFICIAL_CODE_WILD_BOAR, OFFICIAL_CODE_OTTER, OFFICIAL_CODE_LYNX,
                        OFFICIAL_CODE_MUSKRAT, OFFICIAL_CODE_NUTRIA, OFFICIAL_CODE_BLUE_FOX,
                        OFFICIAL_CODE_RACCOON, OFFICIAL_CODE_EUROPEAN_POLECAT, OFFICIAL_CODE_HARBOUR_SEAL,
                        OFFICIAL_CODE_MUFFLON, OFFICIAL_CODE_RED_DEER, OFFICIAL_CODE_SIKA_DEER,
                        OFFICIAL_CODE_GREY_SEAL, OFFICIAL_CODE_WOLF, OFFICIAL_CODE_DOMESTICATED_CAT,
                        OFFICIAL_CODE_MOUNTAIN_HARE, OFFICIAL_CODE_BROWN_HARE, OFFICIAL_CODE_RED_SQUIRREL,
                        OFFICIAL_CODE_CANADIAN_BEAVER, OFFICIAL_CODE_RED_FOX, OFFICIAL_CODE_ERMINE,
                        OFFICIAL_CODE_PINE_MARTEN, OFFICIAL_CODE_AMERICAN_MINK, OFFICIAL_CODE_RABBIT,
                        OFFICIAL_CODE_RACCOON_DOG, OFFICIAL_CODE_BADGER, OFFICIAL_CODE_RINGED_SEAL,
                        OFFICIAL_CODE_EUROPEAN_BEAVER, OFFICIAL_CODE_WOLVERINE, OFFICIAL_CODE_BEAR,
                        OFFICIAL_CODE_ROE_DEER, OFFICIAL_CODE_MOOSE, OFFICIAL_CODE_FALLOW_DEER,
                        OFFICIAL_CODE_WHITE_TAILED_DEER, OFFICIAL_CODE_WILD_FOREST_REINDEER);

        // {halli,susi,saukko,ilves,ahma,karhu}
        static final ImmutableSet<Integer> PERMIT_MANDATORY_WEIGHT = ImmutableSet
                .of(OFFICIAL_CODE_GREY_SEAL, OFFICIAL_CODE_WOLF, OFFICIAL_CODE_OTTER, OFFICIAL_CODE_LYNX, OFFICIAL_CODE_WOLVERINE, OFFICIAL_CODE_BEAR);

        // {karhu,metsäkauris,halli,villisika, norppa}
        private static final ImmutableSet<Integer> SEASON_COMMON_MANDATORY = ImmutableSet
                .of(OFFICIAL_CODE_BEAR, OFFICIAL_CODE_ROE_DEER, OFFICIAL_CODE_GREY_SEAL, OFFICIAL_CODE_WILD_BOAR, OFFICIAL_CODE_RINGED_SEAL);


        private final int huntingYear;
        private final int gameSpeciesCode;
        private final HarvestReportingType reportingType;
        private final HuntingMethod huntingMethod;
        private final boolean fields2020Enabled;
        private final HarvestSpecVersion specVersion;
        private final boolean withPermit;

        private final boolean associatedToHuntingDay;

        /*package*/ SpecimenImpl(final int huntingYear,
                                 final int gameSpeciesCode,
                                 final HuntingMethod huntingMethod,
                                 final HarvestReportingType reportingType,
                                 final HarvestSpecVersion specVersion,
                                 final boolean withPermit) {

            final boolean isClientSupportFor2020Fields = specVersion.supportsAntlerFields2020();

            this.huntingYear = huntingYear;
            this.gameSpeciesCode = gameSpeciesCode;
            this.reportingType = reportingType;
            this.huntingMethod = huntingMethod;
            this.fields2020Enabled = isClientSupportFor2020Fields && huntingYear >= 2020;
            this.specVersion = specVersion;
            this.withPermit = withPermit;

            this.associatedToHuntingDay = reportingType == HUNTING_DAY;
        }

        @Override
        public int getGameSpeciesCode() {
            return gameSpeciesCode;
        }

        @Override
        public RequiredHarvestSpecimenField getAge() {
            if (isMooseOrDeerRequiringPermitForHunting()) {
                if (this.specVersion.supportsMandatoryAgeAndGenderFieldsForMooselikeHarvest()) {
                    // hunter must give gender and age for mooselike harvest
                    return withPermit ? RequiredHarvestSpecimenField.VOLUNTARY : RequiredHarvestSpecimenField.YES;

                } else {
                    // old way
                    return associatedToHuntingDay ? RequiredHarvestSpecimenField.YES : RequiredHarvestSpecimenField.VOLUNTARY;

                }
            }

            return getRequirement(PERMIT_MANDATORY_AGE, gameSpeciesCode);
        }

        @Override
        public RequiredHarvestSpecimenField getGender() {
            if (isMooseOrDeerRequiringPermitForHunting()) {
                if (this.specVersion.supportsMandatoryAgeAndGenderFieldsForMooselikeHarvest()) {
                    // hunter must give gender and age for mooselike harvest

                    return withPermit ? RequiredHarvestSpecimenField.VOLUNTARY : RequiredHarvestSpecimenField.YES;

                } else {
                    // old way
                    return associatedToHuntingDay ? RequiredHarvestSpecimenField.YES : RequiredHarvestSpecimenField.VOLUNTARY;
                }
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
                return voluntaryIf(!fields2020Enabled);
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

                // TODO Can be removed after mobile clients with spec version less than _8 are no longer supported
                // Until then, mobile clients may send harvests with weight field.
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
            return isMoose()
                    ? RequiredHarvestSpecimenField.VOLUNTARY
                    : RequiredHarvestSpecimenField.NO;
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

            return fields2020Enabled
                    ? RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT
                    : RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE;
        }

        @Override
        public RequiredHarvestSpecimenField getAntlersWidth() {
            if (!isMooseOrDeerRequiringPermitForHunting()) {
                return RequiredHarvestSpecimenField.NO;
            }

            if (!fields2020Enabled) {
                return RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE;
            }

            if (isWhiteTailedDeer()) {
                return RequiredHarvestSpecimenField.DEPRECATED_ANTLER_DETAIL;
            }

            return RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT;
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

                return RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE;
            }

            return RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT;
        }

        @Override
        public RequiredHarvestSpecimenField getAntlersGirth() {
            if (!fields2020Enabled || !(isMoose() || isWhiteTailedDeer())) {
                return RequiredHarvestSpecimenField.NO;
            }

            return RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT;
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
                return RequiredHarvestSpecimenField.VOLUNTARY_IF_YOUNG;
            }

            return RequiredHarvestSpecimenField.NO;
        }

        private static RequiredHarvestSpecimenField voluntaryIf(final boolean condition) {
            return condition ? RequiredHarvestSpecimenField.VOLUNTARY : RequiredHarvestSpecimenField.NO;
        }
    }
}

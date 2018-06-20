package fi.riista.feature.gamediary.harvest.fields;

import com.google.common.collect.ImmutableSet;
import fi.riista.feature.common.entity.Required;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.HarvestReportingType;
import fi.riista.feature.gamediary.harvest.HuntingMethod;

public class RequiredHarvestFields {

    private static final int BEAR = GameSpecies.OFFICIAL_CODE_BEAR;
    private static final int WILD_BOAR = GameSpecies.OFFICIAL_CODE_WILD_BOAR;
    private static final int MOOSE = GameSpecies.OFFICIAL_CODE_MOOSE;
    private static final int ROE_DEER = GameSpecies.OFFICIAL_CODE_ROE_DEER;
    private static final int GREY_SEAL = GameSpecies.OFFICIAL_CODE_GREY_SEAL;
    private static final int WOLF = GameSpecies.OFFICIAL_CODE_WOLF;

    public static Report getFormFields(final int huntingYear,
                                       final int gameSpeciesCode,
                                       final HarvestReportingType reportingType) {
        return new Report(huntingYear, gameSpeciesCode, reportingType);
    }

    public static Specimen getSpecimenFields(final int huntingYear,
                                             final int gameSpeciesCode,
                                             final HuntingMethod huntingMethod,
                                             final HarvestReportingType reportingType) {
        return new Specimen(huntingYear, gameSpeciesCode, huntingMethod, reportingType);
    }

    public static class Report {
        final int gameSpeciesCode;
        final int huntingYear;
        final HarvestReportingType reportingType;

        protected Report(final int huntingYear,
                         final int gameSpeciesCode,
                         final HarvestReportingType reportingType) {
            this.huntingYear = huntingYear;
            this.gameSpeciesCode = gameSpeciesCode;
            this.reportingType = reportingType;
        }

        public Required getPermitNumber() {
            return reportingType == HarvestReportingType.PERMIT ? Required.YES : Required.NO;
        }

        public Required getHarvestArea() {
            if (gameSpeciesCode == BEAR || gameSpeciesCode == GREY_SEAL) {
                return reportingType == HarvestReportingType.SEASON ? Required.YES : Required.NO;
            }
            return Required.NO;
        }

        public Required getHuntingMethod() {
            if (gameSpeciesCode == GREY_SEAL) {
                return reportingType != HarvestReportingType.BASIC ? Required.YES : Required.NO;
            }
            return Required.NO;
        }

        public Required getFeedingPlace() {
            if (gameSpeciesCode == WILD_BOAR) {
                return reportingType != HarvestReportingType.BASIC ? Required.VOLUNTARY : Required.NO;
            }
            return Required.NO;
        }

        public Required getTaigaBeanGoose() {
            if (gameSpeciesCode == GameSpecies.OFFICIAL_CODE_BEAN_GOOSE) {
                return reportingType != HarvestReportingType.BASIC ? Required.VOLUNTARY : Required.NO;
            }
            return Required.NO;
        }

        public Required getLukeStatus() {
            if (gameSpeciesCode == WOLF) {
                return reportingType == HarvestReportingType.SEASON ? Required.VOLUNTARY : Required.NO;
            }
            return Required.NO;
        }

        // legacy for metsäkauris, was mandatory for season before 2017
        @Deprecated
        public Required getHuntingAreaType() {
            if (gameSpeciesCode == ROE_DEER && reportingType == HarvestReportingType.SEASON) {
                return huntingYear < 2017 ? Required.YES : Required.VOLUNTARY;
            }
            return Required.NO;
        }

        // legacy for metsäkauris, was voluntary for season before 2017
        @Deprecated
        public Required getHuntingParty() {
            // reportingType=SEASON speciesCode=47507 huntingYear=2017 huntingDay=false has missing fields [HUNTING_PARTY] and invalid fields []
            if (gameSpeciesCode == ROE_DEER && reportingType == HarvestReportingType.SEASON) {
                return huntingYear < 2017 ? Required.VOLUNTARY : Required.NO;
            }
            return Required.NO;
        }

        // legacy for metsäkauris, was mandatory for season before 2017
        @Deprecated
        public Required getHuntingAreaSize() {
            if (gameSpeciesCode == ROE_DEER) {
                return reportingType == HarvestReportingType.SEASON && huntingYear < 2017 ? Required.YES : Required.NO;
            }
            return Required.NO;
        }

        // legacy for bear before 2015
        @Deprecated
        public Required getReportedWithPhoneCall() {
            return gameSpeciesCode == BEAR && huntingYear == 2014 && reportingType == HarvestReportingType.SEASON ? Required.YES : Required.NO;
        }
    }

    public static class Specimen {
        // {mufloni,saksanhirvi,japaninhirvi ,halli,susi,ahma,karhu,hirvi,kuusipeura,valkohäntäpeura,metsäpeura,villisika,saukko,ilves}
        static final ImmutableSet<Integer> PERMIT_MANDATORY_AGE = ImmutableSet.of(47774, 47476, 47479, 47282, 46549, 47212, 47348, 47503, 47484, 47629, 200556, 47926, 47169, 46615);

        // {villisika,saukko,ilves,piisami,rämemajava,"tarhattu naali",pesukarhu,hilleri,kirjohylje,mufloni,saksanhirvi,japaninhirvi ,halli,susi,"villiintynyt kissa",metsäjänis,rusakko,orava,kanadanmajava,kettu,kärppä,näätä,minkki,villikani,supikoira,mäyrä,itämerennorppa,euroopanmajava,ahma,karhu,metsäkauris,hirvi,kuusipeura,valkohäntäpeura,metsäpeura}
        static final ImmutableSet<Integer> PERMIT_MANDATORY_GENDER = ImmutableSet.of(47926, 47169, 46615, 48537, 50336, 46542, 47329, 47240, 47305, 47774, 47476, 47479, 47282, 46549, 53004, 50106, 50386, 48089, 48250, 46587, 47230, 47223, 47243, 50114, 46564, 47180, 200555, 48251, 47212, 47348, 47507, 47503, 47484, 47629, 200556);

        // {halli,susi,saukko,ilves,ahma,karhu}
        static final ImmutableSet<Integer> PERMIT_MANDATORY_WEIGHT = ImmutableSet.of(47282, 46549, 47169, 46615, 47212, 47348);

        // {karhu,metsäkauris,halli,villisika}
        private static final ImmutableSet<Integer> SEASON_COMMON_MANDATORY = ImmutableSet.of(47348, 47507, 47282, 47926);

        private final int huntingYear;
        private final int gameSpeciesCode;
        private final HarvestReportingType reportingType;
        private final HuntingMethod huntingMethod;
        private final boolean isMoose;
        private final boolean isMooseOrDeerRequiringPermitForHunting;
        private final boolean associatedToHuntingDay;

        private Specimen(final int huntingYear,
                         final int gameSpeciesCode,
                         final HuntingMethod huntingMethod,
                         final HarvestReportingType reportingType) {
            this.huntingYear = huntingYear;
            this.gameSpeciesCode = gameSpeciesCode;
            this.reportingType = reportingType;
            this.huntingMethod = huntingMethod;
            this.isMoose = gameSpeciesCode == MOOSE;
            this.isMooseOrDeerRequiringPermitForHunting = GameSpecies.isMooseOrDeerRequiringPermitForHunting(gameSpeciesCode);
            this.associatedToHuntingDay = reportingType == HarvestReportingType.HUNTING_DAY;
        }

        public Required getAge() {
            if (isMooseOrDeerRequiringPermitForHunting) {
                return associatedToHuntingDay ? Required.YES : Required.VOLUNTARY;
            }
            return getRequirement(PERMIT_MANDATORY_AGE, gameSpeciesCode);
        }

        public Required getGender() {
            if (isMooseOrDeerRequiringPermitForHunting) {
                return associatedToHuntingDay ? Required.YES : Required.VOLUNTARY;
            }
            return getRequirement(PERMIT_MANDATORY_GENDER, gameSpeciesCode);
        }

        public Required getWeight() {
            if (gameSpeciesCode == ROE_DEER && reportingType == HarvestReportingType.SEASON) {
                return Required.VOLUNTARY;
            }

            if (gameSpeciesCode == WILD_BOAR && reportingType == HarvestReportingType.SEASON) {
                return Required.VOLUNTARY;
            }

            if (isMooseOrDeerRequiringPermitForHunting) {
                return huntingYear < 2016 ? Required.VOLUNTARY : Required.NO;
            }

            if (gameSpeciesCode == GameSpecies.OFFICIAL_CODE_GREY_SEAL &&
                    huntingMethod == HuntingMethod.SHOT_BUT_LOST) {
                return huntingYear < 2015 ? Required.VOLUNTARY : Required.NO;
            }

            return getRequirement(PERMIT_MANDATORY_WEIGHT, gameSpeciesCode);
        }

        private Required getRequirement(final ImmutableSet<Integer> permitMandatorySpecies, final int gameSpeciesCode) {
            return reportingType == HarvestReportingType.PERMIT && permitMandatorySpecies.contains(gameSpeciesCode) ||
                    reportingType == HarvestReportingType.SEASON && SEASON_COMMON_MANDATORY.contains(gameSpeciesCode) ||
                    reportingType == HarvestReportingType.HUNTING_DAY
                    ? Required.YES : Required.VOLUNTARY;
        }

        public Required getWeightEstimated() {
            return isMooseOrDeerRequiringPermitForHunting ? Required.VOLUNTARY : Required.NO;
        }

        public Required getWeightMeasured() {
            return isMooseOrDeerRequiringPermitForHunting ? Required.VOLUNTARY : Required.NO;
        }

        public Required getAdditionalInfo() {
            return isMooseOrDeerRequiringPermitForHunting ? Required.VOLUNTARY : Required.NO;
        }

        public Required getNotEdible() {
            if (isMooseOrDeerRequiringPermitForHunting) {
                return isMoose && associatedToHuntingDay && huntingYear >= 2016 ? Required.YES : Required.VOLUNTARY;
            }

            return Required.NO;
        }

        public Required getFitnessClass() {
            if (isMoose) {
                return associatedToHuntingDay && huntingYear >= 2016 ? Required.YES : Required.VOLUNTARY;
            }

            return Required.NO;
        }

        public Required getAntlersWidth(final GameAge age, final GameGender gender) {
            return commonMooselikeAdultMale(age, gender);
        }

        // For UI only
        public Required getAntlersWidth() {
            return isMooseOrDeerRequiringPermitForHunting ? Required.VOLUNTARY : Required.NO;
        }

        // For UI only
        public Required getAntlerPoints() {
            return isMooseOrDeerRequiringPermitForHunting ? Required.VOLUNTARY : Required.NO;
        }

        public Required getAntlerPoints(final GameAge age, final GameGender gender) {
            return commonMooselikeAdultMale(age, gender);
        }

        private Required commonMooselikeAdultMale(final GameAge age, final GameGender gender) {
            if (isMooseOrDeerRequiringPermitForHunting && age == GameAge.ADULT && gender == GameGender.MALE) {
                return associatedToHuntingDay && isMoose && huntingYear >= 2016
                        ? Required.YES
                        : Required.VOLUNTARY;
            }
            return Required.NO;
        }

        // For UI only
        public Required getAntlersType() {
            return isMoose ? Required.VOLUNTARY : Required.NO;
        }

        public Required getAntlersType(final GameAge age, final GameGender gender) {
            if (isMoose && age == GameAge.ADULT && gender == GameGender.MALE) {
                return associatedToHuntingDay ? Required.YES : Required.VOLUNTARY;
            }
            return Required.NO;
        }

        // For UI only
        public Required getAlone() {
            return isMoose ? Required.VOLUNTARY : Required.NO;
        }

        public Required getAlone(final GameAge age) {
            if (isMoose && age == GameAge.YOUNG) {
                return associatedToHuntingDay ? Required.YES : Required.VOLUNTARY;
            }
            return Required.NO;
        }
    }
}

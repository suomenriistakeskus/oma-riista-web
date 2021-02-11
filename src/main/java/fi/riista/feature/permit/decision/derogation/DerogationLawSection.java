package fi.riista.feature.permit.decision.derogation;

import fi.riista.feature.gamediary.GameSpecies;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_AMERICAN_MINK;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BADGER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BLUE_FOX;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BROWN_HARE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_CANADIAN_BEAVER;
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

public enum DerogationLawSection {
    SECTION_41A,
    SECTION_41B,
    SECTION_41C;

    public static DerogationLawSection getSpeciesLawSection(final int speciesCode) {
        switch (speciesCode) {

            //  metsästyslain 41 a §:n 1 momentin mukaiset ns. vahinkoperusteiset poikkeusluvat ahma, susi, karhu,
            // saukko, ilves, euroopanmajava, halli, kirjohylje, itämeren norppa, hilleri, näätä tai metsäjänis.
            case OFFICIAL_CODE_WOLVERINE:
            case OFFICIAL_CODE_WOLF:
            case OFFICIAL_CODE_BEAR:
            case OFFICIAL_CODE_OTTER:
            case OFFICIAL_CODE_LYNX:
            case OFFICIAL_CODE_EUROPEAN_BEAVER:
            case OFFICIAL_CODE_RINGED_SEAL:
            case OFFICIAL_CODE_HARBOUR_SEAL:
            case OFFICIAL_CODE_GREY_SEAL:
            case OFFICIAL_CODE_EUROPEAN_POLECAT:
            case OFFICIAL_CODE_PINE_MARTEN:
            case OFFICIAL_CODE_MOUNTAIN_HARE:
                return SECTION_41A;

            // metsästyslain 41 c §:n mukaiset poikkeusluvat villikani, rusakko, orava, kanadanmajava, tarhattu naali,
            // kettu, mäyrä, kärppä, villisika, kuusipeura, saksanhirvi, japaninpeura, metsäkauris, hirvi,
            // valkohäntäpeura, metsäpeura ja mufloni.

            // myös vieraslajit käsitellään 41c:n mukaisesti: supikoira, minkki, pesukarhu, piisami, rämemajava
            case OFFICIAL_CODE_RABBIT:
            case OFFICIAL_CODE_BROWN_HARE:
            case OFFICIAL_CODE_RED_SQUIRREL:
            case OFFICIAL_CODE_CANADIAN_BEAVER:
            case OFFICIAL_CODE_BLUE_FOX:
            case OFFICIAL_CODE_RED_FOX:
            case OFFICIAL_CODE_BADGER:
            case OFFICIAL_CODE_ERMINE:
            case OFFICIAL_CODE_WILD_BOAR:
            case OFFICIAL_CODE_FALLOW_DEER:
            case OFFICIAL_CODE_RED_DEER:
            case OFFICIAL_CODE_SIKA_DEER:
            case OFFICIAL_CODE_ROE_DEER:
            case OFFICIAL_CODE_MOOSE:
            case OFFICIAL_CODE_WHITE_TAILED_DEER:
            case OFFICIAL_CODE_WILD_FOREST_REINDEER:
            case OFFICIAL_CODE_MUFFLON:
            case OFFICIAL_CODE_RACCOON_DOG:
            case OFFICIAL_CODE_AMERICAN_MINK:
            case OFFICIAL_CODE_RACCOON:
            case OFFICIAL_CODE_MUSKRAT:
            case OFFICIAL_CODE_NUTRIA:
                return SECTION_41C;
            default:
                if (GameSpecies.isBirdPermitSpecies(speciesCode)) {
                    return SECTION_41B;
                } else {
                    throw new IllegalArgumentException("No derogation law section spesified for " + speciesCode);
                }

        }
    }
}

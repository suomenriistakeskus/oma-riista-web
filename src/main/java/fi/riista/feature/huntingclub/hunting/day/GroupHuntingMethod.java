package fi.riista.feature.huntingclub.hunting.day;

import fi.riista.util.LocalisedEnum;

public enum GroupHuntingMethod implements LocalisedEnum {
    // KOIRAN KANSSA:
    // 1 = passilinja, koira ohjaajineen metsässä
    OPTION_1(1, true, "PASSILINJA_KOIRA_OHJAAJINEEN_METSASSA"),

    // 2 = hiipiminen pysäyttävälle koiralle
    OPTION_2(2, true, "HIIPIMINEN_PYSAYTTAVALLE_KOIRALLE"),

    // ILMAN KOIRAA:
    // 3 = passilinja ja tiivis ajoketju
    OPTION_3(3, false, "PASSILINJA_JA_TIIVIS_AJOKETJU"),

    // 4 = passilinja ja miesajo jäljityksenä
    OPTION_4(4, false, "PASSILINJA_JA_MIESAJO_JALJITYKSENA"),

    // 5 = jäljitys eli naakiminen ilman passeja
    OPTION_5(5, false, "JALJITYS_ELI_NAAKIMINEN_ILMAN_PASSEJA"),

    // 6 = väijyntä kulkupaikoilta
    OPTION_6(6, false, "VAIJYNTA_KULKUPAIKOILLA"),

    // 7 = väijyntä ravintokohteilta
    OPTION_7(7, false, "VAIJYNTA_RAVINTOKOHTEILLA"),

    // 8 = houkuttelu
    OPTION_8(8, false, "HOUKUTTELU"),

    // 9 = muu, mikä
    OPTION_9(9, false, "MUU");

    private final int typeCode;
    private final boolean withHound;
    private final String explicitName;

    public static GroupHuntingMethod valueOf(final Integer typeCode) {
        if (typeCode == null) {
            return null;
        }

        for (GroupHuntingMethod method : GroupHuntingMethod.values()) {
            if (method.getTypeCode() == typeCode) {
                return method;
            }
        }

        throw new IllegalArgumentException("Invalid HuntingMethod code: " + typeCode);
    }

    GroupHuntingMethod(final int typeCode, final boolean withHound, final String explicitName) {
        this.typeCode = typeCode;
        this.withHound = withHound;
        this.explicitName = explicitName;
    }

    public int getTypeCode() {
        return typeCode;
    }

    public boolean isWithHound() {
        return withHound;
    }

    public String getExplicitName() {
        return explicitName;
    }
}

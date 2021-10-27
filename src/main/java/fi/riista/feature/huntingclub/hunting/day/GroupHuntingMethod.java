package fi.riista.feature.huntingclub.hunting.day;

import fi.riista.util.LocalisedEnum;

public enum GroupHuntingMethod implements LocalisedEnum {
    // KOIRAN KANSSA:
    // 1 = passilinja, koira ohjaajineen metsässä
    PASSILINJA_KOIRA_OHJAAJINEEN_METSASSA(1, true),

    // 2 = hiipiminen pysäyttävälle koiralle
    HIIPIMINEN_PYSAYTTAVALLE_KOIRALLE(2, true),

    // ILMAN KOIRAA:
    // 3 = passilinja ja tiivis ajoketju
    PASSILINJA_JA_TIIVIS_AJOKETJU(3, false),

    // 4 = passilinja ja miesajo jäljityksenä
    PASSILINJA_JA_MIESAJO_JALJITYKSENA(4, false),

    // 5 = jäljitys eli naakiminen ilman passeja
    JALJITYS_ELI_NAAKIMINEN_ILMAN_PASSEJA(5, false),

    // 6 = väijyntä kulkupaikoilta
    VAIJYNTA_KULKUPAIKOILLA(6, false),

    // 7 = väijyntä ravintokohteilta
    VAIJYNTA_RAVINTOKOHTEILLA(7, false),

    // 8 = houkuttelu
    HOUKUTTELU(8, false),

    // 9 = muu, mikä
    MUU(9, false);

    private final int typeCode;
    private final boolean withHound;

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

    GroupHuntingMethod(final int typeCode, final boolean withHound) {
        this.typeCode = typeCode;
        this.withHound = withHound;
    }

    public int getTypeCode() {
        return typeCode;
    }

    public boolean isWithHound() {
        return withHound;
    }
}

package fi.riista.feature.gamediary.observation;

import fi.riista.feature.common.entity.HasMooseDataCardEncoding;
import fi.riista.util.LocalisedEnum;

public enum ObservationType implements HasMooseDataCardEncoding<ObservationType>, LocalisedEnum {

    NAKO,
    JALKI,
    ULOSTE,
    AANI,
    RIISTAKAMERA,
    KOIRAN_RIISTATYO,
    MAASTOLASKENTA,
    KOLMIOLASKENTA,
    LENTOLASKENTA,
    HAASKA,
    SYONNOS,
    KELOMISPUU,
    KIIMAKUOPPA,
    MAKUUPAIKKA,
    PATO,
    PESA,
    PESA_KEKO,
    PESA_PENKKA,
    PESA_SEKA,
    SOIDIN,
    LUOLASTO,
    PESIMALUOTO,
    LEPAILYLUOTO,
    PESIMASUO,
    MUUTON_AIKAINEN_LEPAILYALUE,
    RIISTANKULKUPAIKKA,

    // Metsäkanalintujen (engl. grouse) elinympäristöä kuvaavat havaintotyypit
    POIKUEYMPARISTO,
    VAIHTELEVARAKENTEINEN_MUSTIKKAMETSA,
    KUUSISEKOTTEINEN_METSA,
    VAIHTELEVARAKENTEINEN_MANTYSEKOTTEINEN_METSA,
    VAIHTELEVARAKENTEINEN_LEHTIPUUSEKOTTEINEN_METSA,
    SUON_REUNAMETSA,
    HAKOMAMANTY,
    RUOKAILUKOIVIKKO,
    LEPPAKUUSIMETSA_TAI_KOIVUKUUSIMETSA,
    RUOKAILUPAJUKKO_TAI_KOIVIKKO,

    MUU;

    @Override
    public String getMooseDataCardEncoding() {
        switch (this) {
            case NAKO:
                return "N";
            case JALKI:
                return "J";
            default:
                return null;
        }
    }

}

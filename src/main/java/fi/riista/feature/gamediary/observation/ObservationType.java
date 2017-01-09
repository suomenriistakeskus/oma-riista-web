package fi.riista.feature.gamediary.observation;

import fi.riista.feature.common.entity.HasMooseDataCardEncoding;

public enum ObservationType implements HasMooseDataCardEncoding<ObservationType> {

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

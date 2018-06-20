package fi.riista.integration.lupahallinta;

import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.integration.lupahallinta.model.LH_PositionType;

import java.util.EnumSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LHPositionTypeMapping {
    public static EnumSet<OccupationType> getExportableValues() {
        return Stream.of(OccupationType.values())
                .filter(occupationType -> transform(occupationType) != null)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(OccupationType.class)));
    }

    public static LH_PositionType transform(final OccupationType occupationType) {
        switch (occupationType) {
            case TOIMINNANOHJAAJA:
                return LH_PositionType.TOIMINNANOHJAAJA;
            case SRVA_YHTEYSHENKILO:
                return LH_PositionType.SRVA___YHTEYSHENKILO;
            case PETOYHDYSHENKILO:
                return LH_PositionType.PETOYHDYSHENKILO;
            case METSASTYKSENVALVOJA:
                return LH_PositionType.METSASTYKSENVALVOJA;
            case METSASTAJATUTKINNON_VASTAANOTTAJA:
                return LH_PositionType.METSASTAJATUTKINNON___VASTAANOTTAJA;
            case AMPUMAKOKEEN_VASTAANOTTAJA:
                return LH_PositionType.AMPUMAKOKEEN___VASTAANOTTAJA;
            case RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA:
                return LH_PositionType.RHYN___EDUSTAJA___RIISTAVAHINKOJEN___MAASTOKATSELMUKSESSA;
            case METSASTAJATUTKINTOON_VALMENTAVAN_KOULUTUKSEN_KOULUTTAJA:
                return LH_PositionType.METSASTAJATUTKINTOON___VALMENTAVAN___KOULUTUKSEN___KOULUTTAJA;
            case PUHEENJOHTAJA:
                return LH_PositionType.PUHEENJOHTAJA;
            case VARAPUHEENJOHTAJA:
                return LH_PositionType.VARAPUHEENJOHTAJA;
            case HALLITUKSEN_JASEN:
                return LH_PositionType.HALLITUKSEN___JASEN;
            case HALLITUKSEN_VARAJASEN:
                return LH_PositionType.HALLITUKSEN___VARAJASEN;
            case JASEN:
                return LH_PositionType.HALLITUKSEN___JASEN;
            case VARAJASEN:
                return LH_PositionType.HALLITUKSEN___VARAJASEN;
            case JALJESTYSKOIRAN_OHJAAJA_HIRVI:
                return LH_PositionType.JALJESTYSKOIRAN___OHJAAJA___HIRVI;
            case JALJESTYSKOIRAN_OHJAAJA_PIENET_HIRVIELAIMET:
                return LH_PositionType.JALJESTYSKOIRAN___OHJAAJA___PIENET___HIRVIELAIMET;
            case JALJESTYSKOIRAN_OHJAAJA_SUURPEDOT:
                return LH_PositionType.JALJESTYSKOIRAN___OHJAAJA___SUURPEDOT;
            default:
                return null;
        }
    }

    private LHPositionTypeMapping() {
        throw new AssertionError();
    }
}

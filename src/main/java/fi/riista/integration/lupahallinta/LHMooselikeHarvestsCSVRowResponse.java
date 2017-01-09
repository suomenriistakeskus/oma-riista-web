package fi.riista.integration.lupahallinta;

import fi.riista.config.web.CSVHttpResponse;
import fi.riista.integration.lupahallinta.club.LHMooselikeHarvestsCSVRow;
import fi.riista.util.F;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LHMooselikeHarvestsCSVRowResponse extends CSVHttpResponse {

    private static final String[] HEADER = new String[]{
            "lupanumero",
            "asiakasnumero",
            "eläinlaji",
            "aikuiset urokset",
            "aikuiset naaraat",
            "urosvasat",
            "naarasvasat",
            "ihmisravinnoksi kelpaamattomat aikuiset",
            "ihmisravinnoksi kelpaamattomat vasat",
            "kokonaispinta-ala",
            "käytetty pinta-ala",
            "jäävä kanta koko alueella",
            "jäävä kanta metsästykseen käytetyllä alueella"
    };

    @Override
    public Charset getCharset() {
        return StandardCharsets.ISO_8859_1;
    }

    public LHMooselikeHarvestsCSVRowResponse(String filename, String[] headerRow, List<String[]> rows) {
        super(filename, headerRow, rows);
    }

    public static CSVHttpResponse create(List<LHMooselikeHarvestsCSVRow> rows) {
        return new LHMooselikeHarvestsCSVRowResponse(
                "mooselikeharvests.csv",
                HEADER,
                F.mapNonNullsToList(rows, LHMooselikeHarvestsCSVRowResponse::rowToCsv));

    }

    public static String[] rowToCsv(LHMooselikeHarvestsCSVRow row) {
        return new String[]{
                row.getPermitNumber(),
                row.getCustomerNumber(),
                String.valueOf(row.getSpeciesCode()),
                String.valueOf(row.getAdultMales()),
                String.valueOf(row.getAdultFemales()),
                String.valueOf(row.getYoungMales()),
                String.valueOf(row.getYoungFemales()),
                String.valueOf(row.getAdultsNonEdible()),
                String.valueOf(row.getYoungNonEdible()),
                toStringTrimToNull(row.getTotalHuntingArea()),
                toStringTrimToNull(row.getEffectiveHuntingArea()),
                toStringTrimToNull(row.getRemainingPopulationInTotalArea()),
                toStringTrimToNull(row.getRemainingPopulationInEffectiveArea()),
        };
    }

    private static String toStringTrimToNull(Integer i) {
        return i == null ? "" : String.valueOf(i);
    }
}

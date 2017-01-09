package fi.riista.integration.lupahallinta;

import fi.riista.config.web.CSVHttpResponse;
import fi.riista.integration.lupahallinta.club.LHHuntingClubCSVRow;
import fi.riista.integration.lupahallinta.club.LHHuntingClubLineFieldMapper;
import fi.riista.util.F;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LHHuntingClubCSVRowCSVResponse extends CSVHttpResponse {

    private static final String[] HEADER = new String[]{"Organisaation nimi (FIN)", "Organisaation nimi (SWE)",
            "Valittu alue", "Asiakasnumero", "RHY", "Hirvitalousalue",
            "Organisaation P-koordinaatti", "Organisaation I-koordinaatti", "Pinta-ala", "Yhteyshenkilö",
            "Valittu alue", "Henkilötunnus", "Osoite", "Osoite 2", "Postinumero ja postitoimipaikka", "RHY", "Titteli",
            "Puhelin 1", "Puhelin 2", "Sähköpostiosoite", "Kieli"
    };

    public LHHuntingClubCSVRowCSVResponse(String filename, String[] headerRow, List<String[]> rows) {
        super(filename, headerRow, rows);
    }

    public static CSVHttpResponse create(List<LHHuntingClubCSVRow> rows) {
        return new LHHuntingClubCSVRowCSVResponse(
                "huntingclubs.csv",
                HEADER,
                F.mapNonNullsToList(rows, LHHuntingClubLineFieldMapper::rowToCsv));

    }

    @Override
    public Charset getCharset() {
        return StandardCharsets.ISO_8859_1;
    }
}

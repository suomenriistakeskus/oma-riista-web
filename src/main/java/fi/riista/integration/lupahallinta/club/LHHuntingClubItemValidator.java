package fi.riista.integration.lupahallinta.club;

import com.google.common.base.Joiner;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.util.regex.Pattern;

public class LHHuntingClubItemValidator implements ItemProcessor<LHHuntingClubCSVRow, LHHuntingClubCSVRow> {
    private static final Logger LOG = LoggerFactory.getLogger(LHHuntingClubItemValidator.class);

    private static final String[] IGNORE_KEYWORDS = new String[]{
            "poliisilaitos",
            "riistanhoitoyhdistys",
            "hätäkeskus",
            "kaupunki",
            "seurakunta",
            "tutkimuslaitos",
            "ely-keskus"
    };
    private static final Pattern IGNORE_PATTERN = Pattern.compile(
            "^.*(" + Joiner.on('|').join(IGNORE_KEYWORDS) + ").*$", Pattern.CASE_INSENSITIVE);

    private static final Pattern RHY_PATTERN = Pattern.compile("^\\d{3}[^\\d]?.*$");
    private static final Pattern MOOSE_AREA_PATTERN = Pattern.compile("^\\d{7}[^\\d]?.*$");

    private static String cleanupRhyOfficialCode(final String officialCode) {
        return officialCode != null && RHY_PATTERN.matcher(officialCode).matches()
                ? officialCode.substring(0, 3) : null;
    }

    private static String cleanupMooseAreaCode(final String officialCode) {
        return officialCode != null && MOOSE_AREA_PATTERN.matcher(officialCode).matches()
                ? officialCode.substring(0, 7) : null;
    }

    @Override
    public LHHuntingClubCSVRow process(final LHHuntingClubCSVRow row) {
        row.setRhy(cleanupRhyOfficialCode(row.getRhy()));
        row.setRhy2(cleanupRhyOfficialCode(row.getRhy2()));
        row.setHirvitalousAlue(cleanupMooseAreaCode(row.getHirvitalousAlue()));

        if (StringUtils.equalsIgnoreCase(row.getNimiRuotsi(), row.getNimiSuomi())) {
            // Clear identical Swedish name making searching easier
            row.setNimiRuotsi(null);
        }

        if (shouldSkip(row) && !"1039177".equals(row.getAsiakasNumero())) {
            LOG.info("Skipping item: {}", row);
            return null;
        }

        return row;
    }


    private static boolean shouldSkip(final LHHuntingClubCSVRow row) {
        return row.getAsiakasNumero() == null ||
                row.getAsiakasNumero().length() != 7 ||
                row.getNimiSuomi() == null ||
                row.getNimiSuomi().isEmpty() ||
                IGNORE_PATTERN.matcher(row.getNimiSuomi()).matches();
    }
}

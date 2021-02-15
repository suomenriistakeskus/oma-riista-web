package fi.riista.integration.lupahallinta.parser;

import com.google.common.base.Splitter;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.validation.Validators;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class PermitCSVLineParser {
    private static final int MINIMUM_FIELD_COUNT = 19;

    private static final Splitter COMMA_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();

    private final List<String> errors;
    private final PermitCSVSpeciesAmountParser amountParser;

    public PermitCSVLineParser(final List<String> errors) {
        this.errors = errors;
        this.amountParser = new PermitCSVSpeciesAmountParser(errors);
    }

    public PermitCSVLine parse(String[] line) {
        PermitCSVLine csvLine = new PermitCSVLine();

        // equal or more fields is ok.
        if (line.length < MINIMUM_FIELD_COUNT) {
            errors.add("Rivillä ei riittävästi kenttiä. Tarvitaan vähintään: " + MINIMUM_FIELD_COUNT + ", rivillä on kenttiä:" + line.length);
            return csvLine;
        }

        csvLine.setContactPersonSsn(parseSsn(line[0]));
        csvLine.setPermitHolderClub(line[1]);
        csvLine.setPermitPartners(COMMA_SPLITTER.splitToList(line[2]));
        csvLine.setPermitNumber(parsePermitNumber(line[3]));
        csvLine.setPermitTypeCode(line[4]);
        csvLine.setPermitTypeName(line[5]);
        csvLine.setSpeciesAmounts(amountParser.parse(line[6], line[7], line[8], line[9], line[11], line[12], line[14],
                PermitTypeCode.isMooselikePermitTypeCode(csvLine.getPermitTypeCode())));
        csvLine.setRhyOfficialCode(line[10]);
        csvLine.setOriginalPermitNumber(line[13]);
        csvLine.setPrintingUrl(line[15]);
        csvLine.setHtaNumber(line[16]);
        csvLine.setRelatedRhys(COMMA_SPLITTER.splitToList(line[17]));
        csvLine.setPermitAreaSize(line[18]);

        return csvLine;
    }

    private String parseSsn(final String ssn) {
        if (StringUtils.isBlank(ssn)) {
            errors.add("Puuttuva HETU");
            return null;
        }

        if (!Validators.isValidSsn(ssn)) {
            errors.add("Virheellinen HETU:" + ssn);
            return null;
        }

        return ssn;
    }

    private String parsePermitNumber(final String permitNumber) {
        if (StringUtils.isBlank(permitNumber)) {
            errors.add("Puuttuva lupanumero");
            return null;
        }

        if (!Validators.isValidPermitNumber(permitNumber)) {
            errors.add("Virheellinen lupanumero:" + permitNumber);
            return null;
        }

        return permitNumber;
    }
}

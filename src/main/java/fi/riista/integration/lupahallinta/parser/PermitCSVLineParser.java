package fi.riista.integration.lupahallinta.parser;

import com.google.common.base.Splitter;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.validation.FinnishHuntingPermitNumberValidator;
import fi.riista.validation.FinnishSocialSecurityNumberValidator;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class PermitCSVLineParser {
    private static final FinnishSocialSecurityNumberValidator SSN_VALIDATOR = new FinnishSocialSecurityNumberValidator();
    private static final FinnishHuntingPermitNumberValidator PERMIT_NUMBER_VALIDATOR = new FinnishHuntingPermitNumberValidator();

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
        csvLine.setPermitHolder(line[1]);
        csvLine.setPermitPartners(COMMA_SPLITTER.splitToList(line[2]));
        csvLine.setPermitNumber(parsePermitNumber(line[3]));
        csvLine.setPermitTypeCode(line[4]);
        csvLine.setPermitTypeName(line[5]);
        csvLine.setSpeciesAmounts(amountParser.parse(line[6], line[7], line[8], line[9], line[11], line[12], line[14],
                HarvestPermit.isMooselikePermitTypeCode(csvLine.getPermitTypeCode())));
        csvLine.setRhyOfficialCode(line[10]);
        csvLine.setOriginalPermitNumber(line[13]);
        csvLine.setPrintingUrl(line[15]);
        csvLine.setHtaNumber(line[16]);
        csvLine.setRelatedRhys(COMMA_SPLITTER.splitToList(line[17]));
        csvLine.setPermitAreaSize(line[18]);

        return csvLine;
    }

    private String parseSsn(String ssn) {
        if (StringUtils.isBlank(ssn)) {
            errors.add("Puuttuva HETU");
            return null;
        }

        if (!SSN_VALIDATOR.isValid(ssn, null)) {
            errors.add("Virheellinen HETU:" + ssn);
            return null;
        }

        return ssn;
    }

    private String parsePermitNumber(String permitNumber) {
        if (StringUtils.isBlank(permitNumber)) {
            errors.add("Puuttuva lupanumero");
            return null;
        }

        if (!PERMIT_NUMBER_VALIDATOR.isValid(permitNumber, null)) {
            errors.add("Virheellinen lupanumero:" + permitNumber);
            return null;
        }

        return permitNumber;
    }
}

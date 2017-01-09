package fi.riista.integration.lupahallinta;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Errors in permits parsing should rollback the transaction. This is used to return the parsed errors.
 */
public class HarvestPermitImportException extends Exception {

    private final List<HarvestPermitImportResultDTO.PermitParsingError> allErrors;

    public HarvestPermitImportException(List<HarvestPermitImportResultDTO.PermitParsingError> allErrors) {
        super(allErrors.stream().flatMap(e -> e.getErrors().stream()).collect(Collectors.joining(", ")));
        this.allErrors = allErrors;
    }

    public List<HarvestPermitImportResultDTO.PermitParsingError> getAllErrors() {
        return allErrors;
    }
}

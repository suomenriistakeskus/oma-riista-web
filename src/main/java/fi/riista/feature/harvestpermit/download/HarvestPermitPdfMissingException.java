package fi.riista.feature.harvestpermit.download;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class HarvestPermitPdfMissingException extends IllegalArgumentException {
    public HarvestPermitPdfMissingException(final String permitNumber) {
        super("PDF not available for permitNumber=" + permitNumber);
    }
}

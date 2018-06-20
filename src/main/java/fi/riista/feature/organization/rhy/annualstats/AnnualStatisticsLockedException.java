package fi.riista.feature.organization.rhy.annualstats;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AnnualStatisticsLockedException extends IllegalStateException {

    public AnnualStatisticsLockedException() {
    }

    public AnnualStatisticsLockedException(final String msg) {
        super(msg);
    }
}

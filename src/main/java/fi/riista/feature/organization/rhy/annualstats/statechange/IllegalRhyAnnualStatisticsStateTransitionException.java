package fi.riista.feature.organization.rhy.annualstats.statechange;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IllegalRhyAnnualStatisticsStateTransitionException extends IllegalStateException {

    public IllegalRhyAnnualStatisticsStateTransitionException(final String msg) {
        super(msg);
    }
}

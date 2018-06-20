package fi.riista.feature.organization.rhy.annualstats;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Collection;

import static java.util.stream.Collectors.joining;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AnnualStatisticsModeratorFieldException extends IllegalArgumentException {

    public AnnualStatisticsModeratorFieldException(final String msg) {
        super(msg);
    }

    public static void requestInvolvesMutationOnlyAllowedForModerator(final String mutation) {
        throw new AnnualStatisticsModeratorFieldException(mutation);
    }

    public static void requestInvolvesMutationsOnlyAllowedForModerator(final Collection<String> mutations) {
        throw new AnnualStatisticsModeratorFieldException(mutations.stream().collect(joining(", ")));
    }
}

package fi.riista.feature.gamediary.harvest;

import com.google.common.collect.Streams;

import java.util.Set;

import static java.util.stream.Collectors.joining;

public class HarvestFieldValidationException extends RuntimeException {
    private final Set<HarvestFieldName> illegalFields;
    private final Set<HarvestFieldName> missingFields;

    public HarvestFieldValidationException(final Set<HarvestFieldName> illegalFields,
                                           final Set<HarvestFieldName> missingFields) {
        super(Streams.concat(
                illegalFields.stream().map(a -> "illegal " + a),
                missingFields.stream().map(a -> "missing " + a)).collect(joining(", ")));
        this.illegalFields = illegalFields;
        this.missingFields = missingFields;
    }

    public Set<HarvestFieldName> getIllegalFields() {
        return illegalFields;
    }

    public Set<HarvestFieldName> getMissingFields() {
        return missingFields;
    }
}

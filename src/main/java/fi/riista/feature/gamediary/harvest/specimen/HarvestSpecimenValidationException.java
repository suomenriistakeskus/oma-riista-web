package fi.riista.feature.gamediary.harvest.specimen;

import com.google.common.collect.Streams;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public class HarvestSpecimenValidationException extends RuntimeException {

    private final Set<HarvestSpecimenFieldName> missingFields;
    private final Set<HarvestSpecimenFieldName> illegalFields;
    private final Map<HarvestSpecimenFieldName, String> illegalValues;

    public HarvestSpecimenValidationException(final int speciesCode,
                                              final Set<HarvestSpecimenFieldName> missingFields,
                                              final Set<HarvestSpecimenFieldName> illegalFields,
                                              final Map<HarvestSpecimenFieldName, String> illegalValues) {
        super(Streams
                .concat(illegalFields.stream().map(a -> "illegal " + a),
                        illegalValues.entrySet().stream().map(a -> "invalid " + a.getKey().name() + ": " + a.getValue()),
                        missingFields.stream().map(a -> "missing " + a))
                .collect(joining(", ", format("Game species code %d: ", speciesCode), "")));

        this.illegalFields = requireNonNull(illegalFields);
        this.missingFields = requireNonNull(missingFields);
        this.illegalValues = requireNonNull(illegalValues);
    }

    public Set<HarvestSpecimenFieldName> getMissingFields() {
        return missingFields;
    }

    public Set<HarvestSpecimenFieldName> getIllegalFields() {
        return illegalFields;
    }

    public Map<HarvestSpecimenFieldName, String> getIllegalValues() {
        return illegalValues;
    }
}

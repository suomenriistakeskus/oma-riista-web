package fi.riista.integration.metsastajarekisteri.person.foreign;

import fi.riista.config.Constants;
import fi.riista.integration.metsastajarekisteri.person.MetsastajaRekisteriPerson;
import fi.riista.util.Functions;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.springframework.batch.item.ItemWriter;

import javax.validation.ConstraintViolationException;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public class MetsastajaRekisteriForeignPersonWriter implements ItemWriter<MetsastajaRekisteriPerson> {

    private final MetsastajaRekisteriForeignPersonImportService importService;
    private final DateTime importTimestamp;

    public MetsastajaRekisteriForeignPersonWriter(final MetsastajaRekisteriForeignPersonImportService importService,
                                                  final long importTimestamp) {

        this.importService = requireNonNull(importService, "importService is null");
        this.importTimestamp = new Instant(importTimestamp).toDateTime(Constants.DEFAULT_TIMEZONE);
    }

    @Override
    public void write(final List<? extends MetsastajaRekisteriPerson> items) {
        try {
            importService.updateForeignPersons(items, importTimestamp);

        } catch (final ConstraintViolationException cve) {
            final String joinedContraintViolations = cve
                    .getConstraintViolations().stream()
                    .map(Functions.CONSTRAINT_VIOLATION_TO_STRING)
                    .collect(joining("\n"));

            final String msg = String.format("JSR-303 constraint violations:%n%s", joinedContraintViolations);

            throw new ConstraintViolationException(msg, cve.getConstraintViolations());
        }
    }
}

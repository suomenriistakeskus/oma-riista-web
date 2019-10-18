package fi.riista.integration.mmm.statement;

import com.ancientprogramming.fixedformat4j.exception.FixedFormatException;
import com.ancientprogramming.fixedformat4j.format.FixedFormatManager;
import com.ancientprogramming.fixedformat4j.format.impl.FixedFormatManagerImpl;
import com.google.common.io.CharSource;
import fi.riista.util.fixedformat.LocalDateFormatter;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class AccountStatementParser {

    private static final FixedFormatManager MANAGER = new FixedFormatManagerImpl();

    public static AccountStatement parseFile(@Nonnull final String fileContent) {
        requireNonNull(fileContent);

        final CharSource cs = CharSource.wrap(fileContent);

        try {

            final String firstRawLine = cs.readFirstLine();
            final LocalDate statementDate;

            // The first line has an unknown format except for the account statement date.
            if (firstRawLine.trim().length() < 25 && firstRawLine.matches(".\\d{6}.+")) {
                statementDate = LocalDateFormatter.parseDate(firstRawLine.substring(1, 7));
            } else {
                throw new AccountStatementParseException(
                        format("Could not interpret the first line of account statement: %s", firstRawLine));
            }

            final AtomicInteger lineNumberHolder = new AtomicInteger(2);
            final AtomicReference<String> unparseableLine = new AtomicReference<>();

            final List<AccountStatementLine> validLines = cs.lines()
                    .skip(1)
                    .filter(line -> {
                        if (unparseableLine.get() != null) {
                            throw AccountStatementParseException
                                    .invalidContent(lineNumberHolder.get(), unparseableLine.get());
                        }

                        // Each valid line starts with '3' or '5'. Currently, the last line is
                        // known to have a differing and an unknown format.
                        if (!line.startsWith("3") && !line.startsWith("5")) {
                            unparseableLine.set(line);
                            return false;
                        }

                        return true;
                    })
                    .map(line -> parseLine(line, Optional.of(lineNumberHolder.getAndIncrement())))
                    .collect(toList());

            return new AccountStatement(statementDate, validLines);

        } catch (final IOException ioe) {
            throw new AccountStatementParseException("Parsing account statement failed", ioe);
        }
    }

    public static AccountStatementLine parseLine(final String line, final Optional<Integer> lineNumOpt) {
        try {
            final AccountStatementLine parsedLine = MANAGER.load(AccountStatementLine.class, line);
            AccountStatementLineValidator.validate(parsedLine);
            return parsedLine;

        } catch (final FixedFormatException e) {

            final String lineNumStr = lineNumOpt.map(lineNum -> " #" + lineNum).orElse("");
            final String errMsg = format("Error while parsing account statement line%s: %s", lineNumStr, line);

            throw new AccountStatementParseException(errMsg, e);

        } catch (final AccountStatementLineValidationException e) {
            final String lineNumStr = lineNumOpt.map(lineNum -> format(" #%d", lineNum)).orElse("");
            final String errMsg = format(
                    "Validation of account statement line%s failed: %s", lineNumStr, e.getMessage());
            throw new AccountStatementLineValidationException(errMsg, e);
        }
    }
}

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

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class AccountStatementParser {

    private static final FixedFormatManager MANAGER = new FixedFormatManagerImpl();

    public static AccountStatement parseFile(@Nonnull final String fileContent) {
        requireNonNull(fileContent);

        final CharSource cs = CharSource.wrap(fileContent);

        try {

            final LocalDate statementDateOnFirstLine =
                    parseDateFromBankAccountHeaderLine(cs.readFirstLine(), Optional.of(1));

            final AtomicInteger lineNumberHolder = new AtomicInteger(1);

            final List<AccountStatementLine> validLines = cs.lines()
                    .skip(1)
                    .filter(line -> {
                        final int currentLineNum = lineNumberHolder.incrementAndGet();

                        // Each account transfer line starts with '3' or '5'.
                        if (!line.startsWith("3") && !line.startsWith("5")) {

                            // Bank account header line starts with '0'. Check that account statement date is consistent.
                            if (line.startsWith("0")) {
                                final Optional<Integer> lineNumberOpt = Optional.of(currentLineNum);
                                final LocalDate statementDate = parseDateFromBankAccountHeaderLine(line, lineNumberOpt);

                                if (!statementDate.equals(statementDateOnFirstLine)) {
                                    throw new AccountStatementParseException(format(
                                            "Account statement date mismatch at line%s: %s", lineNumberOpt, statementDate));
                                }
                            } else if (!line.startsWith("9")) {
                                // The lines starting with '9' are some kind of summary/assembly lines for each bank account.
                                // The format of '9' lines is unknown.
                                throw AccountStatementParseException.invalidContent(currentLineNum, line);
                            }

                            return false;
                        }

                        return true;
                    })
                    .map(line -> parseAccountTransferLine(line, Optional.of(lineNumberHolder.get())))
                    .collect(toList());

            return new AccountStatement(statementDateOnFirstLine, validLines);

        } catch (final IOException ioe) {
            throw new AccountStatementParseException("Parsing account statement failed", ioe);
        }
    }

    private static LocalDate parseDateFromBankAccountHeaderLine(final String line, final Optional<Integer> lineNumOpt) {
        // The first line has an unknown format except for the account statement date.
        if (line.trim().length() < 25 && line.matches(".\\d{6}.+")) {
            return LocalDateFormatter.parseDate(line.substring(1, 7));
        } else {
            throw new AccountStatementParseException(format(
                    "Could not interpret the bank account header line at %s: %s", formatLineNumber(lineNumOpt), line));
        }
    }

    public static AccountStatementLine parseAccountTransferLine(final String line, final Optional<Integer> lineNumOpt) {
        try {
            final AccountStatementLine parsedLine = MANAGER.load(AccountStatementLine.class, line);
            AccountStatementLineValidator.validate(parsedLine);
            return parsedLine;

        } catch (final FixedFormatException e) {
            final String errMsg =
                    format("Error while parsing account statement line%s: %s", formatLineNumber(lineNumOpt), line);
            throw new AccountStatementParseException(errMsg, e);

        } catch (final AccountStatementLineValidationException e) {
            final String errMsg = format(
                    "Validation of account statement line%s failed: %s", formatLineNumber(lineNumOpt), e.getMessage());
            throw new AccountStatementLineValidationException(errMsg, e);
        }
    }

    private static String formatLineNumber(final Optional<Integer> lineNumOpt) {
        return lineNumOpt.map(lineNum -> format(" #%d", lineNum)).orElse("");
    }
}

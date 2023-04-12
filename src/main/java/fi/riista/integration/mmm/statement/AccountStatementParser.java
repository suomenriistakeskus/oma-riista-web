package fi.riista.integration.mmm.statement;

import com.ancientprogramming.fixedformat4j.exception.FixedFormatException;
import com.ancientprogramming.fixedformat4j.format.FixedFormatManager;
import com.ancientprogramming.fixedformat4j.format.impl.FixedFormatManagerImpl;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.CharSource;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class AccountStatementParser {

    private static final FixedFormatManager MANAGER = new FixedFormatManagerImpl();

    public static AccountStatement parseFile(@Nonnull final String fileContent) {
        requireNonNull(fileContent);

        final CharSource cs = CharSource.wrap(fileContent);

        try {
            final ImmutableList<String> lines = cs.readLines();
            final ArrayList<AccountStatementLine> validLines = Lists.newArrayListWithExpectedSize(lines.size());

            for (int idx = 0; idx < lines.size(); ++idx) {
                final int lineNum = idx + 1;
                final String line = lines.get(idx);

                switch (line.charAt(0)) {
                    // Bank related metadata, skipped
                    case '0':
                    case '9':
                        continue;
                    // Account statements, processed
                    case '3':
                    case '5':
                        validLines.add(parseAccountTransferLine(line, lineNum));
                        break;
                    default:
                        throw AccountStatementParseException.invalidContent(lineNum, line);
                }
            }

            return new AccountStatement(validLines);

        } catch (final IOException ioe) {
            throw new AccountStatementParseException("Parsing account statement failed", ioe);
        }
    }

    public static AccountStatementLine parseAccountTransferLine(final String line, final int lineNum) {
        try {
            final AccountStatementLine parsedLine = MANAGER.load(AccountStatementLine.class, line);
            AccountStatementLineValidator.validate(parsedLine);
            return parsedLine;

        } catch (final FixedFormatException e) {
            final String errMsg =
                    format("Error while parsing account statement line #%s: %s", lineNum, line);
            throw new AccountStatementParseException(errMsg, e);

        } catch (final AccountStatementLineValidationException e) {
            final String errMsg = format(
                    "Validation of account statement line #%s failed: %s", lineNum, e.getMessage());
            throw new AccountStatementLineValidationException(errMsg, e);
        }
    }
}

package fi.riista.integration.mmm.statement;

import fi.riista.feature.common.entity.CreditorReference;
import org.apache.commons.lang.StringUtils;
import org.iban4j.IbanFormatException;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Supplier;

import static fi.riista.integration.mmm.statement.MMMConstants.CURRENCY_CODE_EURO;
import static fi.riista.integration.mmm.statement.MMMConstants.MMM_ACCOUNT_NUMBER;
import static fi.riista.integration.mmm.statement.MMMConstants.VALID_REVERSAL_INDICATOR;
import static fi.riista.util.fixedformat.FixedFormatHelper.asString;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.util.Objects.requireNonNull;

public class AccountStatementLineValidator {

    public static void validate(@Nonnull final AccountStatementLine line) {
        requireNonNull(line);

        final String accountNumber = line.getCreditorAccountNumber();
        try {
            line.getCreditorAccountNumberAsIban();
        } catch (final IbanFormatException e) {
            throw new AccountStatementLineValidationException("invalid creditor account number: " + accountNumber, e);
        }
        mustEqual("creditor account number", MMM_ACCOUNT_NUMBER, accountNumber);

        final LocalDate bookingDate = line.getBookingDate();
        final LocalDate transactionDate = line.getTransactionDate();

        mustNotBeNull("booking date", bookingDate);
        mustNotBeNull("transaction date", transactionDate);
        mustHold(!bookingDate.isBefore(transactionDate), () -> {
            return format("booking date %s must not be before transaction date %s", bookingDate, transactionDate);
        });

        mustNotBeBlank("account service reference", line.getAccountServiceReference());

        final String creditorReference = line.getCreditorReference();
        mustNotBeNull("creditor reference", creditorReference);

        mustHold(CreditorReference.fromNullable(creditorReference).isValid(), () -> {
            return "creditor reference validation failed: " + creditorReference.toString();
        });

        mustNotBeBlank("debtor name abbreviation", line.getDebtorNameAbbrv());
        mustEqual("currency code", CURRENCY_CODE_EURO, line.getCurrencyCode());

        final BigDecimal amount = line.getAmount();

        mustNotBeNull("amount", amount);
        mustHold(amount.compareTo(ZERO) > 0, () -> "amount must be positive");

        mustEqual("reversal indicator", VALID_REVERSAL_INDICATOR, line.getReversalIndicator());
    }

    private static void mustNotBeNull(final String fieldName, final Object value) {
        if (value == null) {
            throw new AccountStatementLineValidationException(format("%s: must not be null", fieldName));
        }
    }

    private static void mustNotBeBlank(final String fieldName, final String str) {
        if (StringUtils.isBlank(str)) {
            throw new AccountStatementLineValidationException(format("%s: must not be blank", fieldName));
        }
    }

    private static void mustEqual(final String fieldName, final Object expected, final Object value) {
        if (!Objects.equals(expected, value)) {
            throw new AccountStatementLineValidationException(format("%s: illegal value: %s", fieldName, asString(value)));
        }
    }

    private static void mustHold(final boolean condition, final Supplier<String> failureMessage) {
        if (!condition) {
            throw new AccountStatementLineValidationException(failureMessage.get());
        }
    }
}

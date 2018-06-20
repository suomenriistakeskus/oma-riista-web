package fi.riista.feature.permit.invoice;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;
import static org.joda.time.Days.days;

public enum InvoiceType {

    PERMIT_PROCESSING(days(21), days(14)),
    PERMIT_HARVEST(days(7), null);

    private final Days termOfPayment;
    private final Days delayOfEmailReminder;

    InvoiceType(@Nonnull final Days termOfPaymentDays, @Nullable final Days delayOfEmailReminderDays) {
        this.termOfPayment = requireNonNull(termOfPaymentDays);
        this.delayOfEmailReminder = delayOfEmailReminderDays;
    }

    public LocalDate calculateDueDate(@Nonnull final LocalDate invoiceCreationDate) {
        return invoiceCreationDate.plus(termOfPayment);
    }

    public Days getDaysOfEmailReminderBeforeDueDate() {
        return termOfPayment.minus(getDelayOfEmailReminder());
    }

    // Accessors -->

    public Days getTermOfPayment() {
        return termOfPayment;
    }

    public Days getDelayOfEmailReminder() {
        if (delayOfEmailReminder == null) {
            throw new UnsupportedOperationException("delayOfEmailReminder not available for " + this.name());
        }
        return delayOfEmailReminder;
    }
}

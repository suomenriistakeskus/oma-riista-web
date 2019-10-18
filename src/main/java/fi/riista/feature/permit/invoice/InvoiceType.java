package fi.riista.feature.permit.invoice;

import fi.riista.integration.paytrail.auth.PaytrailAccount;
import fi.riista.util.LocalisedString;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

import static java.util.Objects.requireNonNull;
import static org.joda.time.Days.days;

public enum InvoiceType {
    PERMIT_PROCESSING(PaytrailAccount.RIISTAKESKUS, days(21), new LocalisedString("Käsittelymaksu", "Handläggningsavgift"), days(14)),
    PERMIT_HARVEST(PaytrailAccount.MMM, days(7), new LocalisedString("Pyyntilupamaksu", "Licensavgift"), null);

    private final PaytrailAccount paytrailAccount;
    private final Days termOfPayment;
    private final LocalisedString name;
    private final Days delayOfEmailReminder;

    InvoiceType(@Nonnull final PaytrailAccount paytrailAccount,
                @Nonnull final Days termOfPaymentDays,
                @Nonnull final LocalisedString name,
                @Nullable final Days delayOfEmailReminderDays) {
        this.paytrailAccount = requireNonNull(paytrailAccount);
        this.termOfPayment = requireNonNull(termOfPaymentDays);
        this.name = requireNonNull(name);
        this.delayOfEmailReminder = delayOfEmailReminderDays;
    }

    public LocalDate calculateDueDate(@Nonnull final LocalDate invoiceCreationDate) {
        return invoiceCreationDate.plus(termOfPayment);
    }

    public Days getDaysOfEmailReminderBeforeDueDate() {
        return termOfPayment.minus(getDelayOfEmailReminder());
    }

    // Accessors -->

    public PaytrailAccount getPaytrailAccount() {
        return paytrailAccount;
    }

    public Days getTermOfPayment() {
        return termOfPayment;
    }

    public Days getDelayOfEmailReminder() {
        if (delayOfEmailReminder == null) {
            throw new UnsupportedOperationException("delayOfEmailReminder not available for " + this.name());
        }
        return delayOfEmailReminder;
    }

    public String getName(final Locale locale) {
        return name.getAnyTranslation(locale);
    }
}

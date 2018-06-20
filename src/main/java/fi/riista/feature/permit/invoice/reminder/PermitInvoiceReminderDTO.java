package fi.riista.feature.permit.invoice.reminder;

import java.util.Collection;
import java.util.Locale;

import static java.util.Objects.requireNonNull;

public class PermitInvoiceReminderDTO {
    private Locale locale;
    private Collection<String> recipientEmails;

    private Long harvestPermitId;

    public PermitInvoiceReminderDTO(final Locale locale, final Collection<String> recipientEmails,
                                    final Long harvestPermitId) {
        this.locale = requireNonNull(locale);
        this.recipientEmails = requireNonNull(recipientEmails);
        this.harvestPermitId = requireNonNull(harvestPermitId);
    }

    public Locale getLocale() {
        return locale;
    }

    public Collection<String> getRecipientEmails() {
        return recipientEmails;
    }

    public Long getHarvestPermitId() {
        return harvestPermitId;
    }
}

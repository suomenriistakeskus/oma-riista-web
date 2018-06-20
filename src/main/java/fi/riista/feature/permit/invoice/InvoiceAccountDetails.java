package fi.riista.feature.permit.invoice;

import org.iban4j.Bic;
import org.iban4j.Iban;

import java.util.Objects;

public class InvoiceAccountDetails {
    public static InvoiceAccountDetails create(final Invoice invoice) {
        return new InvoiceAccountDetails(invoice.getBic(), invoice.getIban());
    }

    private static String getBankName(final String bic) {
        if ("NDEAFIHH".equalsIgnoreCase(bic)) {
            return "Nordea";

        } else if ("OKOYFIHH".equalsIgnoreCase(bic)) {
            return "OP-Pohjola";
        }

        return "";
    }

    private final Bic bic;
    private final Iban iban;
    private final String bankName;

    public InvoiceAccountDetails(final Bic bic, final Iban iban) {
        this.bic = Objects.requireNonNull(bic, "bic is null");
        this.iban = Objects.requireNonNull(iban, "iban is null");
        this.bankName = getBankName(bic.toString());
    }

    public Bic getBic() {
        return bic;
    }

    public String getCombinedBankNameAndIbanString() {
        return String.format("%-12s %s", bankName, iban.toFormattedString());
    }

    public Iban getIban() {
        return iban;
    }
}

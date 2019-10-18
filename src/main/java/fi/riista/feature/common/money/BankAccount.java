package fi.riista.feature.common.money;

import org.iban4j.Bic;
import org.iban4j.Iban;

import static java.util.Objects.requireNonNull;

public class BankAccount {

    private final Iban iban;
    private final Bic bic;
    private final String bankName;

    public BankAccount(final Bic bic, final Iban iban, final String bankName) {
        this.bic = requireNonNull(bic, "bic is null");
        this.iban = requireNonNull(iban, "iban is null");
        this.bankName = requireNonNull(bankName, "bankName is null");
    }

    public BankAccount(final String bic, final String iban, final String bankName) {
        this(Bic.valueOf(bic), Iban.valueOf(iban), bankName);
    }

    public String getCombinedBankNameAndIbanForInvoicePdf() {
        return String.format("%-12s %s", bankName, iban.toFormattedString());
    }

    // Accessors -->

    public Iban getIban() {
        return iban;
    }

    public Bic getBic() {
        return bic;
    }

    public String getBankName() {
        return bankName;
    }
}

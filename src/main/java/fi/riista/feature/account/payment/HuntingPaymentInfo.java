package fi.riista.feature.account.payment;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import fi.riista.feature.common.entity.CreditorReference;
import fi.riista.util.InvoiceUtil;
import org.iban4j.Bic;
import org.iban4j.Iban;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

public final class HuntingPaymentInfo {

    private static final List<AccountDetails> ACCOUNT_DETAILS = ImmutableList.of(
            new AccountDetails(Bic.valueOf("OKOYFIHH"), Iban.valueOf("FI7850000120378442"), "OP-Pohjola"),
            new AccountDetails(Bic.valueOf("NDEAFIHH"), Iban.valueOf("FI1216603000107212"), "Nordea"),
            new AccountDetails(Bic.valueOf("DABAFIHH"), Iban.valueOf("FI8480001300035350"), "Danske"));

    private static final String PAYMENT_RECEIVER = "RIISTANHOITOMAKSUJEN KERÄILYTILI\nSAMLINGSKONTO FÖR JAKTVÅRDSAVGIFTER";

    private static final String ADDITIONAL_INFO = "Käytä allaolevaa viitenumeroa, sillä ilman viitettä maksettu\n" +
            "maksu ei kohdistu oikein\n" +
            "HUOM! Maksettaessa ulkomailta on myös vastaanottajan kulut maksettava\n" +
            "\n" +
            "Använd nedannämda referensnummer, för utan referensnummer\n" +
            "är det omöjligt att kontera betalningen rätt.\n" +
            "OBS! När du betalar utomlands, måste du betala\n" +
            "också mottagarbankens omkostnader.";

    public static boolean isPaymentInfoAvailable(final int huntingYear) {
        return huntingYear >= 2015 && huntingYear <= 2017;
    }

    @Nonnull
    public static Optional<HuntingPaymentInfo> create(final int huntingYear, final String invoiceReference) {
        return isPaymentInfoAvailable(huntingYear)
                ? Optional.of(new HuntingPaymentInfo(33, 0, invoiceReference, ACCOUNT_DETAILS))
                : Optional.empty();
    }

    public static class AccountDetails {
        private final Bic bic;
        private final Iban iban;
        private final String bankName;

        public AccountDetails(final Bic bic, final Iban iban, final String bankName) {
            this.bic = Objects.requireNonNull(bic, "bic is null");
            this.iban = Objects.requireNonNull(iban, "iban is null");
            this.bankName = Objects.requireNonNull(bankName, "bankName is null");
        }

        public Bic getBic() {
            return bic;
        }

        public Iban getIban() {
            return iban;
        }

        public String getBankName() {
            return bankName;
        }
    }

    private final List<AccountDetails> accounts;
    private final int euros;
    private final int cents;
    private final CreditorReference invoiceReference;

    HuntingPaymentInfo(final int euros, final int cents,
                       final String invoiceReference,
                       final List<AccountDetails> accounts) {
        this.invoiceReference =
                CreditorReference.fromNullable(Objects.requireNonNull(invoiceReference, "invoiceReference is null"));
        this.euros = euros;
        this.cents = cents;
        this.accounts = Objects.requireNonNull(accounts, "accounts is null");

        // Sanity check
        Preconditions.checkArgument(accounts.size() > 0);
        Preconditions.checkArgument(cents >= 0 && cents < 100);
        Preconditions.checkArgument(euros >= 0 && euros < 100);
        Preconditions.checkArgument(this.invoiceReference.isValid());
    }

    @Nonnull
    public String getPaymentReceiverBic() {
        return accounts.stream()
                .map(a -> a.getBic().toString())
                .collect(joining("\n"));
    }

    @Nonnull
    public String getPaymentReceiverIban() {
        return accounts.stream()
                .map(a -> String.format("%-12s %s", a.getBankName(), a.getIban().toFormattedString()))
                .collect(joining("\n"));
    }

    @Nonnull
    public String getPaymentReceiver() {
        return PAYMENT_RECEIVER;
    }

    @Nonnull
    public String getAdditionalInfo() {
        return ADDITIONAL_INFO;
    }

    @Nonnull
    public String getAmountText() {
        return String.format("%d.%02d", euros, cents);
    }

    @Nonnull
    public String getInvoiceReferenceForHuman() {
        return invoiceReference.toString();
    }

    @Nonnull
    public Iban getIbanForBarCode() {
        return accounts.get(0).getIban();
    }

    @Nonnull
    public String createBarCodeMessage(final LocalDate dueDate) {
        return InvoiceUtil.createBarCodeMessage(euros, cents, invoiceReference, accounts.get(0).getIban(), dueDate);
    }
}

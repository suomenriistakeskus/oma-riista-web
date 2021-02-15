package fi.riista.feature.account.payment;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import fi.riista.feature.common.entity.CreditorReference;
import fi.riista.feature.common.money.FinnishBankAccount;
import fi.riista.util.InvoiceUtil;
import org.iban4j.Iban;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public final class HuntingPaymentInfo {

    private static final List<FinnishBankAccount> ACCOUNT_DETAILS_OLD = ImmutableList.of(
            FinnishBankAccount.GAME_MANAGEMENT_FEE_OP_POHJOLA,
            FinnishBankAccount.GAME_MANAGEMENT_FEE_NORDEA,
            FinnishBankAccount.GAME_MANAGEMENT_FEE_DANSKE_BANK);

    private static final List<FinnishBankAccount> ACCOUNT_DETAILS = ImmutableList.of(
            FinnishBankAccount.GAME_MANAGEMENT_FEE_DANSKE_BANK,
            FinnishBankAccount.GAME_MANAGEMENT_FEE_NORDEA);

    private static final String PAYMENT_RECEIVER = "RIISTANHOITOMAKSUJEN KERÄILYTILI\nSAMLINGSKONTO FÖR JAKTVÅRDSAVGIFTER";

    private static final String ADDITIONAL_INFO = "Käytä allaolevaa viitenumeroa, sillä ilman viitettä maksettu\n" +
            "maksu ei kohdistu oikein\n" +
            "HUOM! Maksettaessa ulkomailta on myös vastaanottajan kulut maksettava\n" +
            "\n" +
            "Använd nedannämda referensnummer, för utan referensnummer\n" +
            "är det omöjligt att kontera betalningen rätt.\n" +
            "OBS! När du betalar utomlands, måste du betala\n" +
            "också mottagarbankens omkostnader.";

    public static HuntingPaymentInfo create(final int huntingYear,
                                            final @Nonnull LocalDate dateOfBirth,
                                            final @Nonnull String invoiceReference) {
        requireNonNull(dateOfBirth);
        requireNonNull(invoiceReference);

        switch (huntingYear) {
            case 2018:
                return new HuntingPaymentInfo(39, 0, invoiceReference, ACCOUNT_DETAILS_OLD);

            case 2019: {
                // 31.7.2001 jälkeen syntyneet saavat 20 € laskun
                // 31.7.2001 ja sitä aiemmin syntyneet 39 € laskun
                final LocalDate dateBoundary = new LocalDate(2001, 7, 31);
                final int euros = dateOfBirth.isAfter(dateBoundary) ? 20 : 39;

                return new HuntingPaymentInfo(euros, 0, invoiceReference, ACCOUNT_DETAILS_OLD);
            }

            default: {
                // Minors (after 31.7.huntingYear), reduced payment
                final LocalDate dateBoundary = new LocalDate(huntingYear - 18, 7, 31);
                final int euros = dateOfBirth.isAfter(dateBoundary) ? 20 : 39;

                return new HuntingPaymentInfo(euros, 0, invoiceReference, ACCOUNT_DETAILS);
            }
        }
    }

    private final List<FinnishBankAccount> accounts;
    private final int euros;
    private final int cents;
    private final CreditorReference invoiceReference;

    HuntingPaymentInfo(final int euros, final int cents,
                       final String invoiceReference,
                       final List<FinnishBankAccount> accounts) {
        this.invoiceReference =
                CreditorReference.fromNullable(requireNonNull(invoiceReference, "invoiceReference is null"));
        this.euros = euros;
        this.cents = cents;
        this.accounts = requireNonNull(accounts, "accounts is null");

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

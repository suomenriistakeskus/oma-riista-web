package fi.riista.feature.account.payment;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import fi.riista.validation.FinnishCreditorReferenceValidator;
import org.apache.commons.lang.StringUtils;
import org.iban4j.Bic;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

public final class HuntingPaymentInfo {
    private static final DateTimeFormatter BAR_CODE_DUE_DATE_PATTERN = DateTimeFormat.forPattern("yyMMdd");
    private static final String PAYMENT_RECEIVER = "RIISTANHOITOMAKSUJEN KERÄILYTILI\nSAMLINGSKONTO FÖR JAKTVÅRDSAVGIFTER";
    private static final String ADDITIONAL_INFO = "Käytä allaolevaa viitenumeroa, sillä ilman viitettä maksettu\n" +
            "maksu ei kohdistu oikein\n" +
            "HUOM! Maksettaessa ulkomailta on myös vastaanottajan kulut maksettava\n" +
            "\n" +
            "Använd nedannämda referensnummer, för utan referensnummer\n" +
            "är det omöjligt att kontera betalningen rätt.\n" +
            "OBS! När du betalar utomlands, måste du betala\n" +
            "också mottagarbankens omkostnader.";

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

    public static HuntingPaymentInfo create(final int huntingYear, final String invoiceReference) {
        switch (huntingYear) {
            case 2015:
            case 2016:
                return new HuntingPaymentInfo(33, 0, invoiceReference, Arrays.asList(
                        new AccountDetails(Bic.valueOf("OKOYFIHH"), Iban.valueOf("FI7850000120378442"), "OP-Pohjola"),
                        new AccountDetails(Bic.valueOf("NDEAFIHH"), Iban.valueOf("FI1216603000107212"), "Nordea"),
                        new AccountDetails(Bic.valueOf("DABAFIHH"), Iban.valueOf("FI8480001300035350"), "Danske")));
            default:
                return null;
        }
    }

    private final List<AccountDetails> accounts;
    private final int euros;
    private final int cents;
    private final String invoiceReference;

    HuntingPaymentInfo(final int euros, final int cents,
                       final String invoiceReference,
                       final List<AccountDetails> accounts) {
        this.invoiceReference = Objects.requireNonNull(invoiceReference, "invoiceReference is null");
        this.euros = euros;
        this.cents = cents;
        this.accounts = Objects.requireNonNull(accounts, "accounts is null");

        // Sanity check
        Preconditions.checkArgument(accounts.size() > 0);
        Preconditions.checkArgument(cents >= 0 && cents < 100);
        Preconditions.checkArgument(euros >= 0 && euros < 100);
        Preconditions.checkArgument(FinnishCreditorReferenceValidator.validate(invoiceReference, true));
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
        // Viitenumero tulostetaan sille varattuun kenttään
        // oikealta vasemmalle viiden numeron ryhmiin,
        // joiden välissä on tyhjä merkkipaikka.
        final String reversed = StringUtils.reverse(this.invoiceReference.trim());
        final Iterable<String> parts = Splitter.fixedLength(5).split(reversed);

        // Etunollia ei tulosteta.
        return StringUtils.stripStart(StringUtils.reverse(Joiner.on(' ').join(parts)), "0");
    }

    @Nonnull
    public String getInvoiceReferenceForBarCode() {
        return StringUtils.leftPad(invoiceReference.replaceAll("[^0-9]", ""), 20, '0');
    }

    @Nonnull
    public String getIbanForBarCode() {
        final AccountDetails account = accounts.get(0);

        Preconditions.checkArgument(account.getIban().getCountryCode() == CountryCode.FI, "can only generate for FI");

        // Skip country-code
        return account.getIban().toString().substring(2);
    }

    @Nonnull
    public String createBarCodeMessage(final LocalDate dueDate) {
        // Create message for version 4
        return "4" +
                getIbanForBarCode() +
                String.format("%06d", euros) +
                String.format("%02d", cents) +
                // Reserved fixed value
                "000" +
                getInvoiceReferenceForBarCode() +
                (dueDate != null ? BAR_CODE_DUE_DATE_PATTERN.print(dueDate) : "000000");
    }
}

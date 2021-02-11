package fi.riista.feature.common.money;

import org.iban4j.Bic;
import org.iban4j.Iban;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public enum FinnishBank {

    // Source https://fi.wikipedia.org/wiki/Tilinumero

    DANSKE_BANK("DABAFIHH", "Danske", newHashSet("34", "8")),
    NORDEA("NDEAFIHH", "Nordea", newHashSet("1", "2")),
    OP_POHJOLA("OKOYFIHH", "OP", newHashSet("5")),
    AKTIA("HELSFIHH", "Aktia", newHashSet("405", "497")),
    BIGBANK("BIGKFIH1", "Bigbank", newHashSet("717")),
    POP("POPFFI22", "POP", newHashSet("470", "471", "472", "473", "474", "475", "476", "477", "478")),
    BONUM("POPFFI22", "Bonum", newHashSet("479")),
    CITIBANK("CITIFIHX", "Citibank", newHashSet("713")),
    DNBBANK("DNBAFIHX", "DNB Bank ASA", newHashSet("37")),
    HANDELSBANKEN("HANDFIHH", "Handelsbanken", newHashSet("31")),
    HOLVI("HOLVFIHH", "Holvi", newHashSet("799")),
    SEB("ESSEFIHX", "SEB", newHashSet("33")),
    SPANKKI("SBANFIHH", "S-Pankki", newHashSet("36", "39")),
    SWEDBANK("SWEDFIHH", "Swedbank", newHashSet("38")),
    OMASP("ITELFIHH", "Säästöpankit", newHashSet("715", "400", "402", "403", "406", "407",
            "408", "410", "411", "412", "414", "415", "416", "417", "418", "419", "420", "421",
            "423", "424", "425", "426", "427", "428", "429", "430", "431", "432",
            "435", "436", "437", "438", "439", "440", "441", "442", "443", "444", "445", "446", "447", "448", "449", "450", "451", "452",
            "454", "455", "456", "457", "458", "459", "460", "461", "462", "463", "464",
            "483", "484", "485", "486", "487", "488", "489", "490", "491", "492", "493",
            "495", "496")),
    AAB("AABAFI22", "Ålandsbanken", newHashSet("6"));

    private final Bic bic;
    private final String bankName;
    private final Set<String> bankCodePrefixes;

    @Nullable
    public static FinnishBank resolveFromBic(@Nullable final Bic bic) {
        return Arrays.stream(values())
                .filter(finnishBank -> finnishBank.bic.equals(bic))
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public static FinnishBank resolveFromIban(@Nullable final Iban iban) {
        if (iban == null) {
            return null;
        }

        final String bankCode = iban.getBankCode();

        for (final FinnishBank bank : values()) {
            if (bank.matchesBankCode(bankCode)) {
                return bank;
            }
        }

        return null;
    }

    @Nullable
    public static Bic resolveBic(@Nullable final Iban iban) {
        return Optional.ofNullable(iban).map(FinnishBank::resolveFromIban).map(FinnishBank::getBic).orElse(null);
    }

    FinnishBank(final String bic, final String bankName, final Set<String> bankCodePrefixes) {
        this.bic = Bic.valueOf(bic);
        this.bankName = bankName;
        this.bankCodePrefixes = Collections.unmodifiableSet(bankCodePrefixes);

        checkArgument(isNotBlank(bankName), "bankName must not be blank");
    }

    public boolean matchesIban(@Nonnull final Iban iban) {
        requireNonNull(iban);
        return matchesBankCode(iban.getBankCode());
    }

    private boolean matchesBankCode(@Nullable final String bankCode) {
        return bankCode != null ? bankCodePrefixes.stream().anyMatch(bankCode::startsWith) : false;
    }

    // Accessors -->

    public Bic getBic() {
        return bic;
    }

    public String getBankName() {
        return bankName;
    }

    public Set<String> getBankCodePrefixes() {
        return bankCodePrefixes;
    }
}

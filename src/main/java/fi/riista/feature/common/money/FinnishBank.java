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
    OP_POHJOLA("OKOYFIHH", "OP-Pohjola", newHashSet("5"));

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

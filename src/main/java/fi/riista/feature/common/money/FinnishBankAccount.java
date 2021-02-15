package fi.riista.feature.common.money;

import org.iban4j.Iban;

import javax.annotation.Nonnull;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

// Constrained to have a valid Finnish BIC and IBAN.
public class FinnishBankAccount extends BankAccount {

    // For annual game management fee
    public static final FinnishBankAccount GAME_MANAGEMENT_FEE_OP_POHJOLA = fromIban(BankAccountNumbers.GAME_MANAGEMENT_FEE_OP_POHJOLA);
    public static final FinnishBankAccount GAME_MANAGEMENT_FEE_NORDEA = fromIban(BankAccountNumbers.GAME_MANAGEMENT_FEE_NORDEA);
    public static final FinnishBankAccount GAME_MANAGEMENT_FEE_DANSKE_BANK = fromIban(BankAccountNumbers.GAME_MANAGEMENT_FEE_DANSKE_BANK);

    // For payment of hunting permit
    public static final FinnishBankAccount PERMIT_DECISION_FEE_NORDEA = fromIban(BankAccountNumbers.PERMIT_DECISION_FEE_NORDEA);
    public static final FinnishBankAccount MOOSELIKE_HARVEST_FEE_OP_POHJOLA = fromIban(BankAccountNumbers.MOOSELIKE_HARVEST_FEE_OP_POHJOLA);
    public static final FinnishBankAccount MOOSELIKE_HARVEST_FEE_DANSKE = fromIban(BankAccountNumbers.MOOSELIKE_HARVEST_FEE_DANSKE);
    public static final FinnishBankAccount MOOSELIKE_HARVEST_FEE_NORDEA = fromIban(BankAccountNumbers.MOOSELIKE_HARVEST_FEE_NORDEA);

    @Nonnull
    public static FinnishBankAccount fromIban(final String iban) {
        return fromIban(Iban.valueOf(requireNonNull(iban)));
    }

    @Nonnull
    public static FinnishBankAccount fromIban(final Iban iban) {
        requireNonNull(iban);

        return Optional
                .ofNullable(FinnishBank.resolveFromIban(iban))
                .map(bank -> new FinnishBankAccount(iban, bank))
                .orElseThrow(() -> new IllegalArgumentException("Could not resolve Finnish bank from IBAN " + iban));
    }

    private FinnishBankAccount(final Iban iban, final FinnishBank bank) {
        super(bank.getBic(), iban, bank.getBankName());
    }
}

package fi.riista.feature.common.money;

import com.google.common.collect.ImmutableMap;
import org.iban4j.Iban;
import org.iban4j.Iban4jException;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class FinnishBankAccountTest {

    private static final Map<String, FinnishBank> VALID_PAIRS = ImmutableMap.of(
            BankAccountNumbers.GAME_MANAGEMENT_FEE_DANSKE_BANK, FinnishBank.DANSKE_BANK,
            BankAccountNumbers.GAME_MANAGEMENT_FEE_NORDEA, FinnishBank.NORDEA,
            BankAccountNumbers.GAME_MANAGEMENT_FEE_OP_POHJOLA, FinnishBank.OP_POHJOLA,
            BankAccountNumbers.MOOSELIKE_HARVEST_FEE_OP_POHJOLA, FinnishBank.OP_POHJOLA,
            BankAccountNumbers.PERMIT_DECISION_FEE_NORDEA, FinnishBank.NORDEA);

    @Test
    public void testFromIban_withKnownBanks() {
        VALID_PAIRS.forEach((iban, expectedBank) -> {
            final FinnishBankAccount bankAccount = FinnishBankAccount.fromIban(iban);

            assertEquals(expectedBank.getBic(), bankAccount.getBic());
            assertEquals(expectedBank.getBankName(), bankAccount.getBankName());
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromIban_withForeignBank() {
        FinnishBankAccount.fromIban(Iban.valueOf("FR7630006000011234567890189"));
    }

    @Test(expected = Iban4jException.class)
    public void testFromIban_withInvalidIban() {
        FinnishBankAccount.fromIban("FI0012345600000000");
    }

    @Test(expected = NullPointerException.class)
    public void testFromIban_nullString() {
        FinnishBankAccount.fromIban((String) null);
    }

    @Test(expected = NullPointerException.class)
    public void testFromIban_nullIbanObject() {
        FinnishBankAccount.fromIban((Iban) null);
    }
}

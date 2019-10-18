package fi.riista.feature.common.money;

import com.google.common.collect.ImmutableMap;
import org.iban4j.Bic;
import org.iban4j.Iban;
import org.junit.Test;

import java.util.Map;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FinnishBankTest {

    private static final Map<Iban, FinnishBank> VALID_PAIRS = ImmutableMap.of(
            Iban.valueOf(BankAccountNumbers.GAME_MANAGEMENT_FEE_DANSKE_BANK), FinnishBank.DANSKE_BANK,
            Iban.valueOf(BankAccountNumbers.GAME_MANAGEMENT_FEE_NORDEA), FinnishBank.NORDEA,
            Iban.valueOf(BankAccountNumbers.GAME_MANAGEMENT_FEE_OP_POHJOLA), FinnishBank.OP_POHJOLA,
            Iban.valueOf(BankAccountNumbers.MOOSELIKE_HARVEST_FEE_OP_POHJOLA), FinnishBank.OP_POHJOLA,
            Iban.valueOf(BankAccountNumbers.PERMIT_DECISION_FEE_NORDEA), FinnishBank.NORDEA);

    private static final Bic BIC_FOR_UNKNOWN_BANK = Bic.valueOf("BANKFIHH");
    private static final Iban IBAN_FOR_UNKNOWN_BANK = Iban.valueOf("FI7747722260000694");

    @Test
    public void testResolveFromBic_forKnownBanks() {
        VALID_PAIRS.values().stream().forEach(bank -> {
            assertEquals(bank, FinnishBank.resolveFromBic(bank.getBic()));
        });
    }

    @Test
    public void testResolveFromBic_forUnknownBic() {
        assertNull(FinnishBank.resolveFromBic(BIC_FOR_UNKNOWN_BANK));
    }

    @Test
    public void testResolveFromBic_withNull() {
        assertNull(FinnishBank.resolveFromBic(null));
    }

    @Test
    public void testResolveFromIban_withIbansOfKnownBanks() {
        VALID_PAIRS.forEach((iban, expectedBank) -> {
            assertEquals(expectedBank, FinnishBank.resolveFromIban(iban));
        });
    }

    @Test
    public void testResolveFromIban_forUnknownBank() {
        assertNull(FinnishBank.resolveFromIban(IBAN_FOR_UNKNOWN_BANK));
    }

    @Test
    public void testResolveFromIban_withNull() {
        assertNull(FinnishBank.resolveFromIban(null));
    }

    @Test
    public void testResolveBic_withIbansOfKnownBanks() {
        VALID_PAIRS.forEach((iban, expectedBank) -> {
            assertEquals(expectedBank.getBic(), FinnishBank.resolveBic(iban));
        });
    }

    @Test
    public void testResolveBic_withIbanOfUnknownBank() {
        assertNull(FinnishBank.resolveBic(IBAN_FOR_UNKNOWN_BANK));
    }

    @Test
    public void testResolveBic_withNull() {
        assertNull(FinnishBank.resolveBic(null));
    }

    @Test
    public void testMatchesIban() {
        VALID_PAIRS.forEach((iban, expectedBank) -> {

            for (final FinnishBank candidateBank : FinnishBank.values()) {

                final boolean isSame = candidateBank == expectedBank;
                final String message =
                        format("IBAN %s should%smatch %s", iban.toFormattedString(), isSame ? " " : " not ", expectedBank);

                if (isSame) {
                    assertTrue(message, candidateBank.matchesIban(iban));
                } else {
                    assertFalse(message, candidateBank.matchesIban(iban));
                }
            }
        });
    }
}

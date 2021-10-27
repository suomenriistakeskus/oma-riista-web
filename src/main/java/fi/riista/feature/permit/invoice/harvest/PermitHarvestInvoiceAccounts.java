package fi.riista.feature.permit.invoice.harvest;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.common.money.FinnishBankAccount;

import java.util.List;

import static fi.riista.feature.common.money.FinnishBankAccount.MOOSELIKE_HARVEST_FEE_DANSKE;
import static fi.riista.feature.common.money.FinnishBankAccount.MOOSELIKE_HARVEST_FEE_NORDEA;
import static fi.riista.feature.common.money.FinnishBankAccount.MOOSELIKE_HARVEST_FEE_OP_POHJOLA;

public class PermitHarvestInvoiceAccounts {

    // The account which should be used where only one account can be selected.
    public static final FinnishBankAccount PRIMARY_HARVEST_FEE_ACCOUNT = MOOSELIKE_HARVEST_FEE_DANSKE;

    // Allowed accounts for receipt, includes old harvest account
    public static List<FinnishBankAccount> HARVEST_FEE_ALLOWED_RECEIPT_ACCOUNTS = ImmutableList.of(
            MOOSELIKE_HARVEST_FEE_OP_POHJOLA,
            MOOSELIKE_HARVEST_FEE_DANSKE,
            MOOSELIKE_HARVEST_FEE_NORDEA
    );

    // Accounts visible on the actual invoice document
    public static List<FinnishBankAccount> HARVEST_FEE_ACCOUNTS = ImmutableList.of(
            MOOSELIKE_HARVEST_FEE_DANSKE,
            MOOSELIKE_HARVEST_FEE_NORDEA
    );

    private PermitHarvestInvoiceAccounts() {
        throw new AssertionError();
    }
}

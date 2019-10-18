package fi.riista.integration.mmm.transfer;

import fi.riista.feature.common.entity.CreditorReference;

import javax.annotation.Nonnull;
import java.util.List;

public interface AccountTransferRepositoryCustom {

    List<AccountTransfer> findAccountTransfersNotAssociatedWithInvoice();

    List<AccountTransfer> findAccountTransfersNotAssociatedWithInvoice(@Nonnull CreditorReference creditorReference);
}

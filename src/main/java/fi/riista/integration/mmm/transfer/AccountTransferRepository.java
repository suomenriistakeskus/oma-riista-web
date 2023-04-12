package fi.riista.integration.mmm.transfer;

import fi.riista.feature.common.repository.BaseRepository;

import java.util.List;

public interface AccountTransferRepository
        extends BaseRepository<AccountTransfer, Long>, AccountTransferRepositoryCustom {

    List<AccountTransfer> findAccountTransfersByBatch(AccountTransferBatch batch);

}

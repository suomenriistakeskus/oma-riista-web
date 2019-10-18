package fi.riista.integration.mmm.transfer;

import fi.riista.feature.common.repository.BaseRepository;

public interface AccountTransferRepository
        extends BaseRepository<AccountTransfer, Long>, AccountTransferRepositoryCustom {

}

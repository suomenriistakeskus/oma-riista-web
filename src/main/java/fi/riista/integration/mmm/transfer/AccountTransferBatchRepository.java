package fi.riista.integration.mmm.transfer;

import fi.riista.feature.common.repository.BaseRepository;
import org.joda.time.LocalDate;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AccountTransferBatchRepository extends BaseRepository<AccountTransferBatch, Long> {

    @Query("SELECT o FROM #{#entityName} o WHERE o.statementDate = ?1")
    Optional<AccountTransferBatch> findByStatementDate(LocalDate date);

    @Query("SELECT o FROM #{#entityName} o WHERE o.filenameDate = ?1")
    Optional<AccountTransferBatch> findByFilenameDate(LocalDate date);

}

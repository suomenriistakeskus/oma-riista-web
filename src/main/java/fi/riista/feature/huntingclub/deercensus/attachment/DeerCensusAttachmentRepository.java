package fi.riista.feature.huntingclub.deercensus.attachment;

import fi.riista.feature.common.repository.BaseRepository;
import org.joda.time.DateTime;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeerCensusAttachmentRepository extends BaseRepository<DeerCensusAttachment, Long> {

    @Modifying
    @Query("DELETE FROM DeerCensusAttachment d WHERE d.deerCensus is null AND d.lifecycleFields.creationTime < :before")
    int deleteAllWithoutDeerCensusByCreationTime(@Param("before") DateTime before);
}

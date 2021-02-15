package fi.riista.feature.harvestpermit;

import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.sql.SQLQueryFactory;
import fi.riista.sql.SQGameSpecies;
import fi.riista.sql.SQPermitDecision;
import fi.riista.sql.SQPermitDecisionSpeciesAmount;
import fi.riista.sql.SQPublicPdfDownload;
import org.joda.time.LocalDateTime;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static fi.riista.util.DateUtil.toLocalDateTimeNullSafe;

@Repository
public class HarvestPermitPublicPdfDownloadRepository {

    @Resource
    private SQLQueryFactory sqlQueryFactory;


    @Transactional(readOnly = true)
    public HarvestPermitPublicPdfDownloadStatisticsDTO getStatistics() {
        final SQPublicPdfDownload DOWNLOAD = SQPublicPdfDownload.publicPdfDownload;
        final SQPermitDecision DECISION = SQPermitDecision.permitDecision;
        final SQPermitDecisionSpeciesAmount SPA =
                SQPermitDecisionSpeciesAmount.permitDecisionSpeciesAmount;
        final SQGameSpecies SPECIES = SQGameSpecies.gameSpecies;

        final LocalDateTime since = toLocalDateTimeNullSafe(sqlQueryFactory.select(DOWNLOAD.downloadTime.min())
                .from(DOWNLOAD)
                .fetchOne());

        final NumberExpression<Long> decisionNumberCount = DOWNLOAD.permitDecisionId.countDistinct();
        final NumberExpression<Long> downloadCount = DOWNLOAD.gid.count();

        final List<HarvestPermitPublicPdfDownloadStatisticsDTO.SpeciesDTO> stats = sqlQueryFactory
                .select(SPECIES.officialCode, decisionNumberCount, downloadCount)
                .from(SPA)
                .join(DECISION).on(DECISION.permitDecisionId.eq(SPA.permitDecisionId))
                .join(DOWNLOAD).on(DOWNLOAD.permitDecisionId.eq(DECISION.permitDecisionId))
                .join(SPECIES).on(SPECIES.gameSpeciesId.eq(SPA.gameSpeciesId))
                .groupBy(SPECIES.officialCode)
                .fetch()
                .stream()
                .map(t -> new HarvestPermitPublicPdfDownloadStatisticsDTO.SpeciesDTO(
                        t.get(SPECIES.officialCode), t.get(decisionNumberCount), t.get(downloadCount)))
                .collect(Collectors.toList());

        return new HarvestPermitPublicPdfDownloadStatisticsDTO(since, stats);
    }

    @Transactional
    public void insertDownload(final long decisionId) {
        final SQPublicPdfDownload DOWNLOAD = SQPublicPdfDownload.publicPdfDownload;

        sqlQueryFactory.insert(DOWNLOAD)
                .columns(DOWNLOAD.permitDecisionId)
                .values(decisionId)
                .execute();
    }
}

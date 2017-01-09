package fi.riista.integration.lupahallinta.support;

import fi.riista.integration.lupahallinta.HarvestPermitImportException;
import fi.riista.integration.lupahallinta.HarvestPermitImportFeature;
import fi.riista.integration.lupahallinta.HarvestPermitImportResultDTO;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;

public class LupahallintaHarvestPermitImporter {

    private static final Logger LOG = LoggerFactory.getLogger(LupahallintaHarvestPermitImporter.class);

    private final HarvestPermitImportFeature harvestPermitImportFeature;
    private final LupahallintaHttpClient lupahallintaHttpClient;
    private final LupahallintaImportMailHandler mailHandler;

    public LupahallintaHarvestPermitImporter(HarvestPermitImportFeature harvestPermitImportFeature,
                                             LupahallintaHttpClient lupahallintaHttpClient,
                                             LupahallintaImportMailHandler mailHandler) {
        this.harvestPermitImportFeature = harvestPermitImportFeature;
        this.lupahallintaHttpClient = lupahallintaHttpClient;
        this.mailHandler = mailHandler;
    }

    public HarvestPermitImportResultDTO doImport() {
        // Really, take start time before http request! Otherwise it's possible to miss permits. Http request might take minutes
        final DateTime startTime = DateTime.now();
        try (final Reader reader = lupahallintaHttpClient.getPermits(getLastLhSyncTimeOrOneDayAgo())) {
            final String requestInfo = startTime.toString() + ":" + this.getClass().getSimpleName();
            final HarvestPermitImportResultDTO result = harvestPermitImportFeature.doImport(reader, requestInfo, startTime);
            harvestPermitImportFeature.updateLastLhSyncTime(startTime);

            LOG.info("Import done, changed or added rows count:{} messages:{}",
                    result.getModifiedOrAddedCount(), result.getMessages());

            if (CollectionUtils.isNotEmpty(result.getMessages())) {
                mailHandler.handleMessages(result.getMessages());
            }

            return result;

        } catch (HarvestPermitImportException e) {
            LOG.error("Problem with import data, error count:{}", e.getAllErrors().size());
            mailHandler.handleError(e);
            return new HarvestPermitImportResultDTO(e.getAllErrors());

        } catch (Exception e) {
            mailHandler.handleError(e);
        }

        return null;
    }

    private DateTime getLastLhSyncTimeOrOneDayAgo() {
        DateTime lastLhSyncTime = harvestPermitImportFeature.getLastLhSyncTime();
        if (lastLhSyncTime == null) {
            return DateTime.now().minusDays(1);
        }
        return lastLhSyncTime;
    }
}

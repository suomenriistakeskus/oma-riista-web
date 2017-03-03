package fi.riista.feature.harvestpermit.area;

import com.google.common.base.Stopwatch;
import com.vividsolutions.jts.geom.Geometry;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.gis.hta.GISHirvitalousalueRepository;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.util.F;
import fi.riista.util.GISUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptySet;

@Component
public class ProcessHarvestPermitAreaZoneFeature {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessHarvestPermitAreaZoneFeature.class);

    private static final int MIN_MAPPING_AREA_SIZE = 10_000; // 1 hectare in square meters

    @Resource
    private HarvestPermitAreaRepository harvestPermitAreaRepository;

    @Resource
    private HarvestPermitAreaPartnerRepository harvestPermitAreaPartnerRepository;

    @Resource
    private HarvestPermitAreaEventRepository harvestPermitAreaEventRepository;

    @Resource
    private HarvestPermitAreaRhyRepository harvestPermitAreaRhyRepository;

    @Resource
    private HarvestPermitAreaHtaRepository harvestPermitAreaHtaRepository;

    @Resource
    private RiistanhoitoyhdistysRepository riistanhoitoyhdistysRepository;

    @Resource
    private GISHirvitalousalueRepository hirvitalousalueRepository;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Resource
    private PlatformTransactionManager transactionManager;

    // No @Transaction here to avoid long-running transactions during processing
    public void startProcessing(final long harvestPermitAreaId) {
        final TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        final Stopwatch stopwatch = Stopwatch.createStarted();

        LOG.info("Starting processing of harvestPermitAreaId={}", harvestPermitAreaId);

        txTemplate.execute(txStatus -> {
            updateStatusProcessing(harvestPermitAreaId);
            return null;
        });

        try {
            final List<Long> zoneIds = txTemplate.execute(
                    txStatus -> harvestPermitAreaPartnerRepository.findAreaPartnerZoneIds(harvestPermitAreaId));

            final List<Geometry> geomList = txTemplate.execute(
                    txStatus -> gisZoneRepository.loadSplicedGeometries(zoneIds));

            LOG.info("Loading of geometries for harvestPermitAreaId={} completed at {}", harvestPermitAreaId, stopwatch);

            final Geometry union = GISUtils.computeUnionFaster(geomList);

            LOG.info("Union calculation for harvestPermitAreaId={} completed at {}", harvestPermitAreaId, stopwatch);

            txTemplate.execute(txStatus -> {
                storeResult(harvestPermitAreaId, union);
                return null;
            });

            LOG.info("Processing for harvestPermitAreaId={} completed at {}", harvestPermitAreaId, stopwatch);

        } catch (Exception ex) {
            LOG.error("Processing failed with exception", ex);

            try {
                txTemplate.execute(txStatus -> {
                    updateStatusProcessingFailed(harvestPermitAreaId);
                    return null;
                });
            } catch (Exception ignored) {
            }
        }
    }

    private void updateStatusProcessingFailed(final long harvestPermitAreaId) {
        final HarvestPermitArea harvestPermitArea = harvestPermitAreaRepository.getOne(harvestPermitAreaId);
        harvestPermitArea.setStatusProcessingFailed().ifPresent(harvestPermitAreaEventRepository::save);
    }

    private void updateStatusProcessing(final long harvestPermitAreaId) {
        final HarvestPermitArea harvestPermitArea = harvestPermitAreaRepository.getOne(harvestPermitAreaId);
        harvestPermitArea.setStatusProcessing().ifPresent(harvestPermitAreaEventRepository::save);

        final GISZone permitAreaZone = harvestPermitArea.getZone();
        permitAreaZone.setGeom(null);
        permitAreaZone.setComputedAreaSize(0);
        permitAreaZone.setWaterAreaSize(0);
    }

    private void storeResult(final long harvestPermitAreaId, final Geometry union) {
        final HarvestPermitArea harvestPermitArea = harvestPermitAreaRepository.getOne(harvestPermitAreaId);
        harvestPermitArea.setStatusReady().ifPresent(harvestPermitAreaEventRepository::save);

        final GISZone permitAreaZone = harvestPermitArea.getZone();
        permitAreaZone.setGeom(union);
        permitAreaZone.setExcludedGeom(null);
        permitAreaZone.setMetsahallitusHirvi(emptySet());

        gisZoneRepository.saveAndFlush(permitAreaZone);

        gisZoneRepository.removeZonePalstaAndFeatures(permitAreaZone);
        gisZoneRepository.calculateAreaSize(permitAreaZone.getId());
        updateRhyMapping(harvestPermitArea, permitAreaZone.getId());
        updateHtaMapping(harvestPermitArea, permitAreaZone.getId());
    }

    private void updateHtaMapping(final HarvestPermitArea harvestPermitArea, final Long zoneId) {
        final Map<String, Double> htaAreaSizeList = gisZoneRepository.calculateHtaAreaSize(zoneId);
        final List<String> htaCodes = F.mapNonNullsToList(htaAreaSizeList.entrySet(), Map.Entry::getKey);
        final List<GISHirvitalousalue> htaList = htaCodes.isEmpty()
                ? Collections.emptyList()
                : hirvitalousalueRepository.findByNumber(htaCodes);
        final Map<String, GISHirvitalousalue> htaMapping = F.index(htaList, GISHirvitalousalue::getNumber);

        harvestPermitAreaHtaRepository.deleteByHarvestPermitArea(harvestPermitArea);

        htaAreaSizeList.forEach((k, v) -> {
            if (v >= MIN_MAPPING_AREA_SIZE) {
                harvestPermitAreaHtaRepository.save(new HarvestPermitAreaHta(harvestPermitArea, htaMapping.get(k), v));
            }
        });
    }

    private void updateRhyMapping(final HarvestPermitArea harvestPermitArea, final Long zoneId) {
        final Map<String, Double> rhyAreaSizeList = gisZoneRepository.calculateRhyAreaSize(zoneId);
        final List<String> rhyCodes = F.mapNonNullsToList(rhyAreaSizeList.entrySet(), Map.Entry::getKey);
        final List<Riistanhoitoyhdistys> rhyList = rhyCodes.isEmpty()
                ? Collections.emptyList()
                : riistanhoitoyhdistysRepository.findByOfficialCode(rhyCodes);
        final Map<String, Riistanhoitoyhdistys> rhyMapping = F.index(rhyList, Riistanhoitoyhdistys::getOfficialCode);

        harvestPermitAreaRhyRepository.deleteByHarvestPermitArea(harvestPermitArea);

        rhyAreaSizeList.forEach((k, v) -> {
            if (v >= MIN_MAPPING_AREA_SIZE) {
                harvestPermitAreaRhyRepository.save(new HarvestPermitAreaRhy(harvestPermitArea, rhyMapping.get(k), v));
            }
        });
    }
}

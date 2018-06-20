package fi.riista.feature.permit.area;

import com.google.common.base.Stopwatch;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.vividsolutions.jts.geom.Geometry;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.gis.hta.GISHirvitalousalueRepository;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneSizeRhyDTO;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationRepository;
import fi.riista.feature.permit.area.hta.HarvestPermitAreaHta;
import fi.riista.feature.permit.area.hta.HarvestPermitAreaHtaRepository;
import fi.riista.feature.permit.area.partner.HarvestPermitAreaPartnerRepository;
import fi.riista.feature.permit.area.rhy.HarvestPermitAreaRhy;
import fi.riista.feature.permit.area.rhy.HarvestPermitAreaRhyRepository;
import fi.riista.util.F;
import fi.riista.util.GISUtils;
import io.sentry.Sentry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;

@Component
public class CalculatePermitAreaUnionFeature {
    private static final Logger LOG = LoggerFactory.getLogger(CalculatePermitAreaUnionFeature.class);

    private static final int MIN_MAPPING_AREA_SIZE = 10_000; // 1 hectare in square meters
    private static final int MINIMUM_EXTERIOR_SIZE = 1_000;
    private static final int MINIMUM_INTERIOR_SIZE = 1_000;

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
    private HarvestPermitApplicationRepository harvestPermitApplicationRepository;

    @Resource
    private RiistanhoitoyhdistysRepository riistanhoitoyhdistysRepository;

    @Resource
    private GISHirvitalousalueRepository hirvitalousalueRepository;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Resource
    private PlatformTransactionManager transactionManager;

    // No @Transaction here to avoid long-running transactions during processing
    @Trace
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

            final Geometry result = GISUtils.filterPolygonsByMinimumAreaSize(union, MINIMUM_EXTERIOR_SIZE, MINIMUM_INTERIOR_SIZE);

            LOG.info("Component filtering for harvestPermitAreaId={} completed at {}", harvestPermitAreaId, stopwatch);

            txTemplate.execute(txStatus -> {
                storeResult(harvestPermitAreaId, result);
                return null;
            });

            LOG.info("Processing for harvestPermitAreaId={} completed at {}", harvestPermitAreaId, stopwatch);

        } catch (Exception ex) {
            LOG.error(String.format("Processing harvestPermitAreaId=%d failed with exception", harvestPermitAreaId), ex);

            NewRelic.noticeError(ex, false);
            Sentry.capture(ex);

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
        permitAreaZone.setStateLandAreaSize(null);
        permitAreaZone.setPrivateLandAreaSize(null);
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

        final List<HarvestPermitAreaRhy> rhyAreaSizeList = calculateRhyAreaSize(harvestPermitArea, permitAreaZone.getId());
        final List<HarvestPermitAreaHta> htaAreaSizeList = calculateHtaAreaSize(harvestPermitArea, permitAreaZone.getId());

        // ML 8 § -> state area size inside free hunting municipality > 1000 ha
        final double freeHuntingStateAreaSize = rhyAreaSizeList.stream()
                .filter(rhy -> rhy.getRhy().isFreeHuntingMunicipality())
                .mapToDouble(HarvestPermitAreaRhy::getStateSize)
                .sum();

        harvestPermitArea.setFreeHunting(freeHuntingStateAreaSize / 10_000 > 1000);

        harvestPermitAreaRhyRepository.deleteByHarvestPermitArea(harvestPermitArea);
        harvestPermitAreaHtaRepository.deleteByHarvestPermitArea(harvestPermitArea);

        harvestPermitAreaRhyRepository.save(rhyAreaSizeList);
        harvestPermitAreaHtaRepository.save(htaAreaSizeList);

        updatePermitApplication(harvestPermitArea, rhyAreaSizeList);
    }

    private List<HarvestPermitAreaHta> calculateHtaAreaSize(final HarvestPermitArea harvestPermitArea, final Long zoneId) {
        final Map<String, Double> htaAreaSizeList = gisZoneRepository.calculateHtaAreaSize(zoneId);
        final Set<String> htaCodes = F.mapNonNullsToSet(htaAreaSizeList.entrySet(), Map.Entry::getKey);
        final List<GISHirvitalousalue> htaList = htaCodes.isEmpty()
                ? Collections.emptyList()
                : hirvitalousalueRepository.findByNumber(htaCodes);
        final Map<String, GISHirvitalousalue> htaMapping = F.index(htaList, GISHirvitalousalue::getNumber);

        return htaCodes.stream().map(htaOfficialCode -> new HarvestPermitAreaHta(harvestPermitArea,
                htaMapping.get(htaOfficialCode), htaAreaSizeList.get(htaOfficialCode)))
                .filter(a -> a.getAreaSize() >= MIN_MAPPING_AREA_SIZE)
                .collect(Collectors.toList());
    }

    private List<HarvestPermitAreaRhy> calculateRhyAreaSize(final HarvestPermitArea harvestPermitArea, final Long zoneId) {
        final List<GISZoneSizeRhyDTO> rhyAreaSizeList = gisZoneRepository.calculateRhyAreaSize(zoneId);
        final Set<String> rhyCodes = F.mapNonNullsToSet(rhyAreaSizeList, GISZoneSizeRhyDTO::getRhyOfficialCode);
        final List<Riistanhoitoyhdistys> rhyList = rhyCodes.isEmpty()
                ? Collections.emptyList()
                : riistanhoitoyhdistysRepository.findByOfficialCode(rhyCodes);
        final Map<String, Riistanhoitoyhdistys> rhyMapping = F.index(rhyList, Riistanhoitoyhdistys::getOfficialCode);

        return rhyAreaSizeList.stream()
                .filter(a -> a.getBothSize().getTotal() >= MIN_MAPPING_AREA_SIZE)
                .map(dto -> new HarvestPermitAreaRhy(harvestPermitArea, rhyMapping.get(dto.getRhyOfficialCode()),
                        dto.getBothSize(), dto.getStateSize(), dto.getPrivateSize()))
                .collect(Collectors.toList());
    }

    private void updatePermitApplication(final HarvestPermitArea permitArea,
                                         final List<HarvestPermitAreaRhy> permitAreaRhyList) {
        final Riistanhoitoyhdistys largestRhy = permitAreaRhyList.stream()
                .max(Comparator.comparingDouble(HarvestPermitAreaRhy::getAreaSize))
                .map(HarvestPermitAreaRhy::getRhy)
                .orElse(null);

        final Set<Riistanhoitoyhdistys> relatedRhys = permitAreaRhyList.stream()
                .map(HarvestPermitAreaRhy::getRhy)
                .filter(rhy -> !rhy.equals(largestRhy))
                .collect(Collectors.toSet());

        final Set<HuntingClub> partnerClubs = harvestPermitAreaPartnerRepository.findPartnerClubs(permitArea);
        final List<HarvestPermitApplication> relatedApplications = harvestPermitApplicationRepository.findByPermitArea(permitArea);

        for (final HarvestPermitApplication application : relatedApplications) {
            application.setRhy(largestRhy);
            application.setRelatedRhys(relatedRhys);
            application.setPermitPartners(partnerClubs);
        }
    }
}

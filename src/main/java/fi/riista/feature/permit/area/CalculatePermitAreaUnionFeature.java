package fi.riista.feature.permit.area;

import com.google.common.base.Stopwatch;
import fi.riista.feature.gis.OnlyStateAreaService;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.gis.hta.GISHirvitalousalueRepository;
import fi.riista.feature.gis.verotuslohko.GISVerotusLohkoDTO;
import fi.riista.feature.gis.verotuslohko.GISVerotusLohkoRepository;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneSizeByOfficialCodeDTO;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationRepository;
import fi.riista.feature.permit.area.hta.HarvestPermitAreaHta;
import fi.riista.feature.permit.area.hta.HarvestPermitAreaHtaRepository;
import fi.riista.feature.permit.area.mml.HarvestPermitAreaMml;
import fi.riista.feature.permit.area.mml.HarvestPermitAreaMmlRepository;
import fi.riista.feature.permit.area.partner.HarvestPermitAreaPartnerRepository;
import fi.riista.feature.permit.area.rhy.HarvestPermitAreaRhy;
import fi.riista.feature.permit.area.rhy.HarvestPermitAreaRhyRepository;
import fi.riista.feature.permit.area.verotuslohko.HarvestPermitAreaVerotusLohko;
import fi.riista.feature.permit.area.verotuslohko.HarvestPermitAreaVerotusLohkoRepository;
import fi.riista.util.F;
import fi.riista.util.GISUtils;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.*;
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
    private HarvestPermitAreaVerotusLohkoRepository harvestPermitAreaVerotusLohkoRepository;

    @Resource
    private HarvestPermitApplicationRepository harvestPermitApplicationRepository;

    @Resource
    private RiistanhoitoyhdistysRepository riistanhoitoyhdistysRepository;

    @Resource
    private GISHirvitalousalueRepository hirvitalousalueRepository;

    @Resource
    private GISVerotusLohkoRepository verotusLohkoRepository;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Resource
    private OnlyStateAreaService onlyStateAreaService;

    @Resource
    private PlatformTransactionManager transactionManager;

    @Resource
    private HarvestPermitAreaMmlRepository harvestPermitAreaMmlRepository;

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

            final Geometry result = GISUtils.filterPolygonsByMinimumAreaSize(union, MINIMUM_EXTERIOR_SIZE, MINIMUM_INTERIOR_SIZE);

            LOG.info("Component filtering for harvestPermitAreaId={} completed at {}", harvestPermitAreaId, stopwatch);

            txTemplate.execute(txStatus -> {
                storeResult(harvestPermitAreaId, result);
                return null;
            });

            LOG.info("Processing for harvestPermitAreaId={} completed at {}", harvestPermitAreaId, stopwatch);


        } catch (Exception ex) {
            LOG.error(String.format("Processing harvestPermitAreaId=%d failed with exception", harvestPermitAreaId), ex);

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
        permitAreaZone.setComputedAreaSize(0);
        permitAreaZone.setWaterAreaSize(0);
        permitAreaZone.setStateLandAreaSize(null);
        permitAreaZone.setPrivateLandAreaSize(null);

        gisZoneRepository.saveAndFlush(permitAreaZone);

        gisZoneRepository.removeZonePalstaAndFeatures(permitAreaZone);

        final List<Long> partnerZoneIds = harvestPermitAreaRepository.findPartnerZoneIds(harvestPermitArea);
        final boolean onlyStateLand = onlyStateAreaService.shouldContainOnlyStateLand(partnerZoneIds);

        gisZoneRepository.calculateAreaSize(permitAreaZone.getId(), onlyStateLand);

        final List<HarvestPermitAreaRhy> rhyAreaSizeList = calculateRhyAreaSize(harvestPermitArea, permitAreaZone.getId());
        final List<HarvestPermitAreaHta> htaAreaSizeList = calculateHtaAreaSize(harvestPermitArea, permitAreaZone.getId());
        final List<HarvestPermitAreaVerotusLohko> verotusLohkoAreaSizeList = calculateVerotusLohkoAreaSize(harvestPermitArea, permitAreaZone.getId());
        final List<HarvestPermitAreaMml> mmls = F.mapNonNullsToList(
                gisZoneRepository.findIntersectingPalsta(permitAreaZone.getId()),
                dto -> new HarvestPermitAreaMml(harvestPermitArea,
                        dto.getKiinteistoTunnus(),
                        dto.getPalstaId(),
                        dto.getName(),
                        dto.getIntersectionArea()));

        if (onlyStateLand) {
            rhyAreaSizeList.forEach(HarvestPermitAreaRhy::movePrivateToStateArea);
            verotusLohkoAreaSizeList.forEach(HarvestPermitAreaVerotusLohko::movePrivateToStateArea);
        }

        // ML 8 § -> state area size inside free hunting municipality > 1000 ha
        final double freeHuntingStateAreaSize = rhyAreaSizeList.stream()
                .filter(rhy -> rhy.getRhy().isFreeHuntingMunicipality())
                .mapToDouble(HarvestPermitAreaRhy::getStateSize)
                .sum();

        harvestPermitArea.setFreeHunting(freeHuntingStateAreaSize / 10_000 > 1000);

        harvestPermitAreaRhyRepository.deleteByHarvestPermitArea(harvestPermitArea);
        harvestPermitAreaHtaRepository.deleteByHarvestPermitArea(harvestPermitArea);
        harvestPermitAreaVerotusLohkoRepository.deleteByHarvestPermitArea(harvestPermitArea);
        harvestPermitAreaMmlRepository.deleteByHarvestPermitArea(harvestPermitArea);

        harvestPermitAreaRhyRepository.saveAll(rhyAreaSizeList);
        harvestPermitAreaHtaRepository.saveAll(htaAreaSizeList);
        harvestPermitAreaVerotusLohkoRepository.saveAll(verotusLohkoAreaSizeList);
        harvestPermitAreaMmlRepository.saveAll(mmls);

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

    private List<HarvestPermitAreaRhy> calculateRhyAreaSize(final HarvestPermitArea harvestPermitArea, final long zoneId) {
        final List<GISZoneSizeByOfficialCodeDTO> rhyAreaSizeList = gisZoneRepository.calculateRhyAreaSize(zoneId);
        final Set<String> rhyCodes = F.mapNonNullsToSet(rhyAreaSizeList, GISZoneSizeByOfficialCodeDTO::getOfficialCode);
        final List<Riistanhoitoyhdistys> rhyList = rhyCodes.isEmpty()
                ? Collections.emptyList()
                : riistanhoitoyhdistysRepository.findByOfficialCode(rhyCodes);
        final Map<String, Riistanhoitoyhdistys> rhyMapping = F.index(rhyList, Riistanhoitoyhdistys::getOfficialCode);

        return rhyAreaSizeList.stream()
                .filter(a -> a.getBothSize().getTotal() >= MIN_MAPPING_AREA_SIZE)
                .map(dto -> new HarvestPermitAreaRhy(harvestPermitArea, rhyMapping.get(dto.getOfficialCode()),
                        dto.getBothSize(), dto.getStateSize(), dto.getPrivateSize()))
                .collect(Collectors.toList());
    }

    private List<HarvestPermitAreaVerotusLohko> calculateVerotusLohkoAreaSize(final HarvestPermitArea harvestPermitArea,
                                                                              final long zoneId) {
        final List<GISZoneSizeByOfficialCodeDTO> verotusLohkoAreaSizeList =
                gisZoneRepository.calculateVerotusLohkoAreaSize(harvestPermitArea.getHuntingYear(), zoneId);
        final Set<String> officialCodes =
                F.mapNonNullsToSet(verotusLohkoAreaSizeList, GISZoneSizeByOfficialCodeDTO::getOfficialCode);
        final List<GISVerotusLohkoDTO> dtoList =
                verotusLohkoRepository.findWithoutGeometry(harvestPermitArea.getHuntingYear(), officialCodes);

        final Map<String, GISVerotusLohkoDTO> dtoMapping = F.index(dtoList, GISVerotusLohkoDTO::getOfficialCode);

        return verotusLohkoAreaSizeList.stream()
                .filter(e -> e.getBothSize().getTotal() >= MIN_MAPPING_AREA_SIZE)
                .map(size -> {
                    final GISVerotusLohkoDTO dto = dtoMapping.get(size.getOfficialCode());

                    return new HarvestPermitAreaVerotusLohko(harvestPermitArea, dto.getOfficialCode(), dto.getName(),
                            size.getBothSize(), size.getStateSize(), size.getPrivateSize());
                })
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

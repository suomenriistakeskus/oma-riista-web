package fi.riista.feature.harvestpermit.area;

import com.querydsl.core.types.dsl.BooleanExpression;
import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.gis.hta.GISHirvitalousalueRepository;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import fi.riista.util.GISUtils;
import fi.riista.util.ListTransformer;
import org.geojson.FeatureCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class HarvestPermitAreaFeature extends AbstractCrudFeature<Long, HarvestPermitArea, HarvestPermitAreaDTO> {

    @Resource
    private SecureRandom secureRandom;

    @Resource
    private HarvestPermitAreaRepository harvestPermitAreaRepository;

    @Resource
    private HarvestPermitAreaPartnerRepository harvestPermitAreaPartnerRepository;

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
    private RequireEntityService requireEntityService;

    @Resource
    private HarvestPermitAreaDTOTransformer harvestPermitAreaDTOTransformer;

    @Override
    protected JpaRepository<HarvestPermitArea, Long> getRepository() {
        return harvestPermitAreaRepository;
    }

    @Override
    protected ListTransformer<HarvestPermitArea, HarvestPermitAreaDTO> dtoTransformer() {
        return harvestPermitAreaDTOTransformer;
    }

    @Transactional(readOnly = true)
    public List<HarvestPermitAreaDTO> listByClub(final long clubId) {
        final HuntingClub huntingClub = requireEntityService.requireHuntingClub(clubId, EntityPermission.READ);
        final BooleanExpression predicate = QHarvestPermitArea.harvestPermitArea.club.eq(huntingClub);

        return harvestPermitAreaDTOTransformer.transform(harvestPermitAreaRepository.findAllAsList(predicate));
    }

    @Override
    protected void updateEntity(final HarvestPermitArea entity, final HarvestPermitAreaDTO dto) {
        if (entity.isNew()) {
            final GISZone zone = gisZoneRepository.save(new GISZone(GISZone.SourceType.LOCAL));
            final HuntingClub club = requireEntityService.requireHuntingClub(dto.getClubId(), EntityPermission.READ);

            entity.generateAndStoreExternalId(secureRandom);
            entity.setHuntingYear(dto.getHuntingYear());
            entity.setClub(club);
            entity.setZone(zone);
        } else {
            entity.assertStatus(HarvestPermitArea.StatusCode.INCOMPLETE);
        }

        entity.setNameFinnish(dto.getNameFinnish());
        entity.setNameSwedish(dto.getNameSwedish());
    }

    @Transactional
    public void addArea(final long harvestPermitAreaId, final long huntingClubAreaId) {
        final HarvestPermitArea harvestPermitArea = requireEntity(harvestPermitAreaId, EntityPermission.UPDATE);
        harvestPermitArea.assertStatus(HarvestPermitArea.StatusCode.INCOMPLETE);

        final HuntingClubArea huntingClubArea =
                requireEntityService.requireHuntingClubArea(huntingClubAreaId, EntityPermission.READ);

        final Optional<HarvestPermitAreaPartner> maybePartner = harvestPermitArea.findPartner(huntingClubArea);

        if (maybePartner.isPresent()) {
            // Update existing
            gisZoneRepository.copyZone(huntingClubArea.getZone(), maybePartner.get().getZone());

        } else {
            final GISZone zoneCopy = gisZoneRepository.copyZone(huntingClubArea.getZone(), new GISZone());

            harvestPermitAreaPartnerRepository.save(
                    new HarvestPermitAreaPartner(harvestPermitArea, huntingClubArea, zoneCopy));
        }
    }

    @Transactional
    public void updateGeometry(final long harvestPermitAreaId) {
        final HarvestPermitArea harvestPermitArea = requireEntity(harvestPermitAreaId, EntityPermission.UPDATE);
        harvestPermitArea.assertStatus(HarvestPermitArea.StatusCode.INCOMPLETE);

        final List<GISZone> zoneList = harvestPermitArea.getPartners().stream()
                .map(HarvestPermitAreaPartner::getZone)
                .collect(Collectors.toList());

        final Long zoneId = harvestPermitArea.getZone().getId();

        gisZoneRepository.mergeZones(zoneList, harvestPermitArea.getZone());
        gisZoneRepository.calculateAreaSize(zoneId);

        updateRhyMapping(harvestPermitArea, zoneId);
        updateHtaMapping(harvestPermitArea, zoneId);
    }

    private void updateHtaMapping(final HarvestPermitArea harvestPermitArea, final Long zoneId) {
        final Map<String, Double> htaAreaSizeList = gisZoneRepository.calculateHtaAreaSize(zoneId);
        final Map<String, GISHirvitalousalue> htaMapping = F.index(hirvitalousalueRepository.findByNumber(
                F.mapNonNullsToList(htaAreaSizeList.entrySet(), Map.Entry::getKey)),
                GISHirvitalousalue::getNumber);
        harvestPermitAreaHtaRepository.deleteByHarvestPermitArea(harvestPermitArea);
        htaAreaSizeList.forEach((k, v) -> {
            final HarvestPermitAreaHta hta = new HarvestPermitAreaHta();
            hta.setHarvestPermitArea(harvestPermitArea);
            hta.setHta(htaMapping.get(k));
            hta.setAreaSize(v);
            harvestPermitAreaHtaRepository.save(hta);
        });
    }

    private void updateRhyMapping(final HarvestPermitArea harvestPermitArea, final Long zoneId) {
        final Map<String, Double> rhyAreaSizeList = gisZoneRepository.calculateRhyAreaSize(zoneId);
        final Map<String, Riistanhoitoyhdistys> rhyMapping = F.index(riistanhoitoyhdistysRepository.findByOfficialCode(
                F.mapNonNullsToList(rhyAreaSizeList.entrySet(), Map.Entry::getKey)),
                Riistanhoitoyhdistys::getOfficialCode);
        harvestPermitAreaRhyRepository.deleteByHarvestPermitArea(harvestPermitArea);
        rhyAreaSizeList.forEach((k, v) -> {
            final HarvestPermitAreaRhy rhy = new HarvestPermitAreaRhy();
            rhy.setHarvestPermitArea(harvestPermitArea);
            rhy.setRhy(rhyMapping.get(k));
            rhy.setAreaSize(v);
            harvestPermitAreaRhyRepository.save(rhy);
        });
    }

    @Transactional(readOnly = true)
    public FeatureCollection getGeometry(final long harvestPermitAreaId) {
        final HarvestPermitArea harvestPermitArea = requireEntity(harvestPermitAreaId, EntityPermission.READ);

        return gisZoneRepository.getCombinedFeatures(Collections.singleton(
                harvestPermitArea.getZone().getId()), GISUtils.SRID.ETRS_TM35FIN, 10);
    }

    @Transactional
    public void setCompleteStatus(final long harvestPermitAreaId, final boolean isComplete) {
        final HarvestPermitArea harvestPermitArea = requireEntity(harvestPermitAreaId, EntityPermission.UPDATE);

        if (isComplete) {
            harvestPermitArea.setStatusReady();
        } else {
            harvestPermitArea.setStatusIncomplete();
        }
    }
}

package fi.riista.feature.harvestpermit.area;

import com.querydsl.core.BooleanBuilder;
import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneWithoutGeometryDTO;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import org.geojson.FeatureCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.security.SecureRandom;
import java.util.List;
import java.util.Set;

@Component
public class HarvestPermitAreaFeature extends AbstractCrudFeature<Long, HarvestPermitArea, HarvestPermitAreaDTO> {

    private static final int SIMPLIFY_AMOUNT = 1;

    @Resource
    private SecureRandom secureRandom;

    @Resource
    private HarvestPermitAreaRepository harvestPermitAreaRepository;

    @Resource
    private HarvestPermitAreaEventRepository harvestPermitAreaEventRepository;

    @Resource
    private HarvestPermitAreaRhyRepository harvestPermitAreaRhyRepository;

    @Resource
    private HarvestPermitAreaHtaRepository harvestPermitAreaHtaRepository;

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
    protected HarvestPermitAreaDTO toDTO(@Nonnull final HarvestPermitArea entity) {
        return harvestPermitAreaDTOTransformer.apply(entity);
    }

    private HuntingClub requireClubWithReadPermission(final long clubId) {
        return requireEntityService.requireHuntingClub(clubId, EntityPermission.READ);
    }

    @Transactional(readOnly = true)
    public List<HarvestPermitAreaDTO> listByClub(final long clubId, final Integer huntingYear) {
        final HuntingClub huntingClub = requireClubWithReadPermission(clubId);

        final BooleanBuilder builder = new BooleanBuilder(QHarvestPermitArea.harvestPermitArea.club.eq(huntingClub));

        if (huntingYear != null) {
            builder.and(QHarvestPermitArea.harvestPermitArea.huntingYear.eq(huntingYear));
        }

        return harvestPermitAreaDTOTransformer.transform(harvestPermitAreaRepository.findAllAsList(builder.getValue()));
    }

    @Override
    protected void updateEntity(final HarvestPermitArea entity, final HarvestPermitAreaDTO dto) {
        if (entity.isNew()) {
            final GISZone zone = gisZoneRepository.save(new GISZone());
            final HuntingClub club = requireClubWithReadPermission(dto.getClubId());
            entity.generateAndStoreExternalId(secureRandom);
            entity.setHuntingYear(dto.getHuntingYear());
            entity.setClub(club);
            entity.setZone(zone);
        } else {
            entity.assertStatus(HarvestPermitArea.StatusCode.INCOMPLETE);
        }

        entity.setNameFinnish(dto.getNameFI());
        entity.setNameSwedish(dto.getNameSV());
    }

    @Transactional(readOnly = true)
    public HarvestPermitArea.StatusCode getStatus(final long harvestPermitAreaId) {
        return harvestPermitAreaRepository.getOne(harvestPermitAreaId).getStatus();
    }

    @Transactional(readOnly = true)
    public FeatureCollection getGeometry(final long harvestPermitAreaId) {
        final HarvestPermitArea harvestPermitArea = requireEntity(harvestPermitAreaId, EntityPermission.READ);

        // Harvest permit area always contains a zone.
        return harvestPermitArea.computeCombinedFeatures(gisZoneRepository, SIMPLIFY_AMOUNT).get();
    }

    @Transactional
    public boolean setReadyForProcessing(final long harvestPermitAreaId) {
        final HarvestPermitArea harvestPermitArea = requireEntity(harvestPermitAreaId, EntityPermission.UPDATE);

        harvestPermitArea.setStatusPending().ifPresent(harvestPermitAreaEventRepository::save);
        return shouldProcessImmediately(harvestPermitArea);
    }

    private boolean shouldProcessImmediately(final HarvestPermitArea harvestPermitArea) {
        return getEstimatedTotalAreaSize(harvestPermitArea) < (50_000 * 10_000);
    }

    private double getEstimatedTotalAreaSize(final HarvestPermitArea harvestPermitArea) {
        final Set<Long> zoneIds = F.getUniqueIdsAfterTransform(
                harvestPermitArea.getPartners(), HarvestPermitAreaPartner::getZone);
        final List<GISZoneWithoutGeometryDTO> sourceZoneDtos = gisZoneRepository.fetchWithoutGeometry(zoneIds);
        return F.sum(sourceZoneDtos, GISZoneWithoutGeometryDTO::getComputedAreaSize);
    }

    @Transactional
    public void setIncomplete(final long harvestPermitAreaId) {
        final HarvestPermitArea harvestPermitArea = requireEntity(harvestPermitAreaId, EntityPermission.UPDATE);

        harvestPermitArea.setStatusIncomplete().ifPresent(harvestPermitAreaEventRepository::save);

        final GISZone zone = harvestPermitArea.getZone();
        zone.setComputedAreaSize(0);
        zone.setWaterAreaSize(0);
        zone.setGeom(null);

        harvestPermitAreaRhyRepository.deleteByHarvestPermitArea(harvestPermitArea);
        harvestPermitAreaHtaRepository.deleteByHarvestPermitArea(harvestPermitArea);
    }

    @Transactional(readOnly = true)
    public List<Integer> listHuntingYears(final long clubId) {
        return harvestPermitAreaRepository.listHuntingYears(requireClubWithReadPermission(clubId));
    }

}

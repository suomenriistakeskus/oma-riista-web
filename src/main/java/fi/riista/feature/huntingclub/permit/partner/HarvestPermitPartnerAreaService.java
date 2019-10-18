package fi.riista.feature.huntingclub.permit.partner;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneWithoutGeometryDTO;
import fi.riista.feature.gis.zone.QGISZone;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.QHuntingClubArea;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.BasicClubHuntingSummary;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.MooseHuntingSummary;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.partner.QHarvestPermitAreaPartner;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.util.F;
import fi.riista.util.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
public class HarvestPermitPartnerAreaService {

    @Resource
    private JPQLQueryFactory queryFactory;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public int getPermitAreaSizeLookupWithFallback(final MooseHuntingSummary summary) {
        return getPermitAreaSizeLookupWithFallback(summary.getHarvestPermit()).apply(summary.getClub());
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public int getPermitAreaSizeLookupWithFallback(final BasicClubHuntingSummary summary) {
        return getPermitAreaSizeLookupWithFallback(summary.getSpeciesAmount().getHarvestPermit()).apply(summary.getClub());
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public int getPermitAreaSizeLookupWithFallback(final HarvestPermit permit, final HuntingClub club) {
        return getPermitAreaSizeLookupWithFallback(permit).apply(club);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Function<HuntingClub, Integer> getPermitAreaSizeLookupWithFallback(final HarvestPermit permit) {
        Objects.requireNonNull(permit, "permit is null");
        final int permitAreaSizeHa = requireNonNull(permit.getPermitAreaSize(), "Permit area size is missing for permitId:" + permit.getId());

        // Partner area size is only available for new moose permits ( > 2018 )
        final Map<Long, List<HarvestPermitPartnerAreaDTO>> partnerAreaSize = getApplicationAreasWithSize(permit);

        return club -> {
            final List<HarvestPermitPartnerAreaDTO> dtoList = partnerAreaSize.get(club.getId());

            if (dtoList == null || dtoList.isEmpty()) {
                // fallback
                return permitAreaSizeHa;
            }

            final double landAreaSize = dtoList.stream()
                    .map(HarvestPermitPartnerAreaDTO::getAreaSize)
                    .filter(Objects::nonNull)
                    .mapToDouble(dto -> dto.getAll().getLand())
                    .sum();

            final int applicationAreaSizeHa = (int) NumberUtils.squareMetersToHectares(landAreaSize);

            // Result could be too large if partner club has multiple overlapping areas
            return applicationAreaSizeHa > 0 && applicationAreaSizeHa <= permitAreaSizeHa
                    ? applicationAreaSizeHa
                    : permitAreaSizeHa;
        };
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Map<Long, List<HarvestPermitPartnerAreaDTO>> getApplicationAreasWithSize(final HarvestPermit permit) {
        return Optional.ofNullable(permit.getPermitDecision())
                .map(PermitDecision::getApplication)
                .map(HarvestPermitApplication::getArea)
                .map(permitArea -> {
                    final List<ApplicationAreaDTO> clubIdAndZoneIdList = getApplicationAreas(permitArea);

                    // fetch zone size
                    final HashSet<Long> zoneIds = F.mapNonNullsToSet(clubIdAndZoneIdList, ApplicationAreaDTO::getZoneId);
                    final Map<Long, GISZoneWithoutGeometryDTO> zoneDTOs = gisZoneRepository.fetchWithoutGeometry(zoneIds);

                    // combine results
                    return clubIdAndZoneIdList.stream()
                            .map(area -> {
                                final GISZoneWithoutGeometryDTO zoneDTO = zoneDTOs.get(area.getZoneId());

                                if (zoneDTO == null || zoneDTO.getSize() == null) {
                                    return null;
                                }

                                return new HarvestPermitPartnerAreaDTO(
                                        area.getClubId(), zoneDTO.getSize(), area.getExternalId());
                            })
                            .filter(Objects::nonNull)
                            .collect(groupingBy(HarvestPermitPartnerAreaDTO::getClubId, toList()));

                })
                .orElseGet(Collections::emptyMap);
    }

    private List<ApplicationAreaDTO> getApplicationAreas(final HarvestPermitArea permitArea) {
        final QHarvestPermitAreaPartner AREA_PARTNER = QHarvestPermitAreaPartner.harvestPermitAreaPartner;
        final QHuntingClubArea CLUB_AREA = QHuntingClubArea.huntingClubArea;
        final QGISZone ZONE = QGISZone.gISZone;

        final NumberPath<Long> clubIdPath = CLUB_AREA.club.id;
        final NumberPath<Long> zoneIdPath = AREA_PARTNER.zone.id;
        final StringPath externalIdPath = CLUB_AREA.externalId;

        // clubId -> zoneId
        return queryFactory
                .from(AREA_PARTNER)
                .join(AREA_PARTNER.sourceArea, CLUB_AREA)
                .join(AREA_PARTNER.zone, ZONE)
                .where(AREA_PARTNER.harvestPermitArea.eq(permitArea))
                .select(Projections.constructor(ApplicationAreaDTO.class, clubIdPath, zoneIdPath, externalIdPath))
                .fetch();
    }

    public static final class ApplicationAreaDTO {
        private final Long clubId;
        private final Long zoneId;
        private final String externalId;

        public ApplicationAreaDTO(final Long clubId, final Long zoneId, final String externalId) {
            this.clubId = clubId;
            this.zoneId = zoneId;
            this.externalId = externalId;
        }

        public Long getClubId() {
            return clubId;
        }

        public Long getZoneId() {
            return zoneId;
        }

        public String getExternalId() {
            return externalId;
        }
    }
}

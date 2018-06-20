package fi.riista.feature.huntingclub.area.mobile;

import com.google.common.collect.Streams;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.gis.zone.AreaEntity;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.gis.zone.GISZoneWithoutGeometryDTO;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.area.HuntingClubAreaRepository;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.HarvestPermitAreaRepository;
import fi.riista.util.Collect;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Component
public class MobileMapFeature {

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private HuntingClubAreaRepository huntingClubAreaRepository;

    @Resource
    private HarvestPermitAreaRepository harvestPermitAreaRepository;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Transactional(readOnly = true)
    public MobileHuntingClubAreaDTO findByExternalId(final String externalId) {
        final Optional<HuntingClubArea> clubAreaOptional = huntingClubAreaRepository.findByExternalId(externalId);

        if (clubAreaOptional.isPresent() && zoneAreaSizePositive(clubAreaOptional.get())) {
            return new MobileHuntingClubAreaDTO(clubAreaOptional.get());
        }

        final Optional<HarvestPermitArea> permitAreaOptional = harvestPermitAreaRepository.findByExternalId(externalId);

        if (permitAreaOptional.isPresent() && zoneAreaSizePositive(permitAreaOptional.get())) {
            return new MobileHuntingClubAreaDTO(permitAreaOptional.get());
        }

        return null;
    }

    @Transactional(readOnly = true)
    public List<MobileHuntingClubAreaDTO> listClubMaps(final Integer requestHuntingYear) {
        final Person person = activeUserService.requireActivePerson();
        final List<Occupation> clubOccupations = occupationRepository.findActiveByPersonAndOrganisationTypes(
                person, Collections.singleton(OrganisationType.CLUB));
        final HashSet<Organisation> clubs = F.mapNonNullsToSet(clubOccupations, Occupation::getOrganisation);
        final int huntingYear = requestHuntingYear != null ? requestHuntingYear : DateUtil.huntingYear();

        final List<HuntingClubArea> clubAreas = new LinkedList<>();
        final List<HarvestPermitArea> permitAreas = new LinkedList<>();

        for (final Organisation org : clubs) {
            final HuntingClub club = (HuntingClub) org;

            clubAreas.addAll(huntingClubAreaRepository.findByClubAndYear(club, huntingYear, true, false));
            permitAreas.addAll(harvestPermitAreaRepository.listByClub(club, huntingYear));
        }

        final Predicate<AreaEntity<Long>> areaSizeFilter =
                filterWithZoneAreaSizePositive(clubAreas, permitAreas);

        return Streams.concat(
                processClubAreas(clubAreas, areaSizeFilter),
                processPermitAreas(permitAreas, areaSizeFilter))
                .filter(a -> StringUtils.hasText(a.getExternalId()))
                .collect(toList());
    }

    private Stream<MobileHuntingClubAreaDTO> processClubAreas(final List<HuntingClubArea> clubAreas,
                                                              final Predicate<AreaEntity<Long>> areaSizeFilter) {
        return clubAreas.stream()
                .filter(a -> a.getZone() != null)
                .filter(a -> StringUtils.hasText(a.getExternalId()))
                .filter(areaSizeFilter)
                .map(MobileHuntingClubAreaDTO::new);
    }

    private Stream<MobileHuntingClubAreaDTO> processPermitAreas(final List<HarvestPermitArea> permitAreas,
                                                                final Predicate<AreaEntity<Long>> areaSizeFilter) {
        return permitAreas.stream()
                .filter(a -> a.getZone() != null)
                .filter(a -> StringUtils.hasText(a.getExternalId()))
                .filter(areaSizeFilter)
                .map(MobileHuntingClubAreaDTO::new);
    }

    private Predicate<AreaEntity<Long>> filterWithZoneAreaSizePositive(final List<HuntingClubArea> clubAreas,
                                                                       final List<HarvestPermitArea> permitAreas) {
        final Map<Long, GISZoneWithoutGeometryDTO> zoneMapping = gisZoneRepository.fetchWithoutGeometry(Stream.concat(
                clubAreas.stream(), permitAreas.stream())
                .map(AreaEntity::getZone).collect(Collect.idSet()));

        return a -> {
            final GISZoneWithoutGeometryDTO zone = zoneMapping.get(F.getId(a.getZone()));
            return zone != null && zone.getSize().hasAreaSizeGreaterThanOneHectare();
        };
    }

    private boolean zoneAreaSizePositive(final AreaEntity<Long> areaEntity) {
        return Optional.ofNullable(areaEntity)
                .map(AreaEntity::getZone)
                .map(BaseEntity::getId)
                .map(gisZoneRepository::getAreaSize)
                .map(GISZoneSizeDTO::hasAreaSizeGreaterThanOneHectare)
                .orElse(false);
    }

}

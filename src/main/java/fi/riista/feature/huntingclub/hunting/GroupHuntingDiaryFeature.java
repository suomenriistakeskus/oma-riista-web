package fi.riista.feature.huntingclub.hunting;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameDiaryEntry;
import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.gamediary.HuntingDiaryEntryDTO;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestAuthorization;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationAuthorization;
import fi.riista.feature.gamediary.observation.ObservationDTO;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.HuntingClubGroupAuthorization;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayService;
import fi.riista.feature.huntingclub.hunting.rejection.RejectClubDiaryEntryDTO;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import fi.riista.util.GISUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class GroupHuntingDiaryFeature {

    @Resource
    private GroupHuntingDiaryService groupHuntingDiaryService;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private GroupHuntingDayService groupHuntingDayService;

    @Resource
    private ClubHuntingStatusService clubHuntingStatusService;

    @Transactional(readOnly = true)
    public List<HarvestDTO> getHarvestsOfGroupMembers(final long huntingClubGroupId) {
        final HuntingClubGroup group = requireGroup(huntingClubGroupId, EntityPermission.READ);

        return groupHuntingDiaryService.getHarvestsOfGroupMembers(group);
    }

    @Transactional(readOnly = true)
    public List<ObservationDTO> getObservationsOfGroupMembers(final long huntingClubGroupId) {
        final HuntingClubGroup group = requireGroup(huntingClubGroupId, EntityPermission.READ);

        return groupHuntingDiaryService.getObservationsOfGroupMembers(group);
    }

    @Transactional(readOnly = true)
    public List<HuntingDiaryEntryDTO> getDiaryOfGroupMembers(final long huntingClubGroupId) {
        final HuntingClubGroup group = requireGroup(huntingClubGroupId, EntityPermission.READ);

        return groupHuntingDiaryService.getDiaryOfGroupMembers(group);
    }

    @Transactional
    public void rejectDiaryEntryFromHuntingGroup(final RejectClubDiaryEntryDTO dto) {
        final HuntingClubGroup group = requireGroup(dto.getGroupId(),
                HuntingClubGroupAuthorization.Permission.LINK_DIARY_ENTRY_TO_HUNTING_DAY);

        groupHuntingDayService.rejectDiaryEntry(requireDiaryEntry(dto), group);
    }

    private GameDiaryEntry requireDiaryEntry(final RejectClubDiaryEntryDTO dto) {
        return dto.getType().supply(
                () -> requireEntityService.requireHarvest(
                        dto.getEntryId(), HarvestAuthorization.Permission.LINK_HARVEST_TO_HUNTING_DAY_OF_GROUP),
                () -> requireEntityService.requireObservation(
                        dto.getEntryId(), ObservationAuthorization.Permission.LINK_OBSERVATION_TO_HUNTING_DAY_OF_GROUP));
    }

    @Transactional(readOnly = true)
    public GroupHuntingAreaDTO groupHuntingArea(final Long groupId) {
        final HuntingClubGroup group = requireGroup(groupId, EntityPermission.READ);

        return Optional.ofNullable(group.getHuntingArea())
                .map(HuntingClubArea::getZone)
                .map(GISZone::getId)
                .map(zoneId -> gisZoneRepository.getBounds(zoneId, GISUtils.SRID.WGS84))
                .map(bounds -> new GroupHuntingAreaDTO(F.getId(group.getHuntingArea()), bounds))
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public GroupHuntingStatusDTO getGroupHuntingStatus(final long groupId) {
        final HuntingClubGroup group = requireGroup(groupId, EntityPermission.READ);

        return clubHuntingStatusService.getGroupStatus(group);
    }

    @Transactional(readOnly = true)
    public Map<GameDiaryEntryType, List<Long>> listRejected(long huntingClubGroupId) {
        final HuntingClubGroup group = requireGroup(huntingClubGroupId, EntityPermission.READ);

        return groupHuntingDayService.listRejected(group);
    }

    @Transactional
    public void editHarvestGeolocation(final long harvestId, final GeoLocation location) {
        final Harvest harvest =
                requireEntityService.requireHarvest(harvestId, HarvestAuthorization.Permission.FIX_GEOLOCATION);
        fixGeolocation(harvest, location);
    }

    @Transactional
    public void editObservationLocation(final long observationId, final GeoLocation location) {
        final Observation observation = requireEntityService.requireObservation(
                observationId, ObservationAuthorization.Permission.FIX_GEOLOCATION);
        fixGeolocation(observation, location);
    }

    private static void fixGeolocation(final GameDiaryEntry entry, final GeoLocation location) {
        entry.setGeoLocation(location);
    }

    private HuntingClubGroup requireGroup(long huntingClubGroupId, Enum<?> permission) {
        return requireEntityService.requireHuntingGroup(huntingClubGroupId, permission);
    }
}

package fi.riista.feature.huntingclub.hunting;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gamediary.GameDiaryEntry;
import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.gamediary.HuntingDiaryEntryDTO;
import fi.riista.feature.gamediary.harvest.HarvestAuthorization;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.observation.ObservationAuthorization;
import fi.riista.feature.gamediary.observation.ObservationDTO;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.HuntingClubGroupAuthorization;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayService;
import fi.riista.feature.huntingclub.hunting.rejection.RejectClubDiaryEntryDTO;
import fi.riista.security.EntityPermission;
import org.geojson.FeatureCollection;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class GroupHuntingDiaryFeature {

    private static final int SIMPLIFY_AMOUNT = 1;

    @Resource
    private GroupHuntingDiaryService groupHuntingDiaryService;

    @Resource
    private GISZoneRepository zoneRepository;

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
        final HuntingClubGroup group = requireGroup(dto.getGroupId(), HuntingClubGroupAuthorization.Permission.LINK_DIARY_ENTRY_TO_HUNTING_DAY);

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
    public FeatureCollection huntingAreaGeoJSON(final long huntingClubGroupId) {
        final HuntingClubGroup group = requireGroup(huntingClubGroupId, EntityPermission.READ);

        return Optional.ofNullable(group.getHuntingArea())
                .flatMap(area -> area.computeCombinedFeatures(zoneRepository, SIMPLIFY_AMOUNT))
                .orElseGet(FeatureCollection::new);
    }

    @Transactional(readOnly = true)
    public Map<GameDiaryEntryType, List<Long>> listRejected(long huntingClubGroupId) {
        final HuntingClubGroup group = requireGroup(huntingClubGroupId, EntityPermission.READ);

        return groupHuntingDayService.listRejected(group);
    }

    @Transactional(readOnly = true)
    public GroupHuntingStatusDTO getGroupHuntingStatus(final long groupId) {
        final HuntingClubGroup group = requireGroup(groupId, EntityPermission.READ);

        return clubHuntingStatusService.getGroupStatus(group);
    }

    private HuntingClubGroup requireGroup(long huntingClubGroupId, Enum<?> permission) {
        return requireEntityService.requireHuntingGroup(huntingClubGroupId, permission);
    }
}

package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesRepository;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.HuntingClubGroupRepository;
import fi.riista.feature.huntingclub.hunting.ClubHuntingStatusService;
import fi.riista.feature.huntingclub.hunting.MobileGroupHuntingAreaDTO;
import fi.riista.feature.huntingclub.hunting.MobileGroupHuntingStatusDTO;
import fi.riista.feature.huntingclub.permit.endofhunting.HuntingFinishingService;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import fi.riista.util.GISUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fi.riista.feature.organization.OrganisationType.CLUBGROUP;
import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.util.F.mapNonNullsToSet;
import static fi.riista.util.jpa.CriteriaUtils.singleQueryFunction;
import static java.util.Collections.singleton;

@Service
public class MobileGroupHuntingFeature {

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private HuntingClubGroupRepository groupRepository;

    @Resource
    private HuntingClubRepository clubRepository;

    @Resource
    private GameSpeciesRepository speciesRepository;

    @Resource
    private HarvestPermitRepository permitRepository;

    @Resource
    private HarvestPermitSpeciesAmountRepository speciesAmountRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ClubHuntingStatusService statusService;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Resource
    private MobileHuntingGroupOccupationDTOTransformer occupationDTOTransformer;

    @Resource
    private HuntingFinishingService huntingFinishingService;

    @Transactional(readOnly = true)
    public MobileGroupHuntingLeaderDTO getHuntingGroups() {
        final Person person = activeUserService.requireActivePerson();

        final List<Occupation> occupations =
                occupationRepository.findActiveByPersonsAndOrganisationType(singleton(person), CLUBGROUP).stream()
                        .filter(o -> o.getOccupationType() == RYHMAN_METSASTYKSENJOHTAJA)
                        .collect(Collectors.toList());

        final List<HuntingClubGroup> allGroups =
                groupRepository.findAllById(mapNonNullsToSet(occupations, o -> o.getOrganisation().getId()));

        final Map<HuntingClubGroup, HarvestPermit> permitsByHuntingGroup = permitRepository.getByHuntingGroup(allGroups);

        final Set<HuntingClubGroup> groups = permitsByHuntingGroup.keySet();

        final Function<HuntingClubGroup, GameSpecies> groupToSpeciesMapping = getGroupToSpeciesMapping(groups);

        final Map<Long, Set<HarvestPermitSpeciesAmount>> amountsByPermitId =
                speciesAmountRepository.findAllByPermitId(permitsByHuntingGroup.values());

        final List<HuntingClub> clubs =
                clubRepository.findAllById(mapNonNullsToSet(groups, g -> g.getParentOrganisation().getId()));

        final ArrayList<MobileHuntingClubDTO> clubDtos =
                F.mapNonNullsToList(clubs, MobileHuntingClubDTO::new);

        final ArrayList<MobileHuntingClubGroupDTO> groupDtos = F.mapNonNullsToList(groups, group -> {
            final GameSpecies species = groupToSpeciesMapping.apply(group);
            final HarvestPermit permit = group.getHarvestPermit();
            return new MobileHuntingClubGroupDTO(
                    group,
                    species,
                    permit.getPermitNumber(),
                    selectSpeciesAmount(species, amountsByPermitId.get(permit.getId())));
        });

        return new MobileGroupHuntingLeaderDTO(clubDtos, groupDtos);
    }

    HarvestPermitSpeciesAmount selectSpeciesAmount(final GameSpecies species, final Set<HarvestPermitSpeciesAmount> amounts) {
        return amounts.stream()
                .filter(spa-> spa.getGameSpecies().equals(species))
                .findFirst()
                .orElseThrow(NotFoundException::new);
    }

    @Transactional(readOnly = true)
    public MobileGroupHuntingAreaDTO groupHuntingArea(final long groupId) {
        final HuntingClubGroup group = requireEntityService.requireHuntingGroup(groupId, EntityPermission.READ);

        final HuntingClubArea huntingArea = group.getHuntingArea();

        return Optional.ofNullable(huntingArea)
                .map(HuntingClubArea::getZone)
                .map(GISZone::getId)
                .map(zoneId -> gisZoneRepository.getBounds(zoneId, GISUtils.SRID.WGS84))
                .map(bounds -> new MobileGroupHuntingAreaDTO(huntingArea.getId(), huntingArea.getExternalId(), bounds))
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<MobileHuntingGroupOccupationDTO> getMembers(final long groupId) {
        final HuntingClubGroup group = requireEntityService.requireHuntingGroup(groupId, EntityPermission.READ);

        return occupationDTOTransformer.transform(occupationRepository.findActiveByOrganisation(group));
    }

    @Transactional(readOnly = true)
    public MobileGroupHuntingStatusDTO getGroupStatus(final long groupId) {
        final HuntingClubGroup group = requireEntityService.requireHuntingGroup(groupId, EntityPermission.READ);
        MobileGroupHuntingStatusDTO dto = MobileGroupHuntingStatusDTO.from(
                statusService.getGroupStatus(group),
                huntingFinishingService.hasPermitPartnerFinishedHunting(group));
        return dto;
    }

    @Nonnull
    private Function<HuntingClubGroup, GameSpecies> getGroupToSpeciesMapping(final Iterable<HuntingClubGroup> groups) {
        return singleQueryFunction(groups, HuntingClubGroup::getSpecies, speciesRepository, true);
    }

}

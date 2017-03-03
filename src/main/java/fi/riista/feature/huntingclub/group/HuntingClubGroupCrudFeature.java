package fi.riista.feature.huntingclub.group;

import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gamediary.GameDiaryService;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.copy.CopyClubGroupService;
import fi.riista.feature.huntingclub.copy.HuntingClubGroupCopyDTO;
import fi.riista.feature.huntingclub.hunting.ClubHuntingFinishedException;
import fi.riista.feature.huntingclub.hunting.ClubHuntingStatusService;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayRepository;
import fi.riista.feature.huntingclub.permit.HuntingClubPermitService;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class HuntingClubGroupCrudFeature extends AbstractCrudFeature<Long, HuntingClubGroup, HuntingClubGroupDTO> {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private GameDiaryService gameDiaryService;

    @Resource
    private ClubHuntingStatusService clubHuntingStatusService;

    @Resource
    private HuntingClubPermitService huntingClubPermitService;

    @Resource
    private CopyClubGroupService copyClubGroupService;

    @Resource
    private HuntingClubGroupRepository huntingClubGroupRepository;

    @Resource
    private HuntingClubRepository huntingClubRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private GroupHuntingDayRepository groupHuntingDayRepository;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private HuntingClubGroupDTOTransformer huntingClubGroupDTOTransformer;

    @Override
    protected JpaRepository<HuntingClubGroup, Long> getRepository() {
        return huntingClubGroupRepository;
    }

    @Override
    protected HuntingClubGroupDTO toDTO(@Nonnull final HuntingClubGroup entity) {
        return huntingClubGroupDTOTransformer.apply(entity);
    }

    @Override
    protected void delete(final HuntingClubGroup group) {
        if (group.isFromMooseDataCard()) {
            throw new CannotModifyMooseDataCardHuntingGroupException("Can not delete");
        }

        if (groupHasHuntingData(group)) {
            throw new CannotDeleteHuntingGroupWithHuntingDataException();
        }

        groupHuntingDayRepository.deleteByHuntingClubGroup(group);
        occupationRepository.deleteByOrganisation(group);
        huntingClubGroupRepository.delete(group);
    }

    private static boolean changeDetectedToFieldsLockedAfterHuntingStarted(
            final HuntingClubGroup group, final HuntingClubGroupDTO dto) {
        final boolean equalHuntingYear = dto.getHuntingYear() == group.getHuntingYear();
        final boolean equalSpeciesCode = Objects.equals(dto.getGameSpeciesCode(),
                group.getSpecies() != null ? group.getSpecies().getOfficialCode() : null);
        final boolean equalHuntingArea = Objects.equals(dto.getHuntingAreaId(),
                F.getId(group.getHuntingArea()));
        final boolean equalPermitNumber = Objects.equals(
                dto.hasPermitNumber() ? dto.getPermit().getPermitNumber() : null,
                group.getHarvestPermit() != null ? group.getHarvestPermit().getPermitNumber() : null);

        return !(equalSpeciesCode && equalHuntingYear && equalHuntingArea && equalPermitNumber);
    }

    @Override
    protected void updateEntity(final HuntingClubGroup group, final HuntingClubGroupDTO dto) {
        if (group.isFromMooseDataCard()) {
            throw new CannotModifyMooseDataCardHuntingGroupException("Can not update");
        }

        if (group.isNew()) {
            final HuntingClub club = huntingClubRepository.getOne(dto.getClubId());
            group.setParentOrganisation(club);

            if (GameSpecies.isMoose(dto.getGameSpeciesCode()) &&
                    huntingClubGroupRepository.isClubUsingMooseDataCardForPermit(club, dto.getHuntingYear())) {
                throw new CannotCreateManagedGroupWhenMooseDataCardGroupExists(
                        club.getOfficialCode(), dto.getHuntingYear());
            }

        } else if (changeDetectedToFieldsLockedAfterHuntingStarted(group, dto)) {
            if (groupHasHuntingData(group)) {
                throw new CannotModifyLockedFieldsForGroupWithHuntingDataException();
            }

            if (huntingClubPermitService.hasClubHuntingFinished(group)) {
                throw new ClubHuntingFinishedException("Requested update of hunting group is not allowed after hunting is finished");
            }
        }

        group.setHuntingYear(dto.getHuntingYear());
        group.setNameFinnish(dto.getNameFI());
        group.setNameSwedish(dto.getNameSV());
        group.setSpecies(gameDiaryService.getGameSpeciesByOfficialCode(dto.getGameSpeciesCode()));
        group.setHuntingArea(getHuntingArea(dto));
        group.updateHarvestPermit(getHarvestPermit(dto));
        group.findNameReservedForMooseDataCardGroups().ifPresent(conflictingName -> {
            throw new HuntingGroupNameIsReservedException(conflictingName);
        });
    }

    private HuntingClubArea getHuntingArea(final HuntingClubGroupDTO dto) {
        return Optional.ofNullable(dto.getHuntingAreaId())
                .map(huntingAreaId -> requireEntityService.requireHuntingClubArea(huntingAreaId, EntityPermission.READ))
                .orElse(null);
    }

    private HarvestPermit getHarvestPermit(final HuntingClubGroupDTO dto) {
        // Attach to new permit
        return dto.hasPermitNumber()
                ? Objects.requireNonNull(harvestPermitRepository.findByPermitNumber(dto.getPermit().getPermitNumber()))
                : null;
    }

    private boolean groupHasHuntingData(final HuntingClubGroup group) {
        // Only moose groups have data stored in hunting days preventing deletion
        return group.getSpecies().isMoose() && groupHuntingDayRepository.groupHasHuntingDays(group) ||
                clubHuntingStatusService.groupHasDiaryEntriesLinkedToHuntingDay(group);
    }

    @Transactional(readOnly = true)
    public List<HuntingClubGroupDTO> listByClub(final long clubId) {
        final HuntingClub huntingClub = requireEntityService.requireHuntingClub(clubId, EntityPermission.READ);
        return huntingClubGroupDTOTransformer.apply(huntingClubGroupRepository.findByParentOrganisation(huntingClub));
    }

    @Transactional(readOnly = true)
    public List<Integer> listHuntingYears(final long clubId) {
        final HuntingClub club = requireEntityService.requireHuntingClub(clubId, EntityPermission.READ);
        return huntingClubGroupRepository.listHuntingYears(club);
    }

    @Transactional
    public HuntingClubGroupDTO copy(final Long originalGroupId, final HuntingClubGroupCopyDTO dto) {
        return toDTO(copyClubGroupService.copy(originalGroupId, dto));
    }
}

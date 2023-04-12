package fi.riista.feature.huntingclub.group;

import com.google.common.base.Preconditions;
import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.common.entity.EntityLifecycleFields;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitLockedByDateService;
import fi.riista.feature.harvestpermit.HarvestPermitNotFoundException;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.copy.CopyClubGroupService;
import fi.riista.feature.huntingclub.copy.HuntingClubGroupCopyDTO;
import fi.riista.feature.huntingclub.hunting.ClubHuntingFinishedException;
import fi.riista.feature.huntingclub.hunting.ClubHuntingStatusService;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayRepository;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImport;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportRepository;
import fi.riista.feature.huntingclub.permit.endofhunting.HuntingFinishingService;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationContactInfoVisibilityDTO;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationService;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.security.EntityPermission;
import fi.riista.util.Collect;
import fi.riista.util.F;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;
import static java.util.stream.Collectors.toList;

@Component
public class HuntingClubGroupCrudFeature extends AbstractCrudFeature<Long, HuntingClubGroup, HuntingClubGroupDTO> {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private ClubHuntingStatusService clubHuntingStatusService;

    @Resource
    private HuntingFinishingService huntingFinishingService;

    @Resource
    private CopyClubGroupService copyClubGroupService;

    @Resource
    private FileStorageService fileStorageService;

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
    private MooseDataCardImportRepository mooseDataCardImportRepository;

    @Resource
    private HuntingClubGroupDTOTransformer huntingClubGroupDTOTransformer;

    @Resource
    private HarvestPermitLockedByDateService harvestPermitLockedByDateService;

    @Resource
    private HuntingClubGroupLeaderEmailService huntingClubGroupLeaderEmailService;

    @Resource
    private UserRepository userRepository;

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
        if (groupHasHuntingData(group)) {
            throw new CannotDeleteHuntingGroupWithHuntingDataException();
        }

        assertPermitNotLocked(group);

        if (group.isFromMooseDataCard()) {
            if (!activeUserService.isModeratorOrAdmin()) {
                throw new CannotModifyMooseDataCardHuntingGroupException("Cannot delete moose data card group as non-moderator");
            }

            final List<MooseDataCardImport> imports = mooseDataCardImportRepository.findByGroupOrderByIdAsc(group);

            if (imports.stream().map(LifecycleEntity::getLifecycleFields).map(EntityLifecycleFields::getDeletionTime).anyMatch(Objects::isNull)) {

                throw new CannotModifyMooseDataCardHuntingGroupException("Cannot delete moose data card group when non-deleted imports exist");
            }

            final List<PersistentFileMetadata> fileMetadataList = imports.stream().flatMap(imp -> Stream.of(imp.getXmlFileMetadata(), imp.getPdfFileMetadata())).collect(toList());

            mooseDataCardImportRepository.deleteByGroup(group);

            fileMetadataList.forEach(fileMetadata -> fileStorageService.remove(fileMetadata.getId()));
        }

        groupHuntingDayRepository.deleteByHuntingClubGroup(group);
        occupationRepository.deleteByOrganisation(group);
        huntingClubGroupRepository.delete(group);
    }

    private static boolean changeDetectedToFieldsLockedAfterHuntingStarted(final HuntingClubGroup group, final HuntingClubGroupDTO dto) {

        final boolean equalHuntingYear = dto.getHuntingYear() == group.getHuntingYear();
        final boolean equalSpeciesCode = Objects.equals(dto.getGameSpeciesCode(),
                group.getSpecies() != null ? group.getSpecies().getOfficialCode() : null);
        final boolean equalHuntingArea = Objects.equals(dto.getHuntingAreaId(), F.getId(group.getHuntingArea()));
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

        assertPermitNotLocked(group);

        final int huntingYear = dto.getHuntingYear();

        if (group.isNew()) {
            final HuntingClub club = huntingClubRepository.getOne(dto.getClubId());
            group.setParentOrganisation(club);

            if (GameSpecies.isMoose(dto.getGameSpeciesCode()) &&
                    huntingClubGroupRepository.isClubUsingMooseDataCardForPermit(club, huntingYear)) {
                throw new CannotCreateManagedGroupWhenMooseDataCardGroupExists(club.getOfficialCode(), huntingYear);
            }

        } else if (changeDetectedToFieldsLockedAfterHuntingStarted(group, dto)) {
            if (groupHasHuntingData(group)) {
                throw new CannotModifyLockedFieldsForGroupWithHuntingDataException();
            }

            if (huntingFinishingService.hasPermitPartnerFinishedHunting(group)) {
                throw new ClubHuntingFinishedException("Requested update of hunting group is not allowed after hunting is finished");
            }
        }

        group.setHuntingYear(huntingYear);
        group.setNameFinnish(dto.getNameFI());
        group.setNameSwedish(dto.getNameSV());
        group.setSpecies(gameSpeciesService.requireByOfficialCode(dto.getGameSpeciesCode()));
        group.setHuntingArea(getHuntingArea(dto));
        group.updateHarvestPermit(getHarvestPermit(dto));
        group.findNameReservedForMooseDataCardGroups().ifPresent(conflictingName -> {
            throw new HuntingGroupNameIsReservedException(conflictingName);
        });
    }

    private void assertPermitNotLocked(final HuntingClubGroup group) {
        if (!activeUserService.isModeratorOrAdmin() && harvestPermitLockedByDateService.isPermitLocked(group)) {
            throw new HuntingGroupWithPermitLockedException();
        }
    }

    private HuntingClubArea getHuntingArea(final HuntingClubGroupDTO dto) {
        return Optional.ofNullable(dto.getHuntingAreaId()).map(huntingAreaId ->
                requireEntityService.requireHuntingClubArea(huntingAreaId, EntityPermission.READ)).orElse(null);
    }

    private HarvestPermit getHarvestPermit(final HuntingClubGroupDTO dto) {
        if (!dto.hasPermitNumber()) {
            return null;
        }

        final String permitNumber = dto.getPermit().getPermitNumber();
        final HarvestPermit harvestPermit = harvestPermitRepository.findByPermitNumber(permitNumber);

        if (harvestPermit == null) {
            throw new HarvestPermitNotFoundException(permitNumber);
        }

        return harvestPermit;
    }

    private boolean groupHasHuntingData(final HuntingClubGroup group) {
        // Only moose groups have data stored in hunting days preventing deletion
        return group.getSpecies().isMoose() && groupHuntingDayRepository.groupHasHuntingDays(group)
                || clubHuntingStatusService.groupHasDiaryEntriesLinkedToHuntingDay(group);
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
    public HuntingClubGroupDTO update(final HuntingClubGroupDTO dto) {
        final HuntingClubGroup entity = requireEntity(dto.getId());

        activeUserService.assertHasPermission(entity, getUpdatePermission(entity, dto));
        final Person activePerson = activeUserService.requireActiveUser().getPerson();

        checkForUpdateConflict(dto, entity);

        // TODO: Figure out if this could be done without overriding update from super class
        final boolean permitModified = dto.getPermit() != null && dto.getPermit().getId() != F.getId(entity.getHarvestPermit());

        updateEntity(entity, dto);

        if (permitModified) {
            unifyUsersPermitVisibilitiesByReference(entity);
        }

        // Must use saveAndFlush() to update returned consistencyVersion == dto.revision
        final HuntingClubGroup persisted = getRepository().saveAndFlush(entity);
        afterUpdate(persisted, dto);

        if (permitModified) {
            sendGroupLeaderNotificationEmails(persisted, persisted.getHarvestPermit());
        }
        return toDTO(persisted);
    }

    private void unifyUsersPermitVisibilitiesByReference(final HuntingClubGroup entity) {
        List<Occupation> activeLeaderOccupations =
                occupationRepository.findActiveByHarvestPermitAndOccupationType(
                        entity.getHarvestPermit(), RYHMAN_METSASTYKSENJOHTAJA);

        Map<Long, List<Occupation>> referenceOccupationsByPersonId = activeLeaderOccupations.stream()
                .filter(o -> o.getOrganisation().getId() != entity.getId())
                .collect(Collect.groupingByIdOf(Occupation::getPerson));

        Map<Long, List<Occupation>> occupationsToUpdateByPersonId = activeLeaderOccupations.stream()
                .filter(o -> o.getOrganisation().getId() == entity.getId())
                .collect(Collect.groupingByIdOf(Occupation::getPerson));

        referenceOccupationsByPersonId.entrySet().forEach(e ->
                e.getValue().stream().findFirst().ifPresent(referenceOccupation ->
                        occupationsToUpdateByPersonId.getOrDefault(e.getKey(), Collections.emptyList())
                                .forEach(occupationToUpdate -> {
                                    occupationToUpdate.setContactInfoShare(referenceOccupation.getContactInfoShare());
                                    occupationToUpdate.setNameVisibility(referenceOccupation.isNameVisibility());
                                    occupationToUpdate.setPhoneNumberVisibility(referenceOccupation.isPhoneNumberVisibility());
                                    occupationToUpdate.setEmailVisibility(referenceOccupation.isEmailVisibility());
                                })));

    }

    @Transactional
    public HuntingClubGroupDTO copy(final Long originalGroupId, final HuntingClubGroupCopyDTO dto) {
        final HuntingClubGroup originalGroup =
                requireEntityService.requireHuntingGroup(originalGroupId, HuntingClubGroupAuthorization.Permission.COPY);
        final HuntingClubArea huntingArea =
                requireEntityService.requireHuntingClubArea(dto.getHuntingAreaId(), EntityPermission.READ);

        Preconditions.checkState(Objects.equals(huntingArea.getHuntingYear(), dto.getHuntingYear()),
                "hunting area year must match with selected year");

        final HuntingClubGroup persisted = copyClubGroupService.copyGroup(originalGroup, huntingArea);
        if (persisted.getHarvestPermit() != null) {
            sendGroupLeaderNotificationEmails(persisted, persisted.getHarvestPermit());
        }
        return toDTO(persisted);
    }

    private void sendGroupLeaderNotificationEmails(final HuntingClubGroup huntingClubGroup,
                                                   final HarvestPermit permit) {
        List<Occupation> occupations =
                occupationRepository.findNotDeletedByOrganisationAndType(huntingClubGroup, RYHMAN_METSASTYKSENJOHTAJA);

        ArrayList<Person> persons = F.mapNonNullsToList(occupations, Occupation::getPerson);
        Map<Long, SystemUser> usersByPerson = userRepository.findActiveByPersonIn(persons);
        occupations.stream()
                .filter(o -> usersByPerson.get(o.getPerson().getId()) != null)
                .forEach(leader ->
                        huntingClubGroupLeaderEmailService.sendGroupLeaderNotificationEmail(
                                leader, permit, huntingClubGroup.getParentOrganisation(), huntingClubGroup));
    }
}

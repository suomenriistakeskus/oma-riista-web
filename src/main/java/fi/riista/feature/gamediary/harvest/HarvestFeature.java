package fi.riista.feature.gamediary.harvest;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.pilot.DeerPilotService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.gamediary.HarvestChangeHistory;
import fi.riista.feature.gamediary.HarvestChangeHistoryRepository;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenService;
import fi.riista.feature.gamediary.image.GameDiaryImageService;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonLookupService;
import fi.riista.security.EntityPermission;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Service
public class HarvestFeature {

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private HarvestChangeHistoryRepository harvestChangeHistoryRepository;

    @Resource
    private GroupHuntingDayRepository huntingDayRepository;

    @Resource
    private HarvestService harvestService;

    @Resource
    private HarvestSpecimenService harvestSpecimenService;

    @Resource
    private GameDiaryImageService gameDiaryImageService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private PersonLookupService personLookupService;

    @Resource
    private DeerPilotService deerPilotService;

    @Resource
    private HarvestDTOTransformer dtoTransformer;

    @Transactional(readOnly = true)
    public HarvestDTO getHarvest(final long id) {
        final Harvest harvest = requireEntityService.requireHarvest(id, EntityPermission.READ);

        // TODO overriding HarvestSpecVersion will be removed when deer pilot 2020 is over.
        final SystemUser activeUser = activeUserService.requireActiveUser();
        final boolean isDeerPilotActive = isDeerPilotActive(activeUser, harvest);
        final HarvestSpecVersion overrideSpecVersion =
                HarvestSpecVersion.CURRENTLY_SUPPORTED.revertIfNotOnDeerPilot(isDeerPilotActive);

        return dtoTransformer.apply(harvest, overrideSpecVersion);
    }

    @Transactional
    public HarvestDTO createHarvest(@Nonnull final HarvestDTO dto) {
        requireNonNull(dto);

        final SystemUser activeUser = activeUserService.requireActiveUser();
        final HarvestSpecVersion specVersion = dto.getHarvestSpecVersion();

        // TODO `overrideSpecVersion` will be removed when deer pilot 2020 is over.
        final boolean isDeerPilotActive = isDeerPilotActive(activeUser, dto);
        final HarvestSpecVersion overrideSpecVersion = specVersion.revertIfNotOnDeerPilot(isDeerPilotActive);

        final Harvest harvest = new Harvest();
        harvest.setFromMobile(false);

        final HarvestChangeHistory historyEvent =
                harvestService.updateMutableFields(harvest, dto, activeUser, true, isDeerPilotActive);

        activeUserService.assertHasPermission(harvest, EntityPermission.CREATE);

        harvestRepository.saveAndFlush(harvest);

        harvestSpecimenService.addSpecimens(harvest, dto.getAmount(), dto.getSpecimens(), overrideSpecVersion);

        dto.getImageIds().forEach(uuid -> gameDiaryImageService.associateGameDiaryEntryWithImage(harvest, uuid));

        if (historyEvent != null) {
            harvestChangeHistoryRepository.save(historyEvent);
        }

        return dtoTransformer.apply(harvest, overrideSpecVersion);
    }

    @Transactional
    public HarvestDTO updateHarvest(@Nonnull final HarvestDTO dto) {
        requireNonNull(dto);

        final Harvest harvest = requireEntityService.requireHarvest(dto.getId(), EntityPermission.UPDATE);
        DtoUtil.assertNoVersionConflict(harvest, dto);

        final HarvestSpecVersion specVersion = dto.getHarvestSpecVersion();
        final SystemUser activeUser = activeUserService.requireActiveUser();

        // TODO `overrideSpecVersion` will be removed when deer pilot 2020 is over.
        final boolean isDeerPilotActive = isDeerPilotActive(activeUser, dto);
        final HarvestSpecVersion overrideSpecVersion = specVersion.revertIfNotOnDeerPilot(isDeerPilotActive);

        final Person currentPerson = activeUser.getPerson();

        final boolean businessFieldsCanBeUpdated =
                harvestService.canBusinessFieldsBeUpdatedFromWeb(currentPerson, harvest);

        final HarvestChangeHistory historyEvent = harvestService
                .updateMutableFields(harvest, dto, activeUser, businessFieldsCanBeUpdated, isDeerPilotActive);

        if (businessFieldsCanBeUpdated) {
            final boolean anyChangesDetected = harvestSpecimenService
                    .setSpecimens(harvest, dto.getAmount(), dto.getSpecimens(), overrideSpecVersion)
                    .apply((specimens, anyChanges) -> anyChanges);

            if (anyChangesDetected) {
                harvest.forceRevisionUpdate();
            }
        }

        if (harvest.isAuthorOrActor(currentPerson)) {
            gameDiaryImageService.updateImages(harvest, dto.getImageIds());
        }

        if (historyEvent != null) {
            harvestChangeHistoryRepository.save(historyEvent);
        }

        return dtoTransformer.apply(harvestRepository.saveAndFlush(harvest), overrideSpecVersion);
    }

    @Transactional
    public void deleteHarvest(final long harvestId) {
        final SystemUser activeUser = activeUserService.requireActiveUser();
        final Harvest harvest = requireEntityService.requireHarvest(harvestId, EntityPermission.DELETE);

        harvestSpecimenService.deleteAllSpecimens(harvest);
        gameDiaryImageService.deleteGameDiaryImages(harvest);
        harvestService.deleteHarvest(harvest, activeUser);
    }

    private boolean isDeerPilotActive(final SystemUser activeUser, final Harvest harvest) {
        return findHuntingGroup(F.getId(harvest.getHuntingDayOfGroup()))
                .map(deerPilotService::isPilotGroup)
                .orElseGet(() -> {
                    if (activeUser.isModeratorOrAdmin()) {
                        return deerPilotService.isPilotUser(harvest.getAuthor());
                    }

                    return deerPilotService.isPilotUser();
                });
    }

    private boolean isDeerPilotActive(final SystemUser activeUser, final HarvestDTO dto) {
        return findHuntingGroup(dto.getHuntingDayId())
                .map(deerPilotService::isPilotGroup)
                .orElseGet(() -> {
                    if (activeUser.isModeratorOrAdmin()) {
                        final Person author = personLookupService
                                .findPerson(dto.getAuthorInfo(), false)
                                .orElseThrow(NotFoundException::new);

                        return deerPilotService.isPilotUser(author);
                    }

                    return deerPilotService.isPilotUser();
                });
    }

    private Optional<HuntingClubGroup> findHuntingGroup(@Nullable final Long huntingDayId) {
        return Optional
                .ofNullable(huntingDayId)
                .map(huntingDayRepository::getOne)
                .map(GroupHuntingDay::getGroup);
    }
}

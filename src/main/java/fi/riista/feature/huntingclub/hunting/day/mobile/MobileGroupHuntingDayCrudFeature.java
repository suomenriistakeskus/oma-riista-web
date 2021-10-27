package fi.riista.feature.huntingclub.hunting.day.mobile;

import com.google.common.base.Preconditions;
import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameDiaryEntry;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationRepository;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.ClubHuntingFinishedException;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayAuthorization;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayRepository;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayService;
import fi.riista.feature.huntingclub.hunting.day.NumberOfHoundsMissingException;
import fi.riista.feature.huntingclub.hunting.rejection.AcceptClubDiaryObservationDTO;
import fi.riista.feature.huntingclub.permit.endofhunting.HuntingFinishingService;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class MobileGroupHuntingDayCrudFeature extends AbstractCrudFeature<Long, GroupHuntingDay, MobileGroupHuntingDayDTO> {

    @Resource
    private GroupHuntingDayService service;

    @Resource
    private GroupHuntingDayRepository huntingDayRepository;

    @Resource
    private MobileGroupHuntingDayDTOTransformer dtoTransformer;

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private ObservationRepository gameObservationRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HuntingFinishingService huntingFinishingService;

    @Resource
    private ActiveUserService activeUserService;

    @Override
    protected JpaRepository<GroupHuntingDay, Long> getRepository() {
        return huntingDayRepository;
    }

    @Override
    protected MobileGroupHuntingDayDTO toDTO(@Nonnull final GroupHuntingDay entity) {
        return dtoTransformer.apply(entity);
    }

    @Override
    protected Enum<?> getCreatePermission(final GroupHuntingDay entity, final MobileGroupHuntingDayDTO dto) {
        return entity.getGroup().isFromMooseDataCard()
                ? GroupHuntingDayAuthorization.Permission.CREATE_WITHIN_MOOSE_DATA_CARD_IMPORT
                : EntityPermission.CREATE;
    }

    @Override
    protected Enum<?> getUpdatePermission(final GroupHuntingDay entity, final MobileGroupHuntingDayDTO dto) {
        return entity.getGroup().isFromMooseDataCard()
                ? GroupHuntingDayAuthorization.Permission.UPDATE_MOOSE_DATA_CARD_ORIGINATED
                : EntityPermission.UPDATE;
    }

    @Override
    protected Enum<?> getDeletePermission(final GroupHuntingDay entity) {
        return entity.getGroup().isFromMooseDataCard()
                ? GroupHuntingDayAuthorization.Permission.DELETE_MOOSE_DATA_CARD_ORIGINATED
                : EntityPermission.DELETE;
    }

    @Override
    protected void updateEntity(final GroupHuntingDay entity, final MobileGroupHuntingDayDTO dto) {
        if (entity.isNew()) {
            entity.setGroup(requireEntityService.requireHuntingGroup(dto.getHuntingGroupId(), EntityPermission.READ));
        } else {
            Preconditions.checkArgument(Objects.equals(dto.getHuntingGroupId(), entity.getGroup().getId()),
                    "groupId cannot be changed");
            Preconditions.checkArgument(Objects.equals(dto.getStartDate(), entity.getStartDate()),
                    "startDate cannot be changed");
        }

        if (huntingFinishingService.hasPermitPartnerFinishedHunting(entity.getGroup())) {
            throw new ClubHuntingFinishedException("Cannot add/update hunting day of group whose hunting is finished");
        }

        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());

        entity.setBreakDurationInMinutes(dto.getBreakDurationInMinutes());
        entity.setHuntingMethod(dto.getHuntingMethod());
        entity.setNumberOfHunters(dto.getNumberOfHunters());

        if (dto.getHuntingMethod() != null && dto.getHuntingMethod().isWithHound()) {
            NumberOfHoundsMissingException.assertNumberOfHoundsRequired(dto.getNumberOfHounds());
            entity.setNumberOfHounds(dto.getNumberOfHounds());
        } else {
            entity.setNumberOfHounds(null);
        }
        entity.setSnowDepth(dto.getSnowDepth());

        entity.setCreatedBySystem(Optional.ofNullable(dto.getCreatedBySystem()).orElse(false));
        final int huntingYear = entity.getGroup().getHuntingYear();
        Preconditions.checkArgument(isDateWithingHuntingYear(huntingYear, entity.getStartDate(), entity.getEndDate()));
    }

    private static boolean isDateWithingHuntingYear(final int huntingYear,
                                                    final LocalDate startDate,
                                                    final LocalDate endDate) {
        final LocalDate begin = DateUtil.huntingYearBeginDate(huntingYear);
        final LocalDate end = DateUtil.huntingYearEndDate(huntingYear);

        return startDate != null && endDate != null &&
                DateUtil.overlapsInclusive(begin, end, startDate) &&
                DateUtil.overlapsInclusive(begin, end, endDate);
    }

    @Override
    protected void delete(final GroupHuntingDay groupHuntingDay) {

        if (huntingFinishingService.hasPermitPartnerFinishedHunting(groupHuntingDay.getGroup())) {
            throw new ClubHuntingFinishedException("Cannot delete hunting day for group whose hunting is finished");
        }

        harvestRepository.findByHuntingDayOfGroup(groupHuntingDay)
                .forEach(GameDiaryEntry::unsetHuntingDayOfGroup);

        gameObservationRepository.findByHuntingDayOfGroup(groupHuntingDay)
                .forEach(GameDiaryEntry::unsetHuntingDayOfGroup);

        super.delete(groupHuntingDay);
    }

    @Transactional(readOnly = true)
    public List<MobileGroupHuntingDayDTO> findByClubGroup(final long huntingClubGroupId) {
        final HuntingClubGroup group =
                requireEntityService.requireHuntingGroup(huntingClubGroupId, EntityPermission.READ);

        return dtoTransformer.transform(service.findByClubGroup(group));
    }

    @Transactional
    public MobileGroupHuntingDayDTO getOrCreate(final long huntingClubGroupId, final LocalDate date) {
        final HuntingClubGroup group =
                requireEntityService.requireHuntingGroup(huntingClubGroupId, EntityPermission.READ);

        if (!activeUserService.isModeratorOrAdmin()) {
            Preconditions.checkArgument(!group.getSpecies().isMoose(), "This method should not be called for moose");
        }

        final boolean dayExists = service.existsGroupHuntingDay(group, date);
        final GroupHuntingDay day = service.findOrCreateGroupHuntingDay(group, date);

        // Checking of day.isNew() does not work here due it's persisted in findOrCreateGroupHuntingDay()
        // thus it's always false.
        if (!dayExists) {
            activeUserService.assertHasPermission(day, getCreatePermission(day, null));
        }
        return toDTO(day);
    }

    @Transactional
    public void acceptClubDiaryObservationToHuntingDay(final AcceptClubDiaryObservationDTO dto) {
        final SystemUser activeUser = activeUserService.requireActiveUser();
        final Observation observation = gameObservationRepository.getOne(dto.getObservationId());
        final Long huntingDayId = getOrCreate(dto.getGroupId(), observation.getPointOfTimeAsLocalDate()).getId();
        service.linkDiaryEntryToHuntingDay(observation, huntingDayId, activeUser.getPerson());

        if (activeUser.isModeratorOrAdmin()) {
            observation.setModeratorOverride(true);
        }
    }
}

package fi.riista.feature.huntingclub.hunting.day;

import com.google.common.base.Preconditions;
import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameDiaryEntry;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationRepository;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.ClubHuntingFinishedException;
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
public class GroupHuntingDayCrudFeature extends AbstractCrudFeature<Long, GroupHuntingDay, GroupHuntingDayDTO> {

    @Resource
    private GroupHuntingDayService service;

    @Resource
    private GroupHuntingDayRepository huntingDayRepository;

    @Resource
    private GroupHuntingDayDTOTransformer dtoTransformer;

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
    protected GroupHuntingDayDTO toDTO(@Nonnull final GroupHuntingDay entity) {
        return dtoTransformer.apply(entity);
    }

    @Override
    protected Enum<?> getCreatePermission(final GroupHuntingDay entity, final GroupHuntingDayDTO dto) {
        return entity.getGroup().isFromMooseDataCard()
                ? GroupHuntingDayAuthorization.Permission.CREATE_WITHIN_MOOSE_DATA_CARD_IMPORT
                : EntityPermission.CREATE;
    }

    @Override
    protected Enum<?> getUpdatePermission(final GroupHuntingDay entity, final GroupHuntingDayDTO dto) {
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
    protected void updateEntity(final GroupHuntingDay entity, final GroupHuntingDayDTO dto) {
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

        Preconditions.checkArgument(isDateWithingHarvestPermitDateRange(entity.getGroup(), entity.getStartDate(), entity.getEndDate()));
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

    /**
     * Checks if given date range (start and end date) is within harvest permit time range
     *
     * @param group     target group object
     * @param startDate date range start date
     * @param endDate   date range end date
     * @return True if date range (start and end date) is found inside any harvest permit time ranges else false.
     * False if permit, startDate or endDate is null.
     */
    private static boolean isDateWithingHarvestPermitDateRange(final HuntingClubGroup group,
                                                               final LocalDate startDate,
                                                               final LocalDate endDate) {

        HarvestPermit permit = group.getHarvestPermit();
        if (permit == null || startDate == null || endDate == null) return false;

        return permit.getSpeciesAmounts().stream()
                .filter(speciesAmount -> group.getSpecies().equals(speciesAmount.getGameSpecies()))
                .map(speciesAmount -> speciesAmountContainsInterval(speciesAmount, startDate, endDate))
                .findFirst().orElse(false);
    }

    private static boolean speciesAmountContainsInterval(final HarvestPermitSpeciesAmount speciesAmount, final LocalDate beginDate,
                                                         final LocalDate endDate) {
        final LocalDate permitBegin = speciesAmount.getBeginDate();
        final LocalDate permitEnd = speciesAmount.getEndDate();
        if (DateUtil.overlapsInclusive(permitBegin, permitEnd, beginDate) &&
                DateUtil.overlapsInclusive(permitBegin, permitEnd, endDate)) {
            return true;
        }

        if (speciesAmount.hasBeginAndEndDate2()) {
            final LocalDate permitBegin2 = speciesAmount.getBeginDate2();
            final LocalDate permitEnd2 = speciesAmount.getEndDate2();
            return DateUtil.overlapsInclusive(permitBegin2, permitEnd2, beginDate) &&
                    DateUtil.overlapsInclusive(permitBegin2, permitEnd2, endDate);
        }
        return false;
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
    public List<GroupHuntingDayDTO> findByClubGroup(final long huntingClubGroupId) {
        final HuntingClubGroup group =
                requireEntityService.requireHuntingGroup(huntingClubGroupId, EntityPermission.READ);

        return dtoTransformer.transform(service.findByClubGroup(group));
    }

    @Transactional
    public GroupHuntingDayDTO getOrCreate(final long huntingClubGroupId, final LocalDate date) {
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

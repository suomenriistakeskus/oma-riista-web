package fi.riista.feature.huntingclub.hunting.day;

import com.google.common.base.Preconditions;
import com.querydsl.core.types.dsl.BooleanExpression;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gamediary.GameDiaryEntry;
import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.ClubHuntingFinishedException;
import fi.riista.feature.huntingclub.hunting.rejection.ObservationRejection;
import fi.riista.feature.huntingclub.hunting.rejection.ObservationRejectionRepository;
import fi.riista.feature.huntingclub.hunting.rejection.HarvestRejection;
import fi.riista.feature.huntingclub.hunting.rejection.HarvestRejectionRepository;
import fi.riista.feature.huntingclub.permit.HuntingClubPermitService;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.LocalDateTime;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

@Service
public class GroupHuntingDayService {

    @Resource
    private GroupHuntingDayRepository huntingDayRepository;

    @Resource
    private GroupHuntingDayDTOTransformer dtoTransformer;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HuntingClubPermitService clubPermitService;

    @Resource
    private HarvestRejectionRepository harvestRejectionRepository;

    @Resource
    private ObservationRejectionRepository observationRejectionRepository;

    @Resource
    private HarvestPermitSpeciesAmountRepository speciesAmountRepository;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void linkDiaryEntryToHuntingDay(final GameDiaryEntry diaryEntry,
                                           final Long groupHuntingDayId,
                                           final Person currentPerson) {

        Objects.requireNonNull(diaryEntry, "diaryEntry is null");
        Objects.requireNonNull(groupHuntingDayId, "groupHuntingDayId is null");

        final boolean sameHuntingDay = groupHuntingDayId.equals(F.getId(diaryEntry.getHuntingDayOfGroup()));

        final Enum<?> permission = sameHuntingDay
                ? EntityPermission.NONE
                : GroupHuntingDayAuthorization.Permission.LINK_DIARY_ENTRY_TO_HUNTING_DAY;

        final GroupHuntingDay huntingDay =
                requireEntityService.requireHuntingGroupHuntingDay(groupHuntingDayId, permission);

        if (!isEntryPointOfTimeWithinHuntingDay(diaryEntry, huntingDay)) {
            throw new PointOfTimeOutsideOfHuntingDayException();
        }
        if (!isEntryPointOfTimeWithinPermittedDates(diaryEntry, huntingDay.getGroup())) {
            throw new PointOfTimeOutsideOfPermittedDatesException();
        }

        final HuntingClubGroup group = huntingDay.getGroup();
        checkClubHuntingNotFinished(group);

        if (!Objects.equals(huntingDay, diaryEntry.getHuntingDayOfGroup())) {
            diaryEntry.updateHuntingDayOfGroup(huntingDay, currentPerson);
        }

        if (!sameHuntingDay && !diaryEntry.isNew()) {
            diaryEntry.getType().consume(diaryEntry,
                    harvest -> harvestRejectionRepository.deleteByGroup(group, harvest),
                    observation -> observationRejectionRepository.deleteByGroup(group, observation));
        }
    }

    private static boolean isEntryPointOfTimeWithinHuntingDay(
            final GameDiaryEntry diaryEntry, final GroupHuntingDay huntingDay) {

        final LocalDateTime start = huntingDay.getStartAsLocalDateTime();
        final LocalDateTime end = huntingDay.getEndAsLocalDateTime();
        final LocalDateTime t = DateUtil.toLocalDateTimeNullSafe(diaryEntry.getPointOfTime());

        return (t.isEqual(start) || t.isAfter(start)) && (t.isEqual(end) || t.isBefore(end));
    }

    private boolean isEntryPointOfTimeWithinPermittedDates(
            final GameDiaryEntry diaryEntry, final HuntingClubGroup group) {

        final HarvestPermitSpeciesAmount hpsa = speciesAmountRepository.getOneByHarvestPermitIdAndSpeciesCode(
                group.getHarvestPermit().getId(),
                group.getSpecies().getOfficialCode());
        return hpsa.containsDate(DateUtil.toLocalDateNullSafe(diaryEntry.getPointOfTime()));
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void rejectDiaryEntry(final GameDiaryEntry diaryEntry, final HuntingClubGroup group) {

        checkClubHuntingNotFinished(group);

        unlink(diaryEntry);

        diaryEntry.getType().consume(diaryEntry,
                harvest -> harvestRejectionRepository.save(new HarvestRejection(group, harvest)),
                observation -> observationRejectionRepository.save(new ObservationRejection(group, observation)));
    }

    private void checkClubHuntingNotFinished(final HuntingClubGroup group) {
        if (clubPermitService.hasClubHuntingFinished(group)) {
            throw new ClubHuntingFinishedException(
                    "Cannot link/reject game diary entry to/from hunting day when club hunting is finished");
        }
    }

    private static void unlink(final GameDiaryEntry diaryEntry) {
        Objects.requireNonNull(diaryEntry);
        Preconditions.checkArgument(!diaryEntry.isNew(), "transient diaryEntry not allowed");

        diaryEntry.unsetHuntingDayOfGroup();
    }

    public Map<GameDiaryEntryType, List<Long>> listRejected(HuntingClubGroup group) {
        Map<GameDiaryEntryType, List<Long>> map = new HashMap<>();
        map.put(GameDiaryEntryType.HARVEST, harvestRejectionRepository.findByGroup(group).stream()
                .map(HarvestRejection::getHarvest)
                .map(Harvest::getId).collect(toList()));
        map.put(GameDiaryEntryType.OBSERVATION, observationRejectionRepository.findByGroup(group).stream()
                .map(ObservationRejection::getObservation)
                .map(Observation::getId).collect(toList()));
        return map;
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<GroupHuntingDayDTO> findByClubGroup(HuntingClubGroup group) {
        final QGroupHuntingDay groupHuntingDay = QGroupHuntingDay.groupHuntingDay;
        BooleanExpression predicate = groupHuntingDay.group.eq(group);
        if (!group.getSpecies().isMoose()) {
            predicate = predicate.and(groupHuntingDay.harvests.isNotEmpty());
        }

        final JpaSort sort = new JpaSort(Sort.Direction.DESC, GroupHuntingDay_.startTime);
        return dtoTransformer.apply(huntingDayRepository.findAllAsList(predicate, sort));
    }
}

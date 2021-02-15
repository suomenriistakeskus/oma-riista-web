package fi.riista.feature.huntingclub.hunting.day;

import com.google.common.base.Preconditions;
import com.querydsl.core.types.dsl.BooleanExpression;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.gamediary.GameDiaryEntry;
import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.HuntingClubGroupAuthorization;
import fi.riista.feature.huntingclub.hunting.ClubHuntingFinishedException;
import fi.riista.feature.huntingclub.hunting.rejection.HarvestRejection;
import fi.riista.feature.huntingclub.hunting.rejection.HarvestRejectionRepository;
import fi.riista.feature.huntingclub.hunting.rejection.ObservationRejection;
import fi.riista.feature.huntingclub.hunting.rejection.ObservationRejectionRepository;
import fi.riista.feature.huntingclub.permit.endofhunting.HuntingFinishingService;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import fi.riista.util.jpa.JpaSpecs;
import org.joda.time.LocalDate;
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
import java.util.Optional;

import static fi.riista.util.Collect.idList;
import static fi.riista.util.jpa.JpaSpecs.equal;

@Service
public class GroupHuntingDayService {

    @Resource
    private GroupHuntingDayRepository huntingDayRepository;

    @Resource
    private GroupHuntingDayDTOTransformer dtoTransformer;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HuntingFinishingService huntingFinishingService;

    @Resource
    private HarvestRejectionRepository harvestRejectionRepository;

    @Resource
    private ObservationRejectionRepository observationRejectionRepository;

    @Resource
    private HarvestPermitSpeciesAmountRepository speciesAmountRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private ObservationRepository observationRepository;

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

        if (!huntingDay.containsInstant(diaryEntry.getPointOfTime())) {
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

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void updateGroupHuntingDayForDeerObservation(final Observation observation) {
        final List<HuntingClubGroup> groups = observationRepository.findGroupCandidatesForDeerObservation(observation);
        if (groups.size() == 1) {
            // Only one hunting group is found, linking can be done
            linkDeerObservationToHuntingDayOfGroup(observation, groups.get(0), observation.getAuthor());
        } else {
            // Hunting group cannot be determined automatically, unlink it if set
            observation.getHuntingClubGroup().ifPresent(group -> unlinkDiaryEntryFromHuntingDay(observation, group));
        }
    }

    private void linkDeerObservationToHuntingDayOfGroup(final Observation deerObservation,
                                                        final HuntingClubGroup group,
                                                        final Person currentPerson) {

        Objects.requireNonNull(deerObservation, "deerObservation is null");
        Objects.requireNonNull(group, "group is null");
        Objects.requireNonNull(currentPerson, "currentPerson is null");

        Preconditions.checkArgument(deerObservation.getObservationCategory().isWithinDeerHunting(),
                                    "observation is not done within deer hunting");

        activeUserService.assertHasPermission(
                group,
                HuntingClubGroupAuthorization.Permission.LINK_AUTOMATICALLY_OBSERVATION_TO_HUNTING_DAY);

        if (!isEntryPointOfTimeWithinPermittedDates(deerObservation, group)) {
            throw new PointOfTimeOutsideOfPermittedDatesException();
        }

        checkClubHuntingNotFinished(group);

        // No linking if observation is rejected
        if (!deerObservation.isNew()
                && observationRejectionRepository.findByGroupAndObservation(group, deerObservation).isPresent()) {
            return;
        }

        final GroupHuntingDay huntingDay = findOrCreateGroupHuntingDay(
                group, deerObservation.getPointOfTimeAsLocalDate());

        // Update observation if hunting day changes
        if (!Objects.equals(huntingDay, deerObservation.getHuntingDayOfGroup())) {
            deerObservation.updateHuntingDayOfGroup(huntingDay, currentPerson);
        }

    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public GroupHuntingDay findOrCreateGroupHuntingDay(final HuntingClubGroup group, final LocalDate date) {
        // Find hunting day for the group on given date
        return findHuntingDay(group, date).orElseGet(() -> {
            // If no hunting day found, make one
            return huntingDayRepository.saveAndFlush(GroupHuntingDay.createAllDayHuntingDayForGroup(date, group));
        });
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean existsGroupHuntingDay(final HuntingClubGroup group, final LocalDate date) {
        return findHuntingDay(group, date).isPresent();
    }

    private Optional<GroupHuntingDay> findHuntingDay(final HuntingClubGroup group, final LocalDate date) {
        return huntingDayRepository.findOne(JpaSpecs.and(
                equal(GroupHuntingDay_.group, group),
                equal(GroupHuntingDay_.startDate, date)
        ));
    }

    private boolean isEntryPointOfTimeWithinPermittedDates(final GameDiaryEntry diaryEntry,
                                                           final HuntingClubGroup group) {

        final HarvestPermitSpeciesAmount hpsa = speciesAmountRepository.getOneByHarvestPermitAndSpeciesCode(
                group.getHarvestPermit(), group.getSpecies().getOfficialCode());

        return hpsa.containsDate(diaryEntry.getPointOfTime().toLocalDate());
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void unlinkDiaryEntryFromHuntingDay(final GameDiaryEntry diaryEntry, final HuntingClubGroup group) {
        checkClubHuntingNotFinished(group);
        unlink(diaryEntry);
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
        if (huntingFinishingService.hasPermitPartnerFinishedHunting(group)) {
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
                .collect(idList()));
        map.put(GameDiaryEntryType.OBSERVATION, observationRejectionRepository.findByGroup(group).stream()
                .map(ObservationRejection::getObservation)
                .collect(idList()));
        return map;
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<GroupHuntingDayDTO> findByClubGroup(HuntingClubGroup group) {
        final QGroupHuntingDay groupHuntingDay = QGroupHuntingDay.groupHuntingDay;
        BooleanExpression predicate = groupHuntingDay.group.eq(group);
        if (!group.getSpecies().isMoose()) {
            // TODO: Show only for deer pilot groups
            if (group.getSpecies().isWhiteTailedDeer()) {
                // Filter hunting days where no harvests nor observations
                predicate = predicate.and(
                        groupHuntingDay.harvests.isNotEmpty().or(groupHuntingDay.observations.isNotEmpty()));
            } else {
                // Filter hunting days where no harvests
                predicate = predicate.and(groupHuntingDay.harvests.isNotEmpty());
            }
        }

        final JpaSort sort = JpaSort.of(Sort.Direction.DESC, GroupHuntingDay_.startTime);
        return dtoTransformer.apply(huntingDayRepository.findAllAsList(predicate, sort));
    }
}

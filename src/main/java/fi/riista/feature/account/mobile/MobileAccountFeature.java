package fi.riista.feature.account.mobile;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.gamediary.GameDiaryEntry;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.mobile.MobileOccupationDTOFactory;
import fi.riista.feature.gamediary.observation.ObservationRepository;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import fi.riista.util.F;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.util.stream.Collectors.toCollection;

public abstract class MobileAccountFeature {
    @Resource
    protected ActiveUserService activeUserService;

    @Resource
    protected MobileOccupationDTOFactory mobileOccupationDTOFactory;

    @Resource
    protected OccupationRepository occupationRepository;

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private ObservationRepository observationRepository;

    protected abstract MobileAccountDTO getMobileAccount();

    protected SortedSet<Integer> getBeginningCalendarYearsOfHuntingYearsContainingHarvests(final Person person) {
        return getBeginningCalendarYearsOfHuntingYears(harvestRepository.findByActualShooter(person));
    }

    protected SortedSet<Integer> getBeginningCalendarYearsOfHuntingYearsContainingObservations(final Person person) {
        return getBeginningCalendarYearsOfHuntingYears(observationRepository.findByObserver(person));
    }

    private static <T extends GameDiaryEntry> SortedSet<Integer> getBeginningCalendarYearsOfHuntingYears(
            final Iterable<T> diaryEntries) {

        return F.stream(diaryEntries)
                .map(GameDiaryEntry::getPointOfTime)
                .map(DateUtil::toLocalDateNullSafe)
                .filter(Objects::nonNull)
                .map(DateUtil::huntingYearContaining)
                .collect(toCollection(TreeSet::new));
    }
}

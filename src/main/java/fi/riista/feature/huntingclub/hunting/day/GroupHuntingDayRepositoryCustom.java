package fi.riista.feature.huntingclub.hunting.day;

import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;

public interface GroupHuntingDayRepositoryCustom {
    void deleteByHuntingClubGroup(HuntingClubGroup group);

    boolean groupHasHuntingDays(HuntingClubGroup group);

    boolean clubHasMooseGroupsWithHuntingDays(HuntingClub club);

    boolean groupHasHarvestLinkedToHuntingDay(HuntingClubGroup group);

    boolean groupHasObservationLinkedToHuntingDay(HuntingClubGroup group);

    boolean clubHasHarvestLinkedToHuntingDay(HuntingClub club);

    boolean clubHasObservationLinkedToHuntingDay(HuntingClub club);
}

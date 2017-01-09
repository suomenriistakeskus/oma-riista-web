package fi.riista.feature.huntingclub.statistics;

import fi.riista.feature.organization.Organisation;
import fi.riista.util.LocalisedString;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface HuntingClubStatisticsQueries {
    List<Organisation> listOrganisations();

    Map<Long, Set<Long>> groupClubByOrganisation();

    Set<Long> findClubsWithAnyGroup();

    Map<Long, Set<Long>> groupByParentFindAllClubWithArea();

    Map<Long, Set<Long>> groupByParentFindAllClubWithPendingInvitation();

    Map<Long, Set<Long>> groupClubsWithMembersOtherThanContactPerson();

    Map<Long, Set<Long>> groupClubWithContactPerson();

    Set<Long> findClubWithGroupHuntingLeader();

    LocalisedString getName();
}

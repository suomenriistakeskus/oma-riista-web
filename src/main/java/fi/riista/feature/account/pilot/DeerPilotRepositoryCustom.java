package fi.riista.feature.account.pilot;

import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.person.Person;

import java.util.Collection;
import java.util.List;

public interface DeerPilotRepositoryCustom {

    boolean isPersonInPilotGroup(final Person person);
    boolean isPersonInPilotGroup(final long personId);

    boolean isPilotGroup(final long groupId);

    List<HuntingClubGroup> filterGroupsInPilot(final Collection<HuntingClubGroup> groups);
}

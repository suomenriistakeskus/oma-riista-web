package fi.riista.feature.huntingclub.statistics;

import fi.riista.feature.organization.OrganisationDTO;
import fi.riista.validation.DoNotValidate;

public class HuntingClubStatisticsRow {
    @DoNotValidate
    private final OrganisationDTO organisation;

    private final int countAll;
    private final int countRegistered;
    private final int countAreaDefined;
    private final int countMemberInvited;
    private final int countGroupCreated;
    private final int countGroupLeaderSelect;

    HuntingClubStatisticsRow(final OrganisationDTO organisation,
                             final int countAll,
                             final int countRegistered,
                             final int countAreaDefined,
                             final int countMemberInvited,
                             final int countGroupCreated,
                             final int countGroupLeaderSelect) {
        this.organisation = organisation;
        this.countAll = countAll;
        this.countRegistered = countRegistered;
        this.countAreaDefined = countAreaDefined;
        this.countMemberInvited = countMemberInvited;
        this.countGroupCreated = countGroupCreated;
        this.countGroupLeaderSelect = countGroupLeaderSelect;
    }

    public OrganisationDTO getOrganisation() {
        return organisation;
    }

    public int getCountAll() {
        return countAll;
    }

    public int getCountRegistered() {
        return countRegistered;
    }

    public int getCountAreaDefined() {
        return countAreaDefined;
    }

    public int getCountMemberInvited() {
        return countMemberInvited;
    }

    public int getCountGroupCreated() {
        return countGroupCreated;
    }

    public int getCountGroupLeaderSelect() {
        return countGroupLeaderSelect;
    }
}

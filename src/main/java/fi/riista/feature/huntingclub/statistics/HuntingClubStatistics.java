package fi.riista.feature.huntingclub.statistics;

import com.google.common.collect.Sets;
import fi.riista.feature.organization.OrganisationDTO;
import fi.riista.feature.organization.Organisation;
import fi.riista.util.F;
import fi.riista.util.LocalisedString;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;

public class HuntingClubStatistics {
    private HuntingClubStatisticsQueries queries;

    public HuntingClubStatistics(final HuntingClubStatisticsQueries queries) {
        this.queries = Objects.requireNonNull(queries);
    }

    public List<HuntingClubStatisticsRow> calculate(final Set<Long> clubsWithPermit) {
        // Seurat ryhmiteltynä yläorganisaation perusteella
        final Map<Long, Set<Long>> organisationToClub = queries.groupClubByOrganisation();

        // Seura haltuunotettu, eli yhdyshenkilö määritelty
        final Map<Long, Set<Long>> organisationToClubWithContactPerson = queries.groupClubWithContactPerson();

        // Ainakin yksi jäsen
        final Map<Long, Set<Long>> organisationToClubWithActiveMember = queries.groupClubsWithMembersOtherThanContactPerson();

        // Metsästyksenjohtaja merkattu
        final Set<Long> clubWithHuntingLeader = queries.findClubWithGroupHuntingLeader();

        // Edes yksi alue määritelty
        final Map<Long, Set<Long>> organisationToClubWithAreaDefined = queries.groupByParentFindAllClubWithArea();

        // Ainakin yksi jäsen kutsuttu
        final Map<Long, Set<Long>> organisationToClubWithInvitationPending = queries.groupByParentFindAllClubWithPendingInvitation();

        // Ainakin yksi ryhmä luotu
        final Set<Long> clubWithAtLeastOneGroup = queries.findClubsWithAnyGroup();

        final List<Organisation> organisations = queries.listOrganisations();

        final List<HuntingClubStatisticsRow> result = organisations.stream().map(org -> {
            final Set<Long> allClubs = organisationToClub.getOrDefault(org.getId(), emptySet());
            final Set<Long> registeredClubs = organisationToClubWithContactPerson.getOrDefault(org.getId(), emptySet());
            final Set<Long> clubWithArea = organisationToClubWithAreaDefined.getOrDefault(org.getId(), emptySet());
            final Set<Long> clubWithPendingInvitation = organisationToClubWithInvitationPending.getOrDefault(org.getId(), emptySet());
            final Set<Long> clubWithActiveMember = organisationToClubWithActiveMember.getOrDefault(org.getId(), emptySet());

            final Set<Long> intersection1 = Sets.intersection(allClubs, clubsWithPermit);
            final Set<Long> intersection2 = Sets.intersection(intersection1, registeredClubs);
            final Set<Long> intersection3 = Sets.intersection(intersection2, clubWithArea);
            final Set<Long> intersection4 = Sets.intersection(intersection3, Sets.union(clubWithPendingInvitation, clubWithActiveMember));
            final Set<Long> intersection5 = Sets.intersection(intersection4, clubWithAtLeastOneGroup);
            final Set<Long> intersection6 = Sets.intersection(intersection5, clubWithHuntingLeader);

            final int countWithPermit = intersection1.size();
            final int countRegistered = intersection2.size() - intersection3.size();
            final int countAreaDefined = intersection3.size() - intersection4.size();
            final int countMemberInvited = intersection4.size() - intersection5.size();
            final int countGroupCreated = intersection5.size() - intersection6.size();
            final int countGroupLeaderSelect = intersection6.size();

            return new HuntingClubStatisticsRow(
                    OrganisationDTO.create(org),
                    countWithPermit,
                    countRegistered,
                    countAreaDefined,
                    countMemberInvited,
                    countGroupCreated,
                    countGroupLeaderSelect);
        }).collect(toList());

        return calculateTotal(result, queries.getName());
    }

    private static List<HuntingClubStatisticsRow> calculateTotal(
            List<HuntingClubStatisticsRow> result, LocalisedString name) {

        final OrganisationDTO organisation = new OrganisationDTO();
        organisation.setNameFI(name.getFinnish());
        organisation.setNameSV(name.getSwedish());

        final int countAll = F.sum(result, HuntingClubStatisticsRow::getCountAll);
        final int countRegistered = F.sum(result, HuntingClubStatisticsRow::getCountRegistered);
        final int countAreaDefined = F.sum(result, HuntingClubStatisticsRow::getCountAreaDefined);
        final int countMemberInvited = F.sum(result, HuntingClubStatisticsRow::getCountMemberInvited);
        final int countGroupCreated = F.sum(result, HuntingClubStatisticsRow::getCountGroupCreated);
        final int countGroupLeaderSelect = F.sum(result, HuntingClubStatisticsRow::getCountGroupLeaderSelect);

        final HuntingClubStatisticsRow total = new HuntingClubStatisticsRow(organisation,
                countAll,
                countRegistered,
                countAreaDefined,
                countMemberInvited,
                countGroupCreated,
                countGroupLeaderSelect);

        result.add(0, total);
        return result;
    }
}

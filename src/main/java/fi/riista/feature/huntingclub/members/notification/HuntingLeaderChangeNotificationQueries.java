package fi.riista.feature.huntingclub.members.notification;

import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.util.DateUtil;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Component
public class HuntingLeaderChangeNotificationQueries {

    private static final Comparator<LeaderEmailDTO> GROUP_INTERNAL_LEADER_ORDERING =
            comparing(LeaderEmailDTO::getOrder, nullsLast(naturalOrder()))
                    .thenComparing(LeaderEmailDTO::getName)
                    .thenComparing(LeaderEmailDTO::getHunterNumber);

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Transactional(readOnly = true)
    public List<LeaderEmailDTO> getGroupLeadersOfCurrentAndFutureHuntingYears(final Collection<Long> clubIds) {
        final QOccupation OCCUPATION = QOccupation.occupation;
        final QHuntingClubGroup GROUP = QHuntingClubGroup.huntingClubGroup;
        final QPerson PERSON = QPerson.person;

        final int currentHuntingYear = DateUtil.huntingYear();

        return jpqlQueryFactory
                .select(PERSON.firstName, PERSON.lastName, PERSON.hunterNumber, OCCUPATION.callOrder, GROUP.id,
                        OCCUPATION.lifecycleFields.modificationTime)
                .from(OCCUPATION)
                .join(OCCUPATION.organisation, GROUP._super)
                .join(OCCUPATION.person, PERSON)
                .where(OCCUPATION.organisation.parentOrganisation.id.in(clubIds),
                        OCCUPATION.occupationType.eq(RYHMAN_METSASTYKSENJOHTAJA),
                        OCCUPATION.validAndNotDeleted(),
                        GROUP.harvestPermit.isNotNull(),
                        GROUP.huntingYear.goe(currentHuntingYear))
                .fetch()
                .stream()
                .map(tuple -> new LeaderEmailDTO(
                        String.format("%s %s", tuple.get(PERSON.firstName), tuple.get(PERSON.lastName)),
                        tuple.get(PERSON.hunterNumber),
                        tuple.get(OCCUPATION.callOrder),
                        tuple.get(GROUP.id),
                        tuple.get(OCCUPATION.lifecycleFields.modificationTime)))
                .sorted(comparing(LeaderEmailDTO::getHuntingGroupId).thenComparing(GROUP_INTERNAL_LEADER_ORDERING))
                .collect(toList());
    }

    @Transactional(readOnly = true)
    public List<GroupEmailDTO> resolveHuntingGroups(final List<LeaderEmailDTO> groupLeaders) {
        final Map<Long, List<LeaderEmailDTO>> groupIdToLeaders =
                groupLeaders.stream().collect(groupingBy(LeaderEmailDTO::getHuntingGroupId));

        final QHuntingClubGroup GROUP = QHuntingClubGroup.huntingClubGroup;
        final QGameSpecies SPECIES = QGameSpecies.gameSpecies;
        final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;

        return jpqlQueryFactory
                .select(GROUP.id, GROUP.nameFinnish, GROUP.nameSwedish, GROUP.huntingYear, GROUP.parentOrganisation.id,
                        PERMIT.rhy.id, PERMIT.permitNumber, SPECIES.nameFinnish, SPECIES.nameSwedish)
                .from(GROUP)
                .join(GROUP.species, SPECIES)
                .join(GROUP.harvestPermit, PERMIT)
                .where(GROUP.id.in(groupIdToLeaders.keySet()))
                .orderBy(GROUP.nameFinnish.asc())
                .fetch()
                .stream()
                .map(tuple -> {
                    final long groupId = tuple.get(GROUP.id);

                    final GroupEmailDTO dto = new GroupEmailDTO();

                    dto.setId(groupId);
                    dto.setClubId(tuple.get(GROUP.parentOrganisation.id));
                    dto.setRhyId(tuple.get(PERMIT.rhy.id));

                    dto.setNameFinnish(tuple.get(GROUP.nameFinnish));
                    dto.setNameSwedish(tuple.get(GROUP.nameSwedish));

                    dto.setHuntingYear(tuple.get(GROUP.huntingYear));
                    dto.setPermitNumber(tuple.get(PERMIT.permitNumber));

                    dto.setSpeciesNameFinnish(tuple.get(SPECIES.nameFinnish));
                    dto.setSpeciesNameSwedish(tuple.get(SPECIES.nameSwedish));

                    final List<LeaderEmailDTO> leaders = groupIdToLeaders.get(groupId)
                            .stream()
                            .sorted(GROUP_INTERNAL_LEADER_ORDERING)
                            .collect(toList());

                    dto.setLeaders(leaders);

                    return dto;
                })
                .collect(toList());
    }
}

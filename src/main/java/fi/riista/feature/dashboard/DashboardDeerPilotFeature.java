package fi.riista.feature.dashboard;

import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.account.pilot.QDeerPilot;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.gamediary.harvest.QHarvest;
import fi.riista.feature.gamediary.observation.QObservation;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.QGroupHuntingDay;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.querydsl.core.group.GroupBy.groupBy;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;
import static java.util.stream.Collectors.toList;

@Service
public class DashboardDeerPilotFeature {

    @Resource
    private RiistanhoitoyhdistysRepository riistanhoitoyhdistysRepository;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public List<DashboardDeerPilotMemberDTO> exportRhyDeerStatistics(final String rhyCode) {
        final DateTime now = DateUtil.now();
        final Riistanhoitoyhdistys rhy = riistanhoitoyhdistysRepository.findByOfficialCode(rhyCode);

        List<Long> groups = findPilotGroups(rhy);
        final Map<Long, Person> membersById = fetchPilotMembers(groups);
        final Map<Long, Integer> observationCountByMember = fetchObservations(groups);
        final Map<Long, Integer> harvestCountByMember = fetchHarvests(groups);

        return membersById.values().stream()
                .map(person -> new DashboardDeerPilotMemberDTO(
                        calculateAge(person, now),
                        observationCountByMember.getOrDefault(person.getId(), 0),
                        harvestCountByMember.getOrDefault(person.getId(), 0)))
                .collect(toList());
    }

    private static int calculateAge(final Person p, final DateTime now) {
        return DateUtil.calculateAge(p.parseDateOfBirth(), now).getYears();
    }

    private Map<Long, Integer> fetchObservations(final List<Long> groups) {
        final QPerson MEMBER = QPerson.person;
        final QHuntingClubGroup GROUP = QHuntingClubGroup.huntingClubGroup;
        final QGameSpecies SPECIES = QGameSpecies.gameSpecies;
        final QObservation OBSERVATION = QObservation.observation;
        final QGroupHuntingDay HUNTING_DAY = QGroupHuntingDay.groupHuntingDay;

        return jpqlQueryFactory
                .from(OBSERVATION)
                .innerJoin(OBSERVATION.author, MEMBER)
                .innerJoin(OBSERVATION.huntingDayOfGroup, HUNTING_DAY)
                .innerJoin(HUNTING_DAY.group, GROUP)
                .innerJoin(OBSERVATION.species, SPECIES)
                .where(GROUP.id.in(groups), SPECIES.officialCode.eq(OFFICIAL_CODE_WHITE_TAILED_DEER))
                .groupBy(OBSERVATION.author.id)
                .transform(groupBy(OBSERVATION.author.id).as(OBSERVATION.count().intValue()));
    }

    private Map<Long, Integer> fetchHarvests(final List<Long> groups) {
        final QPerson MEMBER = QPerson.person;
        final QHuntingClubGroup GROUP = QHuntingClubGroup.huntingClubGroup;
        final QGameSpecies SPECIES = QGameSpecies.gameSpecies;
        final QHarvest HARVEST = QHarvest.harvest;
        final QGroupHuntingDay HUNTING_DAY = QGroupHuntingDay.groupHuntingDay;

        return jpqlQueryFactory
                .from(HARVEST)
                .innerJoin(HARVEST.author, MEMBER)
                .innerJoin(HARVEST.huntingDayOfGroup, HUNTING_DAY)
                .innerJoin(HUNTING_DAY.group, GROUP)
                .innerJoin(HARVEST.species, SPECIES)
                .where(GROUP.id.in(groups), SPECIES.officialCode.eq(OFFICIAL_CODE_WHITE_TAILED_DEER))
                .groupBy(HARVEST.author.id)
                .transform(groupBy(HARVEST.author.id).as(HARVEST.count().intValue()));
    }

    private List<Long> findPilotGroups(final Riistanhoitoyhdistys rhy) {
        final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
        final QDeerPilot PILOT = QDeerPilot.deerPilot;
        final QHuntingClubGroup GROUP = QHuntingClubGroup.huntingClubGroup;

        final List<Long> permitIds = jpqlQueryFactory
                .select(PERMIT.id)
                .from(PILOT)
                .innerJoin(PILOT.harvestPermit, PERMIT)
                .where(PERMIT.rhy.eq(rhy))
                .fetch();

        return jpqlQueryFactory
                .select(GROUP.id)
                .from(GROUP)
                .innerJoin(GROUP.harvestPermit, PERMIT)
                .where(PERMIT.id.in(permitIds))
                .fetch();
    }

    private Map<Long, Person> fetchPilotMembers(final Collection<Long> groupIds) {
        final QPerson MEMBER = QPerson.person;
        final QOccupation OCCUPATION = QOccupation.occupation;

        return jpqlQueryFactory
                .selectFrom(OCCUPATION)
                .innerJoin(OCCUPATION.person, MEMBER)
                .where(OCCUPATION.organisation.id.in(groupIds))
                .transform(groupBy(MEMBER.id).as(MEMBER));
    }
}

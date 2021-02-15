package fi.riista.feature.huntingclub.members;

import com.querydsl.core.group.GroupBy;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.QHuntingClub;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;

@Service
public class HuntingClubContactService {

    @Resource
    private JPQLQueryFactory queryFactory;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Map<Long, List<Person>> getContactPersonsSorted(final Collection<HuntingClub> clubs) {
        final QOccupation OCCUPATION = QOccupation.occupation;
        final QOrganisation CLUB = QOrganisation.organisation;

        if (clubs.isEmpty()) {
            return emptyMap();
        }

        return queryFactory.from(OCCUPATION)
                .join(OCCUPATION.organisation, CLUB)
                .where(CLUB.in(clubs),
                        OCCUPATION.validAndNotDeleted(),
                        OCCUPATION.occupationType.eq(OccupationType.SEURAN_YHDYSHENKILO))
                .select(CLUB.id, OCCUPATION.person)
                .orderBy(OCCUPATION.callOrder.asc().nullsLast())
                .transform(GroupBy.groupBy(CLUB.id).as(GroupBy.list(OCCUPATION.person)));
    }

    @Transactional(readOnly = true)
    public List<Occupation> listClubHuntingLeaders(final HarvestPermit harvestPermit, final GameSpecies species, final int huntingYear) {
        final QOccupation OCCUPATION = QOccupation.occupation;
        final QPerson PERSON = QPerson.person;

        final QHuntingClubGroup OCCUPATION_GROUP = new QHuntingClubGroup("occGroup");
        final QHuntingClub OCCUPATION_CLUB = new QHuntingClub("occClub");

        return queryFactory.selectFrom(OCCUPATION)
                .join(OCCUPATION.person, PERSON).fetchJoin()
                .join(OCCUPATION.organisation, OCCUPATION_GROUP._super).fetchJoin()
                .join(OCCUPATION_GROUP.parentOrganisation, OCCUPATION_CLUB._super).fetchJoin()
                .where(
                        OCCUPATION_GROUP.harvestPermit.eq(harvestPermit),
                        OCCUPATION_GROUP.huntingYear.eq(huntingYear),
                        OCCUPATION_GROUP.species.eq(species),
                        OCCUPATION.occupationType.eq(OccupationType.RYHMAN_METSASTYKSENJOHTAJA),
                        OCCUPATION.validAndNotDeleted())
                .orderBy(
                        OCCUPATION_CLUB.nameFinnish.asc(),
                        OCCUPATION_GROUP.nameFinnish.asc(),
                        OCCUPATION.callOrder.asc().nullsLast())
                .fetch();
    }

    @Transactional(readOnly = true)
    public List<Occupation> listRhyContactPersons(final long rhyId) {
        final QOccupation OCCUAPATION = QOccupation.occupation;
        final QPerson PERSON = QPerson.person;
        final QHuntingClub CLUB = QHuntingClub.huntingClub;
        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;

        return queryFactory.selectFrom(OCCUAPATION)
                .join(OCCUAPATION.person, PERSON).fetchJoin()
                .join(OCCUAPATION.organisation, CLUB._super).fetchJoin()
                .join(CLUB.parentOrganisation, RHY._super)
                .where(CLUB.active.isTrue(),
                        OCCUAPATION.validAndNotDeleted(),
                        OCCUAPATION.occupationType.eq(OccupationType.SEURAN_YHDYSHENKILO),
                        RHY.id.eq(rhyId)
                ).orderBy(OCCUAPATION.organisation.nameFinnish.asc(), OCCUAPATION.callOrder.asc().nullsLast())
                .fetch();
    }

    @Transactional(readOnly = true)
    public List<Occupation> listRhyHuntingLeaders(final long rhyId, final int huntingYear) {
        final QOccupation occupation = QOccupation.occupation;
        final QPerson person = QPerson.person;
        final QHuntingClubGroup group = QHuntingClubGroup.huntingClubGroup;
        final QHuntingClub club = QHuntingClub.huntingClub;
        final QHarvestPermit permit = QHarvestPermit.harvestPermit;

        return queryFactory
                .selectFrom(occupation)
                .join(occupation.person, person).fetchJoin()
                .join(occupation.organisation, group._super).fetchJoin()
                .join(group.parentOrganisation, club._super).fetchJoin()
                .join(group.harvestPermit, permit)
                .where(occupation.validAndNotDeleted())
                .where(occupation.occupationType.eq(OccupationType.RYHMAN_METSASTYKSENJOHTAJA))
                .where(group.fromMooseDataCard.isFalse())
                .where(group.huntingYear.eq(huntingYear))
                .where(permit.rhy.id.eq(rhyId))
                .orderBy(club.nameFinnish.asc(), group.nameFinnish.asc(), occupation.callOrder.asc().nullsLast())
                .fetch();
    }
}

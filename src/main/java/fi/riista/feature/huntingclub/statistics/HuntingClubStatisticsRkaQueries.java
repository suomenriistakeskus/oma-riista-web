package fi.riista.feature.huntingclub.statistics;

import com.google.common.collect.ImmutableSet;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.huntingclub.QHuntingClub;
import fi.riista.feature.huntingclub.area.QHuntingClubArea;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.huntingclub.members.invitation.QHuntingClubMemberInvitation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.util.LocalisedString;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.set;

public class HuntingClubStatisticsRkaQueries implements HuntingClubStatisticsQueries {
    private final JPQLQueryFactory queryFactory;

    public HuntingClubStatisticsRkaQueries(final JPQLQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    // N * RKA
    @Override
    public List<Organisation> listOrganisations() {
        final QOrganisation organisation = QOrganisation.organisation;

        return queryFactory.selectFrom(organisation)
                .where(organisation.organisationType.eq(OrganisationType.RKA))
                .orderBy(organisation.officialCode.asc())
                .fetch();
    }

    // RHY -> N * CLUB
    @Override
    public Map<Long, Set<Long>> groupClubByOrganisation() {
        final QOrganisation club = new QOrganisation("club");
        final QOrganisation rhy = new QOrganisation("rhy");
        final QOrganisation rka = new QOrganisation("rka");

        return queryFactory.from(club)
                .join(club.parentOrganisation, rhy)
                .join(rhy.parentOrganisation, rka)
                .select(rka.id, club.id)
                .transform(groupBy(rka.id).as(set(club.id)));
    }

    // N * CLUB
    @Override
    public Set<Long> findClubsWithAnyGroup() {
        final QHuntingClubGroup group = QHuntingClubGroup.huntingClubGroup;

        return ImmutableSet.copyOf(queryFactory.from(group)
                .where(group.fromMooseDataCard.isNull().or(group.fromMooseDataCard.isFalse()))
                .select(group.parentOrganisation.id)
                .groupBy(group.parentOrganisation.id)
                .fetch());
    }

    // RKA -> N * CLUB
    @Override
    public Map<Long, Set<Long>> groupByParentFindAllClubWithArea() {
        final QHuntingClubArea area = QHuntingClubArea.huntingClubArea;
        final QHuntingClub club = new QHuntingClub("club");
        final QOrganisation rhy = new QOrganisation("rhy");
        final QOrganisation rka = new QOrganisation("rka");

        return queryFactory.from(area)
                .join(area.club, club)
                .join(club.parentOrganisation, rhy)
                .join(rhy.parentOrganisation, rka)
                .where(area.active.isTrue())
                .select(rka.id, club.id)
                .groupBy(rka.id, club.id)
                .transform(groupBy(rka.id).as(set(club.id)));
    }

    // RKA -> N * CLUB
    @Override
    public Map<Long, Set<Long>> groupByParentFindAllClubWithPendingInvitation() {
        final QHuntingClubMemberInvitation invitation = QHuntingClubMemberInvitation.huntingClubMemberInvitation;
        final QHuntingClub club = new QHuntingClub("club");
        final QOrganisation rhy = new QOrganisation("rhy");
        final QOrganisation rka = new QOrganisation("rka");

        final BooleanExpression isMemberInvitation = invitation.occupationType.eq(OccupationType.SEURAN_JASEN);
        final BooleanExpression notRejected = invitation.userRejectedTime.isNull();

        return queryFactory.from(invitation)
                .join(invitation.huntingClub, club)
                .join(club.parentOrganisation, rhy)
                .join(rhy.parentOrganisation, rka)
                .where(isMemberInvitation.and(notRejected))
                .select(rka.id, club.id)
                .groupBy(rka.id, club.id)
                .transform(groupBy(rka.id).as(set(club.id)));
    }

    // RKA -> N * CLUB
    @Override
    public Map<Long, Set<Long>> groupClubsWithMembersOtherThanContactPerson() {
        final QOccupation occupation = QOccupation.occupation;
        final QOrganisation club = new QOrganisation("club");
        final QOrganisation rhy = new QOrganisation("rhy");
        final QOrganisation rka = new QOrganisation("rka");

        final BooleanExpression isClubMember = occupation.occupationType.eq(OccupationType.SEURAN_JASEN);
        final BooleanExpression isContactPerson = occupation.occupationType.eq(OccupationType.SEURAN_YHDYSHENKILO);

        final JPQLQuery<Person> clubContactPersons = JPAExpressions.selectFrom(occupation)
                .where(occupation.validAndNotDeleted().and(isContactPerson))
                .select(occupation.person);

        return queryFactory.from(occupation)
                .join(occupation.organisation, club)
                .join(club.parentOrganisation, rhy)
                .join(rhy.parentOrganisation, rka)
                .where(occupation.validAndNotDeleted()
                        .and(isClubMember)
                        .and(occupation.person.notIn(clubContactPersons)))
                .select(rka.id, club.id)
                .groupBy(rka.id, club.id)
                .transform(groupBy(rka.id).as(set(club.id)));
    }

    // RHY -> N * CLUB
    @Override
    public Map<Long, Set<Long>> groupClubWithContactPerson() {
        final QOccupation occupation = QOccupation.occupation;
        final QOrganisation club = new QOrganisation("club");
        final QOrganisation rhy = new QOrganisation("rhy");
        final QOrganisation rka = new QOrganisation("rka");

        final BooleanExpression isClubMember = occupation.occupationType.eq(OccupationType.SEURAN_YHDYSHENKILO);

        return queryFactory.from(occupation)
                .join(occupation.organisation, club)
                .join(club.parentOrganisation, rhy)
                .join(rhy.parentOrganisation, rka)
                .where(occupation.validAndNotDeleted().and(isClubMember))
                .select(rka.id, club.id)
                .groupBy(rka.id, club.id)
                .transform(groupBy(rka.id).as(set(club.id)));
    }

    @Override
    public Set<Long> findClubWithGroupHuntingLeader() {
        final QOrganisation group = QOrganisation.organisation;
        final QOccupation occupation = QOccupation.occupation;

        final BooleanExpression isHuntingLeader = occupation.occupationType.eq(OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        return ImmutableSet.copyOf(queryFactory.from(occupation)
                .join(occupation.organisation, group)
                .where(occupation.validAndNotDeleted().and(isHuntingLeader))
                .select(group.parentOrganisation.id)
                .groupBy(group.parentOrganisation.id)
                .fetch());
    }

    @Override
    public LocalisedString getName() {
        return LocalisedString.of("Suomi", "Finland");
    }
}

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
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.QRiistakeskuksenAlue;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.LocalisedString;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.set;

public class HuntingClubStatisticsRhyQueries implements HuntingClubStatisticsQueries {
    private final JPQLQueryFactory queryFactory;
    private final long rkaId;

    public HuntingClubStatisticsRhyQueries(final JPQLQueryFactory queryFactory,
                                           final long rkaId) {
        this.queryFactory = queryFactory;
        this.rkaId = rkaId;
    }

    // N * RHY
    @Override
    public List<Organisation> listOrganisations() {
        final QOrganisation rhy = new QOrganisation("rhy");
        final QOrganisation rka = new QOrganisation("rka");

        return queryFactory.selectFrom(rhy)
                .join(rhy.parentOrganisation, rka)
                .where(rka.id.eq(rkaId).and(rhy.organisationType.eq(OrganisationType.RHY)))
                .orderBy(rhy.officialCode.asc())
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
                .where(rka.id.eq(rkaId))
                .select(rhy.id, club.id)
                .transform(groupBy(rhy.id).as(set(club.id)));
    }

    // N * CLUB
    @Override
    public Set<Long> findClubsWithAnyGroup() {
        final QHuntingClubGroup group = QHuntingClubGroup.huntingClubGroup;
        final QOrganisation club = new QOrganisation("club");
        final QOrganisation rhy = new QOrganisation("rhy");
        final QOrganisation rka = new QOrganisation("rka");

        return ImmutableSet.copyOf(queryFactory.from(group)
                .join(group.parentOrganisation, club)
                .join(club.parentOrganisation, rhy)
                .join(rhy.parentOrganisation, rka)
                .where(rka.id.eq(rkaId).and(group.fromMooseDataCard.isNull().or(group.fromMooseDataCard.isFalse())))
                .select(group.parentOrganisation.id)
                .groupBy(group.parentOrganisation.id)
                .fetch());
    }

    // RHY -> N * CLUB
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
                .where(rka.id.eq(rkaId).and(area.active.isTrue()))
                .select(rhy.id, club.id)
                .groupBy(rhy.id, club.id)
                .transform(groupBy(rhy.id).as(set(club.id)));
    }

    // RHY -> N * CLUB
    @Override
    public Map<Long, Set<Long>> groupByParentFindAllClubWithPendingInvitation() {
        final QHuntingClubMemberInvitation invitation = QHuntingClubMemberInvitation.huntingClubMemberInvitation;
        final QHuntingClub club = new QHuntingClub("club");
        final QOrganisation rhy = new QOrganisation("rhy");
        final QOrganisation rka = new QOrganisation("rka");

        final BooleanExpression rkaMatches = rka.id.eq(rkaId);
        final BooleanExpression isMemberInvitation = invitation.occupationType.eq(OccupationType.SEURAN_JASEN);
        final BooleanExpression notRejected = invitation.userRejectedTime.isNull();

        return queryFactory.from(invitation)
                .join(invitation.huntingClub, club)
                .join(club.parentOrganisation, rhy)
                .join(rhy.parentOrganisation, rka)
                .where(rkaMatches.and(isMemberInvitation).and(notRejected))
                .select(rhy.id, club.id)
                .groupBy(rhy.id, club.id)
                .transform(groupBy(rhy.id).as(set(club.id)));
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
                .where(rka.id.eq(rkaId)
                        .and(occupation.validAndNotDeleted())
                        .and(isClubMember)
                        .and(occupation.person.notIn(clubContactPersons)))
                .select(rhy.id, club.id)
                .groupBy(rhy.id, club.id)
                .transform(groupBy(rhy.id).as(set(club.id)));
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
                .where(rka.id.eq(rkaId).and(occupation.validAndNotDeleted()).and(isClubMember))
                .select(rhy.id, club.id)
                .groupBy(rhy.id, club.id)
                .transform(groupBy(rhy.id).as(set(club.id)));
    }

    @Override
    public Set<Long> findClubWithGroupHuntingLeader() {
        final QOrganisation rhy = new QOrganisation("rhy");
        final QOrganisation rka = new QOrganisation("rka");
        final QOrganisation club = new QOrganisation("club");
        final QOrganisation group = new QOrganisation("clubGroup");
        final QOccupation occupation = QOccupation.occupation;

        final BooleanExpression isHuntingLeader = occupation.occupationType.eq(OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        return ImmutableSet.copyOf(queryFactory.from(occupation)
                .join(occupation.organisation, group)
                .join(group.parentOrganisation, club)
                .join(club.parentOrganisation, rhy)
                .join(rhy.parentOrganisation, rka)
                .where(rka.id.eq(rkaId).and(occupation.validAndNotDeleted()).and(isHuntingLeader))
                .select(group.parentOrganisation.id)
                .groupBy(group.parentOrganisation.id)
                .fetch());
    }

    @Override
    public LocalisedString getName() {
        final QRiistakeskuksenAlue rka = QRiistakeskuksenAlue.riistakeskuksenAlue;

        return queryFactory.from(rka)
                .where(rka.id.eq(rkaId))
                .select(rka.nameLocalisation())
                .fetchOne();
    }
}

package fi.riista.feature.huntingclub.members.invitation;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.QPerson;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

public class HuntingClubMemberInvitationRepositoryImpl implements HuntingClubMemberInvitationRepositoryCustom {

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Override
    @Transactional
    public void deleteInvitationsForDeceased() {
        final QHuntingClubMemberInvitation invitation = QHuntingClubMemberInvitation.huntingClubMemberInvitation;
        final QPerson person = QPerson.person;

        jpqlQueryFactory.delete(invitation)
                .where(invitation.person.in(JPAExpressions.selectFrom(person)
                        .where(person.deletionCode.eq(Person.DeletionCode.D))))
                .execute();
    }
}

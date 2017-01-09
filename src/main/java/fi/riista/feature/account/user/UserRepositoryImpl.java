package fi.riista.feature.account.user;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.QPerson;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Repository
public class UserRepositoryImpl implements UserRepositoryCustom {
    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Override
    @Transactional
    public void deactivateAccountsForDeceased() {
        final QSystemUser systemUser = QSystemUser.systemUser;
        final QPerson person = QPerson.person;

        jpqlQueryFactory.update(systemUser)
                .where(systemUser.active.isTrue()
                        .and(systemUser.role.eq(SystemUser.Role.ROLE_USER))
                        .and(systemUser.person.isNotNull())
                        .and(systemUser.person.in(JPAExpressions.selectFrom(person)
                                .where(person.deletionCode.eq(Person.DeletionCode.D)))))
                .set(systemUser.active, false)
                .execute();
    }
}

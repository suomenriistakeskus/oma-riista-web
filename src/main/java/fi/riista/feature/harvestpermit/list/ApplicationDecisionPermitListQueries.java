package fi.riista.feature.harvestpermit.list;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermitContactPerson;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.QHarvestPermitApplication;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.QPermitDecision;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;

// These queries should never return amendment permits, applications or decisions.
@Repository
public class ApplicationDecisionPermitListQueries {
    private static final QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;
    private static final QPermitDecision DECISION = QPermitDecision.permitDecision;
    private static final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
    private static final QHarvestPermitContactPerson PERMIT_CONTACT = QHarvestPermitContactPerson.harvestPermitContactPerson;

    private static BooleanExpression applicationPredicate(final @Nonnull Person person) {
        return APPLICATION.contactPerson.eq(person)
                .and(APPLICATION.status.ne(HarvestPermitApplication.Status.HIDDEN))
                .and(APPLICATION.harvestPermitCategory.ne(HarvestPermitCategory.MOOSELIKE_NEW));
    }

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Transactional(readOnly = true)
    public List<HarvestPermitApplication> findApplicationsByContactPerson(final @Nonnull Person person) {
        return jpqlQueryFactory.selectFrom(APPLICATION).where(applicationPredicate(person))
                .orderBy(APPLICATION.id.desc()).fetch();
    }

    @Transactional(readOnly = true)
    public List<PermitDecision> findDecisionsByApplicationContactPerson(final @Nonnull Person person) {
        return jpqlQueryFactory.selectFrom(DECISION)
                .join(DECISION.application, APPLICATION)
                .where(DECISION.status.eq(PermitDecision.Status.PUBLISHED))
                .where(applicationPredicate(person))
                .fetch();
    }

    @Transactional(readOnly = true)
    public List<HarvestPermit> findPermitByApplicationContactPerson(final @Nonnull Person person) {
        return jpqlQueryFactory.selectFrom(PERMIT)
                .join(PERMIT.permitDecision, DECISION)
                .join(DECISION.application, APPLICATION)
                .where(applicationPredicate(person))
                .fetch();
    }

    @Transactional(readOnly = true)
    public List<HarvestPermit> findPermitByContactPerson(final @Nonnull Person person) {
        final BooleanExpression permitContactPersonExists = JPAExpressions
                .selectOne()
                .from(PERMIT_CONTACT)
                .where(PERMIT_CONTACT.harvestPermit.eq(PERMIT))
                .where(PERMIT_CONTACT.contactPerson.eq(person))
                .exists();

        return jpqlQueryFactory.selectFrom(PERMIT)
                .where(PERMIT.permitTypeCode.ne(PermitTypeCode.MOOSELIKE_AMENDMENT))
                .where(PERMIT.originalContactPerson.eq(person).or(permitContactPersonExists))
                .fetch();
    }
}

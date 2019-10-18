package fi.riista.feature.harvestpermit.mobile;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.gamediary.harvest.QHarvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitNotFoundException;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermitContactPerson;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.permit.PermitTypeCode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

import static com.querydsl.core.types.ExpressionUtils.and;
import static com.querydsl.core.types.ExpressionUtils.or;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

@Component
public class MobileHarvestPermitFeature {

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private JPQLQueryFactory queryFactory;

    @Transactional(readOnly = true)
    public MobileHarvestPermitExistsDTO findPermitNumber(final String permitNumber) {
        return Optional.ofNullable(harvestPermitRepository.findByPermitNumber(permitNumber))
                .map(MobileHarvestPermitExistsDTO::create)
                .orElseThrow(() -> new HarvestPermitNotFoundException(permitNumber));
    }

    @Transactional(readOnly = true)
    public List<MobileHarvestPermitExistsDTO> preloadPermits() {
        final Person person = activeUserService.requireActivePerson();
        return MobileHarvestPermitExistsDTO.create(findPermits(person));
    }

    private List<HarvestPermit> findPermits(final Person person) {
        return queryFactory.selectFrom(PredicateFactory.PERMIT)
                .where(PredicateFactory.create(person))
                .fetch();
    }

    private static class PredicateFactory {
        static final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
        static final QHarvestPermitContactPerson CONTACT_PERSON = QHarvestPermitContactPerson.harvestPermitContactPerson;
        static final QHarvest HARVEST = QHarvest.harvest;

        public static Predicate create(final @Nonnull Person person) {
            requireNonNull(person);

            return and(notMoosePermit(), or(
                    harvestAuthorOrShooter(person),
                    and(isPermitContactPerson(person), or(
                            notHarvestsAsList(),
                            notHarvestReportDone()))));
        }

        private static Predicate notMoosePermit() {
            return ExpressionUtils.notIn(PERMIT.permitTypeCode,
                    asList(PermitTypeCode.MOOSELIKE, PermitTypeCode.MOOSELIKE_AMENDMENT));
        }

        private static BooleanExpression notHarvestsAsList() {
            return PERMIT.harvestsAsList.isFalse();
        }

        private static BooleanExpression notHarvestReportDone() {
            return PERMIT.harvestReportState.isNull();
        }

        private static BooleanExpression harvestAuthorOrShooter(final @Nonnull Person person) {
            return JPAExpressions.selectFrom(HARVEST)
                    .where(HARVEST.harvestPermit.eq(PERMIT))
                    .where(HARVEST.author.eq(person).or(HARVEST.actualShooter.eq(person)))
                    .exists();
        }

        private static Predicate isPermitContactPerson(final @Nonnull Person person) {
            return ExpressionUtils.or(
                    PERMIT.originalContactPerson.eq(person),
                    JPAExpressions.selectFrom(CONTACT_PERSON)
                            .from(CONTACT_PERSON)
                            .where(CONTACT_PERSON.harvestPermit.eq(PERMIT))
                            .where(CONTACT_PERSON.contactPerson.eq(person))
                            .exists());
        }

        private PredicateFactory() {
            throw new AssertionError();
        }
    }

}

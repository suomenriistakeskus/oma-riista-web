package fi.riista.feature.harvestpermit.mobile;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.account.pilot.DeerPilotService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.QHarvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitNotFoundException;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermitContactPerson;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.permit.PermitTypeCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

import static com.querydsl.core.types.ExpressionUtils.and;
import static com.querydsl.core.types.ExpressionUtils.or;
import static fi.riista.feature.permit.PermitTypeCode.CANNOT_LINK_HARVESTS;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

@Service
public class MobileHarvestPermitFeature {

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private DeerPilotService deerPilotService;

    @Resource
    private JPQLQueryFactory queryFactory;

    @Transactional(readOnly = true)
    public MobileHarvestPermitExistsDTO findPermitNumber(final String permitNumber,
                                                         @Nonnull final HarvestSpecVersion specVersion) {

        // TODO Remove this when deer pilot 2020 is over.
        final HarvestSpecVersion revisedSpecVersion =
                specVersion.revertIfNotOnDeerPilot(deerPilotService.isPilotUser());

        return Optional.ofNullable(harvestPermitRepository.findByPermitNumber(permitNumber))
                .filter(permit -> PermitTypeCode.canLinkHarvests(permit.getPermitTypeCode()))
                .map(permit -> MobileHarvestPermitExistsDTO.create(permit, revisedSpecVersion))
                .orElseThrow(() -> new HarvestPermitNotFoundException(permitNumber));
    }

    @Transactional(readOnly = true)
    public List<MobileHarvestPermitExistsDTO> preloadPermits(@Nonnull final HarvestSpecVersion specVersion) {
        final Person person = activeUserService.requireActivePerson();

        // TODO Remove this when deer pilot 2020 is over.
        final HarvestSpecVersion revisedSpecVersion =
                specVersion.revertIfNotOnDeerPilot(deerPilotService.isPilotUser(person));

        return MobileHarvestPermitExistsDTO.create(findPermits(person), revisedSpecVersion);
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

            return and(notListedPermits(), or(
                    harvestAuthorOrShooter(person),
                    and(isPermitContactPerson(person), or(
                            notHarvestsAsList(),
                            notHarvestReportDone()))));
        }

        private static Predicate notListedPermits() {
            return ExpressionUtils.notIn(PERMIT.permitTypeCode, CANNOT_LINK_HARVESTS);
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

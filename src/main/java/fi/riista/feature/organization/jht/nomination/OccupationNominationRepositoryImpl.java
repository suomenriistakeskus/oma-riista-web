package fi.riista.feature.organization.jht.nomination;

import com.google.common.base.Preconditions;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.jpa.JPQLQuery;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.jht.email.NotifyJhtOccupationNominationToRkaEmailDTO;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.LocalisedString;
import org.joda.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fi.riista.feature.organization.jht.nomination.OccupationNomination.NominationStatus.ESITETTY;
import static fi.riista.feature.organization.jht.nomination.OccupationNomination.NominationStatus.HYLATTY;
import static fi.riista.feature.organization.jht.nomination.OccupationNomination.NominationStatus.NIMITETTY;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@Repository
public class OccupationNominationRepositoryImpl extends QuerydslRepositorySupport
        implements OccupationNominationRepositoryCustom {

    public OccupationNominationRepositoryImpl() {
        super(OccupationNomination.class);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Map<OccupationNomination.NominationStatus, Long> countByNominationStatus(final Riistanhoitoyhdistys rhy,
                                                                                    final OccupationType occupationType) {
        final QOccupationNomination qOccupationNomination = QOccupationNomination.occupationNomination;
        final EnumPath<OccupationNomination.NominationStatus> nominationStatus = qOccupationNomination.nominationStatus;

        return from(qOccupationNomination)
                .from(qOccupationNomination)
                .select(nominationStatus, nominationStatus.count())
                .where(qOccupationNomination.rhy.eq(rhy)
                        .and(qOccupationNomination.occupationType.eq(occupationType)))
                .groupBy(nominationStatus)
                .transform(GroupBy.groupBy(nominationStatus).as(nominationStatus.count()));
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Page<OccupationNomination> searchPage(@Nonnull final Pageable pageRequest,
                                                 @Nonnull final OccupationType occupationType,
                                                 @Nonnull final OccupationNomination.NominationStatus nominationStatus,
                                                 @Nullable final RiistakeskuksenAlue rka,
                                                 @Nullable final Riistanhoitoyhdistys rhy,
                                                 @Nullable final Person person,
                                                 @Nullable final LocalDate beginDate,
                                                 @Nullable final LocalDate endDate) {
        Preconditions.checkNotNull(pageRequest, "pageRequest is null");
        Preconditions.checkNotNull(occupationType, "occupationType is null");
        Preconditions.checkNotNull(nominationStatus, "nominationStatus is null");

        final QOccupationNomination OCCUPATION_NOMINATION = QOccupationNomination.occupationNomination;
        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
        final List<Predicate> predicateList = new LinkedList<>();

        predicateList.add(OCCUPATION_NOMINATION.occupationType.eq(occupationType));
        predicateList.add(OCCUPATION_NOMINATION.nominationStatus.eq(nominationStatus));

        if (rhy != null) {
            predicateList.add(OCCUPATION_NOMINATION.rhy.eq(rhy));
        } else if (rka != null) {
            predicateList.add(RHY.parentOrganisation.eq(rka));
        }

        if (person != null) {
            predicateList.add(OCCUPATION_NOMINATION.person.eq(person));
        }

        if (nominationStatus == NIMITETTY || nominationStatus == HYLATTY) {
            dateRangePredicate(OCCUPATION_NOMINATION.decisionDate, beginDate, endDate).ifPresent(predicateList::add);
        } else if (nominationStatus == ESITETTY) {
            dateRangePredicate(OCCUPATION_NOMINATION.nominationDate, beginDate, endDate).ifPresent(predicateList::add);
        }

        final Predicate[] predicateArray = predicateList.toArray(new Predicate[predicateList.size()]);

        final JPQLQuery<OccupationNomination> query = from(OCCUPATION_NOMINATION)
                .leftJoin(OCCUPATION_NOMINATION.rhy, RHY)
                .where(predicateArray)
                .select(OCCUPATION_NOMINATION);

        final List<OccupationNomination> resultList = getQuerydsl().applyPagination(pageRequest, query).fetch();

        return new PageImpl<>(resultList, pageRequest, query.fetchCount());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotifyJhtOccupationNominationToRkaEmailDTO> findRkaNotifications(final LocalDate nominationDate) {
        final QOccupationNomination NOMINATION = QOccupationNomination.occupationNomination;
        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
        final QOrganisation RKA = new QOrganisation("rka");

        final Expression<LocalisedString> rhyName = RHY.nameLocalisation();

        return from(NOMINATION)
                .join(NOMINATION.rhy, RHY)
                .join(RHY.parentOrganisation, RKA)
                .where(NOMINATION.nominationDate.eq(nominationDate),
                        NOMINATION.nominationStatus.eq(ESITETTY),
                        RKA.email.isNotNull())
                .select(NOMINATION.occupationType,
                        RHY.officialCode,
                        rhyName,
                        RKA.email)
                .distinct()
                .fetch().stream()
                .map(tuple -> new NotifyJhtOccupationNominationToRkaEmailDTO(
                        tuple.get(NOMINATION.occupationType),
                        tuple.get(RHY.officialCode),
                        tuple.get(rhyName),
                        tuple.get(RKA.email)))
                .collect(toList());
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public LocalDate findProposalDateForNomination(final @Nonnull Riistanhoitoyhdistys rhy,
                                                   final @Nonnull OccupationType occupationType) {
        requireNonNull(rhy);
        requireNonNull(occupationType);
        final QOccupationNomination OCCUPATION_NOMINATION = QOccupationNomination.occupationNomination;

        return from(OCCUPATION_NOMINATION)
                .select(OCCUPATION_NOMINATION.nominationDate.max())
                .where(OCCUPATION_NOMINATION.rhy.eq(rhy),
                        OCCUPATION_NOMINATION.nominationStatus.eq(ESITETTY),
                        OCCUPATION_NOMINATION.occupationType.eq(occupationType))
                .fetchOne();
    }

    private static Optional<BooleanExpression> dateRangePredicate(final DatePath<LocalDate> datePath,
                                                                  final LocalDate beginDate,
                                                                  final LocalDate endDate) {
        if (beginDate != null && endDate != null) {
            return Optional.of(datePath.between(beginDate, endDate));
        } else if (beginDate != null) {
            return Optional.of(datePath.goe(beginDate));
        } else if (endDate != null) {
            return Optional.of(datePath.loe(endDate));
        }
        return Optional.empty();
    }
}

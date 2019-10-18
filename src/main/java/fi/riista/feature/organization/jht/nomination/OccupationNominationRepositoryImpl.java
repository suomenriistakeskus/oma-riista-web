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
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Repository
public class OccupationNominationRepositoryImpl extends QueryDslRepositorySupport
        implements OccupationNominationRepositoryCustom {

    public OccupationNominationRepositoryImpl() {
        super(OccupationNomination.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<OccupationNomination.NominationStatus, Long> countByNominationStatus(final Riistanhoitoyhdistys rhy, final OccupationType occupationType) {
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
    @Transactional(readOnly = true)
    public Page<OccupationNomination> searchPage(@Nonnull final Pageable pageRequest,
                                                 @Nonnull final OccupationType occupationType,
                                                 @Nonnull final OccupationNomination.NominationStatus nominationStatus,
                                                 @Nullable final Riistanhoitoyhdistys rhy,
                                                 @Nullable final Person person,
                                                 @Nullable final LocalDate beginDate,
                                                 @Nullable final LocalDate endDate) {
        Preconditions.checkNotNull(pageRequest, "pageRequest is null");
        Preconditions.checkNotNull(occupationType, "occupationType is null");
        Preconditions.checkNotNull(nominationStatus, "nominationStatus is null");

        final QOccupationNomination qOccupationNomination = QOccupationNomination.occupationNomination;

        final List<Predicate> predicateList = new LinkedList<>();

        predicateList.add(qOccupationNomination.occupationType.eq(occupationType));
        predicateList.add(qOccupationNomination.nominationStatus.eq(nominationStatus));

        if (rhy != null) {
            predicateList.add(qOccupationNomination.rhy.eq(rhy));
        }

        if (person != null) {
            predicateList.add(qOccupationNomination.person.eq(person));
        }

        if (nominationStatus == OccupationNomination.NominationStatus.NIMITETTY ||
                nominationStatus == OccupationNomination.NominationStatus.HYLATTY) {
            dateRangePredicate(qOccupationNomination.decisionDate, beginDate, endDate).ifPresent(predicateList::add);
        } else if (nominationStatus == OccupationNomination.NominationStatus.ESITETTY) {
            dateRangePredicate(qOccupationNomination.nominationDate, beginDate, endDate).ifPresent(predicateList::add);
        }

        final Predicate[] predicateArray = predicateList.toArray(new Predicate[predicateList.size()]);

        final JPQLQuery<OccupationNomination> query = from(qOccupationNomination)
                .where(predicateArray)
                .select(qOccupationNomination);

        final List<OccupationNomination> resultList = getQuerydsl().applyPagination(pageRequest, query).fetch();

        return new PageImpl<>(resultList, pageRequest, query.fetchCount());
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
                        NOMINATION.nominationStatus.eq(OccupationNomination.NominationStatus.ESITETTY),
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
}

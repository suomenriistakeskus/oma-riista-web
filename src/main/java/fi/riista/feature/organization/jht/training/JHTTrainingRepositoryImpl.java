package fi.riista.feature.organization.jht.training;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.joda.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static fi.riista.util.DateUtil.today;

public class JHTTrainingRepositoryImpl extends QueryDslRepositorySupport implements JHTTrainingRepositoryCustom {
    public static final double MAX_FUZZY_DISTANCE = 0.6;

    @Resource
    private JPQLQueryFactory queryFactory;

    public JHTTrainingRepositoryImpl() {
        super(JHTTraining.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JHTTraining> searchPage(
            @Nonnull final Pageable pageRequest,
            @Nonnull final JHTTrainingSearchDTO.SearchType searchType,
            @Nonnull final OccupationType occupationType,
            @Nullable final JHTTraining.TrainingType trainingType,
            @Nullable final String trainingLocation,
            @Nullable final Riistanhoitoyhdistys rhy,
            @Nullable final Person person,
            @Nullable final LocalDate beginDate,
            @Nullable final LocalDate endDate) {
        Objects.requireNonNull(pageRequest, "pageRequest is null");
        Objects.requireNonNull(occupationType, "occupationType is null");
        Objects.requireNonNull(searchType, "searchType is null");

        final QJHTTraining qTraining = QJHTTraining.jHTTraining;
        final QPerson qPerson = QPerson.person;

        final List<Predicate> predicateList = new LinkedList<>();

        predicateList.add(qTraining.occupationType.eq(occupationType));

        if (trainingType != null) {
            predicateList.add(qTraining.trainingType.eq(trainingType));
        }

        if (beginDate != null && endDate != null) {
            predicateList.add(qTraining.trainingDate.between(beginDate, endDate));
        } else if (beginDate != null) {
            predicateList.add(qTraining.trainingDate.goe(beginDate));
        } else if (endDate != null) {
            predicateList.add(qTraining.trainingDate.loe(endDate));
        }

        if (searchType == JHTTrainingSearchDTO.SearchType.PREVIOUS_OCCUPATION) {
            Objects.requireNonNull(rhy, "rhy is null");
            predicateList.add(qTraining.person.in(queryPersonWithExistingOrPastOccupation(occupationType, rhy)));

        } else if (searchType == JHTTrainingSearchDTO.SearchType.TRAINING_LOCATION) {
            predicateList.add(qTraining.trainingType.eq(JHTTraining.TrainingType.LAHI));

            if (StringUtils.hasText(trainingLocation)) {
                predicateList.add(Expressions.booleanTemplate(
                        "true = trgm_match({0},{1})", trainingLocation, qTraining.trainingLocation));

                predicateList.add(Expressions.booleanTemplate(
                        "trgm_dist({0},{1}) < {2}", trainingLocation, qTraining.trainingLocation, MAX_FUZZY_DISTANCE));
            }

        } else if (searchType == JHTTrainingSearchDTO.SearchType.HOME_RHY) {
            Objects.requireNonNull(rhy, "rhy is null");

            predicateList.add(qTraining.trainingType.eq(JHTTraining.TrainingType.LAHI));
            predicateList.add(qPerson.rhyMembership.eq(rhy));
            predicateList.add(qPerson.notIn(queryPersonWithExistingOrPastOccupation(occupationType, rhy)));

        } else if (searchType == JHTTrainingSearchDTO.SearchType.PERSON) {
            Objects.requireNonNull(person, "person is null");

            predicateList.add(qTraining.person.eq(person));

        } else {
            throw new IllegalArgumentException("Invalid searchType");
        }

        final Predicate[] predicateArray = predicateList.toArray(new Predicate[predicateList.size()]);

        final JPQLQuery<JHTTraining> query = queryFactory
                .from(qTraining)
                .where(predicateArray)
                .select(qTraining);

        if (searchType == JHTTrainingSearchDTO.SearchType.HOME_RHY) {
            query.join(qTraining.person, qPerson);
        }

        final List<JHTTraining> resultList = getQuerydsl().applyPagination(pageRequest, query).fetch();

        return new PageImpl<>(resultList, pageRequest, query.fetchCount());
    }

    private static JPQLQuery<Person> queryPersonWithExistingOrPastOccupation(
            final @Nonnull OccupationType occupationType,
            final @Nonnull Riistanhoitoyhdistys rhy) {
        final QOccupation qRhyOccupation = QOccupation.occupation;

        return JPAExpressions
                .select(qRhyOccupation.person)
                .from(qRhyOccupation)
                .where(qRhyOccupation.organisation.eq(rhy)
                        .and(qRhyOccupation.occupationType.eq(occupationType))
                        .and(qRhyOccupation.beginDate.isNull().or(qRhyOccupation.beginDate.loe(today())))
                        .and(qRhyOccupation.notDeleted()));
    }
}

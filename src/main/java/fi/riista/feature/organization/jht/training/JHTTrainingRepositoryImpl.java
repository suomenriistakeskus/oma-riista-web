package fi.riista.feature.organization.jht.training;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.joda.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.util.DateUtil.today;

public class JHTTrainingRepositoryImpl extends QuerydslRepositorySupport implements JHTTrainingRepositoryCustom {
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
            @Nullable final RiistakeskuksenAlue rka,
            @Nullable final Riistanhoitoyhdistys rhy,
            @Nullable final Person person,
            @Nullable final LocalDate beginDate,
            @Nullable final LocalDate endDate) {
        Objects.requireNonNull(pageRequest, "pageRequest is null");
        Objects.requireNonNull(occupationType, "occupationType is null");
        Objects.requireNonNull(searchType, "searchType is null");

        final QJHTTraining TRAINING = QJHTTraining.jHTTraining;
        final QPerson PERSON = QPerson.person;

        final List<Predicate> predicateList = new LinkedList<>();

        predicateList.add(TRAINING.occupationType.eq(occupationType));

        if (trainingType != null) {
            predicateList.add(TRAINING.trainingType.eq(trainingType));
        }

        if (beginDate != null && endDate != null) {
            predicateList.add(TRAINING.trainingDate.between(beginDate, endDate));
        } else if (beginDate != null) {
            predicateList.add(TRAINING.trainingDate.goe(beginDate));
        } else if (endDate != null) {
            predicateList.add(TRAINING.trainingDate.loe(endDate));
        }

        if (searchType == JHTTrainingSearchDTO.SearchType.PREVIOUS_OCCUPATION) {
            checkArgument(rka != null || rhy != null);

            predicateList.add(TRAINING.person.in(queryPersonWithExistingOrPastOccupation(occupationType, rka, rhy)));

        } else if (searchType == JHTTrainingSearchDTO.SearchType.TRAINING_LOCATION) {
            predicateList.add(TRAINING.trainingType.eq(JHTTraining.TrainingType.LAHI));

            if (StringUtils.hasText(trainingLocation)) {
                predicateList.add(Expressions.booleanTemplate(
                        "true = trgm_match({0},{1})", trainingLocation, TRAINING.trainingLocation));

                predicateList.add(Expressions.booleanTemplate(
                        "trgm_dist({0},{1}) < {2}", trainingLocation, TRAINING.trainingLocation, MAX_FUZZY_DISTANCE));
            }

        } else if (searchType == JHTTrainingSearchDTO.SearchType.HOME_RHY) {
            checkArgument(rka != null || rhy != null);

            predicateList.add(TRAINING.trainingType.eq(JHTTraining.TrainingType.LAHI));
            if (rhy != null) {
                predicateList.add(PERSON.rhyMembership.eq(rhy));

            } else {
                predicateList.add(PERSON.rhyMembership.in(queryRhysInRka(rka)));
            }
            predicateList.add(PERSON.notIn(queryPersonWithExistingOrPastOccupation(occupationType, rka, rhy)));

        } else if (searchType == JHTTrainingSearchDTO.SearchType.PERSON) {
            Objects.requireNonNull(person, "person is null");

            predicateList.add(TRAINING.person.eq(person));

        } else {
            throw new IllegalArgumentException("Invalid searchType");
        }

        final Predicate[] predicateArray = predicateList.toArray(new Predicate[predicateList.size()]);

        final JPQLQuery<JHTTraining> query = queryFactory
                .from(TRAINING)
                .where(predicateArray)
                .select(TRAINING);

        if (searchType == JHTTrainingSearchDTO.SearchType.HOME_RHY) {
            query.join(TRAINING.person, PERSON);
        }

        final List<JHTTraining> resultList = getQuerydsl().applyPagination(pageRequest, query).fetch();

        return new PageImpl<>(resultList, pageRequest, query.fetchCount());
    }

    private static JPQLQuery<Riistanhoitoyhdistys> queryRhysInRka(final RiistakeskuksenAlue rka) {
        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;

        return JPAExpressions.selectFrom(RHY).where(RHY.parentOrganisation.eq(rka));
    }

    private static JPQLQuery<Person> queryPersonWithExistingOrPastOccupation(
            final @Nonnull OccupationType occupationType,
            final RiistakeskuksenAlue rka,
            final Riistanhoitoyhdistys rhy) {
        final QOrganisation RHY = new QOrganisation("rhy");
        final QOccupation OCCUPATION = QOccupation.occupation;

        final BooleanExpression matchingValidOccupation = OCCUPATION.occupationType.eq(occupationType)
                .and(OCCUPATION.beginDate.isNull().or(OCCUPATION.beginDate.loe(today())))
                .and(OCCUPATION.notDeleted());

        if (rhy == null) {
            return JPAExpressions
                    .select(OCCUPATION.person)
                    .from(OCCUPATION)
                    .innerJoin(OCCUPATION.organisation, RHY)
                    .where(RHY.parentOrganisation.eq(rka)
                            .and(matchingValidOccupation));
        }
        return JPAExpressions
                .select(OCCUPATION.person)
                .from(OCCUPATION)
                .where(OCCUPATION.organisation.eq(rhy)
                        .and(matchingValidOccupation));
    }
}

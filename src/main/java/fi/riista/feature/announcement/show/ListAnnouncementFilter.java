package fi.riista.feature.announcement.show;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import fi.riista.feature.announcement.QAnnouncement;
import fi.riista.feature.announcement.QAnnouncementSubscriber;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static fi.riista.util.DateUtil.huntingYear;
import static fi.riista.util.DateUtil.huntingYearBeginDate;
import static fi.riista.util.DateUtil.toDateNullSafe;

public class ListAnnouncementFilter {
    private final OccupationRepository occupationRepository;

    private Person subscriberPerson;
    private Organisation subscriberOrganisation;
    private Organisation fromOrganisation;
    private Date createdAfter;

    public ListAnnouncementFilter(final OccupationRepository occupationRepository) {
        this.occupationRepository = occupationRepository;
    }

    public ListAnnouncementFilter withSubscriberPerson(final Person person) {
        this.subscriberPerson = Objects.requireNonNull(person, "person is null");
        return this;
    }

    public ListAnnouncementFilter withSubscriberOrganisation(final Organisation organisation) {
        this.subscriberOrganisation = Objects.requireNonNull(organisation, "organisation is null");
        return this;
    }

    public ListAnnouncementFilter withFromOrganisation(final Organisation fromOrganisation) {
        this.fromOrganisation = Objects.requireNonNull(fromOrganisation, "fromOrganisation is null");
        return this;
    }

    public ListAnnouncementFilter withCreatedAfter(final Date since) {
        this.createdAfter = Objects.requireNonNull(since, "since is null");
        return this;
    }

    public ListAnnouncementFilter withCreatedAfterStartOfPreviousHuntingYearDate() {
        return withCreatedAfter(toDateNullSafe(huntingYearBeginDate(huntingYear() - 1)));
    }

    public Optional<Predicate> buildPredicate() {
        final QAnnouncement MSG = QAnnouncement.announcement;
        final QAnnouncementSubscriber SUB = QAnnouncementSubscriber.announcementSubscriber;

        final BooleanBuilder builder = new BooleanBuilder();

        if (this.createdAfter != null) {
            builder.and(MSG.lifecycleFields.creationTime.goe(this.createdAfter));
        }

        if (this.fromOrganisation != null) {
            builder.and(MSG.fromOrganisation.eq(this.fromOrganisation));
        }

        final BooleanExpression visibleToAll = MSG.visibleToAll.isTrue();

        if (this.subscriberPerson != null) {
            // Person predicate is missing, if person has no suitable occupations
            final List<Occupation> activeOccupations = occupationRepository.findActiveByPerson(this.subscriberPerson);
            final Optional<Predicate> personSubscriberPredicate = new PersonOccupationGraph(activeOccupations)
                    .withOrganisationFilter(subscriberOrganisation)
                    .buildPredicate(SUB);

            if (personSubscriberPredicate.isPresent()) {
                // Limit visibility based on subscriber role matching to active person occupation
                final Predicate subscriberExists = subscriberExists(MSG, SUB, personSubscriberPredicate.get());

                if (this.subscriberOrganisation != null) {
                    builder.and(subscriberExists);
                } else {
                    builder.and(ExpressionUtils.or(subscriberExists, visibleToAll));
                }

            } else if (this.subscriberOrganisation == null) {
                builder.and(visibleToAll);
            } else {
                return Optional.empty();
            }

        } else if (this.subscriberOrganisation != null) {
            builder.and(subscriberExists(MSG, SUB, moderatorPredicate(this.subscriberOrganisation, SUB)));
        }

        return Optional.ofNullable(builder.getValue());
    }

    private static Predicate subscriberExists(final QAnnouncement announcement,
                                              final QAnnouncementSubscriber subscriber,
                                              final Predicate predicate) {
        Objects.requireNonNull(predicate, "subscriber predicate is null");
        return JPAExpressions.selectFrom(subscriber)
                .where(subscriber.announcement.eq(announcement), predicate)
                .exists();
    }

    private static Predicate moderatorPredicate(final Organisation subscriberOrganisation,
                                                final QAnnouncementSubscriber Q_SUBSCRIBER) {
        return Q_SUBSCRIBER.organisation.in(subscriberOrganisation.getAllParentsAndSelf())
                .and(Q_SUBSCRIBER.occupationType.in(
                        OccupationType.applicableValuesFor(subscriberOrganisation.getOrganisationType())));
    }
}

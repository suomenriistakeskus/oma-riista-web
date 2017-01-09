package fi.riista.feature.announcement.show;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPAExpressions;
import fi.riista.feature.announcement.QAnnouncement;
import fi.riista.feature.announcement.QAnnouncementSubscriber;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

public class ListAnnouncementFilter {
    private static final Logger LOG = LoggerFactory.getLogger(ListAnnouncementFilter.class);

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

    public ListAnnouncementFilter withCreatedAfterStartOfHuntingYearDate() {
        return withCreatedAfter(DateUtil.toDateNullSafe(DateUtil.huntingYearBeginDate(
                DateUtil.getFirstCalendarYearOfCurrentHuntingYear())));
    }

    public Optional<Predicate> buildPredicate() {
        final QAnnouncement Q_ANNOUNCEMENT = QAnnouncement.announcement;
        final QAnnouncementSubscriber Q_SUBSCRIBER = QAnnouncementSubscriber.announcementSubscriber;

        final BooleanBuilder builder = new BooleanBuilder();

        if (this.createdAfter != null) {
            builder.and(Q_ANNOUNCEMENT.lifecycleFields.creationTime.goe(this.createdAfter));
        }

        if (this.fromOrganisation != null) {
            builder.and(Q_ANNOUNCEMENT.fromOrganisation.eq(this.fromOrganisation));
        }

        if (this.subscriberPerson != null) {
            // Limit visibility based on subscriber role matching to active person occupation
            final PersonOccupationGraph personOccupationGraph = new PersonOccupationGraph(
                    occupationRepository.findActiveByPerson(this.subscriberPerson));

            if (this.subscriberOrganisation != null) {
                personOccupationGraph.withOrganisationFilter(this.subscriberOrganisation.getAllParentsAndSelf());
            }

            // Predicate is missing, if person has no suitable occupations
            final Optional<Predicate> personPredicate = personOccupationGraph.buildPredicate(Q_SUBSCRIBER);

            if (personPredicate.isPresent()) {
                builder.and(subscriberExists(Q_ANNOUNCEMENT, Q_SUBSCRIBER, personPredicate.get()));

            } else {
                LOG.error("Person occupation graph is empty");

                // Cannot create empty match expression
                return Optional.empty();
            }
        } else if (this.subscriberOrganisation != null) {
            builder.and(subscriberExists(Q_ANNOUNCEMENT, Q_SUBSCRIBER,
                    moderatorPredicate(this.subscriberOrganisation, Q_SUBSCRIBER)));
        }

        return Optional.ofNullable(builder.getValue());
    }

    private static Predicate subscriberExists(final QAnnouncement announcement,
                                              final QAnnouncementSubscriber subscriber,
                                              final Predicate predicate) {
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

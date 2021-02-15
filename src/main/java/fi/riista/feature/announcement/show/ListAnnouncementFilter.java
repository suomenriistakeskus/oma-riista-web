package fi.riista.feature.announcement.show;

import com.google.common.base.Preconditions;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import fi.riista.feature.announcement.AnnouncementSubscriber;
import fi.riista.feature.announcement.QAnnouncement;
import fi.riista.feature.announcement.QAnnouncementSubscriber;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.security.UserInfo;
import fi.riista.util.F;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;

public class ListAnnouncementFilter {
    private boolean isAdminOrModerator;
    private List<Occupation> activePersonOccupations;
    private Organisation activePersonRhy;
    private Organisation organisation;
    private DateTime createdAfter;

    public ListAnnouncementFilter withActiveUser(final UserInfo user) {
        this.isAdminOrModerator = Objects.requireNonNull(user).isAdminOrModerator();
        return this;
    }

    public ListAnnouncementFilter withActivePersonOccupations(final List<Occupation> activePersonOccupations) {
        this.activePersonOccupations = requireNonNull(activePersonOccupations);
        return this;
    }

    public ListAnnouncementFilter withActivePersonRhy(final Riistanhoitoyhdistys rhy) {
        this.activePersonRhy = rhy;
        return this;
    }

    public ListAnnouncementFilter withOrganisation(final Organisation organisation) {
        this.organisation = requireNonNull(organisation, "organisation is null");
        return this;
    }

    public ListAnnouncementFilter withCreatedAfter(final DateTime createdAfter) {
        this.createdAfter = requireNonNull(createdAfter, "createdAfter is null");
        return this;
    }

    public Optional<Predicate> buildPredicate() {
        if (isAdminOrModerator) {
            return Optional.of(buildModeratorPredicate());
        } else if (this.organisation == null) {
            return Optional.of(buildPersonalPredicate());
        } else {
            return Optional.ofNullable(buildOrganisationPredicate());
        }
    }

    private Predicate buildModeratorPredicate() {
        Objects.requireNonNull(organisation);

        return new BooleanBuilder()
                .and(PredicateFactory.messageCreatedAfter(this.createdAfter))
                .and(ExpressionUtils.anyOf(
                        PredicateFactory.messageFromOrganisation(organisation),
                        PredicateFactory.messageVisibleToModerator(organisation),
                        PredicateFactory.messageVisibleForRhyMembers(organisation)))
                .getValue();
    }

    private Predicate buildOrganisationPredicate() {
        Objects.requireNonNull(activePersonOccupations);
        Objects.requireNonNull(organisation);

        final List<GroupedPersonOccupations> occupationsForTargetOrganisation =
                getGroupedOccupationTypes(activePersonOccupations, organisation);

        if (occupationsForTargetOrganisation.isEmpty()) {
            // No occupations for target organisation -> nothing to query
            return null;
        }

        return new BooleanBuilder()
                .and(PredicateFactory.messageCreatedAfter(this.createdAfter))
                .and(ExpressionUtils.anyOf(
                        PredicateFactory.messageVisibleForPersonWithOccupations(occupationsForTargetOrganisation),
                        PredicateFactory.messageVisibleForRhyMembers(organisation)))
                .getValue();
    }

    private Predicate buildPersonalPredicate() {
        Objects.requireNonNull(activePersonOccupations);
        Preconditions.checkArgument(organisation == null);

        final List<GroupedPersonOccupations> groupedOccupationTypes =
                getGroupedOccupationTypes(activePersonOccupations, null);

        return new BooleanBuilder()
                .and(PredicateFactory.messageCreatedAfter(createdAfter))
                .and(ExpressionUtils.anyOf(
                        PredicateFactory.messageIsVisibleToAll(),
                        PredicateFactory.messageVisibleForRhyMembers(activePersonRhy),
                        PredicateFactory.messageVisibleForPersonWithOccupations(groupedOccupationTypes)))
                .getValue();
    }

    private static class PredicateFactory {
        private static final QAnnouncement MSG = QAnnouncement.announcement;
        private static final QAnnouncementSubscriber SUB = QAnnouncementSubscriber.announcementSubscriber;

        public static BooleanExpression messageIsVisibleToAll() {
            return MSG.visibleToAll.isTrue();
        }

        public static BooleanExpression messageCreatedAfter(final DateTime createdAfter) {
            return createdAfter != null ? MSG.lifecycleFields.creationTime.goe(createdAfter) : null;
        }

        public static BooleanExpression messageFromOrganisation(final Organisation from) {
            return from != null ? MSG.fromOrganisation.eq(from) : null;
        }

        public static BooleanExpression messageVisibleForRhyMembers(final Organisation rhy) {
            return rhy != null ? MSG.rhyMembershipSubscriber.eq(rhy) : null;
        }

        public static Predicate messageVisibleToModerator(final @Nonnull Organisation organisationFilter) {
            Objects.requireNonNull(organisationFilter);

            final JPQLQuery<AnnouncementSubscriber> query = JPAExpressions.selectFrom(SUB)
                    .where(SUB.announcement.eq(MSG))
                    .where(SUB.organisation.in(organisationFilter.getAllParentsAndSelf()));

            if (organisationFilter.getOrganisationType() == OrganisationType.CLUB) {
                query.where(SUB.occupationType.in(OccupationType.clubValues()));
            }

            return query.exists();
        }

        public static BooleanExpression messageVisibleForPersonWithOccupations(
                final @Nonnull List<GroupedPersonOccupations> groupList) {
            Objects.requireNonNull(groupList);

            if (groupList.isEmpty()) {
                return null;
            }

            return JPAExpressions.selectFrom(SUB)
                    .where(SUB.announcement.eq(MSG))
                    .where(subscriberOccupationPredicate(groupList))
                    .exists();
        }

        private static Predicate subscriberOccupationPredicate(final List<GroupedPersonOccupations> groupList) {
            Preconditions.checkArgument(!groupList.isEmpty());
            return ExpressionUtils.anyOf(F.mapNonNullsToList(groupList, PredicateFactory::subscriberOccupationPredicate));
        }

        private static Predicate subscriberOccupationPredicate(final GroupedPersonOccupations group) {
            if (group.showAllOccupationTypes()) {
                return SUB.organisation.eq(group.getOrganisation());
            }

            return ExpressionUtils.and(
                    SUB.organisation.eq(group.getOrganisation()),
                    SUB.occupationType.in(group.getVisibleOccupationTypes()));
        }

        private PredicateFactory() {
            throw new AssertionError();
        }
    }

    private static List<GroupedPersonOccupations> getGroupedOccupationTypes(final List<Occupation> activeOccupations,
                                                                            final Organisation filterOrganisation) {
        Objects.requireNonNull(activeOccupations);

        final Map<Organisation, Set<OccupationType>> childOccupationMapping = activeOccupations.stream()
                .filter(occupation -> {
                    if (filterOrganisation == null) {
                        return true;
                    }

                    final Organisation occupationOrganisation = occupation.getOrganisation();
                    final OrganisationType occupationOrganisationType = occupationOrganisation.getOrganisationType();

                    // Filter occupations. Group occupations are special and should be visible for parent club.
                    return filterOrganisation.equals(occupationOrganisationType == OrganisationType.CLUBGROUP
                            ? occupationOrganisation.getParentOrganisation()
                            : occupationOrganisation);
                })
                .flatMap(ListAnnouncementFilter::expandOccupationToParentOrganisations)
                .collect(groupingBy(PersonOccupation::getOrganisation,
                        mapping(PersonOccupation::getOccupationType, toSet())));

        return F.mapNonNullsToList(childOccupationMapping.entrySet(), GroupedPersonOccupations::new);
    }

    /**
     * Child organisation occupation type is inherited by all parents
     * eg. club membership is expanded to parent organisations:
     *
     * CLUB -> CLUB_MEMBER
     * RHY  -> CLUB_MEMBER
     * RKA  -> CLUB_MEMBER
     * RK   -> CLUB_MEMBER
     */
    private static Stream<PersonOccupation> expandOccupationToParentOrganisations(final Occupation occupation) {
        return occupation.getOrganisation()
                .getAllParentsAndSelf().stream()
                .map(parentOrSelf -> new PersonOccupation(parentOrSelf, occupation.getOccupationType()));
    }

    private static class PersonOccupation {
        private final Organisation organisation;
        private final OccupationType occupationType;

        private PersonOccupation(final Organisation organisation, final OccupationType occupationType) {
            this.organisation = requireNonNull(organisation);
            this.occupationType = requireNonNull(occupationType);
        }

        public Organisation getOrganisation() {
            return organisation;
        }

        public OccupationType getOccupationType() {
            return occupationType;
        }
    }

    private static class GroupedPersonOccupations {
        private final Organisation organisation;
        private final Set<OccupationType> occupationTypes;

        private GroupedPersonOccupations(final Map.Entry<Organisation, Set<OccupationType>> entry) {
            this.organisation = requireNonNull(entry.getKey());
            this.occupationTypes = requireNonNull(entry.getValue());
        }

        public Organisation getOrganisation() {
            return organisation;
        }

        public boolean showAllOccupationTypes() {
            // Coordinator should see any message sent by parent organisations or self
            return occupationTypes.contains(OccupationType.TOIMINNANOHJAAJA);
        }

        @Nonnull
        public Set<OccupationType> getVisibleOccupationTypes() {
            // Club contact person should see all club communication
            if (occupationTypes.contains(OccupationType.SEURAN_YHDYSHENKILO)) {
                final Set<OccupationType> result = new HashSet<>(occupationTypes);
                result.add(OccupationType.SEURAN_JASEN);
                result.add(OccupationType.RYHMAN_JASEN);
                result.add(OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

                return result;
            }

            // Hunting leader should see all messages for normal members
            if (occupationTypes.contains(OccupationType.RYHMAN_METSASTYKSENJOHTAJA)) {
                final Set<OccupationType> result = new HashSet<>(occupationTypes);
                result.add(OccupationType.RYHMAN_JASEN);

                return result;
            }

            return occupationTypes;
        }
    }
}


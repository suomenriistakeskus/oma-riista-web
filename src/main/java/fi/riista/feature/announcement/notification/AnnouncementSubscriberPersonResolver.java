package fi.riista.feature.announcement.notification;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.account.user.QSystemUser;
import fi.riista.feature.announcement.Announcement;
import fi.riista.feature.announcement.AnnouncementSubscriber;
import fi.riista.feature.announcement.AnnouncementSubscriberRepository;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysEmailService;
import fi.riista.feature.push.QMobileClientDevice;
import fi.riista.util.F;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
public class AnnouncementSubscriberPersonResolver {

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Resource
    private AnnouncementSubscriberRepository announcementSubscriberRepository;

    @Resource
    private RiistanhoitoyhdistysEmailService riistanhoitoyhdistysEmailService;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public AnnouncementNotificationTargets collectTargets(final Announcement announcement,
                                                          final boolean sendEmail) {
        if (announcement.isVisibleToAll()) {
            // Only push notification for global announcements supported for now
            return new AnnouncementNotificationTargets(emptyList(), collectAllPushTokens());
        }

        final Set<String> pushTokens = new HashSet<>();
        final Set<String> emails = new HashSet<>();

        if (announcement.getRhyMembershipSubscriber() != null) {
            final List<Long> rhyMemberPersonIds =
                    collectRhyMemberPersonIds(announcement.getRhyMembershipSubscriber().getId());

            if (rhyMemberPersonIds.size() > 0) {
                pushTokens.addAll(getPushTokensForPersonIds(rhyMemberPersonIds));

                if (sendEmail) {
                    emails.addAll(getEmailsForPersonIds(rhyMemberPersonIds));
                }
            }
        }

        final List<AnnouncementSubscriber> announcementSubscribers =
                announcementSubscriberRepository.findByAnnouncement(announcement);

        if (announcementSubscribers.size() > 0) {
            final Map<Boolean, List<AnnouncementSubscriber>> subscriberMapping = announcementSubscribers.stream()
                    .collect(partitioningBy(subscriber -> subscriber.getOccupationType() == OccupationType.TOIMINNANOHJAAJA));
            final List<AnnouncementSubscriber> personSubscribers = subscriberMapping.get(false);
            final List<Long> personSubscriberIds = collectReceiverPersonIds(personSubscribers);

            final List<AnnouncementSubscriber> coordinatorSubscribers = subscriberMapping.get(true);
            final List<Long> coordinatorSubscriberIds = collectReceiverPersonIds(coordinatorSubscribers);

            final List<Long> subscriberPersonIds = F.concat(personSubscriberIds, coordinatorSubscriberIds);

            if (subscriberPersonIds.size() > 0) {
                pushTokens.addAll(getPushTokensForPersonIds(subscriberPersonIds));

                if (sendEmail) {
                    final Set<String> personEmails = getEmailsForPersonIds(personSubscriberIds);
                    final Set<String> coordinatorEmails = getEmailsForCoordinatorIds(coordinatorSubscriberIds);
                    Sets.union(personEmails, coordinatorEmails).copyInto(emails);
                }
            }
        }

        return new AnnouncementNotificationTargets(ImmutableList.copyOf(emails), ImmutableList.copyOf(pushTokens));
    }

    private List<Long> collectRhyMemberPersonIds(final long rhyId) {

        //OR-4414: Use explicit join to avoid using rhy._super
        final QPerson PERSON = QPerson.person;
        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
        return jpqlQueryFactory
                .select(PERSON.id)
                .from(PERSON)
                .innerJoin(PERSON.rhyMembership, RHY)
                .where(RHY.id.eq(rhyId)).fetch();
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<String> collectAllPushTokens() {
        final QMobileClientDevice MOBILE = QMobileClientDevice.mobileClientDevice;
        final QPerson PERSON = QPerson.person;

        return jpqlQueryFactory
                .select(MOBILE.pushToken)
                .from(MOBILE)
                .join(MOBILE.person, PERSON)
                .where(PERSON.deletionCode.isNull(),
                        PERSON.lifecycleFields.deletionTime.isNull())
                .fetch();
    }

    private List<Long> collectReceiverPersonIds(final List<AnnouncementSubscriber> subscribers) {
        if (F.isNullOrEmpty(subscribers)) {
            return emptyList();
        }

        final Set<Long> allPersonIds = new HashSet<>();

        // Group subscribers to reduce number of queries
        groupSubscriberByOccupationOrganisationType(subscribers)
                .forEach((occupationOrganisationType, organisationList) -> {
                    groupOccupationTypeByOrganisation(organisationList).forEach((organisation, occupationTypes) -> {
                        allPersonIds.addAll(findReceivers(organisation, occupationOrganisationType, occupationTypes));
                    });
                });

        return ImmutableList.copyOf(allPersonIds);
    }

    private Set<String> getEmailsForPersonIds(final List<Long> personIds) {
        if (F.isNullOrEmpty(personIds)) {
            return emptySet();
        }

        final QPerson PERSON = QPerson.person;
        final QSystemUser USER = QSystemUser.systemUser;

        return Lists.partition(personIds, 1000).stream()
                .flatMap(partition -> jpqlQueryFactory
                        .select(PERSON.email)
                        .from(USER)
                        .innerJoin(USER.person, PERSON)
                        .where(USER.active.isTrue(),
                                PERSON.id.in(partition),
                                PERSON.email.isNotNull(),
                                PERSON.deletionCode.isNull(),
                                PERSON.lifecycleFields.deletionTime.isNull(),
                                PERSON.denyAnnouncementEmail.isFalse())
                        .fetch().stream())
                .filter(StringUtils::hasText)
                .filter(email -> email.contains("@"))
                .map(email -> email.trim().toLowerCase())
                .collect(toSet());
    }

    private Set<String> getEmailsForCoordinatorIds(final List<Long> coordinatorIds) {
        if (F.isNullOrEmpty(coordinatorIds)) {
            return emptySet();
        }

        final QPerson PERSON = QPerson.person;
        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;

        final Set<Long> rhyIds = jpqlQueryFactory.select(RHY.id)
                .from(PERSON)
                .where(PERSON.id.in(coordinatorIds))
                .join(PERSON.rhyMembership, RHY)
                .fetch()
                .stream()
                .collect(toSet());

        final Map<Long, Set<String>> rhyEmails = riistanhoitoyhdistysEmailService.resolveEmails(rhyIds);

        return rhyEmails.values().stream().flatMap(Set::stream).collect(toSet());
    }

    private List<String> getPushTokensForPersonIds(final List<Long> personIds) {
        final QMobileClientDevice MOBILE = QMobileClientDevice.mobileClientDevice;
        final QPerson PERSON = QPerson.person;

        return Lists.partition(personIds, 1000).stream()
                .flatMap(partition -> jpqlQueryFactory
                        .select(MOBILE.pushToken)
                        .from(MOBILE)
                        .join(MOBILE.person, PERSON)
                        .where(MOBILE.person.id.in(partition),
                                PERSON.deletionCode.isNull(),
                                PERSON.lifecycleFields.deletionTime.isNull())
                        .fetch().stream())
                .collect(toList());
    }

    private List<Long> findReceivers(final Organisation organisation,
                                     final OrganisationType occupationOrganisationType,
                                     final Set<OccupationType> occupationTypes) {
        final QPerson PERSON = QPerson.person;
        final QOccupation OCCUPATION = QOccupation.occupation;
        final boolean isGroupOccupation = occupationOrganisationType == OrganisationType.CLUBGROUP;

        return jpqlQueryFactory
                .selectDistinct(PERSON.id)
                .from(OCCUPATION)
                .join(OCCUPATION.person, PERSON)
                .where(OCCUPATION.validAndNotDeleted(),
                        OCCUPATION.occupationType.in(occupationTypes),
                        organisationPredicate(organisation, occupationOrganisationType, OCCUPATION),
                        isGroupOccupation ? clubOccupationExistsPredicate(OCCUPATION, PERSON) : null)
                .fetch();
    }

    private static Map<OrganisationType, List<AnnouncementSubscriber>> groupSubscriberByOccupationOrganisationType(
            final List<AnnouncementSubscriber> subscribers) {
        return subscribers.stream().collect(groupingBy(
                s -> getOccupationOrganisationType(s.getOccupationType()),
                Collectors.mapping(Function.identity(), toList())));
    }

    private static OrganisationType getOccupationOrganisationType(final OccupationType occupationType) {
        if (occupationType.isApplicableFor(OrganisationType.CLUB)) {
            return OrganisationType.CLUB;
        } else if (occupationType.isApplicableFor(OrganisationType.CLUBGROUP)) {
            return OrganisationType.CLUBGROUP;
        } else if (occupationType.isApplicableFor(OrganisationType.RHY)) {
            return OrganisationType.RHY;
        }
        throw new IllegalArgumentException("Target occupationType is not supported: " + occupationType);
    }

    private static Map<Organisation, EnumSet<OccupationType>> groupOccupationTypeByOrganisation(
            final List<AnnouncementSubscriber> subscribers) {
        final Map<Organisation, EnumSet<OccupationType>> grouped = subscribers.stream()
                .collect(groupingBy(
                        AnnouncementSubscriber::getOrganisation,
                        Collectors.mapping(AnnouncementSubscriber::getOccupationType,
                                toCollection(() -> EnumSet.noneOf(OccupationType.class)))));

        for (final EnumSet<OccupationType> occupationTypes : grouped.values()) {
            if (occupationTypes.contains(OccupationType.SEURAN_JASEN)) {
                occupationTypes.add(OccupationType.SEURAN_YHDYSHENKILO);

            } else if (occupationTypes.contains(OccupationType.RYHMAN_JASEN)) {
                occupationTypes.add(OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
            }
        }

        return grouped;
    }

    private static BooleanExpression clubOccupationExistsPredicate(final QOccupation OCCUPATION,
                                                                   final QPerson PERSON) {
        // Include group occupation only if valid club occupation is found.
        // This is done to exclude all invited group members who have not yet accepted club invitation.
        final QOccupation CLUB_OCCUPATION = new QOccupation("club_occ");

        return JPAExpressions.selectOne()
                .from(CLUB_OCCUPATION)
                .where(CLUB_OCCUPATION.validAndNotDeleted(),
                        CLUB_OCCUPATION.organisation.eq(OCCUPATION.organisation.parentOrganisation),
                        CLUB_OCCUPATION.person.eq(PERSON))
                .exists();
    }

    private static BooleanExpression organisationPredicate(final Organisation organisation,
                                                           final OrganisationType occupationOrganisationType,
                                                           final QOccupation OCCUPATION) {
        switch (occupationOrganisationType) {
            case CLUB:
                return organisationPredicateForClubOccupation(organisation, OCCUPATION);
            case RHY:
                return organisationPredicateForRhyOccupation(organisation, OCCUPATION);
            case CLUBGROUP:
                return organisationPredicateForGroupOccupation(organisation, OCCUPATION);
            default:
                return OCCUPATION.organisation.eq(organisation);
        }
    }

    private static BooleanExpression organisationPredicateForRhyOccupation(final Organisation organisation,
                                                                           final QOccupation OCCUPATION) {
        switch (organisation.getOrganisationType()) {
            case RHY:
                return OCCUPATION.organisation.eq(organisation);
            case RKA:
                return OCCUPATION.organisation.in(findRhyForRka(organisation));
            case RK:
                return null;
            default:
                throw new IllegalArgumentException("Invalid organisationType: " +
                        organisation.getOrganisationType());
        }
    }

    private static BooleanExpression organisationPredicateForClubOccupation(final Organisation organisation,
                                                                            final QOccupation OCCUPATION) {
        switch (organisation.getOrganisationType()) {
            case CLUB:
                return OCCUPATION.organisation.eq(organisation);
            case RHY:
                return OCCUPATION.organisation.in(findClubsForRhy(organisation));
            case RKA:
                return OCCUPATION.organisation.in(findClubsForRka(organisation));
            case RK:
                return null;
            default:
                throw new IllegalArgumentException("Invalid organisationType: " +
                        organisation.getOrganisationType());
        }
    }

    private static BooleanExpression organisationPredicateForGroupOccupation(final Organisation organisation,
                                                                             final QOccupation OCCUPATION) {
        switch (organisation.getOrganisationType()) {
            case CLUB:
                return OCCUPATION.organisation.in(findGroupsForClub(organisation));
            case RHY:
                return OCCUPATION.organisation.in(findGroupsForRhy(organisation));
            case RKA:
                return OCCUPATION.organisation.in(findGroupsForRka(organisation));
            case RK:
                return null;
            default:
                throw new IllegalArgumentException("Invalid organisationType: " +
                        organisation.getOrganisationType());
        }
    }

    // CLUB

    private static JPQLQuery<Organisation> findGroupsForClub(final Organisation club) {
        Preconditions.checkArgument(club.getOrganisationType() == OrganisationType.CLUB);
        final QOrganisation GROUP = new QOrganisation("club_group");

        return JPAExpressions.selectFrom(GROUP)
                .where(GROUP.organisationType.eq(OrganisationType.CLUBGROUP),
                        GROUP.parentOrganisation.eq(club));
    }

    // RHY

    private static JPQLQuery<Organisation> findClubsForRhy(final Organisation rhy) {
        Preconditions.checkArgument(rhy.getOrganisationType() == OrganisationType.RHY);
        final QOrganisation CLUB = new QOrganisation("rhy_club");

        return JPAExpressions.selectFrom(CLUB)
                .where(CLUB.organisationType.eq(OrganisationType.CLUB),
                        CLUB.parentOrganisation.eq(rhy));
    }

    private static JPQLQuery<Organisation> findGroupsForRhy(final Organisation rhy) {
        Preconditions.checkArgument(rhy.getOrganisationType() == OrganisationType.RHY);
        final QOrganisation CLUBGROUP = new QOrganisation("rhy_group");

        return JPAExpressions.selectFrom(CLUBGROUP)
                .where(CLUBGROUP.organisationType.eq(OrganisationType.CLUBGROUP),
                        CLUBGROUP.parentOrganisation.in(findClubsForRhy(rhy)));
    }

    // RKA

    private static JPQLQuery<Organisation> findRhyForRka(final Organisation rka) {
        Preconditions.checkArgument(rka.getOrganisationType() == OrganisationType.RKA);
        final QOrganisation RHY = new QOrganisation("rka_rhy");

        return JPAExpressions.selectFrom(RHY)
                .where(RHY.organisationType.eq(OrganisationType.RHY),
                        RHY.parentOrganisation.eq(rka));
    }

    private static JPQLQuery<Organisation> findClubsForRka(final Organisation rka) {
        Preconditions.checkArgument(rka.getOrganisationType() == OrganisationType.RKA);
        final QOrganisation CLUB = new QOrganisation("rka_club");

        return JPAExpressions.selectFrom(CLUB)
                .where(CLUB.organisationType.eq(OrganisationType.CLUB),
                        CLUB.parentOrganisation.in(findRhyForRka(rka)));
    }

    private static JPQLQuery<Organisation> findGroupsForRka(final Organisation rka) {
        Preconditions.checkArgument(rka.getOrganisationType() == OrganisationType.RKA);
        final QOrganisation GROUP = new QOrganisation("rka_group");

        return JPAExpressions.selectFrom(GROUP)
                .where(GROUP.organisationType.eq(OrganisationType.CLUBGROUP),
                        GROUP.parentOrganisation.in(findClubsForRka(rka)));
    }
}

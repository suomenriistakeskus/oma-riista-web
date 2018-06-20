package fi.riista.feature.announcement.show;

import com.google.common.base.Preconditions;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import fi.riista.feature.announcement.QAnnouncementSubscriber;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;

/**
 * Process active person occupations to resolve which announcements the person is subscribed to.
 *
 * 1) Child organisation occupation type is inherited by all parents
 * eg. club membership is unfolded to:
 * RK   -> CLUB_MEMBER
 * RKA  -> CLUB_MEMBER
 * RHY  -> CLUB_MEMBER
 * CLUB -> CLUB_MEMBER
 *
 * 2) If organisation filter is specified, then only include subscribers in occupation tree
 * which are equal or above given organisation.
 *
 * 3) Allow coordinator to see all communications received from moderators.
 *
 * 4) Allow club contact person to see all club messages for parents.
 */
public class PersonOccupationGraph {
    private final List<Occupation> activeOccupations;
    private Set<Organisation> organisationFilter;

    public PersonOccupationGraph(final List<Occupation> activeOccupations) {
        this.activeOccupations = Objects.requireNonNull(activeOccupations, "activeOccupations is null");
    }

    public PersonOccupationGraph withOrganisationFilter(final Organisation organisation) {
        this.organisationFilter = organisation != null ? organisation.getAllParentsAndSelf() : null;
        return this;
    }

    public Optional<Predicate> buildPredicate(final QAnnouncementSubscriber subscriber) {
        final Map<Organisation, Set<OccupationType>> childOccupationMapping = activeOccupations.stream()
                .flatMap(occupation -> occupation.getOrganisation().getAllParentsAndSelf().stream()
                        .map(parent -> new BasicOccupationItem(parent, occupation.getOccupationType())))
                .collect(groupingBy(BasicOccupationItem::getOrganisation,
                        mapping(BasicOccupationItem::getOccupationType, toSet())));

        final List<Predicate> predicateList = childOccupationMapping.entrySet().stream()
                .filter(entry -> organisationFilter == null || organisationFilter.contains(entry.getKey()))
                .map(entry -> {
                    final Organisation organisation = entry.getKey();
                    final Set<OccupationType> childOccupationTypes = entry.getValue();

                    if (childOccupationTypes.contains(OccupationType.TOIMINNANOHJAAJA)) {
                        // Coordinator should see any message sent by parent organisations or self
                        return subscriber.organisation.eq(organisation);

                    } else if (childOccupationTypes.contains(OccupationType.SEURAN_YHDYSHENKILO)) {
                        childOccupationTypes.addAll(OccupationType.clubValues());

                        // Club contact person should see all club communication
                        return subscriber.organisation.eq(organisation)
                                .and(subscriber.occupationType.in(childOccupationTypes));

                    } else {
                        Preconditions.checkArgument(!childOccupationTypes.isEmpty(), "should not be empty");
                        return subscriber.organisation.eq(organisation)
                                .and(subscriber.occupationType.in(childOccupationTypes));
                    }
                })
                .collect(Collectors.toList());

        return predicateList.isEmpty() ? Optional.empty() : Optional.ofNullable(ExpressionUtils.anyOf(predicateList));
    }

    private static class BasicOccupationItem {
        private final Organisation organisation;
        private final OccupationType occupationType;

        private BasicOccupationItem(final Organisation organisation, final OccupationType occupationType) {
            this.organisation = Objects.requireNonNull(organisation);
            this.occupationType = Objects.requireNonNull(occupationType);
        }

        public Organisation getOrganisation() {
            return organisation;
        }

        public OccupationType getOccupationType() {
            return occupationType;
        }
    }
}

package fi.riista.feature.pub.occupation;

import com.google.common.base.Preconditions;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.Organisation_;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.occupation.Occupation_;
import fi.riista.util.DateUtil;
import fi.riista.util.jpa.JpaPreds;
import org.springframework.data.jpa.domain.Specification;

import javax.annotation.Nonnull;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.LinkedList;
import java.util.List;

public final class PublicOccupationSearchParameters {
    private final String areaId;
    private final String rhyId;
    private final OrganisationType organisationType;
    private final OccupationType occupationType;

    private PublicOccupationSearchParameters(final String areaId,
                                             final String rhyId,
                                             final OrganisationType organisationType,
                                             final OccupationType occupationType) {
        this.areaId = areaId;
        this.rhyId = rhyId;
        this.organisationType = organisationType;
        this.occupationType = occupationType;
    }

    @Nonnull
    public Specification<Occupation> toJpaSpecification() {
        return (root, query, cb) -> {
            final List<Predicate> predicateList = getPredicates(root, cb);
            return cb.and(predicateList.toArray(new Predicate[predicateList.size()]));
        };
    }

    @Nonnull
    private List<Predicate> getPredicates(final Root<Occupation> root, final CriteriaBuilder cb) {
        final Join<Occupation, Organisation> organisationJoin = root.join(Occupation_.organisation);
        final Path<OccupationType> occupationTypePath = root.get(Occupation_.occupationType);
        final List<Predicate> predicateList = new LinkedList<>();

        predicateList.add(JpaPreds.withinInterval(cb,
                root.get(Occupation_.beginDate),
                root.get(Occupation_.endDate),
                DateUtil.today()));

        if (occupationType != null) {
            predicateList.add(cb.equal(occupationTypePath, occupationType));
        } else {
            predicateList.add(cb.not(JpaPreds.inCollection(cb, occupationTypePath, OccupationType.clubValues())));
        }

        if (organisationType != null) {
            predicateList.add(cb.equal(organisationJoin.get(Organisation_.organisationType), organisationType));
        }

        if (rhyId != null) {
            predicateList.add(organisationEqual(organisationJoin, cb, rhyId, OrganisationType.RHY));

        } else if (areaId != null) {
            final Join<Organisation, Organisation> parentJoin =
                    organisationJoin.join(Organisation_.parentOrganisation);

            predicateList.add(cb.or(
                    organisationEqual(organisationJoin, cb, areaId, OrganisationType.RKA),
                    organisationEqual(parentJoin, cb, areaId, OrganisationType.RKA)));
        }

        return predicateList;
    }

    private static Predicate organisationEqual(final Path<Organisation> organisationJoin,
                                               final CriteriaBuilder cb,
                                               final String officialCode,
                                               final OrganisationType organisationType) {
        final Path<OrganisationType> organisationTypePath = organisationJoin.get(Organisation_.organisationType);
        final Path<String> organisationCodePath = organisationJoin.get(Organisation_.officialCode);

        return cb.and(
                cb.equal(organisationTypePath, organisationType),
                cb.equal(organisationCodePath, officialCode));
    }

    public String getAreaId() {
        return areaId;
    }

    public String getRhyId() {
        return rhyId;
    }

    public OrganisationType getOrganisationType() {
        return organisationType;
    }

    public OccupationType getOccupationType() {
        return occupationType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String areaId;
        private String rhyId;
        private OrganisationType organisationType;
        private OccupationType occupationType;

        private Builder() {
        }

        public Builder withAreaId(final String areaId) {
            this.areaId = areaId;
            return this;
        }

        public Builder withRhyId(final String rhyId) {
            this.rhyId = rhyId;
            return this;
        }

        public Builder withOrganisationType(final OrganisationType organisationType) {
            this.organisationType = organisationType;
            return this;
        }

        public Builder withOccupationType(final OccupationType occupationType) {
            this.occupationType = occupationType;
            return this;
        }

        public PublicOccupationSearchParameters build() {
            if (organisationType == null && occupationType == null && rhyId == null && areaId == null) {
                throw new IllegalArgumentException("Missing parameters");
            }

            if (organisationType != null || occupationType != null) {
                Preconditions.checkArgument(organisationType != null && occupationType != null,
                        "organisationType and occupationType must be both given or neither");
            }

            if (occupationType != null) {
                Preconditions.checkArgument(!OccupationType.clubValues().contains(occupationType),
                        "Invalid occupationType");
            }

            if (organisationType != null) {
                Preconditions.checkArgument(organisationType.allowListOccupations(),
                        "Invalid organisationType");
            }

            return new PublicOccupationSearchParameters(areaId, rhyId, organisationType, occupationType);
        }
    }
}

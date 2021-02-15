package fi.riista.feature.pub.occupation;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.QRiistakeskuksenAlue;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.LocalDate;

import java.util.Collection;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

public final class PublicOccupationSearchParameters {

    private static final QOccupation OCCUPATION = QOccupation.occupation;
    private static final QRiistakeskuksenAlue RKA = QRiistakeskuksenAlue.riistakeskuksenAlue;
    private static final QOrganisation ORG = QOrganisation.organisation;

    private final String areaId;
    private final Collection<String> rhyIds;
    private final OrganisationType organisationType;
    private final ImmutableSet<OccupationType> occupationTypes;
    private final Integer pageSize;
    private final Integer pageNumber;

    private PublicOccupationSearchParameters(final String areaId,
                                             final Collection<String> rhyIds,
                                             final OrganisationType organisationType,
                                             final Collection<OccupationType> occupationTypes,
                                             final Integer pageSize,
                                             final Integer pageNumber) {
        this.areaId = areaId;
        this.rhyIds = ofNullable(rhyIds).orElse(emptyList());
        this.organisationType = organisationType;
        this.occupationTypes = occupationTypes != null ? ImmutableSet.copyOf(occupationTypes) : ImmutableSet.of();
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
    }


    public Predicate toQueryDslPredicate() {
        return isPresentOccupationPredicate().and(areaPredicate()).and(rhyPredicate()).and(areaPredicate())
                .and(organisationTypePredicate()).and(occupationTypePredicate());
    }

    private static BooleanExpression isPresentOccupationPredicate() {
        final LocalDate today = DateUtil.today();

        final BooleanExpression beginExpression = OCCUPATION.beginDate.loe(today).or(OCCUPATION.beginDate.isNull());
        final BooleanExpression endExpression = OCCUPATION.endDate.goe(today).or(OCCUPATION.endDate.isNull());
        return beginExpression.and(endExpression);
    }

    private BooleanExpression areaPredicate() {
        if (rhyIds.isEmpty() && areaId != null) {
            // If only rka is selected as search criteria, match events for rhys that have
            // the specified rka as their parent organisation.
            return OCCUPATION.organisation.eq(ORG).
                    and(ORG.parentOrganisation.isNotNull().and(ORG.parentOrganisation.eq(RKA._super)))
                    .and(RKA.officialCode.eq(areaId));
        }

        return null;

    }

    private BooleanExpression rhyPredicate() {
        return rhyIds.isEmpty()
                ? OCCUPATION.occupationType.notIn(OccupationType.clubValues())
                : OCCUPATION.organisation.eq(ORG).and(ORG.organisationType.eq(OrganisationType.RHY))
                .and(ORG.officialCode.in(rhyIds));
    }

    private final BooleanExpression organisationTypePredicate() {
        return organisationType == null
                ? null
                : OCCUPATION.organisation.organisationType.eq(organisationType);
    }

    private final BooleanExpression occupationTypePredicate() {
        return F.isNullOrEmpty(occupationTypes)
                ? null
                : OCCUPATION.occupationType.in(occupationTypes);
    }


    public String getAreaId() {
        return areaId;
    }

    public Collection<String> getRhyIds() {
        return rhyIds;
    }

    public OrganisationType getOrganisationType() {
        return organisationType;
    }

    public ImmutableSet<OccupationType> getOccupationTypes() {
        return occupationTypes;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String areaId;
        private Collection<String> rhyIds;
        private OrganisationType organisationType;
        private Collection<OccupationType> occupationTypes;


        private Integer pageSize;
        private Integer pageNumber;

        private Builder() {
        }

        public Builder withAreaId(final String areaId) {
            this.areaId = areaId;
            return this;
        }

        public Builder withRhyIds(final Collection<String> rhyIds) {
            this.rhyIds = rhyIds;
            return this;
        }

        public Builder withOrganisationType(final OrganisationType organisationType) {
            this.organisationType = organisationType;
            return this;
        }

        public Builder withOccupationType(final Collection<OccupationType> occupationTypes) {
            this.occupationTypes = occupationTypes;
            return this;
        }

        public Builder withPageSize(final Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder withPageNumber(final Integer pageNumber) {
            this.pageNumber = pageNumber;
            return this;
        }
        public PublicOccupationSearchParameters build() {
            if (organisationType == null && F.isNullOrEmpty(occupationTypes) && rhyIds == null &&
                    areaId == null && pageSize == null && pageNumber == null) {
                throw new IllegalArgumentException("Missing parameters");
            }

            if (pageSize != null || pageNumber != null) {
                Preconditions.checkArgument(pageSize != null && pageNumber != null,
                        "pageSize and pageNumber must be both given or neither");
            }

            if (organisationType != null || !F.isNullOrEmpty(occupationTypes)) {
                Preconditions.checkArgument(organisationType != null && !F.isNullOrEmpty(occupationTypes),
                        "organisationType and occupationType must be both given or neither");
            }

            if (!F.isNullOrEmpty(occupationTypes)) {
                Preconditions.checkArgument(!OccupationType.clubValues().contains(occupationTypes),
                        "Invalid occupationType");
            }

            if (organisationType != null) {
                Preconditions.checkArgument(organisationType.allowListOccupations(),
                        "Invalid organisationType");
            }

            return new PublicOccupationSearchParameters(areaId, rhyIds, organisationType, occupationTypes, pageSize, pageNumber);
        }
    }
}

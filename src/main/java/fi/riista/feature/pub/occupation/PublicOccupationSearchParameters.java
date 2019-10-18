package fi.riista.feature.pub.occupation;

import com.google.common.base.Preconditions;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.QRiistakeskuksenAlue;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;


public final class PublicOccupationSearchParameters {

    private static final QOccupation OCCUPATION = QOccupation.occupation;
    private static final QRiistakeskuksenAlue RKA = QRiistakeskuksenAlue.riistakeskuksenAlue;
    private static final QOrganisation ORG = QOrganisation.organisation;

    private final String areaId;
    private final String rhyId;
    private final OrganisationType organisationType;
    private final OccupationType occupationType;
    private final Integer pageSize;
    private final Integer pageNumber;

    private PublicOccupationSearchParameters(final String areaId,
                                             final String rhyId,
                                             final OrganisationType organisationType,
                                             final OccupationType occupationType,
                                             final Integer pageSize,
                                             final Integer pageNumber) {
        this.areaId = areaId;
        this.rhyId = rhyId;
        this.organisationType = organisationType;
        this.occupationType = occupationType;
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
    }


    public Predicate toQueryDslPredicate() {
        return isPresentOccupationPredicate().and(areaPredicate()).and(rhyPredicate()).and(areaPredicate())
                .and(organisationTypePredicate()).and(occupationTypePredicate());
    }

    private BooleanExpression isPresentOccupationPredicate() {
        final LocalDate today = DateUtil.today();

        final BooleanExpression beginExpression = OCCUPATION.beginDate.loe(today).or(OCCUPATION.beginDate.isNull());
        final BooleanExpression endExpression = OCCUPATION.endDate.goe(today).or(OCCUPATION.endDate.isNull());
        return beginExpression.and(endExpression);
    }

    private BooleanExpression areaPredicate() {
        if (rhyId == null && areaId != null) {
            // If only rka is selected as search criteria, match events for rhys that have
            // the specified rka as their parent organisation.
            return OCCUPATION.organisation.eq(ORG).
                    and(ORG.parentOrganisation.isNotNull().and(ORG.parentOrganisation.eq(RKA._super)))
                    .and(RKA.officialCode.eq(areaId));
        }

        return null;

    }

    private BooleanExpression rhyPredicate() {
        return rhyId == null
                ? OCCUPATION.occupationType.notIn(OccupationType.clubValues())
                : OCCUPATION.organisation.eq(ORG).and(ORG.organisationType.eq(OrganisationType.RHY))
                .and(ORG.officialCode.eq(rhyId));
    }

    private final BooleanExpression organisationTypePredicate() {
        return organisationType == null
                ? null
                : OCCUPATION.organisation.organisationType.eq(organisationType);
    }

    private final BooleanExpression occupationTypePredicate() {
        return occupationType == null
                ? null
                : OCCUPATION.occupationType.eq(occupationType);
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
        private String rhyId;
        private OrganisationType organisationType;
        private OccupationType occupationType;


        private Integer pageSize;
        private Integer pageNumber;

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

        public Builder withPageSize(final Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder withPageNumber(final Integer pageNumber) {
            this.pageNumber = pageNumber;
            return this;
        }
        public PublicOccupationSearchParameters build() {
            if (organisationType == null && occupationType == null && rhyId == null &&
                    areaId == null && pageSize == null && pageNumber == null) {
                throw new IllegalArgumentException("Missing parameters");
            }

            if (pageSize != null || pageNumber != null) {
                Preconditions.checkArgument(pageSize != null && pageNumber != null,
                        "pageSize and pageNumber must be both given or neither");
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

            return new PublicOccupationSearchParameters(areaId, rhyId, organisationType, occupationType, pageSize, pageNumber);
        }
    }
}

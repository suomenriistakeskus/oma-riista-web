package fi.riista.feature.organization.person;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import fi.riista.feature.organization.OrganisationType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;

import static fi.riista.feature.organization.OrganisationType.CLUB;
import static fi.riista.feature.organization.OrganisationType.CLUBGROUP;
import static java.util.Objects.requireNonNull;

public enum ContactInfoShare {
    ONLY_OFFICIALS(CLUB),
    ALL_MEMBERS(CLUB),
    SAME_PERMIT_LEVEL(CLUBGROUP),
    RHY_LEVEL(CLUBGROUP);

    private final ImmutableSet<OrganisationType> applicableOrganisationTypes;

    ContactInfoShare(@Nonnull final OrganisationType orgType, @Nonnull final OrganisationType... moreOrgTypes) {
        requireNonNull(orgType, "orgType is null");
        requireNonNull(moreOrgTypes, "moreOrgTypes is null");
        this.applicableOrganisationTypes = Sets.immutableEnumSet(orgType, moreOrgTypes);
    }

    public EnumSet<OrganisationType> getApplicableOrganisationTypes() {
        return EnumSet.copyOf(applicableOrganisationTypes);
    }

    public boolean isApplicableFor(@Nullable final OrganisationType organisationType) {
        return applicableOrganisationTypes.contains(organisationType);
    }
}

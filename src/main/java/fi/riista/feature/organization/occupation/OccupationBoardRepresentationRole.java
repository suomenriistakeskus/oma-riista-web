package fi.riista.feature.organization.occupation;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.util.F;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;

import static fi.riista.feature.organization.OrganisationType.ARN;
import static fi.riista.feature.organization.OrganisationType.RHY;
import static fi.riista.feature.organization.OrganisationType.RK;
import static fi.riista.feature.organization.OrganisationType.VRN;
import static java.util.Objects.requireNonNull;

public enum OccupationBoardRepresentationRole {
    METSAHALLITUKSEN_EDUSTAJA(RHY, VRN),
    MAANOMISTAJIEN_EDUSTAJA(RHY),
    METSAHALLITUKSEN_VARAEDUSTAJA(RHY),
    MAANOMISTAJIEN_VARAEDUSTAJA(RHY),

    RH_MAKSU_EDUSTAJA(RK),
    MMM_EDUSTAJA(RK, VRN),
    LUKE_EDUSTAJA(RK, VRN),
    JARJESTO_EDUSTAJA(RK, VRN),
    HENKILOSTO_EDUSTAJA(RK),

    ALUEKOKOUKSEN_EDUSTAJA(VRN),
    YMPARISTOMINISTERION_EDUSTAJA(VRN),
    METSATALOUS_EDUSTAJA(VRN),
    METSASTYS_EDUSTAJA(VRN),
    LUONNONSUOJELU_EDUSTAJA(VRN),

    ALUEKOKOUKSEN_JASEN(ARN),
    MAAKUNTALIITON_JASEN(ARN),
    ELY_KESKUKSEN_EDUSTAJA(ARN),
    METSAKESKUKSEN_EDUSTAJA(ARN),
    ALUEELLISEN_MAANOMISTAJAN_EDUSTAJA(ARN);

    private final ImmutableSet<OrganisationType> applicableOrganisationTypes;

    OccupationBoardRepresentationRole(@Nonnull final OrganisationType orgType, @Nonnull final OrganisationType... moreOrgTypes) {
        requireNonNull(orgType, "orgType is null");
        requireNonNull(moreOrgTypes, "moreOrgTypes is null");
        this.applicableOrganisationTypes = Sets.immutableEnumSet(orgType, moreOrgTypes);
    }

    public static EnumSet<OccupationBoardRepresentationRole> getApplicableRoles(@Nullable final OrganisationType organisationType) {
        return organisationType == null
                ? EnumSet.noneOf(OccupationBoardRepresentationRole.class)
                : F.filterToEnumSet(OccupationBoardRepresentationRole.class, role -> role.isApplicableFor(organisationType));
    }

    public boolean isApplicableFor(@Nullable final OrganisationType organisationType) {
        return applicableOrganisationTypes.contains(organisationType);
    }
}

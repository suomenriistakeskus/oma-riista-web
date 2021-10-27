package fi.riista.feature.organization.occupation;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.util.F;
import fi.riista.util.LocalisedEnum;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import static fi.riista.feature.organization.OrganisationType.ARN;
import static fi.riista.feature.organization.OrganisationType.CLUB;
import static fi.riista.feature.organization.OrganisationType.CLUBGROUP;
import static fi.riista.feature.organization.OrganisationType.RHY;
import static fi.riista.feature.organization.OrganisationType.RK;
import static fi.riista.feature.organization.OrganisationType.VRN;
import static java.util.Objects.requireNonNull;

public enum OccupationType implements LocalisedEnum {

    TOIMINNANOHJAAJA(RHY),
    SRVA_YHTEYSHENKILO(RHY),
    PETOYHDYSHENKILO(RHY),
    METSASTYKSENVALVOJA(RHY),
    METSASTAJATUTKINNON_VASTAANOTTAJA(RHY),
    AMPUMAKOKEEN_VASTAANOTTAJA(RHY),
    RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA(RHY),
    METSASTAJATUTKINTOON_VALMENTAVAN_KOULUTUKSEN_KOULUTTAJA(RHY),
    ALUEKOKOUKSEN_EDUSTAJA(RHY),
    ALUEKOKOUKSEN_VARAEDUSTAJA(RHY),
    PUHEENJOHTAJA(RK, VRN, ARN, RHY),
    VARAPUHEENJOHTAJA(RK, VRN, ARN, RHY),
    HALLITUKSEN_JASEN(RK, RHY),
    HALLITUKSEN_VARAJASEN(RK, RHY),
    JASEN(ARN, VRN),
    VARAJASEN(ARN, VRN),
    JALJESTYSKOIRAN_OHJAAJA_HIRVI(RHY),
    JALJESTYSKOIRAN_OHJAAJA_PIENET_HIRVIELAIMET(RHY),
    JALJESTYSKOIRAN_OHJAAJA_SUURPEDOT(RHY),
    SEURAN_YHDYSHENKILO(CLUB),
    SEURAN_JASEN(CLUB),
    RYHMAN_METSASTYKSENJOHTAJA(CLUBGROUP),
    RYHMAN_JASEN(CLUBGROUP);

    private static final ImmutableSet<OccupationType> ROLE_VALUES = Sets.immutableEnumSet(
            TOIMINNANOHJAAJA, SRVA_YHTEYSHENKILO, AMPUMAKOKEEN_VASTAANOTTAJA, SEURAN_YHDYSHENKILO, SEURAN_JASEN, RYHMAN_METSASTYKSENJOHTAJA);

    private static final ImmutableSet<OccupationType> BOARD_VALUES = Sets.immutableEnumSet(
            PUHEENJOHTAJA, VARAPUHEENJOHTAJA, HALLITUKSEN_JASEN, HALLITUKSEN_VARAJASEN, JASEN, VARAJASEN);

    private static final ImmutableSet<OccupationType> CLUB_VALUES = Sets.immutableEnumSet(
            SEURAN_JASEN, SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA, RYHMAN_JASEN);

    private static final ImmutableSet<OccupationType> VALID_JHT_OCCUPATION_TYPE = Sets.immutableEnumSet(
            AMPUMAKOKEEN_VASTAANOTTAJA,
            METSASTAJATUTKINNON_VASTAANOTTAJA,
            METSASTYKSENVALVOJA,
            RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA);

    private final ImmutableSet<OrganisationType> applicableOrganisationTypes;

    OccupationType(@Nonnull final OrganisationType orgType, @Nonnull final OrganisationType... moreOrgTypes) {
        requireNonNull(orgType, "orgType is null");
        requireNonNull(moreOrgTypes, "moreOrgTypes is null");
        this.applicableOrganisationTypes = Sets.immutableEnumSet(orgType, moreOrgTypes);
    }

    public static EnumSet<OccupationType> clubValues() {
        return EnumSet.copyOf(CLUB_VALUES);
    }

    public static Set<OccupationType> rhyValues() {
        return Arrays.stream(OccupationType.values())
                .filter(o -> o.isApplicableFor(OrganisationType.RHY))
                .collect(Collectors.toSet());
    }

    public static EnumSet<OccupationType> jhtValues() {
        return EnumSet.copyOf(VALID_JHT_OCCUPATION_TYPE);
    }

    public static EnumSet<OccupationType> boardValues() {
        return EnumSet.copyOf(BOARD_VALUES);
    }

    public static OccupationType[] applicableValuesFor(@Nullable final OrganisationType organisationType) {
        return Iterables.toArray(getApplicableTypes(organisationType), OccupationType.class);
    }

    public static EnumSet<OccupationType> getApplicableTypes(@Nullable final OrganisationType organisationType) {
        return organisationType == null
                ? EnumSet.noneOf(OccupationType.class)
                : F.filterToEnumSet(OccupationType.class, occType -> occType.isApplicableFor(organisationType));
    }

    public static boolean hasApplicableValuesFor(@Nullable final OrganisationType organisationType) {
        return !getApplicableTypes(organisationType).isEmpty();
    }

    public static boolean isValidJhtOccupationType(@Nonnull final OccupationType occupationType) {
        return VALID_JHT_OCCUPATION_TYPE.contains(requireNonNull(occupationType, "occupationType is null"));
    }

    public EnumSet<OrganisationType> getApplicableOrganisationTypes() {
        return EnumSet.copyOf(applicableOrganisationTypes);
    }

    public boolean isApplicableFor(@Nullable final OrganisationType organisationType) {
        return applicableOrganisationTypes.contains(organisationType);
    }

    public boolean isBoardSpecific() {
        return BOARD_VALUES.contains(this);
    }

    public boolean isClubOrGroupOccupation() {
        return CLUB_VALUES.contains(this);
    }

    public boolean isRhyOccupation() {
        return isApplicableFor(RHY);
    }

    public boolean isMappedToRole() {
        return ROLE_VALUES.contains(this);
    }

    public boolean isJHTOccupation() {
        return VALID_JHT_OCCUPATION_TYPE.contains(this);
    }

    public boolean isCallOrderPossible() {
        return this == SRVA_YHTEYSHENKILO || this == PETOYHDYSHENKILO;
    }
}

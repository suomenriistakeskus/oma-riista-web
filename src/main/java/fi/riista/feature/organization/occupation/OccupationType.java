package fi.riista.feature.organization.occupation;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.integration.lupahallinta.model.LH_PositionType;
import fi.riista.util.F;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import static fi.riista.feature.organization.OrganisationType.ARN;
import static fi.riista.feature.organization.OrganisationType.CLUB;
import static fi.riista.feature.organization.OrganisationType.CLUBGROUP;
import static fi.riista.feature.organization.OrganisationType.RHY;
import static fi.riista.feature.organization.OrganisationType.RK;
import static fi.riista.feature.organization.OrganisationType.VRN;

public enum OccupationType {

    TOIMINNANOHJAAJA(1, LH_PositionType.TOIMINNANOHJAAJA, EnumSet.of(RHY)),

    SRVA_YHTEYSHENKILO(2, LH_PositionType.SRVA___YHTEYSHENKILO, EnumSet.of(RHY)),

    PETOYHDYSHENKILO(3, LH_PositionType.PETOYHDYSHENKILO, EnumSet.of(RHY)),

    METSASTYKSENVALVOJA(4, LH_PositionType.METSASTYKSENVALVOJA, EnumSet.of(RHY)),

    METSASTAJATUTKINNON_VASTAANOTTAJA(5, LH_PositionType.METSASTAJATUTKINNON___VASTAANOTTAJA, EnumSet.of(RHY)),

    AMPUMAKOKEEN_VASTAANOTTAJA(6, LH_PositionType.AMPUMAKOKEEN___VASTAANOTTAJA, EnumSet.of(RHY)),

    RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA(7,
            LH_PositionType.RHYN___EDUSTAJA___RIISTAVAHINKOJEN___MAASTOKATSELMUKSESSA,
            EnumSet.of(RHY)),

    METSASTAJATUTKINTOON_VALMENTAVAN_KOULUTUKSEN_KOULUTTAJA(8,
            LH_PositionType.METSASTAJATUTKINTOON___VALMENTAVAN___KOULUTUKSEN___KOULUTTAJA,
            EnumSet.of(RHY)),

    PUHEENJOHTAJA(9, LH_PositionType.PUHEENJOHTAJA, EnumSet.of(RK, VRN, ARN, RHY)),

    VARAPUHEENJOHTAJA(10, LH_PositionType.VARAPUHEENJOHTAJA, EnumSet.of(RK, VRN, ARN, RHY)),

    HALLITUKSEN_JASEN(11, LH_PositionType.HALLITUKSEN___JASEN, EnumSet.of(RK, RHY)),

    HALLITUKSEN_VARAJASEN(12, LH_PositionType.HALLITUKSEN___VARAJASEN, EnumSet.of(RK, RHY)),

    JASEN(13, LH_PositionType.HALLITUKSEN___JASEN, EnumSet.of(ARN, VRN)),

    VARAJASEN(14, LH_PositionType.HALLITUKSEN___VARAJASEN, EnumSet.of(ARN, VRN)),

    JALJESTYSKOIRAN_OHJAAJA_HIRVI(15, LH_PositionType.JALJESTYSKOIRAN___OHJAAJA___HIRVI, EnumSet.of(RHY)),

    JALJESTYSKOIRAN_OHJAAJA_PIENET_HIRVIELAIMET(16,
            LH_PositionType.JALJESTYSKOIRAN___OHJAAJA___PIENET___HIRVIELAIMET,
            EnumSet.of(RHY)),

    JALJESTYSKOIRAN_OHJAAJA_SUURPEDOT(17, LH_PositionType.JALJESTYSKOIRAN___OHJAAJA___SUURPEDOT, EnumSet.of(RHY)),

    SEURAN_YHDYSHENKILO(18, EnumSet.of(CLUB)),

    SEURAN_JASEN(19, EnumSet.of(CLUB)),

    RYHMAN_METSASTYKSENJOHTAJA(20, EnumSet.of(CLUBGROUP)),

    RYHMAN_JASEN(21, EnumSet.of(CLUBGROUP));

    private static final ImmutableSet<OccupationType> ROLE_VALUES = Sets.immutableEnumSet(
            TOIMINNANOHJAAJA, SRVA_YHTEYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA, SEURAN_YHDYSHENKILO, SEURAN_JASEN);

    private static final ImmutableSet<OccupationType> BOARD_VALUES = Sets.immutableEnumSet(
            PUHEENJOHTAJA, VARAPUHEENJOHTAJA, HALLITUKSEN_JASEN, HALLITUKSEN_VARAJASEN, JASEN, VARAJASEN);

    private static final ImmutableSet<OccupationType> CLUB_VALUES = Sets.immutableEnumSet(
            SEURAN_JASEN, SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA, RYHMAN_JASEN);

    private static final EnumSet<OccupationType> VALID_JHT_OCCUPATION_TYPE = EnumSet.of(
            OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA,
            OccupationType.METSASTAJATUTKINNON_VASTAANOTTAJA,
            OccupationType.METSASTYKSENVALVOJA,
            OccupationType.RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA);

    private final int order;

    private final LH_PositionType exportType;

    private final ImmutableSet<OrganisationType> applicableOrganisationTypes;

    OccupationType(final int order, @Nonnull final EnumSet<OrganisationType> organisationTypes) {
        this(order, null, organisationTypes);
    }

    OccupationType(
            final int order,
            @Nullable final LH_PositionType exportType,
            @Nonnull final EnumSet<OrganisationType> organisationTypes) {

        this.order = order;
        this.exportType = exportType;
        this.applicableOrganisationTypes =
                Sets.immutableEnumSet(Objects.requireNonNull(organisationTypes, "organisationTypes must not be null"));
    }

    public static EnumSet<OccupationType> clubValues() {
        return EnumSet.copyOf(CLUB_VALUES);
    }

    public static EnumSet<OccupationType> lupahallintaExportValues() {
        return EnumSet.complementOf(clubValues());
    }

    public static EnumSet<OccupationType> jhtValues() {
        return EnumSet.copyOf(VALID_JHT_OCCUPATION_TYPE);
    }

    public static OccupationType[] applicableValuesFor(@Nullable final OrganisationType organisationType) {
        return Iterables.toArray(getListOfApplicableTypes(organisationType), OccupationType.class);
    }

    public static List<OccupationType> getListOfApplicableTypes(@Nullable final OrganisationType organisationType) {
        return organisationType == null
                ? Collections.emptyList()
                : F.filterToList(OccupationType.class, occType -> occType.isApplicableFor(organisationType));
    }

    public static boolean hasApplicableValuesFor(@Nullable final OrganisationType organisationType) {
        return !getListOfApplicableTypes(organisationType).isEmpty();
    }

    public static boolean isValidJhtOccupationType(final OccupationType occupationType) {
        return VALID_JHT_OCCUPATION_TYPE.contains(Objects.requireNonNull(occupationType, "occupationType is null"));
    }

    public int getOrder() {
        return order;
    }

    public LH_PositionType getExportType() {
        return exportType;
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

    public boolean isClubSpecific() {
        return CLUB_VALUES.contains(this);
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

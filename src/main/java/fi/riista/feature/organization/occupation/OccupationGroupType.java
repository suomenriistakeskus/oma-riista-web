package fi.riista.feature.organization.occupation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.util.Collect;
import fi.riista.util.F;
import fi.riista.util.LocalisedEnum;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.riista.util.Collect.nullSafeGroupingBy;

public enum OccupationGroupType implements LocalisedEnum {
    TOIMINNANOHJAAJA,
    SRVA_YHTEYSHENKILO,
    PETOYHDYSHENKILO,
    METSASTYKSENVALVOJA,
    METSASTAJATUTKINNON_VASTAANOTTAJA,
    AMPUMAKOKEEN_VASTAANOTTAJA,
    RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA,
    METSASTAJATUTKINTOON_VALMENTAVAN_KOULUTUKSEN_KOULUTTAJA,
    PUHEENJOHTAJA,
    VARAPUHEENJOHTAJA,
    HALLITUKSEN_JASEN,
    HALLITUKSEN_VARAJASEN,
    JASEN,
    VARAJASEN,
    JALJESTYSKOIRAN_OHJAAJA_HIRVI,
    JALJESTYSKOIRAN_OHJAAJA_PIENET_HIRVIELAIMET,
    JALJESTYSKOIRAN_OHJAAJA_SUURPEDOT,
    SEURAN_YHDYSHENKILO,
    SEURAN_JASEN,
    RYHMAN_METSASTYKSENJOHTAJA,
    RYHMAN_JASEN,
    HALLITUS;

    private static final ImmutableMap<OccupationGroupType, List<OccupationType>> groupTypeToOccupationTypeMapping;

    static {
        final Map<OccupationGroupType, List<OccupationType>> typesByGroup = Stream.of(OccupationType.values())
                .collect(nullSafeGroupingBy(OccupationGroupType::mapToGroupType));

        groupTypeToOccupationTypeMapping =
                ImmutableMap.<OccupationGroupType, List<OccupationType>>builderWithExpectedSize(typesByGroup.size() + 1)
                        .putAll(typesByGroup)
                        .put(HALLITUS, ImmutableList.copyOf(OccupationType.boardValues()))
                        .build();
    }

    public static final List<OccupationType> getOccupationTypes(final OccupationGroupType groupType) {
        final List<OccupationType> occupationTypes = groupTypeToOccupationTypeMapping.get(groupType);
        return Optional.ofNullable(occupationTypes).orElseGet(() -> ImmutableList.of());
    }

    public static ImmutableSet<OccupationGroupType> getApplicableTypes(@Nullable final OrganisationType organisationType) {
        final EnumSet<OccupationType> occupationTypes = organisationType == null
                ? EnumSet.noneOf(OccupationType.class)
                : F.filterToEnumSet(OccupationType.class, occType -> occType.isApplicableFor(organisationType));
        final EnumSet<OccupationGroupType> groupTypes = occupationTypes.stream()
                .map(OccupationGroupType::mapToGroupType)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(OccupationGroupType.class)));
        if (organisationType == OrganisationType.RHY) {
            groupTypes.add(HALLITUS);
        }

        return ImmutableSet.copyOf(groupTypes);
    }

    private static final OccupationGroupType mapToGroupType(final OccupationType occupationType) {
        switch (occupationType) {
            case TOIMINNANOHJAAJA:
                return TOIMINNANOHJAAJA;
            case SRVA_YHTEYSHENKILO:
                return SRVA_YHTEYSHENKILO;
            case PETOYHDYSHENKILO:
                return PETOYHDYSHENKILO;
            case METSASTYKSENVALVOJA:
                return METSASTYKSENVALVOJA;
            case METSASTAJATUTKINNON_VASTAANOTTAJA:
                return METSASTAJATUTKINNON_VASTAANOTTAJA;
            case AMPUMAKOKEEN_VASTAANOTTAJA:
                return AMPUMAKOKEEN_VASTAANOTTAJA;
            case RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA:
                return RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA;
            case METSASTAJATUTKINTOON_VALMENTAVAN_KOULUTUKSEN_KOULUTTAJA:
                return METSASTAJATUTKINTOON_VALMENTAVAN_KOULUTUKSEN_KOULUTTAJA;
            case PUHEENJOHTAJA:
                return PUHEENJOHTAJA;
            case VARAPUHEENJOHTAJA:
                return VARAPUHEENJOHTAJA;
            case HALLITUKSEN_JASEN:
                return HALLITUKSEN_JASEN;
            case HALLITUKSEN_VARAJASEN:
                return HALLITUKSEN_VARAJASEN;
            case JASEN:
                return JASEN;
            case VARAJASEN:
                return VARAJASEN;
            case JALJESTYSKOIRAN_OHJAAJA_HIRVI:
                return JALJESTYSKOIRAN_OHJAAJA_HIRVI;
            case JALJESTYSKOIRAN_OHJAAJA_PIENET_HIRVIELAIMET:
                return JALJESTYSKOIRAN_OHJAAJA_PIENET_HIRVIELAIMET;
            case JALJESTYSKOIRAN_OHJAAJA_SUURPEDOT:
                return JALJESTYSKOIRAN_OHJAAJA_SUURPEDOT;
            case SEURAN_YHDYSHENKILO:
                return SEURAN_YHDYSHENKILO;
            case SEURAN_JASEN:
                return SEURAN_JASEN;
            case RYHMAN_METSASTYKSENJOHTAJA:
                return RYHMAN_METSASTYKSENJOHTAJA;
            case RYHMAN_JASEN:
                return RYHMAN_JASEN;
            case ALUEKOKOUKSEN_EDUSTAJA:
            case ALUEKOKOUKSEN_VARAEDUSTAJA:
                return null;
            default:
                throw new IllegalArgumentException("Unknown occupation type");

        }
    }
}

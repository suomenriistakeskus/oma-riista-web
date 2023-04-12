package fi.riista.feature.organization.occupation;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.organization.OrganisationType;

import static fi.riista.feature.organization.OrganisationType.ARN;
import static fi.riista.feature.organization.OrganisationType.CLUB;
import static fi.riista.feature.organization.OrganisationType.CLUBGROUP;
import static fi.riista.feature.organization.OrganisationType.RHY;
import static fi.riista.feature.organization.OrganisationType.RK;
import static fi.riista.feature.organization.OrganisationType.VRN;
import static fi.riista.feature.organization.occupation.OccupationContactInfoVisibilityRule.VisibilitySetting.ALWAYS;
import static fi.riista.feature.organization.occupation.OccupationContactInfoVisibilityRule.VisibilitySetting.NEVER;
import static fi.riista.feature.organization.occupation.OccupationContactInfoVisibilityRule.VisibilitySetting.OPTIONAL;
import static fi.riista.feature.organization.occupation.OccupationType.ALUEKOKOUKSEN_EDUSTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.ALUEKOKOUKSEN_VARAEDUSTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.HALLITUKSEN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.HALLITUKSEN_VARAJASEN;
import static fi.riista.feature.organization.occupation.OccupationType.JALJESTYSKOIRAN_OHJAAJA_HIRVI;
import static fi.riista.feature.organization.occupation.OccupationType.JALJESTYSKOIRAN_OHJAAJA_PIENET_HIRVIELAIMET;
import static fi.riista.feature.organization.occupation.OccupationType.JALJESTYSKOIRAN_OHJAAJA_SUURPEDOT;
import static fi.riista.feature.organization.occupation.OccupationType.JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.METSASTAJATUTKINNON_VASTAANOTTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.METSASTAJATUTKINTOON_VALMENTAVAN_KOULUTUKSEN_KOULUTTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.METSASTYKSENVALVOJA;
import static fi.riista.feature.organization.occupation.OccupationType.PETOYHDYSHENKILO;
import static fi.riista.feature.organization.occupation.OccupationType.PUHEENJOHTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA;
import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;
import static fi.riista.feature.organization.occupation.OccupationType.SRVA_YHTEYSHENKILO;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static fi.riista.feature.organization.occupation.OccupationType.VARAJASEN;
import static fi.riista.feature.organization.occupation.OccupationType.VARAPUHEENJOHTAJA;

public class OccupationContactInfoVisibilityRuleMapping {

    private static final ImmutableMap<OrganisationType, ImmutableMap<OccupationType, OccupationContactInfoVisibilityRule>> occupationContactInfoVisibilityRules;
    static {
        final ImmutableMap<OccupationType, OccupationContactInfoVisibilityRule> rhyRules = ImmutableMap.<OccupationType, OccupationContactInfoVisibilityRule>builder()
                .put(TOIMINNANOHJAAJA, OccupationContactInfoVisibilityRule.createRule(ALWAYS, ALWAYS, ALWAYS))
                .put(SRVA_YHTEYSHENKILO, OccupationContactInfoVisibilityRule.createRule(ALWAYS, OPTIONAL, NEVER))
                .put(PETOYHDYSHENKILO, OccupationContactInfoVisibilityRule.createRule(ALWAYS, OPTIONAL, OPTIONAL))
                .put(METSASTYKSENVALVOJA, OccupationContactInfoVisibilityRule.createRule(ALWAYS, OPTIONAL, OPTIONAL))
                .put(METSASTAJATUTKINNON_VASTAANOTTAJA, OccupationContactInfoVisibilityRule.createRule(ALWAYS, NEVER, NEVER))
                .put(AMPUMAKOKEEN_VASTAANOTTAJA, OccupationContactInfoVisibilityRule.createRule(ALWAYS, NEVER, NEVER))
                .put(RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA, OccupationContactInfoVisibilityRule.createRule(ALWAYS, NEVER, NEVER))
                .put(METSASTAJATUTKINTOON_VALMENTAVAN_KOULUTUKSEN_KOULUTTAJA, OccupationContactInfoVisibilityRule.createRule(ALWAYS, NEVER, NEVER))
                .put(PUHEENJOHTAJA, OccupationContactInfoVisibilityRule.createRule(ALWAYS, OPTIONAL, OPTIONAL))
                .put(VARAPUHEENJOHTAJA, OccupationContactInfoVisibilityRule.createRule(ALWAYS, OPTIONAL, OPTIONAL))
                .put(HALLITUKSEN_JASEN, OccupationContactInfoVisibilityRule.createRule(ALWAYS, OPTIONAL, OPTIONAL))
                .put(HALLITUKSEN_VARAJASEN, OccupationContactInfoVisibilityRule.createRule(ALWAYS, OPTIONAL, OPTIONAL))
                .put(JALJESTYSKOIRAN_OHJAAJA_HIRVI, OccupationContactInfoVisibilityRule.createRule(OPTIONAL, OPTIONAL, OPTIONAL))
                .put(JALJESTYSKOIRAN_OHJAAJA_PIENET_HIRVIELAIMET, OccupationContactInfoVisibilityRule.createRule(OPTIONAL, OPTIONAL, OPTIONAL))
                .put(JALJESTYSKOIRAN_OHJAAJA_SUURPEDOT, OccupationContactInfoVisibilityRule.createRule(OPTIONAL, OPTIONAL, OPTIONAL))
                .put(ALUEKOKOUKSEN_EDUSTAJA, OccupationContactInfoVisibilityRule.createRule(NEVER, NEVER, NEVER))
                .put(ALUEKOKOUKSEN_VARAEDUSTAJA, OccupationContactInfoVisibilityRule.createRule(NEVER, NEVER, NEVER))
                .build();

        final ImmutableMap<OccupationType, OccupationContactInfoVisibilityRule> rkRules = ImmutableMap.<OccupationType, OccupationContactInfoVisibilityRule>builder()
                .put(PUHEENJOHTAJA, OccupationContactInfoVisibilityRule.createRule(ALWAYS, OPTIONAL, OPTIONAL))
                .put(VARAPUHEENJOHTAJA, OccupationContactInfoVisibilityRule.createRule(ALWAYS, OPTIONAL, OPTIONAL))
                .put(HALLITUKSEN_JASEN, OccupationContactInfoVisibilityRule.createRule(ALWAYS, OPTIONAL, OPTIONAL))
                .put(HALLITUKSEN_VARAJASEN, OccupationContactInfoVisibilityRule.createRule(ALWAYS, OPTIONAL, OPTIONAL))
                .build();

        final ImmutableMap<OccupationType, OccupationContactInfoVisibilityRule> arnRules = ImmutableMap.<OccupationType, OccupationContactInfoVisibilityRule>builder()
                .put(PUHEENJOHTAJA, OccupationContactInfoVisibilityRule.createRule(ALWAYS, OPTIONAL, OPTIONAL))
                .put(VARAPUHEENJOHTAJA, OccupationContactInfoVisibilityRule.createRule(ALWAYS, OPTIONAL, OPTIONAL))
                .put(JASEN, OccupationContactInfoVisibilityRule.createRule(ALWAYS, OPTIONAL, OPTIONAL))
                .put(VARAJASEN, OccupationContactInfoVisibilityRule.createRule(ALWAYS, OPTIONAL, OPTIONAL))
                .build();

        final ImmutableMap<OccupationType, OccupationContactInfoVisibilityRule> vrnRules = ImmutableMap.<OccupationType, OccupationContactInfoVisibilityRule>builder()
                .put(PUHEENJOHTAJA, OccupationContactInfoVisibilityRule.createRule(ALWAYS, OPTIONAL, OPTIONAL))
                .put(VARAPUHEENJOHTAJA, OccupationContactInfoVisibilityRule.createRule(ALWAYS, OPTIONAL, OPTIONAL))
                .put(JASEN, OccupationContactInfoVisibilityRule.createRule(ALWAYS, OPTIONAL, OPTIONAL))
                .put(VARAJASEN, OccupationContactInfoVisibilityRule.createRule(ALWAYS, OPTIONAL, OPTIONAL))
                .build();

        final ImmutableMap<OccupationType, OccupationContactInfoVisibilityRule> clubRules = ImmutableMap.<OccupationType, OccupationContactInfoVisibilityRule>builder()
                .put(SEURAN_YHDYSHENKILO, OccupationContactInfoVisibilityRule.createRule(NEVER, NEVER, NEVER))
                .put(SEURAN_JASEN, OccupationContactInfoVisibilityRule.createRule(NEVER, NEVER, NEVER))
                .build();

        final ImmutableMap<OccupationType, OccupationContactInfoVisibilityRule> clubGroupRules = ImmutableMap.<OccupationType, OccupationContactInfoVisibilityRule>builder()
                .put(RYHMAN_METSASTYKSENJOHTAJA, OccupationContactInfoVisibilityRule.createRule(OPTIONAL, OPTIONAL, OPTIONAL))
                .put(RYHMAN_JASEN, OccupationContactInfoVisibilityRule.createRule(NEVER, NEVER, NEVER))
                .build();

        occupationContactInfoVisibilityRules = ImmutableMap.<OrganisationType, ImmutableMap<OccupationType, OccupationContactInfoVisibilityRule>>builder()
                .put(RHY, rhyRules)
                .put(RK, rkRules)
                .put(ARN, arnRules)
                .put(VRN, vrnRules)
                .put(CLUB, clubRules)
                .put(CLUBGROUP, clubGroupRules)
                .build();
    }

    public static OccupationContactInfoVisibilityRule get(final OrganisationType organisationType,
                                                          final OccupationType occupationType) {
        return occupationContactInfoVisibilityRules.get(organisationType).get(occupationType);
    }

    public static ImmutableMap<OrganisationType, ImmutableMap<OccupationType, OccupationContactInfoVisibilityRule>> listAll() {
        return occupationContactInfoVisibilityRules;
    }
}

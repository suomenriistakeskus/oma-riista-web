package fi.riista.feature.organization.occupation;

import fi.riista.feature.organization.OrganisationType;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.EnumSet;

import static fi.riista.feature.organization.OrganisationType.ARN;
import static fi.riista.feature.organization.OrganisationType.RHY;
import static fi.riista.feature.organization.OrganisationType.RK;
import static fi.riista.feature.organization.OrganisationType.VRN;
import static fi.riista.feature.organization.occupation.OccupationBoardRepresentationRole.ALUEELLISEN_MAANOMISTAJAN_EDUSTAJA;
import static fi.riista.feature.organization.occupation.OccupationBoardRepresentationRole.ALUEKOKOUKSEN_EDUSTAJA;
import static fi.riista.feature.organization.occupation.OccupationBoardRepresentationRole.ALUEKOKOUKSEN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationBoardRepresentationRole.ELY_KESKUKSEN_EDUSTAJA;
import static fi.riista.feature.organization.occupation.OccupationBoardRepresentationRole.HENKILOSTO_EDUSTAJA;
import static fi.riista.feature.organization.occupation.OccupationBoardRepresentationRole.JARJESTO_EDUSTAJA;
import static fi.riista.feature.organization.occupation.OccupationBoardRepresentationRole.LUKE_EDUSTAJA;
import static fi.riista.feature.organization.occupation.OccupationBoardRepresentationRole.LUONNONSUOJELU_EDUSTAJA;
import static fi.riista.feature.organization.occupation.OccupationBoardRepresentationRole.MAAKUNTALIITON_JASEN;
import static fi.riista.feature.organization.occupation.OccupationBoardRepresentationRole.MAANOMISTAJIEN_EDUSTAJA;
import static fi.riista.feature.organization.occupation.OccupationBoardRepresentationRole.MAANOMISTAJIEN_VARAEDUSTAJA;
import static fi.riista.feature.organization.occupation.OccupationBoardRepresentationRole.METSAHALLITUKSEN_EDUSTAJA;
import static fi.riista.feature.organization.occupation.OccupationBoardRepresentationRole.METSAHALLITUKSEN_VARAEDUSTAJA;
import static fi.riista.feature.organization.occupation.OccupationBoardRepresentationRole.METSAKESKUKSEN_EDUSTAJA;
import static fi.riista.feature.organization.occupation.OccupationBoardRepresentationRole.METSASTYS_EDUSTAJA;
import static fi.riista.feature.organization.occupation.OccupationBoardRepresentationRole.METSATALOUS_EDUSTAJA;
import static fi.riista.feature.organization.occupation.OccupationBoardRepresentationRole.MMM_EDUSTAJA;
import static fi.riista.feature.organization.occupation.OccupationBoardRepresentationRole.RH_MAKSU_EDUSTAJA;
import static fi.riista.feature.organization.occupation.OccupationBoardRepresentationRole.YMPARISTOMINISTERION_EDUSTAJA;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assume.assumeTrue;

@RunWith(Theories.class)
public class OccupationBoardRepresentationRoleTest {

    @Theory
    public void testGetApplicableRoles(final OrganisationType organisationType) {
        assumeTrue(asList(RK, VRN, ARN, RHY).contains(organisationType));

        final EnumSet<OccupationBoardRepresentationRole> roles =
                OccupationBoardRepresentationRole.getApplicableRoles(organisationType);

        EnumSet<OccupationBoardRepresentationRole> expectedRoles;
        switch (organisationType) {
            case RK:
                expectedRoles = EnumSet.of(RH_MAKSU_EDUSTAJA, MMM_EDUSTAJA, LUKE_EDUSTAJA, JARJESTO_EDUSTAJA,
                        HENKILOSTO_EDUSTAJA);
                break;
            case VRN:
                expectedRoles = EnumSet.of(METSAHALLITUKSEN_EDUSTAJA, MMM_EDUSTAJA, LUKE_EDUSTAJA, JARJESTO_EDUSTAJA,
                        ALUEKOKOUKSEN_EDUSTAJA, YMPARISTOMINISTERION_EDUSTAJA, METSATALOUS_EDUSTAJA, METSASTYS_EDUSTAJA,
                        LUONNONSUOJELU_EDUSTAJA);
                break;
            case ARN:
                expectedRoles = EnumSet.of(ALUEKOKOUKSEN_JASEN, MAAKUNTALIITON_JASEN, ELY_KESKUKSEN_EDUSTAJA,
                        METSAKESKUKSEN_EDUSTAJA, ALUEELLISEN_MAANOMISTAJAN_EDUSTAJA);
                break;
            case RHY:
                expectedRoles = EnumSet.of(METSAHALLITUKSEN_EDUSTAJA, MAANOMISTAJIEN_EDUSTAJA,
                        METSAHALLITUKSEN_VARAEDUSTAJA, MAANOMISTAJIEN_VARAEDUSTAJA);
                break;
            default:
                throw new IllegalArgumentException();
        }

        assertThat(roles, is(equalTo(expectedRoles)));
    }
}

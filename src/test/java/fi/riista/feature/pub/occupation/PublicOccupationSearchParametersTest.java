package fi.riista.feature.pub.occupation;

import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.OccupationType;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PublicOccupationSearchParametersTest {
    @Test
    public void testBuildRhyAllOccupationSearch() {
        final PublicOccupationSearchParameters result = PublicOccupationSearchParameters.builder()
                .withRhyId("301")
                .build();
        assertFalse(result.canReturnTooManyResults());
    }

    @Test
    public void testBuildSrvaSearch() {
        final PublicOccupationSearchParameters result = PublicOccupationSearchParameters.builder()
                .withOrganisationType(OrganisationType.RHY)
                .withOccupationType(OccupationType.SRVA_YHTEYSHENKILO)
                .build();
        assertFalse(result.canReturnTooManyResults());
    }

    @Test
    public void testBuildSrvaSearchWithRhy() {
        final PublicOccupationSearchParameters result = PublicOccupationSearchParameters.builder()
                .withOrganisationType(OrganisationType.RHY)
                .withOccupationType(OccupationType.SRVA_YHTEYSHENKILO)
                .withRhyId("301")
                .build();
        assertFalse(result.canReturnTooManyResults());
    }

    @Test
    public void testBuildCoordinatorSearch() {
        final PublicOccupationSearchParameters result = PublicOccupationSearchParameters.builder()
                .withOrganisationType(OrganisationType.RHY)
                .withOccupationType(OccupationType.TOIMINNANOHJAAJA)
                .build();
        assertFalse(result.canReturnTooManyResults());
    }

    @Test
    public void testBuildCoordinatorSearchWithArea() {
        final PublicOccupationSearchParameters result = PublicOccupationSearchParameters.builder()
                .withOrganisationType(OrganisationType.RHY)
                .withOccupationType(OccupationType.TOIMINNANOHJAAJA)
                .withAreaId("300")
                .build();
        assertFalse(result.canReturnTooManyResults());
    }

    @Test
    public void testBuildCoordinatorSearchWithRhy() {
        final PublicOccupationSearchParameters result = PublicOccupationSearchParameters.builder()
                .withOrganisationType(OrganisationType.RHY)
                .withOccupationType(OccupationType.TOIMINNANOHJAAJA)
                .withRhyId("301")
                .build();
        assertFalse(result.canReturnTooManyResults());
    }

    @Test
    public void testBuildVrnSearch() {
        final PublicOccupationSearchParameters result = PublicOccupationSearchParameters.builder()
                .withOrganisationType(OrganisationType.VRN)
                .withOccupationType(OccupationType.PUHEENJOHTAJA)
                .build();
        assertFalse(result.canReturnTooManyResults());
    }

    @Test
    public void testBuildArnSearch() {
        final PublicOccupationSearchParameters result = PublicOccupationSearchParameters.builder()
                .withOrganisationType(OrganisationType.ARN)
                .withOccupationType(OccupationType.PUHEENJOHTAJA)
                .build();
        assertFalse(result.canReturnTooManyResults());
    }

    @Test
    public void testBuildArnSearchWithArea() {
        final PublicOccupationSearchParameters result = PublicOccupationSearchParameters.builder()
                .withOrganisationType(OrganisationType.ARN)
                .withOccupationType(OccupationType.PUHEENJOHTAJA)
                .withAreaId("300")
                .build();
        assertFalse(result.canReturnTooManyResults());
    }

    @Test
    public void testBuildRkSearch() {
        final PublicOccupationSearchParameters result = PublicOccupationSearchParameters.builder()
                .withOrganisationType(OrganisationType.RK)
                .withOccupationType(OccupationType.PUHEENJOHTAJA)
                .build();
        assertFalse(result.canReturnTooManyResults());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoCriteria() {
        PublicOccupationSearchParameters.builder().build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOccupationTypeMissingOrganisationType() {
        PublicOccupationSearchParameters.builder()
                .withOccupationType(OccupationType.SRVA_YHTEYSHENKILO)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOrganisationTypeMissingOccupationType() {
        PublicOccupationSearchParameters.builder()
                .withOrganisationType(OrganisationType.RHY)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDenyClubOrganisationType() {
        PublicOccupationSearchParameters.builder()
                .withOrganisationType(OrganisationType.CLUB)
                .withOccupationType(OccupationType.SEURAN_JASEN)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDenyGroupOrganisationType() {
        PublicOccupationSearchParameters.builder()
                .withOrganisationType(OrganisationType.CLUBGROUP)
                .withOccupationType(OccupationType.RYHMAN_JASEN)
                .build();
    }

    @Test
    public void testTooManyResults() {
        final PublicOccupationSearchParameters result = PublicOccupationSearchParameters.builder()
                .withOrganisationType(OrganisationType.RHY)
                .withOccupationType(OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA)
                .build();
        assertTrue(result.canReturnTooManyResults());
    }

    @Test
    public void testTooManyResultsWithArea() {
        final PublicOccupationSearchParameters result = PublicOccupationSearchParameters.builder()
                .withAreaId("300")
                .build();
        assertTrue(result.canReturnTooManyResults());
    }
}

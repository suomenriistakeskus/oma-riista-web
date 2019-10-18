package fi.riista.feature.pub.occupation;

import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.OccupationType;
import org.junit.Test;

public class PublicOccupationSearchParametersTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNoCriteria() {
        PublicOccupationSearchParameters.builder().build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPageSizeMissingPageNumber() {
        PublicOccupationSearchParameters.builder()
                .withPageSize(3).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPageNumberMissingPageSize() {
        PublicOccupationSearchParameters.builder()
                .withPageNumber(3).build();
    }

    @Test
    public void testPageNumberAndPageSizeAreSufficient() {
        PublicOccupationSearchParameters.builder()
                .withPageNumber(3)
                .withPageSize(3).build();
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
}

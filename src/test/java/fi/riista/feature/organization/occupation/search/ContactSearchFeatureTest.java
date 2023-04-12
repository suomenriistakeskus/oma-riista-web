package fi.riista.feature.organization.occupation.search;

import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import java.util.Objects;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ContactSearchFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private ContactSearchFeature contactSearchFeature;

    @Test
    public void test() {
        LocalDate today = DateUtil.today();
        RiistakeskuksenAlue rka1 = model().newRiistakeskuksenAlue();
        Riistanhoitoyhdistys rhy1_1 = model().newRiistanhoitoyhdistys(rka1);
        Riistanhoitoyhdistys rhy1_2 = model().newRiistanhoitoyhdistys(rka1);
        Riistanhoitoyhdistys rhy1_notActive = model().newRiistanhoitoyhdistys(rka1);
        rhy1_notActive.setActive(false);

        RiistakeskuksenAlue rka2 = model().newRiistakeskuksenAlue();
        Riistanhoitoyhdistys rhy2_1 = model().newRiistanhoitoyhdistys(rka2);
        Riistanhoitoyhdistys rhy2_2 = model().newRiistanhoitoyhdistys(rka2);

        Person person1 = model().newPerson();
        Occupation occ1_1 = createOccupation(rhy1_1, person1, null, null, OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA);
        createOccupation(rhy1_notActive, person1, today, null, OccupationType.JALJESTYSKOIRAN_OHJAAJA_PIENET_HIRVIELAIMET);
        Occupation occ1_2 = createOccupation(rhy1_2, person1, today, null, OccupationType.JALJESTYSKOIRAN_OHJAAJA_PIENET_HIRVIELAIMET);
        Occupation occ1_3 = createOccupation(rhy1_1, person1, null, today, OccupationType.METSASTYKSENVALVOJA);
        createOccupation(rhy1_2, person1, null, today.minusDays(1), OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA);
        createOccupation(rhy1_1, person1, today.plusDays(1), null, OccupationType.JALJESTYSKOIRAN_OHJAAJA_PIENET_HIRVIELAIMET);
        createOccupation(rhy1_2, person1, today.plusDays(1), today.plusDays(2), OccupationType.METSASTYKSENVALVOJA);

        Person person2 = model().newPerson();
        Occupation occ2_1 = createOccupation(rhy2_1, person2, null, null, OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA);
        Occupation occ2_2 = createOccupation(rhy2_2, person2, today, null, OccupationType.JALJESTYSKOIRAN_OHJAAJA_PIENET_HIRVIELAIMET);
        Occupation occ2_3 = createOccupation(rhy2_1, person2, null, today, OccupationType.METSASTYKSENVALVOJA);
        createOccupation(rhy2_1, person2, null, today.minusDays(1), OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA);
        createOccupation(rhy2_2, person2, today.plusDays(1), null, OccupationType.JALJESTYSKOIRAN_OHJAAJA_PIENET_HIRVIELAIMET);
        createOccupation(rhy2_1, person2, today.plusDays(1), today.plusDays(2), OccupationType.METSASTYKSENVALVOJA);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            assertOccResult(searchOccupations(occSearchByRhy(null)), occ1_1, occ1_2, occ1_3, occ2_1, occ2_2, occ2_3);
            assertOccResult(searchOccupations(occSearchByRhy(rhy2_2.getOfficialCode())), occ2_2);
            assertOccResult(searchOccupations(occSearchByOccupation(OrganisationType.RHY, OccupationType.METSASTYKSENVALVOJA)), occ1_3, occ2_3);
            assertOccResult(searchOccupations(occSearchByOccupation(OrganisationType.RKA, OccupationType.HALLITUKSEN_JASEN)));
            assertOccResult(searchOccupations(occSearchByOccupation(OrganisationType.RKA, OccupationType.HALLITUKSEN_JASEN)));
            assertRhyResult(searchRhy(rhySearchByRhy(null)), rhy1_1, rhy1_2, rhy2_1, rhy2_2);
            assertRhyResult(searchRhy(rhySearchByRhy(rhy2_2.getOfficialCode())), rhy2_2);
        });
    }

    private List<OccupationContactSearchResultDTO> searchOccupations(OccupationContactSearchDTO... search) {
        return contactSearchFeature.searchOccupations(Arrays.asList(search), null);
    }

    private List<RhyContactSearchResultDTO> searchRhy(RhyContactSearchDTO... search) {
        return contactSearchFeature.searchRhy(Arrays.asList(search), null);
    }

    private static void assertOccResult(List<OccupationContactSearchResultDTO> results, Occupation... expectedOccupations) {
        assertEquals(expectedOccupations.length, results.size());
        for (Occupation occupation : expectedOccupations) {
            OccupationContactSearchResultDTO result = findOccSearchResult(occupation, results);
            assertNotNull(result);
        }
    }

    private static OccupationContactSearchResultDTO findOccSearchResult(Occupation occupation, List<OccupationContactSearchResultDTO> results) {
        for (OccupationContactSearchResultDTO result : results) {
            if (result.getOccupationType() == occupation.getOccupationType()
                    && result.getLastName().equals(occupation.getPerson().getLastName())
                    && result.getFirstName().equals(occupation.getPerson().getFirstName())) {
                return result;
            }
        }
        return null;
    }

    private void assertRhyResult(final List<RhyContactSearchResultDTO> results, Riistanhoitoyhdistys... expectedRhys) {
        assertEquals(expectedRhys.length, results.size());
        for (Riistanhoitoyhdistys rhy : expectedRhys) {
            RhyContactSearchResultDTO result = findRhySearchResult(rhy, results);
            assertNotNull(result);
        }
    }

    private static RhyContactSearchResultDTO findRhySearchResult(Riistanhoitoyhdistys rhy, List<RhyContactSearchResultDTO> results) {
        for (RhyContactSearchResultDTO result : results) {
            if (Objects.equals(result.getOfficialCode(), rhy.getOfficialCode())) {
                return result;
            }
        }
        return null;
    }

    private static OccupationContactSearchDTO occSearchByRhy(String rhyCode) {
        OccupationContactSearchDTO dto = new OccupationContactSearchDTO();
        dto.setOrganisationType(OrganisationType.RHY);
        dto.setRhyCode(rhyCode);
        return dto;
    }

    private static OccupationContactSearchDTO occSearchByOccupation(OrganisationType organisationType, OccupationType type) {
        OccupationContactSearchDTO dto = new OccupationContactSearchDTO();
        dto.setOrganisationType(organisationType);
        dto.setOccupationType(type);
        return dto;
    }

    private RhyContactSearchDTO rhySearchByRhy(final String rhyCode) {
        final RhyContactSearchDTO dto = new RhyContactSearchDTO();
        dto.setRhyCode(rhyCode);
        return dto;
    }

    private Occupation createOccupation(Riistanhoitoyhdistys rhy, Person person, LocalDate beginDate, LocalDate endDate, OccupationType type) {
        return model().newOccupation(rhy, person, type, beginDate, endDate);
    }
}

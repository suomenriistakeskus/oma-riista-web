package fi.riista.feature.organization.occupation.search;

import fi.riista.feature.organization.occupation.search.OccupationContactSearchDTO;
import fi.riista.feature.organization.occupation.search.OccupationContactSearchResultDTO;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.feature.organization.occupation.search.ContactSearchFeature;
import fi.riista.util.DateUtil;
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
    public void testSearchOccupations() {
        LocalDate today = DateUtil.today();
        RiistakeskuksenAlue rka1 = model().newRiistakeskuksenAlue();
        Riistanhoitoyhdistys rhy1_1 = model().newRiistanhoitoyhdistys(rka1);
        Riistanhoitoyhdistys rhy1_2 = model().newRiistanhoitoyhdistys(rka1);

        RiistakeskuksenAlue rka2 = model().newRiistakeskuksenAlue();
        Riistanhoitoyhdistys rhy2_1 = model().newRiistanhoitoyhdistys(rka2);
        Riistanhoitoyhdistys rhy2_2 = model().newRiistanhoitoyhdistys(rka2);

        Person person1 = model().newPerson();
        Occupation occ1_1 = createOccupation(rhy1_1, person1, null, null, OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA);
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
            assertResult(search(byRhy(OrganisationType.RHY, null)), occ1_1, occ1_2, occ1_3, occ2_1, occ2_2, occ2_3);
            assertResult(search(byRhy(OrganisationType.RHY, rhy2_2.getOfficialCode())), occ2_2);
            assertResult(search(byOccupation(OrganisationType.RHY, OccupationType.METSASTYKSENVALVOJA)), occ1_3, occ2_3);
            assertResult(search(byOccupation(OrganisationType.RKA, OccupationType.HALLITUKSEN_JASEN)));
        });
    }

    private List<OccupationContactSearchResultDTO> search(OccupationContactSearchDTO... search) {
        return contactSearchFeature.searchOccupations(Arrays.asList(search), null);
    }

    private static void assertResult(List<OccupationContactSearchResultDTO> results, Occupation... occupations) {
        assertEquals(occupations.length, results.size());
        for (Occupation occupation : occupations) {
            OccupationContactSearchResultDTO result = findSearchResult(occupation, results);
            assertNotNull(result);
        }
    }

    private static OccupationContactSearchResultDTO findSearchResult(Occupation occupation, List<OccupationContactSearchResultDTO> results) {
        for (OccupationContactSearchResultDTO result : results) {
            if (result.getOccupationType() == occupation.getOccupationType()
                    && result.getLastName().equals(occupation.getPerson().getLastName())
                    && result.getFirstName().equals(occupation.getPerson().getFirstName())) {
                return result;
            }
        }
        return null;
    }

    private static OccupationContactSearchDTO byRhy(OrganisationType organisationType, String rhyCode) {
        OccupationContactSearchDTO dto = new OccupationContactSearchDTO();
        dto.setOrganisationType(organisationType);
        dto.setRhyCode(rhyCode);
        return dto;
    }

    private static OccupationContactSearchDTO byOccupation(OrganisationType organisationType, OccupationType type) {
        OccupationContactSearchDTO dto = new OccupationContactSearchDTO();
        dto.setOrganisationType(organisationType);
        dto.setOccupationType(type);
        return dto;
    }

    private Occupation createOccupation(Riistanhoitoyhdistys rhy1_1, Person person1, LocalDate beginDate, LocalDate endDate, OccupationType type) {
        Occupation occ = model().newOccupation(rhy1_1, person1, type);
        occ.setBeginDate(beginDate);
        occ.setEndDate(endDate);
        return occ;
    }
}

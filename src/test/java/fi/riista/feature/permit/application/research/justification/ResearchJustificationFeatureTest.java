package fi.riista.feature.permit.application.research.justification;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.research.ResearchPermitApplication;
import fi.riista.feature.permit.application.research.ResearchPermitApplicationRepository;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class ResearchJustificationFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private ResearchJustificationFeature feature;

    @Resource
    private ResearchPermitApplicationRepository researchPermitApplicationRepository;

    private Riistanhoitoyhdistys rhy;
    private Person contactPerson;
    private HarvestPermitApplication application;
    private ResearchPermitApplication researchPermitApplication;

    @Before
    public void setup() {
        rhy = model().newRiistanhoitoyhdistys();
        contactPerson = model().newPerson();
        application =
                model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.RESEARCH);
        application.setContactPerson(contactPerson);
        application.setStatus(HarvestPermitApplication.Status.DRAFT);
        researchPermitApplication = model().newResearchPermitApplication(application);
        researchPermitApplication.setJustification("Research justification");
    }

    @Test(expected = AccessDeniedException.class)
    public void testAuthentication_unauthorized() {
        onSavedAndAuthenticated(createNewUser(), () -> {
            feature.getJustification(application.getId());
        });
    }

    @Test
    public void testGetJustification() {
        onSavedAndAuthenticated(createUser(contactPerson), () -> {
            final ResearchJustificationDTO justificationDto = feature.getJustification(application.getId());
            assertThat(justificationDto, is(notNullValue()));
            assertThat(justificationDto.getJustification(), is(equalTo(researchPermitApplication.getJustification())));
        });
    }

    @Test
    public void testUpdateJustification() {
        onSavedAndAuthenticated(createUser(contactPerson), () -> {
            final ResearchJustificationDTO updateDto = new ResearchJustificationDTO();
            updateDto.setJustification("Updated research justification");
            feature.updateJustification(application.getId(), updateDto);

            runInTransaction(() -> {
                final ResearchPermitApplication updatedApplication =
                        researchPermitApplicationRepository.findByHarvestPermitApplication(application);
                assertThat(updatedApplication, is(notNullValue()));
                assertThat(updatedApplication.getJustification(), is(equalTo(updateDto.getJustification())));
            });
        });
    }
}
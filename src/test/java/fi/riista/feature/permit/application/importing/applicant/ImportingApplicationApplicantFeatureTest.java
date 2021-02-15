package fi.riista.feature.permit.application.importing.applicant;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationRepository;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.feature.permit.application.PermitHolder.PermitHolderType.RY;
import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class ImportingApplicationApplicantFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private ImportingPermitApplicantFeature feature;

    @Resource
    private HarvestPermitApplicationRepository harvestPermitApplicationRepository;

    private Person applicant;
    private Riistanhoitoyhdistys rhy;
    private HarvestPermitApplication application;

    @Before
    public void setup() {
        applicant = model().newPerson();
        rhy = model().newRiistanhoitoyhdistys();
        application = model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.IMPORTING);
        application.setStatus(HarvestPermitApplication.Status.DRAFT);
        application.setPermitHolder(PermitHolder.createHolderForPerson(applicant));
        application.setContactPerson(applicant);
        persistInNewTransaction();
    }

    @Test
    public void testSmoke() {
        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            final PermitHolderDTO permitHolderInfo = feature.getPermitHolderInfo(application.getId());
            assertThat(permitHolderInfo.getName(), equalTo(applicant.getFullName()));
            assertThat(permitHolderInfo.getCode(), is(nullValue()));
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void test_unAuthorized() {

        onSavedAndAuthenticated(createNewUser(), () -> {
            feature.getPermitHolderInfo(application.getId());
        });
    }

    @Test
    public void testUpdate() {
        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            final PermitHolderDTO dto = PermitHolderDTO.create("Yhdistys", "1234567", RY);
            feature.updatePermitHolder(application.getId(), dto);
        });

        runInTransaction(() -> {
            final List<HarvestPermitApplication> all = harvestPermitApplicationRepository.findAll();
            assertThat(all, hasSize(1));
            final PermitHolder permitHolder = all.get(0).getPermitHolder();
            assertThat(permitHolder.getName(), equalTo("Yhdistys"));
            assertThat(permitHolder.getCode(), equalTo("1234567"));
            assertThat(permitHolder.getType(), equalTo(RY));
        });
    }
}

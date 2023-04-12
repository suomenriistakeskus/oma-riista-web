package fi.riista.feature.permit.application;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.lawsectionten.LawSectionTenPermitApplication;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.HarvestPermitAreaEvent;
import fi.riista.feature.permit.area.HarvestPermitAreaEventRepository;
import fi.riista.feature.permit.area.HarvestPermitAreaRepository;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static fi.riista.feature.harvestpermit.HarvestPermitCategory.EUROPEAN_BEAVER;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.LAW_SECTION_TEN;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.MOOSELIKE;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.PARTRIDGE;
import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.Matchers.is;

public class HarvestPermitApplicationDeleteFeatureTest extends EmbeddedDatabaseTest {

    @Autowired
    private HarvestPermitApplicationDeleteFeature harvestPermitApplicationDeleteFeature;

    @Autowired
    private HarvestPermitAreaEventRepository harvestPermitAreaEventRepository;
    @Autowired
    private HarvestPermitApplicationRepository harvestPermitApplicationRepository;
    @Autowired
    private HarvestPermitAreaRepository harvestPermitAreaRepository;


    private Riistanhoitoyhdistys rhy;
    private HarvestPermitArea area;
    private HarvestPermitAreaEvent event;

    private Person contactPerson;
    private SystemUser admin;

    @Before
    public void setup() {

        admin = createNewAdmin();

        rhy = model().newRiistanhoitoyhdistys();
        contactPerson = model().newPerson(rhy);

        area = model().newHarvestPermitArea();
        event = model().newHarvestPermitAreaEvent(area);

    }

    @Test
    public void testDeleteDraftMooselikeApplication() {

        final HarvestPermitApplication mooselikeDraftApplication =
                model().newHarvestPermitApplication(rhy, null, MOOSELIKE);
        mooselikeDraftApplication.setContactPerson(contactPerson);
        mooselikeDraftApplication.setStatus(HarvestPermitApplication.Status.DRAFT);
        mooselikeDraftApplication.setArea(area);

        onSavedAndAuthenticated(admin, () -> {
                    harvestPermitApplicationDeleteFeature.deleteApplication(mooselikeDraftApplication.getId());
                }
        );

        runInTransaction(()->{
            assertThat(harvestPermitAreaRepository.findById(area.getId()).isPresent(), is(false));
            assertThat(harvestPermitAreaEventRepository.findById(event.getId()).isPresent(), is(false));
            assertThat(
                    harvestPermitApplicationRepository.findById(mooselikeDraftApplication.getId()).isPresent(),
                    is(false));
        });
    }
    
    @Test
    public void testDeleteDraftEuropeanBeaverApplication() {
        testSectionTen(EUROPEAN_BEAVER);
    }

    @Test
    public void testDeleteDraftPartridgeApplication() {
        testSectionTen(PARTRIDGE);
    }

    private void testSectionTen(final HarvestPermitCategory category) {
        final HarvestPermitApplication application = model().newHarvestPermitApplication(rhy, null, category);
        application.setStatus(HarvestPermitApplication.Status.DRAFT);
        final LawSectionTenPermitApplication lawSectionTenApplication =
                model().newLawSectionTenPermitApplication(application);
        onSavedAndAuthenticated(createNewUser("person", application.getContactPerson()), ()->{
            harvestPermitApplicationDeleteFeature.deleteApplication(application.getId());
        });

        runInTransaction(()->{
            assertThat(harvestPermitApplicationRepository.findById(application.getId()).isPresent(), is(false) );
        });
    }


}

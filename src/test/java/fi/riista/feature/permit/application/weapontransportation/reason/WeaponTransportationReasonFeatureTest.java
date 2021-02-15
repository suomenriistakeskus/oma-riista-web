package fi.riista.feature.permit.application.weapontransportation.reason;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.application.weapontransportation.WeaponTransportationPermitApplication;
import fi.riista.feature.permit.application.weapontransportation.WeaponTransportationPermitApplicationRepository;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class WeaponTransportationReasonFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private WeaponTransportationReasonFeature feature;

    @Resource
    private WeaponTransportationPermitApplicationRepository repository;

    private Person applicant;
    private Riistanhoitoyhdistys rhy;
    private HarvestPermitApplication application;
    private SystemUser user;
    private WeaponTransportationPermitApplication transportApplication;

    @Before
    public void setup() {
        applicant = model().newPerson();
        rhy = model().newRiistanhoitoyhdistys();

        application = model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.WEAPON_TRANSPORTATION);
        application.setStatus(HarvestPermitApplication.Status.DRAFT);
        application.setPermitHolder(PermitHolder.createHolderForPerson(applicant));
        application.setContactPerson(applicant);

        user = createNewUser("applicant", applicant);
        transportApplication =
                model().newWeaponTransportationPermitApplication(application,
                        WeaponTransportationReasonType.POROMIES,
                        null,
                        new LocalDate(2020, 7, 2),
                        new LocalDate(2020, 7, 3),
                        "Justification");
    }

    @Test(expected = AccessDeniedException.class)
    public void testGetReason_unauthorized() {

        onSavedAndAuthenticated(createNewUser(), () -> {
            feature.getReason(application.getId());
        });
    }

    @Test
    public void testGetReason() {
        onSavedAndAuthenticated(user, () -> {
            final ReasonDTO dto = feature.getReason(application.getId());

            assertNotNull(dto);
            assertEquals(transportApplication.getReasonType(), dto.getReasonType());
            assertEquals(transportApplication.getReasonDescription(), dto.getReasonDescription());
            assertEquals(transportApplication.getBeginDate(), dto.getBeginDate());
            assertEquals(transportApplication.getEndDate(), dto.getEndDate());
        });
    }

    @Test
    public void testUpdateReason() {
        onSavedAndAuthenticated(user, () -> {
            final ReasonDTO updateDto =
                    new ReasonDTO(WeaponTransportationReasonType.MUU,
                            "Updated description",
                            transportApplication.getBeginDate().plusDays(1),
                            transportApplication.getEndDate().plusDays(1));
            feature.updateReason(application.getId(), updateDto);

            runInTransaction(() -> {
                final WeaponTransportationPermitApplication updatedApplication = repository.findByHarvestPermitApplication(application);

                assertNotNull(updatedApplication);
                assertEquals(updateDto.getReasonType(), updatedApplication.getReasonType());
                assertEquals(updateDto.getReasonDescription(), updatedApplication.getReasonDescription());
                assertEquals(updateDto.getBeginDate(), updatedApplication.getBeginDate());
                assertEquals(updateDto.getEndDate(), updatedApplication.getEndDate());
            });
        });
    }
}

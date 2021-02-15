package fi.riista.feature.permit.application.disability.basicinfo;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.disability.DisabilityPermitApplication;
import fi.riista.feature.permit.application.disability.DisabilityPermitApplicationRepository;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;

import static fi.riista.util.DateUtil.today;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class DisabilityPermitBasicInfoFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private DisabilityPermitBasicInfoFeature feature;

    @Resource
    private DisabilityPermitApplicationRepository repository;

    private Riistanhoitoyhdistys rhy;
    private Person person;
    private HarvestPermitApplication harvestPermitApplication;
    private DisabilityPermitApplication application;

    @Before
    public void setup() {
        final LocalDate today = today();

        rhy = model().newRiistanhoitoyhdistys();
        person = model().newPerson(rhy);

        harvestPermitApplication = model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.DISABILITY);
        harvestPermitApplication.setStatus(HarvestPermitApplication.Status.DRAFT);
        harvestPermitApplication.setContactPerson(person);

        application = model().newDisabilityPermitApplication(harvestPermitApplication);
        application.setUseMotorVehicle(true);
        application.setUseVehicleForWeaponTransport(true);
        application.setBeginDate(today);
        application.setEndDate(today.plusDays(1));
    }

    @Test
    public void testGetBasicInfo() {
        onSavedAndAuthenticated(createUser(person), () -> {
            final BasicInfoDTO dto = feature.getBasicInfo(harvestPermitApplication.getId());

            assertThat(dto, is(notNullValue()));
            assertThat(dto.getUseMotorVehicle(), is(equalTo(application.getUseMotorVehicle())));
            assertThat(dto.getUseVehicleForWeaponTransport(), is(equalTo(application.getUseVehicleForWeaponTransport())));
            assertThat(dto.getBeginDate(), is(equalTo(application.getBeginDate())));
            assertThat(dto.getEndDate(), is(equalTo(application.getEndDate())));
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testGetBasicInfo_unauthorized() {
        onSavedAndAuthenticated(createNewUser(), () -> {
            feature.getBasicInfo(harvestPermitApplication.getId());
        });
    }

    @Test
    public void testUpdateBasicInfo() {
        onSavedAndAuthenticated(createUser(person), () -> {
            final BasicInfoDTO updateDto = new BasicInfoDTO();
            updateDto.setUseMotorVehicle(false);
            updateDto.setUseVehicleForWeaponTransport(false);
            updateDto.setBeginDate(application.getBeginDate().plusDays(10));
            updateDto.setEndDate(application.getEndDate().plusDays(10));

            feature.updateBasicInfo(harvestPermitApplication.getId(), updateDto);

            runInTransaction(() -> {
                final DisabilityPermitApplication updatedApplication = repository.findByHarvestPermitApplication(harvestPermitApplication);

                assertThat(updatedApplication, is(notNullValue()));
                assertThat(updatedApplication.getUseMotorVehicle(), is((equalTo(updateDto.getUseMotorVehicle()))));
                assertThat(updatedApplication.getUseVehicleForWeaponTransport(), is(equalTo(updateDto.getUseVehicleForWeaponTransport())));
                assertThat(updatedApplication.getBeginDate(), is(equalTo(updateDto.getBeginDate())));
                assertThat(updatedApplication.getEndDate(), is(equalTo(updateDto.getEndDate())));
            });
        });
    }
}

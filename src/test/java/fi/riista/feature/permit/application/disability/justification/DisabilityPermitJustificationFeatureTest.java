package fi.riista.feature.permit.application.disability.justification;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.PermitApplicationVehicleType;
import fi.riista.feature.permit.application.disability.DisabilityPermitApplication;
import fi.riista.feature.permit.application.disability.DisabilityPermitApplicationRepository;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;

import java.util.List;

import static fi.riista.util.DateUtil.today;

import static java.util.Collections.singletonList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class DisabilityPermitJustificationFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private DisabilityPermitJustificationFeature feature;

    @Resource
    private DisabilityPermitApplicationRepository applicationRepository;

    @Resource
    private DisabilityPermitVehicleRepository vehicleRepository;

    @Resource
    private DisabilityPermitHuntingTypeInfoRepository huntingTypeInfoRepository;

    private Riistanhoitoyhdistys rhy;
    private Person person;
    private HarvestPermitApplication harvestPermitApplication;
    private DisabilityPermitApplication application;
    private DisabilityPermitVehicle vehicle;
    private DisabilityPermitHuntingTypeInfo huntingTypeInfo;

    @Before
    public void setup() {
        final LocalDate today = today();

        rhy = model().newRiistanhoitoyhdistys();
        person = model().newPerson(rhy);

        harvestPermitApplication = model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.DISABILITY);
        harvestPermitApplication.setStatus(HarvestPermitApplication.Status.DRAFT);
        harvestPermitApplication.setContactPerson(person);

        application = model().newDisabilityPermitApplication(harvestPermitApplication);
        application.setJustification("Justification");

        vehicle = model().newDisabilityPermitVehicle(application);
        vehicle.setType(PermitApplicationVehicleType.MUU);
        vehicle.setDescription("Vehicle description");
        vehicle.setJustification("Vehicle justification");

        huntingTypeInfo = model().newDisabilityPermitHuntingTypeInfo(application);
        huntingTypeInfo.setHuntingType(HuntingType.MUU);
        huntingTypeInfo.setHuntingTypeDescription("Hunting type description");
    }

    @Test
    public void testGetJustification() {
        onSavedAndAuthenticated(createUser(person), () -> {
            final JustificationDTO dto = feature.getJustification(harvestPermitApplication.getId());

            assertThat(dto, is(notNullValue()));
            assertThat(dto.getJustification(), is(equalTo(application.getJustification())));

            final List<DisabilityPermitVehicleDTO> vehicles = dto.getVehicles();
            assertThat(vehicles, is(notNullValue()));
            assertThat(vehicles, hasSize(1));

            final DisabilityPermitVehicleDTO vehicleDto = vehicles.get(0);
            assertThat(vehicleDto.getType(), is(equalTo(vehicle.getType())));
            assertThat(vehicleDto.getDescription(), is(equalTo(vehicle.getDescription())));
            assertThat(vehicleDto.getJustification(), is(equalTo(vehicle.getJustification())));

            final List<DisabilityPermitHuntingTypeInfoDTO> huntingTypeInfos = dto.getHuntingTypeInfos();
            assertThat(huntingTypeInfos, is(notNullValue()));
            assertThat(huntingTypeInfos, hasSize(1));

            final DisabilityPermitHuntingTypeInfoDTO huntingTypeInfoDto = huntingTypeInfos.get(0);
            assertThat(huntingTypeInfoDto.getHuntingType(), is(equalTo(huntingTypeInfo.getHuntingType())));
            assertThat(huntingTypeInfoDto.getHuntingTypeDescription(), is(equalTo(huntingTypeInfo.getHuntingTypeDescription())));
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testGetJustification_unauthorized() {
        onSavedAndAuthenticated(createNewUser(), () -> {
            feature.getJustification(harvestPermitApplication.getId());
        });
    }

    @Test
    public void testUpdateJustification() {
        onSavedAndAuthenticated(createUser(person), () -> {
            final DisabilityPermitVehicleDTO updateVehicleDto = new DisabilityPermitVehicleDTO();
            updateVehicleDto.setType(PermitApplicationVehicleType.AUTO);
            updateVehicleDto.setDescription("Updated description");
            updateVehicleDto.setJustification("Updated justification");

            final DisabilityPermitHuntingTypeInfoDTO updateHuntingTypeInfoDto = new DisabilityPermitHuntingTypeInfoDTO();
            updateHuntingTypeInfoDto.setHuntingType(HuntingType.PIENRIISTA);
            updateHuntingTypeInfoDto.setHuntingTypeDescription("Updated hunting type description");

            final JustificationDTO updateDto = new JustificationDTO("Updated justification",
                    singletonList(updateVehicleDto), singletonList(updateHuntingTypeInfoDto));
            feature.updateJustification(harvestPermitApplication.getId(), updateDto);

            runInTransaction(() -> {
                final DisabilityPermitApplication updatedApplication =
                        applicationRepository.findByHarvestPermitApplication(harvestPermitApplication);
                assertThat(updatedApplication, is(notNullValue()));

                assertThat(updatedApplication.getJustification(), is(equalTo(updateDto.getJustification())));

                final List<DisabilityPermitVehicle> vehicles = vehicleRepository.findByDisabilityPermitApplicationOrderById(application);
                assertThat(vehicles, is(notNullValue()));
                assertThat(vehicles, hasSize(1));

                final DisabilityPermitVehicle vehicle = vehicles.get(0);
                assertThat(vehicle.getType(), is(equalTo(updateVehicleDto.getType())));
                assertThat(vehicle.getDescription(), is(equalTo(updateVehicleDto.getDescription())));
                assertThat(vehicle.getJustification(), is(equalTo(updateVehicleDto.getJustification())));

                final List<DisabilityPermitHuntingTypeInfo> huntingTypeInfos = huntingTypeInfoRepository.findByDisabilityPermitApplicationOrderById(application);
                assertThat(huntingTypeInfos, is(notNullValue()));
                assertThat(huntingTypeInfos, hasSize(1));

                final DisabilityPermitHuntingTypeInfo huntingTypeInfo = huntingTypeInfos.get(0);
                assertThat(huntingTypeInfo.getHuntingType(), is(equalTo(updateHuntingTypeInfoDto.getHuntingType())));
                assertThat(huntingTypeInfo.getHuntingTypeDescription(), is(equalTo(updateHuntingTypeInfoDto.getHuntingTypeDescription())));
            });

        });
    }
}

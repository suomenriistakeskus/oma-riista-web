package fi.riista.feature.permit.application.weapontransportation.justification;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.application.weapontransportation.WeaponTransportationPermitApplication;
import fi.riista.feature.permit.application.weapontransportation.WeaponTransportationPermitApplicationRepository;
import fi.riista.feature.permit.application.weapontransportation.reason.WeaponTransportationReasonType;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;

import java.util.List;

import static java.util.Collections.singletonList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class WeaponTransportationJustificationFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private WeaponTransportationJustificationFeature feature;

    @Resource
    private WeaponTransportationPermitApplicationRepository weaponApplicationRepository;

    @Resource
    private TransportedWeaponRepository transportedWeaponRepository;

    @Resource
    private WeaponTransportationVehicleRepository vehicleRepository;

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
                        "reason",
                        DateUtil.today(),
                        DateUtil.today().plusDays(10),
                        "Justification");

        model().newTransportedWeapon(transportApplication,
                TransportedWeaponType.KIVAARI,
                1,
                null,
                ".57");
        model().newTransportedWeapon(transportApplication,
                TransportedWeaponType.HAULIKKO,
                2,
                null,
                ".44");

        model().newWeaponTransportationVehicle(transportApplication,
                WeaponTransportationVehicleType.AUTO,
                "ABC-123",
                null);

        model().newWeaponTransportationVehicle(transportApplication,
                WeaponTransportationVehicleType.MUU,
                "DEF-456",
                "Description");
    }

    @Test(expected = AccessDeniedException.class)
    public void testGetJustification_unauthorized() {

        onSavedAndAuthenticated(createNewUser(), () -> {
            feature.getJustification(application.getId());
        });
    }

    @Test
    public void testGetJustification() {
        onSavedAndAuthenticated(user, () -> {
            final JustificationDTO dto = feature.getJustification(application.getId());
            assertNotNull(dto);
            assertEquals(transportApplication.getJustification(), dto.getJustification());

            final List<TransportedWeaponDTO> weaponDtos = dto.getTransportedWeapons();
            assertNotNull(weaponDtos);
            assertEquals(2, weaponDtos.size());

            final List<WeaponTransportationVehicleDTO> vehicleDTOs = dto.getVehicles();
            assertNotNull(vehicleDTOs);
            assertEquals(2, vehicleDTOs.size());
        });
    }

    @Test
    public void testUpdateJustification() {
        onSavedAndAuthenticated(user, () -> {
            final TransportedWeaponDTO updateTransportedWeaponDTO =
                    new TransportedWeaponDTO(TransportedWeaponType.METSASTYSJOUSI, 3, "Updated description", ".357");
            final WeaponTransportationVehicleDTO updatedVehicleDTO =
                    new WeaponTransportationVehicleDTO(WeaponTransportationVehicleType.MONKIJA, null, null);
            final JustificationDTO updateJustificationDTO =
                    new JustificationDTO("Updated Justification", singletonList(updateTransportedWeaponDTO), singletonList(updatedVehicleDTO));
            feature.updateJustification(application.getId(), updateJustificationDTO);

            runInTransaction(() -> {
                final WeaponTransportationPermitApplication updatedApplication = weaponApplicationRepository.findByHarvestPermitApplication(application);

                assertNotNull(updatedApplication);
                assertEquals(updateJustificationDTO.getJustification(), updatedApplication.getJustification());

                final List<TransportedWeapon> transportedWeapons =
                        transportedWeaponRepository.findByWeaponTransportationPermitApplicationOrderById(updatedApplication);
                assertEquals(1, transportedWeapons.size());

                final TransportedWeapon weaponInfo = transportedWeapons.get(0);
                assertEquals(updateTransportedWeaponDTO.getType(), weaponInfo.getType());
                assertEquals(updateTransportedWeaponDTO.getAmount(), weaponInfo.getAmount());
                assertEquals(updateTransportedWeaponDTO.getDescription(), weaponInfo.getDescription());
                assertEquals(updateTransportedWeaponDTO.getCaliber(), weaponInfo.getCaliber());

                final List<WeaponTransportationVehicle> vehicles =
                        vehicleRepository.findByWeaponTransportationPermitApplicationOrderById(updatedApplication);
                assertEquals(1, vehicles.size());

                final WeaponTransportationVehicle vehicle = vehicles.get(0);
                assertEquals(updatedVehicleDTO.getType(), vehicle.getType());
                assertEquals(updatedVehicleDTO.getRegisterNumber(), vehicle.getRegisterNumber());
                assertEquals(updatedVehicleDTO.getDescription(), vehicle.getDescription());
            });
        });
    }

}

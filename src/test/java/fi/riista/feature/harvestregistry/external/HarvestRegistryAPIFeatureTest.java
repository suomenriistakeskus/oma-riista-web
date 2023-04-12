package fi.riista.feature.harvestregistry.external;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestregistry.HarvestRegistryItemDTO;
import fi.riista.feature.harvestregistry.HarvestRegistryItemFixtureMixin;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static fi.riista.test.Asserts.assertThat;
import static fi.riista.util.DateUtil.today;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

public class HarvestRegistryAPIFeatureTest extends EmbeddedDatabaseTest implements HarvestRegistryItemFixtureMixin {
    @Resource
    private HarvestRegistryExternalFeature feature;

    private SystemUser userWithoutPermissions;
    private SystemUser userWithControlCriterionPermissions;
    private SystemUser userWithMonitoringCriterionPermissions;

    private Person actualShooter;
    private Person actualShooter2;

    @Before
    public void setup() {
        userWithoutPermissions = createNewUser(SystemUser.Role.ROLE_REST);

        userWithControlCriterionPermissions = createNewUser(SystemUser.Role.ROLE_REST, SystemUserPrivilege.EXPORT_HARVEST_REGISTRY_WITH_CONTROL_CRITERION);

        userWithMonitoringCriterionPermissions = createNewUser(SystemUser.Role.ROLE_REST, SystemUserPrivilege.EXPORT_HARVEST_REGISTRY_WITH_MONITORING_CRITERION);

        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys(rka);

        actualShooter = model().newPerson();
        actualShooter2 = model().newPerson();

        final Harvest harvest = model().newHarvest();
        final Harvest harvest2 = model().newHarvest();

        model().newHarvestRegistryItem(harvest, actualShooter, rka.getOfficialCode(), rhy.getOfficialCode());
        model().newHarvestRegistryItem(harvest2, actualShooter2, rka.getOfficialCode(), rhy.getOfficialCode());

        persistInNewTransaction();
    }

    @Test
    public void testControlQueryWithUserWithControlCriterionPermissions_returnsAlsoShooterNameAndShooterHunterNumber() {
        onSavedAndAuthenticated(userWithControlCriterionPermissions, () -> {
            final HarvestRegistryExternalRequestDTO dto = initialAPIRequest();
            dto.setReason(HarvestRegistryExternalRequestReason.HUNTING_CONTROL);
            dto.setAllSpecies(true);

            final List<HarvestRegistryItemDTO> itemDTOS = feature.export(dto);
            assertThat(itemDTOS, hasSize(2));

            final List<String> shooterNames = new ArrayList<String>() {{
                add(actualShooter.getFullName());
                add(actualShooter2.getFullName());
            }};

            assertThat(itemDTOS.get(0).getShooterName(), is(notNullValue()));
            assertThat(itemDTOS.get(1).getShooterName(), is(notNullValue()));
            assertThat(shooterNames, hasItem(itemDTOS.get(0).getShooterName()));
            assertThat(shooterNames, hasItem(itemDTOS.get(1).getShooterName()));

            final List<String> shooterHunterNumbers = new ArrayList<String>() {{
                add(actualShooter.getHunterNumber());
                add(actualShooter2.getHunterNumber());
            }};

            assertThat(itemDTOS.get(0).getShooterHunterNumber(), is(notNullValue()));
            assertThat(itemDTOS.get(1).getShooterHunterNumber(), is(notNullValue()));
            assertThat(shooterHunterNumbers, hasItem(itemDTOS.get(0).getShooterHunterNumber()));
            assertThat(shooterHunterNumbers, hasItem(itemDTOS.get(1).getShooterHunterNumber()));
        });
    }

    @Test
    public void testControlQueryWithUserWithControlCriterionPermissions_shooterHunterNumberReturnsOnlyItemsWithGivenShooter() {
        onSavedAndAuthenticated(userWithControlCriterionPermissions, () -> {
            final HarvestRegistryExternalRequestDTO dto = initialAPIRequest();
            dto.setReason(HarvestRegistryExternalRequestReason.HUNTING_CONTROL);
            dto.setAllSpecies(true);
            dto.setShooterHunterNumber(actualShooter.getHunterNumber());

            final List<HarvestRegistryItemDTO> itemDTOS = feature.export(dto);
            assertThat(itemDTOS, hasSize(1));

            assertThat(itemDTOS.get(0).getShooterName(), is(notNullValue()));
            assertThat(itemDTOS.get(0).getShooterName(), is(actualShooter.getFullName()));

            assertThat(itemDTOS.get(0).getShooterHunterNumber(), is(notNullValue()));
            assertThat(itemDTOS.get(0).getShooterHunterNumber(), is(actualShooter.getHunterNumber()));
        });
    }

    @Test
    public void testMonitorQueryWithUserWithMonitoringCriterionPermissions_doesntReturnShooterNameAndShooterHunterNumber() {
        onSavedAndAuthenticated(userWithMonitoringCriterionPermissions, () -> {
            final HarvestRegistryExternalRequestDTO dto = initialAPIRequest();
            dto.setReason(HarvestRegistryExternalRequestReason.HUNTING_MONITORING);
            dto.setAllSpecies(true);

            final List<HarvestRegistryItemDTO> itemDTOS = feature.export(dto);
            assertThat(itemDTOS, hasSize(2));

            assertThat(itemDTOS.get(0).getShooterName(), is(nullValue()));
            assertThat(itemDTOS.get(1).getShooterName(), is(nullValue()));

            assertThat(itemDTOS.get(0).getShooterHunterNumber(), is(nullValue()));
            assertThat(itemDTOS.get(1).getShooterHunterNumber(), is(nullValue()));
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMonitorQueryWithUserWithMonitoringCriterionPermissionsWithHunterNumber_throwException() {
        onSavedAndAuthenticated(userWithMonitoringCriterionPermissions, () -> {
            final HarvestRegistryExternalRequestDTO dto = initialAPIRequest();
            dto.setReason(HarvestRegistryExternalRequestReason.HUNTING_MONITORING);
            dto.setAllSpecies(true);
            dto.setShooterHunterNumber(actualShooter.getHunterNumber());
            feature.export(dto);
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testMonitorQueryWithUserWithControlCriterionPermissions_raisesException() {
        onSavedAndAuthenticated(userWithControlCriterionPermissions, () -> {
            final HarvestRegistryExternalRequestDTO dto = initialAPIRequest();
            dto.setReason(HarvestRegistryExternalRequestReason.HUNTING_MONITORING);
            dto.setAllSpecies(true);

            feature.export(dto);
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testControlQueryWithUserWithMonitoringCriterionPermissions_raisesException() {
        onSavedAndAuthenticated(userWithMonitoringCriterionPermissions, () -> {
            final HarvestRegistryExternalRequestDTO dto = initialAPIRequest();
            dto.setReason(HarvestRegistryExternalRequestReason.HUNTING_CONTROL);
            dto.setAllSpecies(true);

            feature.export(dto);
        });
    }


    @Test(expected = AccessDeniedException.class)
    public void testWithUserWithoutPermission_raisesException() {
        onSavedAndAuthenticated(userWithoutPermissions, () -> {
            final HarvestRegistryExternalRequestDTO dto = initialAPIRequest();
            dto.setReason(HarvestRegistryExternalRequestReason.HUNTING_MONITORING);
            dto.setAllSpecies(true);

            feature.export(dto);
        });
    }

    private HarvestRegistryExternalRequestDTO initialAPIRequest() {
        final HarvestRegistryExternalRequestDTO dto = new HarvestRegistryExternalRequestDTO();
        dto.setBeginDate(today().minusMonths(1));
        dto.setEndDate(today().plusMonths(1));
        dto.setPage(0);
        dto.setPageSize(100);
        return dto;
    }
}


package fi.riista.integration.metsastajarekisteri.shootingtest;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.shootingtest.ShootingTestFixtureMixin;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.feature.storage.metadata.PersistentFileMetadataRepository;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.UUID;

import static fi.riista.feature.account.user.SystemUserPrivilege.EXPORT_SHOOTING_TEST_REGISTRY;
import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ShootingTestExportFeatureTest extends EmbeddedDatabaseTest implements ShootingTestFixtureMixin {

    @Resource
    private ShootingTestExportFeature feature;

    @Resource
    private ShootingTestExportTestHelper helper;

    @Resource
    private PersistentFileMetadataRepository metadataRepo;

    @Value("${shootingtest.export.file.uuid}")
    private String exportFileUUID;

    @Test(expected = AccessDeniedException.class)
    public void testExportShootingTestRegistry_accessDenied() {
        onSavedAndAuthenticated(createNewAdmin(), feature::exportShootingTestRegistry);
    }

    @Test
    public void testExportShootingTestRegistry_accessGranted() {
        onSavedAndAuthenticated(createNewApiUser(EXPORT_SHOOTING_TEST_REGISTRY), feature::exportShootingTestRegistry);
    }

    @Test
    public void testWholeChain() throws IOException {
        final LocalDate today = today();
        final SystemUser apiUser = createNewApiUser(EXPORT_SHOOTING_TEST_REGISTRY);

        for (int i = 0; i < 5; i++) {
            createShootingTestAttemptsForSmokeTestCase(today.minusWeeks(i));
        }

        onSavedAndAuthenticated(createNewAdmin(), () -> feature.constructAndStoreShootingTestRegistry(today));
        authenticate(apiUser);

        final byte[] xmlBytes = feature.exportShootingTestRegistry().getBody();

        final PersistentFileMetadata metadata = metadataRepo.findOne(UUID.fromString(exportFileUUID));
        assertNotNull(metadata);

        assertEquals(today, helper.unmarshal(xmlBytes).getRegisterDate());
    }
}

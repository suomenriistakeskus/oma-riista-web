package fi.riista.test;

import fi.riista.config.Constants;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import javax.annotation.Resource;

// This base test class being non-transactional is a distinct design decision.
@Rollback(false)
@ActiveProfiles({ Constants.EMBEDDED_DATABASE, Constants.MOCK_GIS_DATABASE })
public abstract class EmbeddedDatabaseTest extends SpringContextIntegrationTest {

    @Resource
    private DatabaseCleaner dbCleaner;

    @Override
    protected void reset() {
        super.reset();
        dbCleaner.clearManagedEntityTablesFromH2Database();
    }

}

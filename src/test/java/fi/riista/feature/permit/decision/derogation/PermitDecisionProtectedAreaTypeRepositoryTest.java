package fi.riista.feature.permit.decision.derogation;

import fi.riista.feature.permit.application.bird.ProtectedAreaType;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;


public class PermitDecisionProtectedAreaTypeRepositoryTest extends EmbeddedDatabaseTest {

    @Test
    public void testAllEnumerationValuesPersistable() {

        final PermitDecision permitDecision = model().newPermitDecision(model().newRiistanhoitoyhdistys());

        for (ProtectedAreaType type : ProtectedAreaType.values()) {
            model().newPermitDecisionProtectedAreaType(permitDecision, type);
        }

        persistInNewTransaction();
    }


}

package fi.riista.feature.permit.decision.derogation;

import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;


public class PermitDecisionDerogationRepositoryTest extends EmbeddedDatabaseTest {

    @Test
    public void testAllEnumerationValuesPersistable() {

        final PermitDecision permitDecision = model().newPermitDecision(model().newRiistanhoitoyhdistys());
        
        for (PermitDecisionDerogationReasonType type : PermitDecisionDerogationReasonType.values()) {
            model().newPermitDecisionDerogationReason(permitDecision, type);
        }

        persistInNewTransaction();
    }

}

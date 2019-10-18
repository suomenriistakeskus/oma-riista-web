package fi.riista.feature.permit.decision.publish;

import com.google.common.collect.ImmutableSet;
import fi.riista.feature.permit.PermitNumberUtil;
import org.junit.Test;

import static fi.riista.feature.permit.decision.publish.MostRelevantHarvestPermitLookupService.mostRelevantPermitNumber;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MostRelevantHarvestPermitLookupServiceTest {
    private final String p1 = PermitNumberUtil.createPermitNumber(2019, 1, 100);
    private final String p2 = PermitNumberUtil.createPermitNumber(2020, 1, 100);
    private final String p3 = PermitNumberUtil.createPermitNumber(2021, 1, 100);

    @Test
    public void testLookupMostRelevant_emptySets() {
        assertNull(mostRelevantPermitNumber(
                ImmutableSet.of(p1, p2, p3),
                ImmutableSet.of()
        ));

        assertNull(mostRelevantPermitNumber(
                ImmutableSet.of(),
                ImmutableSet.of()
        ));
    }

    @Test
    public void testLookupMostRelevant_pickSmallestDecisionNumber() {
        assertEquals(p1, mostRelevantPermitNumber(
                ImmutableSet.of(p1, p2, p3),
                ImmutableSet.of(p1, p2, p3)
        ));

        assertEquals(p2, mostRelevantPermitNumber(
                ImmutableSet.of(p2, p3),
                ImmutableSet.of(p1, p2, p3)
        ));

        assertEquals(p3, mostRelevantPermitNumber(
                ImmutableSet.of(p3),
                ImmutableSet.of(p1, p2, p3)
        ));
    }

    @Test
    public void testLookupMostRelevant_fallbackToSmallestHarvestPermitNumber() {
        assertEquals(p1, mostRelevantPermitNumber(
                ImmutableSet.of(p2, p3),
                ImmutableSet.of(p1)
        ));

        assertEquals(p1, mostRelevantPermitNumber(
                ImmutableSet.of(p3),
                ImmutableSet.of(p1, p2)
        ));

        assertEquals(p1, mostRelevantPermitNumber(
                ImmutableSet.of(),
                ImmutableSet.of(p1, p2, p3)
        ));
    }
}

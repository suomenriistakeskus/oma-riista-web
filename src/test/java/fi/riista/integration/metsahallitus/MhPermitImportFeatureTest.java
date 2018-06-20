package fi.riista.integration.metsahallitus;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class MhPermitImportFeatureTest {

    @Test
    public void testResultMerge() {
        final Set<Long> aIds = Sets.newHashSet(1L, 2L, 3L);

        final Map<String, Set<String>> aErrors = ImmutableMap.<String, Set<String>> builder()
                .put("a", Sets.newHashSet("errA1", "errA2"))
                .put("x", Sets.newHashSet("errX1", "errX2"))
                .build();
        final MhPermitImportFeature.Result a = new MhPermitImportFeature.Result(aIds, aErrors);

        final Set<Long> bIds = Sets.newHashSet(2L, 3L, 4L);
        final Map<String, Set<String>> bErrors = ImmutableMap.<String, Set<String>> builder()
                .put("b", Sets.newHashSet("errB1", "errB2"))
                .put("x", Sets.newHashSet("errX2", "errX3"))
                .build();
        final MhPermitImportFeature.Result b = new MhPermitImportFeature.Result(bIds, bErrors);

        final MhPermitImportFeature.Result merge = a.merge(b);
        assertEquals(Sets.newHashSet(1L, 2L, 3L, 4L), merge.ids);
        assertEquals(ImmutableMap.<String, Set<String>> builder()
                        .put("a", Sets.newHashSet("errA1", "errA2"))
                        .put("b", Sets.newHashSet("errB1", "errB2"))
                        .put("x", Sets.newHashSet("errX1", "errX2", "errX3"))
                        .build(),
                merge.errors);
    }
}
package fi.riista.feature.organization.rhy;

import java.util.List;

public class MergedRhyMappingTestHelper {

    public static void assignMerges(final List<MergedRhyMapping.RhyMerge> merges) {
        MergedRhyMapping.initMerges(merges);
    }

    public static void reset() {
        MergedRhyMapping.initMerges(MergedRhyMapping.OFFICIAL_MERGES);
    }

    private MergedRhyMappingTestHelper() {
        throw new AssertionError();
    }
}

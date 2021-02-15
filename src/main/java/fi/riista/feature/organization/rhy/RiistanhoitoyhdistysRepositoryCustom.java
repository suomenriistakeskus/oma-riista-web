package fi.riista.feature.organization.rhy;

import java.util.Map;

public interface RiistanhoitoyhdistysRepositoryCustom  {

    Map<Long, Integer> calculateMemberCountsForStatistics(int statisticsYear);
}

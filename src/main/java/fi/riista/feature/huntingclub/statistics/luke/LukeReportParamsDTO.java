package fi.riista.feature.huntingclub.statistics.luke;

import java.util.List;
import java.util.Map;

public class LukeReportParamsDTO {
    private final List<Map<String, Object>> params;
    private final boolean clubReportsExist;

    public LukeReportParamsDTO(
            final List<Map<String, Object>> params,
            final boolean clubReportsExist) {
        this.params = params;
        this.clubReportsExist = clubReportsExist;
    }

    public List<Map<String, Object>> getParams() {
        return params;
    }

    public boolean isClubReportsExist() {
        return clubReportsExist;
    }
}

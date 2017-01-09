package fi.riista.feature.huntingclub.statistics.luke;

import java.util.Map;

public class LukeReportParamsDTO {

    /**
     * Mapping of which presentations are availble for each organisation layer.
     * <p>
     * Organisation -> Presentation -> available
     */
    private final Map<String, Map<String, Boolean>> reportsAvailability;

    private final LukeReportParams.Organisation[] params;


    public LukeReportParamsDTO(
            final Map<String, Map<String, Boolean>> reportsAvailability,
            final LukeReportParams.Organisation[] params) {

        this.reportsAvailability = reportsAvailability;
        this.params = params;
    }

    public LukeReportParams.Organisation[] getParams() {
        return params;
    }

    public Map<String, Map<String, Boolean>> getReportsAvailability() {
        return reportsAvailability;
    }
}

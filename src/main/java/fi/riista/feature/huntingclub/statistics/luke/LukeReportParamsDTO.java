package fi.riista.feature.huntingclub.statistics.luke;

public class LukeReportParamsDTO {

    private final boolean clubReportsExist;

    private final LukeReportParams.Organisation[] params;


    public LukeReportParamsDTO(
            final boolean clubReportsExist,
            final LukeReportParams.Organisation[] params) {

        this.clubReportsExist = clubReportsExist;
        this.params = params;
    }

    public LukeReportParams.Organisation[] getParams() {
        return params;
    }

    public boolean isClubReportsExist() {
        return clubReportsExist;
    }
}

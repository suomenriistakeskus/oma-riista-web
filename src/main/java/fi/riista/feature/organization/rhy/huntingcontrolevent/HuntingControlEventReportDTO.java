package fi.riista.feature.organization.rhy.huntingcontrolevent;

import org.joda.time.LocalDate;

import java.util.List;

public class HuntingControlEventReportDTO {
    private final String rkaName;
    private final String rhyName;
    private final List<HuntingControlEventDTO> events;

    private final String map64Encoded;
    private final LocalDate currentDate;
    private final LocalDate reportStartDate;
    private final LocalDate reportEndDate;

    public HuntingControlEventReportDTO(final String rkaName,
                                        final String rhyName,
                                        final List<HuntingControlEventDTO> events,
                                        final String map64Encoded,
                                        final LocalDate currentDate,
                                        final LocalDate reportStartDate,
                                        final LocalDate reportEndDate) {
        this.rkaName = rkaName;
        this.rhyName = rhyName;
        this.events = events;
        this.map64Encoded = map64Encoded;
        this.currentDate = currentDate;
        this.reportStartDate = reportStartDate;
        this.reportEndDate = reportEndDate;
    }

    public String getRkaName() {
        return rkaName;
    }

    public String getRhyName() {
        return rhyName;
    }

    public List<HuntingControlEventDTO> getEvents() {
        return events;
    }

    public String getMap64Encoded() {
        return map64Encoded;
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public LocalDate getReportStartDate() {
        return reportStartDate;
    }

    public LocalDate getReportEndDate() {
        return reportEndDate;
    }
}

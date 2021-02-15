package fi.riista.feature.organization.rhy;


import org.joda.time.LocalDate;

import javax.validation.constraints.AssertTrue;

public class RhySrvaRotationDTO {

    private SrvaRotation srvaRotation;

    private LocalDate startDate;

    public static RhySrvaRotationDTO create(final Riistanhoitoyhdistys riistanhoitoyhdistys) {
        return new RhySrvaRotationDTO(riistanhoitoyhdistys.getSrvaRotation(), riistanhoitoyhdistys.getRotationStart());
    }

    public RhySrvaRotationDTO(final SrvaRotation srvaRotation, final LocalDate startDate) {
        this.srvaRotation = srvaRotation;
        this.startDate = startDate;
    }

    public RhySrvaRotationDTO() {

    }

    public SrvaRotation getSrvaRotation() {
        return srvaRotation;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setSrvaRotation(final SrvaRotation srvaRotation) {
        this.srvaRotation = srvaRotation;
    }

    public void setStartDate(final LocalDate startDate) {
        this.startDate = startDate;
    }

    @AssertTrue
    boolean isStartDateSetWhenRotationUsed() {
        return srvaRotation == null || startDate != null;
    }
}

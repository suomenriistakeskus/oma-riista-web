package fi.riista.feature.permit.application.send;

import org.joda.time.LocalDate;

import javax.validation.constraints.NotNull;

public class HarvestPermitApplicationSendDTO {
    @NotNull
    private Long id;

    private LocalDate submitDate;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public LocalDate getSubmitDate() {
        return submitDate;
    }

    public void setSubmitDate(final LocalDate submitDate) {
        this.submitDate = submitDate;
    }
}

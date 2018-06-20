package fi.riista.feature.permit.application;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.validation.constraints.NotNull;

public class HarvestPermitApplicationAmendDTO {
    @NotNull
    private Long id;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String changeReason;

    private LocalDate submitDate;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(final String changeReason) {
        this.changeReason = changeReason;
    }

    public LocalDate getSubmitDate() {
        return submitDate;
    }

    public void setSubmitDate(final LocalDate submitDate) {
        this.submitDate = submitDate;
    }
}

package fi.riista.feature.shootingtest;

import fi.riista.feature.common.dto.IdRevisionDTO;

import javax.validation.constraints.Min;

public class ParticipantPaymentUpdateDTO extends IdRevisionDTO {

    @Min(0)
    private int paidAttempts;

    private boolean completed;

    public int getPaidAttempts() {
        return paidAttempts;
    }

    public void setPaidAttempts(final int paidAttempts) {
        this.paidAttempts = paidAttempts;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(final boolean completed) {
        this.completed = completed;
    }
}

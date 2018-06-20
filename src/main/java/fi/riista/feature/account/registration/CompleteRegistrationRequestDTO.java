package fi.riista.feature.account.registration;

import fi.riista.validation.VetumaTransactionId;

public class CompleteRegistrationRequestDTO {
    @VetumaTransactionId
    private String trid;

    public String getTrid() {
        return trid;
    }

    public void setTrid(final String trid) {
        this.trid = trid;
    }
}

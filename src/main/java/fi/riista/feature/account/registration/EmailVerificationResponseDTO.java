package fi.riista.feature.account.registration;

public class EmailVerificationResponseDTO {
    private final String status;
    private final String trid;

    public static EmailVerificationResponseDTO ok(final String trid) {
        return new EmailVerificationResponseDTO("ok", trid);
    }

    public static EmailVerificationResponseDTO error(final String status) {
        return new EmailVerificationResponseDTO(status, null);
    }

    EmailVerificationResponseDTO(final String status, final String trid) {
        this.status = status;
        this.trid = trid;
    }

    public String getStatus() {
        return status;
    }

    public String getTrid() {
        return trid;
    }
}

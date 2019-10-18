package fi.riista.feature.huntingclub.permit.partner;

public class ClubIsNotPermitPartnerException extends IllegalArgumentException {
    public static ClubIsNotPermitPartnerException create(final long clubId, final long permitId) {
        return new ClubIsNotPermitPartnerException(String.format("Club id=%d is not partner of permit id=%d",
                clubId, permitId));
    }

    public ClubIsNotPermitPartnerException(final String s) {
        super(s);
    }
}

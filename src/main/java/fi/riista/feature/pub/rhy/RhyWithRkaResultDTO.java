package fi.riista.feature.pub.rhy;

public class RhyWithRkaResultDTO {

    public static final RhyWithRkaResultDTO EMPTY_RESULT = new RhyWithRkaResultDTO(false, null, null);

    private final boolean isValid;
    private final String rhyCode;
    private final String rkaCode;

    public static RhyWithRkaResultDTO create(final String rhyCode, final String rkaCode) {
        return new RhyWithRkaResultDTO(true, rhyCode, rkaCode);
    }

    private RhyWithRkaResultDTO(final boolean isValid, final String rhyCode, final String rkaCode) {
        this.isValid = isValid;
        this.rhyCode = rhyCode;
        this.rkaCode = rkaCode;
    }

    public String getRhyCode() {
        return rhyCode;
    }

    public String getRkaCode() {
        return rkaCode;
    }
}

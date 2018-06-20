package fi.riista.feature.shootingtest;

import fi.riista.integration.metsastajarekisteri.shootingtest.MR_ShootingTestType;

public enum ShootingTestType {

    MOOSE(4),
    BEAR(4),
    ROE_DEER(4),
    BOW(3);

    private final int numberOfHitsToQualify;

    ShootingTestType(final int numberOfHitsToQualify) {
        this.numberOfHitsToQualify = numberOfHitsToQualify;
    }

    public int getNumberOfHitsToQualify() {
        return numberOfHitsToQualify;
    }

    public MR_ShootingTestType toExportType() {
        switch (this) {
            case MOOSE:
                return MR_ShootingTestType.MOOSE;
            case BEAR:
                return MR_ShootingTestType.BEAR;
            case ROE_DEER:
                return MR_ShootingTestType.ROE_DEER;
            case BOW:
                return MR_ShootingTestType.BOW;
            default:
                throw new IllegalStateException("Unhandled shooting test type");
        }
    }
}

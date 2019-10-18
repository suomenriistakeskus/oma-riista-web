package fi.riista.feature.shootingtest.registration;

import fi.riista.feature.shootingtest.ShootingTestParticipant;

import javax.annotation.Nonnull;

public class SelectedShootingTestTypesDTO {

    private boolean mooseTestIntended;
    private boolean bearTestIntended;
    private boolean roeDeerTestIntended;
    private boolean bowTestIntended;

    public static SelectedShootingTestTypesDTO create(@Nonnull final ShootingTestParticipant participant) {
        final SelectedShootingTestTypesDTO dto = new SelectedShootingTestTypesDTO();
        dto.setMooseTestIntended(participant.isMooseTestIntended());
        dto.setBearTestIntended(participant.isBearTestIntended());
        dto.setRoeDeerTestIntended(participant.isDeerTestIntended());
        dto.setBowTestIntended(participant.isBowTestIntended());
        return dto;
    }

    public boolean isMooseTestIntended() {
        return mooseTestIntended;
    }

    public void setMooseTestIntended(final boolean mooseTestIntended) {
        this.mooseTestIntended = mooseTestIntended;
    }

    public boolean isBearTestIntended() {
        return bearTestIntended;
    }

    public void setBearTestIntended(final boolean bearTestIntended) {
        this.bearTestIntended = bearTestIntended;
    }

    public boolean isRoeDeerTestIntended() {
        return roeDeerTestIntended;
    }

    public void setRoeDeerTestIntended(final boolean roeDeerTestIntended) {
        this.roeDeerTestIntended = roeDeerTestIntended;
    }

    public boolean isBowTestIntended() {
        return bowTestIntended;
    }

    public void setBowTestIntended(final boolean bowTestIntended) {
        this.bowTestIntended = bowTestIntended;
    }
}

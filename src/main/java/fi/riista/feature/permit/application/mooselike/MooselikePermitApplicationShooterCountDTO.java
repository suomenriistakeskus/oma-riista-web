package fi.riista.feature.permit.application.mooselike;

import fi.riista.feature.permit.application.HarvestPermitApplication;

import javax.validation.constraints.Min;

public class MooselikePermitApplicationShooterCountDTO {

    public static MooselikePermitApplicationShooterCountDTO create(final HarvestPermitApplication application) {
        final MooselikePermitApplicationShooterCountDTO dto = new MooselikePermitApplicationShooterCountDTO();
        dto.setShooterOnlyClub(application.getShooterOnlyClub());
        dto.setShooterOtherClubActive(application.getShooterOtherClubActive());
        dto.setShooterOtherClubPassive(application.getShooterOtherClubPassive());
        return dto;
    }

    @Min(0)
    private Integer shooterOnlyClub;

    @Min(0)
    private Integer shooterOtherClubPassive;

    @Min(0)
    private Integer shooterOtherClubActive;

    public Integer getShooterOnlyClub() {
        return shooterOnlyClub;
    }

    public void setShooterOnlyClub(final Integer shooterOnlyClub) {
        this.shooterOnlyClub = shooterOnlyClub;
    }

    public Integer getShooterOtherClubPassive() {
        return shooterOtherClubPassive;
    }

    public void setShooterOtherClubPassive(final Integer shooterOtherClubPassive) {
        this.shooterOtherClubPassive = shooterOtherClubPassive;
    }

    public Integer getShooterOtherClubActive() {
        return shooterOtherClubActive;
    }

    public void setShooterOtherClubActive(final Integer shooterOtherClubActive) {
        this.shooterOtherClubActive = shooterOtherClubActive;
    }
}

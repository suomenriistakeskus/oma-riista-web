package fi.riista.feature.permit.area.partner;

public class HarvestPermitAreaHuntingYearException extends IllegalStateException {

    public HarvestPermitAreaHuntingYearException(final int clubAreaHuntingYear, final int permitAreaHuntingYear) {
        super(String.format(
                "Club area hunting year %d does not match permit area hunting year %d",
                clubAreaHuntingYear, permitAreaHuntingYear));
    }
}

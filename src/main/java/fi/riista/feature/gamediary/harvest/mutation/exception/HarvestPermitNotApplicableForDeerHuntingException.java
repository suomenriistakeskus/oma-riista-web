package fi.riista.feature.gamediary.harvest.mutation.exception;

public class HarvestPermitNotApplicableForDeerHuntingException extends IllegalStateException {

    public HarvestPermitNotApplicableForDeerHuntingException() {
        super("Harvest permit cannot be defined when deer hunting type specified");
    }
}

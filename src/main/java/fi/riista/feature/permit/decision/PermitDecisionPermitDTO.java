package fi.riista.feature.permit.decision;

import fi.riista.feature.harvestpermit.HarvestPermit;

import javax.annotation.Nonnull;

public class PermitDecisionPermitDTO {

    final long id;
    final String permitNumber;

    public static PermitDecisionPermitDTO from(@Nonnull final HarvestPermit permit){
        return new PermitDecisionPermitDTO(permit.getId(), permit.getPermitNumber());
    }

    public PermitDecisionPermitDTO(final long id, final String permitNumber) {
        this.id = id;
        this.permitNumber = permitNumber;
    }
}

package fi.riista.feature.harvestpermit.endofhunting;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;

public class AllPartnersMustFinnishHuntingException extends IllegalStateException {
    public AllPartnersMustFinnishHuntingException(final HarvestPermit harvestPermit, final GameSpecies gameSpecies) {
        super(String.format("All partners for permit %s must finnish hunting gameSpecies %d",
                harvestPermit.getPermitNumber(), gameSpecies.getOfficialCode()));
    }
}

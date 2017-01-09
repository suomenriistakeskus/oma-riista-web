package fi.riista.feature.huntingclub.permit.partner;

import fi.riista.feature.common.entity.Authorizable;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;

public class DeerHuntingPermitPartner extends HuntingPermitPartner implements Authorizable {

    public DeerHuntingPermitPartner(final HarvestPermit permit, final HuntingClub club) {
        super(permit, club);
    }

}

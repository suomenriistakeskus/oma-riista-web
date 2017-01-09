package fi.riista.feature.huntingclub.permit.partner;

import fi.riista.feature.common.entity.Authorizable;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;

public class MooseHuntingPermitPartner extends HuntingPermitPartner implements Authorizable {

    public MooseHuntingPermitPartner(final HarvestPermit permit, final HuntingClub club) {
        super(permit, club);
    }

}

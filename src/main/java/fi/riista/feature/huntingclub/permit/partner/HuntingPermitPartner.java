package fi.riista.feature.huntingclub.permit.partner;

import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.util.F;
import java.util.Objects;

public abstract class HuntingPermitPartner {

    public final HarvestPermit permit;
    public final HuntingClub club;

    protected HuntingPermitPartner(final HarvestPermit permit, final HuntingClub club) {
        this.permit = Objects.requireNonNull(permit, "permit is null");
        this.club = Objects.requireNonNull(club, "club is null");
    }

    public boolean isVerifiedPartner() {
        return F.getUniqueIds(permit.getPermitPartners()).contains(club.getId());
    }

}

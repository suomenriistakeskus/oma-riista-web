package fi.riista.feature.huntingclub.moosedatacard;

import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;

public class MooseDataCardEntitySearchResult {

    public final HuntingClub club;
    public final HarvestPermitSpeciesAmount speciesAmount;
    public final int huntingYear;
    public final long contactPersonId;

    public MooseDataCardEntitySearchResult(final HuntingClub club,
                                           final HarvestPermitSpeciesAmount speciesAmount,
                                           final int huntingYear,
                                           final long contactPersonId) {
        this.club = club;
        this.speciesAmount = speciesAmount;
        this.huntingYear = huntingYear;
        this.contactPersonId = contactPersonId;
    }

    public String getClubOfficialCode() {
        return club.getOfficialCode();
    }
}

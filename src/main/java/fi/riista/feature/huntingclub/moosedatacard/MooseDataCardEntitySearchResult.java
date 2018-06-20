package fi.riista.feature.huntingclub.moosedatacard;

import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.lupahallinta.LHOrganisation;
import io.vavr.control.Either;

public class MooseDataCardEntitySearchResult {

    public final Either<LHOrganisation, HuntingClub> lhOrganisationOrClub;
    public final HarvestPermitSpeciesAmount speciesAmount;
    public final int huntingYear;
    public final long contactPersonId;

    public MooseDataCardEntitySearchResult(final Either<LHOrganisation, HuntingClub> lhOrganisationOrClub,
                                           final HarvestPermitSpeciesAmount speciesAmount,
                                           final int huntingYear,
                                           final long contactPersonId) {

        this.speciesAmount = speciesAmount;
        this.lhOrganisationOrClub = lhOrganisationOrClub;
        this.huntingYear = huntingYear;
        this.contactPersonId = contactPersonId;
    }

    public String getClubOfficialCode() {
        return lhOrganisationOrClub.fold(LHOrganisation::getOfficialCode, HuntingClub::getOfficialCode);
    }
}

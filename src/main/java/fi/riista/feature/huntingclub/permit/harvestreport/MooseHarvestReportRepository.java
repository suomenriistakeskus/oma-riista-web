package fi.riista.feature.huntingclub.permit.harvestreport;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;

public interface MooseHarvestReportRepository extends BaseRepository<MooseHarvestReport, Long> {

    MooseHarvestReport findBySpeciesAmount(HarvestPermitSpeciesAmount speciesAmount);

    default boolean isMooseHarvestReportDone(HarvestPermitSpeciesAmount speciesAmount) {
        return findBySpeciesAmount(speciesAmount) != null;
    }

    default void assertMooseHarvestReportNotDone(HarvestPermitSpeciesAmount speciesAmount) {
        if (isMooseHarvestReportDone(speciesAmount)) {
            throw new MooseHarvestReportDoneException();
        }
    }

    default void assertMooseHarvestReportNotDoneOrModeratorOverriden(HarvestPermitSpeciesAmount speciesAmount) {
        final MooseHarvestReport mooseHarvestReport = findBySpeciesAmount(speciesAmount);
        if (mooseHarvestReport != null && !mooseHarvestReport.isModeratorOverride()) {
            throw new MooseHarvestReportDoneException();
        }
    }

}

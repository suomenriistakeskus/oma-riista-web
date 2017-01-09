package fi.riista.feature.harvestpermit.report.fields;

import fi.riista.feature.gamediary.GameSpecies_;
import fi.riista.util.jpa.JpaSpecs;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;

public class HarvestReportFieldsSpecs {

    public static Specification<HarvestReportFields> withGameSpeciesCode(final int gameSpeciesCode) {
        return JpaSpecs.equal(HarvestReportFields_.species, GameSpecies_.officialCode, gameSpeciesCode);
    }

    public static Specification<HarvestReportFields> withGameSpeciesCodes(final Collection<Long> ids) {
        return JpaSpecs.inIdCollection(HarvestReportFields_.species, GameSpecies_.id, ids);
    }

    public static Specification<HarvestReportFields> withUsedWithPermit(final boolean usedWithPermit) {
        return JpaSpecs.equal(HarvestReportFields_.usedWithPermit, usedWithPermit);
    }

    private HarvestReportFieldsSpecs() {
    }

}

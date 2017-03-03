package fi.riista.feature.harvestpermit.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.dto.DoNotValidate;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.HarvestDTOTransformer;
import fi.riista.feature.harvestpermit.HarvestPermit;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

public class HarvestReportForListPermitDTO extends HarvestReportDTOBase {

    @Nonnull
    public static HarvestReportForListPermitDTO create(@Nonnull final HarvestReport report,
                                                       @Nonnull final SystemUser user,
                                                       @Nonnull final Map<Long, SystemUser> moderatorCreators,
                                                       final boolean includeHarvests,
                                                       @Nonnull final HarvestDTOTransformer dtoTransformer,
                                                       final Predicate<Harvest> harvestFilter) {

        Objects.requireNonNull(report);
        Objects.requireNonNull(user);
        Objects.requireNonNull(moderatorCreators);
        Objects.requireNonNull(dtoTransformer);
        if (includeHarvests) {
            Objects.requireNonNull(harvestFilter, "When includeHarvests=true, then harvestFilter must be non-null");
        }
        final HarvestReportForListPermitDTO dto = new HarvestReportForListPermitDTO();
        dto.setState(report.getState());

        final HarvestPermit permit = report.getHarvestPermit();
        dto.setHarvestsAsList(permit.isHarvestsAsList());
        dto.setRhyId(permit.getRhy().getId());

        if (includeHarvests) {
            dto.setHarvests(dtoTransformer.transform(report.getHarvests().stream().filter(harvestFilter).collect(toList())));
        }

        HarvestReportDTOBase.copyBaseFields(report, dto, user, moderatorCreators);
        return dto;
    }

    private boolean harvestsAsList;
    private List<GameSpeciesDTO> species;

    @DoNotValidate
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<HarvestDTO> harvests;

    public boolean isHarvestsAsList() {
        return harvestsAsList;
    }

    public void setHarvestsAsList(boolean harvestsAsList) {
        this.harvestsAsList = harvestsAsList;
    }

    public List<GameSpeciesDTO> getSpecies() {
        return species;
    }

    public void setSpecies(List<GameSpeciesDTO> species) {
        this.species = species;
    }

    public List<HarvestDTO> getHarvests() {
        return harvests;
    }

    public void setHarvests(List<HarvestDTO> harvests) {
        this.harvests = harvests;
    }
}

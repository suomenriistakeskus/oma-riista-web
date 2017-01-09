package fi.riista.feature.harvestpermit.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.dto.DoNotValidate;
import fi.riista.feature.gamediary.harvest.HarvestDTOTransformer;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.util.F;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HarvestReportForListPermitDTO extends HarvestReportDTOBase {

    @Nonnull
    public static List<HarvestReportForListPermitDTO> create(@Nonnull Iterable<HarvestReport> reports,
                                                             final SystemUser user,
                                                             final Map<Long, SystemUser> moderatorCreators,
                                                             final boolean includeHarvests,
                                                             final HarvestDTOTransformer dtoTransformer) {

        return F.mapNonNullsToList(reports, report -> HarvestReportForListPermitDTO.create(
                report, user, moderatorCreators, includeHarvests, dtoTransformer));
    }


    @Nonnull
    public static HarvestReportForListPermitDTO create(@Nonnull final HarvestReport report,
                                                       final SystemUser user,
                                                       final boolean includeHarvests,
                                                       final HarvestDTOTransformer dtoTransformer) {
        return create(report, user, Collections.emptyMap(), includeHarvests, dtoTransformer);
    }

    @Nonnull
    public static HarvestReportForListPermitDTO create(@Nonnull final HarvestReport report,
                                                       final SystemUser user,
                                                       final Map<Long, SystemUser> moderatorCreators,
                                                       final boolean includeHarvests,
                                                       final HarvestDTOTransformer dtoTransformer) {
        final HarvestReportForListPermitDTO dto = createReadOnly(report, includeHarvests, dtoTransformer);
        HarvestReportDTOBase.copyBaseFields(report, dto, user, moderatorCreators);
        return dto;
    }

    @Nonnull
    private static HarvestReportForListPermitDTO createReadOnly(@Nonnull final HarvestReport report,
                                                                final boolean includeHarvests,
                                                                final HarvestDTOTransformer dtoTransformer) {
        final HarvestReportForListPermitDTO dto = new HarvestReportForListPermitDTO();
        dto.setState(report.getState());

        final HarvestPermit permit = report.getHarvestPermit();
        dto.setHarvestsAsList(permit.isHarvestsAsList());
        dto.setRhyId(permit.getRhy().getId());

        if (includeHarvests) {
            dto.setHarvests(dtoTransformer.transform(Lists.newArrayList(report.getHarvests())));
        }

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

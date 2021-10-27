package fi.riista.feature.largecarnivorereport;

import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.gamediary.srva.SrvaEventDTO;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.harvestpermit.season.HarvestArea;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysDTO;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LargeCarnivoreExcelExportDTO {

    private final int huntingYear;
    private final List<GameSpeciesDTO> reportSpecies;
    private final Map<Integer, List<LargeCarnivorePermitInfoDTO>> derogations;
    private final Map<HarvestPermitCategory, List<LargeCarnivorePermitInfoDTO>> stockManagements;
    private final Map<HarvestArea.HarvestAreaDetailedType, Integer> bearQuotaHarvests;
    private final Map<HarvestArea.HarvestAreaDetailedType, Integer> bearQuotas;
    private final Map<Integer, List<LargeCarnivorePermitInfoDTO>> deportations;
    private final Map<Integer, List<LargeCarnivorePermitInfoDTO>> research;
    private final Map<Integer, List<LargeCarnivoreSrvaEventDTO>> srvas;
    private final Map<Integer, List<LargeCarnivoreOtherwiseDeceasedDTO>> otherwiseDeceased;
    private final Map<Integer, Integer> totalHarvests;
    private final Map<Integer, Integer> reindeerAreaHarvests;
    private final Map<Integer, Integer> totalOtherwiseDeceased;
    private final Map<Integer, Integer> reindeerAreaOtherwiseDeceased;

    public LargeCarnivoreExcelExportDTO(final int huntingYear,
                                        final List<GameSpeciesDTO> reportSpecies,
                                        final Map<Integer, List<LargeCarnivorePermitInfoDTO>> derogations,
                                        final Map<HarvestPermitCategory, List<LargeCarnivorePermitInfoDTO>> stockManagements,
                                        final Map<HarvestArea.HarvestAreaDetailedType, Integer> bearQuotaHarvests,
                                        final Map<HarvestArea.HarvestAreaDetailedType, Integer> bearQuotas,
                                        final Map<Integer, List<LargeCarnivorePermitInfoDTO>> deportations,
                                        final Map<Integer, List<LargeCarnivorePermitInfoDTO>> research,
                                        final Map<Integer, List<LargeCarnivoreSrvaEventDTO>> srvas,
                                        final Map<Integer, List<LargeCarnivoreOtherwiseDeceasedDTO>> otherwiseDeceased,
                                        final Map<Integer, Integer> totalHarvests,
                                        final Map<Integer, Integer> reindeerAreaHarvests,
                                        final Map<Integer, Integer> totalOtherwiseDeceased,
                                        final Map<Integer, Integer> reindeerAreaOtherwiseDeceased) {
        this.huntingYear = huntingYear;
        this.reportSpecies = Optional.ofNullable(reportSpecies).orElseGet(Collections::emptyList);
        this.derogations = Optional.ofNullable(derogations).orElseGet(Collections::emptyMap);
        this.stockManagements = Optional.ofNullable(stockManagements).orElseGet(Collections::emptyMap);
        this.bearQuotaHarvests = Optional.ofNullable(bearQuotaHarvests).orElseGet(Collections::emptyMap);
        this.bearQuotas = Optional.ofNullable(bearQuotas).orElseGet(Collections::emptyMap);
        this.deportations = Optional.ofNullable(deportations).orElseGet(Collections::emptyMap);
        this.research = Optional.ofNullable(research).orElseGet(Collections::emptyMap);
        this.srvas = Optional.ofNullable(srvas).orElseGet(Collections::emptyMap);
        this.otherwiseDeceased = Optional.ofNullable(otherwiseDeceased).orElseGet(Collections::emptyMap);
        this.totalHarvests = Optional.ofNullable(totalHarvests).orElseGet(Collections::emptyMap);
        this.reindeerAreaHarvests = Optional.ofNullable(reindeerAreaHarvests).orElseGet(Collections::emptyMap);
        this.totalOtherwiseDeceased = Optional.ofNullable(totalOtherwiseDeceased).orElseGet(Collections::emptyMap);
        this.reindeerAreaOtherwiseDeceased = Optional.ofNullable(reindeerAreaOtherwiseDeceased).orElseGet(Collections::emptyMap);
    }

    public int getHuntingYear() {
        return huntingYear;
    }

    public List<GameSpeciesDTO> getReportSpecies() {
        return reportSpecies;
    }

    public Map<Integer, List<LargeCarnivorePermitInfoDTO>> getDerogations() {
        return derogations;
    }

    public Map<HarvestPermitCategory, List<LargeCarnivorePermitInfoDTO>> getStockManagements() {
        return stockManagements;
    }

    public Map<HarvestArea.HarvestAreaDetailedType, Integer> getBearQuotaHarvests() {
        return bearQuotaHarvests;
    }

    public Map<HarvestArea.HarvestAreaDetailedType, Integer> getBearQuotas() {
        return bearQuotas;
    }

    public Map<Integer, List<LargeCarnivorePermitInfoDTO>> getDeportations() {
        return deportations;
    }

    public Map<Integer, List<LargeCarnivorePermitInfoDTO>> getResearch() {
        return research;
    }

    public Map<Integer, List<LargeCarnivoreSrvaEventDTO>> getSrvas() {
        return srvas;
    }

    public Map<Integer, List<LargeCarnivoreOtherwiseDeceasedDTO>> getOtherwiseDeceased() {
        return otherwiseDeceased;
    }

    public Map<Integer, Integer> getTotalHarvests() {
        return totalHarvests;
    }

    public Map<Integer, Integer> getReindeerAreaHarvests() {
        return reindeerAreaHarvests;
    }

    public Map<Integer, Integer> getTotalOtherwiseDeceased() {
        return totalOtherwiseDeceased;
    }

    public Map<Integer, Integer> getReindeerAreaOtherwiseDeceased() {
        return reindeerAreaOtherwiseDeceased;
    }
}

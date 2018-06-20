package fi.riista.feature.harvestpermit.report.category;

import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFieldsDTO;
import fi.riista.feature.harvestpermit.season.HarvestAreaDTO;
import fi.riista.feature.harvestpermit.season.HarvestQuota;
import fi.riista.feature.harvestpermit.season.HarvestSeason;
import fi.riista.util.F;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HarvestReportCategoryDTO {
    public enum CategoryType {
        PERMIT,
        SEASON
    }

    public static class SeasonDTO implements Has2BeginEndDates {
        private Long id;

        private Map<String, String> name;

        private LocalDate beginDate;
        private LocalDate endDate;
        private LocalDate endOfReportingDate;

        private LocalDate beginDate2;
        private LocalDate endDate2;
        private LocalDate endOfReportingDate2;

        private SeasonDTO(final HarvestSeason season) {
            setId(season.getId());
            setName(season.getNameLocalisation().asMap());
            copyDatesFrom(season);
            setEndOfReportingDate(season.getEndOfReportingDate());
            setEndOfReportingDate2(season.getEndOfReportingDate2());
        }

        public Long getId() {
            return this.id;
        }

        public void setId(final Long id) {
            this.id = id;
        }

        public Map<String, String> getName() {
            return name;
        }

        public void setName(final Map<String, String> name) {
            this.name = name;
        }

        @Override
        public LocalDate getBeginDate() {
            return beginDate;
        }

        @Override
        public void setBeginDate(final LocalDate beginDate) {
            this.beginDate = beginDate;
        }

        @Override
        public LocalDate getEndDate() {
            return endDate;
        }

        @Override
        public void setEndDate(final LocalDate endDate) {
            this.endDate = endDate;
        }

        public LocalDate getEndOfReportingDate() {
            return endOfReportingDate;
        }

        public void setEndOfReportingDate(final LocalDate endOfReportingDate) {
            this.endOfReportingDate = endOfReportingDate;
        }

        @Override
        public LocalDate getBeginDate2() {
            return beginDate2;
        }

        @Override
        public void setBeginDate2(final LocalDate beginDate2) {
            this.beginDate2 = beginDate2;
        }

        @Override
        public LocalDate getEndDate2() {
            return endDate2;
        }

        @Override
        public void setEndDate2(final LocalDate endDate2) {
            this.endDate2 = endDate2;
        }

        public LocalDate getEndOfReportingDate2() {
            return endOfReportingDate2;
        }

        public void setEndOfReportingDate2(final LocalDate endOfReportingDate2) {
            this.endOfReportingDate2 = endOfReportingDate2;
        }
    }

    @Nonnull
    public static HarvestReportCategoryDTO createForSeason(final @Nonnull HarvestSeason season,
                                                           final RequiredHarvestFieldsDTO fields) {
        final HarvestReportCategoryDTO dto = new HarvestReportCategoryDTO();

        dto.setType(CategoryType.SEASON);
        dto.setSpecies(GameSpeciesDTO.create(season.getSpecies()));
        dto.setFields(fields);
        dto.setSeason(new SeasonDTO(season));
        dto.setHarvestAreas(F.mapNonNullsToSet(season.getQuotas(), HarvestQuota::getHarvestArea).stream()
                .map(HarvestAreaDTO::create).collect(Collectors.toList()));

        return dto;
    }

    @Nonnull
    public static HarvestReportCategoryDTO createForPermit(final @Nonnull GameSpecies species,
                                                           final RequiredHarvestFieldsDTO fields) {
        final HarvestReportCategoryDTO dto = new HarvestReportCategoryDTO();

        dto.setType(CategoryType.PERMIT);
        dto.setSpecies(GameSpeciesDTO.create(species));
        dto.setFields(fields);
        dto.setSeason(null);

        return dto;
    }

    private CategoryType type;
    private GameSpeciesDTO species;
    private SeasonDTO season;
    private List<HarvestAreaDTO> harvestAreas;
    private RequiredHarvestFieldsDTO fields;

    public CategoryType getType() {
        return type;
    }

    public void setType(final CategoryType type) {
        this.type = type;
    }

    public GameSpeciesDTO getSpecies() {
        return species;
    }

    public void setSpecies(final GameSpeciesDTO species) {
        this.species = species;
    }

    public SeasonDTO getSeason() {
        return season;
    }

    public void setSeason(final SeasonDTO season) {
        this.season = season;
    }

    public List<HarvestAreaDTO> getHarvestAreas() {
        return harvestAreas;
    }

    public void setHarvestAreas(final List<HarvestAreaDTO> harvestAreas) {
        this.harvestAreas = harvestAreas;
    }

    public RequiredHarvestFieldsDTO getFields() {
        return fields;
    }

    public void setFields(final RequiredHarvestFieldsDTO fields) {
        this.fields = fields;
    }
}

package fi.riista.feature.harvestpermit.season;

import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.util.F;
import fi.riista.validation.DoNotValidate;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class HarvestSeasonDTO implements Has2BeginEndDates {
    @Nonnull
    public static HarvestSeasonDTO createWithSpeciesAndQuotas(final @Nonnull HarvestSeason season) {
        final HarvestSeasonDTO dto = create(season);

        dto.setSpecies(GameSpeciesDTO.create(season.getSpecies()));
        dto.setQuotas(F.mapNonNullsToList(season.getQuotas(), HarvestQuotaDTO::create));

        return dto;
    }

    @Nonnull
    public static HarvestSeasonDTO create(final @Nonnull HarvestSeason season) {
        final HarvestSeasonDTO dto = new HarvestSeasonDTO();
        dto.setId(season.getId());
        dto.setName(season.getNameLocalisation().asMap());
        dto.copyDatesFrom(season);
        dto.setEndOfReportingDate(season.getEndOfReportingDate());
        dto.setEndOfReportingDate2(season.getEndOfReportingDate2());

        return dto;
    }

    private Long id;

    private Map<String, String> name;

    @DoNotValidate
    private GameSpeciesDTO species;

    private LocalDate beginDate;
    private LocalDate endDate;
    private LocalDate endOfReportingDate;

    private LocalDate beginDate2;
    private LocalDate endDate2;
    private LocalDate endOfReportingDate2;

    private List<HarvestQuotaDTO> quotas;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, String> getName() {
        return name;
    }

    public void setName(final Map<String, String> name) {
        this.name = name;
    }

    public GameSpeciesDTO getSpecies() {
        return species;
    }

    public void setSpecies(final GameSpeciesDTO species) {
        this.species = species;
    }

    @Override
    public LocalDate getBeginDate() {
        return beginDate;
    }

    @Override
    public void setBeginDate(LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    @Override
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDate getEndOfReportingDate() {
        return endOfReportingDate;
    }

    public void setEndOfReportingDate(LocalDate endOfReportingDate) {
        this.endOfReportingDate = endOfReportingDate;
    }

    @Override
    public LocalDate getBeginDate2() {
        return beginDate2;
    }

    @Override
    public void setBeginDate2(LocalDate beginDate2) {
        this.beginDate2 = beginDate2;
    }

    @Override
    public LocalDate getEndDate2() {
        return endDate2;
    }

    @Override
    public void setEndDate2(LocalDate endDate2) {
        this.endDate2 = endDate2;
    }

    public LocalDate getEndOfReportingDate2() {
        return endOfReportingDate2;
    }

    public void setEndOfReportingDate2(LocalDate endOfReportingDate2) {
        this.endOfReportingDate2 = endOfReportingDate2;
    }

    public List<HarvestQuotaDTO> getQuotas() {
        return quotas;
    }

    public void setQuotas(List<HarvestQuotaDTO> quotas) {
        this.quotas = quotas;
    }
}

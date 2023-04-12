package fi.riista.feature.harvestpermit.season;

import com.fasterxml.jackson.annotation.JsonProperty;
import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.util.F;
import fi.riista.validation.DoNotValidate;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import java.util.List;
import java.util.Map;

public class HarvestSeasonDTO extends BaseEntityDTO<Long> implements Has2BeginEndDates {
    @Nonnull
    public static HarvestSeasonDTO createWithSpeciesAndQuotas(final @Nonnull HarvestSeason season,
                                                              final List<HarvestQuota> quotas) {
        final HarvestSeasonDTO dto = create(season);

        final GameSpecies species = season.getSpecies();
        dto.setSpecies(GameSpeciesDTO.create(species));
        dto.setGameSpeciesCode(species.getOfficialCode());
        if (quotas != null) {
            dto.setQuotas(F.mapNonNullsToList(quotas, HarvestQuotaDTO::create));
        }
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
    private Integer rev;

    private Map<String, String> name;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @DoNotValidate
    private GameSpeciesDTO species;
    private int gameSpeciesCode;

    private LocalDate beginDate;
    private LocalDate endDate;
    private LocalDate endOfReportingDate;

    private LocalDate beginDate2;
    private LocalDate endDate2;
    private LocalDate endOfReportingDate2;

    @AssertTrue
    public boolean isEndOfReportingDateValid() {
        return endOfReportingDate != null && (endOfReportingDate.isEqual(endDate) || endOfReportingDate.isAfter(endDate));
    }

    @AssertTrue
    public boolean isSecondPeriodValid() {
        final boolean allNull = F.allNull(beginDate2, endDate2, endOfReportingDate2);
        final boolean allSet = F.allNotNull(beginDate2, endDate2, endOfReportingDate2);
        final boolean endOfReportingDateOrderValid = endOfReportingDate2 != null &&
                (endOfReportingDate2.isEqual(endDate2) || endOfReportingDate2.isAfter(endDate2));
        return allNull || (allSet && endOfReportingDateOrderValid);
    }

    @Valid
    private List<HarvestQuotaDTO> quotas;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
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

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(final int gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }
}

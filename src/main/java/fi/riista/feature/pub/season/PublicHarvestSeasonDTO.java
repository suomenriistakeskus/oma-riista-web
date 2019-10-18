package fi.riista.feature.pub.season;

import fi.riista.feature.common.dto.Has2BeginEndDatesDTO;
import fi.riista.feature.harvestpermit.season.HarvestSeason;
import fi.riista.util.F;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PublicHarvestSeasonDTO extends Has2BeginEndDatesDTO {

    private String nameFinnish;
    private String nameSwedish;
    private LocalDate endOfReportingDate;
    private LocalDate endOfReportingDate2;
    private List<PublicHarvestQuotaDTO> quotas;

    public static @Nonnull
    PublicHarvestSeasonDTO create(
            @Nonnull HarvestSeason season,
            final Map<Long, Integer> quotaIdToUsedQuota) {

        Objects.requireNonNull(season, "season must not be null");

        PublicHarvestSeasonDTO dto = new PublicHarvestSeasonDTO();

        dto.setNameFinnish(season.getNameFinnish());
        dto.setNameSwedish(season.getNameSwedish());

        dto.copyDatesFrom(season);
        dto.setEndOfReportingDate(season.getEndOfReportingDate());
        dto.setEndOfReportingDate2(season.getEndOfReportingDate2());

        dto.setQuotas(F.mapNonNullsToList(season.getQuotas(), quota -> {
            Objects.requireNonNull(quota);
            return PublicHarvestQuotaDTO.create(quota, quotaIdToUsedQuota.get(quota.getId()));
        }));

        return dto;
    }

    public LocalDate getEndOfReportingDate() {
        return endOfReportingDate;
    }

    public void setEndOfReportingDate(LocalDate endOfReportingDate) {
        this.endOfReportingDate = endOfReportingDate;
    }

    public LocalDate getEndOfReportingDate2() {
        return endOfReportingDate2;
    }

    public void setEndOfReportingDate2(LocalDate endOfReportingDate2) {
        this.endOfReportingDate2 = endOfReportingDate2;
    }

    public void setQuotas(List<PublicHarvestQuotaDTO> quotas) {
        this.quotas = quotas;
    }

    public List<PublicHarvestQuotaDTO> getQuotas() {
        return quotas;
    }

    public void setNameFinnish(String nameFinnish) {
        this.nameFinnish = nameFinnish;
    }

    public String getNameFinnish() {
        return nameFinnish;
    }

    public void setNameSwedish(String nameSwedish) {
        this.nameSwedish = nameSwedish;
    }

    public String getNameSwedish() {
        return nameSwedish;
    }
}

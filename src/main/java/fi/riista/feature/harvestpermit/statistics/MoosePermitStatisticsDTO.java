package fi.riista.feature.harvestpermit.statistics;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fi.riista.feature.huntingclub.permit.statistics.HarvestCountDTO;
import fi.riista.util.LocalisedString;
import fi.riista.util.LocalisedStringJsonSerializer;

import javax.annotation.Nonnull;

import static fi.riista.util.NumberUtils.percentRatio;
import static fi.riista.util.NumberUtils.ratio;
import static java.util.Objects.requireNonNull;

public class MoosePermitStatisticsDTO {
    private final Long permitId;
    private final String permitNumber;
    private final LocalisedString permitHolder;
    private final String permitHolderOfficialCode;
    private final Integer partnerCount;
    private final Integer permitLandAreaSize;
    private final LocalisedString speciesName;
    private final LocalisedString rhy;
    private final LocalisedString rka;
    private final LocalisedString hta;

    private final MoosePermitStatisticsAmountDTO permitAmount;
    private final HarvestCountDTO harvestCount;
    private final MoosePermitStatisticsAreaAndPopulation areaAndPopulation;

    MoosePermitStatisticsDTO(final @Nonnull LocalisedString speciesName,
                             final LocalisedString rhy,
                             final LocalisedString rka,
                             final LocalisedString hta,
                             final MoosePermitStatisticsPermitInfo permitInfo,
                             final @Nonnull HarvestCountDTO harvestCount,
                             final @Nonnull MoosePermitStatisticsAmountDTO permitAmount,
                             final @Nonnull MoosePermitStatisticsAreaAndPopulation areaAndPopulation) {
        this.harvestCount = requireNonNull(harvestCount);
        this.permitAmount = requireNonNull(permitAmount);
        this.areaAndPopulation = requireNonNull(areaAndPopulation);
        this.speciesName = requireNonNull(speciesName);
        this.rhy = rhy;
        this.rka = rka;
        this.hta = hta;

        if (permitInfo != null) {
            this.permitId = permitInfo.getPermitId();
            this.permitNumber = permitInfo.getPermitNumber();
            this.permitHolder = permitInfo.getPermitHolderName();
            this.permitHolderOfficialCode = permitInfo.getPermitHolderOfficialCode();
            this.permitLandAreaSize = permitInfo.getPermitAreaSize();
            this.partnerCount = permitInfo.getPartnerCount();
        } else {
            this.permitId = null;
            this.permitNumber = null;
            this.permitHolder = null;
            this.permitHolderOfficialCode = null;
            this.permitLandAreaSize = null;
            this.partnerCount = null;
        }
    }

    MoosePermitStatisticsDTO(final @Nonnull LocalisedString speciesName,
                             final @Nonnull HarvestCountDTO harvestCount,
                             final @Nonnull MoosePermitStatisticsAmountDTO permitAmount,
                             final @Nonnull MoosePermitStatisticsAreaAndPopulation areaAndPopulation) {
        this.harvestCount = requireNonNull(harvestCount);
        this.permitAmount = requireNonNull(permitAmount);
        this.areaAndPopulation = requireNonNull(areaAndPopulation);
        this.speciesName = requireNonNull(speciesName);
        this.permitLandAreaSize = null;
        this.partnerCount = null;
        this.permitId = null;
        this.permitNumber = null;
        this.permitHolder = null;
        this.permitHolderOfficialCode = null;
        this.rhy = null;
        this.rka = null;
        this.hta = null;
    }

    // Json accessor

    @JsonGetter
    public double getUsedPermitPercentage() {
        return percentRatio(harvestCount.getRequiredPermitAmount(), permitAmount.getTotal());
    }

    @JsonGetter
    public double getTotalHarvestPer1000ha() {
        return ratio(harvestCount.getTotal(), areaAndPopulation.getTotalAreaSize() / 1000.0);
    }

    @JsonGetter
    public double getTotalPermitsPer1000ha() {
        return ratio(permitAmount.getTotal(), areaAndPopulation.getTotalAreaSize() / 1000.0);
    }

    @JsonGetter
    public double getApplicationPermitsPer1000ha() {
        return ratio(permitAmount.getApplication(), areaAndPopulation.getTotalAreaSize() / 1000.0);
    }

    // Accessors

    public Long getPermitId() {
        return permitId;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    @JsonSerialize(using = LocalisedStringJsonSerializer.class)
    public LocalisedString getPermitHolder() {
        return permitHolder;
    }

    public String getPermitHolderOfficialCode() {
        return permitHolderOfficialCode;
    }

    @JsonSerialize(using = LocalisedStringJsonSerializer.class)
    public LocalisedString getSpeciesName() {
        return speciesName;
    }

    @JsonSerialize(using = LocalisedStringJsonSerializer.class)
    public LocalisedString getRhy() {
        return rhy;
    }

    @JsonSerialize(using = LocalisedStringJsonSerializer.class)
    public LocalisedString getRka() {
        return rka;
    }

    @JsonSerialize(using = LocalisedStringJsonSerializer.class)
    public LocalisedString getHta() {
        return hta;
    }

    public Integer getPartnerCount() {
        return partnerCount;
    }

    public Integer getPermitLandAreaSize() {
        return permitLandAreaSize;
    }

    public MoosePermitStatisticsAmountDTO getPermitAmount() {
        return permitAmount;
    }

    public HarvestCountDTO getHarvestCount() {
        return harvestCount;
    }

    public MoosePermitStatisticsAreaAndPopulation getAreaAndPopulation() {
        return areaAndPopulation;
    }
}

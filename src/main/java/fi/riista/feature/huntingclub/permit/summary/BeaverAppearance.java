package fi.riista.feature.huntingclub.permit.summary;

import fi.riista.util.F;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.Optional;

@Embeddable
@Access(AccessType.FIELD)
public class BeaverAppearance implements Serializable {

    public static BeaverAppearance revamp(@Nullable final BeaverAppearance appearance) {
        return Optional.ofNullable(appearance)
                .map(BeaverAppearance::new)
                .orElse(null);
    }

    @Column
    private Boolean appeared;

    @Convert(converter = TrendOfPopulationGrowthConverter.class)
    @Column(length = 1)
    private TrendOfPopulationGrowth trendOfPopulationGrowth;

    @Min(0)
    @Column
    private Integer amountOfInhabitedWinterNests;

    @Min(0)
    @Column
    private Integer harvestAmount;

    /**
     * Majavatuhoalueiden hehtaarimäärä
     */
    @Min(0)
    @Column
    private Integer areaOfDamage;

    /**
     * Veden vallassa olevan alueen hehtaarimäärä
     */
    @Min(0)
    @Column
    private Integer areaOccupiedByWater;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "text")
    private String additionalInfo;

    public BeaverAppearance() {
    }

    public BeaverAppearance(@Nonnull final BeaverAppearance that) {
        this.appeared = that.appeared;

        if (!Boolean.FALSE.equals(this.appeared)) {
            this.trendOfPopulationGrowth = that.trendOfPopulationGrowth;
            this.amountOfInhabitedWinterNests = that.amountOfInhabitedWinterNests;
            this.harvestAmount = that.harvestAmount;
            this.areaOfDamage = that.areaOfDamage;
            this.areaOccupiedByWater = that.areaOccupiedByWater;
            this.additionalInfo = that.additionalInfo;
        }
    }

    @AssertTrue
    public boolean isValid() {
        return !Boolean.FALSE.equals(appeared) ||
                F.allNull(trendOfPopulationGrowth, amountOfInhabitedWinterNests, harvestAmount, areaOfDamage,
                        areaOccupiedByWater, additionalInfo);
    }

    // Accessors -->

    public Boolean getAppeared() {
        return appeared;
    }

    public void setAppeared(final Boolean appeared) {
        this.appeared = appeared;
    }

    public TrendOfPopulationGrowth getTrendOfPopulationGrowth() {
        return trendOfPopulationGrowth;
    }

    public void setTrendOfPopulationGrowth(final TrendOfPopulationGrowth trendOfPopulationGrowth) {
        this.trendOfPopulationGrowth = trendOfPopulationGrowth;
    }

    public Integer getAmountOfInhabitedWinterNests() {
        return amountOfInhabitedWinterNests;
    }

    public void setAmountOfInhabitedWinterNests(final Integer amountOfInhabitedWinterNests) {
        this.amountOfInhabitedWinterNests = amountOfInhabitedWinterNests;
    }

    public Integer getHarvestAmount() {
        return harvestAmount;
    }

    public void setHarvestAmount(final Integer harvestAmount) {
        this.harvestAmount = harvestAmount;
    }

    public Integer getAreaOfDamage() {
        return areaOfDamage;
    }

    public void setAreaOfDamage(final Integer areaOfDamage) {
        this.areaOfDamage = areaOfDamage;
    }

    public Integer getAreaOccupiedByWater() {
        return areaOccupiedByWater;
    }

    public void setAreaOccupiedByWater(final Integer areaOccupiedByWater) {
        this.areaOccupiedByWater = areaOccupiedByWater;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(final String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}

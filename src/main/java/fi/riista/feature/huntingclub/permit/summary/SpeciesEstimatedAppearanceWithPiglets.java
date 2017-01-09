package fi.riista.feature.huntingclub.permit.summary;

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
public class SpeciesEstimatedAppearanceWithPiglets implements Serializable {


    public static SpeciesEstimatedAppearanceWithPiglets revamp(
            @Nullable final SpeciesEstimatedAppearanceWithPiglets appearance) {
        return Optional.ofNullable(appearance)
                .map(SpeciesEstimatedAppearanceWithPiglets::cleanedCopy)
                .orElse(null);
    }

    @Column
    private Boolean appeared;

    @Convert(converter = TrendOfPopulationGrowthConverter.class)
    @Column(length = 1)
    private TrendOfPopulationGrowth trendOfPopulationGrowth;

    @Min(0)
    @Column
    private Integer estimatedAmountOfSpecimens;

    @Min(0)
    @Column
    private Integer estimatedAmountOfSowWithPiglets;

    public SpeciesEstimatedAppearanceWithPiglets() {
    }

    public SpeciesEstimatedAppearanceWithPiglets(
            final Boolean appeared,
            final TrendOfPopulationGrowth trendOfPopulationGrowth,
            final Integer estimatedAmountOfSpecimens,
            final Integer estimatedAmountOfSowWithPiglets) {

        this.appeared = appeared;
        this.trendOfPopulationGrowth = trendOfPopulationGrowth;
        this.estimatedAmountOfSpecimens = estimatedAmountOfSpecimens;
        this.estimatedAmountOfSowWithPiglets = estimatedAmountOfSowWithPiglets;
    }

    @AssertTrue
    public boolean isValid() {
        return !Boolean.FALSE.equals(appeared) || trendOfPopulationGrowth == null && estimatedAmountOfSpecimens == null && estimatedAmountOfSowWithPiglets == null;
    }

    public SpeciesEstimatedAppearanceWithPiglets cleanedCopy() {
        final SpeciesEstimatedAppearanceWithPiglets copy =
                new SpeciesEstimatedAppearanceWithPiglets(appeared, trendOfPopulationGrowth, estimatedAmountOfSpecimens, estimatedAmountOfSowWithPiglets);
        copy.cleanState();
        return copy;
    }

    private void cleanState() {
        if (Boolean.FALSE.equals(appeared)) {
            trendOfPopulationGrowth = null;
            estimatedAmountOfSpecimens = null;
            estimatedAmountOfSowWithPiglets = null;
        }
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

    public Integer getEstimatedAmountOfSpecimens() {
        return estimatedAmountOfSpecimens;
    }

    public void setEstimatedAmountOfSpecimens(final Integer estimatedAmountOfSpecimens) {
        this.estimatedAmountOfSpecimens = estimatedAmountOfSpecimens;
    }

    public Integer getEstimatedAmountOfSowWithPiglets() {
        return estimatedAmountOfSowWithPiglets;
    }

    public void setEstimatedAmountOfSowWithPiglets(Integer estimatedAmountOfSowWithPiglets) {
        this.estimatedAmountOfSowWithPiglets = estimatedAmountOfSowWithPiglets;
    }
}

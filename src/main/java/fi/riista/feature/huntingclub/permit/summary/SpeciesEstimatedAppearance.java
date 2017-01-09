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
public class SpeciesEstimatedAppearance implements Serializable {

    public static SpeciesEstimatedAppearance revamp(@Nullable final SpeciesEstimatedAppearance appearance) {
        return Optional.ofNullable(appearance)
                .map(SpeciesEstimatedAppearance::cleanedCopy)
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

    public SpeciesEstimatedAppearance() {
    }

    public SpeciesEstimatedAppearance(
            final Boolean appeared,
            final TrendOfPopulationGrowth trendOfPopulationGrowth,
            final Integer estimatedAmountOfSpecimens) {

        this.appeared = appeared;
        this.trendOfPopulationGrowth = trendOfPopulationGrowth;
        this.estimatedAmountOfSpecimens = estimatedAmountOfSpecimens;
    }

    @AssertTrue
    public boolean isValid() {
        return !Boolean.FALSE.equals(appeared) || trendOfPopulationGrowth == null && estimatedAmountOfSpecimens == null;
    }

    public SpeciesEstimatedAppearance cleanedCopy() {
        final SpeciesEstimatedAppearance copy =
                new SpeciesEstimatedAppearance(appeared, trendOfPopulationGrowth, estimatedAmountOfSpecimens);
        copy.cleanState();
        return copy;
    }

    private void cleanState() {
        if (Boolean.FALSE.equals(appeared)) {
            trendOfPopulationGrowth = null;
            estimatedAmountOfSpecimens = null;
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

}

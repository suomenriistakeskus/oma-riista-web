package fi.riista.feature.huntingclub.permit.summary;

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
public class SpeciesEstimatedAppearance implements Serializable {

    public static SpeciesEstimatedAppearance revamp(@Nullable final SpeciesEstimatedAppearance appearance) {
        return Optional.ofNullable(appearance)
                .map(SpeciesEstimatedAppearance::new)
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

    public SpeciesEstimatedAppearance(final Boolean appeared,
                                      final TrendOfPopulationGrowth trendOfPopulationGrowth,
                                      final Integer estimatedAmountOfSpecimens) {

        this.appeared = appeared;

        if (!Boolean.FALSE.equals(appeared)) {
            this.trendOfPopulationGrowth = trendOfPopulationGrowth;
            this.estimatedAmountOfSpecimens = estimatedAmountOfSpecimens;
        }
    }

    public SpeciesEstimatedAppearance(@Nonnull final SpeciesEstimatedAppearance that) {
        this(that.appeared, that.trendOfPopulationGrowth, that.estimatedAmountOfSpecimens);
    }

    @AssertTrue
    public boolean isValid() {
        return !Boolean.FALSE.equals(appeared) || trendOfPopulationGrowth == null && estimatedAmountOfSpecimens == null;
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

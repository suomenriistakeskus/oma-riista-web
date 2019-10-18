package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup;
import fi.riista.util.F;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static fi.riista.util.F.nullsafeMax;
import static fi.riista.util.NumberUtils.nullableIntSum;
import static java.util.Objects.requireNonNull;

@Embeddable
@Access(AccessType.FIELD)
public class MetsahallitusStatistics
        implements AnnualStatisticsFieldsetReadiness,
        AnnualStatisticsNonComputedFields<MetsahallitusStatistics>,
        Serializable {

    public static final MetsahallitusStatistics reduce(@Nullable final MetsahallitusStatistics a,
                                                       @Nullable final MetsahallitusStatistics b) {

        final MetsahallitusStatistics result = new MetsahallitusStatistics();
        result.setSmallGameLicensesSoldByMetsahallitus(nullableIntSum(a, b, s -> s.getSmallGameLicensesSoldByMetsahallitus()));
        result.setLastModified(nullsafeMax(a, b, s -> s.getLastModified()));
        return result;
    }

    public static MetsahallitusStatistics reduce(@Nonnull final Stream<MetsahallitusStatistics> items) {
        requireNonNull(items);
        return items.reduce(new MetsahallitusStatistics(), MetsahallitusStatistics::reduce);
    }

    public static <T> MetsahallitusStatistics reduce(@Nonnull final Iterable<? extends T> items,
                                                     @Nonnull final Function<? super T, MetsahallitusStatistics> extractor) {

        requireNonNull(extractor, "extractor is null");
        return reduce(F.stream(items).map(extractor));
    }

    // Myydyt pienriistaluvat, Metsähallitukselta
    @Min(0)
    @Column(name = "mh_small_game_sold_licenses")
    private Integer smallGameLicensesSoldByMetsahallitus;

    // Updated when any of the manually updateable fields is changed.
    @Column(name = "mh_last_modified")
    private DateTime lastModified;

    public MetsahallitusStatistics() {
    }

    public MetsahallitusStatistics(@Nonnull final MetsahallitusStatistics that) {
        requireNonNull(that);

        this.smallGameLicensesSoldByMetsahallitus = that.smallGameLicensesSoldByMetsahallitus;
        this.lastModified = that.lastModified;
    }

    @Override
    public AnnualStatisticGroup getGroup() {
        return AnnualStatisticGroup.METSAHALLITUS;
    }

    @Override
    public boolean isEqualTo(@Nonnull final MetsahallitusStatistics that) {
        // Includes manually updateable fields only.

        return Objects.equals(smallGameLicensesSoldByMetsahallitus, that.smallGameLicensesSoldByMetsahallitus);
    }

    @Override
    public void assignFrom(@Nonnull final MetsahallitusStatistics that) {
        // Includes manually updateable fields only.

        this.smallGameLicensesSoldByMetsahallitus = that.smallGameLicensesSoldByMetsahallitus;
    }

    @Override
    public boolean isReadyForInspection() {
        return true;
    }

    @Override
    public boolean isCompleteForApproval() {
        return smallGameLicensesSoldByMetsahallitus != null;
    }

    // Accessors -->

    public Integer getSmallGameLicensesSoldByMetsahallitus() {
        return smallGameLicensesSoldByMetsahallitus;
    }

    public void setSmallGameLicensesSoldByMetsahallitus(final Integer smallGameLicensesSoldByMetsahallitus) {
        this.smallGameLicensesSoldByMetsahallitus = smallGameLicensesSoldByMetsahallitus;
    }

    @Override
    public DateTime getLastModified() {
        return lastModified;
    }

    @Override
    public void setLastModified(final DateTime lastModified) {
        this.lastModified = lastModified;
    }
}

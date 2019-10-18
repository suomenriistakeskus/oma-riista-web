package fi.riista.feature.organization.rhy.subsidy;

import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static fi.riista.util.NumberUtils.currencySum;
import static fi.riista.util.NumberUtils.isPositive;
import static java.util.Objects.requireNonNull;

public class PreviouslyGrantedSubsidiesDTO {

    private final ImmutableMap<String, BigDecimal> rhyCodeToSubsidyGrantedLastYear;
    private final ImmutableMap<String, BigDecimal> rhyCodeToSubsidyGrantedInFirstBatchOfCurrentYear;

    private final BigDecimal totalSubsidyAmountGrantedInFirstBatchOfCurrentYear;

    private static ImmutableMap<String, BigDecimal> withScaleSetRight(@Nonnull final Map<String, BigDecimal> map) {
        return requireNonNull(map)
                .entrySet()
                .stream()
                .collect(toImmutableMap(Map.Entry::getKey, entry -> entry.getValue().setScale(2)));
    }

    public PreviouslyGrantedSubsidiesDTO(@Nonnull final Map<String, BigDecimal> rhyCodeToSubsidyGrantedLastYear,
                                         @Nonnull final Map<String, BigDecimal> rhyCodeToSubsidyGrantedInFirstBatchOfCurrentYear) {

        requireNonNull(rhyCodeToSubsidyGrantedLastYear);
        requireNonNull(rhyCodeToSubsidyGrantedInFirstBatchOfCurrentYear);

        this.rhyCodeToSubsidyGrantedLastYear = withScaleSetRight(rhyCodeToSubsidyGrantedLastYear);
        this.rhyCodeToSubsidyGrantedInFirstBatchOfCurrentYear =
                withScaleSetRight(rhyCodeToSubsidyGrantedInFirstBatchOfCurrentYear);

        this.totalSubsidyAmountGrantedInFirstBatchOfCurrentYear =
                currencySum(rhyCodeToSubsidyGrantedInFirstBatchOfCurrentYear.values());
    }

    public boolean isFirstSubsidyBatchOfCurrentYearAlreadyGranted() {
        return isPositive(totalSubsidyAmountGrantedInFirstBatchOfCurrentYear);
    }

    public BigDecimal getSubsidyGrantedLastYear(final String rhyCode) {
        return rhyCodeToSubsidyGrantedLastYear.get(rhyCode);
    }

    public BigDecimal getSubsidyGrantedInFirstBatchOfCurrentYear(final String rhyCode) {
        return rhyCodeToSubsidyGrantedInFirstBatchOfCurrentYear.get(rhyCode);
    }

    // Accessors -->

    public ImmutableMap<String, BigDecimal> getRhyCodeToSubsidyGrantedLastYear() {
        return rhyCodeToSubsidyGrantedLastYear;
    }

    public ImmutableMap<String, BigDecimal> getRhyCodeToSubsidyGrantedInFirstBatchOfCurrentYear() {
        return rhyCodeToSubsidyGrantedInFirstBatchOfCurrentYear;
    }

    public BigDecimal getTotalSubsidyAmountGrantedInFirstBatchOfCurrentYear() {
        return totalSubsidyAmountGrantedInFirstBatchOfCurrentYear;
    }
}

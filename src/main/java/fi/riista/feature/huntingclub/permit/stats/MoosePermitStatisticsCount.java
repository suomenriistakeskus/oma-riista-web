package fi.riista.feature.huntingclub.permit.stats;

import com.fasterxml.jackson.annotation.JsonGetter;
import fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummaryDTO;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class MoosePermitStatisticsCount {
    public static MoosePermitStatisticsCount create(final Integer permitAreaSize,
                                                    final boolean limitToPermitAreaSize,
                                                    final List<BasicClubHuntingSummaryDTO> summaries) {

        Objects.requireNonNull(permitAreaSize, "permitAreaSize is null");
        Objects.requireNonNull(summaries, "summaries is null");

        final int adultMales = nullSafeIntSum(summaries, BasicClubHuntingSummaryDTO::getNumberOfAdultMales);
        final int adultFemales = nullSafeIntSum(summaries, BasicClubHuntingSummaryDTO::getNumberOfAdultFemales);
        final int youngMales = nullSafeIntSum(summaries, BasicClubHuntingSummaryDTO::getNumberOfYoungMales);
        final int youngFemales = nullSafeIntSum(summaries, BasicClubHuntingSummaryDTO::getNumberOfYoungFemales);
        final int nonEdibleAdults = nullSafeIntSum(summaries, BasicClubHuntingSummaryDTO::getNumberOfNonEdibleAdults);
        final int nonEdibleYoungs = nullSafeIntSum(summaries, BasicClubHuntingSummaryDTO::getNumberOfNonEdibleYoungs);
        final int totalAreaSize = limitToPermitAreaSize ? permitAreaSize : nullSafeIntSum(summaries, BasicClubHuntingSummaryDTO::getTotalHuntingArea);
        final int effectiveAreaSize = Math.min(totalAreaSize, nullSafeIntSum(summaries, BasicClubHuntingSummaryDTO::getEffectiveHuntingArea));
        final int remainingPopulationInEffectiveArea = nullSafeIntSum(summaries, BasicClubHuntingSummaryDTO::getRemainingPopulationInEffectiveArea);
        final int remainingPopulationInTotalArea = nullSafeIntSum(summaries, BasicClubHuntingSummaryDTO::getRemainingPopulationInTotalArea);

        return new MoosePermitStatisticsCount(totalAreaSize, effectiveAreaSize,
                remainingPopulationInTotalArea, remainingPopulationInEffectiveArea,
                adultMales, adultFemales, youngMales, youngFemales, nonEdibleAdults, nonEdibleYoungs);
    }

    private static <T> int nullSafeIntSum(@Nonnull final Collection<T> collection,
                                          @Nonnull final Function<T, Integer> transform) {

        return collection.stream()
                .map(transform)
                .mapToInt(i -> i != null ? i : 0)
                .sum();
    }

    private final int totalAreaSize;
    private final int effectiveAreaSize;
    private final int remainingPopulationInTotalArea;
    private final int remainingPopulationInEffectiveArea;

    private final int adultMales;
    private final int adultFemales;
    private final int youngMales;
    private final int youngFemales;
    private final int adultsNonEdible;
    private final int youngNonEdible;

    MoosePermitStatisticsCount(final int totalAreaSize, final int effectiveAreaSize,
                               final int remainingPopulationInTotalArea, final int remainingPopulationInEffectiveArea,
                               final int adultMales, final int adultFemales,
                               final int youngMales, final int youngFemales,
                               final int nonEdibleAdults, final int youngNonEdible) {
        this.totalAreaSize = totalAreaSize;
        this.effectiveAreaSize = effectiveAreaSize;
        this.remainingPopulationInTotalArea = remainingPopulationInTotalArea;
        this.remainingPopulationInEffectiveArea = remainingPopulationInEffectiveArea;
        this.adultMales = adultMales;
        this.adultFemales = adultFemales;
        this.youngMales = youngMales;
        this.youngFemales = youngFemales;
        this.adultsNonEdible = nonEdibleAdults;
        this.youngNonEdible = youngNonEdible;
    }

    public int getTotalAreaSize() {
        return totalAreaSize;
    }

    public int getEffectiveAreaSize() {
        return effectiveAreaSize;
    }

    public int getRemainingPopulationInTotalArea() {
        return remainingPopulationInTotalArea;
    }

    public int getRemainingPopulationInEffectiveArea() {
        return remainingPopulationInEffectiveArea;
    }

    @JsonGetter
    public double getRemainingPopulationInTotalAreaPer1000ha() {
        return ratio(remainingPopulationInTotalArea, totalAreaSize / 1000.0);
    }

    @JsonGetter
    public double getRemainingPopulationInEffectiveAreaPer1000ha() {
        return ratio(remainingPopulationInEffectiveArea, effectiveAreaSize / 1000.0);
    }

    public int getAdultMales() {
        return adultMales;
    }

    public int getAdultFemales() {
        return adultFemales;
    }

    @JsonGetter
    public int getAdults() {
        return adultMales + adultFemales;
    }

    public int getYoungMales() {
        return youngMales;
    }

    public int getYoungFemales() {
        return youngFemales;
    }

    public int getAdultsNonEdible() {
        return adultsNonEdible;
    }

    public int getYoungNonEdible() {
        return youngNonEdible;
    }

    @JsonGetter
    public int getYoung() {
        return youngMales + youngFemales;
    }

    @JsonGetter
    public int getTotal() {
        return getAdults() + getYoung();
    }

    @JsonGetter
    public double getYoungPercentage() {
        return percentRatio(
                getYoung(),
                getTotal());
    }

    @JsonGetter
    public double getAdultMalePercentage() {
        return percentRatio(
                getAdultMales(),
                getAdults());
    }

    private static double percentRatio(double a, double b) {
        return b == 0 ? 0 : 100.0 * a / b;
    }

    private static double ratio(double a, double b) {
        return b == 0 ? 0 : a / b;
    }
}

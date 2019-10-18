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
public class HuntingControlStatistics
        implements AnnualStatisticsFieldsetReadiness,
        AnnualStatisticsNonComputedFields<HuntingControlStatistics>,
        Serializable {

    public static final HuntingControlStatistics reduce(@Nullable final HuntingControlStatistics a,
                                                        @Nullable final HuntingControlStatistics b) {

        final HuntingControlStatistics result = new HuntingControlStatistics();
        result.setHuntingControlEvents(nullableIntSum(a, b, HuntingControlStatistics::getHuntingControlEvents));
        result.setHuntingControlCustomers(nullableIntSum(a, b, HuntingControlStatistics::getHuntingControlCustomers));
        result.setProofOrders(nullableIntSum(a, b, HuntingControlStatistics::getProofOrders));
        result.setHuntingControllers(nullableIntSum(a, b, HuntingControlStatistics::getHuntingControllers));
        result.setLastModified(nullsafeMax(a, b, HuntingControlStatistics::getLastModified));
        return result;
    }

    public static HuntingControlStatistics reduce(@Nonnull final Stream<HuntingControlStatistics> items) {
        requireNonNull(items);
        return items.reduce(new HuntingControlStatistics(), HuntingControlStatistics::reduce);
    }

    public static <T> HuntingControlStatistics reduce(@Nonnull final Iterable<? extends T> items,
                                                      @Nonnull final Function<? super T, HuntingControlStatistics> extractor) {

        requireNonNull(extractor, "extractor is null");
        return reduce(F.stream(items).map(extractor));
    }

    // Valvontatapahtumien määrä
    @Min(0)
    @Column(name = "hunting_control_events")
    private Integer huntingControlEvents;

    // Metsästävien asiakkaiden määrä
    @Min(0)
    @Column(name = "hunting_control_customers")
    private Integer huntingControlCustomers;

    // Kirjattujen näyttömääräysten määrä
    @Min(0)
    @Column(name = "proof_orders")
    private Integer proofOrders;

    // Metsästyksenvalvojien määrä
    @Min(0)
    @Column(name = "hunting_controllers")
    private Integer huntingControllers;

    // Updated when any of the manually updateable fields is changed.
    @Column(name = "hunting_control_last_modified")
    private DateTime lastModified;

    public HuntingControlStatistics() {
    }

    public HuntingControlStatistics(@Nonnull final HuntingControlStatistics that) {
        requireNonNull(that);

        this.huntingControlEvents = that.huntingControlEvents;
        this.huntingControlCustomers = that.huntingControlCustomers;
        this.proofOrders = that.proofOrders;
        this.huntingControllers = that.huntingControllers;
        this.lastModified = that.lastModified;
    }

    @Override
    public AnnualStatisticGroup getGroup() {
        return AnnualStatisticGroup.HUNTING_CONTROL;
    }

    @Override
    public boolean isEqualTo(@Nonnull final HuntingControlStatistics that) {
        // Includes manually updateable fields only.

        return Objects.equals(huntingControlEvents, that.huntingControlEvents) &&
                Objects.equals(huntingControlCustomers, that.huntingControlCustomers) &&
                Objects.equals(proofOrders, that.proofOrders);
    }

    @Override
    public void assignFrom(@Nonnull final HuntingControlStatistics that) {
        // Includes manually updateable fields only.

        this.huntingControlEvents = that.huntingControlEvents;
        this.huntingControlCustomers = that.huntingControlCustomers;
        this.proofOrders = that.proofOrders;
    }

    @Override
    public boolean isReadyForInspection() {
        return F.allNotNull(huntingControlEvents, huntingControlCustomers, proofOrders, huntingControllers);
    }

    @Override
    public boolean isCompleteForApproval() {
        return isReadyForInspection();
    }

    // Accessors -->

    public Integer getHuntingControlEvents() {
        return huntingControlEvents;
    }

    public void setHuntingControlEvents(final Integer huntingControlEvents) {
        this.huntingControlEvents = huntingControlEvents;
    }

    public Integer getHuntingControlCustomers() {
        return huntingControlCustomers;
    }

    public void setHuntingControlCustomers(final Integer huntingControlCustomers) {
        this.huntingControlCustomers = huntingControlCustomers;
    }

    public Integer getProofOrders() {
        return proofOrders;
    }

    public void setProofOrders(final Integer proofOrders) {
        this.proofOrders = proofOrders;
    }

    public Integer getHuntingControllers() {
        return huntingControllers;
    }

    public void setHuntingControllers(final Integer huntingControllers) {
        this.huntingControllers = huntingControllers;
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

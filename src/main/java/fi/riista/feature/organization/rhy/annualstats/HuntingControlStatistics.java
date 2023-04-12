package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup;
import fi.riista.util.F;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.HUNTING_CONTROLLERS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantFieldGroup.HUNTING_CONTROL_STATISTICS;
import static fi.riista.util.F.nullsafeMax;
import static fi.riista.util.NumberUtils.nullableIntSum;
import static java.util.Objects.requireNonNull;

@Embeddable
@Access(AccessType.FIELD)
public class HuntingControlStatistics
        implements AnnualStatisticsFieldsetReadiness,
        AnnualStatisticsFieldsetParticipants,
        AnnualStatisticsManuallyEditableFields<HuntingControlStatistics>,
        Serializable {

    public static HuntingControlStatistics reduce(@Nullable final HuntingControlStatistics a,
                                                  @Nullable final HuntingControlStatistics b) {

        final HuntingControlStatistics result = new HuntingControlStatistics();
        result.setHuntingControlEvents(nullableIntSum(a, b, HuntingControlStatistics::getHuntingControlEvents));
        result.setNonSubsidizableHuntingControlEvents(nullableIntSum(a, b, HuntingControlStatistics::getNonSubsidizableHuntingControlEvents));
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
    private Integer huntingControlEvents; // Subsidizable events

    @Min(0)
    @Column(name = "non_subsidizable_hunting_control_events")
    private Integer nonSubsidizableHuntingControlEvents;

    @Column(name = "hunting_control_events_overridden", nullable = false)
    private boolean huntingControlEventsOverridden;

    // Metsästävien asiakkaiden määrä
    @Min(0)
    @Column(name = "hunting_control_customers")
    private Integer huntingControlCustomers;

    @Column(name = "hunting_control_customers_overridden", nullable = false)
    private boolean huntingControlCustomersOverridden;

    // Kirjattujen näyttömääräysten määrä
    @Min(0)
    @Column(name = "proof_orders")
    private Integer proofOrders;

    @Column(name = "proof_orders_overridden", nullable = false)
    private boolean proofOrdersOverridden;

    // Metsästyksenvalvojien määrä
    @Min(0)
    @Column(name = "hunting_controllers")
    private Integer huntingControllers;

    // Updated when any of the manually updateable fields is changed.
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name = "hunting_control_last_modified")
    private DateTime lastModified;

    public HuntingControlStatistics() {
    }

    public HuntingControlStatistics(@Nonnull final HuntingControlStatistics that) {
        requireNonNull(that);

        this.huntingControlEvents = that.huntingControlEvents;
        this.nonSubsidizableHuntingControlEvents = that.nonSubsidizableHuntingControlEvents;
        this.huntingControlEventsOverridden = that.huntingControlEventsOverridden;
        this.huntingControlCustomers = that.huntingControlCustomers;
        this.huntingControlCustomersOverridden = that.huntingControlCustomersOverridden;
        this.proofOrders = that.proofOrders;
        this.proofOrdersOverridden = that.proofOrdersOverridden;
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
                Objects.equals(nonSubsidizableHuntingControlEvents, that.nonSubsidizableHuntingControlEvents) &&
                Objects.equals(huntingControlCustomers, that.huntingControlCustomers) &&
                Objects.equals(proofOrders, that.proofOrders);
    }

    @Override
    public void assignFrom(@Nonnull final HuntingControlStatistics that) {
        // Includes manually updateable fields only.

        if (!Objects.equals(this.huntingControlEvents, that.huntingControlEvents) ||
                !Objects.equals(this.nonSubsidizableHuntingControlEvents, that.nonSubsidizableHuntingControlEvents)) {
            huntingControlEventsOverridden = true;
        }
        this.huntingControlEvents = that.huntingControlEvents;
        this.nonSubsidizableHuntingControlEvents = that.nonSubsidizableHuntingControlEvents;

        if (!Objects.equals(this.huntingControlCustomers, that.huntingControlCustomers)) {
            huntingControlCustomersOverridden = true;
        }
        this.huntingControlCustomers = that.huntingControlCustomers;

        if (!Objects.equals(this.proofOrders, that.proofOrders)) {
            proofOrdersOverridden = true;
        }
        this.proofOrders = that.proofOrders;
    }

    @Override
    public boolean isReadyForInspection() {
        return F.allNotNull(huntingControlEvents, huntingControlCustomers, proofOrders, huntingControllers) && hasParticipants();
    }

    private boolean hasParticipants() {
        return listMissingParticipants()._2.isEmpty();
    }

    @Override
    public Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> listMissingParticipants() {
        final List<AnnualStatisticsParticipantField> missing = new ArrayList<>();
        if (huntingControlEvents != null && huntingControlEvents > 0
                && (huntingControllers == null || huntingControllers <= 0)) {
            missing.add(HUNTING_CONTROLLERS);
        }
        return Tuple.of(HUNTING_CONTROL_STATISTICS, missing);
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

    public Integer getNonSubsidizableHuntingControlEvents() {
        return nonSubsidizableHuntingControlEvents;
    }

    public void setNonSubsidizableHuntingControlEvents(final Integer nonSubsidizableHuntingControlEvents) {
        this.nonSubsidizableHuntingControlEvents = nonSubsidizableHuntingControlEvents;
    }

    public Integer getTotalHuntingControlEvents() {
        return nullableIntSum(huntingControlEvents, nonSubsidizableHuntingControlEvents);
    }

    public boolean isHuntingControlEventsOverridden() {
        return huntingControlEventsOverridden;
    }

    public void setHuntingControlEventsOverridden(final boolean huntingControlEventsOverridden) {
        this.huntingControlEventsOverridden = huntingControlEventsOverridden;
    }

    public Integer getHuntingControlCustomers() {
        return huntingControlCustomers;
    }

    public void setHuntingControlCustomers(final Integer huntingControlCustomers) {
        this.huntingControlCustomers = huntingControlCustomers;
    }

    public boolean isHuntingControlCustomersOverridden() {
        return huntingControlCustomersOverridden;
    }

    public void setHuntingControlCustomersOverridden(final boolean huntingControlCustomersOverridden) {
        this.huntingControlCustomersOverridden = huntingControlCustomersOverridden;
    }

    public Integer getProofOrders() {
        return proofOrders;
    }

    public void setProofOrders(final Integer proofOrders) {
        this.proofOrders = proofOrders;
    }

    public boolean isProofOrdersOverridden() {
        return proofOrdersOverridden;
    }

    public void setProofOrdersOverridden(final boolean proofOrdersOverridden) {
        this.proofOrdersOverridden = proofOrdersOverridden;
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

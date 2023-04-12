package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup;
import fi.riista.util.F;
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
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static fi.riista.util.F.nullsafeMax;
import static fi.riista.util.NumberUtils.nullableIntSum;
import static java.util.Objects.requireNonNull;

@Embeddable
@Access(AccessType.FIELD)
public class OtherHuntingRelatedStatistics
        implements AnnualStatisticsFieldsetReadiness,
        AnnualStatisticsManuallyEditableFields<OtherHuntingRelatedStatistics>,
        Serializable {

    public static OtherHuntingRelatedStatistics reduce(@Nullable final OtherHuntingRelatedStatistics a,
                                                       @Nullable final OtherHuntingRelatedStatistics b) {

        final OtherHuntingRelatedStatistics result = new OtherHuntingRelatedStatistics();
        result.setHarvestPermitApplicationPartners(nullableIntSum(a, b, OtherHuntingRelatedStatistics::getHarvestPermitApplicationPartners));
        result.setMooselikeTaxationPlanningEvents(nullableIntSum(a, b, OtherHuntingRelatedStatistics::getMooselikeTaxationPlanningEvents));
        result.setWolfTerritoryWorkgroups(nullableIntSum(a, b, OtherHuntingRelatedStatistics::getWolfTerritoryWorkgroups));
        result.setLastModified(nullsafeMax(a, b, OtherHuntingRelatedStatistics::getLastModified));
        return result;
    }

    public static OtherHuntingRelatedStatistics reduce(@Nonnull final Stream<OtherHuntingRelatedStatistics> items) {
        requireNonNull(items);
        return items.reduce(new OtherHuntingRelatedStatistics(), OtherHuntingRelatedStatistics::reduce);
    }

    public static <T> OtherHuntingRelatedStatistics reduce(@Nonnull final Iterable<? extends T> items,
                                                           @Nonnull final Function<? super T, OtherHuntingRelatedStatistics> extractor) {

        requireNonNull(extractor, "extractor is null");
        return reduce(F.stream(items).map(extractor));
    }

    // Hirvieläinhakemuksissa olevien lupaosakkaiden määrä
    @Min(0)
    @Column(name = "harvest_permit_application_partners")
    private Integer harvestPermitApplicationPartners;

    // Hirviverotussuunnittelun tilaisuudet lupaosakkaille
    // TODO Rename database field
    @Min(0)
    @Column(name = "mooselike_taxation_planning_events")
    private Integer mooselikeTaxationPlanningEvents;

    @Column(name = "mooselike_taxation_planning_events_overridden", nullable = false)
    private boolean mooselikeTaxationPlanningEventsOverridden;

    // Susireviiriyhteistyöryhmän toimintaan osallistuminen, kpl
    @Min(0)
    @Column(name = "wolf_territory_workgroups")
    private Integer wolfTerritoryWorkgroups;

    // Updated when any of the manually updateable fields is changed.
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name = "other_hunting_related_last_modified")
    private DateTime lastModified;

    public OtherHuntingRelatedStatistics() {
    }

    public OtherHuntingRelatedStatistics makeCopy() {
        final OtherHuntingRelatedStatistics copy = new OtherHuntingRelatedStatistics();
        copy.harvestPermitApplicationPartners = this.harvestPermitApplicationPartners;
        copy.mooselikeTaxationPlanningEvents = this.mooselikeTaxationPlanningEvents;
        copy.mooselikeTaxationPlanningEventsOverridden = this.mooselikeTaxationPlanningEventsOverridden;
        copy.wolfTerritoryWorkgroups = this.wolfTerritoryWorkgroups;
        copy.lastModified = this.lastModified;
        return copy;
    }

    @Override
    public AnnualStatisticGroup getGroup() {
        return AnnualStatisticGroup.OTHER_HUNTING_RELATED;
    }

    @Override
    public boolean isEqualTo(@Nonnull final OtherHuntingRelatedStatistics that) {
        // Includes only fields manually updateable by coordinator.
        return Objects.equals(mooselikeTaxationPlanningEvents, that.mooselikeTaxationPlanningEvents);
    }

    @Override
    public void assignFrom(@Nonnull final OtherHuntingRelatedStatistics that) {
        // Includes only fields manually updateable by coordinator.
        if (!Objects.equals(this.mooselikeTaxationPlanningEvents, that.mooselikeTaxationPlanningEvents)) {
            this.mooselikeTaxationPlanningEventsOverridden = true;
        }
        this.mooselikeTaxationPlanningEvents = that.mooselikeTaxationPlanningEvents;
    }

    @Override
    public boolean isReadyForInspection() {
        return harvestPermitApplicationPartners != null && mooselikeTaxationPlanningEvents != null;
    }

    @Override
    public boolean isCompleteForApproval() {
        return isReadyForInspection() && wolfTerritoryWorkgroups != null;
    }

    // Accessors -->

    public Integer getHarvestPermitApplicationPartners() {
        return harvestPermitApplicationPartners;
    }

    public void setHarvestPermitApplicationPartners(final Integer harvestPermitApplicationPartners) {
        this.harvestPermitApplicationPartners = harvestPermitApplicationPartners;
    }

    public Integer getMooselikeTaxationPlanningEvents() {
        return mooselikeTaxationPlanningEvents;
    }

    public void setMooselikeTaxationPlanningEvents(final Integer mooselikeTaxationPlanningEvents) {
        this.mooselikeTaxationPlanningEvents = mooselikeTaxationPlanningEvents;
    }

    public boolean isMooselikeTaxationPlanningEventsOverridden() {
        return mooselikeTaxationPlanningEventsOverridden;
    }

    public void setMooselikeTaxationPlanningEventsOverridden(final boolean mooselikeTaxationPlanningEventsOverridden) {
        this.mooselikeTaxationPlanningEventsOverridden = mooselikeTaxationPlanningEventsOverridden;
    }

    public Integer getWolfTerritoryWorkgroups() {
        return wolfTerritoryWorkgroups;
    }

    public void setWolfTerritoryWorkgroups(final Integer wolfTerritoryWorkgroups) {
        this.wolfTerritoryWorkgroups = wolfTerritoryWorkgroups;
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

package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.util.DateUtil;
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
import static fi.riista.util.NumberUtils.nullsafeSumAsInt;
import static java.util.Objects.requireNonNull;

@Embeddable
@Access(AccessType.FIELD)
public class OtherHuntingRelatedStatistics
        implements AnnualStatisticsFieldsetStatus,
        HasLastModificationStatus<OtherHuntingRelatedStatistics>,
        Serializable {

    public static final OtherHuntingRelatedStatistics reduce(@Nullable final OtherHuntingRelatedStatistics a,
                                                             @Nullable final OtherHuntingRelatedStatistics b) {

        final OtherHuntingRelatedStatistics result = new OtherHuntingRelatedStatistics();
        result.setHarvestPermitApplicationPartners(nullsafeSumAsInt(a, b, s -> s.getHarvestPermitApplicationPartners()));
        result.setWolfTerritoryWorkgroupLeads(nullsafeSumAsInt(a, b, s -> s.getWolfTerritoryWorkgroupLeads()));
        result.setLastModified(nullsafeMax(a, b, s -> s.getLastModified()));
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

    // Susireviirityöryhmän vetäminen, kpl
    @Min(0)
    @Column(name = "wolf_territory_workgroup_leads")
    private Integer wolfTerritoryWorkgroupLeads;

    // Updated when any of the manually updateable fields is changed.
    @Column(name = "other_hunting_related_last_modified")
    private DateTime lastModified;

    public OtherHuntingRelatedStatistics() {
    }

    public OtherHuntingRelatedStatistics(@Nonnull final OtherHuntingRelatedStatistics that) {
        Objects.requireNonNull(that);

        this.harvestPermitApplicationPartners = that.harvestPermitApplicationPartners;
        this.wolfTerritoryWorkgroupLeads = that.wolfTerritoryWorkgroupLeads;
        this.lastModified = that.lastModified;
    }

    @Override
    public boolean isEqualTo(final OtherHuntingRelatedStatistics other) {
        // Includes manually updateable fields only.

        return Objects.equals(wolfTerritoryWorkgroupLeads, other.wolfTerritoryWorkgroupLeads);
    }

    @Override
    public void updateModificationStatus() {
        lastModified = DateUtil.now();
    }

    @Override
    public boolean isReadyForInspection() {
        return harvestPermitApplicationPartners != null;
    }

    @Override
    public boolean isCompleteForApproval() {
        return isReadyForInspection() && wolfTerritoryWorkgroupLeads != null;
    }

    // Accessors -->

    public Integer getHarvestPermitApplicationPartners() {
        return harvestPermitApplicationPartners;
    }

    public void setHarvestPermitApplicationPartners(final Integer harvestPermitApplicationPartners) {
        this.harvestPermitApplicationPartners = harvestPermitApplicationPartners;
    }

    public Integer getWolfTerritoryWorkgroupLeads() {
        return wolfTerritoryWorkgroupLeads;
    }

    public void setWolfTerritoryWorkgroupLeads(final Integer wolfTerritoryWorkgroupLeads) {
        this.wolfTerritoryWorkgroupLeads = wolfTerritoryWorkgroupLeads;
    }

    public DateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(final DateTime lastModified) {
        this.lastModified = lastModified;
    }
}

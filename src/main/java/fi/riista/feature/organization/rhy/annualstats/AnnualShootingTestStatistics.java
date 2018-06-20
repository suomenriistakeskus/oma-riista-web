package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.util.F;
import fi.riista.util.NumberUtils;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.function.Function;
import java.util.stream.Stream;

import static fi.riista.util.F.nullsafeMax;
import static fi.riista.util.NumberUtils.nullsafeSumAsInt;
import static java.util.Objects.requireNonNull;

@Embeddable
@Access(AccessType.FIELD)
public class AnnualShootingTestStatistics implements AnnualStatisticsFieldsetStatus, Serializable {

    public static final AnnualShootingTestStatistics reduce(@Nullable final AnnualShootingTestStatistics a,
                                                            @Nullable final AnnualShootingTestStatistics b) {

        final AnnualShootingTestStatistics result = new AnnualShootingTestStatistics();

        result.firearmTestEvents = nullsafeSumAsInt(a, b, s -> s.getFirearmTestEvents());
        result.bowTestEvents = nullsafeSumAsInt(a, b, s -> s.getBowTestEvents());

        result.allMooseAttempts = nullsafeSumAsInt(a, b, s -> s.getAllMooseAttempts());
        result.qualifiedMooseAttempts = nullsafeSumAsInt(a, b, s -> s.getQualifiedMooseAttempts());
        result.allBearAttempts = nullsafeSumAsInt(a, b, s -> s.getAllBearAttempts());
        result.qualifiedBearAttempts = nullsafeSumAsInt(a, b, s -> s.getQualifiedBearAttempts());
        result.allRoeDeerAttempts = nullsafeSumAsInt(a, b, s -> s.getAllRoeDeerAttempts());
        result.qualifiedRoeDeerAttempts = nullsafeSumAsInt(a, b, s -> s.getQualifiedRoeDeerAttempts());
        result.allBowAttempts = nullsafeSumAsInt(a, b, s -> s.getAllBowAttempts());
        result.qualifiedBowAttempts = nullsafeSumAsInt(a, b, s -> s.getQualifiedBowAttempts());

        result.shootingTestOfficials = nullsafeSumAsInt(a, b, s -> s.getShootingTestOfficials());

        result.firearmTestEventsLastOverridden = nullsafeMax(a, b, s -> s.getFirearmTestEventsLastOverridden());
        result.bowTestEventsLastOverridden = nullsafeMax(a, b, s -> s.getBowTestEventsLastOverridden());
        result.lastModified = nullsafeMax(a, b, s -> s.getLastModified());

        return result;
    }

    public static AnnualShootingTestStatistics reduce(@Nonnull final Stream<AnnualShootingTestStatistics> items) {
        requireNonNull(items);
        return items.reduce(new AnnualShootingTestStatistics(), AnnualShootingTestStatistics::reduce);
    }

    public static <T> AnnualShootingTestStatistics reduce(@Nonnull final Iterable<? extends T> items,
                                                          @Nonnull final Function<? super T, AnnualShootingTestStatistics> extractor) {
        requireNonNull(extractor, "extractor is null");
        return reduce(F.stream(items).map(extractor));
    }

    // Rihlatun luotiaseen kokeet, kpl
    @Min(0)
    @Column(name = "firearm_test_events")
    private Integer firearmTestEvents;

    // Updated when moderator overrides automatically computed value.
    @Column(name = "firearm_test_events_last_overridden")
    private DateTime firearmTestEventsLastOverridden;

    // Jousikokeet, kpl
    @Min(0)
    @Column(name = "bow_test_events")
    private Integer bowTestEvents;

    // Updated when moderator overrides automatically computed value.
    @Column(name = "bow_test_events_last_overridden")
    private DateTime bowTestEventsLastOverridden;

    // Ampumakoesuoritusyrityksiä, hirvi ja peura, kpl
    @Min(0)
    @Column(name = "all_moose_attempts")
    private Integer allMooseAttempts;

    // Ampumakoesuoritusyrityksiä, hirvi ja peura, hyväksytyt, kpl
    @Min(0)
    @Column(name = "qualified_moose_attempts")
    private Integer qualifiedMooseAttempts;

    // Ampumakoesuoritusyrityksiä, karhu, kpl
    @Min(0)
    @Column(name = "all_bear_attempts")
    private Integer allBearAttempts;

    // Ampumakoesuoritusyrityksiä, karhu, hyväksytyt, kpl
    @Min(0)
    @Column(name = "qualified_bear_attempts")
    private Integer qualifiedBearAttempts;

    // Ampumakoesuoritusyrityksiä, metsäkauris, kpl
    @Min(0)
    @Column(name = "all_roe_deer_attempts")
    private Integer allRoeDeerAttempts;

    // Ampumakoesuoritusyrityksiä, metsäkauris, hyväksytyt, kpl
    @Min(0)
    @Column(name = "qualified_roe_deer_attempts")
    private Integer qualifiedRoeDeerAttempts;

    // Ampumakoesuoritusyrityksiä, jousi, kpl
    @Min(0)
    @Column(name = "all_bow_attempts")
    private Integer allBowAttempts;

    // Ampumakoesuoritusyrityksiä, jousi, hyväksytyt, kpl
    @Min(0)
    @Column(name = "qualified_bow_attempts")
    private Integer qualifiedBowAttempts;

    // Ampumakokeen vastaanottajien lukumäärä
    @Min(0)
    @Column(name = "shooting_test_officials")
    private Integer shootingTestOfficials;

    // Updated when any of the manually updateable fields is changed.
    @Column(name = "shooting_tests_last_modified")
    private DateTime lastModified;

    public AnnualShootingTestStatistics() {
    }

    public AnnualShootingTestStatistics(@Nonnull final AnnualShootingTestStatistics that) {
        requireNonNull(that);

        this.firearmTestEvents = that.firearmTestEvents;
        this.bowTestEvents = that.bowTestEvents;

        this.allMooseAttempts = that.allMooseAttempts;
        this.qualifiedMooseAttempts = that.qualifiedMooseAttempts;
        this.allBearAttempts = that.allBearAttempts;
        this.qualifiedBearAttempts = that.qualifiedBearAttempts;
        this.allRoeDeerAttempts = that.allRoeDeerAttempts;
        this.qualifiedRoeDeerAttempts = that.qualifiedRoeDeerAttempts;
        this.allBowAttempts = that.allBowAttempts;
        this.qualifiedBowAttempts = that.qualifiedBowAttempts;

        this.shootingTestOfficials = that.shootingTestOfficials;

        this.firearmTestEventsLastOverridden = that.firearmTestEventsLastOverridden;
        this.bowTestEventsLastOverridden = that.bowTestEventsLastOverridden;
        this.lastModified = that.lastModified;
    }

    @Override
    public boolean isReadyForInspection() {
        return F.allNotNull(
                firearmTestEvents, bowTestEvents,
                allMooseAttempts, qualifiedMooseAttempts,
                allBearAttempts, qualifiedBearAttempts,
                allRoeDeerAttempts, qualifiedRoeDeerAttempts,
                allBowAttempts, qualifiedBowAttempts,
                shootingTestOfficials);
    }

    @Override
    public boolean isCompleteForApproval() {
        return isReadyForInspection();
    }

    public boolean isFirearmTestEventsManuallyOverridden() {
        return this.firearmTestEventsLastOverridden != null;
    }

    public boolean isBowTestEventsManuallyOverridden() {
        return this.bowTestEventsLastOverridden != null;
    }

    public void setFirearmTestEventsWithModeratorOverride(@Nonnull final Integer firearmTestEvents,
                                                          @Nonnull final DateTime overriddenAt) {

        this.firearmTestEvents = requireNonNull(firearmTestEvents, "firearmTestEvents is null");
        this.firearmTestEventsLastOverridden = requireNonNull(overriddenAt, "overriddenAt is null");
    }

    public void setBowTestEventsWithModeratorOverride(@Nonnull final Integer bowTestEvents,
                                                      @Nonnull final DateTime overriddenAt) {

        this.bowTestEvents = requireNonNull(bowTestEvents, "bowTestEvents is null");
        this.bowTestEventsLastOverridden = requireNonNull(overriddenAt, "overriddenAt is null");
    }

    public int countAllShootingTestEvents() {
        return NumberUtils.getIntValueOrZero(firearmTestEvents) + NumberUtils.getIntValueOrZero(bowTestEvents);
    }

    public int countAllShootingTestAttempts() {
        return NumberUtils.getIntValueOrZero(allMooseAttempts)
                + NumberUtils.getIntValueOrZero(allBearAttempts)
                + NumberUtils.getIntValueOrZero(allRoeDeerAttempts)
                + NumberUtils.getIntValueOrZero(allBowAttempts);
    }

    // Accessors -->

    public Integer getFirearmTestEvents() {
        return firearmTestEvents;
    }

    public void setFirearmTestEvents(final Integer firearmTestEvents) {
        this.firearmTestEvents = firearmTestEvents;
    }

    public DateTime getFirearmTestEventsLastOverridden() {
        return firearmTestEventsLastOverridden;
    }

    public Integer getBowTestEvents() {
        return bowTestEvents;
    }

    public void setBowTestEvents(final Integer bowTestEvents) {
        this.bowTestEvents = bowTestEvents;
    }

    public DateTime getBowTestEventsLastOverridden() {
        return bowTestEventsLastOverridden;
    }

    public Integer getAllMooseAttempts() {
        return allMooseAttempts;
    }

    public void setAllMooseAttempts(final Integer allMooseAttempts) {
        this.allMooseAttempts = allMooseAttempts;
    }

    public Integer getQualifiedMooseAttempts() {
        return qualifiedMooseAttempts;
    }

    public void setQualifiedMooseAttempts(final Integer qualifiedMooseAttempts) {
        this.qualifiedMooseAttempts = qualifiedMooseAttempts;
    }

    public Integer getAllBearAttempts() {
        return allBearAttempts;
    }

    public void setAllBearAttempts(final Integer allBearAttempts) {
        this.allBearAttempts = allBearAttempts;
    }

    public Integer getQualifiedBearAttempts() {
        return qualifiedBearAttempts;
    }

    public void setQualifiedBearAttempts(Integer qualifiedBearAttempts) {
        this.qualifiedBearAttempts = qualifiedBearAttempts;
    }

    public Integer getAllRoeDeerAttempts() {
        return allRoeDeerAttempts;
    }

    public void setAllRoeDeerAttempts(final Integer allRoeDeerAttempts) {
        this.allRoeDeerAttempts = allRoeDeerAttempts;
    }

    public Integer getQualifiedRoeDeerAttempts() {
        return qualifiedRoeDeerAttempts;
    }

    public void setQualifiedRoeDeerAttempts(final Integer qualifiedRoeDeerAttempts) {
        this.qualifiedRoeDeerAttempts = qualifiedRoeDeerAttempts;
    }

    public Integer getAllBowAttempts() {
        return allBowAttempts;
    }

    public void setAllBowAttempts(final Integer allBowAttempts) {
        this.allBowAttempts = allBowAttempts;
    }

    public Integer getQualifiedBowAttempts() {
        return qualifiedBowAttempts;
    }

    public void setQualifiedBowAttempts(final Integer qualifiedBowAttempts) {
        this.qualifiedBowAttempts = qualifiedBowAttempts;
    }

    public Integer getShootingTestOfficials() {
        return shootingTestOfficials;
    }

    public void setShootingTestOfficials(final Integer shootingTestOfficials) {
        this.shootingTestOfficials = shootingTestOfficials;
    }

    public DateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(final DateTime lastModified) {
        this.lastModified = lastModified;
    }
}

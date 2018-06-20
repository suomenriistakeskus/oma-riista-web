package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static fi.riista.util.F.nullsafeMax;
import static fi.riista.util.NumberUtils.nullsafeSumAsInt;
import static java.util.Objects.requireNonNull;

@Embeddable
@Access(AccessType.FIELD)
public class CommunicationStatistics
        implements AnnualStatisticsFieldsetStatus, HasLastModificationStatus<CommunicationStatistics>, Serializable {

    public static final CommunicationStatistics reduce(@Nullable final CommunicationStatistics a,
                                                       @Nullable final CommunicationStatistics b) {

        final CommunicationStatistics result = new CommunicationStatistics();
        result.setInterviews(nullsafeSumAsInt(a, b, CommunicationStatistics::getInterviews));
        result.setAnnouncements(nullsafeSumAsInt(a, b, CommunicationStatistics::getAnnouncements));
        result.setOmariistaAnnouncements(nullsafeSumAsInt(a, b, CommunicationStatistics::getOmariistaAnnouncements));
        result.setLastModified(nullsafeMax(a, b, CommunicationStatistics::getLastModified));
        return result;
    }

    public static CommunicationStatistics reduce(@Nonnull final Stream<CommunicationStatistics> items) {
        requireNonNull(items);
        return items.reduce(new CommunicationStatistics(), CommunicationStatistics::reduce);
    }

    public static <T> CommunicationStatistics reduce(@Nonnull final Iterable<? extends T> items,
                                                     @Nonnull final Function<? super T, CommunicationStatistics> extractor) {

        requireNonNull(extractor, "extractor is null");
        return reduce(F.stream(items).map(extractor));
    }

    // Haastattelujen lukumäärä
    @Min(0)
    @Column(name = "interviews")
    private Integer interviews;

    // Tiedotteiden lukumäärä
    @Min(0)
    @Column(name = "announcements")
    private Integer announcements;

    // Toiminnanohjaajan lähettämien Oma riista -viestien lukumäärä
    @Min(0)
    @Column(name = "omariista_announcements")
    private Integer omariistaAnnouncements;

    // Tietoa some-kanavien käytöstä
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(name = "some_info", columnDefinition = "text")
    private String someInfo;

    // Tietoa some-kanavien käytöstä
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Size(max = 255)
    @Column(name = "home_page")
    private String homePage;

    // Tietoa some-kanavien käytöstä
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(name = "info", columnDefinition = "text")
    private String info;

    // Updated when any of the manually updateable fields is changed.
    @Column(name = "communication_last_modified")
    private DateTime lastModified;

    public CommunicationStatistics() {
    }

    public CommunicationStatistics(@Nonnull final CommunicationStatistics that) {
        Objects.requireNonNull(that);

        this.interviews = that.interviews;
        this.announcements = that.announcements;
        this.omariistaAnnouncements = that.omariistaAnnouncements;
        this.someInfo = that.someInfo;
        this.homePage = that.homePage;
        this.info = that.info;
        this.lastModified = that.lastModified;
    }

    @Override
    public boolean isEqualTo(final CommunicationStatistics other) {
        // Includes manually updateable fields only.

        return Objects.equals(interviews, other.interviews) &&
                Objects.equals(announcements, other.announcements) &&
                Objects.equals(someInfo, other.someInfo) &&
                Objects.equals(homePage, other.homePage) &&
                Objects.equals(info, other.info);
    }

    @Override
    public void updateModificationStatus() {
        lastModified = DateUtil.now();
    }

    @Override
    public boolean isReadyForInspection() {
        return F.allNotNull(interviews, announcements, omariistaAnnouncements);
    }

    @Override
    public boolean isCompleteForApproval() {
        return isReadyForInspection();
    }

    // Accessors -->

    public Integer getInterviews() {
        return interviews;
    }

    public void setInterviews(final Integer interviews) {
        this.interviews = interviews;
    }

    public Integer getAnnouncements() {
        return announcements;
    }

    public void setAnnouncements(final Integer announcements) {
        this.announcements = announcements;
    }

    public Integer getOmariistaAnnouncements() {
        return omariistaAnnouncements;
    }

    public void setOmariistaAnnouncements(final Integer omariistaAnnouncements) {
        this.omariistaAnnouncements = omariistaAnnouncements;
    }

    public String getSomeInfo() {
        return someInfo;
    }

    public void setSomeInfo(final String someInfo) {
        this.someInfo = someInfo;
    }

    public String getHomePage() {
        return homePage;
    }

    public void setHomePage(final String homePage) {
        this.homePage = homePage;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(final String info) {
        this.info = info;
    }

    public DateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(final DateTime lastModified) {
        this.lastModified = lastModified;
    }
}

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
public class OtherPublicAdminStatistics
        implements AnnualStatisticsFieldsetReadiness,
        AnnualStatisticsManuallyEditableFields<OtherPublicAdminStatistics>,
        Serializable {

    public static final OtherPublicAdminStatistics reduce(@Nullable final OtherPublicAdminStatistics a,
                                                          @Nullable final OtherPublicAdminStatistics b) {

        final OtherPublicAdminStatistics result = new OtherPublicAdminStatistics();
        result.setGrantedRecreationalShootingCertificates(nullableIntSum(a, b, s -> s.getGrantedRecreationalShootingCertificates()));
        result.setMutualAckShootingCertificates(nullableIntSum(a, b, s -> s.getMutualAckShootingCertificates()));
        result.setLastModified(nullsafeMax(a, b, s -> s.getLastModified()));
        return result;
    }

    public static OtherPublicAdminStatistics reduce(@Nonnull final Stream<OtherPublicAdminStatistics> items) {
        requireNonNull(items);
        return items.reduce(new OtherPublicAdminStatistics(), OtherPublicAdminStatistics::reduce);
    }

    public static <T> OtherPublicAdminStatistics reduce(@Nonnull final Iterable<? extends T> items,
                                                        @Nonnull final Function<? super T, OtherPublicAdminStatistics> extractor) {
        requireNonNull(extractor, "extractor is null");
        return reduce(F.stream(items).map(extractor));
    }

    // Ampuma-aselain harrastustodistukset, annetut todistukset, kpl
    @Min(0)
    @Column(name = "granted_recreational_shooting_certificates")
    private Integer grantedRecreationalShootingCertificates;

    // Ampumakokeen vastavuoroinen tunnustaminen, annetut todistukset, kpl
    @Min(0)
    @Column(name = "mutual_ack_shooting_certificates")
    private Integer mutualAckShootingCertificates;

    // Updated when any of the manually updateable fields is changed.
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name = "other_admin_data_last_modified")
    private DateTime lastModified;

    public OtherPublicAdminStatistics() {
    }

    public OtherPublicAdminStatistics(@Nonnull final OtherPublicAdminStatistics that) {
        requireNonNull(that);

        this.grantedRecreationalShootingCertificates = that.grantedRecreationalShootingCertificates;
        this.mutualAckShootingCertificates = that.mutualAckShootingCertificates;
        this.lastModified = that.lastModified;
    }

    @Override
    public AnnualStatisticGroup getGroup() {
        return AnnualStatisticGroup.OTHER_PUBLIC_ADMIN_TASKS;
    }

    @Override
    public boolean isEqualTo(@Nonnull final OtherPublicAdminStatistics that) {
        // Includes manually updateable fields only.

        return Objects.equals(grantedRecreationalShootingCertificates, that.grantedRecreationalShootingCertificates) &&
                Objects.equals(mutualAckShootingCertificates, that.mutualAckShootingCertificates);
    }

    @Override
    public void assignFrom(@Nonnull final OtherPublicAdminStatistics that) {
        // Includes manually updateable fields only.

        this.grantedRecreationalShootingCertificates = that.grantedRecreationalShootingCertificates;
        this.mutualAckShootingCertificates = that.mutualAckShootingCertificates;
    }

    @Override
    public boolean isReadyForInspection() {
        return F.allNotNull(grantedRecreationalShootingCertificates, mutualAckShootingCertificates);
    }

    @Override
    public boolean isCompleteForApproval() {
        return isReadyForInspection();
    }

    // Accessors -->

    public Integer getGrantedRecreationalShootingCertificates() {
        return grantedRecreationalShootingCertificates;
    }

    public void setGrantedRecreationalShootingCertificates(final Integer grantedRecreationalShootingCertificates) {
        this.grantedRecreationalShootingCertificates = grantedRecreationalShootingCertificates;
    }

    public Integer getMutualAckShootingCertificates() {
        return mutualAckShootingCertificates;
    }

    public void setMutualAckShootingCertificates(final Integer mutualAckShootingCertificates) {
        this.mutualAckShootingCertificates = mutualAckShootingCertificates;
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

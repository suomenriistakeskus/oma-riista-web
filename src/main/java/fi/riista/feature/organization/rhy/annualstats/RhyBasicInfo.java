package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.feature.common.entity.IbanConverter;
import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup;
import fi.riista.util.F;
import org.hibernate.annotations.Type;
import org.iban4j.Iban;
import org.joda.time.DateTime;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
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
public class RhyBasicInfo
        implements AnnualStatisticsFieldsetReadiness, AnnualStatisticsManuallyEditableFields<RhyBasicInfo>, Serializable {

    public static final RhyBasicInfo reduce(@Nullable final RhyBasicInfo first, @Nullable final RhyBasicInfo second) {
        final RhyBasicInfo result = new RhyBasicInfo();
        result.setOperationalLandAreaSize(nullableIntSum(first, second, RhyBasicInfo::getOperationalLandAreaSize));
        result.setRhyMembers(nullableIntSum(first, second, RhyBasicInfo::getRhyMembers));
        result.setLastModified(nullsafeMax(first, second, s -> s.getLastModified()));
        return result;
    }

    public static RhyBasicInfo reduce(@Nonnull final Stream<RhyBasicInfo> items) {
        requireNonNull(items);
        return items.reduce(new RhyBasicInfo(), RhyBasicInfo::reduce);
    }

    public static <T> RhyBasicInfo reduce(@Nonnull final Iterable<? extends T> items,
                                          @Nonnull final Function<? super T, RhyBasicInfo> extractor) {

        requireNonNull(extractor, "extractor is null");
        return reduce(F.stream(items).map(extractor));
    }

    @Column(length = 18)
    @Convert(converter = IbanConverter.class)
    private Iban iban;

    // Toiminta-alueen maapinta-alue, ha
    @Min(0)
    @Column(name = "operational_land_area_size")
    private Integer operationalLandAreaSize;

    @Min(0)
    @Column(name = "rhy_members")
    private Integer rhyMembers;

    // Updated when any of the manually updateable fields is changed.
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name = "basic_info_last_modified")
    private DateTime lastModified;

    public RhyBasicInfo() {
    }

    public RhyBasicInfo makeCopy() {
        final RhyBasicInfo copy = new RhyBasicInfo();
        copy.iban = this.iban;
        copy.operationalLandAreaSize = this.operationalLandAreaSize;
        copy.rhyMembers = this.rhyMembers;
        return copy;
    }

    @Override
    public AnnualStatisticGroup getGroup() {
        return AnnualStatisticGroup.BASIC_INFO;
    }

    @Override
    public boolean isEqualTo(@Nonnull final RhyBasicInfo that) {
        // Includes manually updateable fields only.

        return Objects.equals(iban, that.iban) &&
                Objects.equals(operationalLandAreaSize, that.operationalLandAreaSize);
    }

    @Override
    public void assignFrom(@Nonnull final RhyBasicInfo that) {
        // Includes manually updateable fields only.

        this.iban = that.iban;
        this.operationalLandAreaSize = that.operationalLandAreaSize;
    }

    @Override
    public boolean isReadyForInspection() {
        return F.allNotNull(operationalLandAreaSize, rhyMembers);
    }

    @Override
    public boolean isCompleteForApproval() {
        return isReadyForInspection() && iban != null;
    }

    @Transient
    public String getIbanAsFormattedString() {
        return iban != null ? iban.toFormattedString() : null;
    }

    @Transient
    public void setIbanAsFormattedString(final String newIban) {
        this.iban = StringUtils.hasText(newIban) ? Iban.valueOf(newIban.replaceAll(" ", "")) : null;
    }

    // Accessors -->

    public Iban getIban() {
        return iban;
    }

    public void setIban(final Iban iban) {
        this.iban = iban;
    }

    public Integer getOperationalLandAreaSize() {
        return operationalLandAreaSize;
    }

    public void setOperationalLandAreaSize(final Integer operationalLandAreaSize) {
        this.operationalLandAreaSize = operationalLandAreaSize;
    }

    public Integer getRhyMembers() {
        return rhyMembers;
    }

    public void setRhyMembers(final Integer rhyMembers) {
        this.rhyMembers = rhyMembers;
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

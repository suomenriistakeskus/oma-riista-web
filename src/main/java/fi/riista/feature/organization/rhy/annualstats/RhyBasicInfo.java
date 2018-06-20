package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.feature.common.entity.IbanConverter;
import fi.riista.util.F;
import org.iban4j.Iban;
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
import java.util.function.Function;
import java.util.stream.Stream;

import static fi.riista.util.NumberUtils.nullsafeSumAsInt;
import static java.util.Objects.requireNonNull;

@Embeddable
@Access(AccessType.FIELD)
public class RhyBasicInfo implements AnnualStatisticsFieldsetStatus, Serializable {

    public static final RhyBasicInfo reduce(@Nullable final RhyBasicInfo first, @Nullable final RhyBasicInfo second) {
        final RhyBasicInfo result = new RhyBasicInfo();
        result.setOperationalLandAreaSize(nullsafeSumAsInt(first, second, RhyBasicInfo::getOperationalLandAreaSize));
        result.setRhyMembers(nullsafeSumAsInt(first, second, RhyBasicInfo::getRhyMembers));
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

    public RhyBasicInfo() {
    }

    public RhyBasicInfo(@Nonnull final RhyBasicInfo that) {
        requireNonNull(that);

        this.iban = that.iban;
        this.operationalLandAreaSize = that.operationalLandAreaSize;
        this.rhyMembers = that.rhyMembers;
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
        this.iban = StringUtils.hasText(newIban)
                ? Iban.valueOf(newIban.replaceAll(" ", ""))
                : null;
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
}

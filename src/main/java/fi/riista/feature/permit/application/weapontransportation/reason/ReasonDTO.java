package fi.riista.feature.permit.application.weapontransportation.reason;

import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.permit.application.weapontransportation.WeaponTransportationPermitApplication;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static java.util.Objects.requireNonNull;

public class ReasonDTO implements HasBeginAndEndDate {

    public static ReasonDTO create(final @Nonnull WeaponTransportationPermitApplication application) {
        requireNonNull(application);

        return new ReasonDTO(application.getReasonType(),
                application.getReasonDescription(), application.getBeginDate(), application.getEndDate());
    }

    @NotNull
    private WeaponTransportationReasonType reasonType;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Size(max = 255)
    private String reasonDescription;

    @NotNull
    private LocalDate beginDate;

    @NotNull
    private LocalDate endDate;

    public ReasonDTO() {}

    public ReasonDTO(final WeaponTransportationReasonType reasonType,
                     final String reasonDescription,
                     final LocalDate beginDate,
                     final LocalDate endDate) {
        this.reasonType = reasonType;
        this.reasonDescription = reasonDescription;
        this.beginDate = beginDate;
        this.endDate = endDate;
    }

    @AssertTrue
    public boolean isValidDescription() {
        return (StringUtils.isEmpty(reasonDescription) && reasonType != WeaponTransportationReasonType.MUU) ||
                (!StringUtils.isEmpty(reasonDescription) && reasonType == WeaponTransportationReasonType.MUU);
    }

    @AssertTrue
    public boolean isValidPeriod() {
        final LocalDate maxEndDate = beginDate.plusYears(5).minusDays(1);
        return !endDate.isAfter(maxEndDate);
    }

    public WeaponTransportationReasonType getReasonType() {
        return reasonType;
    }

    public void setReasonType(final WeaponTransportationReasonType reasonType) {
        this.reasonType = reasonType;
    }

    public String getReasonDescription() {
        return reasonDescription;
    }

    public void setReasonDescription(final String reasonDescription) {
        this.reasonDescription = reasonDescription;
    }

    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(final LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

}

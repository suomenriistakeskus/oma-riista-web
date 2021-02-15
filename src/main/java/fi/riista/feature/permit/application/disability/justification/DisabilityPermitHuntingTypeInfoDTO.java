package fi.riista.feature.permit.application.disability.justification;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static java.util.Objects.requireNonNull;

public class DisabilityPermitHuntingTypeInfoDTO {

    @NotNull
    private HuntingType huntingType;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Size(max = 255)
    private String huntingTypeDescription;

    public DisabilityPermitHuntingTypeInfoDTO() {}

    public DisabilityPermitHuntingTypeInfoDTO(final @Nonnull DisabilityPermitHuntingTypeInfo huntingTypeInfo) {
        requireNonNull(huntingTypeInfo);

        this.huntingType = huntingTypeInfo.getHuntingType();
        this.huntingTypeDescription = huntingTypeInfo.getHuntingTypeDescription();
    }

    @AssertTrue
    public boolean isValidHuntingTypeDescription() {
        return (StringUtils.isEmpty(huntingTypeDescription) && huntingType != HuntingType.MUU) ||
                (!StringUtils.isEmpty(huntingTypeDescription) && huntingType == HuntingType.MUU);
    }

    public HuntingType getHuntingType() {
        return huntingType;
    }

    public void setHuntingType(final HuntingType huntingType) {
        this.huntingType = huntingType;
    }

    public String getHuntingTypeDescription() {
        return huntingTypeDescription;
    }

    public void setHuntingTypeDescription(final String huntingTypeDescription) {
        this.huntingTypeDescription = huntingTypeDescription;
    }
}

package fi.riista.feature.permit.application.disability.justification;

import fi.riista.feature.permit.application.PermitApplicationVehicleType;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static java.util.Objects.requireNonNull;

public class DisabilityPermitVehicleDTO {

    @NotNull
    private PermitApplicationVehicleType type;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Size(max = 255)
    private String description;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @NotBlank
    private String justification;

    public DisabilityPermitVehicleDTO() {}

    public DisabilityPermitVehicleDTO(final @Nonnull DisabilityPermitVehicle vehicle) {
        requireNonNull(vehicle);

        this.type = vehicle.getType();
        this.description = vehicle.getDescription();
        this.justification = vehicle.getJustification();
    }

    @AssertTrue
    public boolean isValidDescription() {
        return (StringUtils.isEmpty(description) && type != PermitApplicationVehicleType.MUU) ||
                (!StringUtils.isEmpty(description) && type == PermitApplicationVehicleType.MUU);
    }

    public PermitApplicationVehicleType getType() {
        return type;
    }

    public void setType(final PermitApplicationVehicleType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(final String justification) {
        this.justification = justification;
    }
}

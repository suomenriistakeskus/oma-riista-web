package fi.riista.feature.permit.application.weapontransportation.justification;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class WeaponTransportationVehicleDTO {

    @NotNull
    private WeaponTransportationVehicleType type;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Size(max = 255)
    private String description;

    public WeaponTransportationVehicleDTO() {}

    public WeaponTransportationVehicleDTO(final WeaponTransportationVehicle weaponTransportationVehicle) {
        this(weaponTransportationVehicle.getType(), weaponTransportationVehicle.getDescription());
    }

    public WeaponTransportationVehicleDTO(final @Nonnull WeaponTransportationVehicleType type,
                                          final String description) {
        this.type = type;
        this.description = description;
    }

    @AssertTrue
    public boolean isValidDescription() {
        return (StringUtils.isEmpty(description) && type != WeaponTransportationVehicleType.MUU) ||
                (!StringUtils.isEmpty(description) && type == WeaponTransportationVehicleType.MUU);
    }

    public WeaponTransportationVehicleType getType() {
        return type;
    }

    public void setType(final WeaponTransportationVehicleType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}

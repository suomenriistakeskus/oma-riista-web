package fi.riista.feature.permit.application.weapontransportation.justification;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class TransportedWeaponDTO {

    @NotNull
    private TransportedWeaponType type;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Size(max = 255)
    private String description;

    public TransportedWeaponDTO() {}

    public TransportedWeaponDTO(final TransportedWeapon transportedWeapon) {
        this(transportedWeapon.getType(), transportedWeapon.getDescription());
    }

    public TransportedWeaponDTO(final TransportedWeaponType type,
                                final String description) {
        this.type = type;
        this.description = description;
    }

    @AssertTrue
    public boolean isValidDescription() {
        return (StringUtils.isEmpty(description) && type != TransportedWeaponType.MUU) ||
                (!StringUtils.isEmpty(description) && type == TransportedWeaponType.MUU);
    }

    public TransportedWeaponType getType() {
        return type;
    }

    public void setType(final TransportedWeaponType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

}

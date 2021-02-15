package fi.riista.feature.permit.application.weapontransportation.justification;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class TransportedWeaponDTO {

    @NotNull
    private TransportedWeaponType type;

    @Min(1)
    private int amount;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Size(max = 255)
    private String description;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Size(max = 255)
    private String caliber;

    public TransportedWeaponDTO() {}

    public TransportedWeaponDTO(final TransportedWeapon transportedWeapon) {
        this(transportedWeapon.getType(), transportedWeapon.getAmount(), transportedWeapon.getDescription(), transportedWeapon.getCaliber());
    }

    public TransportedWeaponDTO(final TransportedWeaponType type,
                                final Integer amount,
                                final String description,
                                final String caliber) {
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.caliber = caliber;
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

    public int getAmount() {
        return amount;
    }

    public void setAmount(final int amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getCaliber() {
        return caliber;
    }

    public void setCaliber(final String caliber) {
        this.caliber = caliber;
    }
}

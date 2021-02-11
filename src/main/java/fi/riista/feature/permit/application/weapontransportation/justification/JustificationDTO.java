package fi.riista.feature.permit.application.weapontransportation.justification;

import fi.riista.feature.permit.application.weapontransportation.WeaponTransportationPermitApplication;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

import static fi.riista.feature.permit.application.weapontransportation.justification.WeaponTransportationVehicleType.MUU;
import static java.util.Objects.requireNonNull;

public class JustificationDTO {

    public static JustificationDTO create(final WeaponTransportationPermitApplication application,
                                          final List<TransportedWeaponDTO> transportedWeapons,
                                          final List<WeaponTransportationVehicleDTO> vehicles) {
        requireNonNull(application);
        requireNonNull(transportedWeapons);
        requireNonNull(vehicles);

        return new JustificationDTO(application.getJustification(), transportedWeapons, vehicles);
    }

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @NotBlank
    private String justification;

    @Valid
    private List<TransportedWeaponDTO> transportedWeapons = new ArrayList<>();

    @Valid
    private List<WeaponTransportationVehicleDTO> vehicles = new ArrayList<>();

    public JustificationDTO() {}

    public JustificationDTO(final String justification,
                            final List<TransportedWeaponDTO> transportedWeapons,
                            final List<WeaponTransportationVehicleDTO> vehicles) {
        this.justification = justification;
        this.transportedWeapons = transportedWeapons;
        this.vehicles = vehicles;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(final String justification) {
        this.justification = justification;
    }

    public List<TransportedWeaponDTO> getTransportedWeapons() {
        return transportedWeapons;
    }

    public void setTransportedWeapons(final List<TransportedWeaponDTO> transportedWeapons) {
        this.transportedWeapons = transportedWeapons;
    }

    public List<WeaponTransportationVehicleDTO> getVehicles() {
        return vehicles;
    }

    public void setVehicles(final List<WeaponTransportationVehicleDTO> vehicles) {
        this.vehicles = vehicles;
    }
}

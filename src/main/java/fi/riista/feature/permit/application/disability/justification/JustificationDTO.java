package fi.riista.feature.permit.application.disability.justification;

import fi.riista.feature.permit.application.disability.DisabilityPermitApplication;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class JustificationDTO {

    public static JustificationDTO create(final @Nonnull DisabilityPermitApplication application,
                                          final @Nonnull List<DisabilityPermitVehicleDTO> vehicles,
                                          final @Nonnull List<DisabilityPermitHuntingTypeInfoDTO> huntingTypeInfos) {
        requireNonNull(application);
        requireNonNull(vehicles);
        requireNonNull(huntingTypeInfos);

        final JustificationDTO dto = new JustificationDTO(application.getJustification(),
                vehicles,
                huntingTypeInfos);

        return dto;
    }

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @NotBlank
    private String justification;

    @Valid
    private List<DisabilityPermitVehicleDTO> vehicles = new ArrayList<>();

    @Valid
    private List<DisabilityPermitHuntingTypeInfoDTO> huntingTypeInfos = new ArrayList<>();

    public JustificationDTO() {}

    public JustificationDTO(final String justification,
                            final List<DisabilityPermitVehicleDTO> vehicles,
                            final List<DisabilityPermitHuntingTypeInfoDTO> huntingTypeInfos) {
        this.justification = justification;
        this.vehicles = vehicles;
        this.huntingTypeInfos = huntingTypeInfos;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(final String justification) {
        this.justification = justification;
    }

    public List<DisabilityPermitVehicleDTO> getVehicles() {
        return vehicles;
    }

    public void setVehicles(final List<DisabilityPermitVehicleDTO> vehicles) {
        this.vehicles = vehicles;
    }

    public List<DisabilityPermitHuntingTypeInfoDTO> getHuntingTypeInfos() {
        return huntingTypeInfos;
    }

    public void setHuntingTypeInfos(final List<DisabilityPermitHuntingTypeInfoDTO> huntingTypeInfos) {
        this.huntingTypeInfos = huntingTypeInfos;
    }
}

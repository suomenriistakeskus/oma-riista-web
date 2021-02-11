package fi.riista.feature.shootingtest.registration;

import javax.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

public class RegisterParticipantDTO {

    public static RegisterParticipantDTO create(@Nonnull final String hunterNumber) {
        final RegisterParticipantDTO dto = new RegisterParticipantDTO();
        dto.setHunterNumber(requireNonNull(hunterNumber));
        dto.setSelectedTypes(new SelectedShootingTestTypesDTO());
        return dto;
    }

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String hunterNumber;

    @Valid
    @NotNull
    private SelectedShootingTestTypesDTO selectedTypes;

    // Accessors -->

    public String getHunterNumber() {
        return hunterNumber;
    }

    public void setHunterNumber(final String hunterNumber) {
        this.hunterNumber = hunterNumber;
    }

    public SelectedShootingTestTypesDTO getSelectedTypes() {
        return selectedTypes;
    }

    public void setSelectedTypes(final SelectedShootingTestTypesDTO selectedTypes) {
        this.selectedTypes = selectedTypes;
    }
}

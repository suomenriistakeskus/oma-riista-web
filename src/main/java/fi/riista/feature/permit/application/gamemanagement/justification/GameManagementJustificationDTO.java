package fi.riista.feature.permit.application.gamemanagement.justification;

import fi.riista.feature.permit.application.gamemanagement.GameManagementPermitApplication;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

public class GameManagementJustificationDTO {

    public static GameManagementJustificationDTO create(final @Nonnull GameManagementPermitApplication application) {
        Objects.requireNonNull(application);

        final GameManagementJustificationDTO dto = new GameManagementJustificationDTO();
        dto.setJustification(application.getJustification());

        return dto;
    }

    @NotNull
    @Size(min = 5)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String justification;

    public String getJustification() {
        return justification;
    }

    public void setJustification(final String justification) {
        this.justification = justification;
    }
}

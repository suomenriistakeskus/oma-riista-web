package fi.riista.feature.permit.application.research.justification;

import fi.riista.feature.permit.application.research.ResearchPermitApplication;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.validation.constraints.Size;

import static java.util.Objects.requireNonNull;

public class ResearchJustificationDTO {

    public static ResearchJustificationDTO create(final @Nonnull ResearchPermitApplication application) {
        requireNonNull(application);

        final ResearchJustificationDTO dto = new ResearchJustificationDTO();
        dto.setJustification(application.getJustification());

        return dto;
    }

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

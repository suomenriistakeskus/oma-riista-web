package fi.riista.feature.permit.decision;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fi.riista.feature.permit.decision.informationrequest.InformationRequestLinkType;
import org.joda.time.LocalDateTime;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Objects;

public class DecisionInformationPublishingDTO extends  DecisionInformationLinkDTO{

    @NotNull
    private Long linkOpenedCount;

    public DecisionInformationPublishingDTO(
            @NotNull final Long id,
            @NotNull final String recipientEmail,
            @NotNull final String recipientName,
            @NotNull final Long linkOpenedCount,
            final LocalDateTime expirationDate,
            @NotNull final LocalDateTime creationTime,
            @NotNull final InformationRequestLinkType linkType) {


        super(id, recipientEmail, recipientName, expirationDate, creationTime, linkType);

        this.linkOpenedCount = Objects.requireNonNull(linkOpenedCount);
    }

    public Long getLinkOpenedCount() {
        return linkOpenedCount;
    }

    public void setLinkOpenedCount(final Long linkOpenedCount) {
        this.linkOpenedCount = linkOpenedCount;
    }
}

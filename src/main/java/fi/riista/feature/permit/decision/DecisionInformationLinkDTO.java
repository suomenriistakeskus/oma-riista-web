package fi.riista.feature.permit.decision;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fi.riista.feature.permit.decision.informationrequest.InformationRequestLinkType;
import fi.riista.sql.SQInformationRequestLink;
import org.joda.time.LocalDateTime;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Objects;

public class DecisionInformationLinkDTO {
    @NotNull
    Long id;

    @NotBlank
    @Email
    String recipientEmail;

    @NotBlank
    String recipientName;

    @JsonIgnore
    LocalDateTime expirationDate;

    @NotNull
    LocalDateTime creationTime;

    @Valid
    @NotNull
    InformationRequestLinkType linkType;

    public DecisionInformationLinkDTO(@NotNull final Long id,
                                      @NotNull final String recipientEmail,
                                      @NotNull final String recipientName,
                                      final LocalDateTime expirationDate,
                                      @NotNull final LocalDateTime creationTime,
                                      @NotNull final InformationRequestLinkType linkType) {
        this.id = Objects.requireNonNull(id);
        this.recipientEmail = Objects.requireNonNull(recipientEmail);
        this.recipientName = Objects.requireNonNull(recipientName);
        this.expirationDate = expirationDate;
        this.creationTime = Objects.requireNonNull(creationTime);
        this.linkType = Objects.requireNonNull(linkType);
    }

    public Long getId() {
        return id;
    }

    protected void setId(final Long id) {
        this.id = id;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    protected void setRecipientEmail(final String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    protected String getRecipientName() {
        return recipientName;
    }

    protected void setRecipientName(final String recipientName) {
        this.recipientName = recipientName;
    }

    protected LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    protected void setExpirationDate(final LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    protected LocalDateTime getCreationTime() {
        return creationTime;
    }

    protected void setCreationTime(final LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    public InformationRequestLinkType getLinkType() {
        return linkType;
    }

    protected void setLinkType(final InformationRequestLinkType linkType) {
        this.linkType = linkType;
    }
}

package fi.riista.feature.permit.decision;

import fi.riista.feature.permit.decision.informationrequest.InformationRequestLinkType;
import fi.riista.validation.XssSafe;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class PublishDecisionInformationDTO {
    private Long id;

    @NotBlank
    @Email
    private String recipientEmail;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String recipientName;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String title;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String description;

    @Valid
    @NotNull
    private InformationRequestLinkType linkType;

    public PublishDecisionInformationDTO() {}

    public PublishDecisionInformationDTO(final Long id, final String recipientEmail, final String recipientName, final String title, final String description, final InformationRequestLinkType linkType) {
        this.id = id;
        this.recipientEmail = recipientEmail;
        this.recipientName = recipientName;
        this.title = title;
        this.description = description;
        this.linkType = linkType;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(final String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(final String recipientName) {
        this.recipientName = recipientName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public InformationRequestLinkType getLinkType() {
        return linkType;
    }

    public void setLinkType(final InformationRequestLinkType linkType) {
        this.linkType = linkType;
    }
}

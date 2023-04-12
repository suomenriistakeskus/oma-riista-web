package fi.riista.feature.permit.decision.pdf;

import fi.riista.feature.common.decision.GrantStatus;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.application.DeliveryAddressDTO;
import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionDocument;
import fi.riista.feature.permit.decision.document.PermitDecisionDocumentHeadingDTO;
import fi.riista.util.Locales;
import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

import static fi.riista.feature.permit.decision.PermitDecision.DecisionType.CANCEL_ANNUAL_RENEWAL;
import static org.springframework.util.StringUtils.hasText;

public class PermitDecisionPdfDTO {

    private final boolean swedish;
    private final String permitNumber;

    private final List<String> additionalPermitNumbers;
    private final boolean draft;
    private final PermitDecisionDocument document;
    private final PermitDecisionDocumentHeadingDTO heading;
    private final PermitHolderDTO permitHolder;
    private final OrganisationNameDTO huntingClub;
    private final PersonContactInfoDTO contactPerson;
    private final DeliveryAddressDTO deliveryAddress;
    private final LocalDateTime publishDate;
    private final GrantStatus grantStatus;
    private final boolean classified;
    private final PermitDecision.DecisionType decisionType;

    public boolean isIncludeApplicationReasoning() {
        return hasText(document.getApplicationReasoning());
    }

    public boolean isIncludeProcessing() {
        return hasText(document.getProcessing());
    }

    public boolean isIncludeNotificationObligation() {
        return !isRejected() && hasText(document.getNotificationObligation());
    }

    public boolean isIncludeRestriction() {
        return !isRejected() && !isAnnualRenewalCanceled() && (hasText(document.getRestriction()) || hasText(document.getRestrictionExtra()));
    }

    public boolean isIncludeExecution() {
        return !isRejected() && !isAnnualRenewalCanceled() && hasText(document.getExecution());
    }

    public boolean isIncludePayment() {
        return hasText(document.getPayment());
    }

    public boolean isIncludeAttachments() {
        return hasText(document.getAttachments());
    }

    public PermitDecisionPdfDTO(final @Nonnull String permitNumber,
                                final @Nonnull PermitDecision decision,
                                final @Nonnull PermitDecisionDocument document,
                                final @Nonnull List<String> additionalPermitNumbers,
                                final boolean draft) {
        Objects.requireNonNull(decision);
        Objects.requireNonNull(document);
        Objects.requireNonNull(decision.getContactPerson());
        Objects.requireNonNull(decision.getApplication());

        this.swedish = Locales.isSwedish(decision.getLocale());
        this.permitNumber = Objects.requireNonNull(permitNumber);
        this.draft = draft;
        this.additionalPermitNumbers = additionalPermitNumbers;
        this.publishDate = decision.getPublishDate() != null ? decision.getPublishDate().toLocalDateTime() : null;
        this.document = document;
        this.heading = new PermitDecisionDocumentHeadingDTO(decision.getLocale(), decision.getDecisionName());
        this.contactPerson = PersonContactInfoDTO.create(decision.getContactPerson());
        this.deliveryAddress = DeliveryAddressDTO.from(decision.getDeliveryAddress());
        this.permitHolder = PermitHolderDTO.createFrom(decision.getPermitHolder());
        this.huntingClub = decision.getHuntingClub() != null
                ? OrganisationNameDTO.create(decision.getHuntingClub())
                : null;

        this.grantStatus = decision.getGrantStatus();
        this.classified = PermitTypeCode.isDisabilityPermitTypeCode(decision.getPermitTypeCode());
        this.decisionType = decision.getDecisionType();
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public List<String> getAdditionalPermitNumbers() {
        return additionalPermitNumbers;
    }

    public boolean isDraft() {
        return draft;
    }

    public boolean isSwedish() {
        return swedish;
    }

    public PermitDecisionDocument getDocument() {
        return document;
    }

    public PermitHolderDTO getPermitHolder() {
        return permitHolder;
    }

    public OrganisationNameDTO getHuntingClub() {
        return huntingClub;
    }

    public PermitDecisionDocumentHeadingDTO getHeading() {
        return heading;
    }

    public PersonContactInfoDTO getContactPerson() {
        return contactPerson;
    }

    public DeliveryAddressDTO getDeliveryAddress() {
        return deliveryAddress;
    }

    public LocalDateTime getPublishDate() {
        return publishDate;
    }

    public boolean isRejected() {
        return GrantStatus.REJECTED.equals(grantStatus);
    }

    public boolean isClassified() {
        return classified;
    }

    public boolean isAnnualRenewalCanceled() {
        return  decisionType == CANCEL_ANNUAL_RENEWAL;
    }
}

package fi.riista.feature.common.decision.nomination.document;

import org.hibernate.validator.constraints.SafeHtml;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Access(AccessType.FIELD)
public class NominationDecisionDocument implements Serializable {

    public void updateContent(final NominationDecisionSectionIdentifier sectionId, final String value) {
        Objects.requireNonNull(sectionId, "sectionId is null");

        switch (sectionId) {
            case PROPOSAL:
                this.proposal = value;
                break;
            case PROCESSING:
                this.processing = value;
                break;
            case DECISION:
                this.decision = value;
                break;
            case DECISION_REASONING:
                this.decisionReasoning = value;
                break;
            case LEGAL_ADVICE:
                this.legalAdvice = value;
                break;
            case APPEAL:
                this.appeal = value;
                break;
            case ADDITIONAL_INFO:
                this.additionalInfo = value;
                break;
            case DELIVERY:
                this.delivery = value;
                break;
            case ATTACHMENTS:
                this.attachments = value;
                break;
            default:
                throw new IllegalArgumentException("Unknown sectionId: " + sectionId);
        }
    }

    // Hakemus

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC)
    @Column(name = "proposal_body", columnDefinition = "TEXT")
    private String proposal = "";

    // Välitoimenpiteet

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC)
    @Column(name = "processing_body", columnDefinition = "TEXT")
    private String processing = "";

    // Päätös

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC)
    @Column(name = "decision_body", columnDefinition = "TEXT")
    private String decision = "";

    // Päätöksen perustelut

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC)
    @Column(name = "decision_reasoning_body", columnDefinition = "TEXT")
    private String decisionReasoning = "";

    // Oikeusohjeet

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC)
    @Column(name = "legal_advice_body", columnDefinition = "TEXT")
    private String legalAdvice = "";

    // Muutoksenhaku

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC)
    @Column(name = "appeal_body", columnDefinition = "TEXT")
    private String appeal = "";

    // Lisätiedot

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC)
    @Column(name = "additional_info_body", columnDefinition = "TEXT")
    private String additionalInfo = "";

    // Jakelu

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC)
    @Column(name = "delivery_body", columnDefinition = "TEXT")
    private String delivery = "";

    // Maksu

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC)
    @Column(name = "payment_body", columnDefinition = "TEXT")
    private String payment = "";

    // Liitteet

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC)
    @Column(name = "attachments_body", columnDefinition = "TEXT")
    private String attachments = "";

    public String getProposal() {
        return proposal;
    }

    public void setProposal(final String proposal) {
        this.proposal = proposal;
    }

    public String getProcessing() {
        return processing;
    }

    public void setProcessing(final String processingBody) {
        this.processing = processingBody;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(final String decisionBody) {
        this.decision = decisionBody;
    }

    public String getDecisionReasoning() {
        return decisionReasoning;
    }

    public void setDecisionReasoning(final String decisionReasoningBody) {
        this.decisionReasoning = decisionReasoningBody;
    }

    public String getLegalAdvice() {
        return legalAdvice;
    }

    public void setLegalAdvice(final String legalAdviceBody) {
        this.legalAdvice = legalAdviceBody;
    }

    public String getAppeal() {
        return appeal;
    }

    public void setAppeal(final String appealBody) {
        this.appeal = appealBody;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(final String additionalInfoBody) {
        this.additionalInfo = additionalInfoBody;
    }

    public String getDelivery() {
        return delivery;
    }

    public void setDelivery(final String deliveryBody) {
        this.delivery = deliveryBody;
    }

    public String getPayment() {
        return payment;
    }

    public void setPayment(final String paymentBody) {
        this.payment = paymentBody;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(final String attachmentsBody) {
        this.attachments = attachmentsBody;
    }
}

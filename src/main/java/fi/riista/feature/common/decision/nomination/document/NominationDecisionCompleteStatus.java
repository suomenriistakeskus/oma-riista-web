package fi.riista.feature.common.decision.nomination.document;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Objects;
import java.util.stream.Stream;

@Embeddable
@Access(AccessType.FIELD)
public class NominationDecisionCompleteStatus implements Serializable {

    public static void copy(final NominationDecisionCompleteStatus from, final NominationDecisionCompleteStatus to) {
        to.setProposal(from.isProposal());
        to.setProcessing(from.isProcessing());
        to.setDecisionReasoning(from.isDecisionReasoning());
        to.setDecision(from.isDecision());
        to.setLegalAdvice(from.isLegalAdvice());
        to.setAppeal(from.isAppeal());
        to.setAdditionalInfo(from.isAdditionalInfo());
        to.setDelivery(from.isDelivery());
        to.setAttachments(from.isAttachments());
    }

    public void updateStatus(final NominationDecisionSectionIdentifier sectionId, final boolean value) {
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

    @Transient
    public boolean allComplete() {
        return streamEditableStatus().allMatch(Boolean::booleanValue);
    }


    private Stream<Boolean> streamEditableStatus() {
        return Stream.of(proposal, processing, decision, decisionReasoning, legalAdvice, appeal, additionalInfo,
                delivery, attachments);
    }

    // Esitys

    @Column(name = "proposal_complete", nullable = false)
    private boolean proposal;

    // Välitoimenpiteet

    @Column(name = "processing_complete", nullable = false)
    private boolean processing;

    // Päätös

    @Column(name = "decision_complete", nullable = false)
    private boolean decision;

    // Päätöksen perustelut

    @Column(name = "decision_reasoning_complete", nullable = false)
    private boolean decisionReasoning;

    // Oikeusohjeet

    @Column(name = "legal_advice_complete", nullable = false)
    private boolean legalAdvice;

    // Muutoksenhaku

    @Column(name = "appeal_complete", nullable = false)
    private boolean appeal;

    // Lisätiedot

    @Column(name = "additional_info_complete", nullable = false)
    private boolean additionalInfo;

    // Jakelu

    @Column(name = "delivery_complete", nullable = false)
    private boolean delivery;

    // Liitteet

    @Column(name = "attachments_complete", nullable = false)
    private boolean attachments;

    public boolean isProposal() {
        return proposal;
    }

    public void setProposal(final boolean proposal) {
        this.proposal = proposal;
    }

    public boolean isProcessing() {
        return processing;
    }

    public void setProcessing(final boolean processingComplete) {
        this.processing = processingComplete;
    }

    public boolean isDecision() {
        return decision;
    }

    public void setDecision(final boolean decisionComplete) {
        this.decision = decisionComplete;
    }

    public boolean isDecisionReasoning() {
        return decisionReasoning;
    }

    public void setDecisionReasoning(final boolean decisionReasoningComplete) {
        this.decisionReasoning = decisionReasoningComplete;
    }

    public boolean isLegalAdvice() {
        return legalAdvice;
    }

    public void setLegalAdvice(final boolean legalAdviceComplete) {
        this.legalAdvice = legalAdviceComplete;
    }

    public boolean isAppeal() {
        return appeal;
    }

    public void setAppeal(final boolean appealComplete) {
        this.appeal = appealComplete;
    }

    public boolean isAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(final boolean additionalInfoComplete) {
        this.additionalInfo = additionalInfoComplete;
    }

    public boolean isDelivery() {
        return delivery;
    }

    public void setDelivery(final boolean deliveryComplete) {
        this.delivery = deliveryComplete;
    }

    public boolean isAttachments() {
        return attachments;
    }

    public void setAttachments(final boolean attachmentsComplete) {
        this.attachments = attachmentsComplete;
    }
}

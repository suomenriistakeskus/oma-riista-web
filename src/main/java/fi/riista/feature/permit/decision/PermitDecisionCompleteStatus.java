package fi.riista.feature.permit.decision;

import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.decision.document.PermitDecisionSectionIdentifier;

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
public class PermitDecisionCompleteStatus implements Serializable {

    public static void copy(final PermitDecisionCompleteStatus from, final PermitDecisionCompleteStatus to) {
        to.setApplication(from.isApplication());
        to.setApplicationReasoning(from.isApplicationReasoning());
        to.setProcessing(from.isProcessing());
        to.setDecisionReasoning(from.isDecisionReasoning());
        to.setDecision(from.isDecision());
        to.setRestriction(from.isRestriction());
        to.setExecution(from.isExecution());
        to.setLegalAdvice(from.isLegalAdvice());
        to.setNotificationObligation(from.isNotificationObligation());
        to.setAppeal(from.isAppeal());
        to.setAdditionalInfo(from.isAdditionalInfo());
        to.setDelivery(from.isDelivery());
        to.setPayment(from.isPayment());
        to.setAttachments(from.isAttachments());
        to.setAdministrativeCourt(from.isAdministrativeCourt());
    }

    public void updateStatus(final PermitDecisionSectionIdentifier sectionId, final boolean value) {
        Objects.requireNonNull(sectionId, "sectionId is null");

        switch (sectionId) {
            case APPLICATION:
                this.application = value;
                break;
            case APPLICATION_REASONING:
                this.applicationReasoning = value;
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
            case RESTRICTION:
                this.restriction = value;
                break;
            case EXECUTION:
                this.execution = value;
                break;
            case LEGAL_ADVICE:
                this.legalAdvice = value;
                break;
            case NOTIFICATION_OBLIGATION:
                this.notificationObligation = value;
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
            case PAYMENT:
                this.payment = value;
                break;
            case ADMINISTRATIVE_COURT:
                this.administrativeCourt = value;
                break;
            case ATTACHMENTS:
                this.attachments = value;
                break;
            default:
                throw new IllegalArgumentException("Unknown sectionId: " + sectionId);
        }
    }

    @Transient
    public boolean allComplete(final String permitTypeCode) {
        final boolean commonEditableStatus = streamCommonEditableStatus().allMatch(Boolean::booleanValue);
        final boolean deliveryStatus = PermitTypeCode.isDisabilityPermitTypeCode(permitTypeCode) || delivery;

        return commonEditableStatus && deliveryStatus;
    }

    @Transient
    public boolean allCompleteForRejected(final String permitTypeCode) {
        final boolean commonEditableStatus = streamCommonEditableStatusForRejection().allMatch(Boolean::booleanValue);
        final boolean deliveryStatus = PermitTypeCode.isDisabilityPermitTypeCode(permitTypeCode) || delivery;

        return commonEditableStatus && deliveryStatus;
    }

    private Stream<Boolean> streamCommonEditableStatus() {
        return Stream.of(applicationReasoning, processing, decisionReasoning, execution,
                legalAdvice, notificationObligation, appeal, additionalInfo, payment, administrativeCourt,
                attachments);
    }

    private Stream<Boolean> streamCommonEditableStatusForRejection() {
        return Stream.of(applicationReasoning, processing, decisionReasoning,
                legalAdvice, notificationObligation, appeal, additionalInfo, payment, administrativeCourt,
                attachments);
    }

    // Hakemus

    @Column(name = "application_complete", nullable = false)
    private boolean application;

    // Hakemuksen perustelut

    @Column(name = "application_reasoning_complete", nullable = false)
    private boolean applicationReasoning;

    // Välitoimenpiteet

    @Column(name = "processing_complete", nullable = false)
    private boolean processing;

    // Päätös

    @Column(name = "decision_complete", nullable = false)
    private boolean decision;

    // Päätöksen perustelut

    @Column(name = "decision_reasoning_complete", nullable = false)
    private boolean decisionReasoning;

    // Ehdot

    @Column(name = "restriction_complete", nullable = false)
    private boolean restriction;

    // Päätöksen täytäntöönpano

    @Column(name = "execution_complete", nullable = false)
    private boolean execution;

    // Oikeusohjeet

    @Column(name = "legal_advice_complete", nullable = false)
    private boolean legalAdvice;

    // Tiedoksiantovelvoite

    @Column(name = "notification_obligation_complete", nullable = false)
    private boolean notificationObligation;

    // Muutoksenhaku

    @Column(name = "appeal_complete", nullable = false)
    private boolean appeal;

    // Lisätiedot

    @Column(name = "additional_info_complete", nullable = false)
    private boolean additionalInfo;

    // Jakelu

    @Column(name = "delivery_complete", nullable = false)
    private boolean delivery;

    // Maksu

    @Column(name = "payment_complete", nullable = false)
    private boolean payment;

    // Hallinto oikeus

    @Column(name = "administrative_court_complete", nullable = false)
    private boolean administrativeCourt;

    // Liitteet

    @Column(name = "attachments_complete", nullable = false)
    private boolean attachments;

    public boolean isApplication() {
        return application;
    }

    public void setApplication(final boolean applicationComplete) {
        this.application = applicationComplete;
    }

    public boolean isApplicationReasoning() {
        return applicationReasoning;
    }

    public void setApplicationReasoning(final boolean applicationReasoningComplete) {
        this.applicationReasoning = applicationReasoningComplete;
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

    public boolean isRestriction() {
        return restriction;
    }

    public void setRestriction(final boolean restrictionComplete) {
        this.restriction = restrictionComplete;
    }

    public boolean isExecution() {
        return execution;
    }

    public void setExecution(final boolean executionComplete) {
        this.execution = executionComplete;
    }

    public boolean isLegalAdvice() {
        return legalAdvice;
    }

    public void setLegalAdvice(final boolean legalAdviceComplete) {
        this.legalAdvice = legalAdviceComplete;
    }

    public boolean isNotificationObligation() {
        return notificationObligation;
    }

    public void setNotificationObligation(final boolean notificationObligationComplete) {
        this.notificationObligation = notificationObligationComplete;
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

    public boolean isPayment() {
        return payment;
    }

    public void setPayment(final boolean paymentComplete) {
        this.payment = paymentComplete;
    }

    public boolean isAdministrativeCourt() {
        return administrativeCourt;
    }

    public void setAdministrativeCourt(final boolean administrativeCourtComplete) {
        this.administrativeCourt = administrativeCourtComplete;
    }

    public boolean isAttachments() {
        return attachments;
    }

    public void setAttachments(final boolean attachmentsComplete) {
        this.attachments = attachmentsComplete;
    }
}

package fi.riista.feature.permit.decision;

import fi.riista.feature.permit.decision.document.PermitDecisionSectionIdentifier;
import org.hibernate.validator.constraints.SafeHtml;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Access(AccessType.FIELD)
public class PermitDecisionDocument implements Serializable {

    public void updateContent(final PermitDecisionSectionIdentifier sectionId, final String value) {
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
            case DECISION_EXTRA:
                this.decisionExtra = value;
                break;
            case DECISION_REASONING:
                this.decisionReasoning = value;
                break;
            case RESTRICTION:
                this.restriction = value;
                break;
            case RESTRICTION_EXTRA:
                this.restrictionExtra = value;
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

    // Hakemus

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC)
    @Column(name = "application_body", columnDefinition = "TEXT")
    private String application;

    // Hakemuksen perustelut

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC)
    @Column(name = "application_reasoning_body", columnDefinition = "TEXT")
    private String applicationReasoning;

    // Välitoimenpiteet

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC)
    @Column(name = "processing_body", columnDefinition = "TEXT")
    private String processing;

    // Päätös

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC)
    @Column(name = "decision_body", columnDefinition = "TEXT")
    private String decision;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC)
    @Column(name = "decision_extra", columnDefinition = "TEXT")
    private String decisionExtra;

    // Päätöksen perustelut

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC)
    @Column(name = "decision_reasoning_body", columnDefinition = "TEXT")
    private String decisionReasoning;

    // Ehdot

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC)
    @Column(name = "restriction_body", columnDefinition = "TEXT")
    private String restriction;


    @SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC)
    @Column(name = "restriction_extra", columnDefinition = "TEXT")
    private String restrictionExtra;

    // Päätöksen täytäntöönpano

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC)
    @Column(name = "execution_body", columnDefinition = "TEXT")
    private String execution;

    // Oikeusohjeet

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC)
    @Column(name = "legal_advice_body", columnDefinition = "TEXT")
    private String legalAdvice;

    // Tiedoksiantovelvoite

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC)
    @Column(name = "notification_obligation_body", columnDefinition = "TEXT")
    private String notificationObligation;

    // Muutoksenhaku

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC)
    @Column(name = "appeal_body", columnDefinition = "TEXT")
    private String appeal;

    // Lisätiedot

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC)
    @Column(name = "additional_info_body", columnDefinition = "TEXT")
    private String additionalInfo;

    // Jakelu

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC)
    @Column(name = "delivery_body", columnDefinition = "TEXT")
    private String delivery;

    // Maksu

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC)
    @Column(name = "payment_body", columnDefinition = "TEXT")
    private String payment;

    // Hallinto oikeus

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC)
    @Column(name = "administrative_court_body", columnDefinition = "TEXT")
    private String administrativeCourt;

    // Liitteet

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC)
    @Column(name = "attachments_body", columnDefinition = "TEXT")
    private String attachments;

    public String getApplication() {
        return application;
    }

    public void setApplication(final String applicationBody) {
        this.application = applicationBody;
    }

    public String getApplicationReasoning() {
        return applicationReasoning;
    }

    public void setApplicationReasoning(final String applicationReasoningBody) {
        this.applicationReasoning = applicationReasoningBody;
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

    public String getDecisionExtra() {
        return decisionExtra;
    }

    public void setDecisionExtra(final String decisionExtra) {
        this.decisionExtra = decisionExtra;
    }

    public String getDecisionReasoning() {
        return decisionReasoning;
    }

    public void setDecisionReasoning(final String decisionReasoningBody) {
        this.decisionReasoning = decisionReasoningBody;
    }

    public String getRestriction() {
        return restriction;
    }

    public void setRestriction(final String restrictionBody) {
        this.restriction = restrictionBody;
    }

    public String getRestrictionExtra() {
        return restrictionExtra;
    }

    public void setRestrictionExtra(final String restrictionExtra) {
        this.restrictionExtra = restrictionExtra;
    }

    public String getExecution() {
        return execution;
    }

    public void setExecution(final String executionBody) {
        this.execution = executionBody;
    }

    public String getLegalAdvice() {
        return legalAdvice;
    }

    public void setLegalAdvice(final String legalAdviceBody) {
        this.legalAdvice = legalAdviceBody;
    }

    public String getNotificationObligation() {
        return notificationObligation;
    }

    public void setNotificationObligation(final String notificationObligationBody) {
        this.notificationObligation = notificationObligationBody;
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

    public String getAdministrativeCourt() {
        return administrativeCourt;
    }

    public void setAdministrativeCourt(final String administrativeCourtBody) {
        this.administrativeCourt = administrativeCourtBody;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(final String attachmentsBody) {
        this.attachments = attachmentsBody;
    }
}

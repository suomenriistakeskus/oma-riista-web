package fi.riista.feature.common.decision.nomination;

import fi.riista.util.Locales;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.function.BiFunction;

public class NominationDecisionDocumentHeadingDTO {

    public NominationDecisionDocumentHeadingDTO(final @Nonnull Locale locale, final @Nonnull String decisionName) {
        final boolean swedishLocale = Locales.isSwedish(locale);
        final BiFunction<String, String, String> i18n = (fi, sv) -> swedishLocale ? sv : fi;

        this.title = i18n.apply("PÄÄTÖS", "BESLUT");
        this.decisionDate = i18n.apply("Pvm", "Datum");
        this.documentNumber = i18n.apply("Nro", "Nr.");
        this.decisionName = decisionName.toUpperCase();
        this.applicant = i18n.apply("Hakija", "Sökande");
        this.proposal = i18n.apply("ESITYS", "FÖRSLAG");
        this.processing = i18n.apply("Välitoimenpiteet", "Interimistiska åtgärder");
        this.decision = i18n.apply("PÄÄTÖS", "BESLUT");
        this.restriction = i18n.apply("Ehdot", "Villkor");
        this.decisionReasoning = i18n.apply("Päätöksen perustelut", "Motiveringar till beslutet");
        this.execution = i18n.apply("Päätöksen täytäntöönpano", "Verkställighet av beslutet");
        this.legalAdvice = i18n.apply("Oikeusohjeet", "Rättsnormer");
        this.notificationObligation = i18n.apply("Tiedoksiantovelvoite", "Delgivningsskyldighet");
        this.appeal = i18n.apply("Muutoksenhaku", "Överklagande");
        this.additionalInfo = i18n.apply("LISÄTIEDOT", "TILLÄGGSUPPGIFTER");
        this.delivery = i18n.apply("JAKELU", "DISTRIBUTION");
        this.payment = i18n.apply("MAKSU", "AVGIFT");
        this.attachments = i18n.apply("LIITTEET", "BILAGOR");
    }

    private final String title;
    private final String decisionDate;
    private final String decisionName;
    private final String documentNumber;
    private final String applicant;
    private final String proposal;
    private final String processing;
    private final String decision;
    private final String decisionReasoning;
    private final String restriction;
    private final String execution;
    private final String legalAdvice;
    private final String notificationObligation;
    private final String appeal;
    private final String additionalInfo;
    private final String delivery;
    private final String payment;
    private final String attachments;

    public String getTitle() {
        return title;
    }

    public String getDecisionDate() {
        return decisionDate;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public String getDecisionName() {
        return decisionName;
    }

    public String getProposal() {
        return proposal;
    }

    public String getApplicant() {
        return applicant;
    }

    public String getProcessing() {
        return processing;
    }

    public String getDecision() {
        return decision;
    }

    public String getDecisionReasoning() {
        return decisionReasoning;
    }

    public String getRestriction() {
        return restriction;
    }

    public String getExecution() {
        return execution;
    }

    public String getLegalAdvice() {
        return legalAdvice;
    }

    public String getNotificationObligation() {
        return notificationObligation;
    }

    public String getAppeal() {
        return appeal;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public String getDelivery() {
        return delivery;
    }

    public String getPayment() {
        return payment;
    }

    public String getAttachments() {
        return attachments;
    }
}

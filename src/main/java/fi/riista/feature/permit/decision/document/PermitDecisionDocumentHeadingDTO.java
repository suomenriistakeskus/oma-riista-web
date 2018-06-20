package fi.riista.feature.permit.decision.document;

import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.util.Locales;

import java.util.Locale;
import java.util.function.BiFunction;

public class PermitDecisionDocumentHeadingDTO {

    public PermitDecisionDocumentHeadingDTO(final Locale locale) {
        final boolean swedishLocale = Locales.isSwedish(locale);
        final BiFunction<String, String, String> i18n = (fi, sv) -> swedishLocale ? sv : fi;

        this.title = i18n.apply("PÄÄTÖS", "BESLUT");
        this.decisionDate = i18n.apply("Pvm", "Datum");
        this.decisionNumber = i18n.apply("Nro", "Nr.");
        this.decisionName = HarvestPermit.MOOSELIKE_PERMIT_NAME.getTranslation(locale).toUpperCase();
        this.applicant = i18n.apply("Hakijan asiakasnumero ja nimi", "Sökandens kundnummer och namn");
        this.application = i18n.apply("HAKEMUS", "ANSÖKAN");
        this.applicationReasoning = i18n.apply("Hakemuksen perustelut", "Motiveringar till ansökan");
        this.processing = i18n.apply("Välitoimenpiteet", "Interimistiska åtgärder");
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
    private final String decisionNumber;
    private final String applicant;
    private final String application;
    private final String applicationReasoning;
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

    public String getDecisionNumber() {
        return decisionNumber;
    }

    public String getDecisionName() {
        return decisionName;
    }

    public String getApplicant() {
        return applicant;
    }

    public String getApplication() {
        return application;
    }

    public String getApplicationReasoning() {
        return applicationReasoning;
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

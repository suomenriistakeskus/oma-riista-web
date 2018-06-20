package fi.riista.feature.permit.decision.document;

import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.species.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionDocument;
import fi.riista.feature.permit.decision.action.PermitDecisionAction;
import fi.riista.feature.permit.decision.attachment.PermitDecisionAttachment;
import fi.riista.feature.permit.decision.authority.PermitDecisionAuthority;
import fi.riista.feature.permit.decision.delivery.PermitDecisionDelivery;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;
import fi.riista.util.F;
import fi.riista.util.Locales;
import fi.riista.util.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

@Service
public class PermitDecisionTextService {

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Transactional(noRollbackFor = RuntimeException.class)
    public void fillInBlanks(final PermitDecision decision) {
        if (decision.getDocument() == null) {
            decision.setDocument(new PermitDecisionDocument());
        }
        final PermitDecisionDocument doc = decision.getDocument();

        doc.setApplication(generateApplicationSummary(decision));
        if (StringUtils.isBlank(doc.getApplicationReasoning())) {
            doc.setApplicationReasoning(generateApplicationReasoning(decision));
        }
        doc.setProcessing(generateProcessing(decision));
        doc.setDecision(generateDecision(decision));
        doc.setRestriction(generateRestriction(decision));
        if (StringUtils.isBlank(doc.getDecisionReasoning())) {
            doc.setDecisionReasoning(generateDecisionReasoning(decision));
        }
        if (StringUtils.isBlank(doc.getLegalAdvice())) {
            doc.setLegalAdvice(generateLegalAdvice(decision));
        }
        if (StringUtils.isBlank(doc.getAppeal())) {
            doc.setAppeal(generateAppeal(decision));
        }
        doc.setAdditionalInfo(generateAdditionalInfo(decision));
        doc.setDelivery(generateDelivery(decision));
        doc.setPayment(generatePayment(decision));
        doc.setAttachments(generateAttachments(decision));
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generateApplicationSummary(final PermitDecision decision) {
        final HarvestPermitApplication application = Objects.requireNonNull(decision.getApplication());
        final GISZoneSizeDTO areaSize = gisZoneRepository.getAreaSize(application.getArea().getZone().getId());
        return PermitDecisionApplicationSummaryGenerator.generate(decision.getLocale(), application, areaSize);
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generateApplicationReasoning(final PermitDecision decision) {
        final StringBuilder sb = new StringBuilder();

        for (final HarvestPermitApplicationSpeciesAmount speciesAmount : decision.getApplication().getSpeciesAmounts()) {
            if (StringUtils.isNotBlank(speciesAmount.getDescription())) {
                sb.append(speciesAmount.getGameSpecies().getNameFinnish());
                sb.append(": ");
                sb.append(speciesAmount.getDescription());
                sb.append("\n\n");
            }
        }

        return sb.toString().trim();
    }

    private List<PermitDecisionSpeciesAmount> getSortedSpecies(final PermitDecision decision) {
        return decision.getSpeciesAmounts().stream()
                .sorted(Comparator.comparing(PermitDecisionSpeciesAmount::getAmount).reversed())
                .collect(Collectors.toList());
    }

    private static String i18n(final PermitDecision decision, final String finnish, final String swedish) {
        return Locales.isSwedish(decision.getLocale()) ? swedish : finnish;
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generateDecision(final PermitDecision decision) {
        final StringBuilder sb = new StringBuilder();

        final boolean allAmountsZero = decision.getSpeciesAmounts().stream().noneMatch(spa -> spa.getAmount() > 0);

        if (allAmountsZero) {
            sb.append(i18n(decision,
                    "Suomen riistakeskus on päättänyt hylätä hakemuksen.",
                    "Finlands viltcentral har beslutat att avslå ansökan."));
            return sb.toString();
        }

        final DateTimeFormatter DF = DateTimeFormat.forPattern("dd.MM.YYYY");
        final DecimalFormat NF = new DecimalFormat("#.#", new DecimalFormatSymbols(Locales.FI));
        sb.append(i18n(decision,
                "Suomen riistakeskus on päättänyt myöntää hirvieläimen pyyntiluvan seuraavasti:",
                "Finlands viltcentral har beslutat bevilja jaktlicens för hjortdjur enligt följande:"));
        sb.append("\n\n");
        sb.append("---|---|---\n");

        for (final PermitDecisionSpeciesAmount speciesAmount : getSortedSpecies(decision)) {
            sb.append(formatSpeciesName(speciesAmount, decision.getLocale()));
            sb.append("|");
            sb.append(NF.format(speciesAmount.getAmount()));
            sb.append(" ");
            sb.append(i18n(decision, "kpl", "st."));
            sb.append("|");

            if (speciesAmount.getAmount() > 0) {
                if (speciesAmount.getBeginDate() != null || speciesAmount.getEndDate() != null) {
                    if (speciesAmount.getBeginDate() != null) {
                        sb.append(DF.print(speciesAmount.getBeginDate()));
                    }

                    sb.append(" - ");

                    if (speciesAmount.getEndDate() != null) {
                        sb.append(DF.print(speciesAmount.getEndDate()));
                    }
                }

                if (speciesAmount.getBeginDate2() != null || speciesAmount.getEndDate2() != null) {
                    sb.append(", ");

                    if (speciesAmount.getBeginDate2() != null) {
                        sb.append(DF.print(speciesAmount.getBeginDate2()));
                    }

                    sb.append(" - ");

                    if (speciesAmount.getEndDate2() != null) {
                        sb.append(DF.print(speciesAmount.getEndDate2()));
                    }
                }
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generateRestriction(final PermitDecision decision) {
        final DecimalFormat NF = new DecimalFormat("#.#", new DecimalFormatSymbols(Locales.FI));
        final StringBuilder sb = new StringBuilder();

        for (final PermitDecisionSpeciesAmount speciesAmount : getSortedSpecies(decision)) {
            if (speciesAmount.getRestrictionAmount() != null && speciesAmount.getRestrictionType() != null) {
                sb.append(formatSpeciesName(speciesAmount, decision.getLocale()));
                sb.append(" ");
                sb.append(i18n(decision,
                        "määrä enintään",
                        "antal högst"));
                sb.append(" ");
                sb.append(NF.format(speciesAmount.getRestrictionAmount()));
                sb.append(" ");

                switch (speciesAmount.getRestrictionType()) {
                    case AE:
                        sb.append(i18n(decision,
                                "aikuista eläintä",
                                "djur"));
                        break;
                    case AU:
                        sb.append(i18n(decision,
                                "aikuista urosta",
                                Math.round(speciesAmount.getAmount()) > 1 ? "tjurar" : "tjur"));
                        break;
                }
                sb.append(".\n");
            }
        }

        return sb.toString();
    }

    @Nonnull
    private static String formatSpeciesName(final PermitDecisionSpeciesAmount speciesAmount, final Locale locale) {
        final String name = speciesAmount.getGameSpecies().getNameLocalisation().getTranslation(locale);
        return StringUtils.capitalize(name.toLowerCase());
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generateProcessing(final PermitDecision decision) {
        return decision.getActions().stream()
                .sorted(Comparator.comparing(PermitDecisionAction::getPointOfTime))
                .map(PermitDecisionAction::getDecisionText)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("\n\n"));
    }

    public String generateDecisionReasoning(final PermitDecision decision) {
        return i18n(decision,
                "Keskeiset sovelletut säännökset",
                "Centrala tillämpade bestämmelser") +
                "\n\n";
    }

    public String generateLegalAdvice(final PermitDecision decision) {
        return i18n(decision,
                "Päätöstä tehtäessä on sovellettu seuraavia oikeusohjeita:",
                "Vid fattandet av beslutet har följande rättsnormer följts:") +
                "\n\n";
    }

    public String generateNotificationObligation(final PermitDecision decision) {
        return i18n(decision,
                "Hallintolain (434/2003) 56 §:n 2 momentin mukaan hakijan on ilmoitettava tämän päätöksen" +
                        " tiedoksisaannista muille hakemuksen allekirjoittajille uhalla, että se mainitun lain 68 §:n 1 momentin" +
                        " mukaan laiminlyödessään ilmoitusvelvollisuuden on velvollinen korvaamaan ilmoittamatta jättämisestä" +
                        " taikka sen viivästymisestä aiheutuneen vahingon, sikäli kuin se laiminlyönnin laatuun ja muihin" +
                        " olosuhteisiin nähden harkitaan kohtuulliseksi.",
                "Enligt 56 § 2 mom. i förvaltningslagen (434/2003) ska sökanden delge de övriga parter som har" +
                        " undertecknat ansökan om detta beslut vid äventyr att den sökande enligt 68 § 1 mom. i nämnda" +
                        " lag är skyldig att ersätta en skada som uppstår på grund av att underrättelsen försummas eller" +
                        " av att handlingen inte överlämnas eller av att den försenas, i den mån det prövas vara skäligt" +
                        " med hänsyn till försummelsens art och övriga omständigheter.");
    }

    public String generateAppeal(final PermitDecision decision) {
        return i18n(decision,
                "Suomen riistakeskuksen päätökseen tyytymätön saa hakea siihen muutosta alueella toimivaltaiselta" +
                        " hallinto-oikeudelta kirjallisella valituksella. Valitusosoitus on päätöksen liitteenä." +
                        "\n\n" +
                        "Käsittelymaksun määräämisen osalta valittaja voi vaatia valtion maksuperustelain (150/1992)" +
                        " 11 b §:n nojalla oikaisua Suomen riistakeskukselta. Oikaisuvaatimusosoitus on päätöksen liitteenä.",
                "Den som är missnöjd med Finlands viltcentrals beslut kan söka ändring i det hos den" +
                        " förvaltningsdomstol som är behörig på området genom skriftligt besvär. Besvärsanvisning har" +
                        " bilagts beslutet. I fråga om fastställande av handläggningsavgift kan den som söker ändring" +
                        " yrka på rättelse hos Finlands viltcentral med stöd av 11 b § i lagen om grunderna för avgifter" +
                        " till staten (150/1992). Anvisning om rättelseyrkande har bilagts beslutet.");
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generateAdditionalInfo(final PermitDecision decision) {
        final boolean deliveryByMail = Boolean.TRUE.equals(decision.getApplication().getDeliveryByMail());

        final PermitDecisionAuthority presenter = F.firstNonNull(decision.getPresenter(), decision.getDecisionMaker());

        final StringBuilder sb = new StringBuilder();
        sb.append(i18n(decision,
                "Lisätietoja päätöksestä antaa",
                "Tilläggsuppgifter om beslutet ges av"));
        sb.append(":");

        if (presenter != null) {
            sb.append("<br><br>\n\n");
            sb.append(presenter.getFirstName());
            sb.append(' ');
            sb.append(presenter.getLastName());
            sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
            sb.append(presenter.getPhoneNumber());
            sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
            sb.append(presenter.getEmail());
            sb.append("\n\n");
        }

        sb.append("<br><br>\n\n");
        sb.append(i18n(decision,
                "SUOMEN RIISTAKESKUS",
                "FINLANDS VILTCENTRAL"));

        sb.append("<br>");
        sb.append(i18n(decision,
                "Julkiset hallintotehtävät",
                "Offentliga förvaltningsuppgifter"));
        sb.append("\n\n");

        appendSignature(sb, resolveSigner1(decision));
        appendSignature(sb, resolveSigner2(decision));

        sb.append("<br>\n\n");
        sb.append(i18n(decision,
                "Päätös on allekirjoitettu koneellisesti riistahallintolain (158/2011) 8 §:n 4 momentin nojalla.",
                "Beslutet är undertecknat maskinellt med stöd av viltförvaltningslagens (158/2011) 8 § 4 mom."));
        sb.append("\n\n");

        sb.append((deliveryByMail
                ? i18n(decision,
                "Päätös hakijalle kirjeenä.",
                "Beslut till den sökande per post.")
                : i18n(decision,
                "Päätös hakijalle sähköisen palvelun kautta.",
                "Beslut till den sökande via elektronisk tjänst.")));

        return sb.toString();
    }

    private PermitDecisionAuthority resolveSigner1(final PermitDecision decision) {
        return F.firstNonNull(decision.getDecisionMaker(), decision.getPresenter());
    }

    private PermitDecisionAuthority resolveSigner2(final PermitDecision decision) {
        final PermitDecisionAuthority presenter = F.firstNonNull(decision.getPresenter(), decision.getDecisionMaker());
        final PermitDecisionAuthority decisionMaker = F.firstNonNull(decision.getDecisionMaker(), decision.getPresenter());
        if (presenter == null && decisionMaker == null) {
            return null;
        }
        // both should be non-null, but can be same objects
        if (Objects.requireNonNull(presenter).isSameAs(decisionMaker)) {
            return null;
        }
        return presenter;
    }

    private void appendSignature(final StringBuilder sb, final PermitDecisionAuthority a) {
        if (a != null) {
            sb.append("<br><br>\n\n");
            sb.append(a.getFirstName());
            sb.append(' ');
            sb.append(a.getLastName());
            sb.append("<br>");
            sb.append(a.getTitle());
            sb.append("\n\n");
        }
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generateDelivery(final PermitDecision decision) {
        return i18n(decision,
                "Tiedoksi",
                "Till kännedom") +
                ":\n" +
                decision.getDelivery().stream()
                        .map(PermitDecisionDelivery::getName)
                        .collect(joining("\n"));
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generatePayment(final PermitDecision decision) {
        if (decision.isPaymentAmountPositive()) {
            return String.format("%s %.2f EUR",
                    i18n(decision,
                            "Käsittelymaksu",
                            "Handläggningsavgift"),
                    decision.getPaymentAmount().doubleValue());
        } else {
            return i18n(decision,
                    "Päätös hakijalle käsittelymaksutta.",
                    "Beslut till den sökande utan handläggningsavgift.");
        }
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generateAttachments(final PermitDecision decision) {
        return decision.getAttachments().stream()
                .filter(a -> !a.isDeleted()
                        && a.getOrderingNumber() != null
                        && StringUtils.isNotBlank(a.getDescription()))
                .sorted(PermitDecisionAttachment.ATTACHMENT_COMPARATOR)
                .map(a -> {
                    final StringBuilder sb = new StringBuilder();

                    if (a.getOrderingNumber() != null) {
                        sb.append(i18n(decision, "LIITE", "BILAG"));
                        sb.append(" ");
                        sb.append(Integer.toString(a.getOrderingNumber()));
                        sb.append(": ");
                    }

                    if (a.getDescription() != null) {
                        sb.append(a.getDescription());
                    }

                    return sb.toString();
                })
                .collect(Collectors.joining("\n"));
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generateAdjustedAreaSizeText(final PermitDecision decision) {
        return Optional.ofNullable(decision.getApplication())
                .map(HarvestPermitApplication::getArea)
                .map(HarvestPermitArea::getZone)
                .map(HasID::getId)
                .map(gisZoneRepository::getAdjustedAreaSize)
                .map(size -> {
                    final String msg = "Suomen riistakeskus on tehnyt Oma riista -palvelussa tarkistuslaskennan" +
                            " Metsähallituksen alueista ja määrittänyt uudelleen hakemuksen valtionmaiden" +
                            " maapinta-alan ja yksityismaiden maapinta-alan." +
                            "\n\n" +
                            "Hakemuksen tarkistetut pinta-alat:" +
                            "\n\n" +
                            "Valtionmaiden maapinta-ala %d ha\n" +
                            "Yksityismaiden maapinta-ala %d ha";

                    return String.format(msg,
                            NumberUtils.squareMetersToHectares(size.getStateLandAreaSize()),
                            NumberUtils.squareMetersToHectares(size.getPrivateLandAreaSize()));
                })
                .orElse("");
    }
}

package fi.riista.feature.common.decision.nomination.document;

import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.common.decision.authority.DecisionRkaAuthorityDetails;
import fi.riista.feature.common.decision.nomination.NominationDecision;
import fi.riista.feature.common.decision.nomination.action.NominationDecisionAction;
import fi.riista.feature.common.decision.nomination.action.NominationDecisionActionRepository;
import fi.riista.feature.common.decision.nomination.attachment.NominationDecisionAttachmentRepository;
import fi.riista.feature.common.decision.nomination.authority.NominationDecisionAuthority;
import fi.riista.feature.common.decision.nomination.delivery.NominationDecisionDelivery;
import fi.riista.feature.common.decision.nomination.delivery.NominationDecisionDeliveryRepository;
import fi.riista.feature.organization.jht.JHTPeriod;
import fi.riista.feature.organization.jht.nomination.QOccupationNomination;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.util.F;
import fi.riista.util.Locales;
import org.apache.commons.lang.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static fi.riista.feature.organization.jht.nomination.OccupationNomination.NominationStatus.ESITETTY;
import static fi.riista.util.DateUtil.DATE_FORMAT_FINNISH;
import static fi.riista.util.DateUtil.today;
import static fi.riista.util.F.mapNullable;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

@Service
public class NominationDecisionTextService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern(DATE_FORMAT_FINNISH);

    @Resource
    private NominationDecisionActionRepository actionRepository;

    @Resource
    private NominationDecisionAttachmentRepository attachmentRepository;

    @Resource
    private NominationDecisionDeliveryRepository nominationDecisionDeliveryRepository;

    @Resource
    private MessageSource messageSource;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Transactional(noRollbackFor = RuntimeException.class)
    public void generateDefaultTextSections(final NominationDecision decision) {
        if (decision.getDocument() == null) {
            decision.setDocument(new NominationDecisionDocument());
        }
        final NominationDecisionDocument doc = decision.getDocument();

        doc.setProposal(generateProposal(decision));

        doc.setProcessing(generateProcessing(decision));
        doc.setDecision(generateDecision(decision));
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
    public String generateProposal(final NominationDecision decision) {

        switch (decision.getDecisionType()) {

            case NOMINATION:
                return generateProposalForNomination(decision);
            case NOMINATION_CANCELLATION:
                return generateProposalForCancellation(decision);
            default:
                throw new IllegalArgumentException("Unsupported decision type: " + decision.getDecisionType());
        }
    }

    private String generateProposalForNomination(final NominationDecision decision) {
        final StringBuilder sb = new StringBuilder();
        sb.append(
                i18n(decision,
                        "Riistanhoitoyhdistys on esittänyt, että Suomen riistakeskus nimittäisi " +
                                "riistanhoitoyhdistyksen toimihenkilöiksi esityksessä mainitut henkilöt.",
                        "Jaktvårdsföreningen har föreslagit att Finlands viltcentral utnämner iförslaget " +
                                "nämnda personer till funktionärer i föreningen."))

                .append("\n\n")
                .append(i18n(decision,
                        "Esityksen tiedot: ",
                        "Uppgifter om förslaget: "))
                .append(messageSource.getMessage(
                        "OccupationType." + decision.getOccupationType(),
                        null,
                        decision.getLocale()))
                .append("\n\n");

        searchProposedPersons(decision).forEach(nominee -> {
                    sb.append(nominee.getLastName())
                            .append(", ")
                            .append(nominee.getFirstName());
                    if (nominee.getHunterNumber() != null) {
                        sb.append(" (")
                                .append(nominee.getHunterNumber())
                                .append(")");
                    }
                    sb.append("\n");
                }
        );

        sb.append("\n\n");
        return sb.toString();
    }

    private Iterable<Person> searchProposedPersons(final NominationDecision decision) {
        final QOccupationNomination OCCUPATION_NOMINATION = QOccupationNomination.occupationNomination;
        final QPerson NOMINEE = QPerson.person;

        return jpqlQueryFactory
                .select(NOMINEE)
                .from(OCCUPATION_NOMINATION)
                .join(OCCUPATION_NOMINATION.person, NOMINEE)
                .where(OCCUPATION_NOMINATION.rhy.eq(decision.getRhy()),
                        OCCUPATION_NOMINATION.occupationType.eq(decision.getOccupationType()),
                        OCCUPATION_NOMINATION.nominationStatus.eq(ESITETTY),
                        mapNullable(decision.getProposalDate(), date->OCCUPATION_NOMINATION.nominationDate.loe( date)))
                .orderBy(NOMINEE.lastName.asc(), NOMINEE.lastName.asc()).fetch();
    }

    private String generateProposalForCancellation(final NominationDecision decision) {
        final StringBuilder sb = new StringBuilder();
        return sb.append(
                i18n(decision,
                        "Riistanhoitoyhdistys on esittänyt, että Suomen riistakeskus peruuttaisi " +
                                "riistanhoitoyhdistyksen toimihenkilöksi nimittämisen esityksen mukaisesti.",
                        "Jaktvårdsföreningen har framställt, att Finlands viltcentral annullerar " +
                                "jaktvårdsföreningens framställan om utnämning av jaktvårdsföreningens funktionär " +
                                "i enlighet med förslaget."))
                .append("\n\n")
                .append(i18n(decision,
                        "Esityksen tiedot: ",
                        "Uppgifter om förslaget: "))
                .append(messageSource.getMessage("OccupationType." + decision.getOccupationType(), null,
                        decision.getLocale()))
                .toString();
    }


    private static String i18n(final NominationDecision decision, final String finnish, final String swedish) {
        return Locales.isSwedish(decision.getLocale()) ? swedish : finnish;
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generateSection(final NominationDecision decision,
                                  final NominationDecisionSectionIdentifier sectionIdentifier) {
        switch (sectionIdentifier) {
            case PROPOSAL:
                return generateProposal(decision);
            case DECISION:
                return generateDecision(decision);
            case PROCESSING:
                return generateProcessing(decision);
            case DECISION_REASONING:
                return generateDecisionReasoning(decision);
            case LEGAL_ADVICE:
                return generateLegalAdvice(decision);
            case APPEAL:
                return generateAppeal(decision);
            case ADDITIONAL_INFO:
                return generateAdditionalInfo(decision);
            case DELIVERY:
                return generateDelivery(decision);
            default:
                return "";
        }
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generateDecision(final NominationDecision decision) {
        switch (decision.getDecisionType()) {

            case NOMINATION:
                final JHTPeriod period = new JHTPeriod(today());
                return i18n(decision,
                        "Suomen riistakeskus on päättänyt tehdä nimittämiset esityksen mukaisesti " +
                                "toimikaudelle:\n\n",
                        "Finlands viltcentral har beslutat fastställa utnämningarna av funktionärer " +
                                "enligt det ingivna förslaget för verksamhetsperioden:\n\n") +
                        period.getBeginDate().toString(DATE_FORMATTER) + " - " +
                        period.getEndDate().toString(DATE_FORMATTER) +
                        "\n\n";

            case NOMINATION_CANCELLATION:
                return i18n(decision,
                        "Suomen riistakeskus on päättänyt peruuttaa nimittämisen esityksen mukaisesti.\n\n",
                        "Finlands viltcentral har beslutat annullera utnämningen i enlighet med förslaget.\n\n");
            default:
                throw new IllegalArgumentException("Unsupported decision type: " + decision.getDecisionType());
        }

    }


    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generateProcessing(final NominationDecision decision) {
        if (decision.isNew()){
            return "";
        }
        
        return actionRepository.findAllByNominationDecisionOrderByPointOfTimeAsc(decision).stream()
                .map(NominationDecisionAction::getDecisionText)
                .filter(StringUtils::isNotBlank)
                .collect(joining("\n\n"));
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generateDecisionReasoning(final NominationDecision decision) {

        return i18n(decision,
                "Keskeiset sovelletut säännökset\n\n" +
                        "Riistahallintolain (158/2011) 20 §:n 1 momentin mukaan riistanhoitoyhdistyksellä voi olla " +
                        "metsästyksenvalvojia, metsästäjätutkintojen ja ampumakokeiden vastaanottajia sekä muita " +
                        "riistanhoitoyhdistyksen toiminnan kannalta tarpeellisia toimihenkilöitä.\n\n" +
                        "Riistahallintolain 2 §:n 1 momentin mukaan Suomen riistakeskuksen julkisena " +
                        "hallintotehtävänä on mm. riistanhoitoyhdistysten metsästyksenvalvojien, metsästäjätutkinnon " +
                        "vastaanottajien ja ampumakokeiden vastaanottajien sekä riistavahinkolain (105/2009) 25 §:ssä" +
                        " tarkoitetuissa maastotarkastuksissa toimivien riistanhoitoyhdistysten edustajien " +
                        "nimittäminen, ohjaus ja valvonta.\n\n" +
                        "Riistahallinnosta annetun valtioneuvoston asetuksen (171/2011) 10 §:n 7 kohdan mukaan " +
                        "riistanhoitoyhdistyksen hallituksen tehtävänä on esittää Suomen riistakeskukselle " +
                        "nimitettäväksi metsästyksenvalvojat, ampumakokeiden ja metsästäjätutkintojen vastaanottajat " +
                        "sekä edustajat riistavahinkojen maastotarkastuksiin.\n\n" +
                        "Riistahallintolain 20 §:n 2 momentin mukaan riistanhoitoyhdistyksen toimihenkilöksi voidaan " +
                        "nimittää se, joka:\n" +
                        "1) on täyttänyt 18 vuotta;\n" +
                        "2) on täysivaltainen; ja\n" +
                        "3) tunnetaan rehelliseksi ja luotettavaksi ja on henkilökohtaisilta ominaisuuksiltaan " +
                        "tehtävään sopiva.\n\n" +
                        "Riistahallintolain 21 §:n 1 momentin mukaan metsästyksenvalvojaksi sekä ampumakokeen ja " +
                        "metsästäjätutkinnon vastaanottajaksi voidaan nimittää henkilö, joka on:\n" +
                        "1) hyväksytysti suorittanut Suomen riistakeskuksen järjestämän kyseisen tehtävän vaatiman " +
                        "koulutuksen; tai\n" +
                        "2) hyväksytysti suorittanut aikaisintaan kuusi kuukautta ennen uuden nimityksen hakemista " +
                        "Suomen riistakeskuksen järjestämän kyseisen tehtävän vaatiman kertauskoulutuksen, jos " +
                        "nimittämistä hakeva on jo aikaisemmin nimitetty vastaavaan tehtävään 1 kohdan perusteella.\n" +
                        "Riistahallintolain 21 §:n 2 momentin mukaan maa- ja metsätalousministeriön asetuksella " +
                        "säädetään tarkemmin 1 momentissa mainituista koulutuksista. Ao. koulutuksista säädetään " +
                        "riistanhoitoyhdistyksen toimihenkilöiden koulutusvaatimuksista annetun maa- ja " +
                        "metsätalousministeriön asetuksen (553/2011) 1-5 §:ssä.\n" +
                        "Riistahallintolain 22 §:n mukaan riistanhoitoyhdistyksen metsästyksenvalvojaksi sekä " +
                        "ampumakokeen ja metsästäjätutkinnon vastaanottajaksi voidaan nimittää enintään viideksi " +
                        "vuodeksi kerrallaan. Suomen riistakeskus katsoo, että nimittämisen on tarkoituksenmukaisinta" +
                        " olla voimassa metsästysvuoteen sidotusti ja enintään päätöskohdassa mainitun ajan. Tämän " +
                        "vuoksi Suomen riistakeskus on nimittänyt riistanhoitoyhdistyksen toimihenkilöt " +
                        "päätöskohdassa mainitulle ajanjaksolle.\n\n" +
                        "Edellä mainituilla perusteilla ja ottaen huomioon oikeusohjeissa mainitut säännökset Suomen " +
                        "riistakeskus on tehnyt päätöskohdasta ilmenevän päätöksen.",
                "Centrala tillämpade bestämmelser\n\n" +
                        "Enligt viltförvaltningslagens (158/2011) 20 § mom. 1 kan enjaktvårdsförening ha " +
                        "jaktövervakare, examinatorer för jägarexamina och skjutprov samt andra funktionärer som " +
                        "behövs för jaktvårdsföreningensverksamhet.\n\n" +
                        "Enligt viltförvaltningslagens 2 § mom. 1 har Finlands viltcentral somoffentliga " +
                        "förvaltningsuppgifter bl.a. tillsättande och ledning av samt tillsyn över " +
                        "jaktvårdsföreningarnas jaktövervakare, examinatorer förjägarexamina och skjutprov samt " +
                        "företrädarna för jaktvårdsföreningarnavid terrängundersökningar som avses i 25 § i " +
                        "viltskadelagen (105/2009).\n\n" +
                        "Enligt statsrådets förordning om viltförvaltningen (171/2011) 10 § 7 punkten har en " +
                        "jaktvårdsförenings styrelse till uppgift att ge förslag till Finlands viltcentral om " +
                        "utnämningar av jaktövervakare, examinatorer för skjutprov och jägarexamina samt företrädare " +
                        "vid terrängundersökningar som gäller viltskador.\n\n" +
                        "Enligt viltförvaltningslagens 20 § mom. 2 till funktionär i en jaktvårdsförening kan den " +
                        "väljas\n" +
                        "1) som har fyllt 18 år,\n" +
                        "2) som är myndig, och\n" +
                        "3) som är känd som redbar och tillförlitlig och till sina personliga egenskaper är lämplig " +
                        "för uppgiften.\n\n" +
                        "Enligt viltförvaltningslagens 21 § mom. 1 till jaktövervakare samt till examinatorer för " +
                        "jägarexamina och skjutprov kan den utnämnas\n" +
                        "1) som med godkänt resultat har genomgått den utbildning som ordnats av Finlands viltcentral" +
                        " och som uppgiften i fråga kräver, eller\n" +
                        "2) som tidigare med stöd av 1 punkten har utnämnts till motsvarande uppgift och som tidigast" +
                        " sex månader före ansökan om ny utnämning med godkänt resultat har genomgått den " +
                        "repetitionsutbildning som Finlands viltcentral ordnat och som uppgiften i fråga kräver.\n\n" +
                        "Enligt viltförvaltningslagens 21 § mom. 2 närmare bestämmelser om den utbildning som avses i " +
                        "1 mom. utfärdas genom förordning av jord- och skogsbruksministeriet. Om dessa utbildningar " +
                        "utfärdas i 1-5 § i jord- och skogsbruksministeriets förordning om utbildningskrav för " +
                        "jaktvårdsföreningens funktionärer (553/2011).\n\n" +
                        "Enligt viltförvaltningslagens 22 § får jaktövervakare samt respektive examinatorer för " +
                        "jägarexamina och skjutprov tillsättas för högst fem år åt gången. Finlands viltcentral finner " +
                        "det vara ändamålsenligast att utnämningens giltighet är bunden till jaktåret och högst till " +
                        "i beslutet nämnd tidpunkt. Därför har Finlands viltcentral utnämnt jaktvårdsföreningens " +
                        "funktionärer för i beslutet nämnd period.\n\n" +
                        "På ovannämnda grunder och beaktande gällande rättsregler har Finlands viltcentral fattat det " +
                        "beslut som framgår av beslutsmomentet.") +
                "\n\n";
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generateLegalAdvice(final NominationDecision decision) {
        return i18n(decision,
                "Päätöstä tehtäessä on sovellettu seuraavia oikeusohjeita:\n\n" +
                        "metsästyslaki (615/1993) 90 §\n" +
                        "riistahallintolaki (158/2011) 2 §, 20-22 § ja 30 §\n" +
                        "maksuperustelaki (150/1992) 6 § ja 11 b §\n" +
                        "valtioneuvoston asetus riistahallinnosta (171/2011) 10 §\n" +
                        "maa- ja metsätalousministeriön " +
                        "asetus riistanhoitoyhdistyksentoimihenkilöiden koulutusvaatimuksista (553/2011) 1-5 §\n" +
                        "maa- ja metsätalousministeriön asetus Suomen riistakeskuksen ja riistanhoitoyhdistysten " +
                        "julkisten " +
                        "hallintotehtävien maksuista vuosina 2022 ja 2023 (902/2021) 2 §",
                "Vid beslutsfattande har följande rättsnormer tillämpats:\n" +
                        "jaktlag (615/1993) 90 §\n" +
                        "viltförvaltningslag (158/2011) 2 §, 20-22 § och 30 §\n" +
                        "lag om grunderna för avgifter till staten (150/1992) 6 § och 11 b § statsrådets förordning " +
                        "om viltförvaltningen (171/2011) 10 §\n" +
                        "jord- och skogsbruksministeriets förordning om utbildningskrav för jaktvårdsföreningens " +
                        "funktionärer (553/2011) 1-5 §\n" +
                        "jord- och skogsbruksministeriets förordning om avgifterna 2022 och 2023 för Finlands " +
                        "viltcentrals och jaktvårdsföreningarnas offentliga förvaltningsuppgifter (902/2021) 2 §") +
                "\n\n";
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generateAppeal(final NominationDecision decision) {
        return i18n(decision,
                "Riistahallintolain 30 §:n mukaan Suomen riistakeskuksen muuhun kuin 2 §:n 1 momentin 1 kohdassa " +
                        "tarkoitettuun päätökseen ja riistanhoitoyhdistyksen metsästäjätutkinnon tai " +
                        "ampumakokeen hyväksymistä koskevaan päätökseen saa vaatia oikaisua Suomen riistakeskukselta " +
                        "siten kuin hallintolaissa säädetään.\n\n" +
                        "Oikaisuvaatimusosoitus on päätöksen liitteenä." +
                        "\n\n",
                "Enligt viltförvaltningslagens 30 § omprövning av beslut från Finlands viltcentral, med " +
                        "undantag för beslut enligt 2 § 1 mom. 1 punkten, och av jaktvårdsföreningens beslut om " +
                        "godkännande av jägarexamen och skjutprov får begäras hos Finlands viltcentral i enlighet med " +
                        "förvaltningslagen.\n\n" +
                        "Anvisning för omprövningsbegäran har bilagts beslutet.\n\n");
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generateAdditionalInfo(final NominationDecision decision) {

        final NominationDecisionAuthority authority =
                F.firstNonNull(decision.getPresenter(), decision.getDecisionMaker());

        final StringBuilder sb = new StringBuilder();
        sb.append(i18n(decision,
                "Lisätietoja päätöksestä antaa",
                "Tilläggsuppgifter om beslutet ges av"));
        sb.append(":");

        if (authority != null) {
            final DecisionRkaAuthorityDetails authorityDetails = authority.getAuthorityDetails();
            sb.append("<br><br>\n\n");
            sb.append(authorityDetails.getFirstName());
            sb.append(' ');
            sb.append(authorityDetails.getLastName());
            sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
            sb.append(authorityDetails.getPhoneNumber());
            sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
            sb.append(authorityDetails.getEmail());
            sb.append("\n\n");
        }

        sb.append("<br><br>\n\n");
        sb.append(i18n(decision,
                "SUOMEN RIISTAKESKUS",
                "FINLANDS VILTCENTRAL"));

        sb.append("<br>");
        sb.append(i18n(decision,
                "Julkiset hallintotehtävät",
                "Offentliga förvaltningsuppgifter"));
        sb.append("\n\n");

        appendSignature(sb, resolveSigner1(decision));
        appendSignature(sb, resolveSigner2(decision));

        sb.append("<br>\n\n");

        sb.append(i18n(decision,
                "Päätös hakijalle kirjeenä.",
                "Beslut till den sökande per post."));

        return sb.toString();
    }

    private static DecisionRkaAuthorityDetails resolveSigner1(final NominationDecision decision) {
        final NominationDecisionAuthority authority = F.firstNonNull(decision.getDecisionMaker(),
                decision.getPresenter());
        if (authority != null) {
            return authority.getAuthorityDetails();
        }
        return null;
    }

    private static DecisionRkaAuthorityDetails resolveSigner2(final NominationDecision decision) {
        final NominationDecisionAuthority presenter =
                F.firstNonNull(decision.getPresenter(), decision.getDecisionMaker());
        final NominationDecisionAuthority decisionMaker =
                F.firstNonNull(decision.getDecisionMaker(), decision.getPresenter());

        if (presenter == null && decisionMaker == null) {
            return null;
        }

        // both should be non-null, but can be same objects
        if (requireNonNull(presenter).isEqualTo(decisionMaker)) {
            return null;
        }
        return presenter.getAuthorityDetails();
    }

    private static void appendSignature(final StringBuilder sb, final DecisionRkaAuthorityDetails a) {
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
    public String generateDelivery(final NominationDecision decision) {
        if (decision.isNew()){
            return "";
        }

        return i18n(decision,
                "Tiedoksi",
                "Till kännedom") +
                ":\n" +
                nominationDecisionDeliveryRepository.findAllByNominationDecisionOrderById(decision).stream()
                        .map(NominationDecisionDelivery::getName)
                        .filter(StringUtils::isNotBlank)
                        .map(String::trim)
                        .map(String::toUpperCase)
                        .distinct()
                        .collect(joining("\n"))
                ;
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generatePayment(final NominationDecision decision) {

        return i18n(decision,
                "Päätös hakijalle käsittelymaksutta.",
                "Beslut till den sökande utan handläggningsavgift.");
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generateAttachments(final NominationDecision decision) {

        if (decision.isNew()){
            return "";
        }

        return attachmentRepository.findOrderedByNominationDecision(decision)
                .stream()
                .filter(a -> StringUtils.isNotBlank(a.getDescription()))
                .map(a -> {
                    final StringBuilder sb = new StringBuilder();

                    if (a.getOrderingNumber() != null) {
                        sb.append(i18n(decision, "LIITE", "BILAGA"))
                                .append(" ")
                                .append(a.getOrderingNumber())
                                .append(": ");
                    }

                    if (a.getDescription() != null) {
                        sb.append(a.getDescription());
                    }

                    return sb.toString();
                })
                .collect(joining("\n"));
    }


}

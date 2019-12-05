package fi.riista.feature.permit.decision.document;

import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.amendment.AmendmentApplicationData;
import fi.riista.feature.permit.application.amendment.AmendmentApplicationDataRepository;
import fi.riista.feature.permit.application.bird.BirdPermitApplication;
import fi.riista.feature.permit.application.bird.BirdPermitApplicationRepository;
import fi.riista.feature.permit.application.bird.BirdPermitApplicationSummaryDTO;
import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplication;
import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplicationRepository;
import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplicationSummaryDTO;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReasonService;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReasonsDTO;
import fi.riista.feature.permit.application.mammal.MammalPermitApplication;
import fi.riista.feature.permit.application.mammal.MammalPermitApplicationRepository;
import fi.riista.feature.permit.application.mammal.MammalPermitApplicationSummaryDTO;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionDocument;
import fi.riista.feature.permit.decision.action.PermitDecisionAction;
import fi.riista.feature.permit.decision.attachment.PermitDecisionAttachment;
import fi.riista.feature.permit.decision.authority.PermitDecisionAuthority;
import fi.riista.feature.permit.decision.delivery.PermitDecisionDelivery;
import fi.riista.feature.permit.decision.derogation.DerogationLawSection;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReason;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonRepository;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmountRepository;
import fi.riista.util.F;
import fi.riista.util.Locales;
import fi.riista.util.LocalisedString;
import fi.riista.util.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static fi.riista.feature.permit.decision.derogation.DerogationLawSection.SECTION_41A;
import static fi.riista.feature.permit.decision.derogation.DerogationLawSection.SECTION_41B;
import static fi.riista.feature.permit.decision.derogation.DerogationLawSection.SECTION_41C;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;

@Service
public class PermitDecisionTextService {

    private static final Comparator<PermitDecisionSpeciesAmount> COMPARATOR_AMOUNTS_DESC =
            comparing(PermitDecisionSpeciesAmount::getAmount).reversed();

    private static final Comparator<PermitDecisionSpeciesAmount> COMPARATOR_CARNIVORE =
            comparing(PermitDecisionSpeciesAmount::getAmount);

    private static final Comparator<PermitDecisionSpeciesAmount> COMPARATOR_BIRD_SPECIES =
            comparing((PermitDecisionSpeciesAmount a) -> a.getGameSpecies().getOfficialCode())
                    .thenComparing((PermitDecisionSpeciesAmount a) -> a.getBeginDate().getYear());

    @Nonnull
    private static String formatSpeciesName(final GameSpecies gameSpecies, final Locale locale) {
        final String name = gameSpecies.getNameLocalisation().getTranslation(locale);
        return StringUtils.capitalize(name.toLowerCase());
    }

    @Nonnull
    private static String formatSpeciesName(final HarvestPermitApplicationSpeciesAmount speciesAmount,
                                            final Locale locale) {
        return formatSpeciesName(speciesAmount.getGameSpecies(), locale);
    }

    @Nonnull
    private static String formatSpeciesName(final PermitDecisionSpeciesAmount speciesAmount,
                                            final Locale locale) {
        return formatSpeciesName(speciesAmount.getGameSpecies(), locale);
    }

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Resource
    private MessageSource messageSource;

    @Resource
    private AmendmentApplicationDataRepository amendmentApplicationDataRepository;

    @Resource
    private BirdPermitApplicationRepository birdPermitApplicationRepository;

    @Resource
    private CarnivorePermitApplicationRepository carnivorePermitApplicationRepository;

    @Resource
    private MammalPermitApplicationRepository mammalPermitApplicationRepository;

    @Resource
    private DerogationPermitApplicationReasonService derogationPermitApplicationReasonService;

    @Resource
    private PermitDecisionSpeciesAmountRepository permitDecisionSpeciesAmountRepository;

    @Resource
    private PermitDecisionDerogationReasonRepository permitDecisionDerogationReasonRepository;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Transactional(noRollbackFor = RuntimeException.class)
    public void generateDefaultTextSections(final PermitDecision decision,
                                            final boolean overwriteApplicationReasoning) {
        if (decision.getDocument() == null) {
            decision.setDocument(new PermitDecisionDocument());
        }
        final PermitDecisionDocument doc = decision.getDocument();

        doc.setApplication(generateApplicationSummary(decision));
        if (overwriteApplicationReasoning) {
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

    @Nonnull
    private Map<Integer, String> createSpeciesNames(final Locale locale) {
        return gameSpeciesService.getNameIndex().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getTranslation(locale)));
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generateApplicationSummary(final PermitDecision decision) {
        final HarvestPermitApplication application = Objects.requireNonNull(decision.getApplication());
        final Locale locale = decision.getLocale();

        switch (application.getHarvestPermitCategory()) {
            case MOOSELIKE:
                return PermitDecisionApplicationSummaryGenerator.generate(locale, application,
                        getPermitAreaSize(application));
            case MOOSELIKE_NEW:
                final AmendmentApplicationData data = amendmentApplicationDataRepository.getByApplication(application);
                return PermitDecisionAmendmentApplicationSummaryGenerator.generate(application, data, locale,
                        messageSource);
            case BIRD:
                return createBirdApplicationSummaryGenerator(application, locale).generateApplicationMain();
            case LARGE_CARNIVORE_BEAR:
            case LARGE_CARNIVORE_LYNX:
            case LARGE_CARNIVORE_LYNX_PORONHOITO:
            case LARGE_CARNIVORE_WOLF:
                return createCarnivoreApplicationSummaryGenerator(application, locale).generateApplicationMain();
            case MAMMAL:
                return createMammalApplicationSummaryGenerator(application, locale).generateApplicationMain();
            default:
                throw new IllegalArgumentException("Unsupported permit category: " +
                        application.getHarvestPermitCategory());
        }
    }

    private GISZoneSizeDTO getPermitAreaSize(final HarvestPermitApplication application) {
        return Optional.ofNullable(application.getArea())
                .map(HarvestPermitArea::getZone)
                .map(F::getId)
                .map(zoneId -> gisZoneRepository.getAreaSize(zoneId))
                .orElse(null);
    }

    @Nonnull
    private PermitDecisionBirdApplicationSummaryGenerator createBirdApplicationSummaryGenerator(
            final HarvestPermitApplication application, final Locale locale) {
        final BirdPermitApplication birdApplication =
                birdPermitApplicationRepository.findByHarvestPermitApplication(application);
        final BirdPermitApplicationSummaryDTO dto = BirdPermitApplicationSummaryDTO.create(application,
                birdApplication);

        return new PermitDecisionBirdApplicationSummaryGenerator(dto, locale, createSpeciesNames(locale),
                messageSource);
    }

    @Nonnull
    private PermitDecisionCarnivoreApplicationSummaryGenerator createCarnivoreApplicationSummaryGenerator(
            final HarvestPermitApplication application, final Locale locale) {
        final CarnivorePermitApplication carnivorePermitApplication =
                carnivorePermitApplicationRepository.findByHarvestPermitApplication(application);
        final CarnivorePermitApplicationSummaryDTO dto = CarnivorePermitApplicationSummaryDTO.from(application,
                carnivorePermitApplication);
        return new PermitDecisionCarnivoreApplicationSummaryGenerator(dto, locale, createSpeciesNames(locale),
                messageSource);
    }


    @Nonnull
    private PermitDecisionMammalApplicationSummaryGenerator createMammalApplicationSummaryGenerator(
            final HarvestPermitApplication application, final Locale locale) {

        final MammalPermitApplication mammalPermitApplication =
                mammalPermitApplicationRepository.findByHarvestPermitApplication(application);
        final DerogationPermitApplicationReasonsDTO derogationReasons =
                derogationPermitApplicationReasonService.getDerogationReasons(application, locale);
        final MammalPermitApplicationSummaryDTO dto =
                MammalPermitApplicationSummaryDTO.create(application, mammalPermitApplication, derogationReasons);
        return new PermitDecisionMammalApplicationSummaryGenerator(dto, locale, createSpeciesNames(locale),
                messageSource);
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generateApplicationReasoning(final PermitDecision decision) {
        final HarvestPermitApplication application = Objects.requireNonNull(decision.getApplication());
        final Locale locale = decision.getLocale();

        switch (application.getHarvestPermitCategory()) {
            case MOOSELIKE_NEW:
            case MOOSELIKE:
                return generateApplicationReasoningForMooselike(application, locale);

            case BIRD:
                return createBirdApplicationSummaryGenerator(application, locale).generateApplicationReasoning();
            case LARGE_CARNIVORE_BEAR:
            case LARGE_CARNIVORE_LYNX:
            case LARGE_CARNIVORE_LYNX_PORONHOITO:
            case LARGE_CARNIVORE_WOLF:
                return createCarnivoreApplicationSummaryGenerator(application, locale).generateApplicationReasoning();
            case MAMMAL:
                return createMammalApplicationSummaryGenerator(application, locale).generateApplicationReasoning();
        }

        return "";
    }

    private String generateApplicationReasoningForMooselike(final HarvestPermitApplication application,
                                                            final Locale locale) {
        final StringBuilder sb = new StringBuilder();

        for (final HarvestPermitApplicationSpeciesAmount speciesAmount : application.getSpeciesAmounts()) {
            if (StringUtils.isNotBlank(speciesAmount.getMooselikeDescription())) {
                sb.append(formatSpeciesName(speciesAmount, locale));
                sb.append(": ");
                sb.append(speciesAmount.getMooselikeDescription());
                sb.append("\n\n");
            }
        }

        return sb.toString().trim();
    }

    private List<PermitDecisionSpeciesAmount> getSortedSpecies(final PermitDecision decision) {
        return permitDecisionSpeciesAmountRepository.findByPermitDecision(decision).stream()
                .sorted(getSpeciesComparator(decision.getApplication().getHarvestPermitCategory()))
                .collect(Collectors.toList());
    }

    private static Comparator<PermitDecisionSpeciesAmount> getSpeciesComparator(final HarvestPermitCategory category) {
        switch (category) {
            case MOOSELIKE:
            case MOOSELIKE_NEW:
            case MAMMAL:
                return COMPARATOR_AMOUNTS_DESC;
            case BIRD:
                return COMPARATOR_BIRD_SPECIES;
            case LARGE_CARNIVORE_BEAR:
            case LARGE_CARNIVORE_LYNX:
            case LARGE_CARNIVORE_LYNX_PORONHOITO:
            case LARGE_CARNIVORE_WOLF:
                return COMPARATOR_CARNIVORE;
            default:
                throw new IllegalArgumentException("invalid category: " + category);
        }
    }

    private static String i18n(final PermitDecision decision, final String finnish, final String swedish) {
        return Locales.isSwedish(decision.getLocale()) ? swedish : finnish;
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generateDecision(final PermitDecision decision) {
        if (decision.getDecisionType() == PermitDecision.DecisionType.CANCEL_APPLICATION) {
            return i18n(decision,
                    "Suomen riistakeskus toteaa asian käsittelyn rauenneen hakijan pyynnöstä.",
                    "Finlands viltcentral konstaterar att ärendet har förfallit på sökandens begäran.");
        } else if (decision.getDecisionType() == PermitDecision.DecisionType.IGNORE_APPLICATION) {
            return i18n(decision,
                    "Hakemus jätetään tutkimatta.",
                    "Ansökan har avvisats utan prövning.");
        }

        final List<PermitDecisionSpeciesAmount> speciesAmounts =
                permitDecisionSpeciesAmountRepository.findByPermitDecision(decision);
        final boolean allAmountsZero = speciesAmounts.stream().noneMatch(spa -> spa.getAmount() > 0);

        if (allAmountsZero) {
            return i18n(decision,
                    "Suomen riistakeskus on päättänyt hylätä hakemuksen.",
                    "Finlands viltcentral har beslutat att avslå ansökan.");
        }

        final List<PermitDecisionDerogationReason> derogationReasonList =
                permitDecisionDerogationReasonRepository.findByPermitDecision(decision);

        return generateDecisionSectionTitle(decision) + "\n\n" +
                generateDecisionSectionTable(decision, getSortedSpecies(decision)) +
                generateDerogationReasonText(decision, derogationReasonList);
    }

    private static String generateDerogationReasonText(final PermitDecision decision,
                                                       final List<PermitDecisionDerogationReason> derogationReasonList) {
        if (derogationReasonList.isEmpty()) {
            return "";
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("\n\n");
        sb.append(i18n(decision, "Poikkeusedellytys", "Förutsättning för undantag"));
        sb.append("\n");

        if (hasLawSectionId(derogationReasonList, SECTION_41A, 1)) {
            sb.append("\n");
            sb.append(i18n(decision,
                    "Metsästyslain 41 a §:n 1 momentin 1 kohta: luonnonvaraisen eläimistön tai kasviston " +
                            "säilyttämiseksi",
                    "Jaktlagen 41 a § 1 moment 1 punkten: i syfte att bevara vilda djur eller växter"));
        }

        if (hasLawSectionId(derogationReasonList, SECTION_41A, 2)) {
            sb.append("\n");
            sb.append(i18n(decision,
                    "Metsästyslain 41 a §:n 1 momentin 2 kohta: viljelmille, karjankasvatukselle, metsätaloudelle, " +
                            "kalataloudelle, porotaloudelle, vesistölle tai muulle omaisuudelle aiheutuvan erityisen " +
                            "merkittävän vahingon ehkäisemiseksi",
                    "Jaktlagen 41 a § 1 moment 2 punkten: i syfte att förebygga allvarlig skada på odlingar, " +
                            "boskapsuppfödning, skogsbruk, fiskerinäring, renhushållning, vattendrag eller annan " +
                            "egendom"));
        }

        if (hasLawSectionId(derogationReasonList, SECTION_41A, 3)) {
            sb.append("\n");
            sb.append(i18n(decision,
                    "Metsästyslain 41 a §:n 1 momentin 3 kohta: kansanterveyden, yleisen turvallisuuden tai muun " +
                            "erittäin tärkeän yleisen edun kannalta pakottavista syistä, mukaan lukien taloudelliset " +
                            "ja sosiaaliset syyt, sekä jos poikkeamisesta on ensisijaisen merkittävää hyötyä " +
                            "ympäristölle",
                    "Jaktlagen 41 a § 1 moment 3 punkten: på grund av tvingande skäl med hänsyn till folkhälsan, den " +
                            "allmänna säkerheten eller något annat mycket viktigt allmänt intresse, inbegripet " +
                            "ekonomiska och sociala skäl, och om ett tillstånd till undantag medför synnerligen " +
                            "betydande nytta för miljön"));
        }

        if (hasLawSectionId(derogationReasonList, SECTION_41A, 4)) {
            sb.append("\n");
            sb.append(i18n(decision,
                    "Metsästyslain 41 a §:n 1 momentin 4 kohta: näiden lajien tutkimus-, koulutus-, " +
                            "uudelleensijoittamis- ja istuttamistarkoituksessa taikka eläintautien ehkäisemiseksi",
                    "Jaktlagen 41 a § 1 moment 4 punkten:  i forsknings-, utbildnings-, omplacerings- och " +
                            "utplanteringssyfte eller för att förebygga djursjukdomar när det gäller arterna i fråga"));
        }

        if (hasLawSectionId(derogationReasonList, SECTION_41B, 1)) {
            sb.append("\n");
            sb.append(i18n(decision,
                    "Metsästyslain 41 b §:n 1 momentin 1 kohta: kansanterveyden ja yleisen turvallisuuden " +
                            "turvaamiseksi",
                    "Jaktlagen 41 b § 1 moment 1 punkten: för att trygga folkhälsan och den allmänna säkerheten"));
        }

        if (hasLawSectionId(derogationReasonList, SECTION_41B, 2)) {
            sb.append("\n");
            sb.append(i18n(decision,
                    "Metsästyslain 41 b §:n 1 momentin 2 kohta: lentoturvallisuuden takaamiseksi",
                    "Jaktlagen 41 b § 1 moment 2 punkten: för att trygga flygsäkerheten"));
        }

        if (hasLawSectionId(derogationReasonList, SECTION_41B, 3)) {
            sb.append("\n");
            sb.append(i18n(decision,
                    "Metsästyslain 41 b §:n 1 momentin 3 kohta: viljelmille, kotieläimille, metsille, kalavesille ja " +
                            "vesistöille koituvan vakavan vahingon estämiseksi",
                    "Jaktlagen 41 b § 1 moment 3 punkten: för att förhindra allvarliga skador på odlingar, husdjur, " +
                            "skogar, fiskevatten och vattendrag"));
        }

        if (hasLawSectionId(derogationReasonList, SECTION_41B, 4)) {
            sb.append("\n");
            sb.append(i18n(decision,
                    "Metsästyslain 41 b §:n 1 momentin 4 kohta: kasviston ja eläimistön suojelemiseksi",
                    "Jaktlagen 41 b § 1 moment 4 punkten: för att skydda växter och djur"));
        }

        if (hasLawSectionId(derogationReasonList, SECTION_41B, 5)) {
            sb.append("\n");
            sb.append(i18n(decision,
                    "Metsästyslain 41 b §:n 1 momentin 5 kohta:  tutkimus- ja koulutustarkoituksessa, kannan " +
                            "lisäämis- ja uudelleenistutustarkoituksessa sekä tehdäkseen mahdolliseksi näitä varten " +
                            "tapahtuvan kasvatuksen",
                    "Jaktlagen 41 b § 1 moment 5 punkten: i forsknings- och utbildningssyfte, för att öka och " +
                            "återinföra stammen samt möjliggöra uppfödning för nämnda syften"));
        }

        if (hasLawSectionId(derogationReasonList, SECTION_41A, 6)) {
            sb.append("\n");
            sb.append(i18n(decision,
                    "Metsästyslain 41 a §:n 3 momentin mukaisesti tarkoin valvotuissa oloissa valikoiden ja " +
                            "rajoitetusti tiettyjen yksilöiden pyydystämiseksi tai tappamiseksi.",
                    "Enligt jaktlagen 41 a § 3 moment för att under strängt övervakade förhållanden, " +
                            "selektivt och begränsat fånga eller döda vissa djur."));
        }
        if (hasLawSectionId(derogationReasonList, SECTION_41C, 1)) {
            sb.append("\n");
            sb.append(i18n(decision,
                    "Metsästyslain 41 c §:n 1 kohta: luonnonvaraisen eläimistön tai kasviston säilyttämiseksi",
                    "Jaktlagen 41 c § 1 punkten:  i syfte att bevara vilda djur eller växter"));
        }

        if (hasLawSectionId(derogationReasonList, SECTION_41C, 2)) {
            sb.append("\n");
            sb.append(i18n(decision,
                    "Metsästyslain 41 c §:n 2 kohta: viljelmille, karjankasvatukselle, metsätaloudelle, " +
                            "kalataloudelle, porotaloudelle, riistataloudelle, vesistölle tai muulle omaisuudelle " +
                            "aiheutuvan merkittävän vahingon ehkäisemiseksi",
                    "Jaktlagen 41 c § 2 punkten:  i syfte att förebygga allvarlig skada på odlingar, " +
                            "boskapsuppfödning, skogsbruk, fiskerinäring, renhushållning, vilthushållning, vattendrag" +
                            " eller annan egendom"));
        }

        if (hasLawSectionId(derogationReasonList, SECTION_41C, 3)) {
            sb.append("\n");
            sb.append(i18n(decision,
                    "Metsästyslain 41 c §:n 3 kohta: kansanterveyden, yleisen turvallisuuden tai muun erittäin " +
                            "tärkeän yleisen edun kannalta pakottavista syistä, mukaan lukien taloudelliset ja " +
                            "sosiaaliset syyt, sekä jos poikkeamisesta on ensisijaisen merkittävää hyötyä ympäristölle",
                    "Jaktlagen 41 c § 3 punkten: av tvingande skäl med hänsyn till folkhälsan, den allmänna " +
                            "säkerheten eller något annat mycket viktigt allmänt intresse, inbegripet ekonomiska och " +
                            "sociala skäl, och om ett tillstånd till undantag medför synnerligen betydande nytta för " +
                            "miljön"));
        }

        if (hasLawSectionId(derogationReasonList, SECTION_41C, 4)) {
            sb.append("\n");
            sb.append(i18n(decision,
                    "Metsästyslain 41 c §:n 4 kohta: näiden lajien tutkimus-, koulutus-, uudelleensijoittamis- ja istuttamistarkoituksessa taikka eläintautien ehkäisemiseksi",
                    "Jaktlagen 41 c § 4 punkten: i forsknings-, utbildnings-, omplacerings- och utplanteringssyfte eller för att förebygga djursjukdomar när det gäller arterna i fråga"));
        }
        return sb.toString();
    }

    private static boolean hasLawSectionId(final List<PermitDecisionDerogationReason> derogationReasonList,
                                           final DerogationLawSection lawSection,
                                           final int lawSectionId) {
        return derogationReasonList.stream().filter(r -> r.getReasonType().getLawSection() == lawSection)
                .anyMatch(d -> d.getReasonType().getLawSectionNumber() == lawSectionId);
    }

    private static String generateDecisionSectionTable(final PermitDecision decision,
                                                       final List<PermitDecisionSpeciesAmount> speciesAmounts) {
        final DateTimeFormatter DF = DateTimeFormat.forPattern("dd.MM.YYYY");
        final DecimalFormat NF = new DecimalFormat("#.#", new DecimalFormatSymbols(Locales.FI));
        final long validityYears = speciesAmounts.stream()
                .mapToInt(spa -> spa.getBeginDate().getYear())
                .distinct().count();

        final StringBuilder sb = new StringBuilder();
        sb.append("---|---|---\n");

        for (final PermitDecisionSpeciesAmount speciesAmount : speciesAmounts) {
            sb.append(formatSpeciesName(speciesAmount, decision.getLocale()));

            if (validityYears > 1) {
                sb.append(String.format(" (%d)", speciesAmount.getBeginDate().getYear()));
            }

            sb.append("|");
            sb.append(NF.format(speciesAmount.getAmount()));
            sb.append(" ");
            sb.append(i18n(decision, "kpl", "st."));
            sb.append("|");

            if (speciesAmount.getAmount() > 0) {
                if (speciesAmount.getBeginDate() != null && speciesAmount.getEndDate() != null) {
                    sb.append(DF.print(speciesAmount.getBeginDate()));
                    sb.append(" - ");
                    sb.append(DF.print(speciesAmount.getEndDate()));
                }

                if (speciesAmount.getBeginDate2() != null && speciesAmount.getEndDate2() != null) {
                    sb.append(",\n");
                    sb.append("|||");

                    sb.append(DF.print(speciesAmount.getBeginDate2()));
                    sb.append(" - ");
                    sb.append(DF.print(speciesAmount.getEndDate2()));
                }
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    @Nonnull
    private static String generateDecisionSectionTitle(final PermitDecision decision) {
        switch (decision.getApplication().getHarvestPermitCategory()) {
            case MOOSELIKE:
            case MOOSELIKE_NEW:
                return i18n(decision,
                        "Suomen riistakeskus on päättänyt myöntää hirvieläimen pyyntiluvan seuraavasti:",
                        "Finlands viltcentral har beslutat bevilja jaktlicens för hjortdjur enligt följande:");
            case BIRD:
            case LARGE_CARNIVORE_BEAR:
            case LARGE_CARNIVORE_LYNX:
            case LARGE_CARNIVORE_LYNX_PORONHOITO:
            case LARGE_CARNIVORE_WOLF:
            case MAMMAL:
                return i18n(decision,
                        "Suomen riistakeskus on päättänyt myöntää poikkeusluvan seuraavasti:",
                        "Finlands viltcentral har beslutat att bevilja dispens enligt följande:");
            default:
                throw new IllegalArgumentException("Unsupported application category:" +
                        decision.getApplication().getHarvestPermitCategory());
        }
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generateRestriction(final PermitDecision decision) {
        final DecimalFormat NF = new DecimalFormat("#.#", new DecimalFormatSymbols(Locales.FI));
        final StringBuilder sb = new StringBuilder();

        for (final PermitDecisionSpeciesAmount speciesAmount : getSortedSpecies(decision)) {
            if (speciesAmount.getRestrictionAmount() != null && speciesAmount.getRestrictionType() != null) {
                sb.append(formatSpeciesName(speciesAmount, decision.getLocale()));
                sb.append(" ");
                sb.append(i18n(decision, "määrä enintään", "antal högst"));
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

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generateProcessing(final PermitDecision decision) {
        return decision.getActions().stream()
                .sorted(comparing(PermitDecisionAction::getPointOfTime))
                .map(PermitDecisionAction::getDecisionText)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("\n\n"));
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generateDecisionReasoning(final PermitDecision decision) {
        if (decision.getDecisionType() != PermitDecision.DecisionType.HARVEST_PERMIT) {
            return "";
        }

        return i18n(decision,
                "Keskeiset sovelletut säännökset",
                "Centrala tillämpade bestämmelser") +
                "\n\n";
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generateLegalAdvice(final PermitDecision decision) {
        return i18n(decision,
                "Päätöstä tehtäessä on sovellettu seuraavia oikeusohjeita:",
                "Vid fattandet av beslutet har följande rättsnormer följts:") +
                "\n\n";
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generateNotificationObligation(final PermitDecision decision) {
        return i18n(decision,
                "Hallintolain (434/2003) 56 §:n 2 momentin mukaan hakijan on ilmoitettava tämän päätöksen" +
                        " tiedoksisaannista muille hakemuksen allekirjoittajille uhalla, että se mainitun lain 68 §:n" +
                        " 1 momentin" +
                        " mukaan laiminlyödessään ilmoitusvelvollisuuden on velvollinen korvaamaan ilmoittamatta " +
                        "jättämisestä" +
                        " taikka sen viivästymisestä aiheutuneen vahingon, sikäli kuin se laiminlyönnin laatuun ja " +
                        "muihin" +
                        " olosuhteisiin nähden harkitaan kohtuulliseksi.",
                "Enligt 56 § 2 mom. i förvaltningslagen (434/2003) ska sökanden delge de övriga parter som har" +
                        " undertecknat ansökan om detta beslut vid äventyr att den sökande enligt 68 § 1 mom. i " +
                        "nämnda" +
                        " lag är skyldig att ersätta en skada som uppstår på grund av att underrättelsen försummas " +
                        "eller" +
                        " av att handlingen inte överlämnas eller av att den försenas, i den mån det prövas vara " +
                        "skäligt" +
                        " med hänsyn till försummelsens art och övriga omständigheter.");
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generateAppeal(final PermitDecision decision) {
        return i18n(decision,
                "Suomen riistakeskuksen päätökseen tyytymätön saa hakea siihen muutosta alueella toimivaltaiselta" +
                        " hallinto-oikeudelta kirjallisella valituksella. Valitusosoitus on päätöksen liitteenä." +
                        "\n\n" +
                        "Käsittelymaksun määräämisen osalta valittaja voi vaatia valtion maksuperustelain (150/1992)" +
                        " 11 b §:n nojalla oikaisua Suomen riistakeskukselta. Oikaisuvaatimusosoitus on päätöksen " +
                        "liitteenä.",
                "Den som är missnöjd med Finlands viltcentrals beslut kan söka ändring i det hos den" +
                        " förvaltningsdomstol som är behörig på området genom skriftligt besvär. Besvärsanvisning har" +
                        " bilagts beslutet. I fråga om fastställande av handläggningsavgift kan den som söker ändring" +
                        " yrka på rättelse hos Finlands viltcentral med stöd av 11 b § i lagen om grunderna för " +
                        "avgifter" +
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
                "Offentliga förvaltningsuppgifter"));
        sb.append("\n\n");

        appendSignature(sb, resolveSigner1(decision));
        appendSignature(sb, resolveSigner2(decision));

        sb.append("<br>\n\n");
        sb.append(i18n(decision,
                "Päätös on allekirjoitettu koneellisesti riistahallintolain (158/2011) 8 §:n 5 momentin nojalla.",
                "Beslutet är undertecknat maskinellt med stöd av viltförvaltningslagens (158/2011) 8 § 5 mom."));
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

    private static PermitDecisionAuthority resolveSigner1(final PermitDecision decision) {
        return F.firstNonNull(decision.getDecisionMaker(), decision.getPresenter());
    }

    private static PermitDecisionAuthority resolveSigner2(final PermitDecision decision) {
        final PermitDecisionAuthority presenter = F.firstNonNull(decision.getPresenter(), decision.getDecisionMaker());
        final PermitDecisionAuthority decisionMaker = F.firstNonNull(decision.getDecisionMaker(),
                decision.getPresenter());
        if (presenter == null && decisionMaker == null) {
            return null;
        }
        // both should be non-null, but can be same objects
        if (Objects.requireNonNull(presenter).isSameAs(decisionMaker)) {
            return null;
        }
        return presenter;
    }

    private static void appendSignature(final StringBuilder sb, final PermitDecisionAuthority a) {
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
                "Till kännedom") +
                ":\n" +
                decision.getDelivery().stream()
                        .map(PermitDecisionDelivery::getName)
                        .filter(StringUtils::isNotBlank)
                        .map(String::trim)
                        .map(String::toUpperCase)
                        .distinct()
                        .collect(joining("\n"));
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generatePayment(final PermitDecision decision) {
        if (decision.getDecisionType() != PermitDecision.DecisionType.HARVEST_PERMIT) {
            return "";
        }

        if (decision.isPaymentAmountPositive()) {
            return String.format("%s %.2f EUR",
                    i18n(decision,
                            "Käsittelymaksu",
                            "Handläggningsavgift"),
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
                        sb.append(i18n(decision, "LIITE", "BILAGA"));
                        sb.append(" ");
                        sb.append(a.getOrderingNumber());
                        sb.append(": ");
                    }

                    if (a.getDescription() != null) {
                        sb.append(a.getDescription());
                    }

                    return sb.toString();
                })
                .collect(Collectors.joining("\n"));
    }

    private static final LocalisedString ADJUST_AREA_TEXT = new LocalisedString(
            "Suomen riistakeskus on tehnyt Oma riista -palvelussa tarkistuslaskennan" +
                    " Metsähallituksen alueista ja määrittänyt uudelleen hakemuksen valtionmaiden" +
                    " maapinta-alan ja yksityismaiden maapinta-alan." +
                    "\n\n" +
                    "Hakemuksen tarkistetut pinta-alat:" +
                    "\n\n" +
                    "Valtionmaiden maapinta-ala %d ha\n" +
                    "Yksityismaiden maapinta-ala %d ha",
            "Finlands viltcentral har i Oma riista -tjänsten gjort en kontrollräkning" +
                    " av Forststyrelsens områden och har på nytt i ansökan fastställt landarealen" +
                    " för statsägda marker och landarealen för privatägda marker." +
                    "\n\n" +
                    "I ansökan granskade arealer:" +
                    "\n\n" +
                    "Landareal statsägda marker %d ha\n" +
                    "Landareal privatägda marker %d ha");

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generateAdjustedAreaSizeText(final PermitDecision decision) {
        return Optional.ofNullable(decision.getApplication())
                .map(HarvestPermitApplication::getArea)
                .map(HarvestPermitArea::getZone)
                .map(HasID::getId)
                .map(gisZoneRepository::getAdjustedAreaSize)
                .map(size -> String.format(ADJUST_AREA_TEXT.getTranslation(decision.getLocale()),
                        NumberUtils.squareMetersToHectares(size.getStateLandAreaSize()),
                        NumberUtils.squareMetersToHectares(size.getPrivateLandAreaSize())))
                .orElse("");
    }
}

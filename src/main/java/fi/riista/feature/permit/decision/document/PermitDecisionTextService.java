package fi.riista.feature.permit.decision.document;

import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.common.decision.authority.DecisionRkaAuthorityDetails;
import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.application.PermitApplicationVehicleType;
import fi.riista.feature.permit.application.amendment.AmendmentApplicationData;
import fi.riista.feature.permit.application.amendment.AmendmentApplicationDataRepository;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachmentRepository;
import fi.riista.feature.permit.application.bird.BirdPermitApplication;
import fi.riista.feature.permit.application.bird.BirdPermitApplicationRepository;
import fi.riista.feature.permit.application.bird.BirdPermitApplicationSummaryDTO;
import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplication;
import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplicationRepository;
import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplicationSummaryDTO;
import fi.riista.feature.permit.application.deportation.DeportationPermitApplication;
import fi.riista.feature.permit.application.deportation.DeportationPermitApplicationRepository;
import fi.riista.feature.permit.application.deportation.DeportationSummaryDTO;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReasonService;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReasonsDTO;
import fi.riista.feature.permit.application.disability.DisabilityPermitApplication;
import fi.riista.feature.permit.application.disability.DisabilityPermitApplicationRepository;
import fi.riista.feature.permit.application.disability.justification.DisabilityPermitHuntingTypeInfo;
import fi.riista.feature.permit.application.disability.justification.DisabilityPermitHuntingTypeInfoRepository;
import fi.riista.feature.permit.application.disability.justification.DisabilityPermitVehicle;
import fi.riista.feature.permit.application.disability.justification.DisabilityPermitVehicleRepository;
import fi.riista.feature.permit.application.disability.justification.HuntingType;
import fi.riista.feature.permit.application.disability.summary.DisabilityPermitSummaryDTO;
import fi.riista.feature.permit.application.dogevent.DogEventApplication;
import fi.riista.feature.permit.application.dogevent.DogEventApplicationRepository;
import fi.riista.feature.permit.application.dogevent.DogEventDisturbance;
import fi.riista.feature.permit.application.dogevent.DogEventDisturbanceContactRepository;
import fi.riista.feature.permit.application.dogevent.DogEventDisturbanceRepository;
import fi.riista.feature.permit.application.dogevent.DogEventUnleash;
import fi.riista.feature.permit.application.dogevent.DogEventUnleashRepository;
import fi.riista.feature.permit.application.dogevent.disturbance.DogEventDisturbanceDTO;
import fi.riista.feature.permit.application.dogevent.summary.DogEventDisturbanceSummaryDTO;
import fi.riista.feature.permit.application.dogevent.summary.DogEventUnleashSummaryDTO;
import fi.riista.feature.permit.application.gamemanagement.GameManagementPermitApplication;
import fi.riista.feature.permit.application.gamemanagement.GameManagementPermitApplicationRepository;
import fi.riista.feature.permit.application.gamemanagement.summary.GameManagementSummaryDTO;
import fi.riista.feature.permit.application.importing.ImportingPermitApplication;
import fi.riista.feature.permit.application.importing.ImportingPermitApplicationRepository;
import fi.riista.feature.permit.application.importing.ImportingPermitApplicationSummaryDTO;
import fi.riista.feature.permit.application.lawsectionten.LawSectionTenPermitApplication;
import fi.riista.feature.permit.application.lawsectionten.LawSectionTenPermitApplicationRepository;
import fi.riista.feature.permit.application.lawsectionten.LawSectionTenPermitApplicationSummaryDTO;
import fi.riista.feature.permit.application.mammal.MammalPermitApplication;
import fi.riista.feature.permit.application.mammal.MammalPermitApplicationRepository;
import fi.riista.feature.permit.application.mammal.MammalPermitApplicationSummaryDTO;
import fi.riista.feature.permit.application.nestremoval.NestRemovalPermitApplication;
import fi.riista.feature.permit.application.nestremoval.NestRemovalPermitApplicationRepository;
import fi.riista.feature.permit.application.nestremoval.NestRemovalPermitApplicationSummaryDTO;
import fi.riista.feature.permit.application.research.ResearchPermitApplication;
import fi.riista.feature.permit.application.research.ResearchPermitApplicationRepository;
import fi.riista.feature.permit.application.research.ResearchSummaryDTO;
import fi.riista.feature.permit.application.weapontransportation.WeaponTransportationPermitApplication;
import fi.riista.feature.permit.application.weapontransportation.WeaponTransportationPermitApplicationRepository;
import fi.riista.feature.permit.application.weapontransportation.justification.TransportedWeapon;
import fi.riista.feature.permit.application.weapontransportation.justification.TransportedWeaponRepository;
import fi.riista.feature.permit.application.weapontransportation.justification.TransportedWeaponType;
import fi.riista.feature.permit.application.weapontransportation.justification.WeaponTransportationVehicle;
import fi.riista.feature.permit.application.weapontransportation.justification.WeaponTransportationVehicleRepository;
import fi.riista.feature.permit.application.weapontransportation.justification.WeaponTransportationVehicleType;
import fi.riista.feature.permit.application.weapontransportation.reason.WeaponTransportationReasonType;
import fi.riista.feature.permit.application.weapontransportation.summary.SummaryDTO;
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
import java.util.Optional;
import java.util.stream.Collectors;

import static fi.riista.feature.common.decision.GrantStatus.UNCHANGED;
import static fi.riista.feature.permit.PermitTypeCode.FORBIDDEN_METHODS;
import static fi.riista.feature.permit.PermitTypeCode.GAME_MANAGEMENT;
import static fi.riista.feature.permit.PermitTypeCode.IMPORTING;
import static fi.riista.feature.permit.PermitTypeCode.NEST_REMOVAL_BASED;
import static fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment.Type.OTHER;
import static fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment.Type.PROTECTED_AREA;
import static fi.riista.feature.permit.application.dogevent.DogEventType.DOG_TEST;
import static fi.riista.feature.permit.application.dogevent.DogEventType.DOG_TRAINING;
import static fi.riista.feature.permit.decision.derogation.DerogationLawSection.SECTION_41A;
import static fi.riista.feature.permit.decision.derogation.DerogationLawSection.SECTION_41B;
import static fi.riista.feature.permit.decision.derogation.DerogationLawSection.SECTION_41C;
import static fi.riista.feature.permit.decision.document.PermitDecisionTextUtils.escape;
import static fi.riista.util.DateUtil.DATE_FORMAT_FINNISH;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@Service
public class PermitDecisionTextService {

    private static final Comparator<PermitDecisionSpeciesAmount> COMPARATOR_AMOUNTS_DESC =
            comparing(PermitDecisionSpeciesAmount::getSpecimenAmount).reversed();

    private static final Comparator<PermitDecisionSpeciesAmount> COMPARATOR_CARNIVORE =
            comparing(PermitDecisionSpeciesAmount::getSpecimenAmount);

    private static final Comparator<PermitDecisionSpeciesAmount> COMPARATOR_NEST_REMOVAL =
            comparing(PermitDecisionSpeciesAmount::getNestAmount, nullsLast(naturalOrder()))
                    .thenComparing(PermitDecisionSpeciesAmount::getEggAmount, nullsLast(naturalOrder()))
                    .thenComparing(PermitDecisionSpeciesAmount::getConstructionAmount, nullsLast(naturalOrder()));

    private static final Comparator<PermitDecisionSpeciesAmount> COMPARATOR_SPECIES_YEAR =
            comparing((PermitDecisionSpeciesAmount a) -> a.getGameSpecies().getOfficialCode())
                    .thenComparing((PermitDecisionSpeciesAmount a) -> a.getBeginDate().getYear());

    private static final Comparator<PermitDecisionSpeciesAmount> COMPARATOR_DOES_NOTHING =
            comparing((p) -> 0);

    private static final DateTimeFormatter DF = DateTimeFormat.forPattern(DATE_FORMAT_FINNISH);

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
    private NestRemovalPermitApplicationRepository nestRemovalPermitApplicationRepository;

    @Resource
    private LawSectionTenPermitApplicationRepository lawSectionTenPermitApplicationRepository;

    @Resource
    private WeaponTransportationPermitApplicationRepository weaponTransportationPermitApplicationRepository;

    @Resource
    private DogEventApplicationRepository dogEventApplicationRepository;

    @Resource
    private DogEventDisturbanceRepository dogEventDisturbanceRepository;

    @Resource
    private DogEventDisturbanceContactRepository dogEventDisturbanceContactRepository;

    @Resource
    private DogEventUnleashRepository dogEventUnleashRepository;

    @Resource
    private TransportedWeaponRepository transportedWeaponRepository;

    @Resource
    private WeaponTransportationVehicleRepository weaponTransportationVehicleRepository;

    @Resource
    private DisabilityPermitApplicationRepository disabilityPermitApplicationRepository;

    @Resource
    private DisabilityPermitVehicleRepository disabilityPermitVehicleRepository;

    @Resource
    private DisabilityPermitHuntingTypeInfoRepository disabilityPermitHuntingTypeInfoRepository;

    @Resource
    private ImportingPermitApplicationRepository importingPermitApplicationRepository;

    @Resource
    private DerogationPermitApplicationReasonService derogationPermitApplicationReasonService;

    @Resource
    private PermitDecisionSpeciesAmountRepository permitDecisionSpeciesAmountRepository;

    @Resource
    private PermitDecisionDerogationReasonRepository permitDecisionDerogationReasonRepository;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private HarvestPermitApplicationAttachmentRepository applicationAttachmentRepository;

    @Resource
    private DeportationPermitApplicationRepository deportationPermitApplicationRepository;

    @Resource
    private ResearchPermitApplicationRepository researchPermitApplicationRepository;

    @Resource
    private GameManagementPermitApplicationRepository gameManagementPermitApplicationRepository;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

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
        if (StringUtils.isBlank(doc.getDecisionExtra())) {
            doc.setDecisionExtra(generateDecisionExtra(decision));
        }
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
        final HarvestPermitApplication application = requireNonNull(decision.getApplication());
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
            case LARGE_CARNIVORE_WOLF_PORONHOITO:
                return createCarnivoreApplicationSummaryGenerator(application, locale).generateApplicationMain();
            case MAMMAL:
                return createMammalApplicationSummaryGenerator(application, locale).generateApplicationMain();
            case NEST_REMOVAL:
                return createNestRemovalApplicationSummaryGenerator(application, locale).generateApplicationMain();
            case LAW_SECTION_TEN:
                return createLawSectionTenApplicationSummaryGenerator(application, locale).generateApplicationMain();
            case WEAPON_TRANSPORTATION:
                return createWeaponTransportationApplicationSummaryGenerator(application, locale).generateApplicationMain();
            case DISABILITY:
                return createDisabilityApplicationSummaryGenerator(application, locale).generateApplicationMain();
            case DOG_DISTURBANCE:
                return createDogDisturbanceApplicationSummaryGenerator(application, locale).generateApplicationMain();
            case DOG_UNLEASH:
                return createDogUnleashApplicationSummaryGenerator(application, locale).generateApplicationMain();
            case DEPORTATION:
                return createDeportationApplicationSummaryGenerator(application, locale).generateApplicationMain();
            case RESEARCH:
                return createResearchApplicationSummaryGenerator(application, locale).generateApplicationMain();
            case IMPORTING:
                return createImportingApplicationSummaryGenerator(application, locale).generateApplicationMain();
            case GAME_MANAGEMENT:
                return createGameManagementApplicationSummaryGenerator(application, locale).generateApplicationMain();
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

    @Nonnull
    private PermitDecisionNestRemovalApplicationSummaryGenerator createNestRemovalApplicationSummaryGenerator(
            final HarvestPermitApplication application, final Locale locale) {

        final NestRemovalPermitApplication nestRemovalPermitApplication =
                nestRemovalPermitApplicationRepository.findByHarvestPermitApplication(application);
        final DerogationPermitApplicationReasonsDTO derogationReasons =
                derogationPermitApplicationReasonService.getDerogationReasons(application, locale);
        final NestRemovalPermitApplicationSummaryDTO dto =
                NestRemovalPermitApplicationSummaryDTO.create(application, nestRemovalPermitApplication, derogationReasons);
        return new PermitDecisionNestRemovalApplicationSummaryGenerator(dto, locale, createSpeciesNames(locale),
                messageSource);
    }

    @Nonnull
    private PermitDecisionLawSectionTenApplicationSummaryGenerator createLawSectionTenApplicationSummaryGenerator(
            final HarvestPermitApplication application, final Locale locale) {

        final LawSectionTenPermitApplication lawSectionTenPermitApplication =
                lawSectionTenPermitApplicationRepository.findByHarvestPermitApplication(application);
        final LawSectionTenPermitApplicationSummaryDTO dto =
                LawSectionTenPermitApplicationSummaryDTO.create(application, lawSectionTenPermitApplication);
        return new PermitDecisionLawSectionTenApplicationSummaryGenerator(dto, locale, createSpeciesNames(locale),
                messageSource);
    }

    @Nonnull
    private PermitDecisionWeaponTransportationApplicationSummaryGenerator createWeaponTransportationApplicationSummaryGenerator(
            final HarvestPermitApplication application, final Locale locale) {

        final WeaponTransportationPermitApplication transportApplication =
                weaponTransportationPermitApplicationRepository.findByHarvestPermitApplication(application);
        final List<TransportedWeapon> transportedWeapons =
                transportedWeaponRepository.findByWeaponTransportationPermitApplicationOrderById(transportApplication);
        final List<WeaponTransportationVehicle> vehicles =
                weaponTransportationVehicleRepository.findByWeaponTransportationPermitApplicationOrderById(transportApplication);
        final SummaryDTO dto =
                SummaryDTO.create(application, transportApplication, transportedWeapons, vehicles);
        return new PermitDecisionWeaponTransportationApplicationSummaryGenerator(dto, locale, messageSource);
    }

    @Nonnull
    private PermitDecisionDisabilityApplicationSummaryGenerator createDisabilityApplicationSummaryGenerator(
            final HarvestPermitApplication application, final Locale locale) {

        final DisabilityPermitApplication disabilityPermitApplication =
                disabilityPermitApplicationRepository.findByHarvestPermitApplication(application);
        final List<DisabilityPermitVehicle> vehicles =
                disabilityPermitVehicleRepository.findByDisabilityPermitApplicationOrderById(disabilityPermitApplication);
        final List<DisabilityPermitHuntingTypeInfo> huntingTypeInfos =
                disabilityPermitHuntingTypeInfoRepository.findByDisabilityPermitApplicationOrderById(disabilityPermitApplication);
        final List<HarvestPermitApplicationAttachment> attachments =
                applicationAttachmentRepository.findByHarvestPermitApplication(application);
        final DisabilityPermitSummaryDTO dto =
                DisabilityPermitSummaryDTO.create(application, disabilityPermitApplication, vehicles, huntingTypeInfos,
                        application.getContactPerson(), attachments);
        return new PermitDecisionDisabilityApplicationSummaryGenerator(dto, locale, messageSource);

    }

    @Nonnull
    private PermitDecisionDogDisturbanceApplicationSummaryGenerator createDogDisturbanceApplicationSummaryGenerator(
            final HarvestPermitApplication application, final Locale locale) {

        final DogEventApplication dogEventApplication = dogEventApplicationRepository.findByHarvestPermitApplication(application);
        final DogEventDisturbance trainingEvent =
                dogEventDisturbanceRepository.findByHarvestPermitApplicationAndEventType(application, DOG_TRAINING);
        final DogEventDisturbance testEvent =
                dogEventDisturbanceRepository.findByHarvestPermitApplicationAndEventType(application, DOG_TEST);

        final DogEventDisturbanceSummaryDTO dto = DogEventDisturbanceSummaryDTO.create(
                application,
                dogEventApplication,
                createDogEventDisturbanceDTO(trainingEvent),
                createDogEventDisturbanceDTO(testEvent));

        return new PermitDecisionDogDisturbanceApplicationSummaryGenerator(dto, locale, createSpeciesNames(locale),
                                                                           messageSource);
    }

    private DogEventDisturbanceDTO createDogEventDisturbanceDTO(final DogEventDisturbance event) {
        return DogEventDisturbanceDTO.createFrom(
                event,
                dogEventDisturbanceContactRepository.findAllByEvent(event),
                F.mapNullable(event.getGameSpecies(), GameSpecies::getOfficialCode));
    }


    @Nonnull
    private PermitDecisionDogUnleashApplicationSummaryGenerator createDogUnleashApplicationSummaryGenerator(
            final HarvestPermitApplication application, final Locale locale) {

        final DogEventApplication dogEventApplication = dogEventApplicationRepository.findByHarvestPermitApplication(application);
        final List<DogEventUnleash> dogEvents = dogEventUnleashRepository.findAllByHarvestPermitApplicationOrderByBeginDate(application);
        final DogEventUnleashSummaryDTO dto = DogEventUnleashSummaryDTO.create(application, dogEventApplication, dogEvents);

        return new PermitDecisionDogUnleashApplicationSummaryGenerator(dto, locale, messageSource);
    }

    @Nonnull
    private PermitDecisionDeportationApplicationSummaryGenerator createDeportationApplicationSummaryGenerator(
            final HarvestPermitApplication application, final Locale locale) {

        final DeportationPermitApplication deportationPermitApplication =
                deportationPermitApplicationRepository.findByHarvestPermitApplication(application);
        final DerogationPermitApplicationReasonsDTO derogationReasons =
                derogationPermitApplicationReasonService.getDerogationReasons(application, locale);
        final DeportationSummaryDTO dto =
                DeportationSummaryDTO.create(application, deportationPermitApplication, derogationReasons);
        return new PermitDecisionDeportationApplicationSummaryGenerator(dto, locale, createSpeciesNames(locale),
                messageSource);
    }

    @Nonnull
    private PermitDecisionResearchApplicationSummaryGenerator createResearchApplicationSummaryGenerator(
            final HarvestPermitApplication application, final Locale locale) {

        final ResearchPermitApplication researchPermitApplication =
                researchPermitApplicationRepository.findByHarvestPermitApplication(application);
        final DerogationPermitApplicationReasonsDTO derogationReasons =
                derogationPermitApplicationReasonService.getDerogationReasons(application, locale);
        final ResearchSummaryDTO dto =
                ResearchSummaryDTO.create(application, researchPermitApplication, derogationReasons);
        return new PermitDecisionResearchApplicationSummaryGenerator(dto, locale, createSpeciesNames(locale),
                messageSource);
    }

    @Nonnull
    private PermitDecisionImportingApplicationSummaryGenerator createImportingApplicationSummaryGenerator(
            final HarvestPermitApplication application, final Locale locale) {

        final ImportingPermitApplication importingPermitApplication =
                importingPermitApplicationRepository.findByHarvestPermitApplication(application);
        final List<HarvestPermitApplicationAttachment> attachments = application.getAttachments();
        final List<HarvestPermitApplicationAttachment> areaAttachments =
                F.filterToList(attachments, a -> a.getAttachmentType() == PROTECTED_AREA);
        final List<HarvestPermitApplicationAttachment> otherAttachments =
                F.filterToList(attachments, a -> a.getAttachmentType() == OTHER);
        final ImportingPermitApplicationSummaryDTO dto =
                ImportingPermitApplicationSummaryDTO.from(
                        application, importingPermitApplication, application.getSpeciesAmounts(), areaAttachments, otherAttachments);
        return new PermitDecisionImportingApplicationSummaryGenerator(dto, locale, createSpeciesNames(locale),
                messageSource);
    }

    @Nonnull
    private PermitDecisionGameManagementApplicationSummaryGenerator createGameManagementApplicationSummaryGenerator(
            final HarvestPermitApplication application, final Locale locale) {

        final GameManagementPermitApplication gameManagementPermitApplication =
                gameManagementPermitApplicationRepository.findByHarvestPermitApplication(application);
        final List<HarvestPermitApplicationSpeciesAmount> speciesAmounts =
                harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application);
        final GameManagementSummaryDTO dto =
                GameManagementSummaryDTO.create(application, gameManagementPermitApplication, speciesAmounts);
        return new PermitDecisionGameManagementApplicationSummaryGenerator(dto, locale, createSpeciesNames(locale),
                messageSource);
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generateApplicationReasoning(final PermitDecision decision) {
        final HarvestPermitApplication application = requireNonNull(decision.getApplication());
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
            case LARGE_CARNIVORE_WOLF_PORONHOITO:
                return createCarnivoreApplicationSummaryGenerator(application, locale).generateApplicationReasoning();
            case MAMMAL:
                return createMammalApplicationSummaryGenerator(application, locale).generateApplicationReasoning();
            case NEST_REMOVAL:
                return createNestRemovalApplicationSummaryGenerator(application, locale).generateApplicationReasoning();
            case LAW_SECTION_TEN:
                return createLawSectionTenApplicationSummaryGenerator(application, locale).generateApplicationReasoning();
            case WEAPON_TRANSPORTATION:
                return createWeaponTransportationApplicationSummaryGenerator(application, locale).generateApplicationReasoning();
            case DISABILITY:
                return createDisabilityApplicationSummaryGenerator(application, locale).generateApplicationReasoning();
            case DOG_DISTURBANCE:
                return createDogDisturbanceApplicationSummaryGenerator(application, locale).generateApplicationReasoning();
            case DOG_UNLEASH:
                return createDogUnleashApplicationSummaryGenerator(application, locale).generateApplicationReasoning();
            case DEPORTATION:
                return createDeportationApplicationSummaryGenerator(application, locale).generateApplicationReasoning();
            case RESEARCH:
                return createResearchApplicationSummaryGenerator(application, locale).generateApplicationReasoning();
            case IMPORTING:
                return createImportingApplicationSummaryGenerator(application, locale).generateApplicationReasoning();
            case GAME_MANAGEMENT:
                return createGameManagementApplicationSummaryGenerator(application, locale).generateApplicationReasoning();
            default:
                return "";
        }
    }

    private static String generateApplicationReasoningForMooselike(final HarvestPermitApplication application,
                                                                   final Locale locale) {
        final StringBuilder sb = new StringBuilder();

        for (final HarvestPermitApplicationSpeciesAmount speciesAmount : application.getSpeciesAmounts()) {
            if (StringUtils.isNotBlank(speciesAmount.getMooselikeDescription())) {
                sb.append(formatSpeciesName(speciesAmount, locale));
                sb.append(": ");
                sb.append(escape(speciesAmount.getMooselikeDescription()));
                sb.append("\n\n");
            }
        }

        return sb.toString().trim();
    }

    private List<PermitDecisionSpeciesAmount> getSortedSpecies(final PermitDecision decision) {
        return permitDecisionSpeciesAmountRepository.findByPermitDecision(decision).stream()
                .sorted(getSpeciesComparator(decision.getApplication().getHarvestPermitCategory()))
                .collect(toList());
    }

    private static Comparator<PermitDecisionSpeciesAmount> getSpeciesComparator(final HarvestPermitCategory category) {
        switch (category) {
            case MOOSELIKE:
            case MOOSELIKE_NEW:
            case LAW_SECTION_TEN:
                return COMPARATOR_AMOUNTS_DESC;
            case MAMMAL:
            case BIRD:
            case DEPORTATION:
            case RESEARCH:
            case IMPORTING:
            case GAME_MANAGEMENT:
                return COMPARATOR_SPECIES_YEAR;
            case LARGE_CARNIVORE_BEAR:
            case LARGE_CARNIVORE_LYNX:
            case LARGE_CARNIVORE_LYNX_PORONHOITO:
            case LARGE_CARNIVORE_WOLF:
            case LARGE_CARNIVORE_WOLF_PORONHOITO:
                return COMPARATOR_CARNIVORE;
            case NEST_REMOVAL:
                return COMPARATOR_NEST_REMOVAL;
            default:
                return COMPARATOR_DOES_NOTHING;
        }
    }

    private static String i18n(final PermitDecision decision, final String finnish, final String swedish) {
        return Locales.isSwedish(decision.getLocale()) ? swedish : finnish;
    }

    private String i18nKey(final PermitDecision decision, final String key) {
        return messageSource.getMessage(key, null, decision.getLocale());
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
        } else if (decision.getDecisionType() == PermitDecision.DecisionType.CANCEL_ANNUAL_RENEWAL) {
            return i18n(decision, "Ilmoitusmenettely perutaan", "Anmälningsförfarandet annulleras");
        }

        if (PermitTypeCode.hasSpecies(decision.getPermitTypeCode())) {
            return generateHarvestDecision(decision);
        } else {
            return generateDecisionWithoutSpecies(decision);
        }
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public String generateDecisionExtra(final PermitDecision decision) {
        final HarvestPermitCategory category = decision.getApplication().getHarvestPermitCategory();

        switch (category) {
            case WEAPON_TRANSPORTATION:
                return generateWeaponTransportationExtra(decision);
            case DISABILITY:
                return generateDisabilityExtra(decision);
            default:
                return "";
        }
    }

    private String generateWeaponTransportationExtra(final PermitDecision decision) {
        final EnumLocaliser localiser = new EnumLocaliser(messageSource, decision.getLocale());

        final WeaponTransportationPermitApplication transportApplication =
                weaponTransportationPermitApplicationRepository.findByHarvestPermitApplication(decision.getApplication());

        final StringBuilder sb = new StringBuilder();

        final WeaponTransportationReasonType reasonType = transportApplication.getReasonType();
        sb.append(i18n(decision, "Perustelut:", "Motiveringar:")).append("\n");
        sb.append(localiser.getTranslation(reasonType));
        if (reasonType == WeaponTransportationReasonType.MUU) {
            sb.append(" - ").append(escape(transportApplication.getReasonDescription()));
        }
        sb.append(": ");

        sb.append(DF.print(transportApplication.getBeginDate()))
                .append(" - ")
                .append(DF.print(transportApplication.getEndDate()));

        sb.append("\n\n");

        final List<WeaponTransportationVehicle> vehicles =
                weaponTransportationVehicleRepository.findByWeaponTransportationPermitApplicationOrderById(transportApplication);
        if (!vehicles.isEmpty()) {
            sb.append(i18n(decision, "Ajoneuvotiedot:", "Fordonsinformation:")).append("\n");

            vehicles.forEach(vehicle -> {
                final WeaponTransportationVehicleType type = vehicle.getType();
                sb.append("- ").append(localiser.getTranslation(type));
                if (type == WeaponTransportationVehicleType.MUU) {
                    sb.append(" - ").append(escape(vehicle.getDescription()));
                }
                sb.append("\n");
            });

            sb.append("\n");
        }

        final List<TransportedWeapon> transportedWeapons =
                transportedWeaponRepository.findByWeaponTransportationPermitApplicationOrderById(transportApplication);
        if (!transportedWeapons.isEmpty()) {
            sb.append(i18n(decision, "Asetiedot:", "Vapeninformation:")).append("\n");

            transportedWeapons.forEach(transportedWeapon -> {
                final TransportedWeaponType weaponType = transportedWeapon.getType();
                sb.append("- ").append(localiser.getTranslation(weaponType));
                if (weaponType == TransportedWeaponType.MUU) {
                    sb.append(" - ").append(escape(transportedWeapon.getDescription()));
                }
                sb.append("\n");
            });
        }

        return sb.toString();
    }

    private String generateDisabilityExtra(final PermitDecision decision) {
        final EnumLocaliser localiser = new EnumLocaliser(messageSource, decision.getLocale());

        final DisabilityPermitApplication disabilityApplication =
                disabilityPermitApplicationRepository.findByHarvestPermitApplication(decision.getApplication());

        final StringBuilder sb = new StringBuilder();

        if (disabilityApplication.getUseMotorVehicle()) {
            sb.append("- ").append(i18nKey(decision, "disability.application.useMotorVehicle")).append("\n");
        }
        if (disabilityApplication.getUseVehicleForWeaponTransport()) {
            sb.append("- ").append(i18nKey(decision, "disability.application.useVehicleForWeaponTransport")).append("\n");
        }
        sb.append("\n");

        sb.append(i18n(decision, "Aika: ", "Tid: "))
                .append(DF.print(disabilityApplication.getBeginDate()))
                .append(" - ")
                .append(DF.print(disabilityApplication.getEndDate()))
                .append("\n\n");

        final List<DisabilityPermitVehicle> vehicles =
                disabilityPermitVehicleRepository.findByDisabilityPermitApplicationOrderById(disabilityApplication);
        if (!vehicles.isEmpty()) {
            sb.append(i18n(decision, "Ajoneuvotiedot:", "Fordonsinformation:")).append("\n");

            vehicles.forEach(vehicle -> {
                final PermitApplicationVehicleType type = vehicle.getType();
                sb.append("- ").append(localiser.getTranslation(type));
                if (type == PermitApplicationVehicleType.MUU) {
                    sb.append(" - ").append(escape(vehicle.getDescription()));
                }
                sb.append("\n");
            });

            sb.append("\n");
        }

        final List<DisabilityPermitHuntingTypeInfo> huntingTypeInfos =
                disabilityPermitHuntingTypeInfoRepository.findByDisabilityPermitApplicationOrderById(disabilityApplication);
        if (!huntingTypeInfos.isEmpty()) {
            sb.append(i18n(decision, "Metsästysmuodot:", "Jaktformer:")).append("\n");

            huntingTypeInfos.forEach(huntingTypeInfo -> {
                final HuntingType huntingType = huntingTypeInfo.getHuntingType();
                sb.append("- ").append(localiser.getTranslation(huntingType));
                if (huntingType == HuntingType.MUU) {
                    sb.append(" - ").append(escape(huntingTypeInfo.getHuntingTypeDescription()));
                }
                sb.append("\n");
            });
        }

        return sb.toString();
    }

    private String generateDecisionWithoutSpecies(final PermitDecision decision) {
        if (decision.getGrantStatus() != UNCHANGED) {
            return i18n(decision,
                    "Suomen riistakeskus on päättänyt hylätä hakemuksen.",
                    "Finlands viltcentral har beslutat att avslå ansökan.");
        }

        return generateDecisionSectionTitle(decision);
    }

    private String generateHarvestDecision(final PermitDecision decision) {
        final List<PermitDecisionSpeciesAmount> speciesAmounts =
                permitDecisionSpeciesAmountRepository.findByPermitDecision(decision);
        final boolean allAmountsZero =
                speciesAmounts.stream().noneMatch(PermitDecisionSpeciesAmount::hasGrantedSpecies);

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

        if (decision.getPermitTypeCode().equals(NEST_REMOVAL_BASED)) {
            sb.append(i18n(decision, "Metsästyslain 41 d§.", "Jaktlagen 41 d §."));
            sb.append("\n");
        }

        if (decision.getPermitTypeCode().equals(FORBIDDEN_METHODS)) {
            sb.append("\n");
            sb.append(i18n(decision, "Metsästyslain 41 §:n 3 momentti.", "Jaktlagen 41 § 3 moment."));
            sb.append("\n");
        }

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
                    "Metsästyslain 41 c §:n 4 kohta: näiden lajien tutkimus-, koulutus-, uudelleensijoittamis- ja " +
                            "istuttamistarkoituksessa taikka eläintautien ehkäisemiseksi",
                    "Jaktlagen 41 c § 4 punkten: i forsknings-, utbildnings-, omplacerings- och utplanteringssyfte " +
                            "eller för att förebygga djursjukdomar när det gäller arterna i fråga"));
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
        switch (decision.getPermitTypeCode()) {
            case NEST_REMOVAL_BASED:
                return generateGrantedNestRemovalAmounts(decision, speciesAmounts);
            case IMPORTING:
            case GAME_MANAGEMENT:
                return generateGrantedAmounts(decision, speciesAmounts);
            default:
                return generateGrantedHarvestAmounts(decision, speciesAmounts);
        }
    }

    private static String generateGrantedHarvestAmounts(final PermitDecision decision, final List<PermitDecisionSpeciesAmount> speciesAmounts) {
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
            sb.append(NF.format(speciesAmount.getSpecimenAmount()));
            sb.append(" ");
            sb.append(i18n(decision, "kpl", "st."));

            sb.append("|");

            if (speciesAmount.hasGrantedSpecies()) {
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

    private static String generateGrantedNestRemovalAmounts(final PermitDecision decision, final List<PermitDecisionSpeciesAmount> speciesAmounts) {
        final DecimalFormat NF = new DecimalFormat("#.#", new DecimalFormatSymbols(Locales.FI));
        final long validityYears = speciesAmounts.stream()
                .mapToInt(spa -> spa.getBeginDate().getYear())
                .distinct().count();

        final StringBuilder sb = new StringBuilder();
        sb.append("---|---|---\n");

        for (final PermitDecisionSpeciesAmount speciesAmount : speciesAmounts) {
            sb.append(formatSpeciesName(speciesAmount, decision.getLocale()));

            sb.append("|||");

            if (speciesAmount.hasGrantedSpecies()) {
                if (speciesAmount.getBeginDate() != null && speciesAmount.getEndDate() != null) {
                    sb.append(DF.print(speciesAmount.getBeginDate()));
                    sb.append(" - ");
                    sb.append(DF.print(speciesAmount.getEndDate()));
                }

                if (speciesAmount.getBeginDate2() != null && speciesAmount.getEndDate2() != null) {
                    sb.append(",\n");

                    sb.append("||||");

                    sb.append(DF.print(speciesAmount.getBeginDate2()));
                    sb.append(" - ");
                    sb.append(DF.print(speciesAmount.getEndDate2()));
                }
            }

            sb.append("\n");
            sb.append("||");
            if (speciesAmount.getNestAmount() != null) {
                sb.append(NF.format(speciesAmount.getNestAmount()));
                sb.append(" ");
                sb.append(i18n(decision, "pesää", "bo"));
            }
            sb.append("|");
            if (speciesAmount.getConstructionAmount() != null) {
                sb.append(NF.format(speciesAmount.getConstructionAmount()));
                sb.append(" ");
                sb.append(i18n(decision, "rakennelmaa", "konstruktion"));
            }
            sb.append("|");
            if (speciesAmount.getEggAmount() != null) {
                sb.append(NF.format(speciesAmount.getEggAmount()));
                sb.append(" ");
                sb.append(i18n(decision, "munaa", "ägg"));
            }


            sb.append("\n");
        }

        return sb.toString();
    }

    private static String generateGrantedAmounts(final PermitDecision decision, final List<PermitDecisionSpeciesAmount> speciesAmounts) {
        final DecimalFormat NF = new DecimalFormat("#.#", new DecimalFormatSymbols(Locales.FI));
        final long validityYears = speciesAmounts.stream()
                .mapToInt(spa -> spa.getBeginDate().getYear())
                .distinct().count();

        final StringBuilder sb = new StringBuilder();

        // Table with four columns
        sb.append("---|---|---|---\n");

        for (final PermitDecisionSpeciesAmount speciesAmount : speciesAmounts) {

            // First column: species
            sb.append(formatSpeciesName(speciesAmount, decision.getLocale()));

            if (validityYears > 1) {
                sb.append(String.format(" (%d)", speciesAmount.getBeginDate().getYear()));
            }

            // Second column: number of specimen
            sb.append("|");
            if (speciesAmount.getSpecimenAmount() != null) {
                sb.append(NF.format(speciesAmount.getSpecimenAmount()))
                        .append(" ")
                        .append(i18n(decision, "yksilöä", "individer"));
            }

            // Third column: number of eggs
            sb.append("|");
            if (speciesAmount.getEggAmount() != null){
                    sb.append(NF.format(speciesAmount.getEggAmount()))
                    .append(" ")
                    .append(i18n(decision, "munaa", "ägg"));
            }

            // Fourth column: time period
            sb.append("|");
            if (speciesAmount.hasGrantedSpecies()) {
                if (speciesAmount.getBeginDate() != null && speciesAmount.getEndDate() != null) {
                    sb.append(DF.print(speciesAmount.getBeginDate()));
                    sb.append(" - ");
                    sb.append(DF.print(speciesAmount.getEndDate()));
                }

                // Append second time period onto separate row when present
                if (speciesAmount.getBeginDate2() != null && speciesAmount.getEndDate2() != null) {
                    sb.append(",\n");
                    sb.append("||||");
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
            case LARGE_CARNIVORE_WOLF_PORONHOITO:
            case MAMMAL:
            case NEST_REMOVAL:
            case DEPORTATION:
            case RESEARCH:
            case GAME_MANAGEMENT:
                return i18n(decision,
                        "Suomen riistakeskus on päättänyt myöntää poikkeusluvan seuraavasti:",
                        "Finlands viltcentral har beslutat att bevilja dispens enligt följande:");
            case LAW_SECTION_TEN:
                return i18n(decision,
                        "Suomen riistakeskus on päättänyt myöntää pyyntiluvan seuraavasti:",
                        "Finlands viltcentral har beslutat bevilja jaktlicens enligt följande:");
            case WEAPON_TRANSPORTATION:
                return i18n(decision,
                        "Suomen riistakeskus on päättänyt myöntää aseenkuljetusluvan seuraavasti:",
                        "Finlands viltcentral har beslutat att bevilja tillstånd för transport av vapen enligt följande:");
            case DISABILITY:
                return i18n(decision,
                        "Suomen riistakeskus on päättänyt myöntää luvan moottoriajoneuvon käyttöön liikuntarajoitteisena seuraavasti:",
                        "Suomen riistakeskus on päättänyt myöntää luvan moottoriajoneuvon käyttöön liikuntarajoitteisena seuraavasti:");
            case DOG_DISTURBANCE:
            case DOG_UNLEASH:
                return i18n(decision,
                        "Suomen riistakeskus on päättänyt myöntää luvan seuraavasti:",
                        "Finlands viltcentral har beslutat att bevilja dispens enligt följande:");

            case IMPORTING: {
                return i18n(decision,
                        "Suomen riistakeskus on päättänyt myöntää luvan maahantuontiin seuraavasti:",
                        "Finlands viltcentral har beslutat att bevilja tillstånd för import enligt följande:");
            }
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
                                Math.round(speciesAmount.getSpecimenAmount()) > 1 ? "tjurar" : "tjur"));
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

        final PermitDecisionAuthority authority = F.firstNonNull(decision.getPresenter(), decision.getDecisionMaker());
        final StringBuilder sb = new StringBuilder();
        sb.append(i18n(decision,
                "Lisätietoja päätöksestä antaa",
                "Tilläggsuppgifter om beslutet ges av"));
        sb.append(":");

        if (authority != null) {
            final DecisionRkaAuthorityDetails presenter = authority.getAuthorityDetails();
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
        final PermitDecisionAuthority presenter =
                F.firstNonNull(decision.getPresenter(), decision.getDecisionMaker());
        final PermitDecisionAuthority decisionMaker =
                F.firstNonNull(decision.getDecisionMaker(), decision.getPresenter());
        if (presenter == null && decisionMaker == null) {
            return null;
        }
        // both should be non-null, but can be same objects
        if (requireNonNull(presenter).isEqualTo(decisionMaker)) {
            return null;
        }
        return presenter;
    }

    private static void appendSignature(final StringBuilder sb, final PermitDecisionAuthority a) {
        final DecisionRkaAuthorityDetails details = F.mapNullable(a, PermitDecisionAuthority::getAuthorityDetails);
        if (a != null) {
            sb.append("<br><br>\n\n");
            sb.append(details.getFirstName());
            sb.append(' ');
            sb.append(details.getLastName());
            sb.append("<br>");
            sb.append(details.getTitle());
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
                        sb.append(escape(a.getDescription()));
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

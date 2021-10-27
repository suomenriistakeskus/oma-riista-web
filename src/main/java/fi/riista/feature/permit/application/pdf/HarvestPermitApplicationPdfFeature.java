package fi.riista.feature.permit.application.pdf;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
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
import fi.riista.feature.permit.application.mooselike.MooselikePermitApplicationSpeciesAmountDTO;
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
import fi.riista.feature.permit.application.weapontransportation.justification.WeaponTransportationVehicle;
import fi.riista.feature.permit.application.weapontransportation.justification.WeaponTransportationVehicleRepository;
import fi.riista.feature.permit.application.weapontransportation.summary.SummaryDTO;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.hta.HarvestPermitAreaHtaDTO;
import fi.riista.feature.permit.area.partner.HarvestPermitAreaPartnerDTO;
import fi.riista.feature.permit.area.partner.HarvestPermitAreaPartnerService;
import fi.riista.feature.permit.area.rhy.HarvestPermitAreaRhyDTO;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static fi.riista.feature.permit.application.HarvestPermitApplication.Status.ACTIVE;
import static fi.riista.feature.permit.application.HarvestPermitApplication.Status.AMENDING;
import static fi.riista.feature.permit.application.HarvestPermitApplication.Status.DRAFT;
import static fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment.Type.OTHER;
import static fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment.Type.PROTECTED_AREA;
import static fi.riista.feature.permit.application.dogevent.DogEventType.DOG_TEST;
import static fi.riista.feature.permit.application.dogevent.DogEventType.DOG_TRAINING;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@Component
public class HarvestPermitApplicationPdfFeature {

    private static final String JSP_MOOSELIKE = "pdf/application-mooselike";
    private static final String JSP_MOOSELIKE_AMENDMENT = "pdf/application-mooselike-amendment";
    private static final String JSP_BIRD = "pdf/application-bird";
    private static final String JSP_CARNIVORE = "pdf/application-carnivore";
    private static final String JSP_MAMMAL = "pdf/application-mammal";
    private static final String JSP_NEST_REMOVAL = "pdf/application-nest-removal";
    private static final String JSP_LAW_SECTION_TEN = "pdf/application-law-section-ten";
    private static final String JSP_WEAPON_TRANSPORTATION = "pdf/application-weapon-transportation";
    private static final String JSP_DISABILITY = "pdf/application-disability";
    private static final String JSP_DOG_EVENT_UNLEASH = "pdf/application-dog-event-unleash";
    private static final String JSP_DOG_EVENT_DISTURBANCE = "pdf/application-dog-event-disturbance";
    private static final String JSP_DEPORTATION = "pdf/application-deportation";
    private static final String JSP_RESEARCH = "pdf/application-research";
    private static final String JSP_IMPORTING = "pdf/application-importing";
    private static final String JSP_GAME_MANAGEMENT = "pdf/application-game-management";

    public static class PdfModel {
        private final String view;
        private final Object model;
        private final Map<Integer, String> speciesNames;

        PdfModel(final String view, final Object model, final Map<Integer, String> speciesNames) {
            this.view = requireNonNull(view);
            this.model = requireNonNull(model);
            this.speciesNames = requireNonNull(speciesNames);
        }

        public String getView() {
            return view;
        }

        public Object getModel() {
            return model;
        }

        public Map<Integer, String> getSpeciesNames() {
            return speciesNames;
        }
    }

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private AmendmentApplicationDataRepository amendmentApplicationDataRepository;

    @Resource
    private BirdPermitApplicationRepository birdPermitApplicationRepository;

    @Resource
    private CarnivorePermitApplicationRepository carnivorePermitApplicationRepository;

    @Resource
    MammalPermitApplicationRepository mammalPermitApplicationRepository;

    @Resource
    NestRemovalPermitApplicationRepository nestRemovalPermitApplicationRepository;

    @Resource
    private LawSectionTenPermitApplicationRepository lawSectionTenPermitApplicationRepository;

    @Resource
    private WeaponTransportationPermitApplicationRepository weaponTransportationPermitApplicationRepository;

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
    private HarvestPermitApplicationAttachmentRepository applicationAttachmentRepository;

    @Resource
    private DogEventApplicationRepository dogEventApplicationRepository;

    @Resource
    private DogEventUnleashRepository dogEventUnleashRepository;

    @Resource
    private DogEventDisturbanceRepository dogEventDisturbanceRepository;

    @Resource
    private DogEventDisturbanceContactRepository dogEventDisturbanceContactRepository;

    @Resource
    private DerogationPermitApplicationReasonService derogationPermitApplicationReasonService;

    @Resource
    private DeportationPermitApplicationRepository deportationPermitApplicationRepository;

    @Resource
    private ResearchPermitApplicationRepository researchPermitApplicationRepository;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Resource
    private MessageSource messageSource;

    @Resource
    private HarvestPermitAreaPartnerService harvestPermitAreaPartnerService;

    @Resource
    private GameManagementPermitApplicationRepository gameManagementPermitApplicationRepository;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Transactional(readOnly = true)
    public PdfModel getPdfModel(final long applicationId, final Locale locale) {
        final HarvestPermitApplication application = readApplication(applicationId);
        final Map<Integer, String> speciesNameIndex = gameSpeciesService.getNameIndex().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getTranslation(locale)));

        switch (application.getHarvestPermitCategory()) {
            case MOOSELIKE:
                return new PdfModel(JSP_MOOSELIKE, createMooselikeModel(application, locale), speciesNameIndex);

            case MOOSELIKE_NEW:
                final AmendmentApplicationData data = amendmentApplicationDataRepository.getByApplication(application);
                return new PdfModel(JSP_MOOSELIKE_AMENDMENT, new AmendmentPermitApplicationPdfDTO(application, data), speciesNameIndex);

            case BIRD:
                final BirdPermitApplication birdApplication = birdPermitApplicationRepository.findByHarvestPermitApplication(application);
                return new PdfModel(JSP_BIRD, BirdPermitApplicationSummaryDTO.create(application, birdApplication), speciesNameIndex);
            case LARGE_CARNIVORE_BEAR:
            case LARGE_CARNIVORE_LYNX:
            case LARGE_CARNIVORE_LYNX_PORONHOITO:
            case LARGE_CARNIVORE_WOLF:
            case LARGE_CARNIVORE_WOLF_PORONHOITO:
                final CarnivorePermitApplication carnivorePermitApplication = carnivorePermitApplicationRepository.findByHarvestPermitApplication(application);
                return new PdfModel(JSP_CARNIVORE, CarnivorePermitApplicationSummaryDTO.from(application, carnivorePermitApplication), speciesNameIndex);
            case MAMMAL: {
                final MammalPermitApplication mammalPermitApplication =
                        mammalPermitApplicationRepository.findByHarvestPermitApplication(application);
                final DerogationPermitApplicationReasonsDTO reasonsDTO =
                        derogationPermitApplicationReasonService.getDerogationReasons(application, locale);
                return new PdfModel(JSP_MAMMAL, MammalPermitApplicationSummaryDTO.create(application,
                        mammalPermitApplication, reasonsDTO), speciesNameIndex);
            }
            case NEST_REMOVAL: {
                final NestRemovalPermitApplication nestRemovalPermitApplication =
                        nestRemovalPermitApplicationRepository.findByHarvestPermitApplication(application);
                final DerogationPermitApplicationReasonsDTO reasonsDTO =
                        derogationPermitApplicationReasonService.getDerogationReasons(application, locale);
                return new PdfModel(JSP_NEST_REMOVAL, NestRemovalPermitApplicationSummaryDTO.create(application,
                        nestRemovalPermitApplication, reasonsDTO), speciesNameIndex);
            }
            case LAW_SECTION_TEN: {
                final LawSectionTenPermitApplication lawSectionTenPermitApplication =
                        lawSectionTenPermitApplicationRepository.findByHarvestPermitApplication(application);
                return new PdfModel(JSP_LAW_SECTION_TEN,
                        LawSectionTenPermitApplicationSummaryDTO.create(application, lawSectionTenPermitApplication),
                        speciesNameIndex);
            }
            case WEAPON_TRANSPORTATION:{
                final WeaponTransportationPermitApplication transportApplication =
                        weaponTransportationPermitApplicationRepository.findByHarvestPermitApplication(application);
                final List<TransportedWeapon> transportedWeapons =
                        transportedWeaponRepository.findByWeaponTransportationPermitApplicationOrderById(transportApplication);
                final List<WeaponTransportationVehicle> vehicles =
                        weaponTransportationVehicleRepository.findByWeaponTransportationPermitApplicationOrderById(transportApplication);
                return new PdfModel(JSP_WEAPON_TRANSPORTATION,
                        SummaryDTO.create(application, transportApplication, transportedWeapons, vehicles),
                        speciesNameIndex);
            }
            case DISABILITY: {
                final DisabilityPermitApplication disabilityPermitApplication =
                        disabilityPermitApplicationRepository.findByHarvestPermitApplication(application);
                final List<DisabilityPermitVehicle> vehicles =
                        disabilityPermitVehicleRepository.findByDisabilityPermitApplicationOrderById(disabilityPermitApplication);
                final List<DisabilityPermitHuntingTypeInfo> huntingTypeInfos =
                        disabilityPermitHuntingTypeInfoRepository.findByDisabilityPermitApplicationOrderById(disabilityPermitApplication);
                final List<HarvestPermitApplicationAttachment> attachments =
                        applicationAttachmentRepository.findByHarvestPermitApplication(application);
                return new PdfModel(JSP_DISABILITY,
                        DisabilityPermitSummaryDTO.create(application, disabilityPermitApplication, vehicles, huntingTypeInfos,
                                application.getContactPerson(), attachments),
                        speciesNameIndex);
            }
            case IMPORTING: {
                final ImportingPermitApplication importingPermitApplication =
                        importingPermitApplicationRepository.findByHarvestPermitApplication(application);
                final List<HarvestPermitApplicationAttachment> attachments = application.getAttachments();
                final List<HarvestPermitApplicationAttachment> areaAttachments =
                        F.filterToList(attachments, a -> a.getAttachmentType() == PROTECTED_AREA);
                final List<HarvestPermitApplicationAttachment> otherAttachments =
                        F.filterToList(attachments, a -> a.getAttachmentType() == OTHER);
                return new PdfModel(JSP_IMPORTING,
                        ImportingPermitApplicationSummaryDTO.from(
                                application, importingPermitApplication, application.getSpeciesAmounts(),
                                areaAttachments, otherAttachments),
                        speciesNameIndex);
            }
            case DOG_UNLEASH: {
                final DogEventApplication dogEventApplication =
                        dogEventApplicationRepository.findByHarvestPermitApplication(application);
                final List<DogEventUnleash> dogEvents = dogEventUnleashRepository.findAllByHarvestPermitApplication(application);
                return new PdfModel(JSP_DOG_EVENT_UNLEASH,
                                    DogEventUnleashSummaryDTO.create(application, dogEventApplication, dogEvents),
                                    speciesNameIndex);
            }
            case DOG_DISTURBANCE: {
                final DogEventApplication dogEventApplication = dogEventApplicationRepository.findByHarvestPermitApplication(application);

                final DogEventDisturbance trainingEvent = dogEventDisturbanceRepository.findByHarvestPermitApplicationAndEventType(application, DOG_TRAINING);
                final DogEventDisturbance testEvent = dogEventDisturbanceRepository.findByHarvestPermitApplicationAndEventType(application, DOG_TEST);

                return new PdfModel(JSP_DOG_EVENT_DISTURBANCE,
                        DogEventDisturbanceSummaryDTO.create(
                                application,
                                dogEventApplication,
                                createDogDisturbanceDto(trainingEvent),
                                createDogDisturbanceDto(testEvent)),
                        speciesNameIndex);
            }
            case DEPORTATION: {
                final DeportationPermitApplication deportationPermitApplication =
                        deportationPermitApplicationRepository.findByHarvestPermitApplication(application);
                final DerogationPermitApplicationReasonsDTO reasonsDTO =
                        derogationPermitApplicationReasonService.getDerogationReasons(application, locale);
                return new PdfModel(JSP_DEPORTATION, DeportationSummaryDTO.create(application,
                        deportationPermitApplication, reasonsDTO), speciesNameIndex);
            }
            case RESEARCH: {
                final ResearchPermitApplication researchPermitApplication =
                        researchPermitApplicationRepository.findByHarvestPermitApplication(application);
                final DerogationPermitApplicationReasonsDTO reasonsDTO =
                        derogationPermitApplicationReasonService.getDerogationReasons(application, locale);
                return new PdfModel(JSP_RESEARCH, ResearchSummaryDTO.create(application,
                        researchPermitApplication, reasonsDTO), speciesNameIndex);
            }
            case GAME_MANAGEMENT: {
                final GameManagementPermitApplication gameManagementPermitApplication =
                        gameManagementPermitApplicationRepository.findByHarvestPermitApplication(application);
                final List<HarvestPermitApplicationSpeciesAmount> speciesAmounts =
                        harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application);
                return new PdfModel(JSP_GAME_MANAGEMENT,
                        GameManagementSummaryDTO.create(application, gameManagementPermitApplication, speciesAmounts),
                        speciesNameIndex);
            }
            default:
                throw new IllegalArgumentException();
        }
    }

    private MooselikePermitApplicationPdfDTO createMooselikeModel(final HarvestPermitApplication application,
                                                                  final Locale locale) {
        final HarvestPermitArea permitArea = application.getArea();
        final GISZoneSizeDTO areaSize = gisZoneRepository.getAreaSize(permitArea.getZone().getId());

        final List<MooselikePermitApplicationSpeciesAmountDTO> speciesAmounts = application.getSpeciesAmounts().stream()
                .sorted(Comparator.comparingDouble(HarvestPermitApplicationSpeciesAmount::getSpecimenAmount).reversed())
                .map(MooselikePermitApplicationSpeciesAmountDTO::create)
                .collect(toList());

        final List<OrganisationNameDTO> partners = application.getPermitPartners().stream()
                .map(OrganisationNameDTO::createWithOfficialCode)
                .sorted(Comparator.comparing(OrganisationNameDTO::getOfficialCode))
                .collect(toList());

        final List<HarvestPermitAreaRhyDTO> rhys = permitArea.getRhy().stream()
                .map(HarvestPermitAreaRhyDTO::create)
                .sorted(Comparator.<HarvestPermitAreaRhyDTO>comparingDouble(rhy -> rhy.getBothSize().getTotal()).reversed())
                .collect(toList());

        final List<HarvestPermitAreaHtaDTO> htas = permitArea.getHta().stream()
                .map(HarvestPermitAreaHtaDTO::create)
                .sorted(Comparator.comparingDouble(HarvestPermitAreaHtaDTO::getComputedAreaSize).reversed())
                .collect(toList());

        final String unionAreaId = permitArea.getExternalId();
        final List<HarvestPermitAreaPartnerDTO> areaPartners =
                harvestPermitAreaPartnerService.listPartners(permitArea, locale);
        final String language = locale.getLanguage();

        return new MooselikePermitApplicationPdfDTO(application, speciesAmounts, partners, rhys, htas, areaSize,
                unionAreaId, areaPartners, language);
    }

    private DogEventDisturbanceDTO createDogDisturbanceDto(final DogEventDisturbance event) {
        return F.mapNullable(event,
                             evt -> DogEventDisturbanceDTO.createFrom(
                                     evt,
                                     dogEventDisturbanceContactRepository.findAllByEvent(evt),
                                     F.mapNullable(evt.getGameSpecies(), GameSpecies::getOfficialCode)));
    }

    @Transactional(readOnly = true)
    public HarvestPermitApplicationPdfDTO getApplication(final long applicationId) {
        final HarvestPermitApplication application = readApplication(applicationId);

        final String applicationNumber = application.getApplicationNumber() != null
                ? Integer.toString(application.getApplicationNumber())
                : messageSource.getMessage("pdf.application.header.draft", null, application.getLocale());

        final String fileName = String.format("%s-%s.pdf",
                HarvestPermitApplication.FILENAME_PREFIX.getAnyTranslation(application.getLocale()),
                applicationNumber);

        return new HarvestPermitApplicationPdfDTO(fileName, applicationNumber, application.getLocale(),
                application.getPrintingUrl());
    }

    private HarvestPermitApplication readApplication(final long applicationId) {
        final HarvestPermitApplication application = requireEntityService.requireHarvestPermitApplication(
                applicationId, EntityPermission.READ);
        application.assertStatus(EnumSet.of(ACTIVE, DRAFT, AMENDING));
        return application;
    }
}

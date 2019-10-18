package fi.riista.feature.permit.application.pdf;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
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
import fi.riista.feature.permit.application.mooselike.MooselikePermitApplicationSpeciesAmountDTO;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.hta.HarvestPermitAreaHtaDTO;
import fi.riista.feature.permit.area.rhy.HarvestPermitAreaRhyDTO;
import fi.riista.security.EntityPermission;
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
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@Component
public class HarvestPermitApplicationPdfFeature {

    private static final String JSP_MOOSELIKE = "pdf/application-mooselike";
    private static final String JSP_MOOSELIKE_AMENDMENT = "pdf/application-mooselike-amendment";
    private static final String JSP_BIRD = "pdf/application-bird";
    private static final String JSP_CARNIVORE = "pdf/application-carnivore";

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
    private GameSpeciesService gameSpeciesService;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Resource
    private MessageSource messageSource;

    @Transactional(readOnly = true)
    public PdfModel getPdfModel(final long applicationId, final Locale locale) {
        final HarvestPermitApplication application = readApplication(applicationId);
        final Map<Integer, String> speciesNameIndex = gameSpeciesService.getNameIndex().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getTranslation(locale)));

        switch (application.getHarvestPermitCategory()) {
            case MOOSELIKE:
                return new PdfModel(JSP_MOOSELIKE, createMooselikeModel(application), speciesNameIndex);

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
                final CarnivorePermitApplication carnivorePermitApplication = carnivorePermitApplicationRepository.findByHarvestPermitApplication(application);
                return new PdfModel(JSP_CARNIVORE, CarnivorePermitApplicationSummaryDTO.from(application, carnivorePermitApplication), speciesNameIndex);
            default:
                throw new IllegalArgumentException();
        }
    }

    private MooselikePermitApplicationPdfDTO createMooselikeModel(final HarvestPermitApplication application) {
        final HarvestPermitArea permitArea = application.getArea();
        final GISZoneSizeDTO areaSize = gisZoneRepository.getAreaSize(permitArea.getZone().getId());

        final List<MooselikePermitApplicationSpeciesAmountDTO> speciesAmounts = application.getSpeciesAmounts().stream()
                .sorted(Comparator.comparingDouble(HarvestPermitApplicationSpeciesAmount::getAmount).reversed())
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

        return new MooselikePermitApplicationPdfDTO(application, speciesAmounts, partners, rhys, htas, areaSize);
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

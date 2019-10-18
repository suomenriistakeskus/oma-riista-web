package fi.riista.feature.permit.application.carnivore.area;

import com.vividsolutions.jts.geom.Geometry;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.area.PersonalArea;
import fi.riista.feature.account.area.PersonalAreaRepository;
import fi.riista.feature.account.area.union.PersonalAreaUnion;
import fi.riista.feature.account.area.union.PersonalAreaUnionPrintService;
import fi.riista.feature.account.area.union.PersonalAreaUnionRepository;
import fi.riista.feature.gis.GISBounds;
import fi.riista.feature.gis.GISQueryService;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.area.HuntingClubAreaRepository;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationLockedCondition;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachmentRepository;
import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplication;
import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplicationRepository;
import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplicationService;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.feature.storage.metadata.FileType;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.integration.mapexport.MapPdfBasemap;
import fi.riista.integration.mapexport.MapPdfModel;
import fi.riista.integration.mapexport.MapPdfParameters;
import fi.riista.integration.mapexport.MapPdfRemoteService;
import fi.riista.security.EntityPermission;
import fi.riista.util.GISUtils;
import fi.riista.util.LocalisedString;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Component
public class CarnivorePermitApplicationAreaFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HarvestPermitApplicationLockedCondition harvestPermitApplicationLockedCondition;

    @Resource
    private HarvestPermitApplicationAttachmentRepository harvestPermitApplicationAttachmentRepository;

    @Resource
    private GISZoneRepository zoneRepository;

    @Resource
    private HuntingClubAreaRepository huntingClubAreaRepository;

    @Resource
    private PersonalAreaRepository personalAreaRepository;

    @Resource
    private MapPdfRemoteService mapPdfRemoteService;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private GISQueryService gisQueryService;

    @Resource
    private CarnivorePermitApplicationService carnivorePermitApplicationService;

    @Resource
    private CarnivorePermitApplicationRepository carnivorePermitApplicationRepository;

    @Resource
    private PersonalAreaUnionRepository personalAreaUnionRepository;

    @Resource
    private PersonalAreaUnionPrintService personalAreaUnionPrintService;

    // AREA

    @Transactional(readOnly = true)
    public CarnivorePermitApplicationAreaDTO getArea(final long applicationId) {
        final CarnivorePermitApplication carnivorePermitApplication =
                carnivorePermitApplicationService.findForRead(applicationId);

        return CarnivorePermitApplicationAreaDTO.createFrom(carnivorePermitApplication);
    }

    @Transactional
    public void updateProtectedArea(final long applicationId,
                                    final @NotNull CarnivorePermitApplicationAreaDTO area) {
        final CarnivorePermitApplication carnivorePermitApplication =
                carnivorePermitApplicationService.findForRead(applicationId);

        final HarvestPermitApplication application = carnivorePermitApplication.getHarvestPermitApplication();
        application.setRhy(gisQueryService.findRhyByLocation(area.getGeoLocation()));

        carnivorePermitApplication.setAreaSize(area.getAreaSize());
        carnivorePermitApplication.setGeoLocation(area.getGeoLocation());
        carnivorePermitApplication.setAreaDescription(area.getAreaDescription());
        carnivorePermitApplicationRepository.save(carnivorePermitApplication);
    }

    // AREA ATTACHMENTS

    @Transactional
    public void addAreaAttachment(final CarnivorePermitApplicationAddAreaAttachmentDTO dto) {
        final HarvestPermitApplication application =
                requireEntityService.requireHarvestPermitApplication(dto.getId(), EntityPermission.READ);
        harvestPermitApplicationLockedCondition.assertCanUpdate(application);


        final Optional<PersonalAreaUnion> unionOption =
                personalAreaUnionRepository.findByExternalId(dto.getExternalId());

        final byte[] mapPdfData;
        if (unionOption.isPresent()) {
            final PersonalAreaUnion personalAreaUnion = unionOption.get();
            final MapPdfModel model = personalAreaUnionPrintService.getModel(personalAreaUnion,
                    MapPdfParameters.Overlay.NONE,
                    application.getLocale());
            mapPdfData = personalAreaUnionPrintService.createPdf(personalAreaUnion, application.getLocale(),
                    model, createPdfParameters(model));
        } else {
            final MapPdfModel model = createMapPdfModel(dto.getExternalId(), application.getLocale());

            final MapPdfParameters parameters = createPdfParameters(model);

            mapPdfData = mapPdfRemoteService.renderPdf(parameters, model);

        }
        final String fileName = dto.getExternalId() + ".pdf";

        final HarvestPermitApplicationAttachment attachment = new HarvestPermitApplicationAttachment();
        attachment.setAttachmentType(HarvestPermitApplicationAttachment.Type.PROTECTED_AREA);
        attachment.setHarvestPermitApplication(application);
        attachment.setAttachmentMetadata(storeAttachment(mapPdfData, fileName));

        harvestPermitApplicationAttachmentRepository.save(attachment);
    }

    private MapPdfParameters createPdfParameters(final MapPdfModel model) {
        final MapPdfParameters parameters = new MapPdfParameters();
        parameters.setLayer(MapPdfBasemap.MAASTOKARTTA);
        parameters.setPaperSize(MapPdfParameters.PaperSize.A3);
        parameters.setPaperOrientation(model.isPreferLandscape()
                ? MapPdfParameters.PaperOrientation.LANDSCAPE
                : MapPdfParameters.PaperOrientation.PORTRAIT);
        return parameters;
    }

    private PersistentFileMetadata storeAttachment(final byte[] mapPdfData, final String fileName) {
        try {
            return fileStorageService.storeFile(UUID.randomUUID(), mapPdfData,
                    FileType.PERMIT_APPLICATION_ATTACHMENT, MediaType.APPLICATION_PDF_VALUE,
                    fileName);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private MapPdfModel createMapPdfModel(final String externalId,
                                          final Locale locale) {
        final Optional<HuntingClubArea> clubAreaOptional = huntingClubAreaRepository.findByExternalId(externalId);

        if (clubAreaOptional.isPresent()) {
            return createMapPdfModel(clubAreaOptional.get(), locale);
        }

        final Optional<PersonalArea> personalAreaOptional = personalAreaRepository.findByExternalId(externalId);

        if (personalAreaOptional.isPresent()) {
            return createMapPdfModel(personalAreaOptional.get(), locale);
        }

        throw new IllegalArgumentException("Area not found: " + externalId);
    }

    private MapPdfModel createMapPdfModel(final PersonalArea personalArea,
                                          final Locale locale) {
        final long zoneId = personalArea.getZone().getId();
        final Geometry invertedGeometry = zoneRepository.getInvertedSimplifiedGeometry(zoneId,
                GISUtils.SRID.ETRS_TM35FIN);
        final GISBounds bounds = zoneRepository.getBounds(zoneId, GISUtils.SRID.ETRS_TM35FIN);
        final GISZoneSizeDTO areaSize = zoneRepository.getAreaSize(zoneId);

        return new MapPdfModel.Builder(locale)
                .withExternalId(personalArea.getExternalId())
                .withAreaName(LocalisedString.of(personalArea.getName()))
                .withClubName(LocalisedString.of(personalArea.getPerson().getFullName()))
                .withModificationTime(personalArea.getModificationTime())
                .withAreaSize(areaSize)
                .withGeometry(invertedGeometry)
                .withBbox(bounds.toBBox())
                .build();
    }

    private MapPdfModel createMapPdfModel(final HuntingClubArea clubArea,
                                          final Locale locale) {
        final long zoneId = clubArea.getZone().getId();
        final Geometry invertedGeometry = zoneRepository.getInvertedSimplifiedGeometry(zoneId,
                GISUtils.SRID.ETRS_TM35FIN);
        final GISBounds bounds = zoneRepository.getBounds(zoneId, GISUtils.SRID.ETRS_TM35FIN);
        final GISZoneSizeDTO areaSize = zoneRepository.getAreaSize(zoneId);

        return new MapPdfModel.Builder(locale)
                .withExternalId(clubArea.getExternalId())
                .withAreaName(clubArea.getNameLocalisation())
                .withClubName(clubArea.getClub().getNameLocalisation())
                .withModificationTime(clubArea.getModificationTime())
                .withAreaSize(areaSize)
                .withGeometry(invertedGeometry)
                .withBbox(bounds.toBBox())
                .build();
    }
}

package fi.riista.feature.huntingclub.moosedatacard;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gamediary.GameDiaryEntry_;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen_;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen_;
import fi.riista.feature.gamediary.observation.ObservationRepository;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenRepository;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenRepository;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.HuntingClubGroup_;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayRepository;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay_;
import fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportException;
import fi.riista.feature.storage.FileDownloadService;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.security.EntityPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fi.riista.util.jpa.JpaSpecs.equal;
import static fi.riista.util.jpa.JpaSpecs.inCollection;
import static org.springframework.data.jpa.domain.Specifications.where;

@Service
public class MooseDataCardImportFeature {

    private static final Logger LOG = LoggerFactory.getLogger(MooseDataCardImportFeature.class);

    @Resource
    private MooseDataCardImportService importService;

    @Resource
    private FileDownloadService fileDownloadService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private MooseDataCardImportRepository importRepo;

    @Resource
    private GroupHuntingDayRepository huntingDayRepo;

    @Resource
    private HarvestRepository harvestRepo;

    @Resource
    private HarvestSpecimenRepository harvestSpecimenRepo;

    @Resource
    private ObservationRepository observationRepo;

    @Resource
    private ObservationSpecimenRepository observationSpecimenRepo;

    // Returns a list of messages to be informed to the user about processing of import.
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public List<String> importMooseDataCardAsModerator(
            @Nonnull final MultipartFile xmlFile, @Nonnull final MultipartFile pdfFile)
            throws MooseDataCardImportException {

        return importMooseDataCard(xmlFile, pdfFile);
    }

    @PreAuthorize("isAnonymous()")
    public List<String> importMooseDataCardWithSpecialPrivilege(
            @Nonnull final MultipartFile xmlFile, @Nonnull final MultipartFile pdfFile)
            throws MooseDataCardImportException {

        return importMooseDataCard(xmlFile, pdfFile);
    }

    private List<String> importMooseDataCard(@Nonnull final MultipartFile xmlFile, @Nonnull final MultipartFile pdfFile)
            throws MooseDataCardImportException {
        
        /*
         * Import process is split into two phases: validation and data persistence. Validation is
         * done first in a read-only transaction and, if passed, the parsed moose data card and
         * other resolved information is fed to a method doing actual data persistence in a separate
         * read-write transaction. Separating import into two transactions is done for optimization
         * purposes since the validation may be somewhat expensive/slow operation involving many
         * database queries and computationally intensive processing and while assuming the failure
         * rate of the validation may be quite high on average, doing the validation in a read-only
         * transaction may give some performance benefits while using the combination of Spring and
         * Hibernate. However, if seen appropriate, the whole import process can be easily changed
         * to be done in a single read-write transaction by annotating this method
         * with @Transactional.
         */

        return importService.importMooseDataCard(
                importService.parseAndValidateMooseDataCard(xmlFile, pdfFile), xmlFile, pdfFile);
    }

    @Transactional(readOnly = true)
    public List<MooseDataCardImportDTO> getListOfMooseDataCardImportsForGroup(final long huntingGroupId) {
        final HuntingClubGroup group = requireEntityService.requireHuntingGroup(huntingGroupId, EntityPermission.READ);
        return importService.getMooseDataCardImports(group);
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public ResponseEntity<byte[]> getPdfFile(final long mooseDataCardImportId) throws IOException {
        return getMooseDataCardFile(mooseDataCardImportId, MooseDataCardImport::getPdfFileMetadata);
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public ResponseEntity<byte[]> getXmlFile(final long mooseDataCardImportId) throws IOException {
        return getMooseDataCardFile(mooseDataCardImportId, MooseDataCardImport::getXmlFileMetadata);
    }

    private ResponseEntity<byte[]> getMooseDataCardFile(
            final long mooseDataCardImportId,
            final Function<MooseDataCardImport, PersistentFileMetadata> fileMetadataFn) throws IOException {

        final MooseDataCardImport imp =
                requireEntityService.requireMooseDataCardImport(mooseDataCardImportId, EntityPermission.READ);

        return getResponseEntity(fileMetadataFn.apply(imp));
    }

    private ResponseEntity<byte[]> getResponseEntity(final PersistentFileMetadata fileMetadata) throws IOException {
        final String contentType = fileMetadata.getContentType();
        final String filename = Optional.ofNullable(fileMetadata.getOriginalFilename())
                .orElseGet(() -> "Hirvitietokortti." + contentType.replaceAll(".*/", ""));

        return fileDownloadService.download(fileMetadata, filename);
    }

    @Transactional
    public void revokeMooseDataCardImport(final long mooseDataCardImportId) {
        final MooseDataCardImport imp =
                requireEntityService.requireMooseDataCardImport(mooseDataCardImportId, EntityPermission.DELETE);

        final List<GroupHuntingDay> huntingDays = huntingDayRepo.findAll(where(
                equal(GroupHuntingDay_.mooseDataCardImport, imp))
                        // extra sanity checks imposed for added safety although not strictly necessary
                        .and(equal(GroupHuntingDay_.group, imp.getGroup()))
                        .and(equal(GroupHuntingDay_.group, HuntingClubGroup_.fromMooseDataCard, true)));

        final List<Harvest> harvests = harvestRepo.findAll(inCollection(GameDiaryEntry_.huntingDayOfGroup, huntingDays));
        final List<Observation> observations =
                observationRepo.findAll(inCollection(GameDiaryEntry_.huntingDayOfGroup, huntingDays));

        LOG.info("Hunting days with all related harvests ({}) and observations ({}) are deleted for following dates " +
                "while revoking moose data card import with ID={}: {}",
                harvests.size(),
                observations.size(),
                imp.getId(),
                huntingDays.stream().map(GroupHuntingDay::getStartDate).sorted().collect(Collectors.toList()));

        harvestSpecimenRepo.deleteInBatch(harvestSpecimenRepo.findAll(inCollection(HarvestSpecimen_.harvest, harvests)));
        harvestRepo.deleteInBatch(harvests);

        observationSpecimenRepo.deleteInBatch(
                observationSpecimenRepo.findAll(inCollection(ObservationSpecimen_.observation, observations)));
        observationRepo.deleteInBatch(observations);

        huntingDayRepo.deleteInBatch(huntingDays);

        imp.softDelete();
    }

}

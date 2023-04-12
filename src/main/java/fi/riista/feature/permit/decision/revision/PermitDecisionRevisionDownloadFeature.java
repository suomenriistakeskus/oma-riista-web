package fi.riista.feature.permit.decision.revision;

import fi.riista.api.pub.PermitDecisionDownloadDTO;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.decision.DecisionUtil;
import fi.riista.feature.harvestpermit.HarvestPermitPublicPdfDownloadRepository;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.permit.DocumentNumberUtil;
import fi.riista.feature.permit.PermitClientUriFactory;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.attachment.PermitDecisionAttachment;
import fi.riista.feature.permit.decision.attachment.PermitDecisionAttachmentRepository;
import fi.riista.feature.permit.zip.OmaRiistaDecisionAttachmentsZip;
import fi.riista.feature.permit.zip.OmaRiistaDecisionAttachmentsZipBuilder;
import fi.riista.feature.storage.FileDownloadService;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.feature.storage.metadata.PersistentFileMetadataRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.validation.FinnishHuntingPermitNumberValidator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;

@Component
public class PermitDecisionRevisionDownloadFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private FileDownloadService fileDownloadService;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private PermitDecisionRevisionRepository decisionRevisionRepository;

    @Resource
    private PermitDecisionAttachmentRepository permitDecisionAttachmentRepository;

    @Resource
    private PersistentFileMetadataRepository persistentFileMetadataRepository;

    @Resource
    private PermitClientUriFactory permitClientUriFactory;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private HarvestPermitPublicPdfDownloadRepository downloadRepository;

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public void downloadPdf(final long decisionId,
                            final long revisionId,
                            final HttpServletResponse response) throws IOException {
        final PermitDecision permitDecision = requireEntityService.requirePermitDecision(decisionId,
                EntityPermission.READ);
        final PermitDecisionRevision revision = decisionRevisionRepository.getOne(revisionId);

        download(response, permitDecision, revision);
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public void downloadPdfNoAuthorization(final long revisionId,
                                           final HttpServletResponse response) throws IOException {
        final PermitDecisionRevision revision = decisionRevisionRepository.getOne(revisionId);

        download(response, revision.getPermitDecision(), revision);
    }

    private void download(final HttpServletResponse response,
                          final PermitDecision decision,
                          final PermitDecisionRevision revision) throws IOException {
        if (!Objects.equals(decision, revision.getPermitDecision())) {
            throw new IllegalStateException("invalid revision for decisionId: " + decision.getId());
        }

        final String filename = DecisionUtil.getPermitDecisionFileName(decision.getLocale(), decision.createPermitNumber());

        fileDownloadService.downloadUsingTemporaryFile(revision.getPdfMetadata(), filename, response);
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public void downloadDecisionAttachmentNoAuthorization(final long revisionId, final long attachmentId,
                                                          final HttpServletResponse response) throws IOException {
        final PermitDecisionRevision revision = decisionRevisionRepository.getOne(revisionId);
        final PermitDecision permitDecision = revision.getPermitDecision();
        final PermitDecisionAttachment attachment = permitDecisionAttachmentRepository.getOne(attachmentId);

        // Only allow downloading of attachments actually listed on the decision
        if (attachment.getOrderingNumber() == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        downloadAttachment(response, permitDecision, attachment);
    }

    private void downloadAttachment(final HttpServletResponse response, final PermitDecision permitDecision,
                                    final PermitDecisionAttachment attachment) throws IOException {
        checkArgument(attachment.getPermitDecision().equals(permitDecision));
        fileDownloadService.downloadUsingTemporaryFile(attachment.getAttachmentMetadata(),
                attachment.getAttachmentMetadata().getOriginalFilename(),
                response);
    }

    @Transactional(readOnly = true)
    public PermitDecisionDownloadDTO getDownloadLinks(final UUID uuid, final long revisionId) {
        final PermitDecisionRevision revision = decisionRevisionRepository.getOne(revisionId);
        final PermitDecision decision = revision.getPermitDecision();
        final List<PermitDecisionAttachment> attachments =
                permitDecisionAttachmentRepository.findListedAttachmentsByPermitDecisionRevision(revision);
        final PermitDecisionDownloadDTO.Builder builder = PermitDecisionDownloadDTO.Builder.builder()
                .withDecisionLink(permitClientUriFactory.getAbsoluteAnonymousDecisionPdfDownloadUri(uuid).toString(),
                        decision.createPermitNumber());

        attachments.forEach(a -> builder.withAttachment(
                permitClientUriFactory.getAbsoluteAnonymousDecisionAttachmentUri(uuid, a.getId()).toString(),
                a.getDescription()));

        return builder.build();
    }

    @Nullable
    @Transactional(readOnly = true)
    public Long downloadPublicCarnivoreDecisionNoAuthentication(final HttpServletResponse response,
                                                                final String documentNumber,
                                                                final Locale locale) {
        if (!FinnishHuntingPermitNumberValidator.validate(documentNumber, true)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        final int decisionNumber = DocumentNumberUtil.extractOrderNumber(documentNumber);
        final String filename = DecisionUtil.getPermitDecisionFileName(locale, documentNumber);

        return decisionRevisionRepository.findLatestPublicDecisionPdf(decisionNumber)
                .flatMap(metadata -> harvestPermitRepository.isCarnivorePermitAvailable(documentNumber)
                        .map(decisionId -> {
                            doDownloadPublicPdf(response, metadata, filename);
                            return decisionId;
                        }))
                .orElseGet(() -> {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return null;
                });

    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public ResponseEntity<byte[]> downloadPublicCarnivoreDecisionAttachmentsNoAuthentication(final HttpServletResponse response,
                                                                                             final String documentNumber,
                                                                                             final Locale locale) throws IOException {
        if (!FinnishHuntingPermitNumberValidator.validate(documentNumber, true)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        if (!harvestPermitRepository.isCarnivorePermitAvailable(documentNumber).isPresent()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        final int decisionNumber = DocumentNumberUtil.extractOrderNumber(documentNumber);
        final OmaRiistaDecisionAttachmentsZip attachmentsZip = new OmaRiistaDecisionAttachmentsZipBuilder(fileStorageService, locale)
                .withAttachments(persistentFileMetadataRepository.findLatestPublicDecisionAttachmentsPdf(decisionNumber))
                .withDecisionNumber(documentNumber)
                .build();

        response.setStatus(HttpServletResponse.SC_OK);
        HttpHeaders headers = ContentDispositionUtil.header(attachmentsZip.getFilename());
        response.setHeader(ContentDispositionUtil.CONTENT_DISPOSITION, headers.getContentDisposition().toString());

        return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .body(attachmentsZip.getData());
    }

    @Async
    @Transactional
    public void decisionDownloaded(final long decisionId) {
        downloadRepository.insertDownload(decisionId);
    }

    private void doDownloadPublicPdf(final HttpServletResponse response,
                                     final PersistentFileMetadata metadata,
                                     final String filename) {
        try {
            fileDownloadService.downloadUsingTemporaryFile(metadata, filename, response);
        } catch (final IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}

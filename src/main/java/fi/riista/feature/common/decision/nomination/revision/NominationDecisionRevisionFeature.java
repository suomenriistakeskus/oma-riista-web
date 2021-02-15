package fi.riista.feature.common.decision.nomination.revision;

import fi.riista.api.decision.nomination.NominationDecisionPdfController;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.PdfExportFactory;
import fi.riista.feature.common.decision.DecisionActionType;
import fi.riista.feature.common.decision.DecisionStatus;
import fi.riista.feature.common.decision.nomination.NominationDecision;
import fi.riista.feature.common.decision.nomination.NominationDecisionDocumentTransformer;
import fi.riista.feature.common.decision.nomination.action.NominationDecisionAction;
import fi.riista.feature.common.decision.nomination.action.NominationDecisionActionRepository;
import fi.riista.feature.common.decision.nomination.attachment.NominationDecisionAttachment;
import fi.riista.feature.common.decision.nomination.attachment.NominationDecisionAttachmentRepository;
import fi.riista.feature.common.decision.nomination.delivery.NominationDecisionDelivery;
import fi.riista.feature.common.decision.nomination.delivery.NominationDecisionDeliveryRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.decision.DecisionUnlockDTO;
import fi.riista.feature.storage.FileDownloadService;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.feature.storage.metadata.FileType;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import fi.riista.util.MediaTypeExtras;
import fi.riista.util.PDFUtil;
import fi.riista.util.RandomStringUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static fi.riista.util.DateUtil.now;
import static fi.riista.util.EmailSanitizer.getSanitizedOrNull;
import static java.util.Optional.ofNullable;


@Component
public class NominationDecisionRevisionFeature {

    private static final Logger LOG = LoggerFactory.getLogger(NominationDecisionRevisionFeature.class);

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private NominationDecisionRevisionRepository nominationDecisionRevisionRepository;

    @Resource
    private NominationDecisionAttachmentRepository nominationDecisionAttachmentRepository;

    @Resource
    private NominationDecisionRevisionAttachmentRepository nominationDecisionRevisionAttachmentRepository;

    @Resource
    private NominationDecisionRevisionReceiverRepository nominationDecisionRevisionReceiverRepository;

    @Resource
    private SecureRandom secureRandom;

    @Resource
    private PdfExportFactory pdfExportFactory;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private FileDownloadService fileDownloadService;

    @Resource
    private NominationDecisionActionRepository nominationDecisionActionRepository;

    @Resource
    private NominationDecisionRevisionDTOTransformer nominationDecisionRevisionDTOTransformer;

    @Resource
    private NominationDecisionDeliveryRepository nominationDecisionDeliveryRepository;

    @Transactional(readOnly = true)
    public List<NominationDecisionRevisionDTO> listRevisions(final long id) {
        final NominationDecision decision = requireEntityService.requireNominationDecision(id, EntityPermission.READ);
        final List<NominationDecisionRevision> allRevisions =
                nominationDecisionRevisionRepository.findByNominationDecision(decision);
        return nominationDecisionRevisionDTOTransformer.apply(allRevisions);
    }

    @Transactional
    public void lockDecision(final long id) {
        final NominationDecision decision = requireEntityService.requireNominationDecision(id, EntityPermission.UPDATE);
        decision.assertHandler(activeUserService.requireActiveUser());

        decision.assertStatus(DecisionStatus.DRAFT);

        decision.setStatusLocked();

        decision.setPublishDate(now());

        // Cancel prior revisions
        cancelRevisions(decision);
    }

    @Transactional
    public void createAndPublishDecisionRevision(final long id) {
        final NominationDecision decision = requireEntityService.requireNominationDecision(id, EntityPermission.UPDATE);

        final String revisionExternalId = RandomStringUtil.generateExternalId(secureRandom);
        final PersistentFileMetadata pdfMetadata = storeDecisionPdf(decision.getId(), decision, revisionExternalId);
        final NominationDecisionRevision decisionRevision = createRevisionEntity(decision, revisionExternalId,
                pdfMetadata);
        final List<NominationDecisionAttachment> attachments =
                nominationDecisionAttachmentRepository.findAllByNominationDecision(decision);
        createAttachments(decisionRevision, attachments);

        final DateTime scheduledDate = decision.getPublishDate();

        createContactPersonReceiver(decisionRevision, scheduledDate);
        createRevisionReceivers(decisionRevision, scheduledDate);

        decision.setStatusPublished();
    }

    @Nonnull
    private NominationDecisionRevision createRevisionEntity(final NominationDecision decision,
                                                            final String revisionExternalId,
                                                            final PersistentFileMetadata pdfMetadata) {
        final NominationDecisionRevision decisionRevision = new NominationDecisionRevision();
        decisionRevision.setDecisionType(decision.getDecisionType());
        decisionRevision.setNominationDecision(decision);
        decisionRevision.setDocument(NominationDecisionDocumentTransformer.PASS_THROUGH.copy(decision.getDocument()));
        decisionRevision.setExternalId(revisionExternalId);
        decisionRevision.setLockedDate(decision.getLockedDate());
        decisionRevision.setScheduledPublishDate(decision.getPublishDate());
        decisionRevision.setPdfMetadata(pdfMetadata);
        decisionRevision.setPublishDate(decision.getPublishDate());

        return nominationDecisionRevisionRepository.save(decisionRevision);
    }

    private void createAttachments(final NominationDecisionRevision decisionRevision,
                                   final List<NominationDecisionAttachment> attachments) {
        nominationDecisionRevisionAttachmentRepository.saveAll(attachments.stream()
                .filter(a -> !a.isDeleted())
                .map(a -> new NominationDecisionRevisionAttachment(decisionRevision, a))
                .collect(Collectors.toList()));
    }

    private void createContactPersonReceiver(final NominationDecisionRevision revision, final DateTime scheduledDate) {
        final NominationDecision decision = revision.getNominationDecision();
        final Riistanhoitoyhdistys rhy = decision.getRhy();
        final Person contactPerson = decision.getContactPerson();

        // Use coordinator's personal email only when RHY email not present
        final String email = ofNullable(getSanitizedOrNull(rhy.getEmail())).orElseGet(contactPerson::getEmail);

        // Even though handled as delivered by mail, store contact person email for sending signed decision by email
        nominationDecisionRevisionReceiverRepository.save(new NominationDecisionRevisionReceiver(
                revision, NominationDecisionRevisionReceiver.ReceiverType.CONTACT_PERSON, email,
                decision.getContactPerson().getFullName(), scheduledDate));
    }

    private void createRevisionReceivers(final NominationDecisionRevision revision, final DateTime scheduledDate) {
        final List<NominationDecisionDelivery> deliveries =
                nominationDecisionDeliveryRepository.findAllByNominationDecisionOrderById(revision.getNominationDecision());
        nominationDecisionRevisionReceiverRepository.saveAll(deliveries.stream()
                .map(delivery -> new NominationDecisionRevisionReceiver(revision,
                        NominationDecisionRevisionReceiver.ReceiverType.OTHER,
                        delivery.getEmail(), delivery.getName(), scheduledDate))
                .collect(Collectors.toList()));
    }

    @Transactional
    public void unlockDecision(final DecisionUnlockDTO dto) {
        final NominationDecision decision =
                requireEntityService.requireNominationDecision(dto.getId(), EntityPermission.UPDATE);
        decision.assertHandler(activeUserService.requireActiveUser());

        final NominationDecisionAction action = new NominationDecisionAction();
        action.setNominationDecision(decision);
        action.setPointOfTime(DateUtil.now());
        action.setText(dto.getUnlockReason());
        action.setActionType(DecisionActionType.MUU);
        nominationDecisionActionRepository.save(action);

        decision.setStatusDraft();
        cancelRevisions(decision);
    }

    private void cancelRevisions(final NominationDecision decision) {
        final List<NominationDecisionRevision> byPermitDecision =
                nominationDecisionRevisionRepository.findByNominationDecision(decision);
        final List<NominationDecisionRevision> revsToCancel = byPermitDecision.stream()
                .filter(rev -> !rev.isCancelled())
                .collect(Collectors.toList());

        revsToCancel.forEach(rev -> {
            rev.setCancelled(true);
            nominationDecisionRevisionReceiverRepository.findAllByDecisionRevision(rev)
                    .stream()
                    .filter(receiver -> !receiver.isCancelled() && receiver.getSentDate() == null)
                    .forEach(receiver -> receiver.setCancelled(true));
        });

    }

    @Nonnull
    private PersistentFileMetadata storeDecisionPdf(final long id, final NominationDecision nominationDecision,
                                                    final String revisionExternalId) {
        checkArgument(StringUtils.hasText(revisionExternalId));

        try {
            final Path pdfPath = Files.createTempFile("decision", null);
            final String permitNumber = nominationDecision.createDocumentNumber();
            final String headerRight = permitNumber + " (" + revisionExternalId + ")";

            pdfExportFactory.create()
                    .withHeaderRight(headerRight)
                    .withHtmlPath(NominationDecisionPdfController.getHtmlPath(id))
                    .build()
                    .export(pdfPath);
            validateDecisionPdf(nominationDecision, pdfPath);

            return fileStorageService.storeFile(UUID.randomUUID(), pdfPath.toFile(), FileType.DECISION_PDF,
                    MediaTypeExtras.APPLICATION_PDF_VALUE, null);

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void validateDecisionPdf(final NominationDecision nominationDecision, final Path pdfPath) {
        final String textContent = PDFUtil.extractAllText(pdfPath.toFile());

        // Check postal code since it should include no white space
        final String postalCode = nominationDecision.getDeliveryAddress().getPostalCode();
        if (!textContent.contains(postalCode)) {
            throw new IllegalStateException("Recipient information missing");
        }
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public ResponseEntity<byte[]> getAttachment(final long decisionId, final long attachmentId) throws IOException {
        final NominationDecisionRevisionAttachment attachment =
                nominationDecisionRevisionAttachmentRepository.getOne(attachmentId);
        final NominationDecision nominationDecision = attachment.getDecisionAttachment().getNominationDecision();
        checkArgument(nominationDecision.getId() == decisionId, "Attachment must match decision");
        activeUserService.assertHasPermission(nominationDecision, EntityPermission.READ);

        return fileDownloadService.download(attachment.getDecisionAttachment().getAttachmentMetadata());
    }

    @Transactional
    public long updateViewCountAndResolveRevisionIdByReceiverUuid(final UUID uuid) {
        final NominationDecisionRevisionReceiver receiver =
                nominationDecisionRevisionReceiverRepository.findByUuid(uuid);
        receiver.setViewCount(receiver.getViewCount() + 1);
        return receiver.getDecisionRevision().getId();
    }

    @Transactional(readOnly = true)
    public long resolveRevisionIdByReceiverUuid(final UUID uuid) {
        final NominationDecisionRevisionReceiver receiver =
                nominationDecisionRevisionReceiverRepository.findByUuid(uuid);
        return receiver.getDecisionRevision().getId();
    }

    @Transactional
    public NominationDecisionRevisionDTO updatePosted(final long decisionId, final long revisionId,
                                                      final boolean posted) {
        final NominationDecision decision = requireEntityService.requireNominationDecision(decisionId,
                EntityPermission.UPDATE);
        final NominationDecisionRevision revision = nominationDecisionRevisionRepository.getOne(revisionId);
        checkArgument(revision.getNominationDecision().equals(decision));

        if (posted) {
            checkState(revision.getPostedByMailDate() == null, "Revision is already set as posted");
            final DateTime now = DateUtil.now();
            checkState(revision.getScheduledPublishDate().isBefore(now),
                    "Trying to publish before scheduled publish date");

            revision.setPostedByMailDate(now);
            revision.setPostedByMailUsername(activeUserService.requireActiveUser().getFullName());

            // TODO: Update receiver statuses
        } else {
            checkState(revision.getPostedByMailDate() != null, "Revision is already set as not posted");
            revision.setPostedByMailDate(null);
        }

        return nominationDecisionRevisionDTOTransformer.apply(revision);
    }

}

package fi.riista.feature.permit.decision.revision;

import com.google.common.base.Preconditions;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.common.PdfExportFactory;
import fi.riista.feature.common.decision.DecisionActionType;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.decision.DecisionUnlockDTO;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionDocumentTransformer;
import fi.riista.feature.permit.decision.action.PermitDecisionAction;
import fi.riista.feature.permit.decision.action.PermitDecisionActionRepository;
import fi.riista.feature.permit.decision.attachment.PermitDecisionAttachment;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmountRepository;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.InvoiceState;
import fi.riista.feature.permit.invoice.decision.PermitDecisionInvoice;
import fi.riista.feature.permit.invoice.decision.PermitDecisionInvoiceRepository;
import fi.riista.feature.storage.FileDownloadService;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.feature.storage.metadata.FileType;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.MediaTypeExtras;
import fi.riista.util.PDFUtil;
import fi.riista.util.RandomStringUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static fi.riista.feature.permit.PermitTypeCode.MAMMAL_DAMAGE_BASED;
import static fi.riista.feature.permit.decision.PermitDecision.DecisionType.HARVEST_PERMIT;
import static java.util.Objects.requireNonNull;

@Component
public class PermitDecisionRevisionFeature {

    private static final Logger LOG = LoggerFactory.getLogger(PermitDecisionRevisionFeature.class);
    private static final String BLANKED_INFO = "XXXXXXXXXXXXXXX";

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private PermitDecisionRevisionRepository permitDecisionRevisionRepository;

    @Resource
    private PermitDecisionRevisionAttachmentRepository permitDecisionRevisionAttachmentRepository;

    @Resource
    private PermitDecisionRevisionReceiverRepository permitDecisionRevisionReceiverRepository;

    @Resource
    private PermitDecisionSpeciesAmountRepository permitDecisionSpeciesAmountRepository;

    @Resource
    private SecureRandom secureRandom;

    @Resource
    private PdfExportFactory pdfExportFactory;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private FileDownloadService fileDownloadService;

    @Resource
    private PermitDecisionActionRepository permitDecisionActionRepository;

    @Resource
    private PermitDecisionInvoiceRepository permitDecisionInvoiceRepository;

    @Resource
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<PermitDecisionRevisionDTO> listRevisions(final long id) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(id, EntityPermission.READ);
        final List<PermitDecisionRevision> allRevisions =
                permitDecisionRevisionRepository.findByPermitDecision(decision);
        final Map<Long, String> moderatorIndex = userRepository.getModeratorFullNames(allRevisions);

        return F.mapNonNullsToList(allRevisions, r ->
                PermitDecisionRevisionDTO.create(r, moderatorIndex.get(r.getCreatedByUserId())));
    }

    @Transactional
    public void lockDecision(final long id) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(id, EntityPermission.UPDATE);
        final HarvestPermitApplication application = requireNonNull(decision.getApplication());
        decision.assertHandler(activeUserService.requireActiveUser());

        application.assertStatus(HarvestPermitApplication.Status.ACTIVE);

        decision.setStatusLocked();

        if (decision.getPublishDate() == null) {
            throw new IllegalStateException("Decision publishDate is missing");
        }

        cancelRevisions(decision);
    }

    @Transactional
    public void createDecisionRevision(final long id, final String htmlPath, final String publicHtmlPath) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(id, EntityPermission.UPDATE);

        final String revisionExternalId = RandomStringUtil.generateExternalId(secureRandom);
        final PersistentFileMetadata pdfMetadata = storeDecisionPdf(decision, revisionExternalId, htmlPath);
        final PersistentFileMetadata publicPdfMetadata = shouldGeneratePublicPdf(decision)
                ? storeDecisionPublicPdf(decision, revisionExternalId, publicHtmlPath)
                : null;
        final PermitDecisionRevision decisionRevision =
                createRevisionEntity(decision, revisionExternalId, pdfMetadata, publicPdfMetadata);
        createAttachments(decisionRevision, decision.getSortedAttachments());

        if (decision.getAppealStatus() == null) {
            final DateTime scheduledDate = decision.getPublishDate();

            createContactPersonReceiver(decisionRevision, scheduledDate);
            createRevisionReceivers(decisionRevision, scheduledDate);
            createApplicationReceivers(decisionRevision, scheduledDate);

        } else {
            LOG.info(String.format("Skipping receiver creation for permitDecisionId=%d with appeal status.", id));
        }
    }

    // To create missing public pdfs for revision manually afterwoods, method can be removed once
    // public pdf is created for all existing carnivore decisions
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public boolean generatePublicPdfForSingleDecision(Function<Long, String> htmlGetterByDecisionId) {
        final Optional<PermitDecisionRevision> revisionOptional =
                permitDecisionRevisionRepository.findCarnivoreRevisionWithNoPublicPdf();

        if (revisionOptional.isPresent()) {
            final PermitDecisionRevision revision = revisionOptional.get();
            final PermitDecision decision = revision.getPermitDecision();
            final String htmlPath = htmlGetterByDecisionId.apply(decision.getId());

            LOG.info("Generating public pdf for decision {}", decision.createPermitNumber());
            final PersistentFileMetadata metadata =
                    storeDecisionPublicPdf(decision, revision.getExternalId(), htmlPath);
            revision.setPublicPdfMetadata(metadata);

            LOG.info("Created public pdf.");
            return true;
        }

        LOG.info("No decisions with missing public pdfs.");
        return false;
    }

    @Nonnull
    private PermitDecisionRevision createRevisionEntity(final PermitDecision decision,
                                                        final String revisionExternalId,
                                                        final PersistentFileMetadata pdfMetadata,
                                                        final PersistentFileMetadata blankedPdfMetadata) {
        final PermitDecisionRevision decisionRevision = new PermitDecisionRevision();
        decisionRevision.setDecisionType(decision.getDecisionType());
        decisionRevision.setPermitDecision(decision);
        decisionRevision.setDocument(PermitDecisionDocumentTransformer.SIMPLE.copy(decision.getDocument()));
        decisionRevision.setExternalId(revisionExternalId);
        decisionRevision.setLockedDate(DateUtil.now());
        decisionRevision.setAppealStatus(decision.getAppealStatus());
        decisionRevision.setScheduledPublishDate(decision.getPublishDate());
        decisionRevision.setPostalByMail(Boolean.TRUE.equals(decision.getApplication().getDeliveryByMail()));
        decisionRevision.setPdfMetadata(pdfMetadata);
        decisionRevision.setPublicPdfMetadata(blankedPdfMetadata);

        return permitDecisionRevisionRepository.save(decisionRevision);
    }

    private void createAttachments(final PermitDecisionRevision decisionRevision,
                                   final List<PermitDecisionAttachment> attachments) {
        permitDecisionRevisionAttachmentRepository.saveAll(attachments.stream()
                .filter(a -> !a.isDeleted())
                .map(a -> new PermitDecisionRevisionAttachment(decisionRevision, a))
                .collect(Collectors.toList()));
    }

    private void createContactPersonReceiver(final PermitDecisionRevision revision, final DateTime scheduledDate) {
        final PermitDecision decision = revision.getPermitDecision();

        // If decision is delivered by mail, send the same email to contact person than to other receivers
        final PermitDecisionRevisionReceiver.ReceiverType receiverType = revision.isPostalByMail()
                ? PermitDecisionRevisionReceiver.ReceiverType.OTHER
                : PermitDecisionRevisionReceiver.ReceiverType.CONTACT_PERSON;

        permitDecisionRevisionReceiverRepository.save(new PermitDecisionRevisionReceiver(
                revision, receiverType, decision.getContactPerson().getEmail(),
                decision.getContactPerson().getFullName(), scheduledDate));
    }

    private void createRevisionReceivers(final PermitDecisionRevision revision, final DateTime scheduledDate) {
        permitDecisionRevisionReceiverRepository.saveAll(revision.getPermitDecision().getDelivery().stream()
                .map(delivery -> new PermitDecisionRevisionReceiver(revision,
                        PermitDecisionRevisionReceiver.ReceiverType.OTHER,
                        delivery.getEmail(), delivery.getName(), scheduledDate))
                .collect(Collectors.toList()));
    }

    private void createApplicationReceivers(final PermitDecisionRevision revision, final DateTime scheduledDate) {
        permitDecisionRevisionReceiverRepository.saveAll(revision.getPermitDecision().getApplication().streamEmails()
                .map(email -> new PermitDecisionRevisionReceiver(revision,
                        PermitDecisionRevisionReceiver.ReceiverType.OTHER,
                        email, "Hakemuksessa asetettu tiedoksisaaja", scheduledDate))
                .collect(Collectors.toList()));
    }

    @Transactional
    public void unlockDecision(final DecisionUnlockDTO dto) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(dto.getId(),
                EntityPermission.UPDATE);
        decision.assertHandler(activeUserService.requireActiveUser());

        final PermitDecisionAction action = new PermitDecisionAction();
        action.setPermitDecision(decision);
        action.setPointOfTime(DateUtil.now());
        action.setText(dto.getUnlockReason());
        action.setActionType(DecisionActionType.MUU);
        permitDecisionActionRepository.save(action);

        decision.setStatusDraft();
        cancelRevisions(decision);
    }

    private void cancelRevisions(final PermitDecision decision) {
        final List<PermitDecisionRevision> byPermitDecision =
                permitDecisionRevisionRepository.findByPermitDecision(decision);
        final List<PermitDecisionRevision> revsToCancel = byPermitDecision.stream()
                .filter(rev -> !rev.isCancelled())
                .collect(Collectors.toList());

        revsToCancel.forEach(rev -> rev.setCancelled(true));

        revsToCancel.stream()
                .flatMap(rev -> rev.getReceivers().stream())
                .filter(receiver -> !receiver.isCancelled() && receiver.getSentDate() == null)
                .forEach(receiver -> receiver.setCancelled(true));
    }

    @Nonnull
    private PersistentFileMetadata storeDecisionPdf(final PermitDecision permitDecision,
                                                    final String revisionExternalId, final String htmlPath) {
        final BiConsumer<PermitDecision, Path> validator = PermitDecisionRevisionFeature::validateDecisionPdf;
        return doStoreDecisionPdf(permitDecision, revisionExternalId, htmlPath, validator);
    }

    @Nonnull
    private PersistentFileMetadata storeDecisionPublicPdf(final PermitDecision permitDecision,
                                                          final String revisionExternalId, final String htmlPath) {
        final BiConsumer<PermitDecision, Path> validator = PermitDecisionRevisionFeature::validatePublicDecisionPdf;
        return doStoreDecisionPdf(permitDecision, revisionExternalId, htmlPath, validator);
    }

    private PersistentFileMetadata doStoreDecisionPdf(final PermitDecision permitDecision,
                                                      final String revisionExternalId,
                                                      final String htmlPath,
                                                      final BiConsumer<PermitDecision, Path> validator) {
        Preconditions.checkArgument(StringUtils.hasText(revisionExternalId));

        try {
            final Path pdfPath = Files.createTempFile("decision", null);
            final String permitNumber = permitDecision.createPermitNumber();
            final String headerRight = permitNumber + " (" + revisionExternalId + ")";

            pdfExportFactory.create()
                    .withHeaderRight(headerRight)
                    .withHtmlPath(htmlPath)
                    .build()
                    .export(pdfPath);
            validator.accept(permitDecision, pdfPath);

            return fileStorageService.storeFile(UUID.randomUUID(), pdfPath.toFile(), FileType.DECISION_PDF,
                    MediaTypeExtras.APPLICATION_PDF_VALUE, null);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public ResponseEntity<byte[]> getAttachment(final long decisionId, final long attachmentId) throws IOException {
        final PermitDecisionRevisionAttachment attachment =
                permitDecisionRevisionAttachmentRepository.getOne(attachmentId);
        final PermitDecision permitDecision = attachment.getDecisionAttachment().getPermitDecision();
        Preconditions.checkArgument(permitDecision.getId() == decisionId, "Attachment must match decision");
        activeUserService.assertHasPermission(permitDecision, EntityPermission.READ);

        return fileDownloadService.download(attachment.getDecisionAttachment().getAttachmentMetadata());
    }

    @Transactional
    public long updateViewCountAndResolveRevisionIdByReceiverUuid(final UUID uuid) {
        final PermitDecisionRevisionReceiver receiver = permitDecisionRevisionReceiverRepository.findByUuid(uuid);
        receiver.setViewCount(receiver.getViewCount() + 1);
        return receiver.getDecisionRevision().getId();
    }

    @Transactional(readOnly = true)
    public long resolveRevisionIdByReceiverUuid(final UUID uuid) {
        final PermitDecisionRevisionReceiver receiver = permitDecisionRevisionReceiverRepository.findByUuid(uuid);
        return receiver.getDecisionRevision().getId();
    }

    @Transactional
    public PermitDecisionRevisionDTO updatePosted(final long decisionId, final long revisionId, final boolean posted) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.UPDATE);
        final PermitDecisionRevision revision = permitDecisionRevisionRepository.getOne(revisionId);
        Preconditions.checkState(revision.isPostalByMail(), "Revision should be delivered by mail, but is by email");
        if (posted) {
            Preconditions.checkState(revision.getPostedByMailDate() == null, "Revision is already set as posted");
            Preconditions.checkState(revision.getScheduledPublishDate().isBefore(DateUtil.now()),
                    "Trying to publish before scheduled publish date");

            revision.setPostedByMailDate(DateUtil.now());
            revision.setPostedByMailUsername(activeUserService.requireActiveUser().getFullName());

            // Zero amount permit decision invoices should not exist
            if (decision.isPaymentAmountPositive()) {
                final Invoice invoice = permitDecisionInvoiceRepository.findByDecision(decision)
                        .map(PermitDecisionInvoice::getInvoice)
                        .orElseThrow(() -> new IllegalStateException("Decision invoice missing for decision id:" + decisionId));

                if (invoice.getState() == InvoiceState.CREATED) {
                    invoice.setState(InvoiceState.DELIVERED);
                } else {
                    LOG.warn("Revision is set to posted, but invoice is in state " + invoice.getState());
                }
            }
        } else {
            Preconditions.checkState(revision.getPostedByMailDate() != null, "Revision is already set as not posted");
            revision.setPostedByMailDate(null);
        }

        final Map<Long, String> moderatorIndex =
                userRepository.getModeratorFullNames(Collections.singletonList(revision));
        return PermitDecisionRevisionDTO.create(revision, moderatorIndex.get(revision.getCreatedByUserId()));
    }

    private boolean shouldGeneratePublicPdf(final PermitDecision permitDecision) {

        return permitDecision.getDecisionType() == HARVEST_PERMIT
                && isCarnivorePermitDecision(permitDecision);
    }

    private boolean isCarnivorePermitDecision(final PermitDecision permitDecision) {
        final String permitTypeCode = permitDecision.getPermitTypeCode();

        // Damage based permits may have other mammals granted so species amounts must be
        // checked for those.
        return PermitTypeCode.CARNIVORE_PERMIT_CODES.contains(permitTypeCode)
                && (permitTypeCode != MAMMAL_DAMAGE_BASED || hasCarnivoreSpecies(permitDecision) );
    }

    private boolean hasCarnivoreSpecies(final PermitDecision permitDecision) {
        // Large carnivore permits should only have single granted species so all should match
        return permitDecisionSpeciesAmountRepository.findByPermitDecision(permitDecision).stream()
                .map(PermitDecisionSpeciesAmount::getGameSpecies)
                .map(GameSpecies::getOfficialCode)
                .allMatch(GameSpecies::isLargeCarnivore);
    }

    private static void validateDecisionPdf(final PermitDecision permitDecision, final Path pdfPath) {
        final String textContent = PDFUtil.extractAllText(pdfPath.toFile());

        // Check postal code since it should include no white space and should be included in the
        // pdf text when pdf generation was successful.
        final String postalCode = permitDecision.getDeliveryAddress().getPostalCode();
        if (!textContent.contains(postalCode)) {
            throw new IllegalStateException("Recipient information missing");
        }
    }

    private static void validatePublicDecisionPdf(final PermitDecision notUsed, final Path pdfPath) {
        final String textContent = PDFUtil.extractAllText(pdfPath.toFile());

        checkState(textContent.split(BLANKED_INFO).length > 5,
                "PDF text must contain blanked out info five times.");
    }
}

package fi.riista.feature.permit.decision.informationrequest;

import com.github.jknack.handlebars.Handlebars;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.common.decision.DecisionUtil;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.mail.MailService;
import fi.riista.feature.permit.DocumentNumberUtil;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.archive.PermitApplicationArchive;
import fi.riista.feature.permit.application.archive.PermitApplicationArchiveRepository;
import fi.riista.feature.permit.decision.DecisionInformationLinkDTO;
import fi.riista.feature.permit.decision.DecisionInformationPublishingDTO;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionRepository;
import fi.riista.feature.permit.decision.PublishDecisionInformationDTO;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevisionAttachmentRepository;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevisionRepository;
import fi.riista.feature.permit.zip.OmaRiistaDecisionAttachmentsZip;
import fi.riista.feature.permit.zip.OmaRiistaDecisionAttachmentsZipBuilder;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.feature.storage.metadata.PersistentFileMetadataRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.validation.FinnishHuntingPermitNumberValidator;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static fi.riista.feature.permit.decision.PermitDecisionAuthorization.Permission.HANDLE_INFORMATION_REQUEST_LINK;


@Service
public class PermitDecisionInformationRequestFeature {
    private static final Logger LOG = LoggerFactory.getLogger(PermitDecisionInformationRequestFeature.class);

    public static final int VALID_UNTIL_DAYS = 33;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private InformationRequestLinkRepository informationRequestLinkRepository;

    @Resource
    private InformationRequestLogRepository informationRequestLogRepository;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private PermitDecisionRevisionRepository decisionRevisionRepository;

    @Resource
    private PersistentFileMetadataRepository persistentFileMetadataRepository;

    @Resource
    private PermitApplicationArchiveRepository permitApplicationArchiveRepository;

    @Resource
    private PermitDecisionRepository permitDecisionRepository;

    @Resource
    private MailService mailService;

    @Resource
    private Handlebars handlebars;

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    @Transactional
    public InformationRequestLink createAndSendInformationRequestLink(final PublishDecisionInformationDTO createDto) {

        final PermitDecision decision = requireEntityService.requirePermitDecision(createDto.getId(), HANDLE_INFORMATION_REQUEST_LINK);

        final DateTime validUntil = DateTime.now().plusDays(VALID_UNTIL_DAYS);
        InformationRequestLink link = InformationRequestLink.create(
                getLinkKey(),
                decision,
                createDto.getRecipientEmail(),
                createDto.getRecipientName(),
                createDto.getLinkType(),
                validUntil,
                createDto.getTitle(),
                createDto.getDescription());
        link = informationRequestLinkRepository.save(link);


        final List<HarvestPermit> permits = harvestPermitRepository.findByPermitDecision(decision);
        final String permitNumber;
        if (permits.size() > 0) {
            permitNumber = permits.get(0).getPermitNumber();
        } else {
            permitNumber = decision.createPermitNumber();
        }

        sendInformationRequestEmail(createDto, validUntil, link, permitNumber);

        return link;
    }

    @Transactional(readOnly = true)
    public List<DecisionInformationPublishingDTO> getInformationRequestsStatistics(final long decisionId) {
        // all who has permission to read decision can see the statistics
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.READ);
        return informationRequestLinkRepository.getDecisionLinkList(decision);
    }

    @Transactional
    public void invalidateInformationRequestLink(final long decisionId, final long linkId) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, HANDLE_INFORMATION_REQUEST_LINK);

        informationRequestLinkRepository.findById(linkId).ifPresent(link -> {
            // validate linkId
            if (link.getPermitDecision().getId() != null &&
                    link.getPermitDecision().getId().equals(decision.getId())) {
                // just remove recipient info and invalidate link
                clearLinkRecipientData(link);
                link.setValidUntil(DateTime.now().minusHours(1));
                informationRequestLinkRepository.saveAndFlush(link);
            }
        });
        // corner case where two moderators invalidates the same link at the same time. No need for logging
    }

    @Transactional(rollbackFor = IOException.class)
    public ResponseEntity<byte[]> downloadPublicCarnivoreDecisionThroughInformationRequestNoAuthentication(final HttpServletResponse response,
                                                                                                           final String linkKey,
                                                                                                           final String documentNumber,
                                                                                                           final Locale locale) throws IOException {
        if (!FinnishHuntingPermitNumberValidator.validate(documentNumber, true)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            LOG.warn(
                    String.format("InvalidateInformationRequestLink, Invalid document number. Link key: %s, document number: %s", linkKey, documentNumber)
            );
            return null;
        }

        final int decisionNumber = DocumentNumberUtil.extractOrderNumber(documentNumber);

        final PermitDecision decision = permitDecisionRepository.findByDecisionNumber(decisionNumber);
        if (decision == null) {
            LOG.warn(
                    String.format("InvalidateInformationRequestLink, Decision not found. Link key: %s, document number: %s", linkKey, documentNumber)
            );
            return null;
        }

        final DecisionInformationLinkDTO link = informationRequestLinkRepository.getValidLinkIdByLinkKey(linkKey, decision);
        if (link == null) {
            LOG.warn(
                    String.format("InvalidateInformationRequestLink, Valid link not found. Link key: %s, document number: %s", linkKey, documentNumber)
            );
            return null;
        }

        final List<PersistentFileMetadata> zipContent = new ArrayList<>();

        if (link.getLinkType().equals(InformationRequestLinkType.APPLICATION) ||
                link.getLinkType().equals(InformationRequestLinkType.APPLICATION_AND_DECISION)) {
            // application
            addOriginalApplicationArchiveToZip(decision, zipContent);
        }

        if (link.getLinkType().equals(InformationRequestLinkType.DECISION) ||
                link.getLinkType().equals(InformationRequestLinkType.APPLICATION_AND_DECISION)) {
            // latest decision
            addLatestDecisionToZip(documentNumber, locale, decisionNumber, zipContent);

            // decision attachments
            addLatestDecisionAttachmentsToZip(decisionNumber, zipContent);
        }


        final OmaRiistaDecisionAttachmentsZip attachmentsZip = new OmaRiistaDecisionAttachmentsZipBuilder(fileStorageService, locale)
                .withAttachments(zipContent)
                .withDecisionNumber(documentNumber)
                .build();

        response.setStatus(HttpServletResponse.SC_OK);
        final HttpHeaders headers = ContentDispositionUtil.header(attachmentsZip.getFilename());
        response.setHeader(ContentDispositionUtil.CONTENT_DISPOSITION, headers.getContentDisposition().toString());

        writeLinkOpeningLog(decision, link.getId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .body(attachmentsZip.getData());
    }

    protected void clearLinkRecipientData(final InformationRequestLink link) {
        link.setRecipientName("");
        link.setRecipientEmail("");
    }

    private void sendInformationRequestEmail(final PublishDecisionInformationDTO createDto,
                                             final DateTime validUntil,
                                             final InformationRequestLink link,
                                             final String permitNumber) {
        final Set<String> recipients = new HashSet<>(Collections.singletonList(createDto.getRecipientEmail()));
        mailService.send(new InformationRequestEmail(handlebars, runtimeEnvironmentUtil)
                .withLink(link)
                .withPermitNumber(permitNumber)
                .withValidUntil(validUntil)
                .withRecipients(recipients)
                .build(mailService.getDefaultFromAddress()));
    }

    private void writeLinkOpeningLog(final PermitDecision decision, final Long linkId) {
        final InformationRequestLog log = InformationRequestLog.create(
                decision,
                decision.getPermitTypeCode(),
                linkId
        );
        informationRequestLogRepository.save(log);
    }

    private void addOriginalApplicationArchiveToZip(final PermitDecision decision, final List<PersistentFileMetadata> zipContent) {
        final HarvestPermitApplication application = decision.getApplication();

        permitApplicationArchiveRepository.findByHarvestPermitApplication(application)
                .stream().min(Comparator.comparing(PermitApplicationArchive::getCreationTime))
                .ifPresent(applicationArchive -> {
                    final String prefix = HarvestPermitApplication.FILENAME_PREFIX.getAnyTranslation(LocaleContextHolder.getLocale());
                    final String fileName = String.format("%s-%d.zip", prefix, application.getApplicationNumber());
                    applicationArchive.getFileMetadata().setOriginalFilename(fileName);
                    zipContent.add(applicationArchive.getFileMetadata());
                });
    }

    private void addLatestDecisionToZip(final String documentNumber, final Locale locale, final int decisionNumber, final List<PersistentFileMetadata> zipContent) {
        decisionRevisionRepository.findLatestDecisionMetadataForInformationRequest(decisionNumber).ifPresent(metadata -> {
            final String filename = DecisionUtil.getPermitDecisionFileName(locale, documentNumber);
            metadata.setOriginalFilename(filename);
            zipContent.add(metadata);
        });
    }

    private void addLatestDecisionAttachmentsToZip(final int decisionNumber, final List<PersistentFileMetadata> zipContent) {
        zipContent.addAll(persistentFileMetadataRepository.findLatestDecisionAttachmentsMetadataForInformationRequest(decisionNumber));
    }

    private String getLinkKey() {
        return UUID.randomUUID().toString();
    }
}

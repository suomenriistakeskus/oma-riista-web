package fi.riista.feature.common.decision.nomination;

import fi.riista.feature.common.decision.AppealStatus;
import fi.riista.feature.common.decision.DecisionStatus;
import fi.riista.feature.common.decision.ModeratorDTO;
import fi.riista.feature.common.decision.nomination.document.NominationDecisionCompleteStatus;
import fi.riista.feature.common.decision.nomination.document.NominationDecisionDocument;
import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.permit.application.DeliveryAddressDTO;
import fi.riista.util.DateUtil;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;
import java.util.Locale;

import static java.util.Optional.ofNullable;

public class NominationDecisionDTO extends BaseEntityDTO<Long> {
    private Long id;
    private Integer rev;
    private int decisionNumber;
    private String nominationDecisionNumber;
    private DecisionStatus status;
    private ModeratorDTO handler;
    private boolean userIsHandler;

    private PersonContactInfoDTO contactPerson;
    private OrganisationNameDTO rhy;

    private LocalDateTime lockedDate;
    private LocalDateTime publishDate;
    private Locale locale;
    private Long referenceId;
    private DeliveryAddressDTO deliveryAddress;
    private NominationDecision.NominationDecisionType decisionType;
    private OccupationType occupationType;
    private AppealStatus appealStatus;
    private NominationDecisionDocument document;
    private NominationDecisionCompleteStatus completeStatus;
    private LocalDate proposalDate;

    private boolean canDelete;

    public static NominationDecisionDTO create(final @Nonnull NominationDecision decision,
                                               final @Nonnull NominationDecisionDocument document,
                                               final boolean userIsHandler,
                                               final boolean canDelete) {
        final NominationDecisionDTO dto = new NominationDecisionDTO();


        DtoUtil.copyBaseFields(decision, dto);
        dto.setDecisionNumber(decision.getDecisionNumber());
        dto.setStatus(decision.getStatus());
        dto.setLockedDate(DateUtil.toLocalDateTimeNullSafe(decision.getLockedDate()));
        dto.setPublishDate(DateUtil.toLocalDateTimeNullSafe(decision.getPublishDate()));
        dto.setLocale(decision.getLocale());
        dto.setDocument(document);
        dto.setCompleteStatus(decision.getCompleteStatus());
        dto.setReferenceId(F.getId(decision.getReference()));
        dto.setDeliveryAddress(DeliveryAddressDTO.from(decision.getDeliveryAddress()));
        dto.setDecisionType(decision.getDecisionType());
        dto.setOccupationType(decision.getOccupationType());
        dto.setAppealStatus(decision.getAppealStatus());

        dto.setHandler(ofNullable(decision.getHandler())
                .map(ModeratorDTO::new)
                .orElse(null));

        dto.setUserIsHandler(userIsHandler);

        dto.setContactPerson(ofNullable(decision.getContactPerson())
                .map(PersonContactInfoDTO::create)
                .orElse(null));

        dto.setRhy(OrganisationNameDTO.createWithOfficialCode(decision.getRhy()));


        dto.setNominationDecisionNumber(decision.createDocumentNumber());
        dto.setProposalDate(decision.getProposalDate());

        dto.setCanDelete(canDelete);

        return dto;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public int getDecisionNumber() {
        return decisionNumber;
    }

    public void setDecisionNumber(final int decisionNumber) {
        this.decisionNumber = decisionNumber;
    }

    public String getNominationDecisionNumber() {
        return nominationDecisionNumber;
    }

    public void setNominationDecisionNumber(final String nominationDecisionNumber) {
        this.nominationDecisionNumber = nominationDecisionNumber;
    }

    public DecisionStatus getStatus() {
        return status;
    }

    public void setStatus(final DecisionStatus status) {
        this.status = status;
    }

    public ModeratorDTO getHandler() {
        return handler;
    }

    public void setHandler(final ModeratorDTO handler) {
        this.handler = handler;
    }

    public boolean isUserIsHandler() {
        return userIsHandler;
    }

    public void setUserIsHandler(final boolean userIsHandler) {
        this.userIsHandler = userIsHandler;
    }

    public PersonContactInfoDTO getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(final PersonContactInfoDTO contactPerson) {
        this.contactPerson = contactPerson;
    }

    public OrganisationNameDTO getRhy() {
        return rhy;
    }

    public void setRhy(final OrganisationNameDTO rhy) {
        this.rhy = rhy;
    }

    public LocalDateTime getLockedDate() {
        return lockedDate;
    }

    public void setLockedDate(final LocalDateTime lockedDate) {
        this.lockedDate = lockedDate;
    }

    public LocalDateTime getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(final LocalDateTime publishDate) {
        this.publishDate = publishDate;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(final Locale locale) {
        this.locale = locale;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(final Long referenceId) {
        this.referenceId = referenceId;
    }

    public DeliveryAddressDTO getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(final DeliveryAddressDTO deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public NominationDecision.NominationDecisionType getDecisionType() {
        return decisionType;
    }

    public void setDecisionType(final NominationDecision.NominationDecisionType decisionType) {
        this.decisionType = decisionType;
    }

    public AppealStatus getAppealStatus() {
        return appealStatus;
    }

    public void setAppealStatus(final AppealStatus appealStatus) {
        this.appealStatus = appealStatus;
    }

    public OccupationType getOccupationType() {
        return occupationType;
    }

    public void setOccupationType(final OccupationType occupationType) {
        this.occupationType = occupationType;
    }

    public NominationDecisionDocument getDocument() {
        return document;
    }

    public void setDocument(final NominationDecisionDocument document) {
        this.document = document;
    }

    public NominationDecisionCompleteStatus getCompleteStatus() {
        return completeStatus;
    }

    public void setCompleteStatus(final NominationDecisionCompleteStatus completeStatus) {
        this.completeStatus = completeStatus;
    }

    public LocalDate getProposalDate() {
        return proposalDate;
    }

    public void setProposalDate(final LocalDate proposalDate) {
        this.proposalDate = proposalDate;
    }

    public boolean isCanDelete() {
        return canDelete;
    }

    public void setCanDelete(final boolean canDelete) {
        this.canDelete = canDelete;
    }
}

package fi.riista.feature.permit.decision;

import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.organization.person.PersonWithNameDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.util.F;
import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;

public class PermitDecisionDTO extends BaseEntityDTO<Long> {
    private Long id;
    private Integer rev;
    private Long applicationId;
    private Integer applicationNumber;
    private Long harvestPermitId;
    private String harvestPermitNumber;
    private PermitDecision.Status status;
    private PersonWithNameDTO handler;
    private boolean userIsHandler;

    private PersonContactInfoDTO contactPerson;
    private OrganisationNameDTO permitHolder;

    private PermitDecisionDocument document;
    private PermitDecisionCompleteStatus completeStatus;
    private BigDecimal paymentAmount;
    private LocalDateTime lockedDate;
    private LocalDateTime publishDate;
    private Locale locale;
    private Long referenceId;
    private boolean deliveryByMail;
    private PermitDecision.GrantStatus grantStatus;
    private HarvestPermitApplication.Status applicationStatus;

    public static PermitDecisionDTO create(final @Nonnull PermitDecision decision,
                                           final @Nonnull PermitDecisionDocument document,
                                           final HarvestPermit harvestPermit,
                                           final boolean userIsHandler) {
        final PermitDecisionDTO dto = new PermitDecisionDTO();

        final HarvestPermitApplication application = decision.getApplication();

        dto.setId(decision.getId());
        dto.setRev(decision.getConsistencyVersion());
        dto.setApplicationId(F.getId(application));
        dto.setApplicationNumber(application.getApplicationNumber());
        dto.setDocument(document);
        dto.setCompleteStatus(decision.getCompleteStatus());
        dto.setPaymentAmount(decision.getPaymentAmount());
        dto.setStatus(decision.getStatus());
        dto.setLockedDate(decision.getLockedDate() != null ? decision.getLockedDate().toLocalDateTime() : null);
        dto.setPublishDate(decision.getPublishDate() != null ? decision.getPublishDate().toLocalDateTime() : null);
        dto.setLocale(decision.getLocale());
        dto.setReferenceId(F.getId(decision.getReference()));
        dto.setDeliveryByMail(Boolean.TRUE.equals(application.getDeliveryByMail()));
        dto.setGrantStatus(decision.getGrantStatus());
        dto.setApplicationStatus(application.getStatus());

        if (decision.getHandler() != null) {
            final PersonWithNameDTO handlerDTO = new PersonWithNameDTO();
            handlerDTO.setByName(decision.getHandler().getFirstName());
            handlerDTO.setLastName(decision.getHandler().getLastName());
            dto.setHandler(handlerDTO);
        }
        dto.setUserIsHandler(userIsHandler);

        if (decision.getContactPerson() != null) {
            dto.setContactPerson(PersonContactInfoDTO.create(decision.getContactPerson()));
        }

        dto.setPermitHolder(Optional.ofNullable(decision.getPermitHolder())
                .map(OrganisationNameDTO::createWithOfficialCode).orElse(null));

        if (harvestPermit != null) {
            dto.setHarvestPermitId(harvestPermit.getId());
            dto.setHarvestPermitNumber(harvestPermit.getPermitNumber());
        }

        return dto;
    }

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return this.rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public PermitDecision.Status getStatus() {
        return status;
    }

    public void setStatus(final PermitDecision.Status status) {
        this.status = status;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(final Long applicationId) {
        this.applicationId = applicationId;
    }

    public Integer getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(final Integer applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public Long getHarvestPermitId() {
        return harvestPermitId;
    }

    public void setHarvestPermitId(final Long harvestPermitId) {
        this.harvestPermitId = harvestPermitId;
    }

    public String getHarvestPermitNumber() {
        return harvestPermitNumber;
    }

    public void setHarvestPermitNumber(final String harvestPermitNumber) {
        this.harvestPermitNumber = harvestPermitNumber;
    }

    public PersonWithNameDTO getHandler() {
        return handler;
    }

    public void setHandler(final PersonWithNameDTO handler) {
        this.handler = handler;
    }

    public boolean isUserIsHandler() {
        return userIsHandler;
    }

    public void setUserIsHandler(final boolean userIsHandler) {
        this.userIsHandler = userIsHandler;
    }

    public OrganisationNameDTO getPermitHolder() {
        return permitHolder;
    }

    public void setPermitHolder(final OrganisationNameDTO permitHolder) {
        this.permitHolder = permitHolder;
    }

    public PersonContactInfoDTO getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(final PersonContactInfoDTO contactPerson) {
        this.contactPerson = contactPerson;
    }

    public PermitDecisionDocument getDocument() {
        return document;
    }

    public void setDocument(final PermitDecisionDocument document) {
        this.document = document;
    }

    public PermitDecisionCompleteStatus getCompleteStatus() {
        return completeStatus;
    }

    public void setCompleteStatus(final PermitDecisionCompleteStatus completeStatus) {
        this.completeStatus = completeStatus;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(final BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
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

    public boolean isDeliveryByMail() {
        return deliveryByMail;
    }

    public void setDeliveryByMail(final boolean deliveryByMail) {
        this.deliveryByMail = deliveryByMail;
    }

    public PermitDecision.GrantStatus getGrantStatus() {
        return grantStatus;
    }

    public void setGrantStatus(final PermitDecision.GrantStatus grantStatus) {
        this.grantStatus = grantStatus;
    }

    public HarvestPermitApplication.Status getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(final HarvestPermitApplication.Status applicationStatus) {
        this.applicationStatus = applicationStatus;
    }
}

package fi.riista.feature.permit.decision;

import fi.riista.feature.common.decision.AppealStatus;
import fi.riista.feature.common.decision.DecisionStatus;
import fi.riista.feature.common.decision.GrantStatus;
import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.organization.person.PersonWithNameDTO;
import fi.riista.feature.permit.application.DeliveryAddressDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.decision.PermitDecision.DecisionType;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.NumberUtils;
import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class PermitDecisionDTO extends BaseEntityDTO<Long> {
    private Long id;
    private Integer rev;
    private Long applicationId;
    private Integer applicationNumber;
    private String permitTypeCode;
    private HarvestPermitCategory harvestPermitCategory;
    private List<PermitDecisionPermitDTO> permits;
    private String permitDecisionNumber;
    private DecisionStatus status;
    private PersonWithNameDTO handler;
    private boolean userIsHandler;

    private PersonContactInfoDTO contactPerson;
    private PermitHolderDTO permitHolder;
    private OrganisationNameDTO huntingClub;

    private PermitDecisionDocument document;
    private PermitDecisionCompleteStatus completeStatus;
    private BigDecimal paymentAmount;
    private LocalDateTime lockedDate;
    private LocalDateTime publishDate;
    private Locale locale;
    private Long referenceId;
    private boolean deliveryByMail;
    private DeliveryAddressDTO deliveryAddress;
    private DecisionType decisionType;
    private GrantStatus grantStatus;
    private AppealStatus appealStatus;
    private HarvestPermitApplication.Status applicationStatus;
    private boolean hasDecisionInvoice;
    private boolean hasHarvestInvoices;

    public static PermitDecisionDTO create(final @Nonnull PermitDecision decision,
                                           final @Nonnull PermitDecisionDocument document,
                                           final List<PermitDecisionPermitDTO> permits,
                                           final boolean userIsHandler) {
        final PermitDecisionDTO dto = new PermitDecisionDTO();

        final HarvestPermitApplication application = decision.getApplication();

        dto.setId(decision.getId());
        dto.setRev(decision.getConsistencyVersion());
        dto.setApplicationId(F.getId(application));
        dto.setApplicationNumber(application.getApplicationNumber());
        dto.setPermitTypeCode(decision.getPermitTypeCode());
        dto.setHarvestPermitCategory(application.getHarvestPermitCategory());
        dto.setDocument(document);
        dto.setCompleteStatus(decision.getCompleteStatus());
        dto.setPaymentAmount(decision.getPaymentAmount());
        dto.setStatus(decision.getStatus());
        dto.setLockedDate(DateUtil.toLocalDateTimeNullSafe(decision.getLockedDate()));
        dto.setPublishDate(DateUtil.toLocalDateTimeNullSafe(decision.getPublishDate()));
        dto.setLocale(decision.getLocale());
        dto.setReferenceId(F.getId(decision.getReference()));
        dto.setDeliveryByMail(Boolean.TRUE.equals(application.getDeliveryByMail()));
        dto.setDeliveryAddress(DeliveryAddressDTO.from(decision.getDeliveryAddress()));
        dto.setDecisionType(decision.getDecisionType());
        dto.setGrantStatus(decision.getGrantStatus());
        dto.setAppealStatus(decision.getAppealStatus());
        dto.setApplicationStatus(application.getStatus());

        dto.setHandler(Optional
                .ofNullable(decision.getHandler())
                .map(handler -> {
                    final PersonWithNameDTO handlerDTO = new PersonWithNameDTO();
                    handlerDTO.setByName(handler.getFirstName());
                    handlerDTO.setLastName(handler.getLastName());
                    return handlerDTO;
                })
                .orElse(null));
        dto.setUserIsHandler(userIsHandler);

        dto.setContactPerson(Optional
                .ofNullable(decision.getContactPerson())
                .map(PersonContactInfoDTO::create)
                .orElse(null));

        dto.setPermitHolder(Optional.ofNullable(decision.getPermitHolder())
                .map(PermitHolderDTO::createFrom).orElse(null));

        dto.setHuntingClub(Optional.ofNullable(decision.getHuntingClub())
                .map(OrganisationNameDTO::createWithOfficialCode).orElse(null));

        dto.setPermitDecisionNumber(decision.createPermitNumber());
        dto.setPermits(permits);

        dto.setHasDecisionInvoice(NumberUtils.bigDecimalIsPositive(decision.getPaymentAmount()));

        dto.setHasHarvestInvoices(decision.getGrantStatus() != GrantStatus.REJECTED
                && decision.getDecisionType() == DecisionType.HARVEST_PERMIT
                && application.getHarvestPermitCategory().isMooselike());

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

    public DecisionStatus getStatus() {
        return status;
    }

    public void setStatus(final DecisionStatus status) {
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

    public String getPermitTypeCode() {
        return permitTypeCode;
    }

    public void setPermitTypeCode(final String permitTypeCode) {
        this.permitTypeCode = permitTypeCode;
    }

    public HarvestPermitCategory getHarvestPermitCategory() {
        return harvestPermitCategory;
    }

    public void setHarvestPermitCategory(final HarvestPermitCategory harvestPermitCategory) {
        this.harvestPermitCategory = harvestPermitCategory;
    }


    public List<PermitDecisionPermitDTO> getPermits() {
        return permits;
    }

    public void setPermits(final List<PermitDecisionPermitDTO> permits) {
        this.permits = permits;
    }

    public String getPermitDecisionNumber() {
        return permitDecisionNumber;
    }

    public void setPermitDecisionNumber(final String permitDecisionNumber) {
        this.permitDecisionNumber = permitDecisionNumber;
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

    public PermitHolderDTO getPermitHolder() {
        return permitHolder;
    }

    public void setPermitHolder(final PermitHolderDTO permitHolder) {
        this.permitHolder = permitHolder;
    }

    public OrganisationNameDTO getHuntingClub() {
        return huntingClub;
    }

    public void setHuntingClub(final OrganisationNameDTO huntingClub) {
        this.huntingClub = huntingClub;
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

    public DeliveryAddressDTO getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(DeliveryAddressDTO deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public DecisionType getDecisionType() {
        return decisionType;
    }

    public void setDecisionType(final DecisionType decisionType) {
        this.decisionType = decisionType;
    }

    public GrantStatus getGrantStatus() {
        return grantStatus;
    }

    public void setGrantStatus(final GrantStatus grantStatus) {
        this.grantStatus = grantStatus;
    }

    public AppealStatus getAppealStatus() {
        return appealStatus;
    }

    public void setAppealStatus(final AppealStatus appealStatus) {
        this.appealStatus = appealStatus;
    }

    public HarvestPermitApplication.Status getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(final HarvestPermitApplication.Status applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public boolean isHasDecisionInvoice() {
        return hasDecisionInvoice;
    }

    public void setHasDecisionInvoice(final boolean hasDecisionInvoice) {
        this.hasDecisionInvoice = hasDecisionInvoice;
    }

    public boolean isHasHarvestInvoices() {
        return hasHarvestInvoices;
    }

    public void setHasHarvestInvoices(final boolean hasHarvestInvoices) {
        this.hasHarvestInvoices = hasHarvestInvoices;
    }
}

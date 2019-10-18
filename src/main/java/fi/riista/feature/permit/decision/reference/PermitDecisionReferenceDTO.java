package fi.riista.feature.permit.decision.reference;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.organization.person.PersonWithNameDTO;
import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionDocument;

import javax.annotation.Nullable;

public class PermitDecisionReferenceDTO extends BaseEntityDTO<Long> {
    private Long id;
    private Integer rev;
    private Integer applicationNumber;
    private PersonWithNameDTO contactPerson;
    private PermitHolderDTO permitHolder;
    private PersonWithNameDTO handler;
    private PermitDecision.Status decisionStatus;
    private PermitDecision.GrantStatus grantStatus;

    private PermitDecisionDocument document;

    public static PermitDecisionReferenceDTO create(final @Nullable PermitDecision decision) {
        if (decision == null) {
            return null;
        }

        final PermitDecisionReferenceDTO dto = new PermitDecisionReferenceDTO();

        dto.setId(decision.getId());
        dto.setRev(decision.getConsistencyVersion());
        dto.setApplicationNumber(decision.getDecisionNumber());
        dto.setContactPerson(PersonWithNameDTO.create(decision.getContactPerson()));
        dto.setPermitHolder(decision.getPermitHolder() != null ? PermitHolderDTO.createFrom(decision.getPermitHolder()) : null);
        dto.setDecisionStatus(decision.getStatus());
        dto.setGrantStatus(decision.getGrantStatus());
        dto.setDocument(decision.getDocument() != null ? decision.getDocument() : new PermitDecisionDocument());

        final SystemUser h = decision.getHandler();
        if (h != null) {
            final PersonWithNameDTO handlerDTO = new PersonWithNameDTO();
            handlerDTO.setByName(h.getFirstName());
            handlerDTO.setLastName(h.getLastName());
            dto.setHandler(handlerDTO);
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

    public Integer getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(final Integer applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public PersonWithNameDTO getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(final PersonWithNameDTO contactPerson) {
        this.contactPerson = contactPerson;
    }

    public PermitHolderDTO getPermitHolder() {
        return permitHolder;
    }

    public void setPermitHolder(final PermitHolderDTO permitHolder) {
        this.permitHolder = permitHolder;
    }

    public PersonWithNameDTO getHandler() {
        return handler;
    }

    public void setHandler(final PersonWithNameDTO handler) {
        this.handler = handler;
    }

    public PermitDecision.Status getDecisionStatus() {
        return decisionStatus;
    }

    public void setDecisionStatus(final PermitDecision.Status decisionStatus) {
        this.decisionStatus = decisionStatus;
    }

    public PermitDecision.GrantStatus getGrantStatus() {
        return grantStatus;
    }

    public void setGrantStatus(final PermitDecision.GrantStatus grantStatus) {
        this.grantStatus = grantStatus;
    }

    public PermitDecisionDocument getDocument() {
        return document;
    }

    public void setDocument(final PermitDecisionDocument document) {
        this.document = document;
    }
}

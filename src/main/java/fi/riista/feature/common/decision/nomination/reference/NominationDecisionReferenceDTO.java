package fi.riista.feature.common.decision.nomination.reference;

import fi.riista.feature.common.decision.DecisionStatus;
import fi.riista.feature.common.decision.ModeratorDTO;
import fi.riista.feature.common.decision.nomination.NominationDecision;
import fi.riista.feature.common.decision.nomination.document.NominationDecisionDocument;
import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.organization.person.PersonWithNameDTO;
import fi.riista.util.DtoUtil;

import javax.annotation.Nullable;

import static java.util.Optional.ofNullable;

public class NominationDecisionReferenceDTO extends BaseEntityDTO<Long> {
    private Long id;
    private Integer rev;
    private Integer decisionNumber;
    private PersonWithNameDTO contactPerson;
    private ModeratorDTO handler;
    private DecisionStatus decisionStatus;

    private NominationDecisionDocument document;

    public static NominationDecisionReferenceDTO create(final @Nullable NominationDecision decision) {
        if (decision == null) {
            return null;
        }

        final NominationDecisionReferenceDTO dto = new NominationDecisionReferenceDTO();

        DtoUtil.copyBaseFields(decision, dto);
        dto.setDecisionNumber(decision.getDecisionNumber());
        dto.setContactPerson(PersonWithNameDTO.create(decision.getContactPerson()));
        dto.setDecisionStatus(decision.getStatus());
        dto.setDocument(decision.getDocument());

        dto.setHandler(ofNullable(decision.getHandler())
                .map(ModeratorDTO::new)
                .orElse(null));

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

    public Integer getDecisionNumber() {
        return decisionNumber;
    }

    public void setDecisionNumber(final Integer applicationNumber) {
        this.decisionNumber = applicationNumber;
    }

    public PersonWithNameDTO getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(final PersonWithNameDTO contactPerson) {
        this.contactPerson = contactPerson;
    }

    public ModeratorDTO getHandler() {
        return handler;
    }

    public void setHandler(final ModeratorDTO handler) {
        this.handler = handler;
    }

    public DecisionStatus getDecisionStatus() {
        return decisionStatus;
    }

    public void setDecisionStatus(final DecisionStatus decisionStatus) {
        this.decisionStatus = decisionStatus;
    }

    public NominationDecisionDocument getDocument() {
        return document;
    }

    public void setDocument(final NominationDecisionDocument document) {
        this.document = document;
    }
}

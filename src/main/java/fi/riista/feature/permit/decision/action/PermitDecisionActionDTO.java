package fi.riista.feature.permit.decision.action;

import fi.riista.feature.common.decision.DecisionActionCommunicationType;
import fi.riista.feature.common.decision.DecisionActionType;
import fi.riista.feature.common.dto.BaseEntityDTO;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDateTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class PermitDecisionActionDTO extends BaseEntityDTO<Long> {

    private Long id;
    private Integer rev;

    @NotNull
    private LocalDateTime pointOfTime;

    @NotNull
    private DecisionActionType actionType;

    private DecisionActionCommunicationType communicationType;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String text;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String decisionText;

    private long attachmentCount;

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

    public LocalDateTime getPointOfTime() {
        return pointOfTime;
    }

    public void setPointOfTime(final LocalDateTime pointOfTime) {
        this.pointOfTime = pointOfTime;
    }

    public DecisionActionType getActionType() {
        return actionType;
    }

    public void setActionType(final DecisionActionType actionType) {
        this.actionType = actionType;
    }

    public DecisionActionCommunicationType getCommunicationType() {
        return communicationType;
    }

    public void setCommunicationType(final DecisionActionCommunicationType communicationType) {
        this.communicationType = communicationType;
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public String getDecisionText() {
        return decisionText;
    }

    public void setDecisionText(final String decisionText) {
        this.decisionText = decisionText;
    }

    public long getAttachmentCount() {
        return attachmentCount;
    }

    public void setAttachmentCount(final long attachmentCount) {
        this.attachmentCount = attachmentCount;
    }
}

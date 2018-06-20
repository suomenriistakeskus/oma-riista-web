package fi.riista.feature.permit.decision.action;

import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.util.DtoUtil;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDateTime;

import javax.validation.constraints.NotNull;

public class PermitDecisionActionDTO extends BaseEntityDTO<Long> {

    private Long id;
    private Integer rev;

    @NotNull
    private LocalDateTime pointOfTime;

    @NotNull
    private PermitDecisionAction.ActionType actionType;

    private PermitDecisionAction.CommunicationType communicationType;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String text;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String decisionText;

    public static PermitDecisionActionDTO create(final PermitDecisionAction entity) {
        final PermitDecisionActionDTO dto = new PermitDecisionActionDTO();
        DtoUtil.copyBaseFields(entity, dto);

        dto.setPointOfTime(entity.getPointOfTime().toLocalDateTime());
        dto.setActionType(entity.getActionType());
        dto.setCommunicationType(entity.getCommunicationType());
        dto.setText(entity.getText());
        dto.setDecisionText(entity.getDecisionText());
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

    public LocalDateTime getPointOfTime() {
        return pointOfTime;
    }

    public void setPointOfTime(final LocalDateTime pointOfTime) {
        this.pointOfTime = pointOfTime;
    }

    public PermitDecisionAction.ActionType getActionType() {
        return actionType;
    }

    public void setActionType(final PermitDecisionAction.ActionType actionType) {
        this.actionType = actionType;
    }

    public PermitDecisionAction.CommunicationType getCommunicationType() {
        return communicationType;
    }

    public void setCommunicationType(final PermitDecisionAction.CommunicationType communicationType) {
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
}

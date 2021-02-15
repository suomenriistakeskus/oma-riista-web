package fi.riista.feature.common.decision.nomination.action;

import fi.riista.feature.common.decision.DecisionActionCommunicationType;
import fi.riista.feature.common.decision.DecisionActionType;
import fi.riista.feature.common.decision.nomination.NominationDecision;
import fi.riista.feature.common.decision.nomination.NominationDecision_;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.util.jpa.CriteriaUtils;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.DateTime;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.LinkedList;
import java.util.List;

@Entity
@Access(AccessType.FIELD)
public class NominationDecisionAction extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private NominationDecision nominationDecision;

    @NotNull
    @Column(nullable = false)
    private DateTime pointOfTime;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private DecisionActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column
    private DecisionActionCommunicationType communicationType;

    // Text describing the action, not visible on the decision document
    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String text;

    // Text describing the action, seen on the decision document
    @Column(columnDefinition = "TEXT")
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String decisionText;

    @OneToMany(mappedBy = "nominationDecisionAction")
    private List<NominationDecisionActionAttachment> attachments = new LinkedList<>();

    // Accessors -->

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nomination_decision_action_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public NominationDecision getNominationDecision() {
        return nominationDecision;
    }

    public void setNominationDecision(final NominationDecision nominationDecision) {
        CriteriaUtils.updateInverseCollection(NominationDecision_.actions, this, this.nominationDecision, nominationDecision);
        this.nominationDecision = nominationDecision;
    }

    public DateTime getPointOfTime() {
        return pointOfTime;
    }

    public void setPointOfTime(final DateTime pointOfTime) {
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

    /*package*/ List<NominationDecisionActionAttachment> getAttachments() {
        return attachments;
    }

}

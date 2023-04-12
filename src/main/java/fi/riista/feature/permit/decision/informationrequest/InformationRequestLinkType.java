package fi.riista.feature.permit.decision.informationrequest;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.permit.decision.PermitDecision;
import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static java.util.Objects.requireNonNull;

public enum InformationRequestLinkType {
    APPLICATION,
    DECISION,
    APPLICATION_AND_DECISION

}

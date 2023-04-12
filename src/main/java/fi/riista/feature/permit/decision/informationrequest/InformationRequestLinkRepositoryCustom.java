package fi.riista.feature.permit.decision.informationrequest;

import fi.riista.feature.common.entity.CreditorReference;
import fi.riista.feature.permit.decision.DecisionInformationLinkDTO;
import fi.riista.feature.permit.decision.DecisionInformationPublishingDTO;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.integration.mmm.transfer.AccountTransfer;

import javax.annotation.Nonnull;
import java.util.List;

public interface InformationRequestLinkRepositoryCustom {
    DecisionInformationLinkDTO getValidLinkIdByLinkKey(String linkKey, PermitDecision decision);

    List<DecisionInformationPublishingDTO> getDecisionLinkList(final PermitDecision decision);
}
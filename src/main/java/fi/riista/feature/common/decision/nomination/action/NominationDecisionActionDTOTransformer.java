package fi.riista.feature.common.decision.nomination.action;

import fi.riista.util.DtoUtil;
import fi.riista.util.ListTransformer;
import fi.riista.util.jpa.JpaGroupingUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Component
public class NominationDecisionActionDTOTransformer extends ListTransformer<NominationDecisionAction, NominationDecisionActionDTO> {

    @Resource
    private NominationDecisionActionAttachmentRepository actionAttachmentRepository;

    @Nonnull
    @Override
    protected List<NominationDecisionActionDTO> transform(@Nonnull final List<NominationDecisionAction> list) {
        final Map<NominationDecisionAction, List<NominationDecisionActionAttachment>> groupedAttachments = getAttachments(list);

        return list.stream()
                .map(entity -> {
                    final List<NominationDecisionActionAttachment> attachments = groupedAttachments.get(entity);

                    final NominationDecisionActionDTO dto = new NominationDecisionActionDTO();
                    DtoUtil.copyBaseFields(entity, dto);

                    dto.setPointOfTime(entity.getPointOfTime().toLocalDateTime());
                    dto.setActionType(entity.getActionType());
                    dto.setCommunicationType(entity.getCommunicationType());
                    dto.setText(entity.getText());
                    dto.setDecisionText(entity.getDecisionText());
                    dto.setAttachmentCount(attachments != null ? attachments.size() : 0);

                    return dto;
                })
                .collect(toList());
    }

    @Nonnull
    private Map<NominationDecisionAction, List<NominationDecisionActionAttachment>> getAttachments(final Collection<NominationDecisionAction> actions) {
        return JpaGroupingUtils.groupRelations(actions, NominationDecisionActionAttachment_.nominationDecisionAction, actionAttachmentRepository);
    }
}

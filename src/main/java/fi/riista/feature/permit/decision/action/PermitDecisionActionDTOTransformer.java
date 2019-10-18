package fi.riista.feature.permit.decision.action;

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
public class PermitDecisionActionDTOTransformer extends ListTransformer<PermitDecisionAction, PermitDecisionActionDTO> {

    @Resource
    private PermitDecisionActionAttachmentRepository permitDecisionActionAttachmentRepository;

    @Nonnull
    @Override
    protected List<PermitDecisionActionDTO> transform(@Nonnull final List<PermitDecisionAction> list) {
        final Map<PermitDecisionAction, List<PermitDecisionActionAttachment>> groupedAttachments = getAttachments(list);

        return list.stream()
                .map(entity -> {
                    final List<PermitDecisionActionAttachment> attachments = groupedAttachments.get(entity);

                    final PermitDecisionActionDTO dto = new PermitDecisionActionDTO();
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
    private Map<PermitDecisionAction, List<PermitDecisionActionAttachment>> getAttachments(final Collection<PermitDecisionAction> actions) {
        return JpaGroupingUtils.groupRelations(actions, PermitDecisionActionAttachment_.permitDecisionAction, permitDecisionActionAttachmentRepository);
    }
}

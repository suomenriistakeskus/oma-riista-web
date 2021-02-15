package fi.riista.feature.common.decision.nomination.revision;

import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.common.entity.HasID;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import fi.riista.util.ListTransformer;
import fi.riista.util.jpa.JpaGroupingUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static fi.riista.util.DateUtil.toLocalDateTimeNullSafe;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Component
public class NominationDecisionRevisionDTOTransformer extends ListTransformer<NominationDecisionRevision, NominationDecisionRevisionDTO> {

    @Resource
    private NominationDecisionRevisionAttachmentRepository nominationDecisionRevisionAttachmentRepository;

    @Resource
    private NominationDecisionRevisionReceiverRepository nominationDecisionRevisionReceiverRepository;

    @Resource
    private UserRepository userRepository;

    @Nonnull
    @Override
    protected List<NominationDecisionRevisionDTO> transform(@Nonnull final List<NominationDecisionRevision> list) {

        final Map<Long, String> moderatorIndex = userRepository.getModeratorFullNames(list);
        final Map<NominationDecisionRevision, Set<NominationDecisionRevisionDTO.AttachmentDTO>> attachments =
                nominationDecisionRevisionAttachmentRepository.findByNominationDecisionRevisionIn(list);
        final Map<NominationDecisionRevision, List<NominationDecisionRevisionReceiver>> receivers = getReceivers(list);

        return F.mapNonNullsToList(list, revision->{
            final NominationDecisionRevisionDTO dto = new NominationDecisionRevisionDTO();

            DtoUtil.copyBaseFields(revision, dto);
            dto.setExternalId(revision.getExternalId());
            dto.setLockedDate(toLocalDateTimeNullSafe(revision.getLockedDate()));
            dto.setLockedByUsername(moderatorIndex.get(revision.getCreatedByUserId()));
            dto.setScheduledPublishDate(toLocalDateTimeNullSafe(revision.getScheduledPublishDate()));
            dto.setPublishDate(toLocalDateTimeNullSafe(revision.getPublishDate()));
            dto.setCanTogglePosted(!revision.isCancelled() && revision.getPublishDate() != null);
            dto.setPosted(revision.getPostedByMailDate() != null);
            dto.setPostedByMailDate(toLocalDateTimeNullSafe(revision.getPostedByMailDate()));
            dto.setPostedByMailUsername(revision.getPostedByMailUsername());

            dto.setAttachments(attachments.getOrDefault(revision, emptySet()).stream()
                    .sorted()
                    .collect(toList()));

            dto.setReceivers(receivers.getOrDefault(revision, emptyList()).stream()
                    .sorted(comparing(HasID::getId))
                    .map(NominationDecisionRevisionDTO.ReceiverDTO::new)
                    .collect(toList()));

            return dto;
        });
    }

    private Map<NominationDecisionRevision, List<NominationDecisionRevisionReceiver>> getReceivers(final List<NominationDecisionRevision> list){
        return JpaGroupingUtils.groupRelations(list, NominationDecisionRevisionReceiver_.decisionRevision, nominationDecisionRevisionReceiverRepository);
    }
}

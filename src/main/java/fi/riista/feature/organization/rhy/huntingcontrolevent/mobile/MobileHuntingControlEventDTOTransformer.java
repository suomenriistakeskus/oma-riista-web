package fi.riista.feature.organization.rhy.huntingcontrolevent.mobile;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlAttachment;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlCooperationType;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEvent;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventChange;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventDTOTransformerHelper;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.feature.storage.metadata.PersistentFileMetadataRepository;
import fi.riista.util.ListTransformer;
import fi.riista.util.jpa.CriteriaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@Component
public class MobileHuntingControlEventDTOTransformer extends ListTransformer<HuntingControlEvent, MobileHuntingControlEventDTO> {

    private static final Logger LOG = LoggerFactory.getLogger(MobileHuntingControlEventDTOTransformer.class);

    @Resource
    private HuntingControlEventDTOTransformerHelper helper;

    @Resource
    private PersistentFileMetadataRepository metadataRepository;

    @Override
    protected List<MobileHuntingControlEventDTO> transform(@Nonnull final List<HuntingControlEvent> events) {
        throw new UnsupportedOperationException("No transformation without specVersion supported");
    }

    public MobileHuntingControlEventDTO transform(@Nonnull final HuntingControlEvent event,
                                                  @Nonnull final MobileHuntingControlSpecVersion specVersion) {
        requireNonNull(event);
        requireNonNull(specVersion);

        return transform(Collections.singletonList(event), specVersion).get(0);
    }

    public List<MobileHuntingControlEventDTO> transform(@Nonnull final List<HuntingControlEvent> events,
                                                        @Nonnull final MobileHuntingControlSpecVersion specVersion) {

        requireNonNull(events);
        requireNonNull(specVersion);

        if (events.isEmpty()) {
            return emptyList();
        }

        final Map<HuntingControlEvent, Set<Person>> inspectors = helper.getEventToInspectorsMap(events);
        final Map<HuntingControlEvent, Set<HuntingControlCooperationType>> cooperations = helper.getEventToCooperationsMap(events);
        final Map<HuntingControlEvent, List<MobileChangeHistoryDTO>> changes = getEventToChangeMapping(events);
        final Map<HuntingControlEvent, List<MobileHuntingControlAttachmentDTO>> attachments = getEventToAttachmentMapping(events);

        return events.stream()
                .map(event -> MobileHuntingControlEventDTO.create(specVersion,
                                                                  event,
                                                                  inspectors.getOrDefault(event, emptySet()),
                                                                  cooperations.getOrDefault(event, emptySet()),
                                                                  changes.getOrDefault(event, emptyList()),
                                                                  attachments.getOrDefault(event, emptyList())))
                .collect(toList());
    }

    private Map<HuntingControlEvent, List<MobileChangeHistoryDTO>> getEventToChangeMapping(final List<HuntingControlEvent> events) {
        final List<HuntingControlEventChange> changes = helper.listChanges(events);
        final Map<Long, SystemUser> userMap = helper.getChangeUserIdToUserMap(changes);

        return changes.stream().collect(groupingBy(
                HuntingControlEventChange::getHuntingControlEvent,
                mapping(change -> MobileChangeHistoryDTO.create(change, userMap.get(change.getChangeHistory().getUserId())), toList())));
    }

    private Map<HuntingControlEvent, List<MobileHuntingControlAttachmentDTO>> getEventToAttachmentMapping(final List<HuntingControlEvent> events) {
        final List<HuntingControlAttachment> attachments = helper.listAttachments(events);
        final Function<HuntingControlAttachment, PersistentFileMetadata> metadataFunction =
                getMetadataFunction(attachments);

        return attachments.stream().collect(groupingBy(
                HuntingControlAttachment::getHuntingControlEvent,
                mapping(attachment -> MobileHuntingControlAttachmentDTO.create(attachment, metadataFunction.apply(attachment)), toList())));
    }

    private Function<HuntingControlAttachment, PersistentFileMetadata> getMetadataFunction(final List<HuntingControlAttachment> attachments) {
        return CriteriaUtils.singleQueryFunction(
                attachments, HuntingControlAttachment::getAttachmentMetadata, metadataRepository, true);
    }

}

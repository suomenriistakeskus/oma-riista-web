package fi.riista.feature.otherwisedeceased;

import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.feature.storage.metadata.PersistentFileMetadataRepository;
import fi.riista.util.F;
import fi.riista.util.ListTransformer;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Component
public class OtherwiseDeceasedAttachmentDTOTransformer extends ListTransformer<OtherwiseDeceasedAttachment, OtherwiseDeceasedAttachmentDTO> {

    @Resource
    private PersistentFileMetadataRepository metadataRepository;

    @Nonnull
    @Override
    public List<OtherwiseDeceasedAttachmentDTO> transform(@Nonnull final List<OtherwiseDeceasedAttachment> entities) {
        return F.mapNonNullsToList(entities, OtherwiseDeceasedAttachmentDTO::create);
    }

    public Map<Long, List<OtherwiseDeceasedAttachmentDTO>> transform(final Map<Long, List<OtherwiseDeceasedAttachment>> attachmentMap) {
        final Set<UUID> fileUUIDs = attachmentMap.values()
                .stream()
                .flatMap(e -> e.stream())
                .map(OtherwiseDeceasedAttachment::getAttachmentMetadata)
                .map(PersistentFileMetadata::getId)
                .collect(Collectors.toSet());

        final Map<UUID, PersistentFileMetadata> fileMap = F.indexById(metadataRepository.findAllById(fileUUIDs));
        final Map<Long, List<OtherwiseDeceasedAttachmentDTO>> resultMap = new HashMap<>();
        attachmentMap.forEach((k, v) -> resultMap.put(k, createDTOList(v, fileMap)));
        return resultMap;
    }

    private List<OtherwiseDeceasedAttachmentDTO> createDTOList(final List<OtherwiseDeceasedAttachment> entities,
                                                               final Map<UUID, PersistentFileMetadata> fileMap) {
        return entities.stream()
                .map(e -> OtherwiseDeceasedAttachmentDTO.create(e, fileMap.get(e.getAttachmentMetadata().getId())))
                .collect(toList());
    }
}

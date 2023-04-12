package fi.riista.feature.huntingclub.deercensus;

import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.CannotChangeAssociatedEntityException;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.deercensus.attachment.DeerCensusAttachmentFeature;
import fi.riista.security.EntityPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DeerCensusFeature extends AbstractCrudFeature<Long, DeerCensus, DeerCensusDTO> {

    @Resource
    private DeerCensusRepository deerCensusRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private DeerCensusAttachmentFeature deerCensusAttachmentFeature;

    @Transactional(readOnly = true)
    public List<DeerCensusDTO> findDeerCensusesByClubId(Long huntingClubId) {
        final HuntingClub huntingClub = requireEntityService.requireHuntingClub(huntingClubId, EntityPermission.READ);
        List<DeerCensus> deerCensuses = deerCensusRepository.findAllByHuntingClub(huntingClub);
        Collections.sort(deerCensuses, Comparator.comparing(DeerCensus::getObservationDate));
        Collections.reverse(deerCensuses);

        return deerCensuses.stream()
                .map(DeerCensusDTO::transform)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public DeerCensusDTO create(DeerCensusDTO dto) {
        DeerCensusDTO created = super.create(dto);
        deerCensusAttachmentFeature.connectAttachmentsWithoutDeerCensusAssociation(
                created.getId(), dto.getAttachmentIds());
        return created;
    }

    @Override
    protected void updateEntity(final DeerCensus entity, final DeerCensusDTO dto) {
        if (entity.isNew()) {
            final HuntingClub huntingClub = requireEntityService.requireHuntingClub(dto.getHuntingClubId(), EntityPermission.READ);
            entity.setHuntingClub(huntingClub);
        } else {
            CannotChangeAssociatedEntityException.assertRelationIdNotChanged(
                    entity, DeerCensus::getHuntingClub, dto.getHuntingClubId());
        }

        entity.setObservationDate(dto.getObservationDate());

        entity.setWhiteTailDeers(dto.getWhiteTailDeers());
        entity.setWhiteTailDeersAdditionalInfo(dto.getWhiteTailDeersAdditionalInfo());
        entity.setRoeDeers(dto.getRoeDeers());
        entity.setRoeDeersAdditionalInfo(dto.getRoeDeersAdditionalInfo());
        entity.setFallowDeers(dto.getFallowDeers());
        entity.setFallowDeersAdditionalInfo(dto.getFallowDeersAdditionalInfo());
    }

    @Override
    protected DeerCensusDTO toDTO(@Nonnull final DeerCensus entity) {
        return DeerCensusDTO.transform(entity);
    }

    @Override
    protected JpaRepository<DeerCensus, Long> getRepository() {
        return deerCensusRepository;
    }
}

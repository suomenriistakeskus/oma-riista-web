package fi.riista.feature.common.decision.authority.rka;

import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.RequireEntityService;
import fi.riista.security.EntityPermission;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class DecisionRkaAuthorityFeature extends AbstractCrudFeature<Long, DecisionRkaAuthority,
        DecisionRkaAuthorityDTO> {

    @Resource
    private DecisionRkaAuthorityRepository decisionRkaAuthorityRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public List<DecisionRkaAuthorityDTO> listByRka(final long rkaId) {

        final QDecisionRkaAuthority RKA_DELIVERY = QDecisionRkaAuthority.decisionRkaAuthority;
        return decisionRkaAuthorityRepository.findAllAsList(RKA_DELIVERY.rka.id.eq(rkaId))
                .stream().map(DecisionRkaAuthorityDTO::new)
                .collect(toList());
    }

    @Override
    protected DecisionRkaAuthorityRepository getRepository() {
        return decisionRkaAuthorityRepository;
    }

    @Override
    protected DecisionRkaAuthorityDTO toDTO(@Nonnull final DecisionRkaAuthority entity) {
        return new DecisionRkaAuthorityDTO(entity);
    }

    @Override
    protected void updateEntity(final DecisionRkaAuthority entity, final DecisionRkaAuthorityDTO dto) {
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setTitleFinnish(dto.getTitleFinnish());
        entity.setTitleSwedish(dto.getTitleSwedish());
        entity.setPhoneNumber(dto.getPhoneNumber().replaceAll(" ", ""));
        entity.setEmail(dto.getEmail());

        if (entity.isNew()) {
            entity.setRka(requireEntityService.requireRiistakeskuksenAlue(dto.getRkaId(), EntityPermission.READ));
        }
    }

}

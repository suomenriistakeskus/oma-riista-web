package fi.riista.feature.permit.decision.authority.rka;

import com.google.common.base.Preconditions;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.permit.decision.QPermitDecision;
import fi.riista.security.EntityPermission;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

@Component
public class PermitDecisionRkaAuthorityFeature {

    @Resource
    private PermitDecisionRkaAuthorityRepository permitDecisionRkaAuthorityRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public List<PermitDecisionRkaAuthorityDTO> listByRka(final long rkaId) {
        final QPermitDecisionRkaAuthority RKA_DELIVERY = QPermitDecisionRkaAuthority.permitDecisionRkaAuthority;
        return permitDecisionRkaAuthorityRepository.findAllAsList(RKA_DELIVERY.rka.id.eq(rkaId))
                .stream().map(PermitDecisionRkaAuthorityDTO::new)
                .collect(toList());
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public void create(final PermitDecisionRkaAuthorityDTO dto) {
        final RiistakeskuksenAlue rka = requireEntityService.requireRiistakeskuksenAlue(dto.getRkaId(), EntityPermission.UPDATE);
        final PermitDecisionRkaAuthority entity = new PermitDecisionRkaAuthority();

        entity.setRka(rka);
        updateEntity(dto, entity);

        permitDecisionRkaAuthorityRepository.save(entity);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public void update(final PermitDecisionRkaAuthorityDTO dto) {
        final RiistakeskuksenAlue rka = requireEntityService.requireRiistakeskuksenAlue(dto.getRkaId(), EntityPermission.UPDATE);
        final PermitDecisionRkaAuthority entity = permitDecisionRkaAuthorityRepository.getOne(dto.getId());

        Preconditions.checkState(Objects.equals(rka, entity.getRka()));
        updateEntity(dto, entity);
    }

    private void updateEntity(final PermitDecisionRkaAuthorityDTO dto, final PermitDecisionRkaAuthority entity) {
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setTitleFinnish(dto.getTitleFinnish());
        entity.setTitleSwedish(dto.getTitleSwedish());
        entity.setPhoneNumber(dto.getPhoneNumber().replaceAll(" ", ""));
        entity.setEmail(dto.getEmail());
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public void delete(final long id) {
        permitDecisionRkaAuthorityRepository.delete(id);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public List<PermitDecisionRkaAuthorityDTO> listByDecision(final long decisionId) {
        final QPermitDecisionRkaAuthority RKA_DELIVERY = QPermitDecisionRkaAuthority.permitDecisionRkaAuthority;
        final QPermitDecision DECISION = QPermitDecision.permitDecision;
        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
        final QOrganisation RKA = QOrganisation.organisation;
        final SQLQuery<Long> subQuery = SQLExpressions.select(RKA.id).from(DECISION)
                .join(DECISION.rhy, RHY)
                .join(RHY.parentOrganisation, RKA)
                .where(DECISION.id.eq(decisionId));

        return jpqlQueryFactory.select(RKA_DELIVERY).from(RKA_DELIVERY)
                .where(RKA_DELIVERY.rka.id.eq(subQuery))
                .fetch()
                .stream().map(PermitDecisionRkaAuthorityDTO::new)
                .collect(toList());
    }
}

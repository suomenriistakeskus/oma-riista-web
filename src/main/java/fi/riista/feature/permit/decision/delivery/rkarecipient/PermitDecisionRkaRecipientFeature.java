package fi.riista.feature.permit.decision.delivery.rkarecipient;

import com.google.common.base.Preconditions;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysCoordinatorService;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.security.EntityPermission;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Component
public class PermitDecisionRkaRecipientFeature {

    @Resource
    private PermitDecisionRkaRecipientRepository permitDecisionRkaRecipientRepository;

    @Resource
    private RiistanhoitoyhdistysRepository riistanhoitoyhdistysRepository;

    @Resource
    private RiistanhoitoyhdistysCoordinatorService riistanhoitoyhdistysCoordinatorService;

    @Resource
    private RequireEntityService requireEntityService;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public List<PermitDecisionRkaRecipientListingDTO> list() {
        final Stream<PermitDecisionRkaRecipientListingDTO> recipientStream = permitDecisionRkaRecipientRepository.findAll()
                .stream().map(d -> new PermitDecisionRkaRecipientListingDTO(d, new OrganisationNameDTO(d.getRka())));

        final Stream<PermitDecisionRkaRecipientListingDTO> rhyStream = riistanhoitoyhdistysRepository.findAll()
                .stream().map(rhy -> {
                    final OrganisationNameDTO rka = new OrganisationNameDTO(rhy.getParentOrganisation());
                    final String email = riistanhoitoyhdistysCoordinatorService.resolveRhyEmail(rhy);
                    return new PermitDecisionRkaRecipientListingDTO(rhy.getNameLocalisation(), email, rka);
                });
        return Stream.concat(recipientStream, rhyStream).collect(toList());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public List<PermitDecisionRkaRecipientDTO> listByRka(final long rkaId) {
        final QPermitDecisionRkaRecipient RKA_DELIVERY = QPermitDecisionRkaRecipient.permitDecisionRkaRecipient;
        return permitDecisionRkaRecipientRepository.findAllAsList(RKA_DELIVERY.rka.id.eq(rkaId))
                .stream().map(d -> new PermitDecisionRkaRecipientDTO(d.getId(), d.getRka().getId(), d.getNameLocalisation(), d.getEmail()))
                .collect(toList());

    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public void create(final PermitDecisionRkaRecipientDTO dto) {
        final RiistakeskuksenAlue rka = requireEntityService.requireRiistakeskuksenAlue(dto.getRkaId(), EntityPermission.UPDATE);
        final PermitDecisionRkaRecipient entity = new PermitDecisionRkaRecipient();

        entity.setRka(rka);
        updateEntity(dto, entity);

        permitDecisionRkaRecipientRepository.save(entity);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public void update(final PermitDecisionRkaRecipientDTO dto) {
        final RiistakeskuksenAlue rka = requireEntityService.requireRiistakeskuksenAlue(dto.getRkaId(), EntityPermission.UPDATE);
        final PermitDecisionRkaRecipient entity = permitDecisionRkaRecipientRepository.getOne(dto.getId());

        Preconditions.checkState(Objects.equals(rka, entity.getRka()));
        updateEntity(dto, entity);
    }

    private void updateEntity(final PermitDecisionRkaRecipientDTO dto, final PermitDecisionRkaRecipient entity) {
        entity.setNameFinnish(dto.getNameFI());
        entity.setNameSwedish(dto.getNameSV());
        entity.setEmail(dto.getEmail());
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public void delete(final long id) {
        permitDecisionRkaRecipientRepository.delete(id);
    }
}

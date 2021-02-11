package fi.riista.feature.common.decision.nomination;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.decision.nomination.delivery.NominationDecisionDeliveryRepository;
import fi.riista.feature.common.decision.nomination.delivery.NominationDecisionDeliveryService;
import fi.riista.feature.common.decision.nomination.document.NominationDecisionTextService;
import fi.riista.feature.organization.jht.nomination.OccupationNominationRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysCoordinatorService;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.feature.permit.application.DeliveryAddress;
import fi.riista.feature.permit.application.DocumentNumberAllocationService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static fi.riista.feature.organization.rhy.RiistanhoitoyhdistysAuthorization.RhyModeratorPermission.CREATE_NOMINATION_DECISION;
import static java.util.Collections.emptyList;

@Component
public class NominationDecisionCreateFeature {

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private RiistanhoitoyhdistysRepository riistanhoitoyhdistysRepository;

    @Resource
    private RiistanhoitoyhdistysCoordinatorService riistanhoitoyhdistysCoordinatorService;

    @Resource
    private NominationDecisionRepository nominationDecisionRepository;

    @Resource
    private NominationDecisionTextService nominationDecisionTextService;

    @Resource
    private NominationDecisionDeliveryRepository nominationDecisionDeliveryRepository;

    @Resource
    private NominationDecisionDeliveryService nominationDecisionDeliveryService;

    @Resource
    private OccupationNominationRepository occupationNominationRepository;

    @Resource
    private DocumentNumberAllocationService documentNumberAllocationService;

    @Transactional
    public long createNominationDecision(final CreateNominationDecisionDTO dto) {
        final Riistanhoitoyhdistys rhy = riistanhoitoyhdistysRepository.findByOfficialCode(dto.getRhyCode());
        checkArgument(rhy != null, "RHY not found with official code " + dto.getRhyCode());
        activeUserService.assertHasPermission(rhy, CREATE_NOMINATION_DECISION);

        final SystemUser user = activeUserService.requireActiveUser();

        final Person coordinator = riistanhoitoyhdistysCoordinatorService.findCoordinator(rhy);
        checkState(coordinator != null, "Coordinator not found for RHY");

        final DeliveryAddress deliveryAddress = Stream.of(rhy.getAddress(), coordinator.getAddress())
                .filter(Objects::nonNull)
                .findFirst()
                .map(address-> DeliveryAddress.create(rhy.getNameLocalisation().getTranslation(dto.getLocale()), address))
                .orElseThrow(() -> new IllegalArgumentException("Rhy or contact person must have valid address."));

        final NominationDecision nominationDecision =
                NominationDecision.create(
                        documentNumberAllocationService.allocateNextNumber(),
                        rhy,
                        dto.getOccupationType(),
                        dto.getNominationDecisionType(),
                        coordinator,
                        deliveryAddress,
                        dto.getLocale());

        nominationDecision.setHandler(user);

        nominationDecision.setProposalDate(
                occupationNominationRepository.findProposalDateForNomination(rhy, dto.getOccupationType()));

        final NominationDecision persistedDecision = nominationDecisionRepository.save(nominationDecision);
        nominationDecisionDeliveryRepository.saveAll(
                nominationDecisionDeliveryService.generateDeliveries(nominationDecision, emptyList()));

        nominationDecisionTextService.generateDefaultTextSections(nominationDecision);

        return persistedDecision.getId();
    }

}

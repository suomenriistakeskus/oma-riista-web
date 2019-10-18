package fi.riista.feature.permit.decision.derogation;

import com.google.common.base.Preconditions;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.bird.BirdPermitApplication;
import fi.riista.feature.permit.application.bird.BirdPermitApplicationRepository;
import fi.riista.feature.permit.application.bird.forbidden.BirdPermitApplicationForbiddenMethods;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_POPULATION_PRESERVATION;
import static java.util.Objects.requireNonNull;
import static org.springframework.util.StringUtils.hasText;

@Service
public class PermitDecisionDerogationService {

    @Resource
    private PermitDecisionDerogationReasonRepository permitDecisionDerogationReasonRepository;

    @Resource
    private PermitDecisionProtectedAreaTypeRepository permitDecisionProtectedAreaTypeRepository;

    @Resource
    private BirdPermitApplicationRepository birdPermitApplicationRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional
    public void initializeForApplication(final @Nonnull PermitDecision decision) {
        requireNonNull(decision);

        final HarvestPermitApplication application = decision.getApplication();

        assertNoDerogationsExist(decision);

        switch (application.getHarvestPermitCategory()) {
            case BIRD: {
                final BirdPermitApplication birdApplication =
                        birdPermitApplicationRepository.findByHarvestPermitApplication(application);

                permitDecisionDerogationReasonRepository.save(createBirdDerogationReasons(decision, birdApplication));
                permitDecisionProtectedAreaTypeRepository.save(createProtectedAreaTypeForBird(decision,
                        birdApplication));
                assignLegalSectionDerogationsFromBirdApplication(decision, birdApplication);
                break;
            }
            case LARGE_CARNIVORE_BEAR:
            case LARGE_CARNIVORE_LYNX:
            case LARGE_CARNIVORE_LYNX_PORONHOITO:
            case LARGE_CARNIVORE_WOLF: {
                permitDecisionDerogationReasonRepository.save(createCarnivoreDerogationReasons(decision));
                break;
            }
            case MOOSELIKE_NEW:
            case MOOSELIKE:
            default:
                throw new IllegalArgumentException("Not a derogation type application");
        }


    }

    @Transactional(readOnly = true)
    public PermitDecision requireDecisionDerogationEditable(long decisionId) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.UPDATE);
        decision.assertStatus(PermitDecision.Status.DRAFT);
        decision.assertHandler(activeUserService.requireActiveUser());

        return decision;
    }

    private void assignLegalSectionDerogationsFromBirdApplication(PermitDecision decision,
                                                                  BirdPermitApplication birdApplication) {
        final BirdPermitApplicationForbiddenMethods forbiddenMethods =
                requireNonNull(birdApplication.getForbiddenMethods());

        if (hasText(forbiddenMethods.getDeviateSection32())) {
            decision.setLegalSection32(true);
        }
        if (forbiddenMethods.isTapeRecorders() ||
                hasText(forbiddenMethods.getDeviateSection33())) {
            decision.setLegalSection33(true);
        }
        if (forbiddenMethods.isTraps() ||
                hasText(forbiddenMethods.getDeviateSection34())) {
            decision.setLegalSection34(true);
        }
        if (hasText(forbiddenMethods.getDeviateSection35())) {
            decision.setLegalSection35(true);
        }
        if (hasText(forbiddenMethods.getDeviateSection51())) {
            decision.setLegalSection51(true);
        }
    }

    private PermitDecisionProtectedAreaType createProtectedAreaTypeForBird(final PermitDecision decision,
                                                                           final BirdPermitApplication birdApplication) {
        return new PermitDecisionProtectedAreaType(decision, birdApplication.getProtectedArea().getProtectedAreaType());
    }

    private List<PermitDecisionDerogationReason> createBirdDerogationReasons(final PermitDecision decision,
                                                                             final BirdPermitApplication birdApplication) {
        return PermitDecisionDerogationReasonType.streamSelected(birdApplication.getCause())
                .map(type -> new PermitDecisionDerogationReason(decision, type))
                .collect(Collectors.toList());
    }

    private void assertNoDerogationsExist(PermitDecision decision) {
        Preconditions.checkState(permitDecisionDerogationReasonRepository.findByPermitDecision(decision).isEmpty());
        Preconditions.checkState(permitDecisionProtectedAreaTypeRepository.findByPermitDecision(decision).isEmpty());
    }

    private PermitDecisionDerogationReason createCarnivoreDerogationReasons(final PermitDecision decision) {
        return new PermitDecisionDerogationReason(decision, REASON_POPULATION_PRESERVATION);
    }
}

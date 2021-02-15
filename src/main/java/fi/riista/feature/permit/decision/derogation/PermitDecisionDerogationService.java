package fi.riista.feature.permit.decision.derogation;

import com.google.common.base.Preconditions;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.bird.BirdPermitApplication;
import fi.riista.feature.permit.application.bird.BirdPermitApplicationRepository;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethods;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReason;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReasonRepository;
import fi.riista.feature.permit.application.mammal.MammalPermitApplication;
import fi.riista.feature.permit.application.mammal.MammalPermitApplicationRepository;
import fi.riista.feature.permit.application.nestremoval.NestRemovalPermitApplicationRepository;
import fi.riista.feature.common.decision.DecisionStatus;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.ArrayList;
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
    private MammalPermitApplicationRepository mammalPermitApplicationRepository;

    @Resource
    private NestRemovalPermitApplicationRepository nestRemovalPermitApplicationRepository;

    @Resource
    private DerogationPermitApplicationReasonRepository derogationPermitApplicationReasonRepository;

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

                permitDecisionDerogationReasonRepository.saveAll(createBirdDerogationReasons(decision, birdApplication));
                permitDecisionProtectedAreaTypeRepository.save(createProtectedAreaTypeForBird(decision,
                        birdApplication));
                assignLegalSectionsDerogations(decision, birdApplication.getForbiddenMethods());
                break;
            }
            case LARGE_CARNIVORE_BEAR:
            case LARGE_CARNIVORE_LYNX:
            case LARGE_CARNIVORE_LYNX_PORONHOITO:
            case LARGE_CARNIVORE_WOLF: {
                permitDecisionDerogationReasonRepository.save(createCarnivoreDerogationReasons(decision));
                break;
            }
            case MAMMAL: {
                final MammalPermitApplication mammalPermitApplication =
                        mammalPermitApplicationRepository.findByHarvestPermitApplication(application);
                createDerogationReasons(decision, application);
                assignLegalSectionsDerogations(decision, mammalPermitApplication.getForbiddenMethods());
                break;
            }
            case NEST_REMOVAL:
                createDerogationReasons(decision, application);
                break;
            case MOOSELIKE_NEW:
            case MOOSELIKE:
            default:
                throw new IllegalArgumentException("Not a derogation type application");
        }


    }


    @Transactional(readOnly = true)
    public PermitDecision requireDecisionDerogationEditable(final long decisionId) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.UPDATE);
        decision.assertEditableBy(activeUserService.requireActiveUser());

        return decision;
    }

    private void createDerogationReasons(@Nonnull final PermitDecision decision,
                                         final HarvestPermitApplication application) {
        final List<DerogationPermitApplicationReason> applicationReasons =
                derogationPermitApplicationReasonRepository.findByHarvestPermitApplication(application);
        final ArrayList<PermitDecisionDerogationReason> decisionDerogationReasons =
                F.mapNonNullsToList(applicationReasons,
                        r -> new PermitDecisionDerogationReason(decision, r.getReasonType()));
        permitDecisionDerogationReasonRepository.saveAll(decisionDerogationReasons);
    }

    private static void assignLegalSectionsDerogations(final PermitDecision decision,
                                                       final DerogationPermitApplicationForbiddenMethods forbiddenMethods) {
        requireNonNull(forbiddenMethods);

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

    private static PermitDecisionProtectedAreaType createProtectedAreaTypeForBird(final PermitDecision decision,
                                                                                  final BirdPermitApplication birdApplication) {
        return new PermitDecisionProtectedAreaType(decision, birdApplication.getProtectedArea().getProtectedAreaType());
    }

    private static List<PermitDecisionDerogationReason> createBirdDerogationReasons(final PermitDecision decision,
                                                                                    final BirdPermitApplication birdApplication) {
        return PermitDecisionDerogationReasonType.streamSelectedForBird(birdApplication.getCause())
                .map(type -> new PermitDecisionDerogationReason(decision, type))
                .collect(Collectors.toList());
    }

    private void assertNoDerogationsExist(final PermitDecision decision) {
        Preconditions.checkState(permitDecisionDerogationReasonRepository.findByPermitDecision(decision).isEmpty());
        Preconditions.checkState(permitDecisionProtectedAreaTypeRepository.findByPermitDecision(decision).isEmpty());
    }

    private static PermitDecisionDerogationReason createCarnivoreDerogationReasons(final PermitDecision decision) {
        return new PermitDecisionDerogationReason(decision, REASON_POPULATION_PRESERVATION);
    }
}

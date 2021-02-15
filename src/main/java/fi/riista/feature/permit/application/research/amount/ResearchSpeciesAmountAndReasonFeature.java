package fi.riista.feature.permit.application.research.amount;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountUpdater;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationLawSectionReasonsDTO;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReasonService;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReasonsDTO;
import fi.riista.feature.permit.decision.derogation.DerogationLawSection;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonDTO;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_RESEARCH;
import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_RESEARCH_41A;
import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_RESEARCH_41C;

@Service
public class ResearchSpeciesAmountAndReasonFeature {

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Resource
    private DerogationPermitApplicationReasonService derogationPermitApplicationReasonService;

    @Transactional(readOnly = true)
    public List<ResearchSpeciesAmountDTO> getSpeciesAmounts(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        return application.getSpeciesAmounts().stream()
                .sorted(Comparator.comparing(HarvestPermitApplicationSpeciesAmount::getId))
                .map(ResearchSpeciesAmountDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveSpeciesAmountsAndDerogationReasons(final long applicationId,
                                                       final List<ResearchSpeciesAmountDTO> dtoList) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        final List<HarvestPermitApplicationSpeciesAmount> existingSpecies =
                harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application);

        final Updater speciesUpdater = new Updater(existingSpecies, createCallback(application));
        speciesUpdater.processAll(dtoList);

        final List<HarvestPermitApplicationSpeciesAmount> addedSpeciesAmounts = speciesUpdater.getResultList();
        harvestPermitApplicationSpeciesAmountRepository.saveAll(addedSpeciesAmounts);
        harvestPermitApplicationSpeciesAmountRepository.deleteAll(speciesUpdater.getMissing());

        final List<DerogationPermitApplicationLawSectionReasonsDTO> derogationReasons = addedSpeciesAmounts.stream()
                .map(this::mapReason)
                .distinct()
                .collect(Collectors.toList());

        final DerogationPermitApplicationReasonsDTO reasonsDto = DerogationPermitApplicationReasonsDTO.of(derogationReasons);

        derogationPermitApplicationReasonService.updateDerogationReasons(application, reasonsDto);
    }

    @Nonnull
    private UpdaterCallback createCallback(final HarvestPermitApplication application) {
        return new UpdaterCallback() {
            @Override
            public HarvestPermitApplicationSpeciesAmount create(final ResearchSpeciesAmountDTO dto) {
                final GameSpecies gameSpecies = gameSpeciesService.requireByOfficialCode(dto.getGameSpeciesCode());

                return HarvestPermitApplicationSpeciesAmount.createForHarvest(application, gameSpecies, dto.getAmount());
            }

            @Override
            public void update(final HarvestPermitApplicationSpeciesAmount entity,
                               final ResearchSpeciesAmountDTO dto) {
                entity.setSpecimenAmount((float)dto.getAmount());
            }

            @Override
            public int getSpeciesCode(final ResearchSpeciesAmountDTO dto) {
                return dto.getGameSpeciesCode();
            }
        };
    }

    private static class Updater
            extends HarvestPermitApplicationSpeciesAmountUpdater<ResearchSpeciesAmountDTO> {
        Updater(final List<HarvestPermitApplicationSpeciesAmount> existingList,
                final UpdaterCallback callback) {
            super(existingList, callback);
        }
    }

    private abstract static class UpdaterCallback
            implements HarvestPermitApplicationSpeciesAmountUpdater.Callback<ResearchSpeciesAmountDTO> {
    }

    private DerogationPermitApplicationLawSectionReasonsDTO mapReason(final HarvestPermitApplicationSpeciesAmount species) {
        final int speciesCode = species.getGameSpecies().getOfficialCode();

        final DerogationLawSection lawSection = DerogationLawSection.getSpeciesLawSection(speciesCode);
        PermitDecisionDerogationReasonType reasonType;

        switch (lawSection) {
            case SECTION_41A:
                reasonType = REASON_RESEARCH_41A;
                break;
            case SECTION_41B:
                reasonType = REASON_RESEARCH;
                break;
            case SECTION_41C:
                reasonType = REASON_RESEARCH_41C;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + lawSection);
        }

        return DerogationPermitApplicationLawSectionReasonsDTO.of(lawSection,
                new Integer(speciesCode).toString(),
                PermitDecisionDerogationReasonDTO.toDTOs(Collections.singletonList(reasonType), lawSection));
    }
}

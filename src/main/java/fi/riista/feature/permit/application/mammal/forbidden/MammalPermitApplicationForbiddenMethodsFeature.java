package fi.riista.feature.permit.application.mammal.forbidden;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountUpdater;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethodsDTO;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethodsSpeciesDTO;
import fi.riista.feature.permit.application.mammal.MammalPermitApplication;
import fi.riista.feature.permit.application.mammal.MammalPermitApplicationRepository;
import fi.riista.feature.permit.application.mammal.MammalPermitApplicationService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MammalPermitApplicationForbiddenMethodsFeature {

    @Resource
    private MammalPermitApplicationRepository mammalPermitApplicationRepository;

    @Resource
    private MammalPermitApplicationService mammalPermitApplicationService;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Transactional(readOnly = true)
    public DerogationPermitApplicationForbiddenMethodsDTO getCurrentMethodInfo(final long applicationId) {
        final MammalPermitApplication mammalPermitApplication =
                mammalPermitApplicationService.findForRead(applicationId);

        final List<DerogationPermitApplicationForbiddenMethodsSpeciesDTO> justificationList = mammalPermitApplication
                .getHarvestPermitApplication().getSpeciesAmounts().stream()
                .sorted(Comparator.comparing(HarvestPermitApplicationSpeciesAmount::getId))
                .map(DerogationPermitApplicationForbiddenMethodsSpeciesDTO::new)
                .collect(Collectors.toList());

        return DerogationPermitApplicationForbiddenMethodsDTO.createFrom(mammalPermitApplication.getForbiddenMethods(), justificationList);
    }

    @Transactional
    public void updateMethodInfo(final long applicationId,
                                 final @NotNull DerogationPermitApplicationForbiddenMethodsDTO forbiddenMethods) {
        final MammalPermitApplication mammalPermitApplication =
                mammalPermitApplicationService.findOrCreateForUpdate(applicationId);
        final HarvestPermitApplication application = mammalPermitApplication.getHarvestPermitApplication();
        mammalPermitApplication.setForbiddenMethods(forbiddenMethods.toEntity());
        mammalPermitApplicationRepository.save(mammalPermitApplication);

        final List<HarvestPermitApplicationSpeciesAmount> existingSpecies =
                harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application);

        final Updater updater = new Updater(existingSpecies, new UpdaterCallback());
        updater.processAll(forbiddenMethods.getSpeciesJustifications());
        updater.assertNoSpeciesMissing();
    }

    static class UpdaterCallback
            implements HarvestPermitApplicationSpeciesAmountUpdater.Callback<DerogationPermitApplicationForbiddenMethodsSpeciesDTO> {
        @Override
        public void update(final HarvestPermitApplicationSpeciesAmount entity,
                           final DerogationPermitApplicationForbiddenMethodsSpeciesDTO dto) {
            entity.setForbiddenMethodJustification(dto.getJustification());
            entity.setForbiddenMethodsUsed(dto.isForbiddenMethodsUsed());
        }

        @Override
        public int getSpeciesCode(final DerogationPermitApplicationForbiddenMethodsSpeciesDTO dto) {
            return dto.getGameSpeciesCode();
        }
    }

    private static class Updater
            extends HarvestPermitApplicationSpeciesAmountUpdater<DerogationPermitApplicationForbiddenMethodsSpeciesDTO> {
        Updater(final List<HarvestPermitApplicationSpeciesAmount> existingList,
                final UpdaterCallback callback) {
            super(existingList, callback);
        }
    }
}

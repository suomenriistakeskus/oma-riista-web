package fi.riista.feature.permit.application.lawsectionten.period;

import com.google.common.base.Preconditions;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.application.lawsectionten.LawSectionTenPermitApplicationRepository;
import fi.riista.feature.permit.application.lawsectionten.LawSectionTenPermitApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Service
public class LawSectionTenPermitApplicationSpeciesPeriodFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Resource
    private LawSectionTenPermitApplicationRepository lawSectionTenPermitApplicationRepository;

    @Transactional(readOnly = true)
    public LawSectionTenPermitApplicationSpeciesPeriodDTO getPermitPeriodInformation(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        final LawSectionTenPermitApplication lawSectionTenApplication =
                lawSectionTenPermitApplicationRepository.findByHarvestPermitApplication(application);

        Preconditions.checkNotNull(lawSectionTenApplication, "Law section 10 application must be defined when setting species periods");

        return application.getSpeciesAmounts().stream()
                .map(LawSectionTenPermitApplicationSpeciesPeriodDTO::new)
                .findFirst()
                .orElse(null);
    }

    @Transactional
    public void saveSpeciesPeriods(final long applicationId,
                                   final LawSectionTenPermitApplicationSpeciesPeriodDTO dto) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        final LawSectionTenPermitApplication lawSectionTenApplication =
                lawSectionTenPermitApplicationRepository.findByHarvestPermitApplication(application);

        Preconditions.checkNotNull(lawSectionTenApplication, "Law section 10 application must be defined when setting species periods");

        final List<HarvestPermitApplicationSpeciesAmount> existingSpecies =
                harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application);
        requireNonNull(existingSpecies);

        validateSpecies(dto, existingSpecies);
        final HarvestPermitApplicationSpeciesAmount speciesAmount = existingSpecies.get(0);
        LawSectionTenPermitApplicationSpeciesPeriodValidator.validatePeriod(dto.getGameSpeciesCode(),
                dto.getBeginDate(),
                dto.getEndDate());

        speciesAmount.setBeginDate(dto.getBeginDate());
        speciesAmount.setEndDate(dto.getEndDate());
    }

    private static void validateSpecies(final LawSectionTenPermitApplicationSpeciesPeriodDTO dto,
                                        final List<HarvestPermitApplicationSpeciesAmount> amounts) {

        if(amounts.isEmpty() && amounts.size() > 1) {
            throw new IllegalArgumentException("Incorrect species for period");
        }

        if (amounts.get(0).getGameSpecies().getOfficialCode() != dto.getGameSpeciesCode()) {
            throw new IllegalArgumentException("Species do not match");
        }
    }

}

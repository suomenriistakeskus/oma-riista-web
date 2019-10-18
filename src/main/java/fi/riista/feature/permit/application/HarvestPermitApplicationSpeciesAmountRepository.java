package fi.riista.feature.permit.application;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.gamediary.GameSpecies;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

public interface HarvestPermitApplicationSpeciesAmountRepository
        extends BaseRepository<HarvestPermitApplicationSpeciesAmount, Long> {

    long countByHarvestPermitApplication(HarvestPermitApplication application);

    List<HarvestPermitApplicationSpeciesAmount> findByHarvestPermitApplication(HarvestPermitApplication application);

    @Query("SELECT DISTINCT g FROM HarvestPermitApplication a" +
            " JOIN a.speciesAmounts s" +
            " JOIN s.gameSpecies g" +
            " WHERE a = ?1")
    List<GameSpecies> findSpeciesByApplication(final HarvestPermitApplication application);

    default Optional<HarvestPermitApplicationSpeciesAmount> findAtMostOneByHarvestPermitApplication(final HarvestPermitApplication application) {
        final List<HarvestPermitApplicationSpeciesAmount> speciesAmounts = findByHarvestPermitApplication(application);

        if (speciesAmounts.isEmpty()) {
            return Optional.empty();
        }

        if (speciesAmounts.size() > 1) {
            throw new IllegalStateException(format(
                    "More than one species amount not valid for carnivore permit application (id=%d)",
                    application.getId()));
        }

        return Optional.of(speciesAmounts.get(0));
    }
}

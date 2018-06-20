package fi.riista.feature.permit.application.species;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HarvestPermitApplicationSpeciesAmountRepository
        extends BaseRepository<HarvestPermitApplicationSpeciesAmount, Long> {
    long countByHarvestPermitApplication(HarvestPermitApplication application);

    @Query("SELECT DISTINCT g FROM HarvestPermitApplication a" +
            " JOIN a.speciesAmounts s" +
            " JOIN s.gameSpecies g" +
            " WHERE a = ?1")
    List<GameSpecies> findSpeciesByApplication(final HarvestPermitApplication application);

    void deleteByHarvestPermitApplication(HarvestPermitApplication entity);
}

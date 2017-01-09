package fi.riista.feature.huntingclub.hunting.overview;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.GameSpeciesRepository;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Component
public class CoordinatorClubHarvestFeature {

    @Resource
    private GameSpeciesRepository gameSpeciesRepository;

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private CoordinatorClubHarvestDTOTransformer coordinatorHarvestDTOTransformer;

    @Transactional(readOnly = true)
    public List<HarvestDTO> listHarvest(final long rhyId,
                                        final int speciesCode,
                                        final boolean filterByAreaGeometry,
                                        final LocalDate begin,
                                        final LocalDate end) {

        final Riistanhoitoyhdistys rhy = requireEntityService.requireRiistanhoitoyhdistys(rhyId, EntityPermission.READ);

        final GameSpecies species =
                gameSpeciesRepository.findByOfficialCode(speciesCode).orElseThrow(IllegalStateException::new);

        final Interval interval = DateUtil.createDateInterval(begin, end);

        final List<Harvest> harvestList = filterByAreaGeometry
                ? harvestRepository.findHarvestsLinkedToHuntingDayWithinAreaOfRhy(rhy, species, interval)
                : harvestRepository.findHarvestsLinkedToHuntingDayAndPermitOfRhy(rhy, species, interval);

        return coordinatorHarvestDTOTransformer.apply(harvestList);
    }

}

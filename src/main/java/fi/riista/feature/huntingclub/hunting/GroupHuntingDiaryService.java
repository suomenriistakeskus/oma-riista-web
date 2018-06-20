package fi.riista.feature.huntingclub.hunting;

import fi.riista.feature.gamediary.GameDiaryEntryDTO;
import fi.riista.feature.gamediary.HasHuntingDayId;
import fi.riista.feature.gamediary.HuntingDiaryEntryDTO;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.HarvestDTOTransformer;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationDTO;
import fi.riista.feature.gamediary.observation.ObservationDTOTransformer;
import fi.riista.feature.gamediary.observation.ObservationRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.Interval;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Service
public class GroupHuntingDiaryService {

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private ObservationRepository observationRepository;

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    @Resource
    private HarvestDTOTransformer harvestTransformer;

    @Resource
    private ObservationDTOTransformer observationTransformer;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<HarvestDTO> getHarvestsOfGroupMembers(HuntingClubGroup group) {
        final Interval interval = DateUtil.huntingYearInterval(group.getHuntingYear());
        final List<Harvest> groupHarvest = harvestRepository.findGroupHarvest(group, interval);

        return filterResult(group, harvestTransformer.apply(groupHarvest));
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<ObservationDTO> getObservationsOfGroupMembers(HuntingClubGroup group) {
        final Interval interval = DateUtil.huntingYearInterval(group.getHuntingYear());
        final List<Observation> groupObservations = getObservations(group, interval);

        return filterResult(group, observationTransformer.apply(groupObservations));
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<HuntingDiaryEntryDTO> getDiaryOfGroupMembers(HuntingClubGroup group) {
        final Interval interval = DateUtil.huntingYearInterval(group.getHuntingYear());
        final List<Harvest> groupHarvest = harvestRepository.findGroupHarvest(group, interval);
        final List<Observation> groupObservations = getObservations(group, interval);

        return F.concat(filterResult(group, harvestTransformer.apply(groupHarvest)),
                filterResult(group, observationTransformer.apply(groupObservations)));
    }

    private <T extends GameDiaryEntryDTO & HasHuntingDayId> List<T> filterResult(
            final HuntingClubGroup huntingClubGroup, final List<T> input) {

        if (huntingClubGroup.getHarvestPermit() == null) {
            return emptyList();
        }

        return harvestPermitSpeciesAmountRepository.findByHuntingClubGroupPermit(huntingClubGroup)
                .map(speciesAmount -> input.stream()
                        .filter(entry -> entry.getHuntingDayId() != null || speciesAmount.containsDate(entry.getPointOfTime().toLocalDate()))
                        .collect(toList()))
                .orElseGet(Collections::emptyList);
    }


    private List<Observation> getObservations(HuntingClubGroup group, Interval interval) {
        return group.getSpecies().isMoose()
                ? observationRepository.findGroupObservations(group, interval)
                : Collections.emptyList();
    }
}

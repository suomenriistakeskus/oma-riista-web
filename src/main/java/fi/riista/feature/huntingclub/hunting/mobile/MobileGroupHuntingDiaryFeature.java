package fi.riista.feature.huntingclub.hunting.mobile;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.GroupHuntingDiaryService;
import fi.riista.feature.huntingclub.hunting.rejection.HarvestRejection;
import fi.riista.feature.huntingclub.hunting.rejection.HarvestRejectionRepository;
import fi.riista.feature.huntingclub.hunting.rejection.ObservationRejection;
import fi.riista.feature.huntingclub.hunting.rejection.ObservationRejectionRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import org.joda.time.Interval;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.util.Collect.idList;
import static java.util.stream.Collectors.toList;

@Service
public class MobileGroupHuntingDiaryFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private GroupHuntingDiaryService groupHuntingDiaryService;

    @Resource
    private MobileGroupHarvestDTOTransformer harvestTransformer;

    @Resource
    private MobileGroupObservationDTOTransformer observationTransformer;

    @Resource
    private HarvestRejectionRepository harvestRejectionRepository;

    @Resource
    private ObservationRejectionRepository observationRejectionRepository;

    @Transactional(readOnly = true)
    public MobileGroupHuntingDiaryDTO getDiaryOfGroupMembers(final long huntingClubGroupId) {
        final HuntingClubGroup group = requireEntityService.requireHuntingGroup(huntingClubGroupId, EntityPermission.READ);

        final Interval interval = DateUtil.huntingYearInterval(group.getHuntingYear());
        final List<Harvest> groupHarvests = harvestRepository.findGroupHarvest(group, interval);
        final List<MobileGroupHarvestDTO> allHarvests =
                groupHuntingDiaryService.filterGroupHuntingResult(
                        group,
                        harvestTransformer.apply(groupHarvests, HarvestSpecVersion.CURRENTLY_SUPPORTED));

        final List<Observation> groupObservations = groupHuntingDiaryService.getGroupObservations(group, interval);
        final List<MobileGroupObservationDTO> allObservations =
                groupHuntingDiaryService.filterGroupHuntingResult(
                        group,
                        observationTransformer.apply(groupObservations, ObservationSpecVersion.MOST_RECENT));

        final List<Long> rejectedHarvestsIds = harvestRejectionRepository.findByGroup(group).stream()
                .map(HarvestRejection::getHarvest)
                .collect((idList()));
        final List<Long> rejectedObservationsIds = observationRejectionRepository.findByGroup(group).stream()
                .map(ObservationRejection::getObservation)
                .collect((idList()));

        final List<MobileGroupHarvestDTO> harvests = allHarvests.stream()
                .filter(h -> !rejectedHarvestsIds.contains(h.getId()))
                .collect(toList());
        final List<MobileGroupHarvestDTO> rejectedHarvests = allHarvests.stream()
                .filter(h -> rejectedHarvestsIds.contains(h.getId()))
                .collect(toList());

        final List<MobileGroupObservationDTO> observations = allObservations.stream()
                .filter(o -> !rejectedObservationsIds.contains(o.getId()))
                .collect(toList());
        final List<MobileGroupObservationDTO> rejectedObservations = allObservations.stream()
                .filter(o -> rejectedObservationsIds.contains(o.getId()))
                .collect(toList());

        return new MobileGroupHuntingDiaryDTO(harvests, observations, rejectedHarvests, rejectedObservations);
    }
}

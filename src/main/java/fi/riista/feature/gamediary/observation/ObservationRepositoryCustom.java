package fi.riista.feature.gamediary.observation;

import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import org.joda.time.Interval;

import java.util.List;

public interface ObservationRepositoryCustom {
    List<Observation> findGroupObservationsWithinMooseHunting(HuntingClubGroup huntingClubGroup, Interval interval);

    List<Observation> findGroupObservationsWithinDeerHunting(HuntingClubGroup huntingClubGroup);

    List<HuntingClubGroup> findGroupCandidatesForDeerObservation(final Observation observation);

}

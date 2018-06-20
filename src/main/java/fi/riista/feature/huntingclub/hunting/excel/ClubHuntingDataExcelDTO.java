package fi.riista.feature.huntingclub.hunting.excel;

import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.observation.ObservationDTO;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayDTO;
import fi.riista.util.LocalisedString;

import java.util.List;

public class ClubHuntingDataExcelDTO {
    private final LocalisedString groupName;
    private final List<GroupHuntingDayDTO> days;
    private final List<HarvestDTO> harvests;
    private final List<ObservationDTO> observations;

    public ClubHuntingDataExcelDTO(final LocalisedString groupName,
                                   final List<GroupHuntingDayDTO> days,
                                   final List<HarvestDTO> harvests,
                                   final List<ObservationDTO> observations) {
        this.groupName = groupName;
        this.days = days;
        this.harvests = harvests;
        this.observations = observations;
    }

    public LocalisedString getGroupName() {
        return groupName;
    }

    public List<GroupHuntingDayDTO> getDays() {
        return days;
    }

    public List<HarvestDTO> getHarvests() {
        return harvests;
    }

    public List<ObservationDTO> getObservations() {
        return observations;
    }
}

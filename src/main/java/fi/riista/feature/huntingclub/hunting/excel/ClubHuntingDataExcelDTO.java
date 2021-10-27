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
    private final LocalisedString rhyName;
    private final String permitNumber;

    public ClubHuntingDataExcelDTO(final LocalisedString groupName,
                                   final List<GroupHuntingDayDTO> days,
                                   final List<HarvestDTO> harvests,
                                   final List<ObservationDTO> observations,
                                   final LocalisedString rhyName,
                                   final String permitNumber) {
        this.groupName = groupName;
        this.days = days;
        this.harvests = harvests;
        this.observations = observations;
        this.rhyName = rhyName;
        this.permitNumber = permitNumber;
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

    public LocalisedString getRhyName() {
        return rhyName;
    }

    public String getPermitNumber() {
        return permitNumber;
    }
}

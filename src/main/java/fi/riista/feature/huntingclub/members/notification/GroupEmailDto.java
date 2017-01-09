package fi.riista.feature.huntingclub.members.notification;

import fi.riista.util.LocalisedString;

import java.util.List;

public class GroupEmailDto {
    private final LocalisedString name;
    private final LocalisedString speciesName;
    private final String permitNumber;
    private final List<LeaderEmailDto> leaders;

    public GroupEmailDto(LocalisedString name,
                         LocalisedString speciesName,
                         String permitNumber,
                         List<LeaderEmailDto> leaders) {
        this.name = name;
        this.speciesName = speciesName;
        this.permitNumber = permitNumber;
        this.leaders = leaders;
    }

    public String getNameFinnish() {
        return name.getFinnish();
    }

    public String getNameSwedish() {
        return name.getSwedish();
    }

    public String getSpeciesNameFinnish() {
        return speciesName.getFinnish();
    }

    public String getSpeciesNameSwedish() {
        return speciesName.getSwedish();
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public List<LeaderEmailDto> getLeaders() {
        return leaders;
    }
}

package fi.riista.feature.harvestpermit.report;

import com.google.common.collect.ImmutableList;

import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.harvest.HarvestLukeStatus;
import fi.riista.feature.gamediary.harvest.HuntingAreaType;
import fi.riista.feature.gamediary.harvest.HuntingMethod;

import java.util.List;

public class HarvestReportParametersDTO {
    private final List<GameAge> ages = ImmutableList.copyOf(GameAge.values());
    private final List<GameGender> genders = ImmutableList.copyOf(GameGender.values());
    private final List<HuntingMethod> huntingMethods = ImmutableList.copyOf(HuntingMethod.values());
    private final List<HuntingAreaType> huntingAreaTypes = ImmutableList.copyOf(HuntingAreaType.values());
    private final List<HarvestLukeStatus> lukeStatuses = ImmutableList.copyOf(HarvestLukeStatus.values());

    public HarvestReportParametersDTO() {
    }

    public List<GameAge> getAges() {
        return ages;
    }

    public List<GameGender> getGenders() {
        return genders;
    }

    public List<HuntingMethod> getHuntingMethods() {
        return huntingMethods;
    }

    public List<HuntingAreaType> getHuntingAreaTypes() {
        return huntingAreaTypes;
    }

    public List<HarvestLukeStatus> getLukeStatuses() {
        return lukeStatuses;
    }
}

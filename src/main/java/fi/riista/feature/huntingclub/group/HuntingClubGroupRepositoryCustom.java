package fi.riista.feature.huntingclub.group;

import fi.riista.feature.gamediary.GameDiaryEntry;

import java.util.List;

public interface HuntingClubGroupRepositoryCustom {

    List<HuntingClubGroup> findGroupsByAuthorAndActorWuthAreaIntersecting(GameDiaryEntry diaryEntry, int huntingYear);
}

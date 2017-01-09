package fi.riista.feature.gamediary;

import fi.riista.util.ListTransformer;
import fi.riista.util.jpa.CriteriaUtils;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import java.util.function.Function;

public abstract class GameDiaryEntryDTOTransformer<T extends GameDiaryEntry, DTO extends GameDiaryEntryDTO>
        extends ListTransformer<T, DTO> {

    @Resource
    protected GameSpeciesRepository gameSpeciesRepo;

    @Nonnull
    protected Function<T, GameSpecies> getGameDiaryEntryToSpeciesMapping(final Iterable<T> diaryEntries) {
        return CriteriaUtils.singleQueryFunction(diaryEntries, GameDiaryEntry::getSpecies, gameSpeciesRepo, true);
    }

}

package fi.riista.feature.gamediary;

import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.util.jpa.CriteriaUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.function.Function;

@Component
public class GameDiaryEntryDTOTransformerHelper {

    @Resource
    private PersonRepository personRepo;

    @Resource
    private GameSpeciesRepository gameSpeciesRepo;

    @Nonnull
    public <T extends GameDiaryEntry> Function<T, GameSpecies> createGameDiaryEntryToSpeciesMapping(
            final Iterable<T> diaryEntries) {

        return CriteriaUtils.singleQueryFunction(diaryEntries, GameDiaryEntry::getSpecies, gameSpeciesRepo, true);
    }

    @Nonnull
    public <T extends GameDiaryEntry> Function<T, Person> createAuthorMapping(final Iterable<T> diaryEntries) {
        return createPersonMapping(diaryEntries, GameDiaryEntry::getAuthor, true);
    }

    @Nonnull
    public <T extends GameDiaryEntry> Function<T, Person> createApproverToHuntingDayMapping(final Iterable<T> diaryEntries) {
        return createPersonMapping(diaryEntries, GameDiaryEntry::getApproverToHuntingDay, false);
    }

    @Nonnull
    public <T extends GameDiaryEntry> Function<T, Person> createPersonMapping(
            final Iterable<T> diaryEntries,
            final Function<? super T, Person> diaryEntryToPersonFunction) {

        return createPersonMapping(diaryEntries, diaryEntryToPersonFunction, true);
    }

    @Nonnull
    public <T extends GameDiaryEntry> Function<T, Person> createPersonMapping(
            final Iterable<T> diaryEntries,
            final Function<? super T, Person> diaryEntryToPersonFunction,
            final boolean letFunctionThrowExceptionOnNullResult) {

        return CriteriaUtils.singleQueryFunction(
                diaryEntries, diaryEntryToPersonFunction, personRepo, letFunctionThrowExceptionOnNullResult);
    }
}

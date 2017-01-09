package fi.riista.feature.gamediary;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.util.jpa.CriteriaUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;

import java.util.function.Function;

public abstract class HuntingDiaryEntryDTOTransformer<T extends GameDiaryEntry, DTO extends HuntingDiaryEntryDTO>
        extends GameDiaryEntryDTOTransformer<T, DTO> {

    @Resource
    protected PersonRepository personRepo;

    @Resource
    protected ActiveUserService activeUserService;

    @Nullable
    protected Person getAuthenticatedPerson() {
        return activeUserService.getActiveUser().getPerson();
    }

    @Nonnull
    protected Function<T, Person> getGameDiaryEntryToAuthorMapping(final Iterable<T> diaryEntries) {
        return createGameDiaryEntryToPersonMapping(diaryEntries, GameDiaryEntry::getAuthor);
    }

    @Nonnull
    protected Function<T, Person> createGameDiaryEntryToPersonMapping(
            final Iterable<T> diaryEntries,
            final Function<? super T, Person> diaryEntryPersonFunction) {

        return createGameDiaryEntryToPersonMapping(diaryEntries, diaryEntryPersonFunction, true);
    }

    @Nonnull
    protected Function<T, Person> createGameDiaryEntryToPersonMapping(
            final Iterable<T> diaryEntries,
            final Function<? super T, Person> diaryEntryPersonFunction,
            final boolean letFunctionThrowExceptionOnNullResult) {

        return CriteriaUtils.singleQueryFunction(diaryEntries, diaryEntryPersonFunction, personRepo, letFunctionThrowExceptionOnNullResult);
    }

}

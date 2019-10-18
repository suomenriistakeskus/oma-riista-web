package fi.riista.feature.gamediary;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonLookupService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Objects;

import static fi.riista.feature.gamediary.GameDiaryEntry.FOREIGN_PERSON_ELIGIBLE_AS_ACTOR;
import static fi.riista.feature.gamediary.GameDiaryEntry.FOREIGN_PERSON_ELIGIBLE_AS_AUTHOR;

@Component
public class GameDiaryEntryAuthorActorService {

    @Resource
    private PersonLookupService personLookupService;

    @Resource
    protected RequireEntityService requireEntityService;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void setAuthorAndActor(@Nonnull final GameDiaryEntry diaryEntry,
                                  @Nonnull final HasAuthorAndActor authorAndActor,
                                  @Nonnull final SystemUser activeUser) {

        Objects.requireNonNull(diaryEntry, "diaryEntry is null");
        Objects.requireNonNull(authorAndActor, "authorAndActor is null");
        Objects.requireNonNull(activeUser, "activeUser is null");

        if (activeUser.isModeratorOrAdmin()) {
            diaryEntry.setAuthor(requireAuthor(authorAndActor));
            diaryEntry.setActor(requireActor(authorAndActor));

        } else if (activeUser.getPerson() != null) {
            diaryEntry.setAuthor(!diaryEntry.isNew() ? diaryEntry.getAuthor() : activeUser.getPerson());
            diaryEntry.setActor(personLookupService
                    .findPerson(authorAndActor.getActorInfo(), FOREIGN_PERSON_ELIGIBLE_AS_ACTOR)
                    .orElseGet(diaryEntry::getAuthor));

        } else {
            throw new IllegalStateException("Active person is null");
        }
    }

    @Nonnull
    private Person requireAuthor(@Nonnull final HasAuthorAndActor authorAndActor) {
        return personLookupService
                .findPerson(authorAndActor.getAuthorInfo(), FOREIGN_PERSON_ELIGIBLE_AS_AUTHOR)
                .orElseThrow(() -> new IllegalArgumentException("Author not specified"));
    }

    @Nonnull
    private Person requireActor(@Nonnull final HasAuthorAndActor authorAndActor) {
        return personLookupService
                .findPerson(authorAndActor.getActorInfo(), FOREIGN_PERSON_ELIGIBLE_AS_ACTOR)
                .orElseThrow(() -> new IllegalArgumentException("Actor not specified"));
    }
}

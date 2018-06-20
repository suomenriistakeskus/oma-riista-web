package fi.riista.feature.gamediary;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonLookupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Objects;

@Service
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
            diaryEntry.setActor(personLookupService.findPerson(authorAndActor.getActorInfo()).orElse(diaryEntry.getAuthor()));
        } else {
            throw new IllegalStateException("Active person is null");
        }
    }

    @Nonnull
    private Person requireAuthor(@Nonnull final HasAuthorAndActor authorAndActor) {
        return personLookupService.findPerson(authorAndActor.getAuthorInfo())
                .orElseThrow(() -> new IllegalArgumentException("Author not specified"));
    }

    @Nonnull
    private Person requireActor(@Nonnull final HasAuthorAndActor authorAndActor) {
        return personLookupService.findPerson(authorAndActor.getActorInfo())
                .orElseThrow(() -> new IllegalArgumentException("Actor not specified"));
    }
}

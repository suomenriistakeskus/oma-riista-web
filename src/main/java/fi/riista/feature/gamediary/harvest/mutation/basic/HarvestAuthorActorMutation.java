package fi.riista.feature.gamediary.harvest.mutation.basic;

import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.mutation.HarvestMutation;
import fi.riista.feature.organization.person.Person;

import java.util.Objects;

public class HarvestAuthorActorMutation implements HarvestMutation {
    public static HarvestAuthorActorMutation createForModerator(final Person author,
                                                                final Person actor) {
        return new HarvestAuthorActorMutation(author, actor);
    }

    public static HarvestAuthorActorMutation createForNormalUser(final Person activePerson,
                                                                 final Person actor,
                                                                 final Person previousAuthor) {
        final Person author = previousAuthor != null ? previousAuthor : activePerson;
        return new HarvestAuthorActorMutation(author, actor != null ? actor : author);
    }

    private final Person author;
    private final Person actor;

    private HarvestAuthorActorMutation(final Person author, final Person actor) {
        this.author = Objects.requireNonNull(author);
        this.actor = Objects.requireNonNull(actor);
    }

    @Override
    public void accept(final Harvest harvest) {
        harvest.setAuthor(author);
        harvest.setActor(actor);
    }

    public Person getAuthor() {
        return author;
    }

    public Person getActor() {
        return actor;
    }
}

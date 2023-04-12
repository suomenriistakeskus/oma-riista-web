package fi.riista.feature.organization.fixture;

import fi.riista.feature.common.fixture.FixtureMixin;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.METSASTYKSENVALVOJA;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;

public interface OrganisationFixtureMixin extends FixtureMixin {

    default void withRhy(final Consumer<Riistanhoitoyhdistys> consumer) {
        consumer.accept(getEntitySupplier().newRiistanhoitoyhdistys());
    }

    default void withRhy(final String officialCode, final RiistakeskuksenAlue rka, final Consumer<Riistanhoitoyhdistys> consumer) {
        consumer.accept(getEntitySupplier().newRiistanhoitoyhdistys(rka, officialCode));
    }

    default void withPerson(final Consumer<Person> consumer) {
        consumer.accept(getEntitySupplier().newPersonWithAddress());
    }

    default void withForeignPerson(final Consumer<Person> consumer) {
        consumer.accept(getEntitySupplier().newForeignPerson());
    }

    default void withRhyAndCoordinator(final BiConsumer<Riistanhoitoyhdistys, Person> consumer) {
        withRhyAndCoordinatorOccupation((rhy, occupation) -> consumer.accept(rhy, occupation.getPerson()));
    }

    default void withRhyAndCoordinatorOccupation(final BiConsumer<Riistanhoitoyhdistys, Occupation> consumer) {
        withRhy(rhy -> withPerson(coordinator -> {
            coordinator.setRhyMembership(rhy);
            consumer.accept(rhy, getEntitySupplier().newOccupation(rhy, coordinator, TOIMINNANOHJAAJA));
        }));
    }

    default void withRhyAndShootingTestOfficial(final BiConsumer<Riistanhoitoyhdistys, Person> consumer) {
        withRhyAndShootingTestOfficialOccupation((rhy, occupation) -> consumer.accept(rhy, occupation.getPerson()));
    }

    default void withRhyAndShootingTestOfficialOccupation(final BiConsumer<Riistanhoitoyhdistys, Occupation> consumer) {
        withRhy(rhy -> withPerson(official -> {
            official.setRhyMembership(rhy);
            consumer.accept(rhy, getEntitySupplier().newOccupation(rhy, official, AMPUMAKOKEEN_VASTAANOTTAJA));
        }));
    }

    default void withRhyAndOccupiedPerson(final BiConsumer<Riistanhoitoyhdistys, Person> consumer) {
        withRhyAndSomeOccupation((rhy, occupation) -> consumer.accept(rhy, occupation.getPerson()));
    }

    default void withRhyAndSomeOccupation(final BiConsumer<Riistanhoitoyhdistys, Occupation> consumer) {
        withRhy(rhy -> withPerson(person -> {
            person.setRhyMembership(rhy);
            consumer.accept(rhy, getEntitySupplier().newOccupation(rhy, person));
        }));
    }

    default void withRhyAndGameWarden(final BiConsumer<Riistanhoitoyhdistys, Person> consumer) {
        withRhyAndGameWardenOccupation((rhy, occupation) -> consumer.accept(rhy, occupation.getPerson()));
    }

    default void withRhyAndGameWardenOccupation(final BiConsumer<Riistanhoitoyhdistys, Occupation> consumer) {
        withRhy(rhy -> withPerson(gameWarden -> {
            gameWarden.setRhyMembership(rhy);
            consumer.accept(rhy, getEntitySupplier().newOccupation(rhy, gameWarden, METSASTYKSENVALVOJA));
        }));
    }
}

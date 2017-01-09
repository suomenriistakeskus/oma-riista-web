package fi.riista.feature.gamediary.srva;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesRepository;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.image.GameDiaryImageRepository;
import fi.riista.feature.gamediary.image.GameDiaryImage_;
import fi.riista.feature.gamediary.srva.method.SrvaMethod;
import fi.riista.feature.gamediary.srva.method.SrvaMethodRepository;
import fi.riista.feature.gamediary.srva.method.SrvaMethod_;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimen;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimenRepository;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimen_;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.util.jpa.CriteriaUtils;
import fi.riista.util.jpa.JpaGroupingUtils;
import org.springframework.data.jpa.domain.JpaSort;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class SrvaJpaUtils {

    @Nonnull
    public static Function<SrvaEvent, GameSpecies> getSrvaEventToSpeciesMapping(
            final Iterable<SrvaEvent> srvaEvents, final GameSpeciesRepository gameSpeciesRepo) {

        return CriteriaUtils.singleQueryFunction(srvaEvents, SrvaEvent::getSpecies, gameSpeciesRepo, false);
    }

    @Nonnull
    public static Function<SrvaEvent, Person> getSrvaEventToAuthorMapping(
            final Iterable<SrvaEvent> srvaEvents, final PersonRepository personRepo) {

        return CriteriaUtils.singleQueryFunction(srvaEvents, SrvaEvent::getAuthor, personRepo, true);
    }

    @Nonnull
    public static Function<SrvaEvent, Riistanhoitoyhdistys> getSrvaEventToRhyMapping(
            final Iterable<SrvaEvent> srvaEvents, final RiistanhoitoyhdistysRepository riistanhoitoyhdistysRepo) {

        return CriteriaUtils.singleQueryFunction(srvaEvents, SrvaEvent::getRhy, riistanhoitoyhdistysRepo, true);
    }

    @Nonnull
    public static Map<SrvaEvent, List<SrvaSpecimen>> getSpecimensGroupedBySrvaEvent(
            final Collection<SrvaEvent> srvaEvents, final SrvaSpecimenRepository srvaSpecimenRepo) {

        return JpaGroupingUtils.groupRelations(srvaEvents, SrvaSpecimen_.event, srvaSpecimenRepo, new JpaSort(SrvaSpecimen_.id));
    }

    @Nonnull
    public static Map<SrvaEvent, List<SrvaMethod>> getMethodsGroupedBySrvaEvent(
            final Collection<SrvaEvent> srvaEvents, final SrvaMethodRepository srvaMethodRepo) {

        return JpaGroupingUtils.groupRelations(srvaEvents, SrvaMethod_.event, srvaMethodRepo, new JpaSort(SrvaMethod_.id));
    }

    @Nonnull
    public static Map<SrvaEvent, List<GameDiaryImage>> getImagesGroupedBySrvaEvent(
            final Collection<SrvaEvent> srvaEvents, final GameDiaryImageRepository gameDiaryImageRepo) {

        return JpaGroupingUtils.groupRelations(srvaEvents, GameDiaryImage_.srvaEvent, gameDiaryImageRepo);
    }

    @Nonnull
    public static Function<SrvaEvent, SystemUser> getSrvaEventToApproverAsUserMapping(
            final Iterable<SrvaEvent> srvaEvents, final UserRepository userRepo) {

        return CriteriaUtils.singleQueryFunction(srvaEvents, SrvaEvent::getApproverAsUser, userRepo, false);
    }

    @Nonnull
    public static Function<SrvaEvent, Person> getSrvaEventToApproverAsPersonMapping(
            final Iterable<SrvaEvent> srvaEvents, final PersonRepository personRepo) {
        return CriteriaUtils.singleQueryFunction(srvaEvents, SrvaEvent::getApproverAsPerson, personRepo, false);
    }

}

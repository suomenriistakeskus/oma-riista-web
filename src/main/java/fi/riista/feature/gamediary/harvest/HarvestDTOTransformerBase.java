package fi.riista.feature.gamediary.harvest;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.gamediary.GameDiaryEntryDTOTransformerHelper;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenRepository;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen_;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.image.GameDiaryImageRepository;
import fi.riista.feature.gamediary.image.GameDiaryImage_;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpecs;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.Filters;
import fi.riista.util.jpa.CriteriaUtils;
import fi.riista.util.jpa.JpaGroupingUtils;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

public abstract class HarvestDTOTransformerBase<DTO extends HarvestDTOBase> {

    @Resource
    protected HarvestSpecimenRepository specimenRepo;

    @Resource
    protected GameDiaryImageRepository gameDiaryImageRepo;

    @Resource
    protected HarvestPermitRepository harvestPermitRepository;

    @Resource
    protected ActiveUserService activeUserService;

    @Resource
    private GameDiaryEntryDTOTransformerHelper helper;

    protected abstract List<DTO> transform(@Nonnull List<Harvest> list, @Nonnull HarvestSpecVersion specVersion);

    // Transactional propagation not mandated since entity associations are not traversed.
    @Nullable
    @Transactional(readOnly = true)
    public List<DTO> apply(@Nullable final List<Harvest> list, @Nonnull final HarvestSpecVersion specVersion) {
        requireNonNull(specVersion);

        if (list == null) {
            return null;
        }

        return list.isEmpty() ? emptyList() : transform(list, specVersion);
    }

    // Transactional propagation not mandated since entity associations are not traversed.
    @Nullable
    @Transactional(readOnly = true)
    public DTO apply(@Nullable final Harvest harvest, @Nonnull final HarvestSpecVersion specVersion) {
        if (harvest == null) {
            return null;
        }

        final List<DTO> singletonList = transform(singletonList(harvest), specVersion);

        if (singletonList.size() != 1) {
            throw new IllegalStateException(
                    "Expected list containing exactly one harvest but has: " + singletonList.size());
        }

        return singletonList.get(0);
    }

    @Nullable
    protected Person getAuthenticatedPerson() {
        return activeUserService.requireActiveUser().getPerson();
    }

    @Nonnull
    protected Function<Harvest, GameSpecies> getHarvestToSpeciesMapping(final Iterable<Harvest> harvests) {
        return helper.createGameDiaryEntryToSpeciesMapping(harvests);
    }

    @Nonnull
    protected Function<Harvest, Person> getHarvestToAuthorMapping(final Iterable<Harvest> harvests) {
        return helper.createAuthorMapping(harvests);
    }

    @Nonnull
    protected Function<Harvest, Person> getHarvestToShooterMapping(final Iterable<Harvest> harvests) {
        return helper.createPersonMapping(harvests, Harvest::getActualShooter, true);
    }

    @Nonnull
    protected Function<Harvest, Person> getHarvestToApproverToHuntingDayMapping(final Iterable<Harvest> harvests) {
        return helper.createApproverToHuntingDayMapping(harvests);
    }

    @Nonnull
    protected Map<Harvest, List<HarvestSpecimen>> getSpecimensGroupedByHarvests(final Collection<Harvest> harvests) {
        return JpaGroupingUtils.groupRelations(
                harvests, HarvestSpecimen_.harvest, specimenRepo, JpaSort.of(HarvestSpecimen_.id));
    }

    @Nonnull
    protected Map<Harvest, List<GameDiaryImage>> getImagesGroupedByHarvests(final Collection<Harvest> harvests) {
        return JpaGroupingUtils.groupRelations(harvests, GameDiaryImage_.harvest, gameDiaryImageRepo);
    }

    @Nonnull
    protected Function<Harvest, HarvestPermit> getHarvestToPermitMapping(final Iterable<Harvest> harvests) {
        return CriteriaUtils.singleQueryFunction(harvests, Harvest::getHarvestPermit, harvestPermitRepository, false);
    }

    @Nonnull
    protected Predicate<Harvest> getContactPersonOfPermittedHarvestTester(final Person person) {
        final List<HarvestPermit> permits =
                harvestPermitRepository.findAll(HarvestPermitSpecs.isPermitContactPerson(person));

        return Filters.hasRelationWithAny(Harvest::getHarvestPermit, permits);
    }
}

package fi.riista.feature.gamediary.harvest;

import fi.riista.feature.gamediary.HuntingDiaryEntryDTOTransformer;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.image.GameDiaryImage_;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen_;
import fi.riista.feature.gamediary.image.GameDiaryImageRepository;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpecs;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.report.HarvestReportRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.Filters;
import fi.riista.util.jpa.CriteriaUtils;
import fi.riista.util.jpa.JpaGroupingUtils;

import org.springframework.data.jpa.domain.JpaSort;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class HarvestDTOTransformerBase<DTO extends HarvestDTOBase>
        extends HuntingDiaryEntryDTOTransformer<Harvest, DTO> {

    protected static boolean canEdit(
            final Person person,
            final Harvest harvest,
            final HarvestReport report,
            final Predicate<Harvest> isContactPersonOfPermittedHarvestTester) {

        if (report != null && !report.isInDeletedState()) {
            return false;
        }

        if (harvest.getHuntingDayOfGroup() != null) {
            return false;
        }

        if (harvest.getHarvestPermit() != null
                && harvest.getStateAcceptedToHarvestPermit() == Harvest.StateAcceptedToHarvestPermit.ACCEPTED) {

            // Person is null in case user is moderator or admin
            return person == null || isContactPersonOfPermittedHarvestTester.test(harvest);
        }

        return harvest.isAuthorOrActor(person);
    }

    @Resource
    protected HarvestSpecimenRepository specimenRepo;

    @Resource
    protected GameDiaryImageRepository gameDiaryImageRepo;

    @Resource
    protected HarvestReportRepository harvestReportRepository;

    @Resource
    protected HarvestPermitRepository harvestPermitRepository;

    @Nonnull
    protected Map<Harvest, List<HarvestSpecimen>> getSpecimensGroupedByHarvests(final Collection<Harvest> harvests) {
        return JpaGroupingUtils.groupRelations(
                harvests, HarvestSpecimen_.harvest, specimenRepo, new JpaSort(HarvestSpecimen_.id));
    }

    @Nonnull
    protected Map<Harvest, List<GameDiaryImage>> getImagesGroupedByHarvests(final Collection<Harvest> harvests) {
        return JpaGroupingUtils.groupRelations(harvests, GameDiaryImage_.harvest, gameDiaryImageRepo);
    }

    @Nonnull
    protected Function<Harvest, HarvestReport> getHarvestToReportMapping(final Iterable<Harvest> harvests) {
        return CriteriaUtils.singleQueryFunction(harvests, Harvest::getHarvestReport, harvestReportRepository, false);
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

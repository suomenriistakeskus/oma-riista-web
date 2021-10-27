package fi.riista.feature.huntingclub.hunting.mobile;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTOTransformerBase;
import fi.riista.feature.gamediary.harvest.HarvestLockedCondition;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.F;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@Component
public class MobileGroupHarvestDTOTransformer extends HarvestDTOTransformerBase<MobileGroupHarvestDTO>  {
    protected List<MobileGroupHarvestDTO> transform(@Nonnull final List<Harvest> harvests,
                                               @Nonnull final HarvestSpecVersion specVersion) {

        requireNonNull(harvests, "harvests is null");
        requireNonNull(specVersion, "specVersion is null");

        final Function<Harvest, GameSpecies> harvestToSpecies = getHarvestToSpeciesMapping(harvests);

        final Map<Harvest, List<GameDiaryImage>> groupedImages = getImagesGroupedByHarvests(harvests);
        final Map<Harvest, List<HarvestSpecimen>> groupedSpecimens = getSpecimensGroupedByHarvests(harvests);

        final Function<Harvest, HarvestPermit> harvestToPermit = getHarvestToPermitMapping(harvests);
        final Predicate<Harvest> contactPersonTester = getContactPersonOfPermittedHarvestTester(getAuthenticatedPerson());
        final Predicate<Harvest> groupHuntingStatusTester = harvest -> harvest.getHuntingDayOfGroup() != null;

        final Person authenticatedPerson = getAuthenticatedPerson();

        final Function<Harvest, Person> harvestToAuthor = getHarvestToAuthorMapping(harvests);
        final Function<Harvest, Person> harvestToHunter = getHarvestToShooterMapping(harvests);

        final Function<Harvest, Person> harvestToHuntingDayApprover = getHarvestToApproverToHuntingDayMapping(harvests);

        return harvests.stream()
                .filter(Objects::nonNull)
                .map(harvest -> {
                    final GameSpecies species = harvestToSpecies.apply(harvest);

                    final boolean canEdit = HarvestLockedCondition.canEditFromMobile(
                            authenticatedPerson, harvest, specVersion, groupHuntingStatusTester, contactPersonTester);

                    final Person author = harvestToAuthor.apply(harvest);
                    final Person hunter = harvestToHunter.apply(harvest);
                    final boolean authorOrActor = author.equals(authenticatedPerson) || hunter.equals(authenticatedPerson);

                    final Person approverToHuntingDay = harvestToHuntingDayApprover.apply(harvest);

                    final MobileGroupHarvestDTO dto = MobileGroupHarvestDTO.builder(specVersion)
                            .populateWith(harvest)
                            .withGameSpeciesCode(species.getOfficialCode())
                            .populateWith(harvestToPermit.apply(harvest))
                            .withSpecimensMappedFrom(groupedSpecimens.get(harvest))
                            .populateWith(groupedImages.get(harvest))
                            .withCanEdit(canEdit)
                            .withHuntingDayId(F.getId(harvest.getHuntingDayOfGroup()))
                            .withAuthorInfo(author)
                            .withActorInfo(hunter)
                            .withApproverToHuntingDay(approverToHuntingDay)
                            .build();

                    if (!authorOrActor) {
                        dto.setDescription(null);
                        dto.getImageIds().clear();
                    }

                    return dto;
                })
                .collect(toList());
    }

}

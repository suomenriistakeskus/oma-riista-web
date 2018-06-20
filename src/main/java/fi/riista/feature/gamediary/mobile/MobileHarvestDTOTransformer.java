package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTOTransformerBase;
import fi.riista.feature.gamediary.harvest.HarvestLockedCondition;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.organization.person.Person;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

@Component
public class MobileHarvestDTOTransformer extends HarvestDTOTransformerBase<MobileHarvestDTO> {

    // Transactional propagation not mandated since entity associations are not traversed.
    @Transactional(readOnly = true)
    @Nullable
    public List<MobileHarvestDTO> apply(@Nullable final List<Harvest> list,
                                        @Nonnull final HarvestSpecVersion specVersion) {

        return list == null ? null : transform(list, specVersion);
    }

    // Transactional propagation not mandated since entity associations are not traversed.
    @Transactional(readOnly = true)
    @Nullable
    public MobileHarvestDTO apply(@Nullable final Harvest harvest, @Nonnull final HarvestSpecVersion specVersion) {
        if (harvest == null) {
            return null;
        }

        final List<MobileHarvestDTO> singletonList = apply(Collections.singletonList(harvest), specVersion);

        if (singletonList.size() != 1) {
            throw new IllegalStateException(
                    "Expected list containing exactly one harvest but has: " + singletonList.size());
        }

        return singletonList.get(0);
    }

    @Override
    protected List<MobileHarvestDTO> transform(final List<Harvest> harvests) {
        throw new UnsupportedOperationException("No transformation without harvestSpecVersion supported");
    }

    protected List<MobileHarvestDTO> transform(@Nonnull final List<Harvest> harvests,
                                               @Nonnull final HarvestSpecVersion specVersion) {

        Objects.requireNonNull(harvests, "harvests is null");
        Objects.requireNonNull(specVersion, "specVersion is null");

        final Function<Harvest, GameSpecies> harvestToSpecies = getGameDiaryEntryToSpeciesMapping(harvests);

        final Map<Harvest, List<GameDiaryImage>> groupedImages = getImagesGroupedByHarvests(harvests);
        final Map<Harvest, List<HarvestSpecimen>> groupedSpecimens = getSpecimensGroupedByHarvests(harvests);

        final Function<Harvest, HarvestPermit> harvestToPermit = getHarvestToPermitMapping(harvests);
        final Predicate<Harvest> contactPersonTester = getContactPersonOfPermittedHarvestTester(getAuthenticatedPerson());
        final Predicate<Harvest> groupHuntingStatusTester = harvest -> harvest.getHuntingDayOfGroup() != null;

        final Person authenticatedPerson = getAuthenticatedPerson();

        return harvests.stream()
                .filter(Objects::nonNull)
                .map(harvest -> {
                    final boolean canEdit = HarvestLockedCondition.canEdit(
                            authenticatedPerson, harvest, specVersion,
                            groupHuntingStatusTester, contactPersonTester);

                    return MobileHarvestDTO.builder(specVersion)
                            .populateWith(harvest)
                            .populateWith(harvestToSpecies.apply(harvest))
                            .populateWith(specVersion.supportsHarvestPermitState() ? harvestToPermit.apply(harvest) : null)
                            .populateSpecimensWith(groupedSpecimens.get(harvest))
                            .populateWith(groupedImages.get(harvest))
                            .withCanEdit(canEdit)
                            .build();
                })
                .collect(toList());
    }

}

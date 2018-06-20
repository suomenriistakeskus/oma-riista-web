package fi.riista.feature.gamediary.summary;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.HarvestDTOTransformerBase;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

@Component
public class AdminSummaryHarvestDTOTransformer extends HarvestDTOTransformerBase<HarvestDTO> {

    @Nonnull
    @Override
    protected List<HarvestDTO> transform(@Nonnull final List<Harvest> harvests) {
        final Function<Harvest, GameSpecies> harvestToSpecies = getGameDiaryEntryToSpeciesMapping(harvests);
        final Map<Harvest, List<HarvestSpecimen>> groupedSpecimens = getSpecimensGroupedByHarvests(harvests);

        return harvests.stream()
                .filter(Objects::nonNull)
                .map(harvest -> createDTO(harvest, harvestToSpecies.apply(harvest), groupedSpecimens.get(harvest)))
                .collect(toList());
    }

    private static HarvestDTO createDTO(final Harvest harvest,
                                        final GameSpecies species,
                                        final List<HarvestSpecimen> specimens) {

        return HarvestDTO.builder()
                .populateWith(harvest)
                .populateWith(species)
                .populateSpecimensWith(specimens)
                .withDescription(null)
                .withCanEdit(false)
                .build();
    }
}

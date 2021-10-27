package fi.riista.feature.harvestregistry;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesRepository;
import fi.riista.util.F;
import fi.riista.util.ListTransformer;
import fi.riista.util.jpa.CriteriaUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

@Component
public class HarvestRegistryItemToDTOTransformer {

    @Resource
    private GameSpeciesRepository gameSpeciesRepository;

    @Nonnull
    private Function<HarvestRegistryItem, GameSpecies> getGameDiaryEntryToSpeciesMapping(final Iterable<HarvestRegistryItem> items) {
        return CriteriaUtils.singleQueryFunction(items, HarvestRegistryItem::getSpecies, gameSpeciesRepository, true);
    }

    @Nonnull
    public List<HarvestRegistryItemDTO> transform(@Nonnull final List<HarvestRegistryItem> list,
                                                  @Nonnull final HarvestRegistryItemDTO.Fields includedFields) {
        requireNonNull(includedFields);

        final Function<HarvestRegistryItem, GameSpecies> gameDiaryEntryToSpeciesMapping =
                getGameDiaryEntryToSpeciesMapping(list);

        return F.mapNonNullsToList(list,
                item -> HarvestRegistryItemDTO.from(item, () -> gameDiaryEntryToSpeciesMapping.apply(item), includedFields));
    }
}

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

@Component
public class HarvestRegistryItemToDTOTransformer extends ListTransformer<HarvestRegistryItem, HarvestRegistryItemDTO> {

    @Resource
    private GameSpeciesRepository gameSpeciesRepository;

    @Nonnull
    private Function<HarvestRegistryItem, GameSpecies> getGameDiaryEntryToSpeciesMapping(final Iterable<HarvestRegistryItem> items) {
        return CriteriaUtils.singleQueryFunction(items, HarvestRegistryItem::getSpecies, gameSpeciesRepository, true);
    }

    @Nonnull
    @Override
    protected List<HarvestRegistryItemDTO> transform(@Nonnull final List<HarvestRegistryItem> list) {

        final Function<HarvestRegistryItem, GameSpecies> gameDiaryEntryToSpeciesMapping =
                getGameDiaryEntryToSpeciesMapping(list);

        return F.mapNonNullsToList(list,
                item -> HarvestRegistryItemDTO.from(item, () -> gameDiaryEntryToSpeciesMapping.apply(item)));
    }
}

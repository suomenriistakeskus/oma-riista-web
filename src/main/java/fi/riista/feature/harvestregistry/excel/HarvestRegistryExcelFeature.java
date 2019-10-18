package fi.riista.feature.harvestregistry.excel;

import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.harvestregistry.HarvestRegistryItem;
import fi.riista.feature.harvestregistry.HarvestRegistryItemDTO;
import fi.riista.feature.harvestregistry.HarvestRegistryItemRepository;
import fi.riista.feature.harvestregistry.HarvestRegistryItemToDTOTransformer;
import fi.riista.feature.harvestregistry.HarvestRegistryQueries;
import fi.riista.feature.harvestregistry.HarvestRegistryRequestDTO;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;

import static fi.riista.feature.harvestregistry.HarvestRegistryQueries.POINT_OF_TIME_ORDERING;

@Component
public class HarvestRegistryExcelFeature {

    @Resource
    private HarvestRegistryItemRepository harvestRegistryItemRepository;

    @Resource
    private MessageSource messageSource;

    @Resource
    private HarvestRegistryItemToDTOTransformer harvestRegistryItemToDTOTransformer;

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasPrivilege('HARVEST_REGISTRY')")
    public HarvestRegistryExcelView export(final HarvestRegistryRequestDTO dto,
                                           final Locale locale) {
        final EnumLocaliser localiser = new EnumLocaliser(messageSource, locale);
        final List<HarvestRegistryItem> items =
                harvestRegistryItemRepository.findAllAsList(HarvestRegistryQueries.predicateFromDTO(dto),
                        POINT_OF_TIME_ORDERING);

        final List<HarvestRegistryItemDTO> dtos = harvestRegistryItemToDTOTransformer.apply(items);
        return new HarvestRegistryExcelView(localiser, dtos);
    }
}

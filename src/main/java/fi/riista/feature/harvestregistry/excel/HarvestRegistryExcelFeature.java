package fi.riista.feature.harvestregistry.excel;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.harvestregistry.HarvestRegistryCoordinatorRequestDTO;
import fi.riista.feature.harvestregistry.HarvestRegistryCoordinatorSearchReason;
import fi.riista.feature.harvestregistry.HarvestRegistryItem;
import fi.riista.feature.harvestregistry.HarvestRegistryItemDTO;
import fi.riista.feature.harvestregistry.HarvestRegistryItemRepository;
import fi.riista.feature.harvestregistry.HarvestRegistryItemToDTOTransformer;
import fi.riista.feature.harvestregistry.HarvestRegistryQueries;
import fi.riista.feature.harvestregistry.HarvestRegistryRequestDTO;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;

import static fi.riista.feature.harvestregistry.HarvestRegistryCoordinatorSearchReason.HUNTING_CONTROL;
import static fi.riista.feature.harvestregistry.HarvestRegistryItemDTO.Fields.COMMON;
import static fi.riista.feature.harvestregistry.HarvestRegistryItemDTO.Fields.COMMON_WITH_SHOOTER;
import static fi.riista.feature.harvestregistry.HarvestRegistryItemDTO.Fields.FULL;
import static fi.riista.feature.harvestregistry.HarvestRegistryQueries.POINT_OF_TIME_ORDERING;
import static fi.riista.security.EntityPermission.READ;

@Component
public class HarvestRegistryExcelFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private UserAuthorizationHelper authorizationHelper;

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

        final List<HarvestRegistryItemDTO> dtos = harvestRegistryItemToDTOTransformer.transform(items, FULL);
        return new HarvestRegistryExcelView(localiser, dtos, FULL);
    }

    @Transactional(readOnly = true)
    public HarvestRegistryExcelView exportRhy(final HarvestRegistryCoordinatorRequestDTO dto,
                                              final Locale locale) {
        authorizationHelper.assertCoordinatorOrModerator(dto.getRhyId());
        final Riistanhoitoyhdistys rhy = requireEntityService.requireRiistanhoitoyhdistys(dto.getRhyId(), READ);

        final HarvestRegistryRequestDTO requestDTO = dto.toHarvestReqistryRequestDTO(rhy);

        final EnumLocaliser localiser = new EnumLocaliser(messageSource, locale);
        final List<HarvestRegistryItem> items =
                harvestRegistryItemRepository.findAllAsList(HarvestRegistryQueries.predicateFromDTO(requestDTO),
                        POINT_OF_TIME_ORDERING);

        final HarvestRegistryItemDTO.Fields includedFields = dto.getSearchReason() == HUNTING_CONTROL
                ? COMMON_WITH_SHOOTER
                : COMMON;

        final List<HarvestRegistryItemDTO> dtos = harvestRegistryItemToDTOTransformer.transform(items, includedFields);
        return new HarvestRegistryExcelView(localiser, dtos, includedFields);
    }
}

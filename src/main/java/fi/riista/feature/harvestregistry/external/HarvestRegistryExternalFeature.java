package fi.riista.feature.harvestregistry.external;

import com.querydsl.core.types.Predicate;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.harvestregistry.HarvestRegistryItem;
import fi.riista.feature.harvestregistry.HarvestRegistryItemDTO;
import fi.riista.feature.harvestregistry.HarvestRegistryItemRepository;
import fi.riista.feature.harvestregistry.HarvestRegistryItemToDTOTransformer;
import fi.riista.feature.harvestregistry.HarvestRegistryQueries;
import fi.riista.feature.harvestregistry.external.HarvestRegistryExternalRequestDTO;
import fi.riista.security.UserInfo;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HarvestRegistryExternalFeature {

    @Resource
    private HarvestRegistryItemRepository harvestRegistryItemRepository;

    @Resource
    private HarvestRegistryItemToDTOTransformer harvestRegistryItemToDTOTransformer;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private HarvestRegistryExternalAuditService harvestRegistryExternalAuditService;

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_HARVEST_REGISTRY_WITH_CONTROL_CRITERION') or hasPrivilege('EXPORT_HARVEST_REGISTRY_WITH_MONITORING_CRITERION')")
    public List<HarvestRegistryItemDTO> export(final HarvestRegistryExternalRequestDTO dto) {
        switch (dto.getReason()) {
            case HUNTING_CONTROL: {
                assertPrivilege(SystemUserPrivilege.EXPORT_HARVEST_REGISTRY_WITH_CONTROL_CRITERION);
                return exportHuntingControl(dto);
            }
            case HUNTING_MONITORING: {
                assertPrivilege(SystemUserPrivilege.EXPORT_HARVEST_REGISTRY_WITH_MONITORING_CRITERION);
                return exportHuntingMonitoring(dto);
            }
            default: {
                throw new IllegalArgumentException("Unknown query reason");
            }
        }
    }

    private void assertPrivilege(SystemUserPrivilege requiredPrivilege) {
        Optional.ofNullable(activeUserService.getActiveUserInfoOrNull())
                .map(info -> info.hasPrivilege(requiredPrivilege))
                .filter(value -> value)
                .orElseThrow(() -> new AccessDeniedException("Function not allowed"));
    }

    private void assertPageInfoValid(HarvestRegistryExternalRequestDTO dto) {
        if (dto.getPage() == null || dto.getPageSize() == null || dto.getPageSize() < 1 || dto.getPage() < 0) {
            throw new IllegalArgumentException("Page arguments must be set for this query");
        }
    }

    private List<HarvestRegistryItemDTO> exportHuntingControl(final HarvestRegistryExternalRequestDTO dto) {
        assertPageInfoValid(dto);

        return getHarvestRegistryItemDTOS(dto, HarvestRegistryItemDTO.Fields.COMMON_WITH_SHOOTER);
    }

    private List<HarvestRegistryItemDTO> exportHuntingMonitoring(final HarvestRegistryExternalRequestDTO dto) {
        assertPageInfoValid(dto);

        if (dto.getShooterHunterNumber() != null) {
            throw new IllegalArgumentException("Shooter hunter number is not allowed in this request");
        }

        return getHarvestRegistryItemDTOS(dto, HarvestRegistryItemDTO.Fields.COMMON);
    }

    private List<HarvestRegistryItemDTO> getHarvestRegistryItemDTOS(final HarvestRegistryExternalRequestDTO dto, final HarvestRegistryItemDTO.Fields includedFields) {
        final Predicate predicate = HarvestRegistryQueries.predicateFromDTO(dto);
        final Pageable pageRequest = PageRequest.of(dto.getPage(), dto.getPageSize());

        final Slice<HarvestRegistryItem> slice = harvestRegistryItemRepository.findAllAsSlice(predicate,
                pageRequest, HarvestRegistryQueries.POINT_OF_TIME_ORDERING);
        final List<HarvestRegistryItem> items = slice.stream().collect(Collectors.toList());

        return harvestRegistryItemToDTOTransformer.transform(items, includedFields);
    }
}
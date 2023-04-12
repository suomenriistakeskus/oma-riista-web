package fi.riista.api.external;

import fi.riista.feature.harvestregistry.HarvestRegistryItemDTO;
import fi.riista.feature.harvestregistry.external.HarvestRegistryExternalAuditService;
import fi.riista.feature.harvestregistry.external.HarvestRegistryExternalFeature;
import fi.riista.feature.harvestregistry.external.HarvestRegistryExternalRequestDTO;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = HarvestRegistryExportApiResource.URL_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
public class HarvestRegistryExportApiResource {
    public static final String URL_PREFIX = "/api/v1/export";


    @Resource
    private HarvestRegistryExternalFeature harvestRegistryExternalFeature;

    @Resource
    private HarvestRegistryExternalAuditService harvestRegistryExternalAuditService;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "/harvestregistry", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<HarvestRegistryItemDTO> exportJson(final @RequestBody @Valid HarvestRegistryExternalRequestDTO dto) {
        final List<HarvestRegistryItemDTO> dtos = harvestRegistryExternalFeature.export(dto);
        harvestRegistryExternalAuditService.logRequest(dto);
        return dtos;
    }
}

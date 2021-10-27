package fi.riista.api.harvestregistry;

import fi.riista.feature.harvestregistry.HarvestRegistryAuditService;
import fi.riista.feature.harvestregistry.HarvestRegistryCoordinatorRequestDTO;
import fi.riista.feature.harvestregistry.HarvestRegistryFeature;
import fi.riista.feature.harvestregistry.HarvestRegistryItemDTO;
import fi.riista.feature.harvestregistry.HarvestRegistryRequestDTO;
import fi.riista.feature.harvestregistry.excel.HarvestRegistryExcelFeature;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Locale;

@RestController
@RequestMapping(value = HarvestRegistryApiResource.URL_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
public class HarvestRegistryApiResource {
    public static final String URL_PREFIX = "/api/v1/harvestregistry";

    @Resource
    private HarvestRegistryFeature harvestRegistryFeature;

    @Resource
    private HarvestRegistryExcelFeature harvestRegistryExcelFeature;

    @Resource
    private HarvestRegistryAuditService harvestRegistryAuditService;

    @PostMapping(value = "/me")
    public Slice<HarvestRegistryItemDTO> listMinePaged(final @RequestBody @Valid HarvestRegistryRequestDTO dto,
                                                       final Locale locale) {
        return harvestRegistryFeature.listMine(dto, locale);
    }

    @PostMapping(value = "/{personId:\\d+}")
    public Slice<HarvestRegistryItemDTO> listForPersonPaged(final @PathVariable long personId,
                                                            final @RequestBody @Valid HarvestRegistryRequestDTO dto) {
        return harvestRegistryFeature.listForPerson(personId, dto);
    }

    @PostMapping(value = "/excel")
    public ModelAndView exportExcel(final @RequestBody @Valid HarvestRegistryRequestDTO dto,
                                    final Locale locale) {
        return new ModelAndView(harvestRegistryExcelFeature.export(dto, locale));
    }

    @PostMapping
    public Slice<HarvestRegistryItemDTO> listPaged(final @RequestBody @Valid HarvestRegistryRequestDTO dto) {
        return harvestRegistryFeature.listPaged(dto);
    }

    @PostMapping(value = "/rhy")
    public Slice<HarvestRegistryItemDTO> listForCoordinator(final @RequestBody @Valid HarvestRegistryCoordinatorRequestDTO dto) {
        harvestRegistryAuditService.addSearch(dto);
        return harvestRegistryFeature.listForCoordinator(dto);
    }

    @PostMapping(value = "/rhy/excel")
    public ModelAndView exportRhyExcel(final @RequestBody @Valid HarvestRegistryCoordinatorRequestDTO dto,
                                       final Locale locale) {
        harvestRegistryAuditService.addSearch(dto);
        return new ModelAndView(harvestRegistryExcelFeature.exportRhy(dto, locale));
    }
}

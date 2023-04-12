package fi.riista.api.moderator;

import static fi.riista.util.MediaTypeExtras.APPLICATION_EXCEL_VALUE;

import com.fasterxml.jackson.core.JsonProcessingException;
import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.srva.SrvaEventSearchDTO;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventCrudFeature;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventDTO;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventExcelView;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventExportDTO;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventExportFeature;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventSearchParametersDTO;
import java.util.List;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping(value = ModeratorHuntingControlApiResource.RESOURCE_URL)
public class ModeratorHuntingControlApiResource {
    public static final String RESOURCE_URL = "api/v1/moderator/huntingcontrol";

    @Resource
    private HuntingControlEventCrudFeature huntingControlEventCrudFeature;

    @Resource
    private HuntingControlEventExportFeature exportFeature;

    @Resource
    private MessageSource messageSource;

    @Resource
    private CustomJacksonObjectMapper objectMapper;

    @PostMapping("/search")
    public List<HuntingControlEventDTO> search(@RequestBody @Valid final HuntingControlEventSearchParametersDTO dto) {
        return huntingControlEventCrudFeature.searchModerator(dto);
    }

    @PostMapping(value = "/search/excel", produces = APPLICATION_EXCEL_VALUE)
    public ModelAndView exportAllExcel(@RequestParam(value = "json") @NotBlank final String jsonData) throws JsonProcessingException {
        final HuntingControlEventSearchParametersDTO filter = objectMapper.readValue(jsonData, HuntingControlEventSearchParametersDTO.class);
        final List<HuntingControlEventExportDTO> events = exportFeature.exportAll(filter);

        return new ModelAndView(new HuntingControlEventExcelView(new EnumLocaliser(messageSource), events));
    }
}

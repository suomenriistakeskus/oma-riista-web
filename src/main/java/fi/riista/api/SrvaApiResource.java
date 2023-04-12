package fi.riista.api;

import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.srva.SrvaCrudFeature;
import fi.riista.feature.gamediary.srva.SrvaEventDTO;
import fi.riista.feature.gamediary.srva.SrvaEventExportExcelDTO;
import fi.riista.feature.gamediary.srva.SrvaEventListExcelView;
import fi.riista.feature.gamediary.srva.SrvaEventSearchDTO;
import fi.riista.feature.gamediary.srva.SrvaEventSpecVersion;
import fi.riista.feature.gamediary.srva.SrvaEventStateEnum;
import fi.riista.feature.gamediary.srva.SrvaParametersDTO;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import javax.validation.constraints.NotBlank;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/srva", produces = MediaType.APPLICATION_JSON_VALUE)
public class SrvaApiResource {

    @Resource
    private SrvaCrudFeature srvaCrudFeature;

    @Resource
    private CustomJacksonObjectMapper objectMapper;

    @Resource
    private MessageSource messageSource;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/parameters", method = RequestMethod.GET)
    public SrvaParametersDTO getSrvaParameters() {
        return srvaCrudFeature.getSrvaParameters(SrvaEventSpecVersion.MOST_RECENT);
    }

    @RequestMapping(value = "/srvaevent", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public SrvaEventDTO createSrvaEvent(@RequestBody @Valid SrvaEventDTO dto) {
        return srvaCrudFeature.createSrvaEvent(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/srvaevent/{id:\\d+}", method = RequestMethod.DELETE)
    public void deleteSrvaEvent(@PathVariable Long id) {
        srvaCrudFeature.deleteSrvaEvent(id);
    }

    @RequestMapping(value = "/srvaevent/{id:\\d+}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public SrvaEventDTO updateSrvaEvent(@RequestBody @Valid SrvaEventDTO dto) {
        return srvaCrudFeature.updateSrvaEvent(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/srvaevent/{id:\\d+}", method = RequestMethod.GET)
    public SrvaEventDTO getSrvaEvent(@PathVariable Long id) {
        return srvaCrudFeature.getSrvaEvent(id);
    }

    @RequestMapping(value = "/changestate/{id:\\d+}", method = RequestMethod.PUT)
    public SrvaEventDTO changeState(@PathVariable final Long id,
                                    @RequestParam final SrvaEventStateEnum newState,
                                    @RequestParam final Integer rev) {
        return srvaCrudFeature.changeState(id, rev, newState);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public List<SrvaEventDTO> search(@RequestBody @Valid final SrvaEventSearchDTO searchParams) {
        return srvaCrudFeature.search(searchParams);
    }

    @RequestMapping(value = "/searchPage", method = RequestMethod.POST)
    public Slice<SrvaEventDTO> searchPage(
            @RequestBody @Valid final SrvaEventSearchDTO dto, final Pageable pageRequest) {
        return srvaCrudFeature.searchPage(dto, pageRequest);
    }

    @RequestMapping(value = "/search/excel", method = RequestMethod.POST)
    public ModelAndView searchExcel(@RequestParam(value = "json") @NotBlank final String jsonData) throws IOException {
        final SrvaEventSearchDTO dto = objectMapper.readValue(jsonData, SrvaEventSearchDTO.class);

        final List<SrvaEventExportExcelDTO> data = srvaCrudFeature.searchExcel(dto);
        final SrvaEventListExcelView view =
                new SrvaEventListExcelView(data, new EnumLocaliser(messageSource, LocaleContextHolder.getLocale()));
        return new ModelAndView(view);
    }
}

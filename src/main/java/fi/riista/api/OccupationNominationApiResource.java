package fi.riista.api;

import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.feature.organization.jht.JHTPeriod;
import fi.riista.feature.organization.jht.excel.OccupationNominationExcelView;
import fi.riista.feature.organization.jht.nomination.OccupationNomination;
import fi.riista.feature.organization.jht.nomination.OccupationNominationCrudFeature;
import fi.riista.feature.organization.jht.nomination.OccupationNominationDTO;
import fi.riista.feature.organization.jht.nomination.OccupationNominationSearchDTO;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.util.DateUtil;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@RequestMapping(value = "/api/v1/occupation-nomination")
public class OccupationNominationApiResource {

    @Resource
    private OccupationNominationCrudFeature occupationNominationFeature;

    @Resource
    private CustomJacksonObjectMapper objectMapper;

    @Resource
    private MessageSource messageSource;

    @RequestMapping(value = "counts", method = RequestMethod.GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public Map<OccupationNomination.NominationStatus, Long> count(@RequestParam String rhyOfficialCode,
                                                                  @RequestParam OccupationType occupationType) {
        return occupationNominationFeature.count(rhyOfficialCode, occupationType);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "occupationPeriod",
            method = RequestMethod.GET,
            produces = APPLICATION_JSON_UTF8_VALUE)
    public JHTPeriod getOccupationPeriod() {
        return new JHTPeriod(DateUtil.today());
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(
            value = "search",
            method = RequestMethod.POST,
            produces = APPLICATION_JSON_UTF8_VALUE,
            consumes = APPLICATION_JSON_UTF8_VALUE)
    public Page<OccupationNominationDTO> list(@RequestBody @Validated OccupationNominationSearchDTO dto) {
        return occupationNominationFeature.search(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/searchExcel", method = RequestMethod.POST)
    public ModelAndView searchExcel(@RequestParam(value = "json") @NotBlank final String jsonData,
                                    final Locale locale) throws IOException {

        final OccupationNominationSearchDTO dto = objectMapper.readValue(jsonData, OccupationNominationSearchDTO.class);
        final Page<OccupationNominationDTO> results = occupationNominationFeature.search(dto);

        return new ModelAndView(new OccupationNominationExcelView(results.getContent(), dto, messageSource, locale));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "{id:\\d+}/cancel", method = RequestMethod.POST)
    public void cancel(@PathVariable long id) {
        occupationNominationFeature.cancelPropose(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "{id:\\d+}/propose", method = RequestMethod.POST)
    public void propose(@PathVariable long id) {
        occupationNominationFeature.propose(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "{id:\\d+}/reject", method = RequestMethod.POST)
    public void reject(@PathVariable long id) {
        occupationNominationFeature.reject(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "{id:\\d+}/accept",
            method = RequestMethod.POST,
            consumes = APPLICATION_JSON_UTF8_VALUE)
    public void accept(@Validated(OccupationNominationDTO.AcceptValidation.class)
                       @RequestBody OccupationNominationDTO dto) {
        occupationNominationFeature.accept(dto.getId(), dto.getOccupationPeriod());
    }
}

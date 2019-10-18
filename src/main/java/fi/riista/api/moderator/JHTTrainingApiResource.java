package fi.riista.api.moderator;

import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.feature.organization.jht.excel.JHTTrainingExcelView;
import fi.riista.feature.organization.jht.training.JHTTrainingCrudFeature;
import fi.riista.feature.organization.jht.training.JHTTrainingDTO;
import fi.riista.feature.organization.jht.training.JHTTrainingSearchDTO;
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
import java.util.List;
import java.util.Locale;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@RequestMapping(value = "/api/v1/jht-training")
public class JHTTrainingApiResource {

    @Resource
    private JHTTrainingCrudFeature jhtTrainingCrudFeature;

    @Resource
    private CustomJacksonObjectMapper objectMapper;

    @Resource
    private MessageSource messageSource;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(
            value = "search",
            method = RequestMethod.POST,
            produces = APPLICATION_JSON_UTF8_VALUE,
            consumes = APPLICATION_JSON_UTF8_VALUE)
    public Page<JHTTrainingDTO> search(@RequestBody @Validated JHTTrainingSearchDTO dto) {
        return jhtTrainingCrudFeature.search(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/searchExcel", method = RequestMethod.POST)
    public ModelAndView searchExcel(@RequestParam(value = "json") @NotBlank final String jsonData,
                                    Locale locale) throws IOException {
        final JHTTrainingSearchDTO dto = objectMapper.readValue(jsonData, JHTTrainingSearchDTO.class);
        final Page<JHTTrainingDTO> results = jhtTrainingCrudFeature.search(dto);

        return new ModelAndView(new JHTTrainingExcelView(results.getContent(), messageSource, locale));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(
            method = RequestMethod.POST,
            consumes = APPLICATION_JSON_UTF8_VALUE,
            produces = APPLICATION_JSON_UTF8_VALUE)
    public JHTTrainingDTO create(@RequestBody @Validated JHTTrainingDTO dto) {
        return jhtTrainingCrudFeature.create(dto);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "{id:\\d+}/propose",
            method = RequestMethod.POST,
            consumes = APPLICATION_JSON_UTF8_VALUE)
    public void propose(@Validated(JHTTrainingDTO.IdAndRhyValidation.class)
                        @RequestBody JHTTrainingDTO dto,
                        @PathVariable Long id) {
        dto.setId(id);
        jhtTrainingCrudFeature.propose(dto.getId(), dto.getRhyId());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "{id:\\d+}", method = RequestMethod.DELETE)
    public void delete(@PathVariable long id) {
        jhtTrainingCrudFeature.delete(id);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/mine", method = RequestMethod.GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public List<JHTTrainingDTO> listMyTrainings() {
        return jhtTrainingCrudFeature.listMine();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/person/{personId:\\d+}", method = RequestMethod.GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public List<JHTTrainingDTO> forPerson(@PathVariable long personId) {
        return jhtTrainingCrudFeature.listForPerson(personId);
    }
}

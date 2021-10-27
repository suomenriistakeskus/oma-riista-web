package fi.riista.api.moderator;

import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.feature.organization.jht.excel.JHTTrainingExcelView;
import fi.riista.feature.organization.jht.training.JHTMultiTrainingDTO;
import fi.riista.feature.organization.jht.training.JHTTrainingCrudFeature;
import fi.riista.feature.organization.jht.training.JHTTrainingDTO;
import fi.riista.feature.organization.jht.training.JHTTrainingSearchDTO;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

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
    @PostMapping(value = "search", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public Page<JHTTrainingDTO> search(@RequestBody @Validated JHTTrainingSearchDTO dto) {
        return jhtTrainingCrudFeature.search(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "/searchExcel")
    public ModelAndView searchExcel(@RequestParam(value = "json") @NotBlank final String jsonData,
                                    Locale locale) throws IOException {
        final JHTTrainingSearchDTO dto = objectMapper.readValue(jsonData, JHTTrainingSearchDTO.class);
        final Page<JHTTrainingDTO> results = jhtTrainingCrudFeature.search(dto);

        return new ModelAndView(new JHTTrainingExcelView(results.getContent(), messageSource, locale));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public JHTTrainingDTO create(@RequestBody @Validated JHTTrainingDTO dto) {
        return jhtTrainingCrudFeature.create(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/multi", consumes = APPLICATION_JSON_VALUE)
    public void insertMultipleTrainings(@RequestBody @Validated JHTMultiTrainingDTO dto) {
        jhtTrainingCrudFeature.createMulti(dto);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "{id:\\d+}/propose", consumes = APPLICATION_JSON_VALUE)
    public void propose(@Validated(JHTTrainingDTO.IdAndRhyValidation.class)
                        @RequestBody JHTTrainingDTO dto,
                        @PathVariable Long id) {
        dto.setId(id);
        jhtTrainingCrudFeature.propose(dto.getId(), dto.getRhyId());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "{id:\\d+}")
    public void delete(@PathVariable long id) {
        jhtTrainingCrudFeature.delete(id);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/mine", produces = APPLICATION_JSON_VALUE)
    public List<JHTTrainingDTO> listMyTrainings() {
        return jhtTrainingCrudFeature.listMine();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/person/{personId:\\d+}", produces = APPLICATION_JSON_VALUE)
    public List<JHTTrainingDTO> forPerson(@PathVariable long personId) {
        return jhtTrainingCrudFeature.listForPerson(personId);
    }
}

package fi.riista.api.organisation;

import com.google.common.collect.Maps;
import fi.riista.feature.organization.OrganisationCrudFeature;
import fi.riista.feature.organization.OrganisationDTO;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationCrudFeature;
import fi.riista.feature.organization.occupation.OccupationDTO;
import fi.riista.feature.organization.occupation.OccupationExcelView;
import fi.riista.feature.organization.occupation.OccupationNotApplicableForOrganisationException;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.organization.person.PersonSearchFeature;
import fi.riista.util.F;
import fi.riista.util.Localiser;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/organisation", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class OccupationApiResource {

    @Resource
    private OccupationCrudFeature occupationCrudFeature;

    @Resource
    private PersonSearchFeature personSearchFeature;

    @Resource
    private OrganisationCrudFeature organisationCrudFeature;

    @Resource
    private MessageSource messageSource;

    @ExceptionHandler(OccupationNotApplicableForOrganisationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void occupationNotApplicableForOrganisation() {
    }

    @PostMapping(value = "/findperson/hunternumber")
    public PersonContactInfoDTO findByHunterNumber(@RequestParam final String hunterNumber) {
        return personSearchFeature
                .findPersonContactInfoByHunterNumber(hunterNumber, Occupation.FOREIGN_PERSON_ELIGIBLE_FOR_OCCUPATION);
    }

    @PostMapping(value = "/findperson/ssn")
    public PersonContactInfoDTO findBySSN(@RequestParam final String ssn) {
        return personSearchFeature.findPersonContactInfoBySsn(ssn);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{orgId:\\d+}/occupation")
    public List<OccupationDTO> listAllOccupations(@PathVariable final long orgId) {
        return occupationCrudFeature.listOccupations(orgId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{orgId:\\d+}/candidates")
    public List<PersonContactInfoDTO> listCandidatesForNewOccupation(@PathVariable final long orgId) {
        return occupationCrudFeature.listCandidatesForNewOccupation(orgId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "{orgId:\\d+}/occupation", consumes = MediaType.APPLICATION_JSON_VALUE)
    public OccupationDTO createOccupation(@RequestBody @Validated final OccupationDTO dto,
                                          @PathVariable final long orgId) {

        dto.setOrganisationId(orgId);

        return occupationCrudFeature.create(dto);
    }

    @PutMapping(value = "{orgId:\\d+}/occupation/{id:\\d+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public OccupationDTO updateOccupation(@RequestBody @Validated final OccupationDTO dto,
                                          @PathVariable final long orgId,
                                          @PathVariable final long id) {

        dto.setId(id);
        dto.setOrganisationId(orgId);

        return occupationCrudFeature.update(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/occupationTypes")
    public Map<OrganisationType, OccupationType[]> getOccupationTypes() {
        final Map<OrganisationType, OccupationType[]> map = Maps.newHashMap();
        for (OrganisationType orgType : OrganisationType.values()) {
            map.put(orgType, OccupationType.applicableValuesFor(orgType));
        }
        return map;
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{orgId:\\d+}/occupationTypes")
    public Map<String, List<OccupationType>> getOccupationTypes(@PathVariable final long orgId) {
        final List<OccupationType> allOccupationTypes = occupationCrudFeature.getApplicableOccupationTypes(orgId);
        final List<OccupationType> boardTypes = F.filterToList(allOccupationTypes, OccupationType::isBoardSpecific);

        return new HashMap<String, List<OccupationType>>() {{
            put("all", allOccupationTypes);
            if (!boardTypes.isEmpty()) {
                put("board", boardTypes);
            }
        }};
    }

    // EXCEL
    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "/excel/occupations", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView searchExcel(@RequestParam(value = "orgId") final long orgId) {
        final List<OccupationDTO> occupations = occupationCrudFeature.listOccupations(orgId);
        final OrganisationDTO organisation = organisationCrudFeature.read(orgId);
        final Locale locale = LocaleContextHolder.getLocale();
        final String organisationName = Localiser.select(organisation.getNameFI(), organisation.getNameSV());

        return new ModelAndView(new OccupationExcelView(locale, messageSource, organisationName,
                organisation.getOrganisationType(), occupations));
    }
}

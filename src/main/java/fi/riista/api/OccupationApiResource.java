package fi.riista.api;

import com.google.common.collect.Maps;
import fi.riista.feature.organization.OrganisationCrudFeature;
import fi.riista.feature.organization.OrganisationDTO;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.OccupationCrudFeature;
import fi.riista.feature.organization.occupation.OccupationDTO;
import fi.riista.feature.organization.occupation.OccupationExcelView;
import fi.riista.feature.organization.occupation.OccupationNotApplicableForOrganisationException;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.PersonDTO;
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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

    @RequestMapping(value = "/findperson/hunternumber", method = RequestMethod.POST)
    public PersonDTO findByHunterNumber(@RequestParam String hunterNumber) {
        return personSearchFeature.findHunterByNumber(hunterNumber);
    }

    @RequestMapping(value = "/findperson/ssn", method = RequestMethod.POST)
    public PersonDTO findBySSN(@RequestParam String ssn) {
        return personSearchFeature.findBySsn(ssn);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "{orgId:\\d+}/occupation", method = RequestMethod.GET)
    public List<OccupationDTO> listAllOccupations(@PathVariable Long orgId) {
        return occupationCrudFeature.listOccupations(orgId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "{orgId:\\d+}/candidates", method = RequestMethod.GET)
    public List<PersonDTO> listCandidateForNewOccupation(@PathVariable long orgId) {
        return occupationCrudFeature.listCandidateForNewOccupation(orgId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "{orgId:\\d+}/occupation", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public OccupationDTO createOccupation(@RequestBody @Validated OccupationDTO dto, @PathVariable Long orgId) {

        dto.setOrganisationId(orgId);

        return occupationCrudFeature.create(dto);
    }

    @RequestMapping(value = "{orgId:\\d+}/occupation/{id:\\d+}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public OccupationDTO updateOccupation(
            @RequestBody @Validated OccupationDTO dto, @PathVariable Long orgId, @PathVariable Long id) {

        dto.setId(id);
        dto.setOrganisationId(orgId);

        return occupationCrudFeature.update(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "{orgId:\\d+}/occupation/{id:\\d+}", method = RequestMethod.DELETE)
    public void deleteOccupation(@PathVariable Long id) {
        occupationCrudFeature.delete(id);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/occupationTypes", method = RequestMethod.GET)
    public Map<OrganisationType, OccupationType[]> getOccupationTypes() {
        Map<OrganisationType, OccupationType[]> map = Maps.newHashMap();
        for (OrganisationType orgType : OrganisationType.values()) {
            map.put(orgType, OccupationType.applicableValuesFor(orgType));
        }
        return map;
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "{orgId:\\d+}/occupationTypes", method = RequestMethod.GET)
    public Map<String, List<OccupationType>> getOccupationTypes(@PathVariable Long orgId) {
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
    @RequestMapping(value = "/excel/occupations", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView searchExcel(@RequestParam(value = "orgId") long orgId) {
        final List<OccupationDTO> occupations = occupationCrudFeature.listOccupations(orgId);
        final OrganisationDTO organisation = organisationCrudFeature.read(orgId);
        final Locale locale = LocaleContextHolder.getLocale();
        final String organisationName = Localiser.select(organisation.getNameFI(), organisation.getNameSV());

        return new ModelAndView(new OccupationExcelView(locale, messageSource, organisationName,
                organisation.getOrganisationType(), occupations));
    }

}

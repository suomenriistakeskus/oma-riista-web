package fi.riista.api;

import fi.riista.feature.organization.calendar.VenueDTO;
import fi.riista.feature.organization.calendar.VenueSearchFeature;
import fi.riista.feature.organization.person.PersonSearchFeature;
import fi.riista.feature.organization.person.PersonWithHunterNumberDTO;
import fi.riista.feature.search.SearchResultsDTO;
import fi.riista.feature.search.SiteSearchFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping(value = "/api/v1/search", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class SearchApiResource {

    @Resource
    private SiteSearchFeature siteSearchFeature;

    @Resource
    private VenueSearchFeature venueSearchFeature;

    @Resource
    private PersonSearchFeature personSearchFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(method = RequestMethod.GET)
    public SearchResultsDTO search(@RequestParam(value = "term") String searchTerm,
                                   @RequestParam(value = "locale", required = false) Locale locale) {
        return siteSearchFeature.search(searchTerm, locale);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/venue", method = RequestMethod.GET)
    public Page<VenueDTO> searchVenue(Pageable pageRequest, @RequestParam(value = "term") String searchTerm) {
        return venueSearchFeature.searchVenue(searchTerm, pageRequest);
    }

    @RequestMapping(value = "/person/hunternumber", method = RequestMethod.POST)
    public PersonWithHunterNumberDTO findByHunterNumber(@RequestParam String hunterNumber) {
        return personSearchFeature.findNameByHunterNumber(hunterNumber);
    }

    @RequestMapping(value = "/person/ssn", method = RequestMethod.POST)
    public PersonWithHunterNumberDTO findBySSN(@RequestParam String ssn) {
        return personSearchFeature.findNameAndHunterNumberBySsn(ssn);
    }

    @RequestMapping(value = "/person/permitnumber", method = RequestMethod.POST)
    public PersonWithHunterNumberDTO findByPermitNumber(@RequestParam String permitNumber) {
        return personSearchFeature.findNameAndHunterNumberByPermitNumber(permitNumber);
    }

    @RequestMapping(value = "/person/name", method = RequestMethod.POST)
    public List<PersonWithHunterNumberDTO> findByPersonName(@RequestParam String name) {
        return personSearchFeature.findNameAndHunterNumberOfAllByNameMatch(name);
    }

}

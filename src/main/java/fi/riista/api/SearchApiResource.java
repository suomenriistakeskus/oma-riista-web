package fi.riista.api;

import fi.riista.feature.huntingclub.search.HuntingClubNameDTO;
import fi.riista.feature.huntingclub.search.HuntingClubSearchFeature;
import fi.riista.feature.organization.calendar.VenueDTO;
import fi.riista.feature.organization.calendar.VenueSearchFeature;
import fi.riista.feature.organization.person.PersonSearchFeature;
import fi.riista.feature.organization.person.PersonWithHunterNumberDTO;
import fi.riista.feature.search.SearchDTO;
import fi.riista.feature.search.SearchResultsDTO;
import fi.riista.feature.search.SiteSearchFeature;
import java.util.List;
import javax.annotation.Resource;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/search", produces = MediaType.APPLICATION_JSON_VALUE)
public class SearchApiResource {

    @Resource
    private SiteSearchFeature siteSearchFeature;

    @Resource
    private VenueSearchFeature venueSearchFeature;

    @Resource
    private PersonSearchFeature personSearchFeature;

    @Resource
    private HuntingClubSearchFeature huntingClubSearchFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping
    public SearchResultsDTO search(@RequestBody @Validated SearchDTO searchDTO) {
        return siteSearchFeature.search(searchDTO);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/venue")
    public Page<VenueDTO> searchVenue(Pageable pageRequest, @RequestParam(value = "term") String searchTerm) {
        return venueSearchFeature.searchVenue(searchTerm, pageRequest);
    }

    @PostMapping(value = "/person/hunternumber")
    public PersonWithHunterNumberDTO findByHunterNumber(@RequestParam String hunterNumber) {
        return personSearchFeature.findNameByHunterNumber(hunterNumber);
    }

    @PostMapping(value = "/person/ssn")
    public PersonWithHunterNumberDTO findBySSN(@RequestParam String ssn) {
        return personSearchFeature.findNameAndHunterNumberBySsn(ssn);
    }

    @PostMapping(value = "/person/permitnumber")
    public PersonWithHunterNumberDTO findByPermitNumber(@RequestParam String permitNumber) {
        return personSearchFeature.findNameAndHunterNumberByPermitNumber(permitNumber);
    }

    @PostMapping(value = "/person/name")
    public List<PersonWithHunterNumberDTO> findByPersonName(@RequestParam String name) {
        return personSearchFeature.findNameAndHunterNumberOfAllByNameMatch(name);
    }

    @PostMapping(value = "/person")
    public List<PersonWithHunterNumberDTO> findByPersonNameOrHunterNumber(@RequestParam String searchTerm) {
        return personSearchFeature.findPersonsByHunterNumberOrNameFuzzyMatch(searchTerm);
    }

    @PostMapping(value = "/club/officialcode")
    public HuntingClubNameDTO findByOfficialCode(@RequestParam String officialCode) {
        return huntingClubSearchFeature.findNameByOfficialCode(officialCode);
    }

    @PostMapping(value = "/club/huntingclubid")
    public HuntingClubNameDTO findByHuntingClubId(@RequestParam Long huntingClubId) {
        return huntingClubSearchFeature.findNameById(huntingClubId);
    }
}

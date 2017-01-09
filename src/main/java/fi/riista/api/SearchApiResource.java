package fi.riista.api;

import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import fi.riista.feature.organization.calendar.VenueDTO;
import fi.riista.feature.search.SiteSearchFeature;
import fi.riista.feature.organization.calendar.VenueSearchFeature;
import fi.riista.feature.search.SearchResultsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Locale;

@RestController
@RequestMapping(value = "/api/v1/search", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class SearchApiResource {

    @Resource
    private SiteSearchFeature siteSearchFeature;

    @Resource
    private VenueSearchFeature venueSearchFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(method = RequestMethod.GET)
    public SearchResultsDTO search(@RequestParam(value = "term") String searchTerm,
                                   @RequestParam(value = "locale", required = false) Locale locale) {
        return siteSearchFeature.search(searchTerm, locale);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value="/venue", method = RequestMethod.GET)
    public Page<VenueDTO> searchVenue(Pageable pageRequest, @RequestParam(value = "term") String searchTerm) {
        return venueSearchFeature.searchVenue(searchTerm, pageRequest);
    }
}

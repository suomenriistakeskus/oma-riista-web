package fi.riista.api;

import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import fi.riista.feature.organization.calendar.CalendarEventCrudFeature;
import fi.riista.feature.organization.OrganisationCrudFeature;
import fi.riista.feature.organization.calendar.VenueCrudFeature;
import fi.riista.feature.organization.calendar.CalendarEventDTO;
import fi.riista.feature.organization.OrganisationDTO;
import fi.riista.feature.organization.calendar.VenueDTO;
import fi.riista.feature.organization.calendar.CalendarEventType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/organisation", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class OrganisationEventApiResource {
    @Resource
    private OrganisationCrudFeature organisationCrudFeature;

    @Resource
    private CalendarEventCrudFeature calendarEventCrudFeature;

    @Resource
    private VenueCrudFeature venueCrudFeature;

    // EVENTS

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/eventtypes", method = RequestMethod.GET)
    public List<CalendarEventType> readEventTypes() {
        return Arrays.asList(CalendarEventType.values());
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "{orgId:\\d+}/events", method = RequestMethod.GET)
    public List<CalendarEventDTO> listEvents(@PathVariable Long orgId) {
        return organisationCrudFeature.listEvents(orgId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "{orgId:\\d+}/events", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public CalendarEventDTO createEvent(@RequestBody @Validated CalendarEventDTO dto, @PathVariable Long orgId) {
        dto.setOrganisation(new OrganisationDTO());
        dto.getOrganisation().setId(orgId);
        return calendarEventCrudFeature.create(dto);
    }

    @RequestMapping(value = "{orgId:\\d+}/events/{id:\\d+}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public CalendarEventDTO updateEvent(
            @RequestBody @Validated CalendarEventDTO dto, @PathVariable Long orgId, @PathVariable Long id) {
        dto.setId(id);
        dto.getOrganisation().setId(orgId);
        return calendarEventCrudFeature.update(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "{orgId:\\d+}/events/{id:\\d+}", method = RequestMethod.DELETE)
    public void deleteEvent(@PathVariable Long id) {
        calendarEventCrudFeature.delete(id);
    }

    // VENUES

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "{orgId:\\d+}/venues", method = RequestMethod.GET)
    public List<VenueDTO> listVenues(@PathVariable Long orgId) {
        return organisationCrudFeature.listVenues(orgId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "{orgId:\\d+}/venues", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public VenueDTO createVenue(@RequestBody @Validated VenueDTO dto, @PathVariable Long orgId) {
        return venueCrudFeature.createForOrganisation(orgId, dto);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "{orgId:\\d+}/venues/{venueId:\\d+}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public VenueDTO updateVenue(@RequestBody @Validated VenueDTO dto, @PathVariable Long venueId) {
        dto.setId(venueId);
        return venueCrudFeature.update(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "{orgId:\\d+}/attachvenue/{venueId:\\d+}", method = RequestMethod.PUT)
    public void attachVenue(@PathVariable Long orgId, @PathVariable Long venueId) {
        venueCrudFeature.attachVenue(orgId, venueId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "{orgId:\\d+}/detachvenue/{venueId:\\d+}", method = RequestMethod.DELETE)
    public void detachVenue(@PathVariable Long orgId, @PathVariable Long venueId) {
        venueCrudFeature.detachVenue(orgId, venueId);
    }

}

package fi.riista.api;

import fi.riista.feature.organization.OrganisationCrudFeature;
import fi.riista.feature.organization.OrganisationDTO;
import fi.riista.feature.organization.calendar.CalendarEventCrudFeature;
import fi.riista.feature.organization.calendar.CalendarEventDTO;
import fi.riista.feature.organization.calendar.CalendarEventType;
import fi.riista.feature.organization.calendar.VenueCrudFeature;
import fi.riista.feature.organization.calendar.VenueDTO;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    @GetMapping(value = "/eventtypes")
    public List<CalendarEventType> readEventTypes() {
        return Arrays.asList(CalendarEventType.values());
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{orgId:\\d+}/events")
    public List<CalendarEventDTO> listEvents(@PathVariable final long orgId) {
        return organisationCrudFeature.listEvents(orgId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "{orgId:\\d+}/events", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CalendarEventDTO createEvent(@RequestBody @Validated final CalendarEventDTO dto,
                                        @PathVariable final long orgId) {

        dto.setOrganisation(new OrganisationDTO());
        dto.getOrganisation().setId(orgId);
        return calendarEventCrudFeature.create(dto);
    }

    @PutMapping(value = "{orgId:\\d+}/events/{id:\\d+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CalendarEventDTO updateEvent(@RequestBody @Validated final CalendarEventDTO dto,
                                        @PathVariable final long orgId,
                                        @PathVariable final long id) {

        dto.setId(id);
        dto.getOrganisation().setId(orgId);
        return calendarEventCrudFeature.update(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "{orgId:\\d+}/events/{id:\\d+}")
    public void deleteEvent(@PathVariable final long id) {
        calendarEventCrudFeature.delete(id);
    }

    // VENUES

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{orgId:\\d+}/venues")
    public List<VenueDTO> listVenues(@PathVariable final long orgId) {
        return organisationCrudFeature.listVenues(orgId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "{orgId:\\d+}/venues", consumes = MediaType.APPLICATION_JSON_VALUE)
    public VenueDTO createVenue(@RequestBody @Validated final VenueDTO dto, @PathVariable final long orgId) {
        return venueCrudFeature.createForOrganisation(orgId, dto);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping(value = "{orgId:\\d+}/venues/{venueId:\\d+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public VenueDTO updateVenue(@RequestBody @Validated final VenueDTO dto, @PathVariable final long venueId) {
        dto.setId(venueId);
        return venueCrudFeature.update(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "{orgId:\\d+}/attachvenue/{venueId:\\d+}")
    public void attachVenue(@PathVariable final long orgId, @PathVariable final long venueId) {
        venueCrudFeature.attachVenue(orgId, venueId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "{orgId:\\d+}/detachvenue/{venueId:\\d+}")
    public void detachVenue(@PathVariable final long orgId, @PathVariable final long venueId) {
        venueCrudFeature.detachVenue(orgId, venueId);
    }
}

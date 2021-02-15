package fi.riista.api.application;

import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.application.dogevent.DogEventType;
import fi.riista.feature.permit.application.dogevent.applicant.DogEventApplicantFeature;
import fi.riista.feature.permit.application.dogevent.disturbance.DogEventDisturbanceDTO;
import fi.riista.feature.permit.application.dogevent.disturbance.DogEventDisturbanceFeature;
import fi.riista.feature.permit.application.dogevent.summary.DogEventDisturbanceSummaryDTO;
import fi.riista.feature.permit.application.dogevent.summary.DogEventSummaryFeature;
import fi.riista.feature.permit.application.dogevent.summary.DogEventUnleashSummaryDTO;
import fi.riista.feature.permit.application.dogevent.unleash.DogEventUnleashDTO;
import fi.riista.feature.permit.application.dogevent.unleash.DogEventUnleashFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Locale;

import static fi.riista.api.application.HarvestPermitApplicationApiResource.API_PREFIX;

@RestController
@RequestMapping(value = API_PREFIX + "/dogevent")
public class DogEventApplicationApiResource {

    @Resource
    private DogEventApplicantFeature applicantFeature;

    @Resource
    private DogEventUnleashFeature unleashDetailsFeature;

    @Resource
    private DogEventDisturbanceFeature disturbanceDetailsFeature;

    @Resource
    private DogEventSummaryFeature summaryFeature;

    // PERMIT HOLDER

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/permit-holder", produces = MediaType.APPLICATION_JSON_VALUE)
    public PermitHolderDTO getPermitHolderinfo(@PathVariable final long applicationId) {
        return applicantFeature.getPermitHolderInfo(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/permit-holder")
    public void updatePermitHolder(@PathVariable final long applicationId,
                                   @Valid @RequestBody final PermitHolderDTO permitHolder) {
        applicantFeature.updatePermitHolder(applicationId, permitHolder);
    }

    // UNLEASH

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/unleash")
    public List<DogEventUnleashDTO> getUnleashEvents(@PathVariable final long applicationId) {
        return unleashDetailsFeature.getEvents(applicationId);
    }

    @PostMapping(value = "/{applicationId:\\d+}/unleash")
    public DogEventUnleashDTO saveUnleashEvent(@PathVariable final long applicationId,
                                               @Valid @RequestBody final DogEventUnleashDTO event) {
        return unleashDetailsFeature.updateEvent(applicationId, event);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{applicationId:\\d+}/unleash/{eventId:\\d+}")
    public void deleteUnleashEvent(@PathVariable final long applicationId,
                                   @PathVariable final long eventId) {

        unleashDetailsFeature.deleteEvent(applicationId, eventId);
    }

    // DISTURBANCE

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/disturbance")
    public DogEventDisturbanceDTO getDisturbanceEvent(
            @PathVariable final long applicationId,
            @RequestParam final DogEventType eventType) {
        return disturbanceDetailsFeature.getEvent(applicationId, eventType);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/disturbance")
    public void saveDisturbanceEvent(@PathVariable final long applicationId,
                                     @Valid @RequestBody final DogEventDisturbanceDTO event) {
        disturbanceDetailsFeature.updateEvent(applicationId, event);
    }

    // SUMMARY

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/unleash/full", produces = MediaType.APPLICATION_JSON_VALUE)
    public DogEventUnleashSummaryDTO readUnleashDetails(@PathVariable final long applicationId, final Locale locale) {
        return summaryFeature.readUnleashDetails(applicationId, locale);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/disturbance/full", produces = MediaType.APPLICATION_JSON_VALUE)
    public DogEventDisturbanceSummaryDTO readDisturbanceDetails(@PathVariable final long applicationId, final Locale locale) {
        return summaryFeature.readDisturbanceDetails(applicationId, locale);
    }


}

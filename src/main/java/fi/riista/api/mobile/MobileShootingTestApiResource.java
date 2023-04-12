package fi.riista.api.mobile;

import fi.riista.feature.common.dto.IdRevisionDTO;
import fi.riista.feature.shootingtest.HunterNumberSearchDTO;
import fi.riista.feature.shootingtest.ParticipantPaymentUpdateDTO;
import fi.riista.feature.shootingtest.ShootingTestAttemptCrudFeature;
import fi.riista.feature.shootingtest.ShootingTestAttemptDTO;
import fi.riista.feature.shootingtest.ShootingTestCalendarEventDTO;
import fi.riista.feature.shootingtest.ShootingTestFeature;
import fi.riista.feature.shootingtest.ShootingTestParticipantDTO;
import fi.riista.feature.shootingtest.ShootingTestParticipantDetailsDTO;
import fi.riista.feature.shootingtest.SsnSearchDTO;
import fi.riista.feature.shootingtest.official.ShootingTestOfficialFeature;
import fi.riista.feature.shootingtest.official.ShootingTestOfficialOccupationDTO;
import fi.riista.feature.shootingtest.official.ShootingTestOfficialsDTO;
import fi.riista.feature.shootingtest.registration.RegisterParticipantDTO;
import fi.riista.feature.shootingtest.registration.SelectedShootingTestTypesDTO;
import fi.riista.feature.shootingtest.registration.ShootingTestRegistrationFeature;
import fi.riista.feature.shootingtest.registration.ShootingTestRegistrationPersonSearchDTO;
import fi.riista.util.DateUtil;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.joda.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/mobile/v2/shootingtest", produces = MediaType.APPLICATION_JSON_VALUE)
public class MobileShootingTestApiResource {

    @Resource
    private ShootingTestFeature shootingTestFeature;

    @Resource
    private ShootingTestOfficialFeature officialFeature;

    @Resource
    private ShootingTestRegistrationFeature registrationFeature;

    @Resource
    private ShootingTestAttemptCrudFeature attemptCrudFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/calendarevents")
    public List<ShootingTestCalendarEventDTO> listCalendarEvents() {
        return shootingTestFeature.listRecentCalendarEventsForAllRhys();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/rhy/{rhyId:\\d+}/officials")
    public List<ShootingTestOfficialOccupationDTO> listAvailableShootingTestOfficials(
            @PathVariable final long rhyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date) {

        final LocalDate eventDate = Optional.ofNullable(date).orElseGet(DateUtil::today);
        return officialFeature.listAvailableOfficials(rhyId, eventDate);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/calendarevent/{calendarEventId:\\d+}")
    public ShootingTestCalendarEventDTO getCalendarEvent(@PathVariable final long calendarEventId) {
        return shootingTestFeature.getCalendarEvent(calendarEventId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/calendarevent/{calendarEventId:\\d+}/open")
    public void openShootingTestEvent(@PathVariable final long calendarEventId,
                                      @RequestBody @Validated final ShootingTestOfficialsDTO dto) {

        dto.setCalendarEventId(calendarEventId);
        shootingTestFeature.openEvent(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/event/{eventId:\\d+}/close")
    public void closeShootingTestEvent(@PathVariable final long eventId) {
        shootingTestFeature.closeEvent(eventId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/event/{eventId:\\d+}/reopen")
    public void reopenShootingTestEvent(@PathVariable final long eventId) {
        shootingTestFeature.reopenEvent(eventId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/event/{eventId:\\d+}/qualifyingofficials")
    public List<ShootingTestOfficialOccupationDTO> listQualifyingShootingTestOfficials(@PathVariable final long eventId) {
        return officialFeature.listQualifyingOfficials(eventId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/event/{eventId:\\d+}/assignedofficials")
    public List<ShootingTestOfficialOccupationDTO> listAssignedShootingTestOfficials(@PathVariable final long eventId) {
        return officialFeature.listAssignedOfficials(eventId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "/event/{eventId:\\d+}/officials")
    public void assignShootingTestOfficials(@PathVariable final long eventId,
                                            @RequestBody @Validated final ShootingTestOfficialsDTO dto) {

        dto.setShootingTestEventId(eventId);
        officialFeature.assignOfficials(dto);
    }

    // TODO Will be removed after an adequate base of mobile app installations is capable of adding foreign hunters.
    @PostMapping(value = "/event/{eventId:\\d+}/findperson/hunternumber")
    public ShootingTestRegistrationPersonSearchDTO findFinnishPersonByHunterNumberForRegistration(@PathVariable final long eventId,
                                                                                                  @RequestParam final String hunterNumber) {

        return registrationFeature.findFinnishHunterByHunterNumber(eventId, hunterNumber);
    }

    @PostMapping(value = "/event/{eventId:\\d+}/findhunter/hunternumber", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE })
    public ShootingTestRegistrationPersonSearchDTO findPersonByHunterNumberForRegistration(@PathVariable final long eventId,
                                                                                           @RequestParam final String hunterNumber) {

        return registrationFeature.findHunterByHunterNumber(eventId, hunterNumber);
    }

    @PostMapping(value = "/event/{eventId:\\d+}/findperson/ssn", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE })
    public ShootingTestRegistrationPersonSearchDTO findFinnishPersonBySsnForRegistration(@PathVariable final long eventId,
                                                                                         @RequestParam final String ssn) {

        return registrationFeature.findFinnishHunterBySsn(eventId, ssn);
    }

    // New mobile implementation uses these search methods with data on payload
    @PostMapping(value = "/event/{eventId:\\d+}/findhunter/hunternumber", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ShootingTestRegistrationPersonSearchDTO findPersonByHunterNumberForRegistration(@PathVariable final long eventId,
                                                                                           @RequestBody @Valid final HunterNumberSearchDTO hunterNumberDTO) {

        return registrationFeature.findHunterByHunterNumber(eventId, hunterNumberDTO.getHunterNumber());
    }

    @PostMapping(value = "/event/{eventId:\\d+}/findperson/ssn", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ShootingTestRegistrationPersonSearchDTO findFinnishPersonBySsnForRegistration(@PathVariable final long eventId,
                                                                                         @RequestBody @Valid final SsnSearchDTO ssnDTO) {

        return registrationFeature.findFinnishHunterBySsn(eventId, ssnDTO.getSsn());
    }

    // TODO Will be removed after an adequate base of mobile app installations is capable of adding foreign hunters.
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/event/{eventId:\\d+}/participant/person/{personId:\\d+}")
    public void registerParticipant(@PathVariable final long eventId,
                                    @PathVariable final long personId,
                                    @RequestBody @Valid final SelectedShootingTestTypesDTO selectedTypes) {

        registrationFeature.registerParticipant(eventId, personId, selectedTypes);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/event/{eventId:\\d+}/participant")
    public void registerParticipant(@PathVariable final long eventId,
                                    @RequestBody @Valid final RegisterParticipantDTO registration) {

        registrationFeature.registerParticipant(eventId, registration);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/event/{eventId:\\d+}/participants")
    public List<ShootingTestParticipantDTO> listParticipants(@PathVariable final long eventId,
                                                             @RequestParam(required = false, defaultValue = "false") final boolean unfinishedOnly) {

        return shootingTestFeature.listParticipants(eventId, unfinishedOnly);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/participant/{participantId:\\d+}")
    public ShootingTestParticipantDTO getParticipant(@PathVariable final long participantId) {
        return shootingTestFeature.getParticipant(participantId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/participant/{participantId:\\d+}/attempts")
    public ShootingTestParticipantDetailsDTO getDetailedAttemptsOfParticipant(@PathVariable final long participantId) {
        return shootingTestFeature.getDetailedAttemptsOfParticipant(participantId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/attempt/{attemptId:\\d+}")
    public ShootingTestAttemptDTO getAttempt(@PathVariable final long attemptId) {
        return attemptCrudFeature.read(attemptId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "/participant/{participantId:\\d+}/attempt")
    public void addAttempt(@PathVariable final long participantId,
                           @RequestBody @Valid final ShootingTestAttemptDTO dto) {

        dto.setId(null);
        dto.setRev(null);
        dto.setParticipantId(participantId);

        attemptCrudFeature.create(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/attempt/{attemptId:\\d+}")
    public void updateAttempt(@PathVariable final long attemptId,
                              @RequestBody @Valid final ShootingTestAttemptDTO dto) {

        dto.setId(attemptId);
        attemptCrudFeature.update(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/attempt/{attemptId:\\d+}")
    public void removeAttempt(@PathVariable final long attemptId) {
        attemptCrudFeature.delete(attemptId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "/participant/{participantId:\\d+}/payment")
    public void completePayment(@PathVariable final long participantId, @RequestBody @Valid final IdRevisionDTO dto) {
        dto.setId(participantId);
        shootingTestFeature.completePayment(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/participant/{participantId:\\d+}/payment")
    public void updatePayment(@PathVariable final long participantId,
                              @RequestBody @Valid final ParticipantPaymentUpdateDTO dto) {

        dto.setId(participantId);
        shootingTestFeature.updatePayment(dto);
    }
}

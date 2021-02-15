package fi.riista.api.pub;

import fi.riista.feature.huntingclub.HuntingClubNameValidationFeature;
import fi.riista.validation.PhoneNumber;
import javax.validation.constraints.Email;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/api/v1/validation")
public class ValidationApiResource {

    private static final String BODY_VALID_VALUE = "ok";
    // UI relies on this value, do not change.
    private static final String BODY_INVALID_VALUE = "invalid";

    @Resource(name = "mvcValidator")
    private Validator mvcValidator;

    @Resource
    private HuntingClubNameValidationFeature huntingClubNameValitationFeature;

    @GetMapping(value = "/phonenumber", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> validatePhoneNumber(@ModelAttribute("phoneNumber") final String phoneNumber) {
        final ValidationDTO dto = new ValidationDTO();
        dto.phoneNumber = phoneNumber;
        return validate(dto);
    }

    @GetMapping(value = "/email", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> validateEmail(@ModelAttribute("email") final String email) {
        final ValidationDTO dto = new ValidationDTO();
        dto.email = email;
        return validate(dto);
    }

    @GetMapping(value = "/clubname", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> validateClubName(@RequestParam(required = false) final Long clubId,
                                                   @RequestParam final String name) {

        return createResponse(!huntingClubNameValitationFeature.isClubNameDuplicate(clubId, name));
    }

    private static ResponseEntity<String> createResponse(final boolean result) {
        return ResponseEntity.ok(result ? BODY_VALID_VALUE : BODY_INVALID_VALUE);
    }

    private ResponseEntity<String> validate(final Object dto) {
        final DataBinder binder = new DataBinder(dto);
        binder.setValidator(mvcValidator);
        binder.validate();

        return createResponse(!binder.getBindingResult().hasErrors());
    }

    private static class ValidationDTO {
        @Email
        private String email;

        @PhoneNumber
        private String phoneNumber;
    }
}

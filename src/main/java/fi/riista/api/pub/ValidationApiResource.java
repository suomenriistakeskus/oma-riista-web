package fi.riista.api.pub;

import fi.riista.feature.huntingclub.HuntingClubNameValidationFeature;
import fi.riista.validation.PhoneNumber;
import org.hibernate.validator.constraints.Email;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

    @RequestMapping(value = "/phonenumber", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> validatePhoneNumber(@ModelAttribute("phoneNumber") String phoneNumber) {
        final ValidationDTO dto = new ValidationDTO();
        dto.phoneNumber = phoneNumber;
        return validate(dto);
    }

    @RequestMapping(value = "/email", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> validateEmail(@ModelAttribute("email") String email) {
        final ValidationDTO dto = new ValidationDTO();
        dto.email = email;
        return validate(dto);
    }

    @RequestMapping(value = "/clubname", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> validateClubName(@RequestParam(required = false) Long clubId,
                                                   @RequestParam String name) {
        return ResponseEntity.ok(huntingClubNameValitationFeature.isClubNameDuplicate(clubId, name)
                ? BODY_INVALID_VALUE
                : BODY_VALID_VALUE);
    }

    private ResponseEntity<String> validate(Object dto) {
        final DataBinder binder = new DataBinder(dto);
        binder.setValidator(mvcValidator);
        binder.validate();

        return ResponseEntity.ok(binder.getBindingResult().hasErrors()
                ? BODY_INVALID_VALUE
                : BODY_VALID_VALUE);
    }

    private static class ValidationDTO {
        @Email
        private String email;

        @PhoneNumber
        private String phoneNumber;
    }
}

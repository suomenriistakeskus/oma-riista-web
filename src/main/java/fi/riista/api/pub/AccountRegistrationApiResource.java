package fi.riista.api.pub;

import fi.riista.feature.account.registration.RegisterAccountFeature;
import fi.riista.feature.account.registration.CompleteRegistrationDTO;
import fi.riista.feature.account.registration.CompleteRegistrationRequestDTO;
import fi.riista.feature.account.registration.EmailVerificationDTO;
import fi.riista.feature.account.registration.RegisterAccountDTO;
import fi.riista.feature.mail.token.EmailTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;

@RestController
public class AccountRegistrationApiResource {
    private static final Logger LOG = LoggerFactory.getLogger(AccountRegistrationApiResource.class);

    public static final String URI_SEND_EMAIL = "/api/v1/register/send-email";
    public static final String URI_FROM_EMAIL = "/api/v1/register/from-email";

    @Resource
    private RegisterAccountFeature registerAccountFeature;

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = URI_SEND_EMAIL, method = RequestMethod.POST)
    public void registerByEmail(@RequestBody @Validated RegisterAccountDTO dto,
                                HttpServletRequest request) {
        registerAccountFeature.sendEmail(dto, request);
    }

    @RequestMapping(value = URI_FROM_EMAIL, method = RequestMethod.POST)
    public Map<String, Object> checkTokenValidity(@RequestBody @Validated EmailVerificationDTO dto,
                                                  HttpServletRequest request) {
        try {
            return registerAccountFeature.fromEmail(dto, request);

        } catch (final EmailTokenException ex) {
            return Collections.singletonMap("status", "expired");

        } catch (final Exception ex) {
            LOG.error("Could not verify token", ex);

            return Collections.singletonMap("status", "error");
        }
    }

    @RequestMapping(value = "/api/v1/register/data", method = RequestMethod.POST)
    public CompleteRegistrationDTO getRegistrationConfirmation(
            @RequestBody @Validated CompleteRegistrationRequestDTO dto) {
        return registerAccountFeature.complete(dto);
    }

    @RequestMapping(value = "/api/v1/register/confirm", method = RequestMethod.POST)
    public Map<String, Object> confirmRegistration(@RequestBody @Validated CompleteRegistrationDTO dto,
                                                   HttpServletRequest request) {
        registerAccountFeature.completeAccountRegistration(dto, request);
        return Collections.singletonMap("status", "ok");
    }
}

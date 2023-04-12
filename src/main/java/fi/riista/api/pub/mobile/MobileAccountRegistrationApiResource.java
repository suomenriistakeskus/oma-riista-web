package fi.riista.api.pub.mobile;

import fi.riista.feature.account.mobile.MobileForgotPasswordDTO;
import fi.riista.feature.account.mobile.MobileRegisterAccountDTO;
import fi.riista.feature.account.password.PasswordResetFeature;
import fi.riista.feature.account.registration.RegisterAccountFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;


@RestController
public class MobileAccountRegistrationApiResource {
    private static final Logger LOG = LoggerFactory.getLogger(MobileAccountRegistrationApiResource.class);

    public static final String URI_SEND_EMAIL = "/api/mobile/v2/register/send-email";
    public static final String URI_RESET_PASSWORD = "/api/mobile/v2/password/reset";

    @Resource
    private RegisterAccountFeature registerAccountFeature;

    @Resource
    private PasswordResetFeature passwordResetFeature;

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = URI_SEND_EMAIL)
    public void registerByEmail(@RequestBody @Validated MobileRegisterAccountDTO dto,
                                HttpServletRequest request) {
        registerAccountFeature.sendEmail(dto.getEmail(), dto.getLang(), request);
    }


    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = URI_RESET_PASSWORD)
    public void forgotPassword(@RequestBody @Validated MobileForgotPasswordDTO dto, HttpServletRequest request) {
        passwordResetFeature.sendPasswordResetEmail(dto.getEmail(), Optional.of(dto.getLang()), request);
    }
}

package fi.riista.api.pub;

import fi.riista.feature.account.password.ForgotPasswordDTO;
import fi.riista.feature.account.password.PasswordResetDTO;
import fi.riista.feature.account.password.PasswordResetFeature;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
public class PasswordResetApiResource {
    public static final String URI_SEND_MAIL = "/api/v1/password/forgot";
    public static final String URI_VERIFY_TOKEN = "/api/v1/password/verifytoken";
    public static final String URI_RESET_PASSWORD = "/api/v1/password/reset";

    @Resource
    private PasswordResetFeature passwordResetFeature;

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = URI_SEND_MAIL, method = RequestMethod.POST)
    public void forgotPassword(@RequestBody @Validated ForgotPasswordDTO dto, HttpServletRequest request) {
        passwordResetFeature.sendPasswordResetEmail(dto.getEmail(), request);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = URI_RESET_PASSWORD, method = RequestMethod.POST)
    public void resetPassword(@RequestBody @Validated PasswordResetDTO dto, HttpServletRequest request) {
        passwordResetFeature.processPasswordReset(dto, request);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = URI_VERIFY_TOKEN, method = RequestMethod.POST)
    public void verifyToken(@RequestBody String token) {
        passwordResetFeature.verifyToken(token);
    }
}

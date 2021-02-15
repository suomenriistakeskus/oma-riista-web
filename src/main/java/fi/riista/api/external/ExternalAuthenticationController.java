package fi.riista.api.external;

import fi.riista.feature.sso.CheckExternalAuthenticationFeature;
import fi.riista.feature.sso.dto.ExternalAuthenticationFailure;
import fi.riista.feature.sso.dto.ExternalAuthenticationRequest;
import fi.riista.util.Patterns;
import javax.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.Pattern;

@RestController
public class ExternalAuthenticationController {
    private static final Logger LOG = LoggerFactory.getLogger(ExternalAuthenticationController.class);

    @Resource
    private CheckExternalAuthenticationFeature checkExternalAuthenticationFeature;

    @RequestMapping(value = "/api/v1/export/account",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> validate(
            @RequestParam @NotBlank String username,
            @RequestParam @NotBlank String password,
            @RequestParam @Pattern(regexp = Patterns.IPV4) String remoteAddress,
            @RequestParam(required = false) @Pattern(regexp = "\\d+") String otp,
            @RequestParam(required = false) Boolean otpRequired) {

        LOG.info("Received request for /api/v1/export/account: username={} otpRequired={} otp={} remoteAddress={}",
                username, otpRequired, otp, remoteAddress);

        final ExternalAuthenticationRequest authenticationRequest = new ExternalAuthenticationRequest();
        authenticationRequest.setUsername(username);
        authenticationRequest.setPassword(password);
        authenticationRequest.setRemoteAddress(remoteAddress);
        authenticationRequest.setOtp(otp);
        authenticationRequest.setRequireOtp(otpRequired != null ? otpRequired : false);

        try {
            return checkExternalAuthenticationFeature.checkAuthentication(authenticationRequest);

        } catch (Exception ex) {
            LOG.error("External authentication failed for username=" + username, ex);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ExternalAuthenticationFailure.unknownError());
        }
    }
}

package fi.riista.feature.sso;

import fi.riista.config.properties.SecurityConfigurationProperties;
import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.sso.dto.ExternalAuthenticationFailure;
import fi.riista.feature.sso.dto.ExternalAuthenticationRequest;
import fi.riista.feature.sso.dto.ExternalAuthenticationResponse;
import fi.riista.feature.sso.dto.ExternalAuthenticationStatusCode;
import fi.riista.security.otp.OneTimePasswordRequiredException;
import fi.riista.security.otp.OneTimePasswordSMSService;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.annotation.Resource;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ExternalAuthenticationFeatureTest extends EmbeddedDatabaseTest {

    private static final String DEFAULT_PASSWORD = "password";

    @Resource
    private SecurityConfigurationProperties securityConfigurationProperties;

    @Resource
    private CheckExternalAuthenticationFeature checkExternalAuthenticationFeature;

    @Test
    public void testOK() {
        final SystemUser apiUser = createNewApiUser(SystemUserPrivilege.CHECK_EXTERNAL_AUTHENTICATION);
        final SystemUser user = createNewUserWithPasswordAndPerson("user", DEFAULT_PASSWORD, SystemUser.Role.ROLE_USER);
        final Person person = user.getPerson();

        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys(rka);

        final EnumSet<OccupationType> occupationTypesNotToExport = EnumSet.complementOf(CheckExternalAuthenticationFeature.OCCUPATION_TO_EXPORT);
        createTransientOccupations(person, getRiistakeskus(), occupationTypesNotToExport);
        createTransientOccupations(person, rka, occupationTypesNotToExport);
        createTransientOccupations(person, rhy, occupationTypesNotToExport);

        final List<Occupation> occupationsToExport = createTransientOccupations(person, rhy, CheckExternalAuthenticationFeature.OCCUPATION_TO_EXPORT);
        //sanity check, we want to check that all exported occupations were created
        assertEquals(CheckExternalAuthenticationFeature.OCCUPATION_TO_EXPORT.size(), occupationsToExport.size());

        persistInNewTransaction();

        authenticate(apiUser);

        final ResponseEntity<?> responseEntity =
                checkAuthentication(user.getUsername(), DEFAULT_PASSWORD, "127.0.0.1", null, null);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        ExternalAuthenticationResponse resp = (ExternalAuthenticationResponse) responseEntity.getBody();
        assertEquals(user.getPerson().getId(), resp.getPersonId());
        assertOccupations(resp.getOccupations(), occupationsToExport);
    }

    private List<Occupation> createTransientOccupations(Person person, Organisation org, Set<OccupationType> occupationTypes) {
        return occupationTypes.stream()
                .filter(occType -> occType.isApplicableFor(org.getOrganisationType()))
                .map(occType -> model().newOccupation(org, person, occType))
                .collect(toList());
    }

    private static void assertOccupations(
            List<ExternalAuthenticationResponse.ActiveOccupation> dtos, List<Occupation> expecteds) {

        assertEquals(expecteds.size(), dtos.size());
        expecteds.forEach(expected -> assertNotNull(find(expected, dtos)));
    }

    private static ExternalAuthenticationResponse.ActiveOccupation find(
            Occupation expected, List<ExternalAuthenticationResponse.ActiveOccupation> dtos) {

        for (ExternalAuthenticationResponse.ActiveOccupation dto : dtos) {
            if (Objects.equals(dto.getOrganisationCode(), expected.getOrganisation().getOfficialCode())
                    && Objects.equals(dto.getOccupation(), expected.getOccupationType())) {
                return dto;
            }
        }
        return null;
    }

    @Test
    public void testWrongPassword() {
        SystemUser apiUser = createNewApiUser(SystemUserPrivilege.CHECK_EXTERNAL_AUTHENTICATION);
        SystemUser user = createNewUserWithPasswordAndPerson("user", DEFAULT_PASSWORD, SystemUser.Role.ROLE_USER);

        persistInNewTransaction();

        authenticate(apiUser);

        ResponseEntity<?> responseEntity =
                checkAuthentication(user.getUsername(), "wrongpassword", "127.0.0.1", null, null);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());

        ExternalAuthenticationFailure resp = (ExternalAuthenticationFailure) responseEntity.getBody();
        assertEquals(ExternalAuthenticationStatusCode.INVALID_CREDENTIALS, resp.getStatus());
    }

    @Test
    public void testUserDoesNotExist() {
        SystemUser apiUser = createNewApiUser(SystemUserPrivilege.CHECK_EXTERNAL_AUTHENTICATION);
        createNewUserWithPasswordAndPerson("user", DEFAULT_PASSWORD, SystemUser.Role.ROLE_USER);

        persistInNewTransaction();

        authenticate(apiUser);

        ResponseEntity<?> responseEntity =
                checkAuthentication("nonexistentuser", DEFAULT_PASSWORD, "127.0.0.1", null, null);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());

        ExternalAuthenticationFailure resp = (ExternalAuthenticationFailure) responseEntity.getBody();
        assertEquals(ExternalAuthenticationStatusCode.INVALID_CREDENTIALS, resp.getStatus());
    }

    @Test
    public void testRemoteAddressMatchesWhiteList() {
        SystemUser apiUser = createNewApiUser(SystemUserPrivilege.CHECK_EXTERNAL_AUTHENTICATION);
        SystemUser user = createNewUserWithPasswordAndPerson("user", DEFAULT_PASSWORD, SystemUser.Role.ROLE_USER);
        user.setIpWhiteList("10.9.8.7/24");

        persistInNewTransaction();

        authenticate(apiUser);

        ResponseEntity<?> responseEntity =
                checkAuthentication(user.getUsername(), DEFAULT_PASSWORD, "10.9.8.2", null, null);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        ExternalAuthenticationResponse resp = (ExternalAuthenticationResponse) responseEntity.getBody();
        assertEquals(user.getPerson().getId(), resp.getPersonId());
    }

    @Test
    public void testRemoteAddressBlocked() {
        SystemUser apiUser = createNewApiUser(SystemUserPrivilege.CHECK_EXTERNAL_AUTHENTICATION);
        SystemUser user = createNewUserWithPasswordAndPerson("user", DEFAULT_PASSWORD, SystemUser.Role.ROLE_USER);
        user.setIpWhiteList("10.9.8.7/24");

        persistInNewTransaction();

        authenticate(apiUser);

        ResponseEntity<?> responseEntity =
                checkAuthentication(user.getUsername(), DEFAULT_PASSWORD, "10.9.9.9", null, null);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());

        ExternalAuthenticationFailure resp = (ExternalAuthenticationFailure) responseEntity.getBody();
        assertEquals(ExternalAuthenticationStatusCode.REMOTE_ADDRESS_BLOCKED, resp.getStatus());
    }

    @Test
    public void testTwoFactorAuthenticationRequiredButSmsSendFailure() {
        withOTPEnabled(() -> {
            SystemUser apiUser = createNewApiUser(SystemUserPrivilege.CHECK_EXTERNAL_AUTHENTICATION);
            SystemUser user = createNewUserWithPasswordAndPerson("user", DEFAULT_PASSWORD, SystemUser.Role.ROLE_USER);

            persistInNewTransaction();

            authenticate(apiUser);

            ResponseEntity<?> responseEntity =
                    checkAuthentication(user.getUsername(), DEFAULT_PASSWORD, "127.0.0.1", null, Boolean.TRUE);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());

            ExternalAuthenticationFailure resp = (ExternalAuthenticationFailure) responseEntity.getBody();
            assertEquals(ExternalAuthenticationStatusCode.SMS_SEND_FAILURE, resp.getStatus());
        });
    }

    @Test
    public void testTwoFactorAuthenticationSent() {
        assertFakeSendOTP();
    }

    @Test
    public void testTwoFactorAuthenticationSentAndAccepted() {
        final String otp = assertFakeSendOTP();
        withOTPEnabled(() -> {
            ResponseEntity<?> responseEntity =
                    checkAuthentication("user", DEFAULT_PASSWORD, "127.0.0.1", otp, Boolean.TRUE);
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        });
    }

    @Test
    public void testTwoFactorAuthenticationSentButWrongCode() {
        assertFakeSendOTP();

        withOTPEnabled(() -> {
            ResponseEntity<?> responseEntity =
                    checkAuthentication("user", DEFAULT_PASSWORD, "127.0.0.1", "55555", Boolean.TRUE);
            assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        });
    }

    private String assertFakeSendOTP() {
        final AtomicReference<String> expectedCode = new AtomicReference<>();

        withOTPEnabled(() -> {
            OneTimePasswordSMSService original = checkExternalAuthenticationFeature.getOneTimePasswordSMSService();
            checkExternalAuthenticationFeature.setOneTimePasswordSMSService(new OneTimePasswordSMSService() {
                @Override
                public boolean sendCodeUsingSMS(final OneTimePasswordRequiredException otpException) {
                    expectedCode.set(otpException.getExpectedCode());
                    return true;
                }
            });

            try {
                SystemUser apiUser = createNewApiUser(SystemUserPrivilege.CHECK_EXTERNAL_AUTHENTICATION);
                SystemUser user =
                        createNewUserWithPasswordAndPerson("user", DEFAULT_PASSWORD, SystemUser.Role.ROLE_USER);

                persistInNewTransaction();

                authenticate(apiUser);

                ResponseEntity<?> responseEntity =
                        checkAuthentication(user.getUsername(), DEFAULT_PASSWORD, "127.0.0.1", null, Boolean.TRUE);
                assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());

                ExternalAuthenticationFailure resp = (ExternalAuthenticationFailure) responseEntity.getBody();
                assertEquals(ExternalAuthenticationStatusCode.TWO_FACTOR_AUTHENTICATION_REQUIRED, resp.getStatus());
            } finally {
                checkExternalAuthenticationFeature.setOneTimePasswordSMSService(original);
            }
        });

        return expectedCode.get();
    }

    private ResponseEntity<?> checkAuthentication(final String username, final String password,
                                                  final String remoteAddress, final String otp,
                                                  final Boolean otpRequired) {
        final ExternalAuthenticationRequest authenticationRequest = new ExternalAuthenticationRequest();
        authenticationRequest.setUsername(username);
        authenticationRequest.setPassword(password);
        authenticationRequest.setRemoteAddress(remoteAddress);
        authenticationRequest.setOtp(otp);
        authenticationRequest.setRequireOtp(otpRequired != null ? otpRequired : false);

        return checkExternalAuthenticationFeature.checkAuthentication(authenticationRequest);
    }

    private void withOTPEnabled(Runnable runnable) {
        securityConfigurationProperties.setOtpLoginEnabled(true);
        securityConfigurationProperties.setOtpExtAuthEnabled(true);
        try {
            runnable.run();
        } finally {
            securityConfigurationProperties.setOtpLoginEnabled(false);
            securityConfigurationProperties.setOtpExtAuthEnabled(false);
        }
    }

}

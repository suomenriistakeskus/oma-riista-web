package fi.riista.feature.sso;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.sso.dto.ExternalAuthenticationFailure;
import fi.riista.feature.sso.dto.ExternalAuthenticationRequest;
import fi.riista.feature.sso.dto.ExternalAuthenticationResponse;
import fi.riista.feature.sso.service.ExternalAuthenticationService;
import fi.riista.security.UserInfo;
import fi.riista.security.authentication.RemoteAddressBlockedException;
import fi.riista.security.otp.OneTimePasswordRequiredException;
import fi.riista.security.otp.OneTimePasswordSMSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

@Component
public class CheckExternalAuthenticationFeature {
    private static final Logger LOG = LoggerFactory.getLogger(CheckExternalAuthenticationFeature.class);

    private static final EnumSet<OrganisationType> ORGANISATION_TYPES = EnumSet.of(OrganisationType.RHY);

    private static final Predicate<Occupation> ORGANISATION_TYPE_PREDICATE = occupation ->
            occupation != null && ORGANISATION_TYPES.contains(occupation.getOrganisation().getOrganisationType());

    public static final EnumSet<OccupationType> OCCUPATION_TO_EXPORT = EnumSet.of(
            OccupationType.TOIMINNANOHJAAJA,
            OccupationType.PETOYHDYSHENKILO,
            OccupationType.PUHEENJOHTAJA,
            OccupationType.METSASTAJATUTKINNON_VASTAANOTTAJA,
            OccupationType.METSASTAJATUTKINTOON_VALMENTAVAN_KOULUTUKSEN_KOULUTTAJA,
            OccupationType.METSASTYKSENVALVOJA,
            OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA,
            OccupationType.RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA
    );

    private static final Predicate<Occupation> OCCUPATION_TYPE_PREDICATE = occupation ->
            OCCUPATION_TO_EXPORT.contains(occupation.getOccupationType());

    @Resource
    private OneTimePasswordSMSService oneTimePasswordSMSService;

    @Resource
    private ExternalAuthenticationService externalAuthenticationService;

    @Resource
    private UserRepository userRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private ActiveUserService activeUserService;

    // for tests to override
    public void setOneTimePasswordSMSService(OneTimePasswordSMSService oneTimePasswordSMSService) {
        this.oneTimePasswordSMSService = oneTimePasswordSMSService;
    }

    public OneTimePasswordSMSService getOneTimePasswordSMSService() {
        return oneTimePasswordSMSService;
    }

    @PreAuthorize("hasPrivilege('CHECK_EXTERNAL_AUTHENTICATION')")
    @Transactional(readOnly = true, noRollbackFor = {AuthenticationException.class, AccessDeniedException.class})
    public ResponseEntity<?> checkAuthentication(final ExternalAuthenticationRequest externalAuthRequest) {

        final UserInfo activeUserInfo = activeUserService.getActiveUserInfo();

        Objects.requireNonNull(activeUserInfo, "no activeUser");

        try {
            LOG.info("Checking external user credentials for username {}", externalAuthRequest.getUsername());

            // Check external user credentials. Active user has already been authenticated.
            final UserInfo userInfo = externalAuthenticationService.authenticate(externalAuthRequest, activeUserInfo);
            final SystemUser registeredUser = getRegisteredUser(userInfo);

            return ResponseEntity.ok(ExternalAuthenticationResponse.from(
                    registeredUser, getOccupationsToExport(registeredUser.getPerson()), activeUserInfo));

        } catch (OneTimePasswordRequiredException e) {
            return sendSmsToken(externalAuthRequest, e);

        } catch (RemoteAddressBlockedException e) {
            LOG.error("External authentication failed blocked remote address: {}",
                    externalAuthRequest.getRemoteAddress());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ExternalAuthenticationFailure.remoteAddressBlocked());

        } catch (AccessDeniedException e) {
            LOG.error("Unknown access denied for external authentication request", e);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ExternalAuthenticationFailure.unknownError());

        } catch (AuthenticationException e) {
            LOG.error("External authentication for username={} was not successful: {}",
                    externalAuthRequest.getUsername(), e.getMessage());

            // Should not propagate exceptions which are not related to authenticated external client
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ExternalAuthenticationFailure.invalidCredentials(e));
        }
    }

    private List<Occupation> getOccupationsToExport(final Person person) {
        // Include only occupations valid today
        final List<Occupation> occupations = occupationRepository.findActiveByPerson(person);

        // Include only occupations which should be exported
        return occupations.stream()
                .filter(OCCUPATION_TYPE_PREDICATE)
                .filter(ORGANISATION_TYPE_PREDICATE)
                .collect(toList());
    }

    private ResponseEntity<ExternalAuthenticationFailure> sendSmsToken(ExternalAuthenticationRequest externalAuthRequest,
                                                                       OneTimePasswordRequiredException exception) {

        if (oneTimePasswordSMSService.sendCodeUsingSMS(exception)) {
            LOG.info("Sent SMS for external authentication request for username {}",
                    externalAuthRequest.getUsername());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ExternalAuthenticationFailure.twoFactorAuthenticationRequired());
        }

        LOG.error("Could not send SMS for external authentication request for username {}",
                externalAuthRequest.getUsername());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ExternalAuthenticationFailure.smsSentFailed());
    }

    private SystemUser getRegisteredUser(final UserInfo authentication) {
        Objects.requireNonNull(authentication);

        final SystemUser user = userRepository.getOne(UserInfo.extractFrom(authentication).getUserId());

        if (user.getPerson() == null) {
            throw new NotFoundException("User is not registered");
        }

        return user;
    }
}

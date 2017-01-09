package fi.riista.feature.account.password;

import com.github.jknack.handlebars.Handlebars;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import fi.riista.feature.account.audit.AccountActivityMessage;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.account.audit.AccountAuditService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.mail.MailService;
import fi.riista.feature.mail.token.EmailToken;
import fi.riista.feature.mail.token.EmailTokenService;
import fi.riista.feature.mail.token.EmailTokenType;
import fi.riista.util.Localiser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

import static org.springframework.session.FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME;

@Component
public class PasswordResetFeature {
    private static final Logger LOG = LoggerFactory.getLogger(PasswordResetFeature.class);

    private static final String TEMPLATE_PASSWORD_RESET = "email_password_reset";
    private static final String TEMPLATE_PASSWORD_RESET_SV = "email_password_reset.sv";

    @Resource
    private EmailTokenService emailTokenService;

    @Resource
    private ChangePasswordService changePasswordService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private MailService mailService;

    @Resource
    private AccountAuditService accountAuditService;

    @Resource
    private Handlebars handlebars;

    @Resource
    private MessageSource messageSource;

    @Resource
    private UserRepository userRepository;

    @Resource
    private FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    /**
     * Sends password renewal link to user(s) specified by email.
     * User can use the link to login to the application and to change her password.
     *
     * @param email Specifies the user(s) who will receive password renewal link.
     */
    @Transactional
    public void sendPasswordResetEmail(final String email, final HttpServletRequest request) {
        LOG.debug("Send password renewal link to '{}'", email);

        // There can be multiple users with the same email
        final SystemUser user = userRepository.findByUsernameIgnoreCaseAndActive(email, true);

        if (user == null) {
            LOG.error("Could not find registered SystemUser using email {}", email);
            return;
        }

        // Log account activity
        accountAuditService.auditUserEvent(user, activeUserService.getAuthentication(),
                AccountActivityMessage.ActivityType.PASSWORD_RESET_REQUESTED, null);

        final Locale userLocale = MoreObjects.firstNonNull(user.getLocale(), LocaleContextHolder.getLocale());
        final String subject = messageSource.getMessage("account.password.reset.mail.subject", null, userLocale);

        final URI passwordResetLink = UriComponentsBuilder.fromUri(runtimeEnvironmentUtil.getBackendBaseUri())
                .path("/")
                .fragment("/password/reset/{token}")
                .buildAndExpand(emailTokenService.allocateToken(EmailTokenType.PASSWORD_RESET, user, email, request))
                .toUri();

        final ImmutableMap<String, Object> params = ImmutableMap.<String, Object>of(
                "link", passwordResetLink.toString(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName());

        mailService.sendLater(new MailMessageDTO.Builder()
                .withTo(email)
                .withSubject(subject)
                .withHandlebarsBody(handlebars, selectTemplate(), params), null);
    }

    private static String selectTemplate() {
        return Localiser.select(TEMPLATE_PASSWORD_RESET, TEMPLATE_PASSWORD_RESET_SV);
    }

    @Transactional
    public void processPasswordReset(final PasswordResetDTO resetDTO, final HttpServletRequest request) {
        Objects.requireNonNull(resetDTO, "No dto");
        Preconditions.checkArgument(StringUtils.hasText(resetDTO.getToken()), "Token is empty");
        Preconditions.checkArgument(StringUtils.hasText(resetDTO.getPassword()), "Empty password");

        final EmailToken emailToken = emailTokenService.validateAndRevoke(resetDTO.getToken(), request);

        final SystemUser systemUser = Objects.requireNonNull(emailToken.getUser());

        if (systemUser.isActive()) {
            // Make sure all existing rememberMe logins are revoked
            findSessionKeysByUsername(systemUser.getUsername()).forEach(sessionRepository::delete);

            // Log account activity
            accountAuditService.auditUserEvent(systemUser, activeUserService.getAuthentication(),
                    AccountActivityMessage.ActivityType.PASSWORD_RESET, null);

            changePasswordService.setUserPassword(systemUser, resetDTO.getPassword());

            if (systemUser.getRole() == SystemUser.Role.ROLE_USER) {
                activeUserService.loginWithoutCheck(systemUser);
            }
        }
    }

    private Stream<String> findSessionKeysByUsername(final String username) {
        return sessionRepository.findByIndexNameAndIndexValue(PRINCIPAL_NAME_INDEX_NAME, username).keySet().stream();
    }

    @Transactional(readOnly = true)
    public void verifyToken(final String token) {
        emailTokenService.validate(token);
    }
}

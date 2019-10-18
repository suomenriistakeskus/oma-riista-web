package fi.riista.feature.account.password;

import com.github.jknack.handlebars.Handlebars;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.account.AccountSessionService;
import fi.riista.feature.account.audit.AccountAuditService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Locale;
import java.util.Objects;

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
    private AccountSessionService accountSessionService;

    @Resource
    private Handlebars handlebars;

    @Resource
    private MessageSource messageSource;

    @Resource
    private UserRepository userRepository;

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
        final SystemUser user = userRepository.findByUsernameIgnoreCase(email);

        if (user == null) {
            LOG.error("Could not find registered SystemUser using email {}", email);
            return;
        }

        if (!user.isActive()) {
            LOG.error("Cannot reset password for inactive userId={}", user.getId());
            return;
        }

        // Log account activity
        accountAuditService.auditPasswordResetRequest(user, request);

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

        mailService.send(MailMessageDTO.builder()
                .withFrom(mailService.getDefaultFromAddress())
                .addRecipient(email)
                .withSubject(subject)
                .appendHandlebarsBody(handlebars, selectTemplate(), params)
                .build());
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
            // Make sure all other sessions are logged out
            accountSessionService.deleteOtherActiveSessions(systemUser.getUsername(), request);

            // Log account activity
            accountAuditService.auditPasswordResetDone(systemUser, request);

            changePasswordService.setUserPassword(systemUser, resetDTO.getPassword());

            if (systemUser.getRole() == SystemUser.Role.ROLE_USER) {
                activeUserService.loginWithoutCheck(systemUser);
            }
        }
    }

    @Transactional(readOnly = true)
    public void verifyToken(final String token) {
        emailTokenService.validate(token);
    }
}

package fi.riista.feature.account.registration;

import com.github.jknack.handlebars.Handlebars;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.mail.MailService;
import fi.riista.feature.mail.token.EmailToken;
import fi.riista.feature.mail.token.EmailTokenService;
import fi.riista.feature.mail.token.EmailTokenType;
import fi.riista.util.Locales;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

@Service
public class RegisterAccountEmailService {
    private static final String TEMPLATE_REGISTER_ACCOUNT = "register_account";
    private static final String TEMPLATE_REGISTER_ACCOUNT_SV = "register_account.sv";

    private static String selectTemplate(final Locale locale) {
        return Locales.isSwedish(locale)
                ? TEMPLATE_REGISTER_ACCOUNT_SV
                : TEMPLATE_REGISTER_ACCOUNT;
    }

    @Resource
    private MailService mailService;

    @Resource
    private EmailTokenService emailTokenService;

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    @Resource
    private Handlebars handlebars;

    @Resource
    private MessageSource messageSource;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public String sendEmail(final String email, final String lang, final HttpServletRequest request) {
        // Forbid using internal Riistakeskus email addresses
        if (email.endsWith("@riista.fi") || email.endsWith(".riista.fi")) {
            throw new IllegalArgumentException("Invalid email domain");
        }

        final String token = emailTokenService.allocateToken(
                EmailTokenType.VERIFY_EMAIL, null, email, request);

        final URI emailLink = UriComponentsBuilder.fromUri(runtimeEnvironmentUtil.getBackendBaseUri())
                .path("/")
                .fragment("/register/from-email/{token}?lang={lang}")
                .buildAndExpand(token, lang)
                .toUri();

        final Locale locale = Locales.getLocaleByLanguageCode(lang);

        final String subject = messageSource.getMessage("registration.email.title", null, locale);

        final Map<String, Object> params = Collections.singletonMap("registerLink", emailLink.toString());

        mailService.send(MailMessageDTO.builder()
                .withFrom(mailService.getDefaultFromAddress())
                .addRecipient(email)
                .withSubject(subject)
                .appendHandlebarsBody(handlebars, selectTemplate(locale), params)
                .build());

        return token;
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public EmailToken getEmailToken(final EmailVerificationDTO dto) {
        // Decrypt and validate token data
        return emailTokenService.validate(dto.getToken());
    }
}

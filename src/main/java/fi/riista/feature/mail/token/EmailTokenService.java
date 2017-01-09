package fi.riista.feature.mail.token;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.keygen.BytesKeyGenerator;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Service
@Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
public class EmailTokenService {
    private static final Logger LOG = LoggerFactory.getLogger(EmailTokenService.class);

    public static final int RANDOM_BYTE_COUNT = 32;

    @Resource
    private EmailTokenRepository emailTokenRepository;

    private BytesKeyGenerator pseudoRandomGenerator = KeyGenerators.secureRandom(RANDOM_BYTE_COUNT);

    @Transactional
    public String allocateToken(final EmailTokenType tokenType,
                                final SystemUser systemUser,
                                final String email,
                                final HttpServletRequest request) {
        if (systemUser == null && tokenType.isUserRequired()) {
            throw new IllegalStateException("User is required for tokenType " + tokenType);
        }

        final EmailToken emailToken = new EmailToken(
                EmailTokenType.generateSecureToken(pseudoRandomGenerator),
                systemUser,
                tokenType,
                tokenType.calculateValidUntil(DateUtil.now()),
                request.getRemoteAddr(),
                email);

        return emailTokenRepository.saveAndFlush(emailToken).getId();
    }

    @Transactional
    public EmailToken validateAndRevoke(final String key, final HttpServletRequest request) {
        final EmailToken emailToken = validate(key);
        emailToken.revoke(request);

        // Make sure all email tokens are revoked
        emailTokenRepository.findNonRevokedByEmail(emailToken.getEmail()).forEach(t -> t.revoke(request));

        if (emailToken.getUser() != null) {
            // Make sure all user tokens are revoked
            emailTokenRepository.findNonRevokedByUser(emailToken.getUser()).forEach(t -> t.revoke(request));
        }

        return emailToken;
    }

    @Transactional
    public void revoke(final String key, final HttpServletRequest request) {
        emailTokenRepository.getOne(key).revoke(request);
    }

    @Transactional(readOnly = true)
    public EmailToken validate(final String key) {
        final EmailToken emailToken = emailTokenRepository.findOne(key);

        if (emailToken == null) {
            LOG.error("Could not find token {}", key);
            throw new EmailTokenException("not found");
        }

        if (emailToken.getRevokedAt() != null) {
            LOG.error("Received emailToken for {} revoked at {}",
                    emailToken.getEmail(), emailToken.getRevokedAt());

            throw new EmailTokenException("revoked");
        }

        if (!emailToken.isValid(DateUtil.now())) {
            LOG.error("Received emailToken for {} expired at {}",
                    emailToken.getEmail(), emailToken.getValidUntil());
            throw new EmailTokenException("expired");
        }

        final SystemUser user = emailToken.getUser();

        if (user != null) {
            if (!user.isActive()) {
                LOG.error("Received emailToken for {} for inactive user",
                        emailToken.getEmail());
                throw new EmailTokenException("invalid");
            }

            if (!user.getRole().canAcceptEmailToken()) {
                LOG.error("Received emailToken for {} invalid role {}",
                        emailToken.getEmail(), user.getRole());
                throw new EmailTokenException("invalid");
            }

        } else if (emailToken.getTokenType().isUserRequired()) {
            LOG.error("Received emailToken for {} without user information",
                    emailToken.getEmail());
            throw new EmailTokenException("invalid");
        }

        return emailToken;
    }
}

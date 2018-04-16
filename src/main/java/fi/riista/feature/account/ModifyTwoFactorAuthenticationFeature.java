package fi.riista.feature.account;

import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.security.otp.OneTimePasswordCodeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

@Component
public class ModifyTwoFactorAuthenticationFeature {

    private static String createQrCodeUrl(final String user, final String host, final String secret) {
        return String.format("otpauth://totp/%s@%s?secret=%s", user, host, secret);
    }

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    @Resource
    private OneTimePasswordCodeService oneTimePasswordCodeService;

    @Resource
    private ActiveUserService activeUserService;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public ModifyTwoFactorAuthenticationDTO getTwoFactorAuthentication() {
        return createDTO(activeUserService.getActiveUser());
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Transactional
    public ModifyTwoFactorAuthenticationDTO updateTwoFactorAuthentication(final ModifyTwoFactorAuthenticationDTO dto) {
        final SystemUser activeUser = activeUserService.getActiveUser();
        final String otpSecret = oneTimePasswordCodeService.getOtpSecret(activeUser);

        if (dto.getTwoFactorAuthentication() == SystemUser.TwoFactorAuthenticationMode.SMS) {
            activeUser.setTwoFactorAuthentication(SystemUser.TwoFactorAuthenticationMode.SMS);

        } else if (dto.getTwoFactorAuthentication() == SystemUser.TwoFactorAuthenticationMode.OFFLINE) {
            if (OneTimePasswordCodeService.checkOneTimePassword(otpSecret, dto.getTwoFactorCode())) {
                activeUser.setTwoFactorAuthentication(SystemUser.TwoFactorAuthenticationMode.OFFLINE);
            }
        }

        return createDTO(activeUser);
    }

    @Nonnull
    private ModifyTwoFactorAuthenticationDTO createDTO(final SystemUser activeUser) {
        final String otpSecret = oneTimePasswordCodeService.getOtpSecret(activeUser);
        final String host = runtimeEnvironmentUtil.getBackendBaseUri().getHost();

        final ModifyTwoFactorAuthenticationDTO dto = new ModifyTwoFactorAuthenticationDTO();
        dto.setTwoFactorCodeUrl(createQrCodeUrl(activeUser.getUsername(), host, otpSecret));
        dto.setTwoFactorAuthentication(activeUser.getTwoFactorAuthentication());
        if (dto.getTwoFactorAuthentication() == null) {
            dto.setTwoFactorAuthentication(SystemUser.TwoFactorAuthenticationMode.SMS);
        }
        return dto;
    }
}

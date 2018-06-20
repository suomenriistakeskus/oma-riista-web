package fi.riista.feature.account.password;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.ActiveUserService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.annotation.Resource;

@Component
public class ChangePasswordDTOValidator implements Validator {
    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private ChangePasswordService changePasswordService;

    @Override
    public boolean supports(Class<?> type) {
        return ChangePasswordDTO.class.isAssignableFrom(type);
    }

    @Override
    @Transactional(readOnly = true)
    public void validate(Object target, Errors errors) {
        final ChangePasswordDTO dto = ChangePasswordDTO.class.cast(target);

        if (!isPreviousPasswordCorrect(dto)) {
            // Careful here! Do not reject value, because validation errors might
            // be logged, resulting password in log
            errors.reject("validation.error.user.password.current.does.not.match", "Incorrect previous password");
        }
    }

    private boolean isPreviousPasswordCorrect(final ChangePasswordDTO dto) {
        if (StringUtils.hasText(dto.getPasswordCurrent())) {
            final SystemUser user = activeUserService.requireActiveUser();
            return changePasswordService.confirmPassword(user, dto.getPasswordCurrent());
        }
        return false;
    }
}

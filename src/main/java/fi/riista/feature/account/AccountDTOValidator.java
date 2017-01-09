package fi.riista.feature.account;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.annotation.Resource;

@Component
public class AccountDTOValidator implements Validator {

    @Resource
    private UserRepository userRepository;

    @Override
    public boolean supports(Class<?> type) {
        return AccountDTO.class.isAssignableFrom(type);
    }

    @Override
    @Transactional(readOnly = true)
    public void validate(Object target, Errors errors) {
        final AccountDTO dto = AccountDTO.class.cast(target);

        if (isUsernameAlreadyTaken(dto)) {
            errors.rejectValue("username",
                    "validation.error.user.username.taken",
                    "Username is already in use.");
        }
    }

    private boolean isUsernameAlreadyTaken(AccountDTO dto) {
        if (dto != null && StringUtils.hasLength(dto.getUsername())) {
            final SystemUser user = userRepository.findByUsernameIgnoreCase(dto.getUsername());
            return user != null && !user.getId().equals(dto.id);
        }
        return false;
    }
}

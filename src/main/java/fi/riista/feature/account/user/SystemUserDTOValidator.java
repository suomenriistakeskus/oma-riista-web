package fi.riista.feature.account.user;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.annotation.Resource;

@Component
public class SystemUserDTOValidator implements Validator {
    @Resource
    private UserRepository userRepository;

    @Override
    public boolean supports(Class<?> type) {
        return SystemUserDTO.class.isAssignableFrom(type);
    }

    @Override
    @Transactional(readOnly = true)
    public void validate(Object target, Errors errors) {
        final SystemUserDTO dto = (SystemUserDTO) target;

        if (dto != null && StringUtils.hasText(dto.getUsername())) {
            if (isUsernameAlreadyTaken(dto)) {
                errors.rejectValue("username", "validation.error.user.username.taken", "Username is already in use.");
            }
        }
    }

    private boolean isUsernameAlreadyTaken(SystemUserDTO dto) {
        final SystemUser user = userRepository.findByUsernameIgnoreCase(dto.getUsername());

        return user != null && !user.getId().equals(dto.getId());
    }
}

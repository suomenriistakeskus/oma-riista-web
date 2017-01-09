package fi.riista.feature.account.password;

import com.google.common.base.Preconditions;
import fi.riista.feature.account.user.SystemUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Objects;

@Service
public class ChangePasswordService {
    @Resource
    private PasswordEncoder passwordEncoder;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean confirmPassword(final SystemUser user, final String plainPassword) {
        Objects.requireNonNull(plainPassword, "No password to confirm given");
        return user.getHashedPassword() != null && passwordEncoder.matches(plainPassword, user.getHashedPassword());
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void setUserPassword(final SystemUser user, final String plainPassword) {
        Preconditions.checkArgument(StringUtils.hasText(plainPassword), "Empty password");
        user.setPasswordAsPlaintext(plainPassword, passwordEncoder);
    }
}

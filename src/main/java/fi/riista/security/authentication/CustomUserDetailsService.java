package fi.riista.security.authentication;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.security.UserInfo;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

public class CustomUserDetailsService implements UserDetailsService {
    @Resource
    private UserRepository userRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public UserDetails loadUserByUsername(String username) {
        if (username == null) {
            throw new UsernameNotFoundException("Invalid empty login username");
        }

        try {
            final SystemUser user = userRepository.findByUsernameIgnoreCase(username);

            if (user != null) {
                return new UserInfo.UserInfoBuilder(user)
                        .withOccupations(user, occupationRepository)
                        .createUserInfo();
            }

        } catch (DataAccessException dae) {
            throw new UsernameNotFoundException("Could not lookup user", dae);
        }

        throw new UsernameNotFoundException("No such username");
    }
}

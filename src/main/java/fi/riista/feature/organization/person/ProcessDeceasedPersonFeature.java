package fi.riista.feature.organization.person;

import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.organization.occupation.OccupationRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
public class ProcessDeceasedPersonFeature {
    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private UserRepository userRepository;

    @Transactional
    public void execute() {
        userRepository.deactivateAccountsForDeceased();
        occupationRepository.endOccupationsForDeceased();
    }
}

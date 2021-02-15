package fi.riista.feature.account.pilot;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.person.Person;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static java.util.Objects.requireNonNull;

@Component
public class DeerPilotService {

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private DeerPilotRepository deerPilotRepository;

    @Transactional(readOnly = true)
    public boolean isPilotUser() {
        if (activeUserService.isModeratorOrAdmin()) {
            return false;
        }

        final Person activePerson = activeUserService.requireActivePerson();
        return deerPilotRepository.isPersonInPilotGroup(activePerson);
    }

    @Transactional(readOnly = true)
    public boolean isPilotUser(final long personId) {
        return deerPilotRepository.isPersonInPilotGroup(personId);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isPilotUser(final Person person) {
        requireNonNull(person);
        return deerPilotRepository.isPersonInPilotGroup(person);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isPilotGroup(final HuntingClubGroup group) {
        return deerPilotRepository.isPilotGroup(group.getId());
    }

    @Transactional(readOnly = true)
    public boolean isPilotPermit(final long permitId) {
        return deerPilotRepository.findByHarvestPermitId(permitId).isPresent();
    }
}

package fi.riista.feature.organization.rhy;

import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class RiistanhoitoyhdistysCoordinatorService {

    @Resource
    private OccupationRepository occupationRepository;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Person findCoordinator(final Riistanhoitoyhdistys rhy) {
        final List<Occupation> valids =
                occupationRepository.findActiveByOrganisationAndOccupationType(rhy, OccupationType.TOIMINNANOHJAAJA);

        return valids.isEmpty() ? null : valids.get(0).getPerson();
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public String resolveRhyEmail(final Riistanhoitoyhdistys rhy) {
        final String email = rhy.getEmail();
        if (StringUtils.isNotBlank(email)) {
            return email;
        }
        final Person coordinator = findCoordinator(rhy);
        return coordinator != null ? coordinator.getEmail() : null;
    }
}

package fi.riista.feature.organization.jht.mobile;

import static java.util.stream.Collectors.toList;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.organization.jht.training.JHTTraining;
import fi.riista.feature.organization.jht.training.JHTTrainingRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MobileJHTTrainingFeature {

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private JHTTrainingRepository jhtTrainingRepository;

    @Resource
    private MobileJHTTrainingDTOTransformer mobileJhtTrainingDTOTransformer;

    @Transactional(readOnly = true)
    public List<MobileJHTTrainingDTO> listMine() {
        final SystemUser activeUser = activeUserService.requireActiveUser();

        if (activeUser.getRole() == SystemUser.Role.ROLE_USER && activeUser.getPerson() != null) {
            return listForPerson(activeUser.getPerson());
        }

        return Collections.emptyList();
    }

    private List<MobileJHTTrainingDTO> listForPerson(final Person person) {
        final List<JHTTraining> byPerson = jhtTrainingRepository.findByPersonOrderByTrainingDateDesc(person);

        final List<OccupationType> usedOccupationTypes = new ArrayList<>();

        return mobileJhtTrainingDTOTransformer.apply(byPerson.stream()
                .filter(training -> !training.isArtificialTraining())
                .filter(training -> {
                    if (usedOccupationTypes.contains(training.getOccupationType())) {
                        return false;
                    }
                    usedOccupationTypes.add(training.getOccupationType());
                    return true;
                })
                .collect(toList()));
    }
}

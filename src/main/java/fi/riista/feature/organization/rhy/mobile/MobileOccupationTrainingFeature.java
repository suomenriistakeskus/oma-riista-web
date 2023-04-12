package fi.riista.feature.organization.rhy.mobile;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.training.OccupationTrainingRepository;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MobileOccupationTrainingFeature {

    @Resource
    private OccupationTrainingRepository occupationTrainingRepository;

    @Resource
    private MobileOccupationTrainingDTOTransformer mobileOccupationTrainingDTOTransformer;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional(readOnly = true)
    public List<MobileOccupationTrainingDTO> listMine() {
        final Person person = activeUserService.requireActivePerson();
        return fetchAndTransformMobile(person);
    }

    private List<MobileOccupationTrainingDTO> fetchAndTransformMobile(final Person person) {
        return mobileOccupationTrainingDTOTransformer.transform(occupationTrainingRepository.findByPerson(person));
    }
}

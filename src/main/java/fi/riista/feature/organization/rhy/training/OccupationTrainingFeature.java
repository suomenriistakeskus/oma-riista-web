package fi.riista.feature.organization.rhy.training;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Component
public class OccupationTrainingFeature {

    @Resource
    private OccupationTrainingRepository occupationTrainingRepository;

    @Resource
    private OccupationTrainingDTOTransformer occupationTrainingDTOTransformer;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ActiveUserService activeUserService;


    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public List<OccupationTrainingDTO> listForPerson(final Long personId) {
        final Person person = requireEntityService.requirePerson(personId, EntityPermission.READ);
        return fetchAndTransform(person);
    }

    @Transactional(readOnly = true)
    public List<OccupationTrainingDTO> listMine() {
        final Person person = activeUserService.requireActivePerson();
        return fetchAndTransform(person);
    }

    private List<OccupationTrainingDTO> fetchAndTransform(final Person person) {
        return occupationTrainingDTOTransformer.transform(occupationTrainingRepository.findByPerson(person));
    }

}

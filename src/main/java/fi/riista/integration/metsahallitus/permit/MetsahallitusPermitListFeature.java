package fi.riista.integration.metsahallitus.permit;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class MetsahallitusPermitListFeature {

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private PersonRepository personRepository;

    @Resource
    private MetsahallitusPermitRepository metsahallitusPermitRepository;

    @Transactional(readOnly = true)
    public List<MetsahallitusPermitListDTO> listAll(final Long personId) {
        final Person person = activeUserService.isModeratorOrAdmin()
                ? personRepository.getOne(personId)
                : activeUserService.requireActivePerson();

        final List<MetsahallitusPermit> list = metsahallitusPermitRepository.findByPerson(person);

        return list.stream()
                .filter(p -> MetsahallitusPermitImportDTO.PAID_CODES.contains(p.getStatus()))
                .map(MetsahallitusPermitListDTO::create)
                .collect(toList());
    }
}

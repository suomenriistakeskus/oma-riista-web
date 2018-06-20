package fi.riista.feature.harvestpermit.list;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.permit.application.HarvestPermitApplicationRepository;
import fi.riista.feature.permit.application.QHarvestPermitApplication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.toList;

@Component
public class ListPermitApplicationsFeature {

    @Resource
    private HarvestPermitApplicationRepository harvestPermitApplicationRepository;

    @Resource
    private PersonRepository personRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional(readOnly = true)
    public List<PermitApplicationListDTO> listApplicationsForPerson(final Long personId) {
        final Person person = activeUserService.isModeratorOrAdmin()
                ? personRepository.getOne(personId)
                : activeUserService.requireActivePerson();

        final QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;

        return harvestPermitApplicationRepository.findAllAsList(APPLICATION.contactPerson.eq(person))
                .stream()
                .map(PermitApplicationListDTO::create)
                .sorted(comparingLong(PermitApplicationListDTO::getId).reversed())
                .collect(toList());
    }

}

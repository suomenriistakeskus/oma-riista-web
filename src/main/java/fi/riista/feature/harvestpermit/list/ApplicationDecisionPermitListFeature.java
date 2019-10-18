package fi.riista.feature.harvestpermit.list;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Component
public class ApplicationDecisionPermitListFeature {

    @Resource
    private PersonRepository personRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private ApplicationDecisionPermitListQueries queries;

    @Transactional(readOnly = true)
    public List<ApplicationDecisionPermitListDTO> listApplicationsAndDecisionsForPerson(final Long personId) {
        final Person person = activeUserService.isModeratorOrAdmin()
                ? personRepository.getOne(personId)
                : activeUserService.requireActivePerson();

        final ApplicationDecisionPermitListDTOBuilder builder = new ApplicationDecisionPermitListDTOBuilder();

        // Order of these calls is significant !!!
        queries.findApplicationsByContactPerson(person).forEach(builder::addApplication);
        queries.findDecisionsByApplicationContactPerson(person).forEach(builder::addDecision);
        queries.findPermitByApplicationContactPerson(person).forEach(builder::addPermit);
        queries.findPermitByContactPerson(person).forEach(builder::addPermit);

        return builder.build();
    }
}

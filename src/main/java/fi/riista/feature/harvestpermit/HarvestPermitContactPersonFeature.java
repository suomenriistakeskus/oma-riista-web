package fi.riista.feature.harvestpermit;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Component
public class HarvestPermitContactPersonFeature {

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private PersonRepository personRepository;

    @Transactional(readOnly = true)
    public List<HarvestPermitContactPersonDTO> getContactPersons(final long permitId) {
        final SystemUser activeUser = activeUserService.requireActiveUser();
        final HarvestPermit permit = requireEntityService.requireHarvestPermit(permitId, EntityPermission.READ);

        Objects.requireNonNull(permit, "permit must not be null");

        final List<HarvestPermitContactPersonDTO> dtos = F.mapNonNullsToList(permit.getContactPersons(), pc -> {
            final Person person = pc.getContactPerson();

            final HarvestPermitContactPersonDTO dto = HarvestPermitContactPersonDTO.create(person);
            // contact person can't delete himself
            dto.setCanBeDeleted(!Objects.equals(person, activeUser.getPerson()));

            return dto;
        });

        dtos.add(HarvestPermitContactPersonDTO.create(permit.getOriginalContactPerson()));

        return dtos;
    }

    @Transactional
    public void updateContactPersons(final long permitId, final List<HarvestPermitContactPersonDTO> contactPersons) {
        final HarvestPermit permit = requireEntityService.requireHarvestPermit(permitId, EntityPermission.UPDATE);

        permit.getContactPersons().clear();

        for (final HarvestPermitContactPersonDTO cp : contactPersons) {
            // dto:s contact person hunter number can be empty if the person is same as original contact person
            if (StringUtils.isNotBlank(cp.getHunterNumber())) {
                personRepository.findByHunterNumber(cp.getHunterNumber()).ifPresent(p -> {
                    if (!permit.getOriginalContactPerson().equals(p)) {
                        permit.getContactPersons().add(new HarvestPermitContactPerson(permit, p));
                    }
                });
            }
        }
    }
}

package fi.riista.feature.organization.occupation;

import fi.riista.feature.huntingclub.members.ClubRole;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.SimpleEntityDTOAuthorization;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import fi.riista.security.authorization.support.AuthorizationTokenCollector;
import fi.riista.util.F;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.huntingclub.members.ClubRole.RYHMAN_JASEN;
import static fi.riista.feature.huntingclub.members.ClubRole.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.feature.huntingclub.members.ClubRole.SEURAN_JASEN;
import static fi.riista.feature.huntingclub.members.ClubRole.SEURAN_YHDYSHENKILO;
import static fi.riista.feature.organization.rhy.RhyRole.COORDINATOR;
import static java.util.Arrays.asList;

@Component
public class OccupationAuthorization extends SimpleEntityDTOAuthorization<Occupation, OccupationDTO, Long> {
    private static final Logger LOG = LoggerFactory.getLogger(OccupationAuthorization.class);

    private enum Role {
        PERSONAL_CLUB_MEMBERSHIP
    }

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private PersonRepository personRepository;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public OccupationAuthorization() {
        super("occupation");

        asList(CREATE, READ, UPDATE, DELETE).forEach(permission -> {
            allow(permission, ROLE_ADMIN, ROLE_MODERATOR);
            allow(permission, COORDINATOR);
            allow(permission, SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA);
        });

        allow(CREATE, Role.PERSONAL_CLUB_MEMBERSHIP);
        allow(READ, SEURAN_JASEN, RYHMAN_JASEN);
        allow(DELETE, SEURAN_JASEN, RYHMAN_JASEN, Role.PERSONAL_CLUB_MEMBERSHIP);
    }

    @Override
    protected JpaRepository<Occupation, Long> getRepository() {
        return occupationRepository;
    }

    @Override
    protected void authorizeTarget(
            final AuthorizationTokenCollector collector,
            final EntityAuthorizationTarget target,
            final UserInfo userInfo) {
        final Person activePerson = userAuthorizationHelper.getPerson(userInfo);

        if (activePerson == null) {
            return;
        }

        ifOccupationOrganisationAndPersonResolved(target, (occupationOrganisation, occupationPerson) -> {
            final OrganisationType organisationType = occupationOrganisation.getOrganisationType();

            if (organisationType == OrganisationType.RHY) {
                collector.addAuthorizationRole(COORDINATOR,
                        () -> userAuthorizationHelper.isCoordinator(occupationOrganisation, activePerson));

            } else if (organisationType == OrganisationType.CLUB) {
                collector.addAuthorizationRole(SEURAN_YHDYSHENKILO,
                        () -> userAuthorizationHelper.isClubContact(occupationOrganisation, activePerson));

                collector.addAuthorizationRole(ClubRole.SEURAN_JASEN,
                        () -> Objects.equals(activePerson, occupationPerson)
                                && userAuthorizationHelper.isClubMember(occupationOrganisation, activePerson));

                // user accepting invitation and creating occupation
                collector.addAuthorizationRole(Role.PERSONAL_CLUB_MEMBERSHIP,
                        () -> Objects.equals(activePerson, occupationPerson));

            } else if (organisationType == OrganisationType.CLUBGROUP) {
                collector.addAuthorizationRole(SEURAN_YHDYSHENKILO,
                        () -> userAuthorizationHelper.isClubContact(occupationOrganisation.getParentOrganisation(), activePerson));

                collector.addAuthorizationRole(RYHMAN_METSASTYKSENJOHTAJA,
                        () -> userAuthorizationHelper.isGroupLeader(occupationOrganisation, activePerson));

                collector.addAuthorizationRole(RYHMAN_JASEN,
                        () -> Objects.equals(activePerson, occupationPerson)
                                && userAuthorizationHelper.isGroupMember(occupationOrganisation, activePerson));
            }
        });
    }

    private void ifOccupationOrganisationAndPersonResolved(
            final EntityAuthorizationTarget target, final BiConsumer<Organisation, Person> consumer) {
        final Optional<Organisation> occupationOrganisation = findOccupationOrganisation(target);
        final Optional<Person> occupationPerson = findOccupationPerson(target);

        if (occupationOrganisation.isPresent() && occupationPerson.isPresent()) {
            consumer.accept(occupationOrganisation.get(), occupationPerson.get());
        } else {
            LOG.error("Could not resolve person or organisation");
        }
    }

    private Optional<Organisation> findOccupationOrganisation(final EntityAuthorizationTarget target) {
        final Optional<OccupationDTO> dtoOpt = findDto(target);
        return dtoOpt.isPresent()
                ? dtoOpt.map(OccupationDTO::getOrganisationId).map(organisationRepository::getOne)
                : findEntity(target).map(Occupation::getOrganisation);
    }

    private Optional<Person> findOccupationPerson(final EntityAuthorizationTarget target) {
        final Optional<OccupationDTO> dtoOpt = findDto(target);
        return dtoOpt.isPresent()
                ? dtoOpt.map(OccupationAuthorization::getPersonId).map(personRepository::getOne)
                : findEntity(target).map(Occupation::getPerson);
    }

    private static Long getPersonId(final OccupationDTO dto) {
        return F.hasId(dto.getPerson()) ? F.getId(dto.getPerson()) : dto.getPersonId();
    }
}

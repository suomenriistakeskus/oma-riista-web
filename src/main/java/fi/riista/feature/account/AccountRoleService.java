package fi.riista.feature.account;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.util.F;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class AccountRoleService {

    private static final Logger LOG = LoggerFactory.getLogger(AccountRoleService.class);

    private static final EnumSet<OccupationType> CLUB_OCCUPATIONS_WITH_ROLE =
            EnumSet.of(OccupationType.SEURAN_JASEN, OccupationType.SEURAN_YHDYSHENKILO);

    private static final Ordering<Occupation> CLUB_ROLE_ORDERING = Ordering.natural().nullsLast().onResultOf(occ -> {
        switch (occ.getOccupationType()) {
            case SEURAN_YHDYSHENKILO:
                return 10;
            case RYHMAN_METSASTYKSENJOHTAJA:
                return 20;
            case SEURAN_JASEN:
                return 30;
            default:
                return null;
        }
    });

    private static String getFullName(final SystemUser user) {
        final Person person = user.getPerson();
        return person != null ? person.getFullName() : user.getFullName();
    }

    private static AccountRoleDTO getBasicUserRole(final SystemUser user) {
        final AccountRoleDTO roleDTO = AccountRoleDTO.fromUser(user);
        roleDTO.setDisplayName(getFullName(user));

        if (user.getPerson() != null) {
            roleDTO.getContext().setPersonId(user.getPerson().getId());
        }

        return roleDTO;
    }

    private static AccountRoleDTO transformToRoleDTO(final Occupation occupation) {
        final AccountRoleDTO roleDTO = AccountRoleDTO.fromOccupation(occupation);
        final AccountRoleDTO.ContextDTO context = roleDTO.getContext();
        Organisation organisation = occupation.getOrganisation();

        switch (organisation.getOrganisationType()) {
            case RHY:
                context.setRhyId(organisation.getId());
                break;
            case CLUBGROUP:
                organisation = organisation.getParentOrganisation();
                // intentional fall-through to CLUB case
            case CLUB:
                context.setClubId(organisation.getId());
                break;
            default:
                break;
        }

        context.setNameFI(organisation.getNameFinnish());
        context.setNameSV(organisation.getNameSwedish());

        return roleDTO;
    }

    @Resource
    private OccupationRepository occupationRepository;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<AccountRoleDTO> getRoles(@Nonnull final SystemUser user) {
        Objects.requireNonNull(user, "user must not be null");

        final List<AccountRoleDTO> roleList = Lists.newArrayList(getBasicUserRole(user));

        // Do not add person related account roles for admins and moderators
        if (user.getRole() == SystemUser.Role.ROLE_USER && user.getPerson() != null) {
            roleList.addAll(getRolesForPerson(user.getPerson()));
        }

        return roleList;
    }

    private List<AccountRoleDTO> getRolesForPerson(final Person person) {
        final Stream<Occupation> roleMappedOccupations = occupationRepository.findActiveByPerson(person).stream()
                .filter(o -> o.getOccupationType().isMappedToRole());

        final Map<Boolean, List<Occupation>> partitionByIsRelatedToClub =
                roleMappedOccupations.collect(Collectors.partitioningBy(o -> o.getOccupationType().isClubSpecific()));
        final List<Occupation> clubOccupations = partitionByIsRelatedToClub.get(true);
        final List<Occupation> nonClubOccupations = partitionByIsRelatedToClub.get(false);

        // role-mapped club-occupations indexed by hunting club
        final Map<Organisation, List<Occupation>> occupationsByClub = F.nullSafeGroupBy(clubOccupations, occ -> {
            switch (occ.getOccupationType()) {
                case SEURAN_YHDYSHENKILO:
                case SEURAN_JASEN:
                    return occ.getOrganisation();
                case RYHMAN_METSASTYKSENJOHTAJA:
                    return occ.getOrganisation().getParentOrganisation();
                default:
                    return null;
            }
        });

        final Stream<Occupation> selectedClubOccupations = occupationsByClub.entrySet().stream()
                .filter(entry -> {
                    if (entry.getKey().getOrganisationType() != OrganisationType.CLUB) {
                        LOG.warn("While resolving club roles encountered {} when expected CLUB "
                                        + "(organisation type), ignoring illegal entry.",
                                entry.getKey().getOrganisationType());
                        return false;
                    }

                    return true;
                })
                .map(Entry::getValue)
                .filter(occupations -> occupations.stream()
                        .anyMatch(o -> CLUB_OCCUPATIONS_WITH_ROLE.contains(o.getOccupationType())))
                // Pick most prominent role
                .map(CLUB_ROLE_ORDERING::min);

        return Stream.concat(nonClubOccupations.stream(), selectedClubOccupations)
                .map(AccountRoleService::transformToRoleDTO)
                .collect(Collectors.toList());
    }
}

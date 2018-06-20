package fi.riista.feature.account;

import com.google.common.collect.Ordering;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpecs;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import fi.riista.util.jpa.JpaSpecs;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.util.Collect.nullSafeGroupingBy;
import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.jpa.domain.Specifications.where;

@Component
public class AccountRoleService {

    private static final Ordering<Occupation> CLUB_ROLE_ORDERING = Ordering.natural().nullsLast().onResultOf(occ -> {
        switch (occ.getOccupationType()) {
            case SEURAN_YHDYSHENKILO:
                return 1;
            case RYHMAN_METSASTYKSENJOHTAJA:
                return 2;
            case SEURAN_JASEN:
                return 3;
            default:
                return null;
        }
    });

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<AccountRoleDTO> getRoles(@Nonnull final SystemUser user) {
        Objects.requireNonNull(user);

        final List<AccountRoleDTO> roleList = new LinkedList<>();

        roleList.add(AccountRoleDTO.fromUser(user));

        // Do not add person related account roles for admins and moderators
        if (user.getRole() == SystemUser.Role.ROLE_USER && user.getPerson() != null) {
            roleList.addAll(getRolesDerivedFromOccupations(user.getPerson()));
            roleList.addAll(getRolesDerivedFromPermits(user.getPerson()));
        }

        return roleList;
    }

    private List<AccountRoleDTO> getRolesDerivedFromPermits(final Person person) {
        final int currentHuntingYear = DateUtil.huntingYear();
        final int nextHuntingYear = currentHuntingYear + 1;

        return harvestPermitRepository.findAll(where(JpaSpecs.and(
                HarvestPermitSpecs.isPermitContactPerson(person),
                JpaSpecs.or(
                        HarvestPermitSpecs.withYear(Integer.toString(currentHuntingYear)),
                        HarvestPermitSpecs.withYear(Integer.toString(nextHuntingYear)))))).stream()
                .sorted(comparingLong(HarvestPermit::getId).reversed())
                .map(AccountRoleDTO::fromPermit)
                .collect(toList());
    }

    private List<AccountRoleDTO> getRolesDerivedFromOccupations(final Person person) {
        final Stream<Occupation> roleMappedOccupations = occupationRepository.findOccupationsForRoleMapping(person, DateUtil.huntingYear())
                .stream()
                .filter(o -> {
                    final OccupationType occType = o.getOccupationType();
                    return occType.isMappedToRole() &&
                            (occType != AMPUMAKOKEEN_VASTAANOTTAJA || person.isShootingTestsEnabled());
                });

        final Map<Boolean, List<Occupation>> partitionByIsRelatedToClub =
                roleMappedOccupations.collect(partitioningBy(o -> o.getOccupationType().isClubSpecific()));
        final List<Occupation> clubOccupations = partitionByIsRelatedToClub.get(true);
        final List<Occupation> nonClubOccupations = partitionByIsRelatedToClub.get(false);

        final Set<Organisation> clubsWithMembership = clubOccupations.stream()
                .filter(occ -> occ.getOccupationType().isApplicableFor(OrganisationType.CLUB))
                .map(Occupation::getOrganisation)
                .collect(Collectors.toSet());

        // filter out occupations where invitation is not accepted
        final List<Occupation> clubOccupationsWithAcceptedInvites = clubOccupations.stream()
                .filter(occ -> occ.getOccupationType() != RYHMAN_METSASTYKSENJOHTAJA || clubsWithMembership.contains(occ.getOrganisation().getParentOrganisation()))
                .collect(toList());

        // role-mapped club-occupations indexed by hunting club
        final Map<Organisation, List<Occupation>> occupationsByClub = clubOccupationsWithAcceptedInvites
                .stream()
                .collect(nullSafeGroupingBy(occ -> occ.getOccupationType() == RYHMAN_METSASTYKSENJOHTAJA
                        ? occ.getOrganisation().getParentOrganisation()
                        : occ.getOrganisation()));

        final Stream<Occupation> selectedClubOccupations = occupationsByClub.entrySet().stream()
                .map(Entry::getValue)
                .map(CLUB_ROLE_ORDERING::min);

        return Stream.concat(nonClubOccupations.stream(), selectedClubOccupations)
                .map(AccountRoleDTO::fromOccupation)
                .collect(toList());
    }
}

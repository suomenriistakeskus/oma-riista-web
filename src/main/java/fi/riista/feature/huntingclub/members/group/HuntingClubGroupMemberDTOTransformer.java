package fi.riista.feature.huntingclub.members.group;

import com.google.common.base.Preconditions;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationDTO;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.ContactInfoShare;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.F;
import fi.riista.util.ListTransformer;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static fi.riista.util.F.mapNullable;

@Component
public class HuntingClubGroupMemberDTOTransformer extends ListTransformer<Occupation, OccupationDTO> {

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private OccupationRepository occupationRepository;

    @Nonnull
    @Override
    protected List<OccupationDTO> transform(@Nonnull final List<Occupation> list) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        final SystemUser activeUser = activeUserService.requireActiveUser();
        final boolean isModerator = activeUser.isModeratorOrAdmin();

        final List<Organisation> groups = list.stream().map(Occupation::getOrganisation)
                .distinct()
                .collect(Collectors.toList());
        Preconditions.checkArgument(groups.size() == 1, "Multiple groups found from occupations");

        final Organisation club = groups.get(0).getParentOrganisation();
        final ArrayList<Long> personIds = F.mapNonNullsToList(list, o -> o.getPerson().getId());
        final Map<Long, Occupation> clubOccupations =
                occupationRepository.findActiveByOrganisationAndPersonIds(club, personIds);

        return F.mapNonNullsToList(list, occupation -> {
            final boolean isLeader = isModerator || isLeader(activeUser.getPerson(), occupation.getOrganisation());
            final ContactInfoShare contactInfoShare =
                    mapNullable(clubOccupations.get(occupation.getPerson().getId()), Occupation::getContactInfoShare);
            final boolean showContactInformation = checkShowContactInfo(isLeader, contactInfoShare);

            return OccupationDTO.create(occupation, isLeader, showContactInformation);
        });
    }

    private static boolean checkShowContactInfo(final boolean isLeader, final ContactInfoShare share) {
        return share == ContactInfoShare.ALL_MEMBERS
                || share == ContactInfoShare.ONLY_OFFICIALS && isLeader;
    }

    private boolean isLeader(final Person activePerson, final Organisation organisation) {
        if (activePerson == null) {
            return false;
        }

        boolean isLeader = userAuthorizationHelper.hasRoleInOrganisation(
                organisation,
                activePerson,
                OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        // If not leader in group then check if leader in club
        return isLeader
                ? true
                : userAuthorizationHelper.hasRoleInOrganisation(
                organisation.getParentOrganisation(),
                activePerson,
                OccupationType.SEURAN_YHDYSHENKILO);

    }
}

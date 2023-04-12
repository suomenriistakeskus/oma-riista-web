package fi.riista.feature.huntingclub.members;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationDTO;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.ContactInfoShare;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.F;
import fi.riista.util.ListTransformer;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Component
public class HuntingClubOccupationDTOTransformer extends ListTransformer<Occupation, OccupationDTO> {

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private ActiveUserService activeUserService;

    @Nonnull
    @Override
    protected List<OccupationDTO> transform(@Nonnull final List<Occupation> list) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        final SystemUser activeUser = activeUserService.requireActiveUser();
        final boolean isModerator = activeUser.isModeratorOrAdmin();

        return F.mapNonNullsToList(list, occupation -> {
            final boolean isLeader = isModerator || isLeader(activeUser.getPerson(), occupation.getOrganisation());
            final boolean showContactInformation = checkShowContactInfo(isLeader, occupation);

            return OccupationDTO.create(occupation, isLeader, showContactInformation);
        });
    }

    private static boolean checkShowContactInfo(final boolean isLeader, final Occupation occupation) {
        final ContactInfoShare cis = occupation.getContactInfoShare();

        return cis == ContactInfoShare.ALL_MEMBERS
                || cis == ContactInfoShare.ONLY_OFFICIALS && isLeader;
    }

    private boolean isLeader(final Person activePerson, final Organisation organisation) {
        if (activePerson == null) {
            return false;
        }

        return userAuthorizationHelper.hasRoleInOrganisation(
                organisation,
                activePerson,
                OccupationType.SEURAN_YHDYSHENKILO);
    }

}

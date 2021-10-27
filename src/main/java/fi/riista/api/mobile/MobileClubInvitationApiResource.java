package fi.riista.api.mobile;

import fi.riista.feature.huntingclub.members.club.HuntingClubMemberCrudFeature;
import fi.riista.feature.huntingclub.members.invitation.HuntingClubMemberInvitationFeature;
import fi.riista.feature.huntingclub.members.invitation.mobile.MobileHuntingClubMemberInvitationDTO;
import fi.riista.feature.organization.occupation.OccupationDTO;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/api/mobile/v2/club")
public class MobileClubInvitationApiResource {

    @Resource
    private HuntingClubMemberInvitationFeature huntingClubInvitationFeature;

    @Resource
    private HuntingClubMemberCrudFeature crudFeature;

    @GetMapping(value = "invitations", produces = MediaType.APPLICATION_JSON_VALUE)
    @CacheControl(policy = CachePolicy.NO_CACHE)
    public List<MobileHuntingClubMemberInvitationDTO> myInvitations() {
        return huntingClubInvitationFeature.listMyInvitationsMobile();
    }

    @PutMapping(value = "/invitation/{id:\\d+}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void acceptInvitation(@PathVariable final long id) {
        final OccupationDTO occupationDTO = huntingClubInvitationFeature.acceptInvitation(id);
        crudFeature.create(occupationDTO);
    }

    @PutMapping(value = "/invitation/{id:\\d+}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rejectInvitation(@PathVariable final long id) {
        huntingClubInvitationFeature.rejectInvitation(id);
    }

}

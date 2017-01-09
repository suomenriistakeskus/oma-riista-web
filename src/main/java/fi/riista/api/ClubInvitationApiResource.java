package fi.riista.api;

import fi.riista.feature.huntingclub.members.invitation.HuntingClubMemberInvitationFeature;
import fi.riista.feature.huntingclub.members.club.HuntingClubMemberCrudFeature;
import fi.riista.feature.huntingclub.members.invitation.HuntingClubMemberInvitationCreateDTO;
import fi.riista.feature.huntingclub.members.invitation.HuntingClubMemberInvitationDTO;
import fi.riista.feature.organization.occupation.OccupationDTO;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(value = "/api/v1/club")
public class ClubInvitationApiResource {

    @Resource
    private HuntingClubMemberInvitationFeature huntingClubInvitationFeature;

    @Resource
    private HuntingClubMemberCrudFeature crudFeature;

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/{clubId:\\d+}/validateHunterNumbers",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Set<String> findInvalidHunterNumbers(@PathVariable long clubId,
                                                @RequestParam Set<String> hunterNumbers) {
        return huntingClubInvitationFeature.findInvalidHunterNumbers(clubId, hunterNumbers);
    }

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/{clubId:\\d+}/invite",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void inviteMembers(@PathVariable long clubId,
                              @RequestBody @Valid HuntingClubMemberInvitationCreateDTO dto) {
        huntingClubInvitationFeature.invite(clubId, dto.getGroupId(), dto.getHunterNumbers());
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/{clubId:\\d+}/invitation",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @CacheControl(policy = CachePolicy.NO_CACHE)
    public List<HuntingClubMemberInvitationDTO> listInvitations(@PathVariable long clubId) {
        return huntingClubInvitationFeature.listInvitations(clubId);
    }

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/invitation/{id:\\d+}/resend")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reSendInvitation(@PathVariable long id) {
        huntingClubInvitationFeature.reSendInvitation(id);
    }

    @RequestMapping(
            method = RequestMethod.DELETE,
            value = "/invitation/{id:\\d+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInvitation(@PathVariable long id) {
        huntingClubInvitationFeature.deleteInvitation(id);
    }

    @RequestMapping(
            method = RequestMethod.PUT,
            value = "/invitation/{id:\\d+}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void acceptInvitation(@PathVariable long id) {
        final OccupationDTO occupationDTO = huntingClubInvitationFeature.acceptInvitation(id);
        crudFeature.create(occupationDTO);
    }

    @RequestMapping(
            method = RequestMethod.PUT,
            value = "/invitation/{id:\\d+}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rejectInvitation(@PathVariable long id) {
        huntingClubInvitationFeature.rejectInvitation(id);
    }
}

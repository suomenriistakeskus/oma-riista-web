package fi.riista.api.club;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.huntingclub.members.club.HuntingClubMemberCrudFeature;
import fi.riista.feature.huntingclub.members.club.HuntingClubMemberTypeDTO;
import fi.riista.feature.organization.occupation.OccupationDTO;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/club")
public class ClubMemberApiResource {

    @Resource
    private HuntingClubMemberCrudFeature crudFeature;

    @RequestMapping(
            method = RequestMethod.PUT,
            value = "/{clubId:\\d+}/member/{id:\\d+}/type",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public OccupationDTO updateMemberType(@PathVariable long clubId,
                                          @PathVariable long id,
                                          @RequestBody @Valid HuntingClubMemberTypeDTO dto) {
        return crudFeature.updateOccupationType(id, dto.getOccupationType());
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/{clubId:\\d+}/member/{id:\\d+}/locked",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ImmutableMap<String, Boolean> isLocked(@PathVariable long clubId,
                                                  @PathVariable long id) {
        return ImmutableMap.of("isLocked", crudFeature.isLocked(id));
    }

    @RequestMapping(
            method = RequestMethod.PUT,
            value = "/{clubId:\\d+}/member/{id:\\d+}/primarycontact")
    public void setPrimaryContact(@PathVariable long clubId, @PathVariable long id) {
        crudFeature.updatePrimaryContact(clubId, id);
    }

    @RequestMapping(
            method = RequestMethod.DELETE,
            value = "/{clubId:\\d+}/member/{id:\\d+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMember(@PathVariable long clubId, @PathVariable long id) {
        crudFeature.delete(id);
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/{clubId:\\d+}/member",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @CacheControl(policy = CachePolicy.NO_CACHE)
    public List<OccupationDTO> listMembers(@PathVariable long clubId) {
        return crudFeature.listMembers(clubId);
    }
}

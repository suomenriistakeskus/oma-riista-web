package fi.riista.api.club;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.huntingclub.members.group.GroupMemberCrudFeature;
import fi.riista.feature.huntingclub.members.group.GroupMemberTypeDTO;
import fi.riista.feature.organization.occupation.OccupationDTO;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping(value = "/api/v1/club/group/{groupId:\\d+}/member", produces = MediaType.APPLICATION_JSON_VALUE)
public class ClubGroupMemberApiResource {

    @Resource
    private GroupMemberCrudFeature crudFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(method = RequestMethod.GET)
    public List<OccupationDTO> listMembers(@PathVariable Long groupId) {
        return crudFeature.listMembers(groupId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public OccupationDTO createMember(@PathVariable Long groupId,
                                      @RequestBody @Validated
                                      OccupationDTO dto) {
        dto.setOrganisationId(groupId);
        return crudFeature.create(dto);
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/{id:\\d+}/locked",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ImmutableMap<String, Boolean> isLocked(@PathVariable long groupId,
                                                  @PathVariable long id) {
        return ImmutableMap.of("isLocked", crudFeature.isLocked(id));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id:\\d+}", method = RequestMethod.DELETE)
    public void deleteMember(@PathVariable Long id) {
        crudFeature.delete(id);
    }


    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(
            method = RequestMethod.PUT,
            value = "/{id:\\d+}/type",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public OccupationDTO updateMemberType(@PathVariable long groupId,
                                          @PathVariable long id,
                                          @RequestBody @Valid GroupMemberTypeDTO dto) {
        return crudFeature.updateOccupationType(id, dto.getOccupationType());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/order", method = RequestMethod.POST)
    public void updateContactOrder(@PathVariable Long groupId,
                                   @RequestBody List<Long> memberIds) {
        crudFeature.updateContactOrder(groupId, memberIds);
    }
}

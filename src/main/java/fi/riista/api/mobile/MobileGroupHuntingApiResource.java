package fi.riista.api.mobile;

import fi.riista.feature.gamediary.mobile.MobileGroupHuntingFeature;
import fi.riista.feature.gamediary.mobile.MobileGroupHuntingLeaderDTO;
import fi.riista.feature.gamediary.mobile.MobileHuntingGroupOccupationDTO;
import fi.riista.feature.huntingclub.hunting.MobileGroupHuntingAreaDTO;
import fi.riista.feature.huntingclub.hunting.MobileGroupHuntingStatusDTO;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/api/mobile/v2/grouphunting")
public class MobileGroupHuntingApiResource {

    @Resource
    private MobileGroupHuntingFeature feature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/groups")
    public MobileGroupHuntingLeaderDTO getGroups() {
        return feature.getHuntingGroups();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{groupId:\\d+}/members")
    public List<MobileHuntingGroupOccupationDTO> getMembers(@PathVariable final long groupId) {
        return feature.getMembers(groupId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{id:\\d+}/huntingArea")
    public ResponseEntity<MobileGroupHuntingAreaDTO> huntingArea(@PathVariable final long id) {
        final MobileGroupHuntingAreaDTO dto = feature.groupHuntingArea(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.noContent().build();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{groupId:\\d+}/status")
    public MobileGroupHuntingStatusDTO getStatus(@PathVariable final long groupId) {
        return feature.getGroupStatus(groupId);
    }
}

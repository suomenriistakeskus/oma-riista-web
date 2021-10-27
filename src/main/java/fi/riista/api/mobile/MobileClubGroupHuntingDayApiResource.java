package fi.riista.api.mobile;

import fi.riista.feature.huntingclub.hunting.day.mobile.MobileGroupHuntingDayCrudFeature;
import fi.riista.feature.huntingclub.hunting.day.mobile.MobileGroupHuntingDayDTO;
import fi.riista.feature.huntingclub.hunting.day.mobile.MobileGroupHuntingDayForDeerDTO;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/api/mobile/v2/grouphunting", produces = MediaType.APPLICATION_JSON_VALUE)
public class MobileClubGroupHuntingDayApiResource {

    @Resource
    private MobileGroupHuntingDayCrudFeature crudFeature;

    // List by group

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{groupId:\\d+}/huntingdays")
    public List<MobileGroupHuntingDayDTO> list(@PathVariable final long groupId) {
        return crudFeature.findByClubGroup(groupId);
    }

    // Single hunting day operations

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/huntingday/{id:\\d+}")
    public MobileGroupHuntingDayDTO read(@PathVariable final long id) {
        return crudFeature.read(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/huntingday", consumes = MediaType.APPLICATION_JSON_VALUE)
    public MobileGroupHuntingDayDTO create(@RequestBody @Valid final MobileGroupHuntingDayDTO dto) {
        return crudFeature.create(dto);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = "/huntingday/{id:\\d+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public MobileGroupHuntingDayDTO update(@PathVariable final long id,
                                           @RequestBody @Valid final MobileGroupHuntingDayDTO dto) {
        dto.setId(id);
        return crudFeature.update(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/huntingday/{id:\\d+}")
    public void delete(@PathVariable final long id) {
        crudFeature.delete(id);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/huntingday/get-or-create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public MobileGroupHuntingDayDTO getOrCreate(@RequestBody @Valid final MobileGroupHuntingDayForDeerDTO dto) {
        return crudFeature.getOrCreate(dto.getHuntingGroupId(), dto.getDate());
    }
}

package fi.riista.api.club;

import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayCrudFeature;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayDTO;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayForDeerDTO;
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

@RestController
@RequestMapping(value = "/api/v1/club/group/huntingday", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ClubGroupHuntingDayApiResource {

    @Resource
    private GroupHuntingDayCrudFeature groupHuntingDayCrudFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/{id:\\d+}", method = RequestMethod.GET)
    public GroupHuntingDayDTO read(@PathVariable final long id) {
        return groupHuntingDayCrudFeature.read(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public GroupHuntingDayDTO create(@RequestBody @Valid GroupHuntingDayDTO dto) {
        return groupHuntingDayCrudFeature.create(dto);
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/{id:\\d+}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public GroupHuntingDayDTO update(@PathVariable final long id,
                                     @RequestBody @Valid GroupHuntingDayDTO dto) {
        dto.setId(id);
        return groupHuntingDayCrudFeature.update(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id:\\d+}", method = RequestMethod.DELETE)
    public void delete(@PathVariable Long id) {
        groupHuntingDayCrudFeature.delete(id);
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/get-or-create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public GroupHuntingDayDTO getOrCreate(@RequestBody @Valid GroupHuntingDayForDeerDTO dto) {
        return groupHuntingDayCrudFeature.getOrCreate(dto.getHuntingGroupId(), dto.getDate());
    }
}

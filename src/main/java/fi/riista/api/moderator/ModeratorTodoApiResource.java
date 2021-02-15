package fi.riista.api.moderator;

import fi.riista.feature.permit.application.search.ModeratorApplicationsTodoDTO;
import fi.riista.feature.permit.application.search.ModeratorApplicationsTodoFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = ModeratorTodoApiResource.RESOURCE_URL)
public class ModeratorTodoApiResource {

    public static final String RESOURCE_URL = "api/v1/moderator/todo";

    @Resource
    private ModeratorApplicationsTodoFeature moderatorApplicationsTodoFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ModeratorApplicationsTodoDTO getMineTodoCount() {
        return moderatorApplicationsTodoFeature.getApplicationsTodoCount();
    }
}

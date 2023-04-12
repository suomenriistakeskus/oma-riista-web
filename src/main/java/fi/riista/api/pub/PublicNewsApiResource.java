package fi.riista.api.pub;

import fi.riista.feature.news.NewsDTO;
import fi.riista.feature.news.NewsFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1/anon/news", produces = APPLICATION_JSON_VALUE)
public class PublicNewsApiResource {

    @Resource
    private NewsFeature feature;

    @GetMapping(value = "/latest")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 300)
    public List<NewsDTO> latest() {
        return feature.listLatestNews();
    }
}

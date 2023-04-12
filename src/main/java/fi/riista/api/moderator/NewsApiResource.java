package fi.riista.api.moderator;

import fi.riista.feature.news.NewsDTO;
import fi.riista.feature.news.NewsFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1/news")
public class NewsApiResource {

    @Resource
    private NewsFeature newsFeature;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = APPLICATION_JSON_VALUE)
    public NewsDTO create(@RequestBody @Valid final NewsDTO dto) {
        return newsFeature.create(dto);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = "/{id:\\d+}", produces = APPLICATION_JSON_VALUE)
    public NewsDTO update(@PathVariable final long id, @RequestBody @Valid final NewsDTO dto) {
        return newsFeature.update(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/list", produces = APPLICATION_JSON_VALUE)
    public Slice<NewsDTO> list(
            @PageableDefault(sort = "publishTime", direction = Sort.Direction.DESC) final Pageable pageRequest) {
        return newsFeature.listNews(pageRequest);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id:\\d+}")
    public void delete(@PathVariable final long id) {
        newsFeature.delete(id);
    }
}

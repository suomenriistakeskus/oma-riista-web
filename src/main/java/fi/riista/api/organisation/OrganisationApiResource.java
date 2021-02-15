package fi.riista.api.organisation;

import fi.riista.feature.organization.OrganisationCrudFeature;
import fi.riista.feature.organization.OrganisationDTO;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/api/v1/organisation", produces = MediaType.APPLICATION_JSON_VALUE)
public class OrganisationApiResource {

    @Resource
    private OrganisationCrudFeature crudFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "{id:\\d+}", method = RequestMethod.GET)
    public OrganisationDTO read(@PathVariable Long id) {
        return crudFeature.read(id);
    }
}

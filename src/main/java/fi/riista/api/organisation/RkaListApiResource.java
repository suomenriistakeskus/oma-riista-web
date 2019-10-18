package fi.riista.api.organisation;

import fi.riista.feature.organization.occupation.search.RkaListFeature;
import fi.riista.feature.organization.occupation.search.RkaListOrganisationDTO;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping(value = "/api/v1/organisation/rka", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class RkaListApiResource {

    @Resource
    private RkaListFeature rkaListFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/list-active")
    public List<RkaListOrganisationDTO> listAreas(final Locale locale) {
        return rkaListFeature.listAreasWithActiveRhys(locale);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/list-all")
    public List<RkaListOrganisationDTO> listAllAreas(final Locale locale) {
        return rkaListFeature.listAreasWithAllRhys(locale);
    }
}

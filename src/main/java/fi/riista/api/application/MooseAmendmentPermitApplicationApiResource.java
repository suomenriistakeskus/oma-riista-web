package fi.riista.api.application;

import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.permit.application.amendment.HarvestPermitAmendmentApplicationCreateDTO;
import fi.riista.feature.permit.application.amendment.HarvestPermitAmendmentApplicationDTO;
import fi.riista.feature.permit.application.amendment.HarvestPermitAmendmentApplicationFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping(value = "/api/v1/harvestpermit/application/amendment")
public class MooseAmendmentPermitApplicationApiResource {

    @Resource
    private HarvestPermitAmendmentApplicationFeature harvestPermitAmendmentApplicationFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}", produces = MediaType.APPLICATION_JSON_VALUE)
    public HarvestPermitAmendmentApplicationDTO get(@PathVariable final long applicationId) {
        return harvestPermitAmendmentApplicationFeature.getApplication(applicationId);
    }

    @PutMapping(value = "/{applicationId:\\d+}", produces = MediaType.APPLICATION_JSON_VALUE)
    public HarvestPermitAmendmentApplicationDTO update(
            final @RequestBody @Valid HarvestPermitAmendmentApplicationDTO dto, final Locale locale) {

        return harvestPermitAmendmentApplicationFeature.updateApplication(dto, locale);
    }

    @PostMapping
    public HarvestPermitAmendmentApplicationDTO createAmendmentApplication(
            final @RequestBody @Valid HarvestPermitAmendmentApplicationCreateDTO dto, final Locale locale) {

        return harvestPermitAmendmentApplicationFeature.createAmendmentApplication(dto, locale);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{permitId:\\d+}/partners", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OrganisationNameDTO> listPartners(@PathVariable final long permitId) {
        return harvestPermitAmendmentApplicationFeature.listPartners(permitId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{permitId:\\d+}/species", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GameSpeciesDTO> listSpecies(@PathVariable final long permitId) {
        return harvestPermitAmendmentApplicationFeature.listSpecies(permitId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/list/{permitId:\\d+}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<HarvestPermitAmendmentApplicationDTO> list(@PathVariable final long permitId) {
        return harvestPermitAmendmentApplicationFeature.list(permitId);
    }
}

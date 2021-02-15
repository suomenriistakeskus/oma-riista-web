package fi.riista.api.application;

import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.derogation.area.DerogationPermitApplicationAddAreaAttachmentDTO;
import fi.riista.feature.permit.application.derogation.area.DerogationPermitApplicationAreaDTO;
import fi.riista.feature.permit.application.derogation.area.DerogationPermitApplicationAreaFeature;
import fi.riista.feature.permit.application.derogation.attachments.DerogationPermitApplicationAttachmentDTO;
import fi.riista.feature.permit.application.derogation.attachments.DerogationPermitApplicationAttachmentFeature;
import fi.riista.feature.permit.application.derogation.damage.DerogationPermitApplicationDamageDTO;
import fi.riista.feature.permit.application.derogation.damage.DerogationPermitApplicationDamageFeature;
import fi.riista.feature.permit.application.derogation.population.DerogationPermitApplicationSpeciesPopulationDTO;
import fi.riista.feature.permit.application.derogation.population.DerogationPermitApplicationSpeciesPopulationFeature;
import fi.riista.feature.permit.application.partner.ListPermitApplicationAreaPartnersFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static fi.riista.api.application.HarvestPermitApplicationApiResource.API_PREFIX;

@RestController
@RequestMapping(value = API_PREFIX + "/derogation")
public class DerogationPermitApplicationApiResource {

    @Resource
    private DerogationPermitApplicationAreaFeature derogationPermitApplicationAreaFeature;

    @Resource
    private ListPermitApplicationAreaPartnersFeature listPermitApplicationAreaPartnersFeature;

    @Resource
    private DerogationPermitApplicationDamageFeature derogationPermitApplicationDamageFeature;

    @Resource
    private DerogationPermitApplicationSpeciesPopulationFeature derogationPermitApplicationSpeciesPopulationFeature;

    @Resource
    private DerogationPermitApplicationAttachmentFeature derogationPermitApplicationAttachmentFeature;

    // AREA

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/area", produces = MediaType.APPLICATION_JSON_VALUE)
    public DerogationPermitApplicationAreaDTO getProtectedAreaInfo(@PathVariable final long applicationId) {
        return derogationPermitApplicationAreaFeature.getArea(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/area", produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateProtectedAreaInfo(@PathVariable final long applicationId,
                                        @Valid @RequestBody final DerogationPermitApplicationAreaDTO dto) {
        derogationPermitApplicationAreaFeature.updateProtectedArea(applicationId, dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("/{applicationId:\\d+}/area/clubs")
    public List<OrganisationNameDTO> listAvailableClubs(@PathVariable final long applicationId) {
        return listPermitApplicationAreaPartnersFeature.listAvailablePartners(applicationId);
    }


    // AREA ATTACHMENTS

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/{applicationId:\\d+}/area-attachment")
    public void addAreaAttachment(@PathVariable final long applicationId,
                                  @Valid @RequestBody final DerogationPermitApplicationAddAreaAttachmentDTO dto) {
        derogationPermitApplicationAreaFeature.addAreaAttachment(dto);
    }

    // DAMAGE, EVICTION, EFFECTS

    static class DamageList {
        @Valid
        public List<DerogationPermitApplicationDamageDTO> list;

        public List<DerogationPermitApplicationDamageDTO> getList() {
            return list;
        }

        public void setList(final List<DerogationPermitApplicationDamageDTO> list) {
            this.list = list;
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/damage", produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateDamage(
            @PathVariable final long applicationId,
            @Valid @RequestBody final DamageList request) {
        derogationPermitApplicationDamageFeature.saveSpeciesDamage(applicationId, request.list);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/damage", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DerogationPermitApplicationDamageDTO> getDamage(@PathVariable final long applicationId) {
        return derogationPermitApplicationDamageFeature.getSpeciesDamage(applicationId);
    }


    // POPULATION

    static class PopulationList {
        @Valid
        private List<DerogationPermitApplicationSpeciesPopulationDTO> list;

        public List<DerogationPermitApplicationSpeciesPopulationDTO> getList() {
            return list;
        }

        public void setList(final List<DerogationPermitApplicationSpeciesPopulationDTO> list) {
            this.list = list;
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/population", produces = MediaType.APPLICATION_JSON_VALUE)
    public void updatePopulation(
            @PathVariable final long applicationId,
            @Valid @RequestBody final PopulationList request) {
        derogationPermitApplicationSpeciesPopulationFeature.saveSpeciesPopulation(applicationId, request.list);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/population", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DerogationPermitApplicationSpeciesPopulationDTO> getPopulation(@PathVariable final long applicationId) {
        return derogationPermitApplicationSpeciesPopulationFeature.getSpeciesPopulation(applicationId);
    }


    // ATTACHMENTS

    static class AttachmentList {
        @Valid
        public List<DerogationPermitApplicationAttachmentDTO> list;

        public List<DerogationPermitApplicationAttachmentDTO> getList() {
            return list;
        }

        public void setList(final List<DerogationPermitApplicationAttachmentDTO> list) {
            this.list = list;
        }
    }

    @GetMapping(value = "/{applicationId:\\d+}/attachment", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DerogationPermitApplicationAttachmentDTO> listAttachments(final @PathVariable long applicationId,
                                                                          final @RequestParam(required = false) HarvestPermitApplicationAttachment.Type typeFilter) {
        return derogationPermitApplicationAttachmentFeature.listAttachments(applicationId, typeFilter);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/attachment", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateAttachmentDescriptions(@PathVariable final long applicationId,
                                             @RequestBody @Valid final AttachmentList request) {
        derogationPermitApplicationAttachmentFeature.updateAttachmentDescriptions(applicationId, request.list);
    }
}

package fi.riista.api.application;

import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplicationSummaryDTO;
import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplicationSummaryFeature;
import fi.riista.feature.permit.application.carnivore.applicant.CarnivorePermitApplicationApplicantFeature;
import fi.riista.feature.permit.application.carnivore.area.CarnivorePermitApplicationAddAreaAttachmentDTO;
import fi.riista.feature.permit.application.carnivore.area.CarnivorePermitApplicationAreaDTO;
import fi.riista.feature.permit.application.carnivore.area.CarnivorePermitApplicationAreaFeature;
import fi.riista.feature.permit.application.carnivore.attachments.CarnivorePermitApplicationAttachmentDTO;
import fi.riista.feature.permit.application.carnivore.attachments.CarnivorePermitApplicationAttachmentFeature;
import fi.riista.feature.permit.application.carnivore.justification.CarnivorePermitApplicationJustificationDTO;
import fi.riista.feature.permit.application.carnivore.justification.CarnivorePermitApplicationJustificationFeature;
import fi.riista.feature.permit.application.carnivore.species.CarnivorePermitApplicationSpeciesAmountDTO;
import fi.riista.feature.permit.application.carnivore.species.CarnivorePermitApplicationSpeciesAmountFeature;
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
@RequestMapping(value = API_PREFIX + "/carnivore")
public class CarnivorePermitApplicationApiResource {

    @Resource
    private CarnivorePermitApplicationApplicantFeature carnivorePermitApplicationApplicantFeature;

    @Resource
    private CarnivorePermitApplicationAreaFeature carnivorePermitApplicationAreaFeature;

    @Resource
    private CarnivorePermitApplicationSpeciesAmountFeature carnivorePermitApplicationSpeciesAmountFeature;

    @Resource
    private CarnivorePermitApplicationJustificationFeature carnivorePermitApplicationJustficationFeature;

    @Resource
    private CarnivorePermitApplicationAttachmentFeature carnivorePermitApplicationAttachmentFeature;

    @Resource
    private CarnivorePermitApplicationSummaryFeature carnivorePermitApplicationSummaryFeature;

    @Resource
    private ListPermitApplicationAreaPartnersFeature listPermitApplicationAreaPartnersFeature;
    // READ

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/full", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public CarnivorePermitApplicationSummaryDTO readDetails(@PathVariable final long applicationId) {
        return carnivorePermitApplicationSummaryFeature.readDetails(applicationId);
    }

    // PERMIT HOLDER

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/permit-holder", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PermitHolderDTO getPermitHolderinfo(@PathVariable final long applicationId) {
        return carnivorePermitApplicationApplicantFeature.getPermitHolderInfo(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/permit-holder")
    public void updatePermitHolder(@PathVariable final long applicationId,
                                   @Valid @RequestBody final PermitHolderDTO permitHolder) {

        carnivorePermitApplicationApplicantFeature.updatePermitHolder(applicationId, permitHolder);
    }

    // AREA

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/area", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public CarnivorePermitApplicationAreaDTO getProtectedAreaInfo(@PathVariable final long applicationId) {
        return carnivorePermitApplicationAreaFeature.getArea(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/area", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void updateProtectedAreaInfo(@PathVariable final long applicationId,
                                        @Valid @RequestBody final CarnivorePermitApplicationAreaDTO dto) {
        carnivorePermitApplicationAreaFeature.updateProtectedArea(applicationId, dto);
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
                                  @Valid @RequestBody final CarnivorePermitApplicationAddAreaAttachmentDTO dto) {
        carnivorePermitApplicationAreaFeature.addAreaAttachment(dto);
    }

    // SPECIES AMOUNT

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/species", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public CarnivorePermitApplicationSpeciesAmountDTO getSpeciesAmount(@PathVariable final long applicationId) {
        return carnivorePermitApplicationSpeciesAmountFeature.getSpeciesAmount(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/species", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void saveSpeciesAmount(final @PathVariable long applicationId,
                                  final @Valid @RequestBody CarnivorePermitApplicationSpeciesAmountDTO dto) {

        carnivorePermitApplicationSpeciesAmountFeature.saveSpeciesAmount(applicationId, dto);
    }

    // JUSTIFICATION

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/justification", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public CarnivorePermitApplicationJustificationDTO getJustification(@PathVariable final long applicationId) {
        return carnivorePermitApplicationJustficationFeature.getJustification(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/justification")
    public void updateJustification(@PathVariable final long applicationId,
                                    @Valid @RequestBody final CarnivorePermitApplicationJustificationDTO justification) {

        carnivorePermitApplicationJustficationFeature.updateJustification(applicationId, justification);
    }

    // ATTACHMENTS

    static class AttachmentList {
        @Valid
        public List<CarnivorePermitApplicationAttachmentDTO> list;

        public List<CarnivorePermitApplicationAttachmentDTO> getList() {
            return list;
        }

        public void setList(final List<CarnivorePermitApplicationAttachmentDTO> list) {
            this.list = list;
        }
    }

    @GetMapping(value = "/{applicationId:\\d+}/attachment", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<CarnivorePermitApplicationAttachmentDTO> listAttachments(final @PathVariable long applicationId,
                                                                         final @RequestParam(required = false) HarvestPermitApplicationAttachment.Type typeFilter) {
        return carnivorePermitApplicationAttachmentFeature.listAttachments(applicationId, typeFilter);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/attachment", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void updateAttachmentDescriptions(@PathVariable final long applicationId,
                                             @RequestBody @Valid final AttachmentList request) {
        carnivorePermitApplicationAttachmentFeature.updateAttachmentDescriptions(applicationId, request.list);
    }
}

package fi.riista.api.application;

import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.application.weapontransportation.applicant.WeaponTransportationApplicantFeature;
import fi.riista.feature.permit.application.weapontransportation.reason.ReasonDTO;
import fi.riista.feature.permit.application.weapontransportation.reason.WeaponTransportationReasonFeature;
import fi.riista.feature.permit.application.weapontransportation.justification.JustificationDTO;
import fi.riista.feature.permit.application.weapontransportation.justification.WeaponTransportationJustificationFeature;
import fi.riista.feature.permit.application.weapontransportation.summary.SummaryDTO;
import fi.riista.feature.permit.application.weapontransportation.summary.WeaponTransportationSummaryFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import java.util.Locale;

import static fi.riista.api.application.HarvestPermitApplicationApiResource.API_PREFIX;

@RestController
@RequestMapping(value = API_PREFIX + "/weapontransportation")
public class WeaponTransportationPermitApplicationApiResource {

    @Resource
    private WeaponTransportationApplicantFeature weaponTransportationApplicantFeature;

    @Resource
    private WeaponTransportationReasonFeature weaponTransportationReasonFeature;

    @Resource
    private WeaponTransportationJustificationFeature weaponTransportationJustificationFeature;

    @Resource
    private WeaponTransportationSummaryFeature weaponTransportationSummaryFeature;

    // PERMIT HOLDER

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/permit-holder", produces = MediaType.APPLICATION_JSON_VALUE)
    public PermitHolderDTO getPermitHolderinfo(@PathVariable final long applicationId) {
        return weaponTransportationApplicantFeature.getPermitHolderInfo(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/permit-holder")
    public void updatePermitHolder(@PathVariable final long applicationId,
                                   @Valid @RequestBody final PermitHolderDTO permitHolder) {
        weaponTransportationApplicantFeature.updatePermitHolder(applicationId, permitHolder);
    }

    // REASON

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/reason")
    public ReasonDTO getReason(@PathVariable final long applicationId) {
        return weaponTransportationReasonFeature.getReason(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/reason")
    public void updateReason(@PathVariable final long applicationId,
                             @Valid @RequestBody final ReasonDTO reason) {
        weaponTransportationReasonFeature.updateReason(applicationId, reason);
    }

    // JUSTIFICATION

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/justification")
    public JustificationDTO getJustification(@PathVariable final long applicationId) {
        return weaponTransportationJustificationFeature.getJustification(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/justification")
    public void updateJustification(@PathVariable final long applicationId,
                                    @Valid @RequestBody final JustificationDTO justification) {
        weaponTransportationJustificationFeature.updateJustification(applicationId, justification);
    }

    // READ

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/full", produces = MediaType.APPLICATION_JSON_VALUE)
    public SummaryDTO readDetails(@PathVariable final long applicationId, final Locale locale) {
        return weaponTransportationSummaryFeature.readDetails(applicationId, locale);
    }
}

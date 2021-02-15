package fi.riista.api.application;

import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.application.disability.applicant.DisabilityPermitApplicationApplicantFeature;
import fi.riista.feature.permit.application.disability.basicinfo.BasicInfoDTO;
import fi.riista.feature.permit.application.disability.basicinfo.DisabilityPermitBasicInfoFeature;
import fi.riista.feature.permit.application.disability.justification.DisabilityPermitJustificationFeature;
import fi.riista.feature.permit.application.disability.justification.JustificationDTO;
import fi.riista.feature.permit.application.disability.summary.DisabilityPermitSummaryFeature;
import fi.riista.feature.permit.application.disability.summary.DisabilityPermitSummaryDTO;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import java.util.Locale;

import static fi.riista.api.application.HarvestPermitApplicationApiResource.API_PREFIX;

@RestController
@RequestMapping(value = API_PREFIX + "/disability")
public class DisabilityPermitApplicationApiResource {

    @Resource
    private DisabilityPermitApplicationApplicantFeature applicantFeature;

    @Resource
    private DisabilityPermitBasicInfoFeature basicInfoFeature;

    @Resource
    private DisabilityPermitJustificationFeature justificationFeature;

    @Resource
    private DisabilityPermitSummaryFeature summaryFeature;

    // PERMIT HOLDER

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/permit-holder", produces = MediaType.APPLICATION_JSON_VALUE)
    public PermitHolderDTO getPermitHolderinfo(@PathVariable final long applicationId) {
        return applicantFeature.getPermitHolderInfo(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/permit-holder")
    public void updatePermitHolder(@PathVariable final long applicationId,
                                   @Valid @RequestBody final PermitHolderDTO permitHolder) {
        applicantFeature.updatePermitHolder(applicationId, permitHolder);
    }

    // BASIC INFO

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/basicinfo", produces = MediaType.APPLICATION_JSON_VALUE)
    public BasicInfoDTO getBasicInfo(@PathVariable final long applicationId) {
        return basicInfoFeature.getBasicInfo(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "/{applicationId:\\d+}/basicinfo")
    public void updateBasicInfo(@PathVariable final long applicationId,
                                @Valid @RequestBody final BasicInfoDTO basicInfo) {
        basicInfoFeature.updateBasicInfo(applicationId, basicInfo);
    }

    // JUSTIFICATION

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/justification", produces = MediaType.APPLICATION_JSON_VALUE)
    public JustificationDTO getJustification(@PathVariable final long applicationId) {
        return justificationFeature.getJustification(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "/{applicationId:\\d+}/justification")
    public void updatePeriod(@PathVariable final long applicationId,
                             @Valid @RequestBody final JustificationDTO dto) {
        justificationFeature.updateJustification(applicationId, dto);
    }

    // READ

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/full", produces = MediaType.APPLICATION_JSON_VALUE)
    public DisabilityPermitSummaryDTO readDetails(@PathVariable final long applicationId, final Locale locale) {
        return summaryFeature.readDetails(applicationId, locale);
    }

}

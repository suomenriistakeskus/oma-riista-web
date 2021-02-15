package fi.riista.api.admin;

import fi.riista.api.decision.permit.PermitDecisionPdfController;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevisionFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(value = "/api/v1/admin/decision")
public class PermitDecisionPublicPdfApiResource {

    @Resource
    private PermitDecisionRevisionFeature feature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/public-pdf")
    public boolean generatePublicPdf(final HttpServletRequest httpServletRequest,
                                     final HttpServletResponse httpServletResponse) {
        return feature.generatePublicPdfForSingleDecision(PermitDecisionPdfController::getPublicHtmlPath);

    }
}

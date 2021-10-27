package fi.riista.api.admin;

import fi.riista.api.decision.permit.PermitDecisionPdfController;
import fi.riista.feature.permit.application.conflict.SearchApplicationConflictsFeature;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevisionFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(value = "/api/v1/admin")
public class HarvestPermitApplicationConflictApiResource {

    @Resource
    private SearchApplicationConflictsFeature feature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/batch/{batchId:\\d+}/conflict/{conflictId:\\d+}/retry")
    public String createArchiveIfMissing(@PathVariable final long batchId,
                                         @PathVariable final long conflictId,
                                         @RequestParam final int chunkSize) throws Exception {
        return feature.adminRetryConflictCalculation(batchId, conflictId, chunkSize);
    }
}

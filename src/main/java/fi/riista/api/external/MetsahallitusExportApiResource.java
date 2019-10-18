package fi.riista.api.external;

import fi.riista.integration.metsahallitus.MetsahallitusHarvestSummaryFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.joda.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/api/v1/export/metsahallitus")
public class MetsahallitusExportApiResource {

    @Resource
    private MetsahallitusHarvestSummaryFeature metsahallitusHarvestSummaryFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/saaliit/hirvi", produces = "text/csv")
    public String harvestMetsahallitusHirvi(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate end) {
        return metsahallitusHarvestSummaryFeature.getHirviSummary(start, end);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/saaliit/pienriista", produces = "text/csv")
    public String harvestMetsahallitusPienriista(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate end) {
        return metsahallitusHarvestSummaryFeature.getPienriistaSummary(start, end);
    }

}

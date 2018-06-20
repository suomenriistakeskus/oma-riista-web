package fi.riista.api.external;

import fi.riista.config.web.CSVHttpResponse;
import fi.riista.integration.lupahallinta.HarvestReportExportCSVDTO;
import fi.riista.integration.lupahallinta.HarvestReportExportToLupahallintaFeature;
import fi.riista.integration.lupahallinta.HarvestReportListCSVResponse;
import fi.riista.integration.lupahallinta.HuntingClubExportToLupahallintaFeature;
import fi.riista.integration.lupahallinta.LHHuntingClubCSVRowCSVResponse;
import fi.riista.integration.lupahallinta.LHMooselikeHarvestsCSVRowResponse;
import fi.riista.integration.lupahallinta.LupaHallintaExportFeature;
import fi.riista.integration.lupahallinta.MooselikeHarvestExportToLupahallintaFeature;
import fi.riista.integration.lupahallinta.club.LHHuntingClubCSVRow;
import fi.riista.integration.lupahallinta.club.LHMooselikeHarvestsCSVRow;
import fi.riista.util.MediaTypeExtras;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/export/lupahallinta")
public class LHExportApiResource {

    @Resource
    private LupaHallintaExportFeature feature;

    @Resource
    private HarvestReportExportToLupahallintaFeature harvestReportExportToLupahallintoFeature;

    @Resource
    private HuntingClubExportToLupahallintaFeature huntingClubExportToLupahallintoFeature;

    @Resource
    private MooselikeHarvestExportToLupahallintaFeature mooselikeHarvestExportToLupahallintaFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(produces = MediaType.APPLICATION_XML_VALUE)
    public String getLupaHallintaExportData() {
        return feature.export();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "harvestreport", produces = MediaTypeExtras.TEXT_CSV_VALUE)
    public CSVHttpResponse harvestReportCsv(@RequestParam(required = false) Long after) {
        final List<HarvestReportExportCSVDTO> rows = harvestReportExportToLupahallintoFeature.exportToCSCV(after);
        return HarvestReportListCSVResponse.create(rows);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "huntingclubs", produces = MediaTypeExtras.TEXT_CSV_VALUE)
    public CSVHttpResponse huntingClubsCsv() {
        final List<LHHuntingClubCSVRow> rows = huntingClubExportToLupahallintoFeature.exportToCSCV();
        return LHHuntingClubCSVRowCSVResponse.create(rows);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "mooselikeharvests", produces = MediaTypeExtras.TEXT_CSV_VALUE)
    public CSVHttpResponse mooselikeHarvests(@RequestParam int huntingYear) {
        final List<LHMooselikeHarvestsCSVRow> rows = mooselikeHarvestExportToLupahallintaFeature.exportToCSV(huntingYear);
        return LHMooselikeHarvestsCSVRowResponse.create(rows);
    }
}

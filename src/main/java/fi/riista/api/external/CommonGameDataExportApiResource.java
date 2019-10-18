package fi.riista.api.external;

import fi.riista.integration.common.export.CommonHarvestExportFeature;
import fi.riista.integration.common.export.CommonHarvestPermitExportFeature;
import fi.riista.integration.common.export.CommonHuntingSummaryExportFeature;
import fi.riista.integration.common.export.CommonObservationExportFeature;
import fi.riista.integration.common.export.CommonSrvaEventExportFeature;
import fi.riista.integration.common.export.harvests.CHAR_Harvests;
import fi.riista.integration.common.export.huntingsummaries.CSUM_HuntingSummaries;
import fi.riista.integration.common.export.observations.COBS_Observations;
import fi.riista.integration.common.export.permits.CPER_Permits;
import fi.riista.integration.common.export.srva.CEV_SrvaEvents;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/api/v1/export/gamedata")
public class CommonGameDataExportApiResource {

    @Resource
    private CommonHarvestExportFeature commonHarvestExportFeature;

    @Resource
    private CommonObservationExportFeature commonObservationExportFeature;

    @Resource
    private CommonSrvaEventExportFeature commonSrvaEventExportFeature;

    @Resource
    private CommonHarvestPermitExportFeature commonHarvestPermitExportFeature;

    @Resource
    private CommonHuntingSummaryExportFeature commonHuntingSummaryExportFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/observation", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public COBS_Observations getObservations(
            final @RequestParam("year") int year, final @RequestParam("month") int month) {
        return commonObservationExportFeature.exportObservations(year, month);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/observation/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String getObservationsXml(final @RequestParam("year") int year, final @RequestParam("month") int month) {
        return commonObservationExportFeature.exportObservationsAsXml(year, month);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/harvest", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public CHAR_Harvests getHarvests(final @RequestParam("year") int year, final @RequestParam("month") int month) {
        return commonHarvestExportFeature.exportAllHarvests(year, month);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/harvest/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String getHarvestsXml(final @RequestParam("year") int year, final @RequestParam("month") int month) {
        return commonHarvestExportFeature.exportAllHarvestsAsXml(year, month);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/harvest/rvr", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public CHAR_Harvests getRVRHarvests(final @RequestParam("year") int year, final @RequestParam("month") int month) {
        return commonHarvestExportFeature.exportRVRHarvests(year, month);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/harvest/rvr/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String getRVRHarvestsXml(final @RequestParam("year") int year, final @RequestParam("month") int month) {
        return commonHarvestExportFeature.exportRVRHarvestsAsXml(year, month);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/srva", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public CEV_SrvaEvents getSrvaEvents(final @RequestParam("year") int year, final @RequestParam("month") int month) {
        return commonSrvaEventExportFeature.exportSrvaEvents(year, month);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/srva/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String getSrvaEventsXml(final @RequestParam("year") int year, final @RequestParam("month") int month) {
        return commonSrvaEventExportFeature.exportSrvaEventsAsXml(year, month);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/permit", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public CPER_Permits getPermits(final @RequestParam("year") int year) {
        return commonHarvestPermitExportFeature.exportPermits(year);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/permit/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String getPermitsXml(final @RequestParam("year") int year) {
        return commonHarvestPermitExportFeature.exportPermitsAsXml(year);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/huntingsummary", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public CSUM_HuntingSummaries getMooseHuntingSummaries(final @RequestParam("year") int year) {
        return commonHuntingSummaryExportFeature.exportHuntingSummaries(year);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/huntingsummary/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String getMooseHuntingSummariesXml(final @RequestParam("year") int year) {
        return commonHuntingSummaryExportFeature.exportHuntingSummariesAsXml(year);
    }
}

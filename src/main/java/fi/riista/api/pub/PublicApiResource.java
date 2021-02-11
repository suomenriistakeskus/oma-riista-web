package fi.riista.api.pub;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.common.MunicipalityFeature;
import fi.riista.feature.gis.GISWGS84Point;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.OccupationGroupType;
import fi.riista.feature.permit.application.archive.PermitApplicationArchiveDownloadFeature;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevisionDownloadFeature;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevisionFeature;
import fi.riista.feature.pub.calendar.PublicCalendarEventGroupTypeDTO;
import fi.riista.feature.pub.calendar.PublicCalendarEventSearchDTO;
import fi.riista.feature.pub.calendar.PublicCalendarEventSearchFeature;
import fi.riista.feature.pub.calendar.PublicCalendarEventSearchResultDTO;
import fi.riista.feature.pub.municipality.PublicMunicipalityDTO;
import fi.riista.feature.pub.occupation.PublicOccupationGroupTypeDTO;
import fi.riista.feature.pub.occupation.PublicOccupationSearchFeature;
import fi.riista.feature.pub.occupation.PublicOccupationSearchParameters;
import fi.riista.feature.pub.occupation.PublicOccupationsAndOrganisationsDTO;
import fi.riista.feature.pub.occupation.PublicOrganisationDTO;
import fi.riista.feature.pub.rhy.RhyWithRkaResultDTO;
import fi.riista.feature.pub.season.PublicHarvestSeasonDTO;
import fi.riista.feature.pub.season.PublicHarvestSeasonFeature;
import fi.riista.feature.pub.statistics.PublicBearReportFeature;
import fi.riista.feature.pub.statistics.PublicHarvestPivotTableFeature;
import fi.riista.feature.pub.statistics.PublicWolfReportFeature;
import fi.riista.util.DateUtil;
import fi.riista.util.Locales;
import fi.riista.util.Patterns;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.geojson.FeatureCollection;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping(value = PublicApiResource.API_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
public class PublicApiResource {

    public static final String API_PREFIX = "/api/v1/anon";

    @Resource
    private PublicOccupationSearchFeature occupationSearchFeature;

    @Resource
    private PublicCalendarEventSearchFeature calendarEventSearchFeature;

    @Resource
    private PublicHarvestSeasonFeature harvestSeasonPublicFeature;

    @Resource
    private PublicHarvestPivotTableFeature harvestSpecimenPivotTableFeature;

    @Resource
    private PublicWolfReportFeature wolfReportFeature;

    @Resource
    private PublicBearReportFeature bearReportFeature;

    @Resource
    private PermitDecisionRevisionFeature permitDecisionRevisionFeature;

    @Resource
    private PermitDecisionRevisionDownloadFeature permitDecisionRevisionDownloadFeature;

    @Resource
    private PermitApplicationArchiveDownloadFeature permitApplicationArchiveDownloadFeature;

    @Resource
    private MunicipalityFeature municipalityFeature;


    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/rk")
    public PublicOrganisationDTO getRiistakeskus(@RequestParam(required = false) final String lang) {
        setLocale(lang);
        return occupationSearchFeature.getRiistakeskus();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{organisationType:\\w+}/{officialCode:\\w+}")
    public PublicOrganisationDTO getOrganisation(
            @PathVariable final OrganisationType organisationType,
            @PathVariable final String officialCode,
            @RequestParam(required = false) final String lang) {

        setLocale(lang);
        return occupationSearchFeature.getByTypeAndOfficialCode(organisationType, officialCode);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/tehtavatyypit")
    public List<PublicOccupationGroupTypeDTO> getOccupationTypes(@RequestParam(required = false) final String lang) {
        setLocale(lang);
        return occupationSearchFeature.getAllOccupationGroupTypes();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/tehtavat")
    public PublicOccupationsAndOrganisationsDTO listOccupations(
            @RequestParam(required = false) final String areaId,
            @RequestParam(required = false) final Collection<String> rhyId,
            @RequestParam(required = false) final OrganisationType organisationType,
            @RequestParam(required = false) final OccupationGroupType occupationType,
            @RequestParam(required = false) final Integer pageSize,
            @RequestParam(required = false) final Integer pageNumber,
            @RequestParam(required = false) final String lang) {

        setLocale(lang);

        return occupationSearchFeature.findOccupationsAndOrganisations(PublicOccupationSearchParameters.builder()
                .withAreaId(areaId)
                .withRhyIds(rhyId)
                .withOrganisationType(organisationType)
                .withOccupationType(OccupationGroupType.getOccupationTypes(occupationType))
                .withPageSize(pageSize)
                .withPageNumber(pageNumber)
                .build());
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/tehtavat/kunta")
    public PublicOccupationsAndOrganisationsDTO listOccupationsByMunicipality(
            @RequestParam(required = false) final String municipalityCode,
            @RequestParam(required = false) final OrganisationType organisationType,
            @RequestParam(required = false) final OccupationGroupType occupationType,
            @RequestParam(required = false) final Integer pageSize,
            @RequestParam(required = false) final Integer pageNumber,
            @RequestParam(required = false) final String lang) {

        setLocale(lang);

        final PublicMunicipalityDTO municipality = municipalityFeature.findMunicipality(municipalityCode, Locales.getLocaleByLanguageCode(lang));
        if (municipality.getRhyIds().isEmpty()) {
            return new PublicOccupationsAndOrganisationsDTO(true, Collections.emptyList(), Collections.emptyList());
        }

        return occupationSearchFeature.findOccupationsAndOrganisations(PublicOccupationSearchParameters.builder()
                .withRhyIds(municipality.getRhyIds())
                .withOrganisationType(organisationType)
                .withOccupationType(OccupationGroupType.getOccupationTypes(occupationType))
                .withPageSize(pageSize)
                .withPageNumber(pageNumber)
                .build());
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/tapahtumatyypit")
    public List<PublicCalendarEventGroupTypeDTO> getCalendarEventTypes(@RequestParam(required = false) final String lang) {
        setLocale(lang);
        return calendarEventSearchFeature.getCalendarEventGroupTypes();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("/tapahtumat")
    public PublicCalendarEventSearchResultDTO listCalendarEvents(
            @ModelAttribute @Valid final PublicCalendarEventSearchDTO params,
            @RequestParam(required = false) final String lang) {

        setLocale(lang);
        fixBeginToBeBeforeEnd(params);
        return calendarEventSearchFeature.findCalendarEvents(params);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/tapahtumat/kunta")
    public PublicCalendarEventSearchResultDTO listCalendarEventsByMunicipality(
            @ModelAttribute @Valid final PublicCalendarEventSearchDTO params,
            @RequestParam(required = false) final String municipalityCode,
            @RequestParam(required = false) final String lang) {

        setLocale(lang);
        fixBeginToBeBeforeEnd(params);

        final PublicMunicipalityDTO municipality = municipalityFeature.findMunicipality(municipalityCode, Locales.getLocaleByLanguageCode(lang));
        if (municipality.getRhyIds().isEmpty()) {
            return new PublicCalendarEventSearchResultDTO(Collections.emptyList(), true);
        }
        params.setRhyId(municipality.getRhyIds());

        return calendarEventSearchFeature.findCalendarEvents(params);
    }


    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("/rhy")
    public RhyWithRkaResultDTO getRhyByLocation(
            @RequestParam final double latitude,
            @RequestParam final double longitude,
            @RequestParam(required = false) final String lang) {

        setLocale(lang);
        return occupationSearchFeature.getRkaAndRhyByWGS84Location(GISWGS84Point.create(latitude, longitude));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("/kunnat")
    public List<PublicMunicipalityDTO> listMunicipalities(@RequestParam(required = false) final String lang) {
        final Locale locale = Locales.getLocaleByLanguageCode(lang);
        return municipalityFeature.listMunicipalities(locale);
    }

    private static void fixBeginToBeBeforeEnd(final PublicCalendarEventSearchDTO params) {
        if (params.getEnd().isBefore(params.getBegin())) {
            final LocalDate begin = params.getBegin();
            final LocalDate end = params.getEnd();
            params.setBegin(end);
            params.setEnd(begin);
        }
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("/kiintiometsastys")
    public List<PublicHarvestSeasonDTO> listSeasonsWithQuotas(@RequestParam(required = false) final Boolean onlyActive) {
        return harvestSeasonPublicFeature.listSeasonsWithQuotas(onlyActive);
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 600)
    @GetMapping("/saaliit/rka")
    public Object harvestByRka(
            @RequestParam(required = false) final Integer species,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate end) {
        return harvestSpecimenPivotTableFeature.summary(species, start, end);
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 600)
    @GetMapping("/saaliit/rka/{officialCode:\\w+}")
    public Object harvestByRhy(
            @PathVariable final String officialCode,
            @RequestParam(required = false) final Integer species,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate end) {
        return harvestSpecimenPivotTableFeature.summaryForRka(species, officialCode, start, end);
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 600)
    @GetMapping("/saaliit/susi/kannanhoidollinen/vuodet")
    public List<Map<String, Object>> getWolfYears() {
        final IntStream range = IntStream.rangeClosed(PublicWolfReportFeature.MIN_YEAR,
                                                      PublicWolfReportFeature.MAX_YEAR);
        return generateYearRange(DateTime.now(), range);
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 600)
    @GetMapping("/saaliit/susi/kannanhoidollinen")
    public FeatureCollection getWolfPropertyPolygon(@RequestParam final Integer year) {
        return Optional.ofNullable(year).map(wolfReportFeature::report).orElse(null);
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 600)
    @GetMapping("/saaliit/karhu/kaikki/vuodet")
    public List<Map<String, Object>> getBearYears() {
        return generateYearRange(PublicBearReportFeature.MIN_YEAR);
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 600)
    @GetMapping("/saaliit/karhu/kaikki")
    public FeatureCollection getBearPropertyPolygon(@RequestParam final Integer year) {
        return Optional.ofNullable(year).map(bearReportFeature::report).orElse(null);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("/decision/receiver/download/{uuid:" + Patterns.UUID + "}")
    public PermitDecisionDownloadDTO getDownloadLinks(@PathVariable final UUID uuid) {

        final long revisionId = permitDecisionRevisionFeature.resolveRevisionIdByReceiverUuid(uuid);

        return permitDecisionRevisionDownloadFeature.getDownloadLinks(uuid, revisionId);
    }

    // TODO: After links in old decision emails are no needed to be rerouted, this can be removed
    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("/decision/receiver/pdf/download/{uuid:" + Patterns.UUID + "}")
    public void getDecisionRedirect(@PathVariable final UUID uuid,
                                    final HttpServletResponse response) throws IOException {
        response.sendRedirect("/#/public/decision/" + uuid);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("/decision/receiver/decision-pdf/download/{uuid:" + Patterns.UUID + "}")
    public void getDecision(@PathVariable final UUID uuid,
                            final HttpServletResponse response) throws IOException {

        final long revisionId = permitDecisionRevisionFeature.updateViewCountAndResolveRevisionIdByReceiverUuid(uuid);
        permitDecisionRevisionDownloadFeature.downloadPdfNoAuthorization(revisionId, response);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("/decision/receiver/attachment/download/{uuid:" + Patterns.UUID + "}/{attachmentId:\\d+}")
    public void getAttachment(@PathVariable final UUID uuid, @PathVariable final long attachmentId,
                              final HttpServletResponse response) throws IOException {

        final long revisionId = permitDecisionRevisionFeature.resolveRevisionIdByReceiverUuid(uuid);
        permitDecisionRevisionDownloadFeature.downloadDecisionAttachmentNoAuthorization(revisionId, attachmentId,
                                                                                        response);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("/application/zip/{uuid:" + Patterns.UUID + "}")
    public void getApplicationArchive(final @PathVariable UUID uuid,
                                      final HttpServletResponse response) throws IOException {
        permitApplicationArchiveDownloadFeature.downloadArchiveWithoutAuthorization(uuid, response);
    }

    private static List<Map<String, Object>> generateYearRange(final int beginYear) {
        final DateTime now = DateTime.now();
        final LocalDate begin = DateUtil.huntingYearInterval(beginYear).getStart().toLocalDate();
        final LocalDate end = now.toLocalDate();
        final IntStream years = DateUtil.huntingYearsBetween(begin, end);
        return generateYearRange(now, years);
    }

    private static List<Map<String, Object>> generateYearRange(final DateTime now, final IntStream years) {
        return years.<Map<String, Object>>mapToObj(year -> {
            final Interval interval = DateUtil.huntingYearInterval(year);

            return ImmutableMap.of("year", year,
                                   "current", interval.contains(now),
                                   "text", String.format("%s-%s", interval.getStart().getYear(),
                                                         interval.getEnd().getYear()));
        }).collect(toList());
    }

    private static void setLocale(final String lang) {
        if (lang != null) {
            LocaleContextHolder.setLocale(new Locale(lang));
        }
    }
}

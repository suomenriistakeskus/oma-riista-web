package fi.riista.api.pub;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.pub.calendar.PublicCalendarEventSearchDTO;
import fi.riista.feature.pub.calendar.PublicCalendarEventSearchFeature;
import fi.riista.feature.pub.calendar.PublicCalendarEventSearchResultDTO;
import fi.riista.feature.pub.calendar.PublicCalendarEventTypeDTO;
import fi.riista.feature.pub.occupation.PublicOccupationSearchFeature;
import fi.riista.feature.pub.occupation.PublicOccupationSearchParameters;
import fi.riista.feature.pub.occupation.PublicOccupationTypeDTO;
import fi.riista.feature.pub.occupation.PublicOccupationsAndOrganisationsDTO;
import fi.riista.feature.pub.occupation.PublicOrganisationDTO;
import fi.riista.feature.pub.season.PublicHarvestSeasonDTO;
import fi.riista.feature.pub.season.PublicHarvestSeasonFeature;
import fi.riista.feature.pub.statistics.PublicBearReportFeature;
import fi.riista.feature.pub.statistics.PublicHarvestPivotTableFeature;
import fi.riista.feature.pub.statistics.PublicMetsahallitusHarvestSummaryFeature;
import fi.riista.feature.pub.statistics.PublicWolfReportFeature;
import fi.riista.util.DateUtil;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.geojson.FeatureCollection;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping(value = PublicApiResource.API_PREFIX, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
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
    private PublicMetsahallitusHarvestSummaryFeature metsahallitusHarvestSummaryFeature;

    @Resource
    private PublicWolfReportFeature wolfReportFeature;

    @Resource
    private PublicBearReportFeature bearReportFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/rk", method = RequestMethod.GET)
    public PublicOrganisationDTO getRiistakeskus(@RequestParam(required = false) String lang) {
        setLocale(lang);
        return occupationSearchFeature.getRiistakeskus();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/{organisationType:\\w+}/{officialCode:\\w+}", method = RequestMethod.GET)
    public PublicOrganisationDTO getOrganisation(
            @PathVariable OrganisationType organisationType,
            @PathVariable String officialCode,
            @RequestParam(required = false) String lang) {

        setLocale(lang);
        return occupationSearchFeature.getByTypeAndOfficialCode(organisationType, officialCode);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/tehtavatyypit", method = RequestMethod.GET)
    public List<PublicOccupationTypeDTO> getOccupationTypes(@RequestParam(required = false) String lang) {
        setLocale(lang);
        return occupationSearchFeature.getAllOccupationTypes();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/tehtavat", method = RequestMethod.GET)
    public PublicOccupationsAndOrganisationsDTO listOccupations(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String rhyId,
            @RequestParam(required = false) OrganisationType organisationType,
            @RequestParam(required = false) OccupationType occupationType,
            @RequestParam(required = false) String lang) {

        setLocale(lang);

        return occupationSearchFeature.findOccupationsAndOrganisations(PublicOccupationSearchParameters.builder()
                .withAreaId(areaId)
                .withRhyId(rhyId)
                .withOrganisationType(organisationType)
                .withOccupationType(occupationType)
                .build());
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/tapahtumatyypit", method = RequestMethod.GET)
    public List<PublicCalendarEventTypeDTO> getCalendarEventTypes(@RequestParam(required = false) String lang) {
        setLocale(lang);
        return calendarEventSearchFeature.getCalendarEventTypes();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/tapahtumat", method = RequestMethod.GET)
    public PublicCalendarEventSearchResultDTO listCalendarEvents(
            @ModelAttribute @Valid PublicCalendarEventSearchDTO params, @RequestParam(required = false) String lang) {

        setLocale(lang);
        fixBeginToBeBeforeEnd(params);
        return calendarEventSearchFeature.findCalendarEvents(params);
    }

    private static void fixBeginToBeBeforeEnd(PublicCalendarEventSearchDTO params) {
        if (params.getEnd().isBefore(params.getBegin())) {
            LocalDate begin = params.getBegin();
            LocalDate end = params.getEnd();
            params.setBegin(end);
            params.setEnd(begin);
        }
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/kiintiometsastys", method = RequestMethod.GET)
    public List<PublicHarvestSeasonDTO> listSeasonsWithQuotas(@RequestParam(required = false) Boolean onlyActive) {
        return harvestSeasonPublicFeature.listSeasonsWithQuotas(onlyActive);
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 600)
    @RequestMapping(value = "/saaliit/rka", method = RequestMethod.GET)
    public Object harvestByRka(
            @RequestParam(required = false) Integer species,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return harvestSpecimenPivotTableFeature.summary(species, start, end);
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 600)
    @RequestMapping(value = "/saaliit/rka/{officialCode:\\w+}", method = RequestMethod.GET)
    public Object harvestByRhy(
            @PathVariable String officialCode,
            @RequestParam(required = false) Integer species,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return harvestSpecimenPivotTableFeature.summaryForRka(species, officialCode, start, end);
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 600)
    @RequestMapping(value = "/saaliit/metsahallitus/hirvi", method = RequestMethod.GET, produces = "text/csv")
    public Object harvestMetsahallitusHirvi(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return metsahallitusHarvestSummaryFeature.hirviSummary(start, end);
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 600)
    @RequestMapping(value = "/saaliit/metsahallitus/pienriista", method = RequestMethod.GET, produces = "text/csv")
    public Object harvestMetsahallitusPienriista(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return metsahallitusHarvestSummaryFeature.pienriistaSummary(start, end);
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 600)
    @RequestMapping(value = "/saaliit/susi/kannanhoidollinen/vuodet", method = RequestMethod.GET)
    public List<Map<String, Object>> getWolfYears() {
        final IntStream range = IntStream.rangeClosed(PublicWolfReportFeature.MIN_YEAR, PublicWolfReportFeature.MAX_YEAR);
        return generateYearRange(DateTime.now(), range);
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 600)
    @RequestMapping(value = "/saaliit/susi/kannanhoidollinen", method = RequestMethod.GET)
    public FeatureCollection getWolfPropertyPolygon(@RequestParam Integer year) {
        return Optional.ofNullable(year).map(wolfReportFeature::report).orElse(null);
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 600)
    @RequestMapping(value = "/saaliit/karhu/kaikki/vuodet", method = RequestMethod.GET)
    public List<Map<String, Object>> getBearYears() {
        return generateYearRange(PublicBearReportFeature.MIN_YEAR);
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 600)
    @RequestMapping(value = "/saaliit/karhu/kaikki", method = RequestMethod.GET)
    public FeatureCollection getBearPropertyPolygon(@RequestParam Integer year) {
        return Optional.ofNullable(year).map(bearReportFeature::report).orElse(null);
    }

    private static List<Map<String, Object>> generateYearRange(int beginYear) {
        final DateTime now = DateTime.now();
        final LocalDate begin = DateUtil.huntingYearInterval(beginYear).getStart().toLocalDate();
        final LocalDate end = now.toLocalDate();
        final IntStream years = DateUtil.huntingYearsBetween(begin, end);
        return generateYearRange(now, years);
    }

    private static List<Map<String, Object>> generateYearRange(DateTime now, IntStream years) {
        return years.<Map<String, Object>>mapToObj(year -> {
            final Interval interval = DateUtil.huntingYearInterval(year);

            return ImmutableMap.of("year", year,
                    "current", interval.contains(now),
                    "text", String.format("%s-%s", interval.getStart().getYear(), interval.getEnd().getYear()));
        }).collect(toList());
    }

    private static void setLocale(String lang) {
        if (lang != null) {
            LocaleContextHolder.setLocale(new Locale(lang));
        }
    }
}

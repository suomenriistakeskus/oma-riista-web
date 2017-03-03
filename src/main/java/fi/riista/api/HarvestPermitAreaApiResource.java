package fi.riista.api;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.harvestpermit.area.HarvestPermitArea;
import fi.riista.feature.harvestpermit.area.HarvestPermitAreaDTO;
import fi.riista.feature.harvestpermit.area.HarvestPermitAreaFeature;
import fi.riista.feature.harvestpermit.area.HarvestPermitAreaPartnerDTO;
import fi.riista.feature.harvestpermit.area.HarvestPermitAreaPartnersFeature;
import fi.riista.feature.harvestpermit.area.ProcessHarvestPermitAreaZoneFeature;
import fi.riista.feature.huntingclub.area.print.AreaPrintFeature;
import fi.riista.feature.huntingclub.area.print.AreaPrintRequestDTO;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.MediaTypeExtras;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.geojson.FeatureCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/harvestpermitarea")
public class HarvestPermitAreaApiResource {

    private static final Logger LOG = LoggerFactory.getLogger(HarvestPermitAreaApiResource.class);

    @Resource
    private HarvestPermitAreaFeature harvestPermitAreaFeature;

    @Resource
    private HarvestPermitAreaPartnersFeature harvestPermitAreaPartnersFeature;

    @Resource
    private ProcessHarvestPermitAreaZoneFeature processHarvestPermitAreaZoneFeature;

    @Resource
    private AreaPrintFeature huntingClubAreaPrintFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<HarvestPermitAreaDTO> list(@RequestParam long clubId, @RequestParam(required = false) Integer year) {
        return harvestPermitAreaFeature.listByClub(clubId, year);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{id:\\d+}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public HarvestPermitAreaDTO read(@PathVariable long id) {
        return harvestPermitAreaFeature.read(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public HarvestPermitAreaDTO createArea(@RequestBody @Validated HarvestPermitAreaDTO dto) {
        return harvestPermitAreaFeature.create(dto);
    }

    @PutMapping(value = "/{id:\\d+}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public HarvestPermitAreaDTO updateArea(@PathVariable long id, @RequestBody @Validated HarvestPermitAreaDTO dto) {
        dto.setId(id);
        return harvestPermitAreaFeature.update(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{id:\\d+}/areas")
    public List<HarvestPermitAreaPartnerDTO> listPartnerAreas(@PathVariable long id, Locale locale) {
        return harvestPermitAreaPartnersFeature.list(id, locale);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "/{id:\\d+}/areas/{externalId}")
    public HarvestPermitAreaPartnerDTO addPartnerArea(@PathVariable long id, @PathVariable String externalId) {
        return harvestPermitAreaPartnersFeature.add(id, externalId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "/{id:\\d+}/areas/{partnerId:\\d+}/geometry")
    public void updatePartnerGeometry(@SuppressWarnings("unused") @PathVariable long id, @PathVariable long partnerId) {
        harvestPermitAreaPartnersFeature.updateGeometry(partnerId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CacheControl(policy = CachePolicy.NO_CACHE)
    @DeleteMapping(value = "/{id:\\d+}/areas/{partnerId:\\d+}")
    public void removePartnerArea(@PathVariable long id, @PathVariable long partnerId) {
        harvestPermitAreaPartnersFeature.remove(id, partnerId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{id:\\d+}/geometry", produces = MediaTypeExtras.APPLICATION_GEOJSON_VALUE)
    public FeatureCollection getGeometry(@PathVariable long id) {
        return harvestPermitAreaFeature.getGeometry(id);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{id:\\d+}/status", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map<String, HarvestPermitArea.StatusCode> getStatus(@PathVariable long id) {
        return ImmutableMap.of("status", harvestPermitAreaFeature.getStatus(id));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "/{id:\\d+}/ready")
    public Map<String, String> setReadyForProcessing(@PathVariable long id) {
        final boolean processImmediately = harvestPermitAreaFeature.setReadyForProcessing(id);

        if (processImmediately) {
            processHarvestPermitAreaZoneFeature.startProcessing(id);
        }

        return ImmutableMap.of("result", processImmediately ? "ok" : "pending");
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "/{id:\\d+}/incomplete")
    public void setIncomplete(@PathVariable long id) {
        harvestPermitAreaFeature.setIncomplete(id);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/huntingyears/{clubId:\\d+}", method = RequestMethod.GET)
    public List<Integer> listHuntingYears(@PathVariable long clubId) {
        return harvestPermitAreaFeature.listHuntingYears(clubId);
    }

    @RequestMapping(value = "/{id:\\d+}/print",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> print(@PathVariable final long id,
                                   @ModelAttribute @Valid final AreaPrintRequestDTO dto) {
        try {
            final Locale locale = LocaleContextHolder.getLocale();
            final FeatureCollection featureCollection = huntingClubAreaPrintFeature.exportHarvestPermitAreaFeatures(id, locale);

            final String filename = huntingClubAreaPrintFeature.getHarvestPermitAreaExportFileName(id, locale);
            final byte[] imageData = huntingClubAreaPrintFeature.printGeoJson(dto, featureCollection);
            final MediaType mediaType = MediaTypeExtras.APPLICATION_PDF;

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .contentLength(imageData.length)
                    .header(ContentDispositionUtil.HEADER_NAME, ContentDispositionUtil.encodeAttachmentFilename(filename))
                    .body(imageData);

        } catch (final Exception ex) {
            LOG.error("Permit area map export for printing has failed", ex);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Kartan tulostus epäonnistui. Yritä myöhemmin uudelleen");
        }
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/{id:\\d+}/excel/partners", method = RequestMethod.POST)
    public ModelAndView exportExcel(@PathVariable final long id, Locale locale) {
        return new ModelAndView(harvestPermitAreaPartnersFeature.listExcel(id, locale));
    }
}

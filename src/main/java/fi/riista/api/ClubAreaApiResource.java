package fi.riista.api;

import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.feature.gis.garmin.CGPSMapperAdapter;
import fi.riista.feature.gis.geojson.FeatureCollectionWithProperties;
import fi.riista.feature.huntingclub.area.HuntingClubAreaCopyFeature;
import fi.riista.feature.huntingclub.area.HuntingClubAreaCrudFeature;
import fi.riista.feature.huntingclub.area.HuntingClubAreaDTO;
import fi.riista.feature.huntingclub.area.HuntingClubAreaListFeature;
import fi.riista.feature.huntingclub.area.excel.HuntingClubAreaExcelFeature;
import fi.riista.feature.huntingclub.area.print.HuntingClubAreaPrintFeature;
import fi.riista.feature.huntingclub.area.transfer.HuntingClubAreaExportFeature;
import fi.riista.feature.huntingclub.area.transfer.HuntingClubAreaImportFeature;
import fi.riista.feature.huntingclub.area.zone.HuntingClubAreaZoneFeature;
import fi.riista.feature.huntingclub.copy.HuntingClubAreaCopyDTO;
import fi.riista.integration.mapexport.MapPdfModel;
import fi.riista.integration.mapexport.MapPdfParameters;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

@RestController
@RequestMapping(value = "/api/v1/clubarea", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ClubAreaApiResource {
    private static final Logger LOG = LoggerFactory.getLogger(ClubAreaApiResource.class);

    @Resource
    private HuntingClubAreaListFeature huntingClubAreaListFeature;

    @Resource
    private HuntingClubAreaCrudFeature huntingClubAreaCrudFeature;

    @Resource
    private HuntingClubAreaCopyFeature huntingClubAreaCopyFeature;

    @Resource
    private HuntingClubAreaZoneFeature huntingClubAreaZoneFeature;

    @Resource
    private HuntingClubAreaImportFeature huntingClubAreaImportFeature;

    @Resource
    private HuntingClubAreaExportFeature huntingClubAreaExportFeature;

    @Resource
    private HuntingClubAreaExcelFeature huntingClubAreaExcelFeature;

    @Resource
    private HuntingClubAreaPrintFeature huntingClubAreaPrintFeature;

    @Resource
    private CGPSMapperAdapter cgpsMapperAdapter;

    @Resource
    private CustomJacksonObjectMapper objectMapper;

    @GetMapping
    @CacheControl(policy = CachePolicy.NO_CACHE)
    public List<HuntingClubAreaDTO> list(@RequestParam long clubId,
                                         @RequestParam(required = false) Integer year,
                                         @RequestParam(defaultValue = "false") boolean activeOnly,
                                         @RequestParam(defaultValue = "false") boolean includeEmpty) {
        return huntingClubAreaListFeature.listByClubAndYear(clubId, year, activeOnly, includeEmpty);
    }

    @GetMapping(value = "/huntingyears")
    @CacheControl(policy = CachePolicy.NO_CACHE)
    public List<Integer> listHuntingYears(@RequestParam long clubId) {
        return huntingClubAreaListFeature.listHuntingYears(clubId);
    }

    @GetMapping(value = "/{id:\\d+}")
    @CacheControl(policy = CachePolicy.NO_CACHE)
    public HuntingClubAreaDTO read(@PathVariable Long id) {
        return huntingClubAreaCrudFeature.read(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public HuntingClubAreaDTO createArea(@RequestBody @Validated HuntingClubAreaDTO dto) {
        return huntingClubAreaCrudFeature.create(dto);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping(value = "/{id:\\d+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public HuntingClubAreaDTO updateArea(@PathVariable Long id, @RequestBody @Validated HuntingClubAreaDTO dto) {
        dto.setId(id);
        return huntingClubAreaCrudFeature.update(dto);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/{id:\\d+}/activate")
    public void activate(@PathVariable long id) {
        huntingClubAreaCrudFeature.setActiveStatus(id, true);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/{id:\\d+}/deactivate")
    public void deactivate(@PathVariable long id) {
        huntingClubAreaCrudFeature.setActiveStatus(id, false);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/{id:\\d+}/copy")
    public HuntingClubAreaDTO copyArea(@PathVariable Long id,
                                       @RequestBody @Valid HuntingClubAreaCopyDTO dto) {
        dto.setId(id);
        return huntingClubAreaCopyFeature.copy(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{id:\\d+}/features", produces = MediaTypeExtras.APPLICATION_GEOJSON_VALUE)
    public FeatureCollection geoJSON(@PathVariable long id) {
        return huntingClubAreaZoneFeature.geoJSON(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping(value = "/{id:\\d+}/features", produces = MediaTypeExtras.APPLICATION_GEOJSON_VALUE)
    public void updateGeoJSON(@PathVariable long id, @RequestBody @Valid FeatureCollection featureCollection) {
        final long zoneId = huntingClubAreaZoneFeature.updateGeoJSON(id, featureCollection);
        huntingClubAreaZoneFeature.updateAreaSize(zoneId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{id:\\d+}/combinedFeatures", produces = MediaTypeExtras.APPLICATION_GEOJSON_VALUE)
    public FeatureCollection combinedGeoJSON(@PathVariable long id) {
        return huntingClubAreaZoneFeature.combinedGeoJSON(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/{id:\\d+}/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void importGeojson(@PathVariable long id,
                              @RequestPart("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Empty upload");
        }

        huntingClubAreaImportFeature.importGeojson(id,
                toFeatureCollection(file.getInputStream(), file.getOriginalFilename()));
    }

    private FeatureCollectionWithProperties toFeatureCollection(final InputStream inputStream,
                                                                final String filename) throws IOException {
        if (filename != null && filename.toLowerCase().endsWith(".gz")) {
            return objectMapper.readValue(new GZIPInputStream(inputStream), FeatureCollectionWithProperties.class);
        }
        return objectMapper.readValue(inputStream, FeatureCollectionWithProperties.class);
    }

    @PostMapping(value = "/{id:\\d+}/zip", produces = "application/zip")
    public ResponseEntity<byte[]> exportCombinedGeojsonZip(@PathVariable final long id, final Locale locale) {
        final byte[] zipFile = huntingClubAreaExportFeature.exportCombinedGeoJsonAsArchive(id, locale);
        final String filename = "club-area-" + id + ".zip";

        return ResponseEntity
                .status(HttpStatus.OK)
                .headers(ContentDispositionUtil.header(filename))
                .body(zipFile);
    }

    @PostMapping(value = "/{id:\\d+}/garmin")
    public ResponseEntity<?> exportGarmin(@PathVariable long id) {
        final HuntingClubAreaDTO areaDTO = huntingClubAreaCrudFeature.read(id);
        final FeatureCollection featureCollection = huntingClubAreaExportFeature.exportCombinedGeoJsonForGarmin(id);
        final String filename = "omariista-" + areaDTO.getExternalId() + ".img";

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .headers(ContentDispositionUtil.header(filename))
                .body(cgpsMapperAdapter.exportToFile(featureCollection, areaDTO));
    }

    @PostMapping(value = "/{id:\\d+}/excel/{type}")
    public ModelAndView exportExcel(@PathVariable final long id, @PathVariable final String type) {
        if ("all".equals(type)) {
            return new ModelAndView(huntingClubAreaExcelFeature.exportAll(id));

        } else if ("changed".equals(type)) {
            return new ModelAndView(huntingClubAreaExcelFeature.exportChanged(id));
        }

        throw new IllegalArgumentException("Unknown excel type: " + type);
    }

    @PostMapping(value = "/{id:\\d+}/print", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> print(@PathVariable final long id,
                                   @ModelAttribute @Valid final MapPdfParameters dto) {
        try {
            final Locale locale = LocaleContextHolder.getLocale();
            final MapPdfModel model = huntingClubAreaPrintFeature.getModel(id, locale);
            final byte[] imageData = huntingClubAreaPrintFeature.renderPdf(dto, model);
            final MediaType mediaType = MediaTypeExtras.APPLICATION_PDF;

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .contentLength(imageData.length)
                    .headers(ContentDispositionUtil.header(model.getExportFileName()))
                    .body(imageData);

        } catch (final Exception ex) {
            LOG.error("Club area map export for printing has failed", ex);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Kartan tulostus epäonnistui. Yritä myöhemmin uudelleen");
        }
    }
}

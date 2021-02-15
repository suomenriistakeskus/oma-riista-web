package fi.riista.api.club;

import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.feature.gis.geojson.FeatureCollectionWithProperties;
import fi.riista.feature.huntingclub.area.HuntingClubAreaCopyFeature;
import fi.riista.feature.huntingclub.area.HuntingClubAreaCrudFeature;
import fi.riista.feature.huntingclub.area.HuntingClubAreaDTO;
import fi.riista.feature.huntingclub.area.HuntingClubAreaExcelFeature;
import fi.riista.feature.huntingclub.area.HuntingClubAreaListFeature;
import fi.riista.feature.huntingclub.area.HuntingClubAreaPrintFeature;
import fi.riista.feature.huntingclub.area.HuntingClubAreaZipFeature;
import fi.riista.feature.huntingclub.area.HuntingClubAreaZoneFeature;
import fi.riista.feature.huntingclub.copy.HuntingClubAreaCopyDTO;
import fi.riista.integration.garmin.GarminAreaExportFeature;
import fi.riista.integration.koiratutka.HuntingClubAreaImportFeature;
import fi.riista.integration.mapexport.MapPdfParameters;
import fi.riista.integration.mapexport.MapPdfRemoteService;
import fi.riista.util.MediaTypeExtras;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.geojson.FeatureCollection;
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
@RequestMapping(value = "/api/v1/clubarea")
public class ClubAreaApiResource {

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
    private HuntingClubAreaZipFeature huntingClubAreaZipFeature;

    @Resource
    private HuntingClubAreaExcelFeature huntingClubAreaExcelFeature;

    @Resource
    private HuntingClubAreaPrintFeature huntingClubAreaPrintFeature;

    @Resource
    private GarminAreaExportFeature garminAreaExportFeature;

    @Resource
    private MapPdfRemoteService mapPdfRemoteService;

    @Resource
    private CustomJacksonObjectMapper objectMapper;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @CacheControl(policy = CachePolicy.NO_CACHE)
    public List<HuntingClubAreaDTO> list(@RequestParam long clubId,
                                         @RequestParam(required = false) Integer year,
                                         @RequestParam(defaultValue = "false") boolean activeOnly,
                                         @RequestParam(defaultValue = "false") boolean includeEmpty) {
        return huntingClubAreaListFeature.listByClubAndYear(clubId, year, activeOnly, includeEmpty);
    }

    @GetMapping(value = "/huntingyears", produces = MediaType.APPLICATION_JSON_VALUE)
    @CacheControl(policy = CachePolicy.NO_CACHE)
    public List<Integer> listHuntingYears(@RequestParam long clubId) {
        return huntingClubAreaListFeature.listHuntingYears(clubId);
    }

    @GetMapping(value = "/{id:\\d+}", produces = MediaType.APPLICATION_JSON_VALUE)
    @CacheControl(policy = CachePolicy.NO_CACHE)
    public HuntingClubAreaDTO read(@PathVariable Long id) {
        return huntingClubAreaCrudFeature.read(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public HuntingClubAreaDTO createArea(@RequestBody @Validated HuntingClubAreaDTO dto) {
        return huntingClubAreaCrudFeature.create(dto);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping(value = "/{id:\\d+}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
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
    @PostMapping(value = "/{id:\\d+}/copy",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public HuntingClubAreaDTO copyArea(@PathVariable Long id,
                                       @RequestBody @Valid HuntingClubAreaCopyDTO dto) {
        dto.setId(id);
        return huntingClubAreaCopyFeature.copy(dto);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/{id:\\d+}/import-personal/{personalAreaId:\\d+}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public HuntingClubAreaDTO copyArea(@PathVariable Long id,
                                       @PathVariable Long personalAreaId) {
        return huntingClubAreaCopyFeature.importFromPersonalArea(id, personalAreaId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{id:\\d+}/features", produces = MediaTypeExtras.APPLICATION_GEOJSON_VALUE)
    public FeatureCollection geoJSON(@PathVariable long id) {
        return huntingClubAreaZoneFeature.geoJSON(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping(value = "/{id:\\d+}/features", produces = MediaTypeExtras.APPLICATION_GEOJSON_VALUE)
    public void updateGeoJSON(@PathVariable long id, @RequestBody @Valid FeatureCollection featureCollection) {
        huntingClubAreaZoneFeature.updateGeoJSON(id, featureCollection);
        huntingClubAreaZoneFeature.updateAreaSize(id);
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

    @PostMapping(value = "/{id:\\d+}/zip", produces = MediaTypeExtras.APPLICATION_ZIP_VALUE)
    public ResponseEntity<byte[]> exportZip(@PathVariable final long id, final Locale locale) throws IOException {
        return huntingClubAreaZipFeature.exportZip(id, locale);
    }

    @PostMapping(value = "/{id:\\d+}/garmin")
    public ResponseEntity<?> exportGarmin(@PathVariable long id) {
        return garminAreaExportFeature.exportClubArea(id);
    }

    @GetMapping(value = "/{id:\\d+}/excel/{type}")
    public ModelAndView exportExcel(@PathVariable final long id, @PathVariable final String type) {
        if ("all".equals(type)) {
            return new ModelAndView(huntingClubAreaExcelFeature.exportAll(id));

        } else if ("changed".equals(type)) {
            return new ModelAndView(huntingClubAreaExcelFeature.exportChanged(id));
        }

        throw new IllegalArgumentException("Unknown excel type: " + type);
    }

    @PostMapping(value = "/{id:\\d+}/print", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> print(@PathVariable final long id, final Locale locale,
                                   @ModelAttribute @Valid final MapPdfParameters dto) {
        return mapPdfRemoteService.renderPdf(dto,
                () -> huntingClubAreaPrintFeature.getModel(id, dto.getOverlay(), locale));
    }
}

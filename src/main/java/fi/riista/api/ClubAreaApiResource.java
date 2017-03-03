package fi.riista.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.riista.feature.gis.garmin.CGPSMapperAdapter;
import fi.riista.feature.gis.geojson.FeatureCollectionWithProperties;
import fi.riista.feature.huntingclub.area.HuntingClubAreaCrudFeature;
import fi.riista.feature.huntingclub.area.HuntingClubAreaDTO;
import fi.riista.feature.huntingclub.area.excel.HuntingClubAreaExcelFeature;
import fi.riista.feature.huntingclub.area.print.AreaPrintFeature;
import fi.riista.feature.huntingclub.area.print.AreaPrintRequestDTO;
import fi.riista.feature.huntingclub.area.transfer.HuntingClubAreaExportFeature;
import fi.riista.feature.huntingclub.area.transfer.HuntingClubAreaImportFeature;
import fi.riista.feature.huntingclub.area.zone.HuntingClubAreaZoneFeature;
import fi.riista.feature.huntingclub.copy.HuntingClubAreaCopyDTO;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
@RequestMapping(value = "/api/v1/club/{clubId:\\d+}/area", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ClubAreaApiResource {
    private static final Logger LOG = LoggerFactory.getLogger(ClubAreaApiResource.class);

    @Resource
    private HuntingClubAreaCrudFeature huntingClubAreaCrudFeature;

    @Resource
    private HuntingClubAreaZoneFeature huntingClubAreaZoneFeature;

    @Resource
    private HuntingClubAreaImportFeature huntingClubAreaImportFeature;

    @Resource
    private HuntingClubAreaExportFeature huntingClubAreaExportFeature;

    @Resource
    private HuntingClubAreaExcelFeature huntingClubAreaExcelFeature;

    @Resource
    private AreaPrintFeature huntingClubAreaPrintFeature;

    @Resource
    private CGPSMapperAdapter cgpsMapperAdapter;

    @Resource
    private ObjectMapper objectMapper;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(method = RequestMethod.GET)
    public List<HuntingClubAreaDTO> list(@PathVariable long clubId,
                                         @RequestParam(required = false) Integer year,
                                         @RequestParam(defaultValue = "false") boolean activeOnly) {
        return huntingClubAreaCrudFeature.listByClubAndYear(clubId, year, activeOnly);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/{id:\\d+}", method = RequestMethod.GET)
    public HuntingClubAreaDTO read(@PathVariable Long id) {
        return huntingClubAreaCrudFeature.read(id);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/huntingyears", method = RequestMethod.GET)
    public List<Integer> listHuntingYears(@PathVariable Long clubId) {
        return huntingClubAreaCrudFeature.listHuntingYears(clubId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public HuntingClubAreaDTO createArea(@PathVariable Long clubId,
                                         @RequestBody @Validated HuntingClubAreaDTO dto) {

        dto.setClubId(clubId);
        return huntingClubAreaCrudFeature.create(dto);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/{id:\\d+}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public HuntingClubAreaDTO updateArea(@PathVariable Long clubId,
                                         @PathVariable Long id,
                                         @RequestBody @Validated HuntingClubAreaDTO dto) {
        dto.setClubId(clubId);
        dto.setId(id);
        return huntingClubAreaCrudFeature.update(dto);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/{id:\\d+}/copy", method = RequestMethod.POST)
    public HuntingClubAreaDTO copyArea(@PathVariable Long id,
                                       @RequestBody @Valid HuntingClubAreaCopyDTO dto) {
        dto.setId(id);
        return huntingClubAreaCrudFeature.copy(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/{id:\\d+}/features", method = RequestMethod.GET, produces = MediaTypeExtras.APPLICATION_GEOJSON_VALUE)
    public FeatureCollection geoJSON(@PathVariable long id) {
        return huntingClubAreaZoneFeature.geoJSON(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/{id:\\d+}/features", method = RequestMethod.PUT, produces = MediaTypeExtras.APPLICATION_GEOJSON_VALUE)
    public void updateGeoJSON(@PathVariable long id, @RequestBody @Valid FeatureCollection featureCollection) {
        final long zoneId = huntingClubAreaZoneFeature.updateGeoJSON(id, featureCollection);
        huntingClubAreaZoneFeature.updateAreaSize(zoneId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/{id:\\d+}/combinedFeatures", method = RequestMethod.GET, produces = MediaTypeExtras.APPLICATION_GEOJSON_VALUE)
    public FeatureCollection combinedGeoJSON(@PathVariable long id) {
        return huntingClubAreaZoneFeature.combinedGeoJSON(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/{id:\\d+}/import", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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

    @RequestMapping(value = "/{id:\\d+}/zip", method = RequestMethod.POST, produces = "application/zip")
    public ResponseEntity<byte[]> exportCombinedGeojsonZip(@PathVariable final long id,
                                                           final Locale locale) {
        final byte[] zipFile = huntingClubAreaExportFeature.exportCombinedGeoJsonAsArchive(id, locale);
        final String filename = "club-area-" + id + ".zip";

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(ContentDispositionUtil.HEADER_NAME, ContentDispositionUtil.encodeAttachmentFilename(filename))
                .body(zipFile);
    }

    @RequestMapping(value = "/{id:\\d+}/garmin", method = RequestMethod.POST)
    public ResponseEntity<?> exportGarmin(@PathVariable long id) {
        final HuntingClubAreaDTO areaDTO = huntingClubAreaCrudFeature.read(id);
        final FeatureCollection featureCollection = huntingClubAreaExportFeature.exportCombinedGeoJsonForGarmin(id);
        final String filename = "omariista-" + areaDTO.getExternalId() + ".img";

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(ContentDispositionUtil.HEADER_NAME,
                        ContentDispositionUtil.encodeAttachmentFilename(filename))
                .body(cgpsMapperAdapter.exportToFile(featureCollection, areaDTO));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/{id:\\d+}/excel/{type}", method = RequestMethod.POST)
    public ModelAndView exportExcel(@PathVariable final long id, @PathVariable final String type) {
        if ("all".equals(type)) {
            return new ModelAndView(huntingClubAreaExcelFeature.exportAll(id));

        } else if ("changed".equals(type)) {
            return new ModelAndView(huntingClubAreaExcelFeature.exportChanged(id));
        }

        throw new IllegalArgumentException("Unknown excel type: " + type);
    }

    @RequestMapping(value = "/{id:\\d+}/print",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> print(@PathVariable final long id,
                                   @ModelAttribute @Valid final AreaPrintRequestDTO dto) {
        try {
            final Locale locale = LocaleContextHolder.getLocale();
            final FeatureCollection featureCollection = huntingClubAreaPrintFeature.exportClubAreaFeatures(id, locale);

            final String filename = huntingClubAreaPrintFeature.getClubAreaExportFileName(id, locale);
            final byte[] imageData = huntingClubAreaPrintFeature.printGeoJson(dto, featureCollection);
            final MediaType mediaType = MediaTypeExtras.APPLICATION_PDF;

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .contentLength(imageData.length)
                    .header(ContentDispositionUtil.HEADER_NAME, ContentDispositionUtil.encodeAttachmentFilename(filename))
                    .body(imageData);

        } catch (final Exception ex) {
            LOG.error("Club area map export for printing has failed", ex);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Kartan tulostus epäonnistui. Yritä myöhemmin uudelleen");
        }
    }
}

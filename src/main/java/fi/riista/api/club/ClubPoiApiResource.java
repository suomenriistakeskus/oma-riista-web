package fi.riista.api.club;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.huntingclub.poi.PoiLocationDTO;
import fi.riista.feature.huntingclub.poi.PoiLocationGroupCrudFeature;
import fi.riista.feature.huntingclub.poi.PoiLocationGroupDTO;
import fi.riista.feature.huntingclub.poi.PointOfInterestService;
import fi.riista.feature.huntingclub.poi.excel.ClubPoiExcelDTO;
import fi.riista.feature.huntingclub.poi.excel.HuntingClubPoiExcelFeature;
import fi.riista.feature.huntingclub.poi.excel.HuntingClubPoiExcelView;
import fi.riista.feature.huntingclub.poi.gpx.HuntingClubPoiGpxExportFeature;
import fi.riista.feature.huntingclub.poi.gpx.HuntingClubPoiGpxImportFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping(value = "/api/v1/club/{clubId:\\d+}/poi", produces = MediaType.APPLICATION_JSON_VALUE)
public class ClubPoiApiResource {

    private static final Logger LOG = LoggerFactory.getLogger(ClubPoiApiResource.class);

    @Resource
    private PointOfInterestService poiService;

    @Resource
    private PoiLocationGroupCrudFeature crudFeature;

    @Resource
    private HuntingClubPoiExcelFeature excelFeature;

    @Resource
    private HuntingClubPoiGpxExportFeature gpxExportFeature;

    @Resource
    private HuntingClubPoiGpxImportFeature gpxImportFeature;

    @Resource
    private EnumLocaliser localiser;


    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/editable")
    public boolean canEdit(@PathVariable final long clubId) {
        return poiService.canEdit(clubId);
    }

    // ACTUAL POIs

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/pois/list")
    public List<PoiLocationGroupDTO> listPois(@PathVariable final long clubId) {
        return crudFeature.list(clubId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/pois/get/{poiId:\\d+}")
    public PoiLocationGroupDTO get(@PathVariable final long poiId) {
        return crudFeature.read(poiId);
    }

    @PutMapping(value = "/pois/update")
    public PoiLocationGroupDTO update(@Nonnull @RequestBody @Valid final PoiLocationGroupDTO dto) {
        return crudFeature.update(dto);
    }

    @PostMapping(value = "/pois/create")
    public PoiLocationGroupDTO create(@PathVariable final long clubId,
                                      @Nonnull @RequestBody @Valid final PoiLocationGroupDTO dto) {
        dto.setClubId(clubId);
        return crudFeature.create(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/pois/delete/{poiId:\\d+}")
    public void delete(@PathVariable final long poiId) {
        crudFeature.delete(poiId);
    }


    // EXCEL

    @PostMapping("/excel")
    public ModelAndView exportExcelByClub(@PathVariable final long clubId) {
        final ClubPoiExcelDTO poiExcelDTO = excelFeature.exportExcel(clubId);
        final HuntingClubPoiExcelView view = new HuntingClubPoiExcelView(poiExcelDTO, localiser);

        return new ModelAndView(view);
    }

    @PostMapping("/excel/{areaId:\\d+}")
    public ModelAndView exportExcelByArea(@PathVariable final long areaId) {
        final ClubPoiExcelDTO poiExcelDTO = excelFeature.exportExcelByArea(areaId);
        final HuntingClubPoiExcelView view = new HuntingClubPoiExcelView(poiExcelDTO, localiser);

        return new ModelAndView(view);
    }

    // GPX

    @PostMapping("/gpx")
    public void exportGpxByClub(@PathVariable final long clubId,
                                final HttpServletResponse response,
                                final Locale locale) throws IOException {
        response.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);

        gpxExportFeature.exportByClub(clubId, response, locale);
    }

    @PostMapping("/gpx/{areaId:\\d+}")
    public void exportGpxByArea(@PathVariable final long areaId,
                                final HttpServletResponse response,
                                final Locale locale) throws IOException {
        response.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);

        gpxExportFeature.exportByArea(areaId, response, locale);
    }

    @PostMapping(value = "/gpx/convert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<PoiLocationDTO> convertGpxFile(@RequestPart("file") MultipartFile file) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Empty file uploaded");
        }

        return gpxImportFeature.convertGpxPoints(file);
    }

    // Return some insight about the error to the user. Someone might consider this as poor security.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentErrors(Exception ex) {
        LOG.error("POI illegal argument: ", ex);
        return ResponseEntity.internalServerError().body(ImmutableMap.of("status", "ERROR", "message", ex.getMessage()));
    }
}

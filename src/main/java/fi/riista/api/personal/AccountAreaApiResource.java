package fi.riista.api.personal;

import fi.riista.feature.account.area.PersonalAreaCopyFeature;
import fi.riista.feature.account.area.PersonalAreaCrudFeature;
import fi.riista.feature.account.area.PersonalAreaDTO;
import fi.riista.feature.account.area.PersonalAreaExcelFeature;
import fi.riista.feature.account.area.PersonalAreaListFeature;
import fi.riista.feature.account.area.PersonalAreaPrintFeature;
import fi.riista.feature.account.area.PersonalAreaZipFeature;
import fi.riista.feature.account.area.PersonalAreaZoneFeature;
import fi.riista.integration.garmin.GarminAreaExportFeature;
import fi.riista.integration.mapexport.MapPdfParameters;
import fi.riista.integration.mapexport.MapPdfRemoteService;
import fi.riista.util.MediaTypeExtras;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.geojson.FeatureCollection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping(value = AccountAreaApiResource.ACCOUNT_RESOURCE_URL)
public class AccountAreaApiResource {
    static final String ACCOUNT_RESOURCE_URL = "/api/v1/account/area";

    @Resource
    private PersonalAreaListFeature personalAreaListFeature;

    @Resource
    private PersonalAreaCrudFeature personalAreaCrudFeature;

    @Resource
    private PersonalAreaCopyFeature personalAreaCopyFeature;

    @Resource
    private PersonalAreaZoneFeature personalAreaZoneFeature;

    @Resource
    private PersonalAreaExcelFeature personalAreaExcelFeature;

    @Resource
    private PersonalAreaPrintFeature personalAreaPrintFeature;

    @Resource
    private PersonalAreaZipFeature personalAreaZipFeature;

    @Resource
    private GarminAreaExportFeature garminAreaExportFeature;

    @Resource
    private MapPdfRemoteService mapPdfRemoteService;

    @GetMapping(value = "/page/me", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @CacheControl(policy = CachePolicy.NO_CACHE)
    public Slice<PersonalAreaDTO> listMinePaged(@RequestParam int page,
                                                @RequestParam int size) {
        return personalAreaListFeature.listMinePaged(new PageRequest(page, size, Sort.Direction.DESC, "id"));
    }

    @GetMapping(value = "/page/{personId:\\d+}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @CacheControl(policy = CachePolicy.NO_CACHE)
    public Slice<PersonalAreaDTO> listForPersonPaged(@PathVariable long personId,
                                                     @RequestParam int page,
                                                     @RequestParam int size) {
        return personalAreaListFeature.listForPersonPaged(
                personId,
                new PageRequest(page, size, Sort.Direction.DESC, "id"));
    }

    @GetMapping(value = "/list/me", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @CacheControl(policy = CachePolicy.NO_CACHE)
    public List<PersonalAreaDTO> listMine() {
        return personalAreaListFeature.listMine();
    }


    @GetMapping(value = "/list/{personId:\\d+}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @CacheControl(policy = CachePolicy.NO_CACHE)
    public List<PersonalAreaDTO> listForPerson(@PathVariable long personId) {
        return personalAreaListFeature.listForPerson(personId);
    }

    @GetMapping(value = "/{id:\\d+}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @CacheControl(policy = CachePolicy.NO_CACHE)
    public PersonalAreaDTO read(@PathVariable Long id) {
        return personalAreaCrudFeature.read(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PersonalAreaDTO createArea(@RequestBody @Validated PersonalAreaDTO dto) {
        return personalAreaCrudFeature.create(dto);
    }

    @PutMapping(value = "/{id:\\d+}",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PersonalAreaDTO updateArea(@PathVariable Long id, @RequestBody @Validated PersonalAreaDTO dto) {
        dto.setId(id);
        return personalAreaCrudFeature.update(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id:\\d+}")
    public void deleteArea(@PathVariable Long id) {
        personalAreaCrudFeature.delete(id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{id:\\d+}/copy",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void copyArea(@PathVariable long id, final Locale locale) {
        personalAreaCopyFeature.copy(id, locale);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{id:\\d+}/features", produces = MediaTypeExtras.APPLICATION_GEOJSON_VALUE)
    public FeatureCollection geoJSON(@PathVariable long id) {
        return personalAreaZoneFeature.geoJSON(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping(value = "/{id:\\d+}/features", produces = MediaTypeExtras.APPLICATION_GEOJSON_VALUE)
    public void updateGeoJSON(@PathVariable long id, @RequestBody @Valid FeatureCollection featureCollection) {
        final long zoneId = personalAreaZoneFeature.updateGeoJSON(id, featureCollection);
        personalAreaZoneFeature.updateAreaSize(zoneId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{id:\\d+}/combinedFeatures", produces = MediaTypeExtras.APPLICATION_GEOJSON_VALUE)
    public FeatureCollection combinedGeoJSON(@PathVariable long id) {
        return personalAreaZoneFeature.combinedGeoJSON(id);
    }

    @PostMapping(value = "/{id:\\d+}/zip", produces = MediaTypeExtras.APPLICATION_ZIP_VALUE)
    public ResponseEntity<byte[]> exportZip(@PathVariable final long id, final Locale locale) throws IOException {
        return personalAreaZipFeature.exportZip(id, locale);
    }

    @PostMapping(value = "/{id:\\d+}/garmin")
    public ResponseEntity<?> exportGarmin(@PathVariable long id) {
        return garminAreaExportFeature.exportPersonalArea(id);
    }

    @GetMapping(value = "/{id:\\d+}/excel/{type}")
    public ModelAndView exportExcel(@PathVariable final long id, @PathVariable final String type) {
        if ("all".equals(type)) {
            return new ModelAndView(personalAreaExcelFeature.exportAll(id));

        } else if ("changed".equals(type)) {
            return new ModelAndView(personalAreaExcelFeature.exportChanged(id));
        }

        throw new IllegalArgumentException("Unknown excel type: " + type);
    }

    @PostMapping(value = "/{id:\\d+}/print", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> print(@PathVariable final long id, final Locale locale,
                                   @ModelAttribute @Valid final MapPdfParameters dto) {
        return mapPdfRemoteService.renderPdf(dto, () -> personalAreaPrintFeature.getModel(id, dto.getOverlay(),
                locale));
    }

}

package fi.riista.api.personal;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameDiaryEntryDTO;
import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.gamediary.GameDiaryFeature;
import fi.riista.feature.gamediary.GameDiaryImageFeature;
import fi.riista.feature.gamediary.GameDiaryMetadataFeature;
import fi.riista.feature.gamediary.GameDiaryParametersDTO;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.gamediary.excel.GameDiaryExcelFeature;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.HarvestExceptionMapper;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFieldsQuery;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFieldsQueryResponse;
import fi.riista.feature.gamediary.observation.ObservationDTO;
import fi.riista.feature.gamediary.observation.metadata.GameSpeciesObservationMetadataDTO;
import fi.riista.feature.gamediary.observation.metadata.ObservationMetadataDTO;
import fi.riista.feature.gamediary.search.GameDiarySearchDTO;
import fi.riista.feature.gamediary.search.GameDiarySearchFeature;
import fi.riista.util.MediaTypeExtras;
import fi.riista.util.Patterns;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.joda.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/gamediary", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class GameDiaryApiResource {

    @Resource
    private GameDiaryFeature diaryFeature;

    @Resource
    private GameDiarySearchFeature gameDiarySearchFeature;

    @Resource
    private GameDiaryImageFeature gameDiaryImageFeature;

    @Resource
    private GameDiaryMetadataFeature gameDiaryMetadataFeature;

    @Resource
    private GameDiaryExcelFeature excelFeature;

    @Resource
    private HarvestExceptionMapper harvestExceptionMapper;

    @PostMapping
    public List<GameDiaryEntryDTO> diaryEntries(@Valid @RequestBody GameDiarySearchDTO search) {
        return gameDiarySearchFeature.listDiaryEntriesForActiveUser(search);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/parameters")
    public GameDiaryParametersDTO getGameDiaryParameters() {
        return gameDiaryMetadataFeature.getGameDiaryParameters();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/species/withinMooseHunting")
    public List<GameSpeciesDTO> getGameSpeciesRegistrableAsObservationsWithinMooseHunting() {
        return gameDiaryMetadataFeature.getGameSpeciesRegistrableAsObservationsWithinMooseHunting();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/observation/metadata")
    public ObservationMetadataDTO getObservationMetadata() {
        return gameDiaryMetadataFeature.getObservationFieldMetadata();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/observation/metadata/{gameSpeciesCode:\\d+}")
    public GameSpeciesObservationMetadataDTO getObservationMetadata(@PathVariable int gameSpeciesCode) {
        return gameDiaryMetadataFeature.getObservationFieldMetadataForSpecies(gameSpeciesCode);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/harvest/fields")
    public RequiredHarvestFieldsQueryResponse getHarvestFields(@RequestParam int gameSpeciesCode,
                                                               @RequestParam boolean withPermit,
                                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate harvestDate,
                                                               @RequestParam int longitude,
                                                               @RequestParam int latitude) {
        return gameDiaryMetadataFeature.getHarvestFields(new RequiredHarvestFieldsQuery(
                gameSpeciesCode, harvestDate, new GeoLocation(latitude, longitude), withPermit));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/harvest/fields/{id:\\d+}")
    public RequiredHarvestFieldsQueryResponse getHarvestFields(@PathVariable Long id) {
        return gameDiaryMetadataFeature.getHarvestFields(id);
    }

    @PostMapping(value = "/harvest", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createHarvest(@RequestBody @Valid HarvestDTO dto) {
        try {
            return ResponseEntity.ok(diaryFeature.createHarvest(dto));
        } catch (RuntimeException e) {
            return harvestExceptionMapper.handleException(e);
        }
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/harvest/{id:\\d+}")
    public HarvestDTO getHarvest(@PathVariable Long id) {
        return diaryFeature.getHarvest(id);
    }

    @PutMapping(
            value = "/harvest/{id:\\d+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateHarvest(@RequestBody @Valid HarvestDTO dto) {
        try {
            return ResponseEntity.ok(diaryFeature.updateHarvest(dto));
        } catch (RuntimeException e) {
            return harvestExceptionMapper.handleException(e);
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/harvest/{id:\\d+}")
    public void deleteHarvest(@PathVariable Long id) {
        diaryFeature.deleteHarvest(id);
    }

    @PostMapping(value = "/observation", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ObservationDTO createObservation(@RequestBody @Valid ObservationDTO dto) {
        return diaryFeature.createObservation(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/observation/{id:\\d+}")
    public ObservationDTO getObservation(@PathVariable Long id) {
        return diaryFeature.getObservation(id);
    }

    @PutMapping(
            value = "/observation/{id:\\d+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ObservationDTO updateObservation(@RequestBody @Valid ObservationDTO dto) {
        return diaryFeature.updateObservation(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/observation/{id:\\d+}")
    public void deleteObservation(@PathVariable Long id) {
        diaryFeature.deleteObservation(id);
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 31536000)
    @GetMapping("/image/{imageId:" + Patterns.UUID + "}")
    public ResponseEntity<?> getGameDiaryImage(@PathVariable UUID imageId) throws IOException {
        return gameDiaryImageFeature.getGameDiaryImageBytes(imageId, false);
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 31536000)
    @GetMapping("/image/{imageId:" + Patterns.UUID + "}/resize/{width:\\d{1,4}}x{height:\\d{1,4}}x{keepProportions:\\d{1}}")
    public ResponseEntity<?> getGameDiaryImageResized(@PathVariable UUID imageId,
                                                      @PathVariable int width,
                                                      @PathVariable int height,
                                                      @PathVariable boolean keepProportions) throws IOException {

        return gameDiaryImageFeature.getGameDiaryImageBytesResized(imageId, width, height, keepProportions);
    }

    @PostMapping(value = "/image/uploadForHarvest",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String addGameDiaryImageForHarvest(
            @RequestParam("gameDiaryEntryId") long harvestId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "replace", required = false) UUID replacedUuid)
            throws IOException {

        return addGameDiaryImageForDiaryEntry(harvestId, GameDiaryEntryType.HARVEST, file, replacedUuid);
    }

    @PostMapping(value = "/image/uploadForObservation",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String addGameDiaryImageForObservation(
            @RequestParam("gameDiaryEntryId") long observationId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "replace", required = false) UUID replacedUuid)
            throws IOException {

        return addGameDiaryImageForDiaryEntry(observationId, GameDiaryEntryType.OBSERVATION, file, replacedUuid);
    }

    private String addGameDiaryImageForDiaryEntry(final long diaryEntryId,
                                                  final GameDiaryEntryType diaryEntryType,
                                                  final MultipartFile file,
                                                  final UUID replacedUuid) throws IOException {

        final UUID newUuid = UUID.randomUUID();

        if (replacedUuid != null) {
            gameDiaryImageFeature.replaceImageForDiaryEntry(diaryEntryId, diaryEntryType, replacedUuid, newUuid, file);
        } else {
            gameDiaryImageFeature.addGameDiaryImageForDiaryEntry(diaryEntryId, diaryEntryType, newUuid, file);
        }

        return newUuid.toString();
    }

    @PostMapping(value = "/image/uploadtmp",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String addTemporaryGameDiaryImage(@RequestParam("file") MultipartFile file) throws IOException {
        final UUID uuid = UUID.randomUUID();
        gameDiaryImageFeature.addGameDiaryImageWithoutDiaryEntryAssociation(uuid, file);
        return uuid.toString();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "/excel", produces = MediaTypeExtras.APPLICATION_EXCEL_VALUE)
    public ModelAndView excel() {
        return new ModelAndView(excelFeature.export());
    }
}

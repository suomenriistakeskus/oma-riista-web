package fi.riista.api.personal;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameDiaryEntryDTO;
import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.gamediary.GameDiaryImageFeature;
import fi.riista.feature.gamediary.GameDiaryMetadataFeature;
import fi.riista.feature.gamediary.GameDiaryParametersDTO;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.gamediary.excel.GameDiaryExcelFeature;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.HarvestExceptionMapper;
import fi.riista.feature.gamediary.harvest.HarvestFeature;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFieldsRequestDTO;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFieldsResponseDTO;
import fi.riista.feature.gamediary.observation.ObservationDTO;
import fi.riista.feature.gamediary.observation.ObservationFeature;
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
@RequestMapping(value = "/api/v1/gamediary", produces = MediaType.APPLICATION_JSON_VALUE)
public class GameDiaryApiResource {

    @Resource
    private HarvestFeature harvestFeature;

    @Resource
    private ObservationFeature observationFeature;

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
    public List<GameDiaryEntryDTO> diaryEntries(@Valid @RequestBody final GameDiarySearchDTO search) {
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
    public GameSpeciesObservationMetadataDTO getObservationMetadata(@PathVariable final int gameSpeciesCode) {
        return gameDiaryMetadataFeature.getObservationFieldMetadataForSpecies(gameSpeciesCode);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/harvest/fields")
    public RequiredHarvestFieldsResponseDTO getRequiredHarvestFields(
            @RequestParam final int gameSpeciesCode,
            @RequestParam final boolean withPermit,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate harvestDate,
            @RequestParam final int longitude,
            @RequestParam final int latitude,
            @RequestParam(required = false) final Long personId) {

        return gameDiaryMetadataFeature.getRequiredHarvestFields(
                new RequiredHarvestFieldsRequestDTO(
                        gameSpeciesCode,
                        harvestDate,
                        new GeoLocation(latitude, longitude),
                        withPermit,
                        personId),
                HarvestSpecVersion.CURRENTLY_SUPPORTED);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/harvest/fields/{harvestId:\\d+}")
    public RequiredHarvestFieldsResponseDTO getRequiredHarvestFields(@PathVariable final long harvestId) {
        return gameDiaryMetadataFeature.getRequiredFieldsForHarvest(harvestId, HarvestSpecVersion.CURRENTLY_SUPPORTED);
    }

    @PostMapping(value = "/harvest", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createHarvest(@RequestBody @Valid final HarvestDTO dto) {
        try {
            return ResponseEntity.ok(harvestFeature.createHarvest(dto));
        } catch (RuntimeException e) {
            return harvestExceptionMapper.handleException(e);
        }
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/harvest/{id:\\d+}")
    public HarvestDTO getHarvest(@PathVariable final long id) {
        return harvestFeature.getHarvest(id);
    }

    @PutMapping(value = "/harvest/{id:\\d+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateHarvest(@RequestBody @Valid final HarvestDTO dto) {
        try {
            return ResponseEntity.ok(harvestFeature.updateHarvest(dto));
        } catch (RuntimeException e) {
            return harvestExceptionMapper.handleException(e);
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/harvest/{id:\\d+}")
    public void deleteHarvest(@PathVariable final long id) {
        harvestFeature.deleteHarvest(id);
    }

    @PostMapping(value = "/observation", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ObservationDTO createObservation(@RequestBody @Valid final ObservationDTO dto) {
        return observationFeature.createObservation(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/observation/{id:\\d+}")
    public ObservationDTO getObservation(@PathVariable final long id) {
        return observationFeature.getObservation(id);
    }

    @PutMapping(value = "/observation/{id:\\d+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ObservationDTO updateObservation(@RequestBody @Valid final ObservationDTO dto) {
        return observationFeature.updateObservation(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/observation/{id:\\d+}")
    public void deleteObservation(@PathVariable final long id) {
        observationFeature.deleteObservation(id);
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 31536000)
    @GetMapping("/image/{imageId:" + Patterns.UUID + "}")
    public ResponseEntity<?> getGameDiaryImage(@PathVariable final UUID imageId) throws IOException {
        return gameDiaryImageFeature.getGameDiaryImageBytes(imageId, false);
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 31536000)
    @GetMapping("/image/{imageId:" + Patterns.UUID + "}/resize/{width:\\d{1,4}}x{height:\\d{1,4}}x{keepProportions:\\d{1}}")
    public ResponseEntity<?> getGameDiaryImageResized(@PathVariable final UUID imageId,
                                                      @PathVariable final int width,
                                                      @PathVariable final int height,
                                                      @PathVariable final boolean keepProportions) throws IOException {

        return gameDiaryImageFeature.getGameDiaryImageBytesResized(imageId, width, height, keepProportions);
    }

    @PostMapping(value = "/image/uploadForHarvest",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String addGameDiaryImageForHarvest(
            @RequestParam("gameDiaryEntryId") final long harvestId,
            @RequestParam("file") final MultipartFile file,
            @RequestParam(value = "replace", required = false) final UUID replacedUuid)
            throws IOException {

        return addGameDiaryImageForDiaryEntry(harvestId, GameDiaryEntryType.HARVEST, file, replacedUuid);
    }

    @PostMapping(value = "/image/uploadForObservation",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String addGameDiaryImageForObservation(
            @RequestParam("gameDiaryEntryId") final long observationId,
            @RequestParam("file") final MultipartFile file,
            @RequestParam(value = "replace", required = false) final UUID replacedUuid)
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
    public String addTemporaryGameDiaryImage(@RequestParam("file") final MultipartFile file) throws IOException {
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

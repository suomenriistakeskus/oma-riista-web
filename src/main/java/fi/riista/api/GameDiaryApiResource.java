package fi.riista.api;

import fi.riista.feature.gamediary.todo.GameDiaryTodoFeature;
import fi.riista.feature.gamediary.excel.GameDiaryExcelFeature;
import fi.riista.feature.gamediary.GameDiaryFeature;
import fi.riista.feature.gamediary.GameDiaryEntryDTO;
import fi.riista.feature.gamediary.GameDiaryParametersDTO;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.gamediary.observation.metadata.GameSpeciesObservationMetadataDTO;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.observation.ObservationDTO;
import fi.riista.feature.gamediary.observation.metadata.ObservationMetadataDTO;
import fi.riista.feature.gamediary.PersonRelationshipToGameDiaryEntryDTO;
import fi.riista.feature.organization.person.PersonWithHunterNumberDTO;
import fi.riista.feature.gamediary.todo.GameDiaryTodoHarvestDTO;
import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.harvestpermit.report.search.HarvestReportPersonSearch;
import fi.riista.feature.gamediary.srva.SrvaCrudFeature;
import fi.riista.util.DateUtil;
import fi.riista.util.Patterns;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.apache.commons.lang.BooleanUtils;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
    private GameDiaryExcelFeature excelFeature;

    @Resource
    private GameDiaryTodoFeature gameDiaryTodoFeature;

    @Resource
    private SrvaCrudFeature srvaCrudFeature;

    @Resource
    private HarvestReportPersonSearch harvestReportPersonSearch;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/parameters", method = RequestMethod.GET)
    public GameDiaryParametersDTO getGameDiaryParameters() {
        return new GameDiaryParametersDTO(diaryFeature.getGameCategories(), diaryFeature.getGameSpecies());
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/species/withinMooseHunting", method = RequestMethod.GET)
    public List<GameSpeciesDTO> getGameSpeciesRegistrableAsObservationsWithinMooseHunting() {
        return diaryFeature.getGameSpeciesRegistrableAsObservationsWithinMooseHunting();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/observation/metadata", method = RequestMethod.GET)
    public ObservationMetadataDTO getObservationMetadata() {
        return diaryFeature.getObservationFieldMetadata();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/observation/metadata/{gameSpeciesCode:\\d+}", method = RequestMethod.GET)
    public GameSpeciesObservationMetadataDTO getObservationMetadata(@PathVariable int gameSpeciesCode) {
        return diaryFeature.getObservationFieldMetadataForSpecies(gameSpeciesCode);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/observation/{id:\\d+}/relationship", method = RequestMethod.GET)
    public PersonRelationshipToGameDiaryEntryDTO getRelationshipToObservation(@PathVariable final long id) {
        return diaryFeature.getRelationshipToGameDiaryEntry(GameDiaryEntryType.OBSERVATION, id);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/harvest/{id:\\d+}/relationship", method = RequestMethod.GET)
    public PersonRelationshipToGameDiaryEntryDTO getRelationshipToHarvest(@PathVariable final long id) {
        return diaryFeature.getRelationshipToGameDiaryEntry(GameDiaryEntryType.HARVEST, id);
    }

    @RequestMapping(value = "/harvest", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public HarvestDTO createHarvest(@RequestBody @Valid HarvestDTO dto) {
        return diaryFeature.createHarvest(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/harvest/{id:\\d+}", method = RequestMethod.GET)
    public HarvestDTO getHarvest(@PathVariable Long id) {
        return diaryFeature.getHarvest(id);
    }

    @RequestMapping(
            value = "/harvest/{id:\\d+}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public HarvestDTO updateHarvest(@RequestBody @Valid HarvestDTO dto) {
        return diaryFeature.updateHarvest(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/harvest/{id:\\d+}", method = RequestMethod.DELETE)
    public void deleteHarvest(@PathVariable Long id) {
        diaryFeature.deleteHarvest(id);
    }

    @RequestMapping(value = "/observation", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ObservationDTO createObservation(@RequestBody @Valid ObservationDTO dto) {
        return diaryFeature.createObservation(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/observation/{id:\\d+}", method = RequestMethod.GET)
    public ObservationDTO getObservation(@PathVariable Long id) {
        return diaryFeature.getObservation(id);
    }

    @RequestMapping(
            value = "/observation/{id:\\d+}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ObservationDTO updateObservation(@RequestBody @Valid ObservationDTO dto) {
        return diaryFeature.updateObservation(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/observation/{id:\\d+}", method = RequestMethod.DELETE)
    public void deleteObservation(@PathVariable Long id) {
        diaryFeature.deleteObservation(id);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(method = RequestMethod.GET)
    public List<GameDiaryEntryDTO> diaryEntries(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate beginDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate endDate,
            @RequestParam(required = false) final Boolean reportedForOthers,
            @RequestParam(required = false) final Boolean srvaEvents) {

        final Interval interval = beginDate.isAfter(endDate)
                ? DateUtil.createDateInterval(endDate, beginDate)
                : DateUtil.createDateInterval(beginDate, endDate);

        final List<GameDiaryEntryDTO> dtos = diaryFeature.listDiaryEntriesForActiveUser(
                interval, BooleanUtils.isTrue(reportedForOthers));

        if (BooleanUtils.isTrue(srvaEvents)) {
            dtos.addAll(srvaCrudFeature.listSrvaEventsForActiveUser(interval));
        }

        return dtos;
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/todo", method = RequestMethod.GET)
    public GameDiaryTodoHarvestDTO todo(@RequestParam(required = false) Long personId) {
        return gameDiaryTodoFeature.listAllHarvestsRequiringAction(personId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/accepted/permit/{permitId:\\d+}", method = RequestMethod.GET)
    public List<HarvestDTO> listHarvestsAcceptedToPermit(@PathVariable Long permitId) {
        return diaryFeature.listHarvestsAcceptedToPermit(permitId);
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 31536000)
    @RequestMapping(value = "/image/{imageId:" + Patterns.UUID + "}", method = RequestMethod.GET)
    public ResponseEntity<?> getGameDiaryImage(@PathVariable UUID imageId) throws IOException {
        return diaryFeature.getGameDiaryImageBytes(imageId, false);
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 31536000)
    @RequestMapping(
            value = "/image/{imageId:" + Patterns.UUID + "}/resize/{width:\\d{1,4}}x{height:\\d{1,4}}x{keepProportions:\\d{1}}",
            method = RequestMethod.GET)
    public ResponseEntity<?> getGameDiaryImageResized(
            @PathVariable UUID imageId,
            @PathVariable int width,
            @PathVariable int height,
            @PathVariable boolean keepProportions) throws IOException {

        return diaryFeature.getGameDiaryImageBytesResized(imageId, width, height, keepProportions);
    }

    @RequestMapping(value = "/image/uploadForHarvest", method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String addGameDiaryImageForHarvest(
            @RequestParam("gameDiaryEntryId") long harvestId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "replace", required = false) UUID replacedUuid)
            throws IOException {

        return addGameDiaryImageForDiaryEntry(harvestId, GameDiaryEntryType.HARVEST, file, replacedUuid);
    }

    @RequestMapping(value = "/image/uploadForObservation", method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String addGameDiaryImageForObservation(
            @RequestParam("gameDiaryEntryId") long observationId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "replace", required = false) UUID replacedUuid)
            throws IOException {

        return addGameDiaryImageForDiaryEntry(observationId, GameDiaryEntryType.OBSERVATION, file, replacedUuid);
    }

    private String addGameDiaryImageForDiaryEntry(
            final long diaryEntryId,
            final GameDiaryEntryType diaryEntryType,
            final MultipartFile file,
            final UUID replacedUuid)
            throws IOException {

        final UUID newUuid = UUID.randomUUID();

        if (replacedUuid != null) {
            diaryFeature.replaceImageForDiaryEntry(diaryEntryId, diaryEntryType, replacedUuid, newUuid, file);
        } else {
            diaryFeature.addGameDiaryImageForDiaryEntry(diaryEntryId, diaryEntryType, newUuid, file);
        }

        return newUuid.toString();
    }

    @RequestMapping(value = "/image/uploadtmp", method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String addTemporaryGameDiaryImage(@RequestParam("file") MultipartFile file) throws IOException {
        final UUID uuid = UUID.randomUUID();
        diaryFeature.addGameDiaryImageWithoutDiaryEntryAssociation(uuid, file);
        return uuid.toString();
    }

    @RequestMapping(value = "/checkHunterNumber", method = RequestMethod.POST)
    public PersonWithHunterNumberDTO checkHunterNumber(@RequestParam("hunterNumber") String hunterNumber) {
        return harvestReportPersonSearch.findHunterByNumber(hunterNumber);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/excel", method = RequestMethod.POST)
    public ModelAndView excel() {
        return new ModelAndView(excelFeature.export());
    }

}

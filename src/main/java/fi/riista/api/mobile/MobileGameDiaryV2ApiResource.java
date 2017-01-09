package fi.riista.api.mobile;

import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.mobile.MobileGameDiaryV2Feature;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.mobile.MobileGameSpeciesCodesetDTO;
import fi.riista.feature.gamediary.mobile.MobileAccountV2DTO;
import fi.riista.feature.gamediary.mobile.MobileHarvestDTO;
import fi.riista.feature.gamediary.mobile.MobileHarvestPermitExistsDTO;
import fi.riista.feature.gamediary.mobile.MobileObservationDTO;
import fi.riista.feature.gamediary.observation.metadata.ObservationMetadataDTO;
import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.util.Patterns;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
public class MobileGameDiaryV2ApiResource {

    private static final Logger LOG = LoggerFactory.getLogger(MobileGameDiaryV2ApiResource.class);

    private static final String URL_PREFIX = "/api/mobile/v2/gamediary";

    public static final String ACCOUNT_RESOURCE_URL = URL_PREFIX + "/account";
    public static final String SPECIES_RESOURCE_URL = URL_PREFIX + "/gamespeciescodeset";
    public static final String OBSERVATION_METADATA_RESOURCE_URL =
            URL_PREFIX + "/observation/metadata/{observationSpecVersion:\\d+}";

    public static final String HARVEST_LIST_RESOURCE_URL =
            URL_PREFIX + "/harvests/{firstCalendarYearOfHuntingYear:\\d+}";
    public static final String HARVEST_CHANGES_RESOURCE_URL =
            URL_PREFIX + "/harvests/haschanges/{firstCalendarYearOfHuntingYear:\\d+}/{since:" + Patterns.DATETIME_ISO_8601 + "}";
    public static final String CREATE_HARVEST_RESOURCE_URL = URL_PREFIX + "/harvest";
    public static final String UPDATE_HARVEST_RESOURCE_URL = URL_PREFIX + "/harvest/{id:\\d+}";
    public static final String DELETE_HARVEST_RESOURCE_URL = URL_PREFIX + "/harvest/{id:\\d+}";

    public static final String OBSERVATION_LIST_RESOURCE_URL =
            URL_PREFIX + "/observations/{firstCalendarYearOfHuntingYear:\\d+}";
    public static final String OBSERVATION_CHANGES_RESOURCE_URL =
            URL_PREFIX + "/observation/haschanges/{firstCalendarYearOfHuntingYear:\\d+}/{since:" + Patterns.DATETIME_ISO_8601 + "}";
    public static final String CREATE_OBSERVATION_RESOURCE_URL = URL_PREFIX + "/observation";
    public static final String UPDATE_OBSERVATION_RESOURCE_URL = URL_PREFIX + "/observation/{id:\\d+}";
    public static final String DELETE_OBSERVATION_RESOURCE_URL = URL_PREFIX + "/observation/{id:\\d+}";

    public static final String IMAGE_RESOURCE_URL = URL_PREFIX + "/image/{imageUuid:" + Patterns.UUID + "}";
    public static final String IMAGE_RESOURCE_RESIZED_URL =
            URL_PREFIX + "/image/{imageUuid:" + Patterns.UUID + "}/resize/{width:\\d{1,4}}x{height:\\d{1,4}}x{keepProportions:\\d{1}}";
    public static final String HARVEST_IMAGE_UPLOAD_RESOURCE_URL = URL_PREFIX + "/image/uploadforharvest";
    public static final String OBSERVATION_IMAGE_UPLOAD_RESOURCE_URL = URL_PREFIX + "/image/uploadforobservation";

    public static final String CHECK_PERMIT_NUMBER = URL_PREFIX + "/checkPermitNumber";
    public static final String PRELOAD_PERMITS = URL_PREFIX + "/preloadPermits";

    @Resource
    private MobileGameDiaryV2Feature feature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = SPECIES_RESOURCE_URL, method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public MobileGameSpeciesCodesetDTO getGameSpecies() {
        return new MobileGameSpeciesCodesetDTO(feature.getGameCategories(), feature.getGameSpecies());
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = ACCOUNT_RESOURCE_URL, method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public MobileAccountV2DTO getAccount() {
        return feature.getMobileAccount();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = OBSERVATION_METADATA_RESOURCE_URL, method = RequestMethod.GET)
    public ObservationMetadataDTO getObservationMetadata(
            @PathVariable final int observationSpecVersion) {

        return feature.getMobileObservationFieldMetadata(ObservationSpecVersion.fromIntValue(observationSpecVersion));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = HARVEST_LIST_RESOURCE_URL, method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<MobileHarvestDTO> getHarvests(
            @PathVariable final int firstCalendarYearOfHuntingYear, @RequestParam final int harvestSpecVersion) {

        return feature.getHarvests(firstCalendarYearOfHuntingYear, HarvestSpecVersion.fromIntValue(harvestSpecVersion));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = HARVEST_CHANGES_RESOURCE_URL, method = RequestMethod.GET)
    public String checkHarvestsAreUpdated(
            @PathVariable final Integer firstCalendarYearOfHuntingYear,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") final LocalDateTime since) {

        return "true";
    }

    @RequestMapping(
            value = CREATE_HARVEST_RESOURCE_URL,
            method = {RequestMethod.POST, RequestMethod.PUT},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public MobileHarvestDTO createHarvest(@RequestBody @Valid final MobileHarvestDTO dto) {
        // on create force id and rev to be null
        dto.setId(null);
        dto.setRev(null);
        return feature.createHarvest(dto);
    }

    @RequestMapping(value = UPDATE_HARVEST_RESOURCE_URL, method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public MobileHarvestDTO updateHarvest(
            @PathVariable final Long id, @RequestBody @Validated final MobileHarvestDTO dto) {

        dto.setId(id);
        return feature.updateHarvest(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = DELETE_HARVEST_RESOURCE_URL, method = RequestMethod.DELETE)
    public void deleteHarvest(@PathVariable final Long id) {
        try {
            feature.deleteHarvest(id);
        } catch (Exception e) {
            LOG.warn("Deletion failed for harvest id " + id, e);
        }
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = OBSERVATION_LIST_RESOURCE_URL, method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<MobileObservationDTO> getObservations(
            @PathVariable final int firstCalendarYearOfHuntingYear, @RequestParam final int observationSpecVersion) {

        return feature.getObservations(
                firstCalendarYearOfHuntingYear, ObservationSpecVersion.fromIntValue(observationSpecVersion));
    }

    @RequestMapping(
            value = CREATE_OBSERVATION_RESOURCE_URL,
            method = {RequestMethod.POST, RequestMethod.PUT},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public MobileObservationDTO createObservation(@RequestBody @Validated final MobileObservationDTO dto) {
        // on create force id to be null
        dto.setId(null);
        return feature.createObservation(dto);
    }

    @RequestMapping(value = UPDATE_OBSERVATION_RESOURCE_URL, method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public MobileObservationDTO updateObservation(
            @PathVariable final Long id, @RequestBody @Validated final MobileObservationDTO dto) {

        dto.setId(id);
        return feature.updateObservation(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = DELETE_OBSERVATION_RESOURCE_URL, method = RequestMethod.DELETE)
    public void deleteObservation(@PathVariable final Long id) {
        try {
            feature.deleteObservation(id);
        } catch (Exception e) {
            LOG.warn("Deletion failed for observation id " + id, e);
        }
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 31536000)
    @RequestMapping(value = IMAGE_RESOURCE_URL, method = RequestMethod.GET)
    public ResponseEntity<?> getGameDiaryImage(@PathVariable final UUID imageUuid) throws IOException {
        return feature.getGameDiaryImageBytes(imageUuid, true);
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 31536000)
    @RequestMapping(value = IMAGE_RESOURCE_RESIZED_URL, method = RequestMethod.GET)
    public ResponseEntity<?> getGameDiaryImageResized(
            @PathVariable final UUID imageUuid,
            @PathVariable final int width,
            @PathVariable final int height,
            @PathVariable final boolean keepProportions) throws IOException {

        return feature.getGameDiaryImageBytesResized(imageUuid, width, height, keepProportions);
    }

    @RequestMapping(value = HARVEST_IMAGE_UPLOAD_RESOURCE_URL, method = RequestMethod.POST)
    public void addImageForHarvest(
            @RequestParam("harvestId") final long harvestId,
            @RequestParam("uuid") final UUID uuid,
            @RequestParam("file") final MultipartFile file)
            throws IOException {

        feature.addGameDiaryImageForDiaryEntry(harvestId, GameDiaryEntryType.HARVEST, uuid, file);
    }

    @RequestMapping(value = OBSERVATION_IMAGE_UPLOAD_RESOURCE_URL, method = RequestMethod.POST)
    public void addImageForObservation(
            @RequestParam("observationId") final long observationId,
            @RequestParam("uuid") final UUID uuid,
            @RequestParam("file") final MultipartFile file)
            throws IOException {

        feature.addGameDiaryImageForDiaryEntry(observationId, GameDiaryEntryType.OBSERVATION, uuid, file);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = IMAGE_RESOURCE_URL, method = RequestMethod.DELETE)
    public void deleteGameDiaryImage(@PathVariable final UUID imageUuid) {
        feature.deleteGameDiaryImage(imageUuid);
    }

    @RequestMapping(value = CHECK_PERMIT_NUMBER, method = RequestMethod.POST)
    public MobileHarvestPermitExistsDTO checkPermitNumber(@RequestParam final String permitNumber) {
        return feature.findPermitNumber(permitNumber);
    }

    @RequestMapping(value = PRELOAD_PERMITS, method = RequestMethod.GET)
    public List<MobileHarvestPermitExistsDTO> preloadPermits() {
        return feature.preloadPermits();
    }

}

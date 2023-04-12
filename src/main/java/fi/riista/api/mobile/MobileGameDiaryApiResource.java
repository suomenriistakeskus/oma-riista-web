package fi.riista.api.mobile;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.gamediary.GameDiaryImageFeature;
import fi.riista.feature.gamediary.GameDiaryMetadataFeature;
import fi.riista.feature.gamediary.harvest.HarvestExceptionMapper;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFieldsRequestDTO;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFieldsResponseDTO;
import fi.riista.feature.gamediary.mobile.MobileDeletedDiaryEntriesDTO;
import fi.riista.feature.gamediary.mobile.MobileDiaryEntryPageDTO;
import fi.riista.feature.gamediary.mobile.MobileGameDiaryFeature;
import fi.riista.feature.gamediary.mobile.MobileGameSpeciesCodesetDTO;
import fi.riista.feature.gamediary.mobile.MobileHarvestDTO;
import fi.riista.feature.gamediary.mobile.MobileHarvestFeature;
import fi.riista.feature.gamediary.mobile.MobileObservationDTO;
import fi.riista.feature.gamediary.mobile.MobileObservationFeature;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.observation.metadata.ObservationMetadataDTO;
import fi.riista.feature.harvestpermit.mobile.MobileHarvestPermitExistsDTO;
import fi.riista.feature.harvestpermit.mobile.MobileHarvestPermitFeature;
import fi.riista.util.Patterns;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
public class MobileGameDiaryApiResource {

    private static final Logger LOG = LoggerFactory.getLogger(MobileGameDiaryApiResource.class);

    private static final String URL_PREFIX = "/api/mobile/v2/gamediary";

    private static final String SPECIES_RESOURCE_URL = URL_PREFIX + "/gamespeciescodeset";

    private static final String HARVEST_LIST_RESOURCE_URL =
            URL_PREFIX + "/harvests/{firstCalendarYearOfHuntingYear:\\d+}";

    private static final String HARVEST_PAGE_RESOURCE_URL =
            URL_PREFIX + "/harvests/page";
    private static final String HARVEST_CHANGES_RESOURCE_URL =
            URL_PREFIX + "/harvests/haschanges/{firstCalendarYearOfHuntingYear:\\d+}/{since:" + Patterns.DATETIME_ISO_8601 + "}";
    private static final String HARVEST_BASE_RESOURCE_URL = URL_PREFIX + "/harvest";
    private static final String HARVEST_INSTANCE_RESOURCE_URL = HARVEST_BASE_RESOURCE_URL + "/{id:\\d+}";

    private static final String OBSERVATION_LIST_RESOURCE_URL =
            URL_PREFIX + "/observations/{firstCalendarYearOfHuntingYear:\\d+}";

    private static final String OBSERVATION_PAGE_RESOURCE_URL =
            URL_PREFIX + "/observations/page";
    private static final String OBSERVATION_BASE_RESOURCE_URL = URL_PREFIX + "/observation";
    private static final String OBSERVATION_INSTANCE_RESOURCE_URL = OBSERVATION_BASE_RESOURCE_URL + "/{id:\\d+}";
    private static final String OBSERVATION_METADATA_RESOURCE_URL = OBSERVATION_BASE_RESOURCE_URL + "/metadata/{observationSpecVersion:\\d+}";

    private static final String IMAGE_RESOURCE_URL = URL_PREFIX + "/image/{imageUuid:" + Patterns.UUID + "}";
    private static final String IMAGE_RESOURCE_RESIZED_URL =
            URL_PREFIX + "/image/{imageUuid:" + Patterns.UUID + "}/resize/{width:\\d{1,4}}x{height:\\d{1,4}}x{keepProportions:\\d{1}}";
    private static final String HARVEST_IMAGE_UPLOAD_RESOURCE_URL = URL_PREFIX + "/image/uploadforharvest";
    private static final String OBSERVATION_IMAGE_UPLOAD_RESOURCE_URL = URL_PREFIX + "/image/uploadforobservation";

    private static final String CHECK_PERMIT_NUMBER = URL_PREFIX + "/checkPermitNumber";
    private static final String PRELOAD_PERMITS = URL_PREFIX + "/preloadPermits";
    private static final String DELETED_HARVESTS = HARVEST_BASE_RESOURCE_URL + "/deleted";
    private static final String DELETED_OBSERVATIONS = OBSERVATION_BASE_RESOURCE_URL + "/deleted";

    @Resource
    private MobileHarvestFeature mobileHarvestFeature;

    @Resource
    private MobileObservationFeature mobileObservationFeature;

    @Resource
    private MobileHarvestPermitFeature mobileHarvestPermitFeature;

    @Resource
    private MobileGameDiaryFeature mobileGameDiaryFeature;

    @Resource
    private GameDiaryImageFeature gameDiaryImageFeature;

    @Resource
    private GameDiaryMetadataFeature gameDiaryMetadataFeature;

    @Resource
    private HarvestExceptionMapper harvestExceptionMapper;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = SPECIES_RESOURCE_URL, produces = MediaType.APPLICATION_JSON_VALUE)
    public MobileGameSpeciesCodesetDTO getGameSpecies() {
        return gameDiaryMetadataFeature.getMobileGameSpecies();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = HARVEST_BASE_RESOURCE_URL + "/fields")
    public RequiredHarvestFieldsResponseDTO getRequiredHarvestFields(
            @RequestParam final int harvestSpecVersion,
            @RequestParam final int gameSpeciesCode,
            @RequestParam final boolean withPermit,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate harvestDate,
            @RequestParam final int longitude,
            @RequestParam final int latitude) {

        return gameDiaryMetadataFeature.getRequiredHarvestFields(
                new RequiredHarvestFieldsRequestDTO(
                        gameSpeciesCode,
                        harvestDate,
                        new GeoLocation(latitude, longitude),
                        withPermit),
                HarvestSpecVersion.fromIntValue(harvestSpecVersion));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = HARVEST_INSTANCE_RESOURCE_URL + "/fields")
    public RequiredHarvestFieldsResponseDTO getRequiredHarvestFields(@PathVariable final int id,
                                                                     @RequestParam final int harvestSpecVersion) {
        return gameDiaryMetadataFeature
                .getRequiredFieldsForHarvest(id, HarvestSpecVersion.fromIntValue(harvestSpecVersion));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = OBSERVATION_METADATA_RESOURCE_URL)
    public ObservationMetadataDTO getObservationMetadata(@PathVariable final int observationSpecVersion) {
        return mobileObservationFeature
                .getMobileObservationFieldMetadata(ObservationSpecVersion.fromIntValue(observationSpecVersion));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = HARVEST_LIST_RESOURCE_URL, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<MobileHarvestDTO> getHarvests(@PathVariable final int firstCalendarYearOfHuntingYear,
                                              @RequestParam final int harvestSpecVersion) {

        return mobileHarvestFeature
                .getHarvests(firstCalendarYearOfHuntingYear, HarvestSpecVersion.fromIntValue(harvestSpecVersion));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = HARVEST_PAGE_RESOURCE_URL, produces = MediaType.APPLICATION_JSON_VALUE)
    public MobileDiaryEntryPageDTO<MobileHarvestDTO> getHarvestPage(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") final LocalDateTime modifiedAfter,
            @RequestParam final int harvestSpecVersion) {
        final HarvestSpecVersion specVersion = HarvestSpecVersion.fromIntValue(harvestSpecVersion);
        return mobileHarvestFeature.fetchPageForActiveUser(modifiedAfter, specVersion);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = HARVEST_CHANGES_RESOURCE_URL)
    public String checkHarvestsAreUpdated(
            @PathVariable final Integer firstCalendarYearOfHuntingYear,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") final LocalDateTime since) {

        return "true";
    }

    @RequestMapping(value = HARVEST_BASE_RESOURCE_URL,
            method = {RequestMethod.POST, RequestMethod.PUT},
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createHarvest(@RequestBody @Valid final MobileHarvestDTO dto) {
        // on create force id and rev to be null
        dto.setId(null);
        dto.setRev(null);
        try {
            return ResponseEntity.ok(mobileHarvestFeature.createHarvest(dto));
        } catch (final RuntimeException e) {
            return harvestExceptionMapper.handleException(e);
        }
    }

    @PutMapping(value = HARVEST_INSTANCE_RESOURCE_URL,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateHarvest(@PathVariable final long id,
                                           @RequestBody @Validated final MobileHarvestDTO dto) {
        dto.setId(id);
        try {
            return ResponseEntity.ok(mobileHarvestFeature.updateHarvest(dto));
        } catch (final RuntimeException e) {
            return harvestExceptionMapper.handleException(e);
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = HARVEST_INSTANCE_RESOURCE_URL)
    public void deleteHarvest(@PathVariable final long id) {
        try {
            mobileHarvestFeature.deleteHarvest(id);
        } catch (final Exception e) {
            LOG.warn("Deletion failed for harvest id " + id, e);
        }
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = OBSERVATION_LIST_RESOURCE_URL, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<MobileObservationDTO> getObservations(@PathVariable final int firstCalendarYearOfHuntingYear,
                                                      @RequestParam final int observationSpecVersion) {

        return mobileObservationFeature.getObservations(
                firstCalendarYearOfHuntingYear, ObservationSpecVersion.fromIntValue(observationSpecVersion));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = OBSERVATION_PAGE_RESOURCE_URL, produces = MediaType.APPLICATION_JSON_VALUE)
    public MobileDiaryEntryPageDTO<MobileObservationDTO> getObservationPage(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") final LocalDateTime modifiedAfter,
            @RequestParam final int observationSpecVersion) {
        final ObservationSpecVersion specVersion = ObservationSpecVersion.fromIntValue(observationSpecVersion);
        return mobileObservationFeature.fetchPageForActiveUser(modifiedAfter, specVersion);
    }

    @RequestMapping(value = OBSERVATION_BASE_RESOURCE_URL,
            method = {RequestMethod.POST, RequestMethod.PUT},
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public MobileObservationDTO createObservation(@RequestBody @Validated final MobileObservationDTO dto) {
        // on create force id to be null
        dto.setId(null);
        return mobileObservationFeature.createObservation(dto);
    }

    @PutMapping(value = OBSERVATION_INSTANCE_RESOURCE_URL,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public MobileObservationDTO updateObservation(@PathVariable final long id,
                                                  @RequestBody @Validated final MobileObservationDTO dto) {

        dto.setId(id);
        return mobileObservationFeature.updateObservation(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = OBSERVATION_INSTANCE_RESOURCE_URL)
    public void deleteObservation(@PathVariable final long id) {
        try {
            mobileObservationFeature.deleteObservation(id);
        } catch (final Exception e) {
            LOG.warn("Deletion failed for observation id " + id, e);
        }
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 31536000)
    @GetMapping(value = IMAGE_RESOURCE_URL)
    public ResponseEntity<?> getGameDiaryImage(@PathVariable final UUID imageUuid) throws IOException {
        return gameDiaryImageFeature.getGameDiaryImageBytes(imageUuid, true);
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 31536000)
    @GetMapping(value = IMAGE_RESOURCE_RESIZED_URL)
    public ResponseEntity<?> getGameDiaryImageResized(@PathVariable final UUID imageUuid,
                                                      @PathVariable final int width,
                                                      @PathVariable final int height,
                                                      @PathVariable final boolean keepProportions) throws IOException {

        return gameDiaryImageFeature.getGameDiaryImageBytesResized(imageUuid, width, height, keepProportions);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = HARVEST_IMAGE_UPLOAD_RESOURCE_URL)
    public void addImageForHarvest(@RequestParam("harvestId") final long harvestId,
                                   @RequestParam("uuid") final UUID uuid,
                                   @RequestParam("file") final MultipartFile file) throws IOException {

        gameDiaryImageFeature.addGameDiaryImageForDiaryEntry(harvestId, GameDiaryEntryType.HARVEST, uuid, file);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = OBSERVATION_IMAGE_UPLOAD_RESOURCE_URL)
    public void addImageForObservation(@RequestParam("observationId") final long observationId,
                                       @RequestParam("uuid") final UUID uuid,
                                       @RequestParam("file") final MultipartFile file) throws IOException {

        gameDiaryImageFeature.addGameDiaryImageForDiaryEntry(observationId, GameDiaryEntryType.OBSERVATION, uuid, file);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = IMAGE_RESOURCE_URL)
    public void deleteGameDiaryImage(@PathVariable final UUID imageUuid) {
        mobileGameDiaryFeature.deleteGameDiaryImage(imageUuid);
    }

    @PostMapping(value = CHECK_PERMIT_NUMBER)
    public MobileHarvestPermitExistsDTO checkPermitNumber(
            @RequestParam final String permitNumber,
            @RequestParam(value = "harvestSpecVersion", required = false) final Integer specVersionNumber) {

        // HarvestSpecVersion parameter was introduced in specVersion 8 into this method.
        // Hence, we can assume version 7 here, if it is missing.

        final HarvestSpecVersion specVersion = Optional
                .ofNullable(specVersionNumber)
                .map(HarvestSpecVersion::fromIntValue)
                .orElse(HarvestSpecVersion._7);

        return mobileHarvestPermitFeature.findPermitNumber(permitNumber, specVersion);
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 31536000)
    @GetMapping(value = PRELOAD_PERMITS)
    public List<MobileHarvestPermitExistsDTO> preloadPermits(
            @RequestParam(value = "harvestSpecVersion", required = false) final Integer specVersionNumber) {

        // HarvestSpecVersion parameter was introduced in specVersion 8 into this method.
        // Hence, we can assume version 7 here, if it is missing.

        final HarvestSpecVersion specVersion = Optional
                .ofNullable(specVersionNumber)
                .map(HarvestSpecVersion::fromIntValue)
                .orElse(HarvestSpecVersion._7);

        return mobileHarvestPermitFeature.preloadPermits(specVersion);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = DELETED_HARVESTS, produces = MediaType.APPLICATION_JSON_VALUE)
    public MobileDeletedDiaryEntriesDTO getDeletedHarvests(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") final LocalDateTime deletedAfter) {
        return mobileHarvestFeature.getDeletedHarvestIds(deletedAfter)
                .combine(mobileHarvestFeature.getHarvestsWhereOnlyAuthor());
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = DELETED_OBSERVATIONS, produces = MediaType.APPLICATION_JSON_VALUE)
    public MobileDeletedDiaryEntriesDTO getDeletedObservations(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") final LocalDateTime deletedAfter) {
        return mobileObservationFeature.getDeletedObservationIds(deletedAfter)
                .combine(mobileObservationFeature.getObservationsWhereOnlyAuthor());
    }

}

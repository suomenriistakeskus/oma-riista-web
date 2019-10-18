package fi.riista.api.mobile;

import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.gamediary.GameDiaryImageFeature;
import fi.riista.feature.gamediary.GameDiaryMetadataFeature;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.mobile.MobileGameDiaryFeature;
import fi.riista.feature.gamediary.mobile.MobileGameSpeciesCodesetDTO;
import fi.riista.feature.gamediary.mobile.MobileHarvestDTO;
import fi.riista.feature.harvestpermit.mobile.MobileHarvestPermitExistsDTO;
import fi.riista.feature.harvestpermit.mobile.MobileHarvestPermitFeature;
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
import java.util.List;
import java.util.UUID;

@RestController
public class MobileGameDiaryV1ApiResource {

    private static final Logger LOG = LoggerFactory.getLogger(MobileGameDiaryV1ApiResource.class);

    private static final int API_VERSION = 1;
    private static final String URL_PREFIX = "/api/mobile/v1/gamediary";

    private static final String SPECIES_RESOURCE_URL = URL_PREFIX + "/gamespeciescodeset";

    private static final String HARVEST_LIST_RESOURCE_URL =
            URL_PREFIX + "/entries/{firstCalendarYearOfHuntingYear:\\d+}";
    private static final String HARVEST_CHANGES_RESOURCE_URL =
            URL_PREFIX + "/entries/haschanges/{firstCalendarYearOfHuntingYear:\\d+}/{since:" + Patterns.DATETIME_ISO_8601 + "}";
    private static final String CREATE_HARVEST_RESOURCE_URL = URL_PREFIX + "/entry";
    private static final String UPDATE_HARVEST_RESOURCE_URL = URL_PREFIX + "/entry/{id:\\d+}";
    private static final String DELETE_HARVEST_RESOURCE_URL = URL_PREFIX + "/entry/{id:\\d+}";

    private static final String IMAGE_RESOURCE_URL = URL_PREFIX + "/image/{imageUuid:" + Patterns.UUID + "}";
    private static final String IMAGE_RESOURCE_RESIZED_URL =
            URL_PREFIX + "/image/{imageUuid:" + Patterns.UUID + "}/resize/{width:\\d{1,4}}x{height:\\d{1,4}}x{keepProportions:\\d{1}}";
    private static final String IMAGE_UPLOAD_RESOURCE_URL = URL_PREFIX + "/image/upload";

    private static final String CHECK_PERMIT_NUMBER = URL_PREFIX + "/checkPermitNumber";
    private static final String PRELOAD_PERMITS = URL_PREFIX + "/preloadPermits";

    private static HarvestSpecVersion resolveHarvestSpecVersion(final MobileHarvestDTO dto) {
        return dto.getApiVersion() == null ? HarvestSpecVersion._1 : HarvestSpecVersion._2;
    }

    @Resource
    private MobileGameDiaryFeature feature;

    @Resource
    private MobileHarvestPermitFeature mobileHarvestPermitFeature;

    @Resource
    private GameDiaryImageFeature gameDiaryImageFeature;

    @Resource
    private GameDiaryMetadataFeature gameDiaryMetadataFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = SPECIES_RESOURCE_URL, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public MobileGameSpeciesCodesetDTO getGameSpecies() {
        return gameDiaryMetadataFeature.getMobileGameSpecies();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = HARVEST_LIST_RESOURCE_URL, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<MobileHarvestDTO> getHarvests(@PathVariable Integer firstCalendarYearOfHuntingYear) {
        return feature.getHarvests(firstCalendarYearOfHuntingYear, HarvestSpecVersion._2);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = HARVEST_CHANGES_RESOURCE_URL)
    public String checkHarvestsAreUpdated(
            @PathVariable Integer firstCalendarYearOfHuntingYear,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime since) {

        return "true";
    }

    @RequestMapping(
            value = CREATE_HARVEST_RESOURCE_URL,
            method = {RequestMethod.POST, RequestMethod.PUT},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public MobileHarvestDTO createHarvest(@RequestBody @Valid MobileHarvestDTO dto) {
        // on create force id and rev to be null
        dto.setId(null);
        dto.setRev(null);

        // Set manually because old mobile clients do not support this attribute.
        dto.setHarvestSpecVersion(resolveHarvestSpecVersion(dto));

        try {
            return feature.createHarvest(dto, API_VERSION);
        } catch (Exception e) {
            // Creation will fail for example when there is two concurrent requests with same mobileClientRefId.
            // To keep mobile client not showing duplicates until next sync, check if creation failed because of
            // duplicate mobileClientRefId, if so then return then previously created entity.
            final MobileHarvestDTO existing = feature.getExistingByMobileClientRefId(dto, API_VERSION);
            if (existing != null) {
                return existing;
            }
            throw e;
        }
    }

    @PutMapping(value = UPDATE_HARVEST_RESOURCE_URL,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public MobileHarvestDTO updateHarvest(@PathVariable Long id, @RequestBody @Validated MobileHarvestDTO dto) {
        dto.setId(id);

        // Set manually because old mobile clients do not support this attribute.
        dto.setHarvestSpecVersion(resolveHarvestSpecVersion(dto));

        return feature.updateHarvest(dto, API_VERSION);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = DELETE_HARVEST_RESOURCE_URL)
    public void deleteHarvest(@PathVariable Long id) {
        try {
            feature.deleteHarvest(id);
        } catch (Exception e) {
            LOG.warn("Deletion failed for harvest id " + id, e);
        }
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 31536000)
    @GetMapping(value = IMAGE_RESOURCE_URL)
    public ResponseEntity<?> getGameDiaryImage(@PathVariable UUID imageUuid) throws IOException {
        return gameDiaryImageFeature.getGameDiaryImageBytes(imageUuid, true);
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 31536000)
    @GetMapping(value = IMAGE_RESOURCE_RESIZED_URL)
    public ResponseEntity<?> getGameDiaryImageResized(@PathVariable UUID imageUuid,
                                                      @PathVariable int width,
                                                      @PathVariable int height,
                                                      @PathVariable boolean keepProportions) throws IOException {

        return gameDiaryImageFeature.getGameDiaryImageBytesResized(imageUuid, width, height, keepProportions);
    }

    @PostMapping(value = IMAGE_UPLOAD_RESOURCE_URL)
    public void addImageForHarvest(@RequestParam("gameDiaryEntryId") long harvestId,
                                   @RequestParam("uuid") UUID uuid,
                                   @RequestParam("file") MultipartFile file) throws IOException {

        gameDiaryImageFeature.addGameDiaryImageForDiaryEntry(harvestId, GameDiaryEntryType.HARVEST, uuid, file);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = IMAGE_RESOURCE_URL)
    public void deleteGameDiaryImage(@PathVariable UUID imageUuid) {
        feature.deleteGameDiaryImage(imageUuid);
    }

    @PostMapping(value = CHECK_PERMIT_NUMBER)
    public MobileHarvestPermitExistsDTO checkPermitNumber(@RequestParam String permitNumber) {
        return mobileHarvestPermitFeature.findPermitNumber(permitNumber);
    }

    @GetMapping(value = PRELOAD_PERMITS)
    public List<MobileHarvestPermitExistsDTO> preloadPermits() {
        return mobileHarvestPermitFeature.preloadPermits();
    }

}

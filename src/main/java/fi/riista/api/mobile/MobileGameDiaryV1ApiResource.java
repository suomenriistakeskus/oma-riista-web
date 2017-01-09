package fi.riista.api.mobile;

import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.mobile.MobileGameDiaryV1Feature;
import fi.riista.feature.gamediary.mobile.MobileGameSpeciesCodesetDTO;
import fi.riista.feature.gamediary.mobile.MobileAccountV1DTO;
import fi.riista.feature.gamediary.mobile.MobileHarvestDTO;
import fi.riista.feature.gamediary.mobile.MobileHarvestPermitExistsDTO;
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
public class MobileGameDiaryV1ApiResource {

    private static final Logger LOG = LoggerFactory.getLogger(MobileGameDiaryV1ApiResource.class);

    private static final String URL_PREFIX = "/api/mobile/v1/gamediary";

    public static final String ACCOUNT_RESOURCE_URL = URL_PREFIX + "/account";

    public static final String SPECIES_RESOURCE_URL = URL_PREFIX + "/gamespeciescodeset";

    public static final String HARVEST_LIST_RESOURCE_URL =
            URL_PREFIX + "/entries/{firstCalendarYearOfHuntingYear:\\d+}";
    public static final String HARVEST_CHANGES_RESOURCE_URL =
            URL_PREFIX + "/entries/haschanges/{firstCalendarYearOfHuntingYear:\\d+}/{since:" + Patterns.DATETIME_ISO_8601 + "}";
    public static final String CREATE_HARVEST_RESOURCE_URL = URL_PREFIX + "/entry";
    public static final String UPDATE_HARVEST_RESOURCE_URL = URL_PREFIX + "/entry/{id:\\d+}";
    public static final String DELETE_HARVEST_RESOURCE_URL = URL_PREFIX + "/entry/{id:\\d+}";

    public static final String IMAGE_RESOURCE_URL = URL_PREFIX + "/image/{imageUuid:" + Patterns.UUID + "}";
    public static final String IMAGE_RESOURCE_RESIZED_URL =
            URL_PREFIX + "/image/{imageUuid:" + Patterns.UUID + "}/resize/{width:\\d{1,4}}x{height:\\d{1,4}}x{keepProportions:\\d{1}}";
    public static final String IMAGE_UPLOAD_RESOURCE_URL = URL_PREFIX + "/image/upload";

    public static final String CHECK_PERMIT_NUMBER = URL_PREFIX + "/checkPermitNumber";
    public static final String PRELOAD_PERMITS = URL_PREFIX + "/preloadPermits";

    @Resource
    private MobileGameDiaryV1Feature feature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = SPECIES_RESOURCE_URL, method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public MobileGameSpeciesCodesetDTO getGameSpecies() {
        return new MobileGameSpeciesCodesetDTO(feature.getGameCategories(), feature.getGameSpecies());
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = ACCOUNT_RESOURCE_URL, method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public MobileAccountV1DTO getAccount() {
        return feature.getMobileAccount();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = HARVEST_LIST_RESOURCE_URL, method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<MobileHarvestDTO> getHarvests(@PathVariable Integer firstCalendarYearOfHuntingYear) {
        return feature.getHarvests(firstCalendarYearOfHuntingYear, HarvestSpecVersion._2);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = HARVEST_CHANGES_RESOURCE_URL, method = RequestMethod.GET)
    public String checkHarvestsAreUpdated(
            @PathVariable Integer firstCalendarYearOfHuntingYear,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime since) {

        return "true";
    }

    @RequestMapping(
            value = CREATE_HARVEST_RESOURCE_URL,
            method = {RequestMethod.POST, RequestMethod.PUT},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public MobileHarvestDTO createHarvest(@RequestBody @Valid MobileHarvestDTO dto) {
        // on create force id and rev to be null
        dto.setId(null);
        dto.setRev(null);

        // Set manually because old mobile clients do not support this attribute.
        setHarvestSpecVersion(dto);

        try {
            return feature.createHarvest(dto);
        } catch (Exception e) {
            // Creation will fail for example when there is two concurrent requests with same mobileClientRefId.
            // To keep mobile client not showing duplicates until next sync, check if creation failed because of
            // duplicate mobileClientRefId, if so then return then previously created entity.
            final MobileHarvestDTO existing = feature.getExistingByMobileClientRefId(dto);
            if (existing != null) {
                return existing;
            }
            throw e;
        }
    }

    @RequestMapping(value = UPDATE_HARVEST_RESOURCE_URL, method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public MobileHarvestDTO updateHarvest(
            @PathVariable Long id, @RequestBody @Validated MobileHarvestDTO dto) {

        dto.setId(id);

        // Set manually because old mobile clients do not support this attribute.
        setHarvestSpecVersion(dto);

        return feature.updateHarvest(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = DELETE_HARVEST_RESOURCE_URL, method = RequestMethod.DELETE)
    public void deleteHarvest(@PathVariable Long id) {
        try {
            feature.deleteHarvest(id);
        } catch (Exception e) {
            LOG.warn("Deletion failed for harvest id " + id, e);
        }
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 31536000)
    @RequestMapping(value = IMAGE_RESOURCE_URL, method = RequestMethod.GET)
    public ResponseEntity<?> getGameDiaryImage(@PathVariable UUID imageUuid) throws IOException {
        return feature.getGameDiaryImageBytes(imageUuid, true);
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 31536000)
    @RequestMapping(value = IMAGE_RESOURCE_RESIZED_URL, method = RequestMethod.GET)
    public ResponseEntity<?> getGameDiaryImageResized(
            @PathVariable UUID imageUuid,
            @PathVariable int width,
            @PathVariable int height,
            @PathVariable boolean keepProportions) throws IOException {

        return feature.getGameDiaryImageBytesResized(imageUuid, width, height, keepProportions);
    }

    @RequestMapping(value = IMAGE_UPLOAD_RESOURCE_URL, method = RequestMethod.POST)
    public void addImageForHarvest(
            @RequestParam("gameDiaryEntryId") long harvestId,
            @RequestParam("uuid") UUID uuid,
            @RequestParam("file") MultipartFile file)
            throws IOException {

        feature.addGameDiaryImageForDiaryEntry(harvestId, GameDiaryEntryType.HARVEST, uuid, file);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = IMAGE_RESOURCE_URL, method = RequestMethod.DELETE)
    public void deleteGameDiaryImage(@PathVariable UUID imageUuid) {
        feature.deleteGameDiaryImage(imageUuid);
    }

    @RequestMapping(value = CHECK_PERMIT_NUMBER, method = RequestMethod.POST)
    public MobileHarvestPermitExistsDTO checkPermitNumber(@RequestParam String permitNumber) {
        return feature.findPermitNumber(permitNumber);
    }

    @RequestMapping(value = PRELOAD_PERMITS, method = RequestMethod.GET)
    public List<MobileHarvestPermitExistsDTO> preloadPermits() {
        return feature.preloadPermits();
    }

    private static HarvestSpecVersion resolveHarvestSpecVersion(final MobileHarvestDTO dto) {
        return dto.getApiVersion() == null ? HarvestSpecVersion._1 : HarvestSpecVersion._2;
    }

    private static void setHarvestSpecVersion(final MobileHarvestDTO dto) {
        dto.setHarvestSpecVersion(resolveHarvestSpecVersion(dto));
    }

}

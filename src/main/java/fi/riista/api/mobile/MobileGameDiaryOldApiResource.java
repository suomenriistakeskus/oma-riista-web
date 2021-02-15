package fi.riista.api.mobile;

import fi.riista.util.Patterns;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.joda.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

// TODO To be removed later when use of iOS 2.0.0.1 release is diminished.
@RestController
public class MobileGameDiaryOldApiResource {

    private static final String URL_PREFIX = "/api/mobile/v1/gamediary";

    private static final String HARVEST_CHANGES_RESOURCE_URL =
            URL_PREFIX + "/entries/haschanges/{firstCalendarYearOfHuntingYear:\\d+}/{since:" + Patterns.DATETIME_ISO_8601 + "}";

    // iOS v2.0.0.1 app still references this.
    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = HARVEST_CHANGES_RESOURCE_URL)
    public String checkHarvestsAreUpdated(
            @PathVariable final Integer firstCalendarYearOfHuntingYear,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") final LocalDateTime since) {

        return "true";
    }
}

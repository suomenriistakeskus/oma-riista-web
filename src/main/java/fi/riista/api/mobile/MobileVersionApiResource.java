package fi.riista.api.mobile;

import com.google.common.collect.ImmutableMap;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class MobileVersionApiResource {

    public static final String LATEST_RELEASE_URL = "/api/mobile/v2/release";

    private final Map<String, Object> VERSION_RESPONSE;

    public MobileVersionApiResource() {
        this.VERSION_RESPONSE = ImmutableMap.<String, Object> builder()
                .put("android", "1.9.0")
                .put("ios", "1.8.0.0")
                .put("wp", "1.8.0.0")
                .build();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = LATEST_RELEASE_URL, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map<String, Object> getLatestRelease() {
        return VERSION_RESPONSE;
    }
}

package fi.riista.api.mobile;

import fi.riista.feature.account.mobile.MobileAccountDTO;
import fi.riista.feature.account.mobile.MobileAccountFeature;
import fi.riista.feature.account.mobile.MobileOccupationDTO;
import fi.riista.feature.organization.occupation.OccupationCrudFeature;
import fi.riista.feature.push.MobilePushRegistrationDTO;
import fi.riista.feature.push.RegisterMobileClientDeviceFeature;
import fi.riista.util.DateUtil;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController
public class MobileAccountApiResource {

    // Transition to "v2" was done within release of observation API.
    private static final String API_PREFIX = "/api/mobile/v2";

    public static final String ACCOUNT_RESOURCE_URL = API_PREFIX + "/gamediary/account";
    public static final String PUSH_REGISTER_RESOURCE_URL = API_PREFIX + "/push/register";

    @Resource
    private MobileAccountFeature mobileAccountFeature;

    @Resource
    private RegisterMobileClientDeviceFeature registerMobileClientDeviceFeature;

    @Resource
    private OccupationCrudFeature occupationCrudFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = ACCOUNT_RESOURCE_URL, produces = MediaType.APPLICATION_JSON_VALUE)
    public MobileAccountDTO getAccount() {
        return mobileAccountFeature.getMobileAccount();
    }

    @PostMapping(value = PUSH_REGISTER_RESOURCE_URL, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void registerDeviceForPushNotifications(@RequestBody @Valid final MobilePushRegistrationDTO dto) {
        registerMobileClientDeviceFeature.registerDevice(dto);
    }

    @GetMapping(value = API_PREFIX + "/club/my-memberships")
    @CacheControl(policy = CachePolicy.NO_CACHE)
    public List<MobileOccupationDTO> myMemberships() {
        return occupationCrudFeature.listMyClubMemberships();
    }

    @PostMapping(value = API_PREFIX + "/unregister")
    public LocalDateTime unregister() {
        final DateTime unregisterRequestedTimestamp = mobileAccountFeature.unregister();

        return DateUtil.toLocalDateTimeNullSafe(unregisterRequestedTimestamp);
    }

    @PostMapping(value = API_PREFIX + "/cancel-unregister")
    public void cancelUnregister() {
        mobileAccountFeature.cancelUnregister();
    }
}

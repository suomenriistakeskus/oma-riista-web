package fi.riista.api.mobile;

import fi.riista.feature.account.mobile.MobileAccountV1DTO;
import fi.riista.feature.account.mobile.MobileAccountV1Feature;
import fi.riista.feature.account.mobile.MobileAccountV2DTO;
import fi.riista.feature.account.mobile.MobileAccountV2Feature;
import fi.riista.feature.push.MobilePushRegistrationDTO;
import fi.riista.feature.push.RegisterMobileClientDeviceFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
public class MobileAccountApiResource {

    public static final String ACCOUNT_V1_RESOURCE_URL = "/api/mobile/v1/gamediary/account";
    public static final String ACCOUNT_V2_RESOURCE_URL = "/api/mobile/v2/gamediary/account";
    public static final String PUSH_REGISTER_RESOURCE_URL = "/api/mobile/v2/push/register";

    @Resource
    private MobileAccountV1Feature mobileAccountV1Feature;

    @Resource
    private MobileAccountV2Feature mobileAccountV2Feature;

    @Resource
    private RegisterMobileClientDeviceFeature registerMobileClientDeviceFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = ACCOUNT_V1_RESOURCE_URL, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public MobileAccountV1DTO getV1Account() {
        return mobileAccountV1Feature.getMobileAccount();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = ACCOUNT_V2_RESOURCE_URL, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public MobileAccountV2DTO getV2Account() {
        return mobileAccountV2Feature.getMobileAccount();
    }

    @PostMapping(value = PUSH_REGISTER_RESOURCE_URL, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void registerDeviceForPushNotifications(@RequestBody @Valid MobilePushRegistrationDTO dto) {
        registerMobileClientDeviceFeature.registerDevice(dto);
    }
}

package fi.riista.feature.push;

import fi.riista.validation.XssSafe;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotBlank;

import javax.validation.constraints.NotNull;

public class MobilePushRegistrationDTO {

    @NotNull
    @Length(min = 10)
    @XssSafe
    private String pushToken;

    @NotNull
    private MobileClientDevice.Platform platform;

    @NotBlank
    @XssSafe
    private String deviceName;

    @NotBlank
    @XssSafe
    private String clientVersion;

    public MobileClientDevice.Platform getPlatform() {
        return platform;
    }

    public void setPlatform(final MobileClientDevice.Platform platform) {
        this.platform = platform;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(final String deviceName) {
        this.deviceName = deviceName;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(final String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(final String pushToken) {
        this.pushToken = pushToken;
    }
}

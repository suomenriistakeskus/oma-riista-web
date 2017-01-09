package fi.riista.feature.sso.support;

import fi.riista.feature.sso.dto.ExternalAuthenticationRequest;
import fi.riista.security.UserInfo;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

public class ExternalAuthenticationDetails implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String remoteAddress;
    private final UserInfo apiUserInfo;

    public ExternalAuthenticationDetails(ExternalAuthenticationRequest request, UserInfo apiUserInfo) {
        this.remoteAddress = request.getRemoteAddress();
        this.apiUserInfo = apiUserInfo;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public UserInfo getApiUserInfo() {
        return apiUserInfo;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("remoteAddress", remoteAddress)
                .append("apiUserInfo", apiUserInfo)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ExternalAuthenticationDetails)) {
            return false;
        }

        ExternalAuthenticationDetails that = (ExternalAuthenticationDetails) o;

        if (apiUserInfo != null ? !apiUserInfo.equals(that.apiUserInfo) : that.apiUserInfo != null) {
            return false;
        }

        if (remoteAddress != null ? !remoteAddress.equals(that.remoteAddress) : that.remoteAddress != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = remoteAddress != null ? remoteAddress.hashCode() : 0;
        result = 31 * result + (apiUserInfo != null ? apiUserInfo.hashCode() : 0);
        return result;
    }
}

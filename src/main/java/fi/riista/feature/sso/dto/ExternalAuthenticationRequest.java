package fi.riista.feature.sso.dto;

public class ExternalAuthenticationRequest {
    private String username;
    private String password;
    private String otp;
    private boolean requireOtp;
    private String remoteAddress;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public boolean isRequireOtp() {
        return requireOtp;
    }

    public void setRequireOtp(boolean requireOtp) {
        this.requireOtp = requireOtp;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }
}

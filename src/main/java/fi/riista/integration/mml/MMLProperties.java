package fi.riista.integration.mml;

public class MMLProperties {
    private String wfsUri;
    private String wfsUsername;
    private String wfsPassword;

    public MMLProperties(String uri, String username, String password) {
        this.wfsUri = uri;
        this.wfsUsername = username;
        this.wfsPassword = password;
    }

    public String getEndpointUrl() {
        return wfsUri;
    }

    public String getWfsUsername() {
        return wfsUsername;
    }

    public String getWfsPassword() {
        return wfsPassword;
    }
}

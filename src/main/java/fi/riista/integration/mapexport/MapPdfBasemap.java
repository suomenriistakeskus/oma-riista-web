package fi.riista.integration.mapexport;

public enum MapPdfBasemap {
    MAASTOKARTTA("maasto"),
    TAUSTAKARTTA("tausta");

    private String name;

    MapPdfBasemap(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

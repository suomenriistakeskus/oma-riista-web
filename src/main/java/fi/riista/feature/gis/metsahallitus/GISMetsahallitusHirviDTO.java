package fi.riista.feature.gis.metsahallitus;

public class GISMetsahallitusHirviDTO {
    private final int id;
    private final int number;
    private final String name;
    private final long size;

    public GISMetsahallitusHirviDTO(final int id, final int number, final String name, final long size) {
        this.id = id;
        this.number = number;
        this.name = name;
        this.size = size;
    }

    public int getId() {
        return id;
    }

    public int getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }
}

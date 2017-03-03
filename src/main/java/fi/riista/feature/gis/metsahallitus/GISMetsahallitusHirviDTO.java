package fi.riista.feature.gis.metsahallitus;

public class GISMetsahallitusHirviDTO {
    private final int gid;
    private final int year;
    private final int number;
    private final String name;
    private final long size;

    public GISMetsahallitusHirviDTO(final int gid, final int year, final int number,
                                    final String name, final long size) {
        this.gid = gid;
        this.year = year;
        this.number = number;
        this.name = name;
        this.size = size;
    }

    public int getGid() {
        return gid;
    }

    public int getYear() {
        return year;
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

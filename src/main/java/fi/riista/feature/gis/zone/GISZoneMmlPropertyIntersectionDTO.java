package fi.riista.feature.gis.zone;

public class GISZoneMmlPropertyIntersectionDTO {

    private final long zoneId;

    private final long kiinteistoTunnus;

    private final int palstaId;

    private final String name;

    private final double intersectionArea;

    public GISZoneMmlPropertyIntersectionDTO(final long zoneId,
                                             final long kiinteistoTunnus,
                                             final int palstaId,
                                             final String name,
                                             final double intersectionArea) {
        this.zoneId = zoneId;
        this.kiinteistoTunnus = kiinteistoTunnus;
        this.palstaId = palstaId;
        this.name = name;
        this.intersectionArea = intersectionArea;
    }


    public long getZoneId() {
        return zoneId;
    }


    public long getKiinteistoTunnus() {
        return kiinteistoTunnus;
    }

    public String getName() {
        return name;
    }

    public double getIntersectionArea() {
        return intersectionArea;
    }

    public int getPalstaId() {
        return palstaId;
    }

}

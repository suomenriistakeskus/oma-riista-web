package fi.riista.feature.permit.area.mml;

public class HarvestPermitAreaMmlDTO {

    private final String tunnus;

    private final int id;

    private final String name;

    private final double area;

    public HarvestPermitAreaMmlDTO(final String tunnus,
                                   final int id,
                                   final String name,
                                   final double area) {
        this.tunnus = tunnus;
        this.id = id;
        this.name = name;
        this.area = area;
    }

    public String getTunnus() {
        return tunnus;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getArea() {
        return area;
    }
}

package fi.riista.feature.huntingclub.area.zone;


import com.vividsolutions.jts.geom.Geometry;

import java.util.Set;

public class HuntingClubAreaFeatureDTO {
    private Long id;
    private String name;
    private Double size;
    private Geometry geometry;
    private String propertyIdentifier;
    private Set<Integer> validSpecies;
    private boolean changed;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getSize() {
        return size;
    }

    public void setSize(Double size) {
        this.size = size;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(final Geometry geometry) {
        this.geometry = geometry;
    }

    public String getPropertyIdentifier() {
        return propertyIdentifier;
    }

    public void setPropertyIdentifier(final String propertyIdentifier) {
        this.propertyIdentifier = propertyIdentifier;
    }

    public Set<Integer> getValidSpecies() {
        return validSpecies;
    }

    public void setValidSpecies(final Set<Integer> validSpecies) {
        this.validSpecies = validSpecies;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }
}

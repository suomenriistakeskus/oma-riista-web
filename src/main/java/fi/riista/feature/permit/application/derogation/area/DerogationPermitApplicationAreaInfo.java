package fi.riista.feature.permit.application.derogation.area;

import fi.riista.feature.common.entity.GeoLocation;

public interface DerogationPermitApplicationAreaInfo {

    Integer getAreaSize();

    void setAreaSize(Integer size);

    GeoLocation getGeoLocation();

    void setGeoLocation(GeoLocation geoLocation);

    String getAreaDescription();

    void setAreaDescription(String areaDescription);

}

package fi.riista.feature.gis;

import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;

public class RhyNotResolvableByGeoLocationException extends RuntimeException {

    public static void assertNotNull(Riistanhoitoyhdistys rhyByLocation) {
        if (rhyByLocation == null) {
            throw new RhyNotResolvableByGeoLocationException();
        }
    }
}

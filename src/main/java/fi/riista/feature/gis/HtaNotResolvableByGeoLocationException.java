package fi.riista.feature.gis;

import fi.riista.feature.gis.hta.GISHirvitalousalue;

public class HtaNotResolvableByGeoLocationException extends RuntimeException {

    public static void assertNotNull(GISHirvitalousalue htaByLocation) {
        if (htaByLocation == null) {
            throw new HtaNotResolvableByGeoLocationException();
        }
    }
}

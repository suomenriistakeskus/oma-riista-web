package fi.riista.feature.gis.vector;

import com.vividsolutions.jts.geom.Envelope;

public class VectorTileUtil {

    private static final double EARTH_CIRCUMFERENCE = 40075017.0;

    public static boolean isValidTile(final int zoom, final int x, final int y) {
        return (zoom >= 0) && (zoom <= 19)
                && (x >= 0) && (y >= 0)
                && (x <= ((1 << zoom) - 1))
                && (y <= ((1 << zoom) - 1));
    }

    public static double tileResolution(final int zoom) {
        return EARTH_CIRCUMFERENCE / 256.0 / (1 << zoom);
    }

    public static Envelope tileEnvelope(final int zoom,
                                        final int x,
                                        final int y) {
        return new Envelope(
                tile2lon(x, zoom),
                tile2lon(x + 1, zoom),
                tile2lat(y + 1, zoom),
                tile2lat(y, zoom));
    }

    private static double tile2lon(int x, int z) {
        return x / (double) (1 << z) * 360.0 - 180.0;
    }

    private static double tile2lat(final int y, final int z) {
        final double n = Math.PI - (2.0 * Math.PI * y) / (double) (1 << z);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }

    private VectorTileUtil() {
        throw new AssertionError();
    }
}

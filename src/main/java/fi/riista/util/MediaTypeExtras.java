package fi.riista.util;

import org.springframework.http.MediaType;

public class MediaTypeExtras {

    public final static String TEXT_PLAIN_UTF8_VALUE = "text/plain;charset=UTF-8";
    public final static MediaType TEXT_PLAIN_UTF8 = MediaType.parseMediaType("text/plain;charset=UTF-8");

    // TODO: Is there already a constant for this?
    public final static MediaType APPLICATION_JSON_UTF8 = MediaType.parseMediaType("application/json;charset=utf-8");
    public static final String IMAGE_TIFF_VALUE = "image/tiff";
    public static final MediaType IMAGE_TIFF = MediaType.parseMediaType(IMAGE_TIFF_VALUE);

    public static final String APPLICATION_PDF_VALUE = "application/pdf";
    public static final MediaType APPLICATION_PDF = MediaType.parseMediaType(APPLICATION_PDF_VALUE);

    public static final String TEXT_CSV_VALUE = "text/csv";
    public static final MediaType TEXT_CSV = MediaType.parseMediaType(TEXT_CSV_VALUE);

    public static final String APPLICATION_GEOJSON_VALUE = "application/vnd.geo+json";
    public static final MediaType APPLICATION_GEOJSON = MediaType.parseMediaType(APPLICATION_GEOJSON_VALUE);

    public static final String APPLICATION_VECTOR_TILE_VALUE = "application/vnd.mapbox-vector-tile";
    public static final MediaType APPLICATION_VECTOR_TILE = MediaType.parseMediaType(APPLICATION_VECTOR_TILE_VALUE);

    public static final String APPLICATION_EXCEL_VALUE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final MediaType APPLICATION_EXCEL = MediaType.parseMediaType(APPLICATION_EXCEL_VALUE);

    public static final String APPLICATION_ZIP_VALUE = "application/zip";
    public static final MediaType APPLICATION_ZIP = MediaType.parseMediaType(APPLICATION_ZIP_VALUE);

    public static final String APPLICATION_WORD_VALUE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
}

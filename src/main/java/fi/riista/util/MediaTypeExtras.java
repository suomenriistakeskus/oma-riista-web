package fi.riista.util;

import org.springframework.http.MediaType;

public class MediaTypeExtras {

    public static final String IMAGE_TIFF_VALUE = "image/tiff";
    public static final MediaType IMAGE_TIFF = MediaType.parseMediaType(IMAGE_TIFF_VALUE);

    public static final String APPLICATION_PDF_VALUE = "application/pdf";
    public static final MediaType APPLICATION_PDF = MediaType.parseMediaType(APPLICATION_PDF_VALUE);

    public static final String TEXT_CSV_VALUE = "text/csv";
    public static final MediaType TEXT_CSV = MediaType.parseMediaType(TEXT_CSV_VALUE);

    public static final String APPLICATION_GEOJSON_VALUE = "application/vnd.geo+json";
    public static final MediaType APPLICATION_GEOJSON = MediaType.parseMediaType(APPLICATION_GEOJSON_VALUE);

    public static final String APPLICATION_EXCEL_VALUE = "application/vnd.ms-excel; charset=UTF-8";
    public static final MediaType APPLICATION_EXCEL = MediaType.parseMediaType(APPLICATION_EXCEL_VALUE);
}

package fi.riista.feature.gis.zip;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import fi.riista.feature.account.area.PersonalArea;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.util.DateUtil;
import fi.riista.util.NumberUtils;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.geojson.FeatureCollection;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.Objects.requireNonNull;

public class OmaRiistaAreaZipBuilder {
    private static final String FILENAME_GEOJSON = "area.json";
    private static final String FILENAME_METADATA = "README.txt";

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("d.M.yyyy HH:mm");

    private final ObjectMapper objectMapper;
    private FeatureCollection featureCollection;
    private String ownerName;
    private String areaName;
    private LocalDateTime saveDateTime;
    private Long areaSize;

    public OmaRiistaAreaZipBuilder(final ObjectMapper objectMapper) {
        this.objectMapper = requireNonNull(objectMapper);
    }

    public OmaRiistaAreaZipBuilder withMetadata(final HuntingClubArea huntingClubArea,
                                                final GISZone zone,
                                                final Locale locale) {
        final String ownerName = huntingClubArea.getClub().getNameLocalisation().getAnyTranslation(locale);
        final String areaName = huntingClubArea.getNameLocalisation().getAnyTranslation(locale);
        final LocalDateTime saveDateTime = DateUtil.toLocalDateTimeNullSafe(huntingClubArea.getModificationTime());
        final long areaSize = NumberUtils.squareMetersToHectares(zone.getComputedAreaSize());

        return withOwnerName(ownerName)
                .withAreaName(areaName)
                .withSaveDateTime(saveDateTime)
                .withAreaSize(areaSize);
    }

    public OmaRiistaAreaZipBuilder withMetadata(final PersonalArea personalArea,
                                                final GISZone zone,
                                                final Locale locale) {
        final String ownerName = personalArea.getPerson().getFullName();
        final String areaName = personalArea.getName();
        final LocalDateTime saveDateTime = DateUtil.toLocalDateTimeNullSafe(personalArea.getModificationTime());
        final long areaSize = NumberUtils.squareMetersToHectares(zone.getComputedAreaSize());

        return withOwnerName(ownerName)
                .withAreaName(areaName)
                .withSaveDateTime(saveDateTime)
                .withAreaSize(areaSize);
    }

    public OmaRiistaAreaZipBuilder withGeoJson(final FeatureCollection featureCollection) {
        this.featureCollection = requireNonNull(featureCollection);
        return this;
    }

    public OmaRiistaAreaZipBuilder withOwnerName(final String ownerName) {
        this.ownerName = requireNonNull(ownerName);
        return this;
    }

    public OmaRiistaAreaZipBuilder withAreaName(final String areaName) {
        this.areaName = requireNonNull(areaName);
        return this;
    }

    public OmaRiistaAreaZipBuilder withSaveDateTime(final LocalDateTime saveDateTime) {
        this.saveDateTime = requireNonNull(saveDateTime);
        return this;
    }

    public OmaRiistaAreaZipBuilder withAreaSize(final Long areaSize) {
        this.areaSize = requireNonNull(areaSize);
        return this;
    }

    public OmaRiistaAreaZip build() throws IOException {
        Preconditions.checkState(featureCollection != null
                && ownerName != null
                && areaName != null
                && saveDateTime != null
                && areaSize != null);

        return new OmaRiistaAreaZip(buildData(), String.format("%s - %s.zip", ownerName, areaName));
    }

    private byte[] buildData() throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try (final ZipOutputStream zip = new ZipOutputStream(bos, StandardCharsets.UTF_8)) {
            zip.setComment("Exported from oma.riista.fi on " + DTF.print(DateUtil.now()));
            zip.setLevel(9);
    
            appendMetadata(zip);
            appendGeoJson(zip);
    
            zip.flush();
        }

        return bos.toByteArray();
    }

    private void appendMetadata(final ZipOutputStream zip) throws IOException {
        final OutputStreamWriter mos = new OutputStreamWriter(zip, StandardCharsets.UTF_8);
        zip.putNextEntry(new ZipEntry(FILENAME_METADATA));
        mos.write(ownerName);
        mos.write('\n');
        mos.write(areaName);
        mos.write('\n');
        mos.write(String.format("%d ha", areaSize));
        mos.write('\n');
        mos.write(DTF.print(saveDateTime));
        mos.write('\n');
        mos.flush();
        zip.closeEntry();
    }

    private void appendGeoJson(final ZipOutputStream zip) throws IOException {
        final OutputStreamWriter writer = new OutputStreamWriter(new CloseShieldOutputStream(zip), StandardCharsets.UTF_8);
        zip.putNextEntry(new ZipEntry(FILENAME_GEOJSON));
        objectMapper.writeValue(writer, featureCollection);
        zip.closeEntry();
    }
}

package fi.riista.feature.huntingclub.area.excel;

import fi.riista.config.Constants;
import fi.riista.feature.common.entity.PropertyIdentifier;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.LocalisedString;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class HuntingClubAreaExcelView extends AbstractXlsView {
    private static final DateTimeFormatter DATETIME_PATTERN = DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm-ss");
    private static final String COLUMN_HEADER_PREFIX = "ClubAreaExportExcel.";
    private static final String[] COLUMN_HEADERS = new String[]{
            "propertyIdentifier",
            "propertyIdentifierPart1",
            "propertyIdentifierPart2",
            "propertyIdentifierPart3",
            "propertyIdentifierPart4",
            "palstaId",
            "propertyName",
            "propertySize",
            "propertyOriginalSize",
            "propertyIsChanged"
    };

    private final Locale locale;
    private final EnumLocaliser localiser;

    private final LocalisedString clubName;
    private final LocalisedString areaName;
    private final List<ExcelRow> rows;

    public HuntingClubAreaExcelView(final Locale locale,
                                    final EnumLocaliser localiser,
                                    final LocalisedString clubName,
                                    final LocalisedString areaName,
                                    final List<ExcelRow> rows) {
        this.locale = locale;
        this.localiser = Objects.requireNonNull(localiser);
        this.clubName = Objects.requireNonNull(clubName);
        this.areaName = Objects.requireNonNull(areaName);
        this.rows = Objects.requireNonNull(rows);
    }

    private String createFilename() {
        return String.format(
                "%s - %s-%s.xls",
                clubName.getAnyTranslation(locale),
                areaName.getAnyTranslation(locale),
                DATETIME_PATTERN.print(DateUtil.now()));
    }

    private String[] translate(String prefix, final String[] arr) {
        return Arrays.stream(arr)
                .map(key -> localiser.getTranslation(prefix + key))
                .toArray(String[]::new);
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> model,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {
        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        response.setHeader(ContentDispositionUtil.HEADER_NAME,
                ContentDispositionUtil.encodeAttachmentFilename(createFilename()));

        final ExcelHelper helper = new ExcelHelper(workbook);
        helper.appendHeaderRow(translate(COLUMN_HEADER_PREFIX, COLUMN_HEADERS));

        final String booleanTrue = localiser.getTranslation("Boolean.true");
        final String booleanFalse = localiser.getTranslation("Boolean.false");

        rows.forEach(feature -> {
            helper.appendRow()
                    .appendTextCell(feature.getPropertyIdentifier().getDelimitedValue())
                    .appendTextCell(feature.getPropertyIdentifier().getKuntanumero())
                    .appendTextCell(feature.getPropertyIdentifier().getSijaintialuenumero())
                    .appendTextCell(feature.getPropertyIdentifier().getRyhmanumero())
                    .appendTextCell(feature.getPropertyIdentifier().getYksikkonumero())
                    .appendNumberCell(feature.getPalstaId())
                    .appendTextCell(feature.getPropertyName())
                    .appendDoubleCell(feature.formatActualSize(), 2)
                    .appendDoubleCell(feature.formatOriginalSize(), 2)
                    .appendTextCell(feature.isChanged() ? booleanTrue : booleanFalse);
        });

        helper.autoSizeColumns();
    }

    public static class ExcelRow {
        private final Integer palstaId;
        private final String propertyName;
        private final Double originalSize;
        private final Double excludedSize;
        private final PropertyIdentifier propertyIdentifier;
        private final boolean changed;

        public ExcelRow(final Integer palstaId,
                        final PropertyIdentifier propertyIdentifier,
                        final String propertyName,
                        final Double originalSize,
                        final Double excludedSize,
                        final Boolean changed) {
            this.palstaId = Objects.requireNonNull(palstaId);
            this.propertyIdentifier = Objects.requireNonNull(propertyIdentifier);
            this.propertyName = propertyName;
            this.originalSize = Objects.requireNonNull(originalSize);
            this.excludedSize = Objects.requireNonNull(excludedSize);
            this.changed = Objects.requireNonNull(changed);
        }

        public Double formatActualSize() {
            return excludedSize > 1.0
                    ? (originalSize - excludedSize) / 10_000
                    : originalSize / 10_000;
        }

        public Double formatOriginalSize() {
            return excludedSize > 1.0 ? originalSize / 10_000 : null;
        }

        public Integer getPalstaId() {
            return palstaId;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public PropertyIdentifier getPropertyIdentifier() {
            return propertyIdentifier;
        }

        public boolean isChanged() {
            return changed;
        }
    }
}

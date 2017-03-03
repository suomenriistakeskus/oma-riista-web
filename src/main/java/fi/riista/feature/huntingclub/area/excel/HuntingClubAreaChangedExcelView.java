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

public class HuntingClubAreaChangedExcelView extends AbstractXlsView {
    private static final DateTimeFormatter DATETIME_PATTERN = DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm-ss");
    private static final String COLUMN_HEADER_PREFIX = "ClubAreaChangesExportExcel.";
    private static final String[] COLUMN_HEADERS = new String[]{
            "propertyIdentifier",
            "propertyIdentifierNew",
            "propertySize",
            "propertySizeNew"
    };

    private final Locale locale;
    private final EnumLocaliser localiser;

    private final LocalisedString clubName;
    private final LocalisedString areaName;
    private final List<ExcelRow> rows;

    public HuntingClubAreaChangedExcelView(final Locale locale,
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

        for (final ExcelRow f : rows) {
            helper.appendRow();

            helper.appendTextCell(f.getPropertyIdentifier().getDelimitedValue());

            if (f.getPropertyIdentifierNew() != null &&
                    !f.getPropertyIdentifierNew().equals(f.getPropertyIdentifier())) {
                helper.appendTextCell(f.getPropertyIdentifierNew().getDelimitedValue());
            } else {
                helper.appendTextCell("-").appendTextCell("-");
            }

            helper.appendDoubleCell(f.formatOriginalSize(), 2);
            helper.appendDoubleCell(f.formatNewSize(), 2);
        }

        helper.autoSizeColumns();
    }

    public static class ExcelRow {
        private final PropertyIdentifier propertyIdentifier;
        private final PropertyIdentifier propertyIdentifierNew;
        private final Double originalSize;
        private final Double areaSizeDifference;

        public ExcelRow(final Long propertyIdentifier,
                        final Long propertyIdentifierNew,
                        final Double originalSize,
                        final Double areaSizeDifference) {
            this.propertyIdentifier = PropertyIdentifier.create(Objects.requireNonNull(propertyIdentifier));
            this.propertyIdentifierNew = PropertyIdentifier.create(propertyIdentifierNew);
            this.originalSize = Objects.requireNonNull(originalSize);
            this.areaSizeDifference = areaSizeDifference;
        }

        public Double formatOriginalSize() {
            return originalSize / 10_000;
        }

        public Double formatNewSize() {
            return areaSizeDifference != null ? (originalSize + areaSizeDifference) / 10_000 : null;
        }

        public PropertyIdentifier getPropertyIdentifier() {
            return propertyIdentifier;
        }

        public PropertyIdentifier getPropertyIdentifierNew() {
            return propertyIdentifierNew;
        }
    }
}

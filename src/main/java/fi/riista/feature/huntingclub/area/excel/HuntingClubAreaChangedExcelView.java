package fi.riista.feature.huntingclub.area.excel;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.common.entity.PropertyIdentifier;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.LocalisedString;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class HuntingClubAreaChangedExcelView extends AbstractXlsxView {

    private static final String COLUMN_HEADER_PREFIX = "ClubAreaChangesExportExcel.";
    private static final String[] COLUMN_HEADERS = new String[] {
            "propertyIdentifier",
            "propertyName",
            "propertyIdentifierNew",
            "propertyNameNew",
            "propertySize",
            "propertySizeNew"
    };

    private final EnumLocaliser localiser;

    private final LocalisedString clubName;
    private final LocalisedString areaName;
    private final List<ExcelRow> rows;

    public HuntingClubAreaChangedExcelView(final EnumLocaliser localiser,
                                           final LocalisedString clubName,
                                           final LocalisedString areaName,
                                           final List<ExcelRow> rows) {

        this.localiser = requireNonNull(localiser);
        this.clubName = requireNonNull(clubName);
        this.areaName = requireNonNull(areaName);
        this.rows = requireNonNull(rows);
    }

    private String createFilename() {
        return String.format(
                "%s - %s-%s.xlsx",
                localiser.getTranslation(clubName),
                localiser.getTranslation(areaName),
                Constants.FILENAME_TS_PATTERN.print(DateUtil.now()));
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> model,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {

        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(response, createFilename());

        final ExcelHelper helper = new ExcelHelper(workbook)
                .appendHeaderRow(localiser.translate(COLUMN_HEADER_PREFIX, COLUMN_HEADERS));

        for (final ExcelRow f : rows) {
            helper.appendRow()
                    .appendTextCell(f.getPropertyIdentifier().getDelimitedValue())
                    .appendTextCell(f.getPropertyName());

            if (f.getPropertyIdentifierNew() != null &&
                    !f.getPropertyIdentifierNew().equals(f.getPropertyIdentifier())) {

                helper.appendTextCell(f.getPropertyIdentifierNew().getDelimitedValue());
                helper.appendTextCell(f.getPropertyNameNew());
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
        private final String propertyName;
        private final String propertyNameNew;
        private final Double originalSize;
        private final Double areaSizeDifference;

        public ExcelRow(final Long propertyIdentifier,
                        final Long propertyIdentifierNew,
                        final String propertyName,
                        final String propertyNameNew,
                        final Double originalSize,
                        final Double areaSizeDifference) {

            this.propertyIdentifier = PropertyIdentifier.create(requireNonNull(propertyIdentifier));
            this.propertyIdentifierNew = PropertyIdentifier.create(propertyIdentifierNew);
            this.propertyName = propertyName;
            this.propertyNameNew = propertyNameNew;
            this.originalSize = requireNonNull(originalSize);
            this.areaSizeDifference = areaSizeDifference;
        }

        public Double formatOriginalSize() {
            return originalSize / 10_000;
        }

        public Double formatNewSize() {
            return areaSizeDifference != null ? (originalSize + areaSizeDifference) / 10_000 : null;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public String getPropertyNameNew() {
            return propertyNameNew;
        }

        public PropertyIdentifier getPropertyIdentifier() {
            return propertyIdentifier;
        }

        public PropertyIdentifier getPropertyIdentifierNew() {
            return propertyIdentifierNew;
        }
    }
}

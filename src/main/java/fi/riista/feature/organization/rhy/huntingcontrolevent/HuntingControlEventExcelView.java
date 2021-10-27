package fi.riista.feature.organization.rhy.huntingcontrolevent;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.LocalisedString;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static fi.riista.util.DateUtil.now;

public class HuntingControlEventExcelView extends AbstractXlsxView {

    private ExcelHelper excelHelper;
    private EnumLocaliser localiser;
    private List<HuntingControlEventExportDTO> events;

    public HuntingControlEventExcelView(final EnumLocaliser localiser,
                                        final List<HuntingControlEventExportDTO> events) {
        this.localiser = localiser;
        this.events = events;
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> model,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {
        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(response, createFilename());

        createSheet(workbook);
    }

    private String createFilename() {
        return String.format("%s-%s.xlsx",
                localiser.getTranslation("HuntingControlEventExcelView.huntingControl"),
                Constants.FILENAME_TS_PATTERN.print(now()));
    }

    private void createSheet(final Workbook workbook) {
        excelHelper = new ExcelHelper(workbook);

        createHeaderRow();

        createDataRows();

        excelHelper.autoSizeColumns();
    }

    private void createHeaderRow() {
        final String HEADER_PREXIX = "HuntingControlEventExcelView.";
        final String[] HEADERS = new String[]{
                "rkaCode",
                "rkaName",
                "rhyCode",
                "rhyName",
                "title",
                "inspectorCount",
                "latitude",
                "longitude",
                "cooperationType",
                "wolfTerritory",
                "inspectors",
                "date",
                "beginTime",
                "endTime",
                "customers",
                "proofOrders",
                "description"
        };
        final List<String> headers = Arrays.asList(localiser.translate(HEADER_PREXIX, HEADERS));

        excelHelper.appendRow();
        headers.forEach(header -> excelHelper.appendTextCell(header));
    }

    private void createDataRows() {
        events.forEach(exportedEvent -> {
            final List<HuntingControlEventDTO> huntingControlEvents = exportedEvent.getEvents();

            huntingControlEvents.forEach(event -> {
                excelHelper.appendRow();

                final OrganisationNameDTO rka = exportedEvent.getRkaName();
                final OrganisationNameDTO rhy = exportedEvent.getRhyName();
                final GeoLocation location = event.getGeoLocation();
                excelHelper.appendTextCell(rka.getOfficialCode())
                        .appendTextCell(localiser.getTranslation(LocalisedString.of(rka.getNameFI(), rka.getNameSV())))
                        .appendTextCell(rhy.getOfficialCode())
                        .appendTextCell(localiser.getTranslation(LocalisedString.of(rhy.getNameFI(), rhy.getNameSV())))
                        .appendTextCell(event.getTitle())
                        .appendNumberCell(event.getInspectorCount())
                        .appendNumberCell(location.getLatitude())
                        .appendNumberCell(location.getLongitude())
                        .appendTextCell(localiser.getTranslation(event.getCooperationType()))
                        .appendTextCell(event.getWolfTerritory() ? "X" : "")
                        .appendTextCell(event.getInspectors())
                        .appendDateCell(event.getDate())
                        .appendTimeCell(DateUtil.toDateTimeNullSafe(event.getDate(), event.getBeginTime()).toDate())
                        .appendTimeCell(DateUtil.toDateTimeNullSafe(event.getDate(), event.getEndTime()).toDate())
                        .appendNumberCell(event.getCustomers())
                        .appendNumberCell(event.getProofOrders())
                        .appendTextCell(event.getDescription());
            });
        });
    }
}

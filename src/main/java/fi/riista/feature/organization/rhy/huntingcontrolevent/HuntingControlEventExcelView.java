package fi.riista.feature.organization.rhy.huntingcontrolevent;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.LocalisedEnum;
import fi.riista.util.LocalisedString;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventStatus.ACCEPTED_SUBSIDIZED;
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
                "status",
                "subsidized",
                "type",
                "title",
                "inspectorCount",
                "latitude",
                "longitude",
                "locationDescription",
                "cooperationType",
                "wolfTerritory",
                "inspectors",
                "otherParticipants",
                "date",
                "beginTime",
                "endTime",
                "duration",
                "customers",
                "proofOrders",
                "description"
        };
        final List<String> headers = Arrays.asList(localiser.translate(HEADER_PREXIX, HEADERS));

        excelHelper.appendRow();
        headers.forEach(header -> excelHelper.appendTextCellWrapping(header));
    }

    private void createDataRows() {
        events.forEach(exportedEvent -> {
            final List<HuntingControlEventDTO> huntingControlEvents = exportedEvent.getEvents();

            huntingControlEvents.forEach(event -> {
                excelHelper.appendRow();

                final OrganisationNameDTO rka = exportedEvent.getRkaName();
                final OrganisationNameDTO rhy = exportedEvent.getRhyName();
                final GeoLocation location = event.getGeoLocation();

                final HuntingControlEventStatus status = event.getStatus();
                final String statusKey = status == ACCEPTED_SUBSIDIZED ?
                        "HuntingControlEventExcelView.ACCEPTED" :
                        "HuntingControlEventExcelView." + status;

                final Duration duration =
                        DateUtil.toDuration(event.getDate(), event.getBeginTime(), event.getDate(), event.getEndTime());
                final PeriodFormatter formatter = new PeriodFormatterBuilder()
                        .printZeroAlways()
                        .minimumPrintedDigits(2)
                        .appendHours()
                        .appendLiteral(":")
                        .appendMinutes()
                        .toFormatter();

                excelHelper.appendTextCell(rka.getOfficialCode())
                        .appendTextCell(localiser.getTranslation(LocalisedString.of(rka.getNameFI(), rka.getNameSV())))
                        .appendTextCell(rhy.getOfficialCode())
                        .appendTextCell(localiser.getTranslation(LocalisedString.of(rhy.getNameFI(), rhy.getNameSV())))
                        .appendTextCell(localiser.getTranslation(statusKey))
                        .appendTextCell(status == ACCEPTED_SUBSIDIZED ? "X" : "")
                        .appendTextCell(localiser.getTranslation(event.getEventType()))
                        .appendTextCell(event.getTitle())
                        .appendNumberCell(event.getInspectorCount())
                        .appendNumberCell(location.getLatitude())
                        .appendNumberCell(location.getLongitude())
                        .appendTextCell(event.getLocationDescription())
                        .appendWrappedTextCell(localiseEnumList(event.getCooperationTypes()))
                        .appendTextCell(event.getWolfTerritory() ? "X" : "")
                        .appendWrappedTextCell(inspectorsToString(event.getInspectors()))
                        .appendTextCell(event.getOtherParticipants())
                        .appendDateCell(event.getDate())
                        .appendTimeCell(DateUtil.toDateTimeNullSafe(event.getDate(), event.getBeginTime()).toDate())
                        .appendTimeCell(DateUtil.toDateTimeNullSafe(event.getDate(), event.getEndTime()).toDate())
                        .appendTextCell(formatter.print(duration.toPeriod()))
                        .appendNumberCell(event.getCustomers())
                        .appendNumberCell(event.getProofOrders())
                        .appendTextCell(event.getDescription());
            });
        });
    }

    private <E extends Enum<E> & LocalisedEnum> String localiseEnumList(Set<E> enums) {
        return enums.stream().map(localiser::getTranslation).collect(Collectors.joining("\n"));
    }

    private String inspectorsToString(List<HuntingControlInspectorDTO> inspectors) {
        return inspectors.stream()
                .map(i -> String.format("%s %s", i.getFirstName(), i.getLastName()))
                .collect(Collectors.joining("\n"));
    }
}

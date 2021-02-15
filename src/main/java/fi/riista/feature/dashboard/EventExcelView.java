package fi.riista.feature.dashboard;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.calendar.CalendarEventSearchResultDTO;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EventExcelView extends AbstractXlsxView {

    private static final String LOCALISATION_PREFIX = "DashboardEventExcelView.";

    private final List<CalendarEventSearchResultDTO> events;
    private final int year;
    private final Map<Long, OrganisationNameDTO> organisationMap;
    private final Map<Long, String> venueNameMap;
    private final Map<Long, String> venueAddressMap;
    private final EnumLocaliser localiser;

    public EventExcelView(final List<CalendarEventSearchResultDTO> events,
                          final int year,
                          final Map<Long, OrganisationNameDTO> organisationMap,
                          final Map<Long, String> venueNameMap,
                          final Map<Long, String> venueAddressMap,
                          final EnumLocaliser localiser) {
        this.events = events;
        this.year = year;
        this.organisationMap = organisationMap;
        this.venueNameMap = venueNameMap;
        this.venueAddressMap = venueAddressMap;
        this.localiser = localiser;
    }

    private String createFilename() {
        final String timestamp = Constants.FILENAME_TS_PATTERN.print(DateUtil.now());
        return "tapahtumat-" + year + "-" + timestamp + ".xlsx";
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> model,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {

        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(response, createFilename());

        final ExcelHelper helper = new ExcelHelper(workbook);

        helper.appendHeaderRow(localiser.translate(LOCALISATION_PREFIX, new String[] {
                "date",
                "beginTime",
                "endTime",
                "rhy",
                "rhyNumber",
                "eventType",
                "name",
                "venueName",
                "venueAddress",
                "description"
        }));

        events.forEach(event -> {
            final OrganisationNameDTO organisationNameDTO = organisationMap.get(event.getOrganisationId());
            final long venueId = event.getVenueId();

            helper.appendRow()
                    .appendDateCell(event.getDate())
                    .appendTimeCell(event.getBeginTime().toDateTimeToday().toDate())
                    .appendTimeCell(Optional.ofNullable(event.getEndTime()).map(endTime -> endTime.toDateTimeToday().toDate()).orElse(null))
                    .appendTextCell(localiser.getTranslation(organisationNameDTO.getNameLocalisation()))
                    .appendTextCell(organisationNameDTO.getOfficialCode())
                    .appendTextCell(localiser.getTranslation(event.getCalendarEventType()))
                    .appendTextCell(event.getName())
                    .appendTextCell(venueNameMap.get(venueId))
                    .appendTextCell(venueAddressMap.get(venueId))
                    .appendTextCell(event.getDescription());
        });

        helper.autoSizeColumns();
    }
}

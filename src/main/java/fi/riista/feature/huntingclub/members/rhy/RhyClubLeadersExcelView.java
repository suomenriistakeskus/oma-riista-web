package fi.riista.feature.huntingclub.members.rhy;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.occupation.OccupationType;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

public class RhyClubLeadersExcelView extends AbstractXlsView {

    private static final DateTimeFormatter DATETIME_PATTERN = DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm-ss");

    private static final String HEADER_PREFIX = "RhyClubLeadersExcel.";
    private static final String[] HEADERS_CONTACTS = new String[]{"club", "name", "primary", "email", "phoneNumber"};
    private static final String[] HEADERS_LEADERS = new String[]{"club", "group", "name", "primary", "email", "phoneNumber"};

    private final EnumLocaliser localiser;
    private final Locale locale;
    private final LocalisedString rhyName;
    private final List<RhyClubOccupationDTO> contacts;
    private final List<RhyClubOccupationDTO> leaders;

    public RhyClubLeadersExcelView(final EnumLocaliser localiser,
                                   final Locale locale,
                                   final LocalisedString rhyName,
                                   final List<RhyClubOccupationDTO> contacts,
                                   final List<RhyClubOccupationDTO> leaders) {
        this.localiser = localiser;
        this.locale = locale;
        this.rhyName = rhyName;
        this.contacts = contacts;
        this.leaders = leaders;
    }

    private String createFilename() {
        return String.format(
                "Seurajohtajat_%s_%s.xls",
                rhyName.getAnyTranslation(locale),
                DATETIME_PATTERN.print(DateUtil.now()));
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> map,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {

        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        response.setHeader(ContentDispositionUtil.HEADER_NAME, ContentDispositionUtil.encodeAttachmentFilename(createFilename()));

        createSheet(workbook, OccupationType.SEURAN_YHDYSHENKILO, contacts, HEADERS_CONTACTS);
        createSheet(workbook, OccupationType.RYHMAN_METSASTYKSENJOHTAJA, leaders, HEADERS_LEADERS);
    }

    private void createSheet(final Workbook workbook,
                             final OccupationType occType,
                             final List<RhyClubOccupationDTO> rhyOccupations,
                             final String[] headers) {

        final ExcelHelper helper = new ExcelHelper(workbook, localiser.getTranslation(occType));
        helper.appendHeaderRow(createHeaderRow(headers));

        for (RhyClubOccupationDTO dto : rhyOccupations) {
            createRow(helper, dto, occType);
        }

        helper.autoSizeColumns();
    }

    private void createRow(final ExcelHelper helper, final RhyClubOccupationDTO dto, OccupationType occType) {
        helper.appendRow();
        helper.appendTextCell(dto.getClub().getNameLocalisation().getAnyTranslation(locale));
        if (occType == OccupationType.RYHMAN_METSASTYKSENJOHTAJA) {
            helper.appendTextCell(dto.getGroup().getNameLocalisation().getAnyTranslation(locale));
        }
        helper.appendTextCell(dto.getLastName() + " " + dto.getFirstName());
        helper.appendTextCell(dto.getCallOrder() != null && dto.getCallOrder() == 0 ? "x" : "");
        helper.appendTextCell(dto.getEmail());
        helper.appendTextCell(dto.getPhoneNumber());
    }

    private String[] createHeaderRow(String[] headers) {
        return Stream.of(headers)
                .map(key -> localiser.getTranslation(HEADER_PREFIX + key))
                .toArray(String[]::new);
    }
}

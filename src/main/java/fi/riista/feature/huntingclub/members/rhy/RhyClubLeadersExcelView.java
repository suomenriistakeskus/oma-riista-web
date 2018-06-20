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
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public class RhyClubLeadersExcelView extends AbstractXlsxView {

    private static final String HEADER_PREFIX = "RhyClubLeadersExcel.";
    private static final String[] HEADERS_CONTACTS = new String[]{"club", "name", "primary", "email", "phoneNumber"};
    private static final String[] HEADERS_LEADERS = new String[]{"club", "group", "name", "primary", "email", "phoneNumber"};

    private final EnumLocaliser localiser;
    private final int huntingYear;
    private final LocalisedString rhyName;
    private final List<RhyClubOccupationDTO> contacts;
    private final List<RhyClubOccupationDTO> leaders;

    public RhyClubLeadersExcelView(final EnumLocaliser localiser,
                                   final int huntingYear,
                                   final LocalisedString rhyName,
                                   final List<RhyClubOccupationDTO> contacts,
                                   final List<RhyClubOccupationDTO> leaders) {

        this.localiser = localiser;
        this.huntingYear = huntingYear;
        this.rhyName = rhyName;
        this.contacts = contacts;
        this.leaders = leaders;
    }

    private String createFilename() {
        return String.format(
                "Seurajohtajat_%s_%s_%s.xlsx",
                huntingYear,
                localiser.getTranslation(rhyName),
                Constants.FILENAME_TS_PATTERN.print(DateUtil.now()));
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> map,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {

        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(response, createFilename());

        createSheet(workbook, OccupationType.SEURAN_YHDYSHENKILO, contacts, HEADERS_CONTACTS);
        createSheet(workbook, OccupationType.RYHMAN_METSASTYKSENJOHTAJA, leaders, HEADERS_LEADERS);
    }

    private void createSheet(final Workbook workbook,
                             final OccupationType occType,
                             final List<RhyClubOccupationDTO> rhyOccupations,
                             final String[] headers) {

        final String sheetName = localiser.getTranslation(occType)
                + (occType == OccupationType.RYHMAN_METSASTYKSENJOHTAJA ? " " + huntingYear : "");

        final ExcelHelper helper = new ExcelHelper(workbook, sheetName)
                .appendHeaderRow(localiser.translate(HEADER_PREFIX, headers));

        for (final RhyClubOccupationDTO dto : rhyOccupations) {
            createRow(helper, dto, occType);
        }

        helper.autoSizeColumns();
    }

    private void createRow(final ExcelHelper helper, final RhyClubOccupationDTO dto, OccupationType occType) {
        helper.appendRow()
                .appendTextCell(localiser.getTranslation(dto.getClub().getNameLocalisation()));

        if (occType == OccupationType.RYHMAN_METSASTYKSENJOHTAJA) {
            helper.appendTextCell(localiser.getTranslation(dto.getGroup().getNameLocalisation()));
        }

        helper.appendTextCell(dto.getLastName() + " " + dto.getFirstName())
                .appendTextCell(dto.getCallOrder() != null && dto.getCallOrder() == 0 ? "x" : "")
                .appendTextCell(dto.getEmail())
                .appendTextCell(dto.getPhoneNumber());
    }
}

package fi.riista.feature.huntingclub.members.excel;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.LocalisedString;
import fi.riista.util.Localiser;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Excel is used to export members list from the club members section
 */
public class HuntingClubMembersExportView extends AbstractXlsxView {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd");

    private final String filename;
    private final List<HuntingClubMemberRowDTO> rows;

    private ExcelHelper excelHelper;
    private final EnumLocaliser localiser;

    public HuntingClubMembersExportView(final EnumLocaliser localiser,
                                        final HuntingClub club,
                                        final List<HuntingClubMemberRowDTO> rows) {
        this.localiser = localiser;
        this.filename = createFilename(club);
        this.rows = rows;
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> model,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {
        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(response, filename);

        excelHelper = new ExcelHelper(workbook, "members");
        createHeaderRow();

        for (final HuntingClubMemberRowDTO dto : rows) {
            excelHelper.appendRow()
                    .appendTextCell(Localiser.select(dto.getClubName()))
                    .appendTextCell(dto.getLastName())
                    .appendTextCell(dto.getFirstName())
                    .appendTextCell(dto.getHunterNumber())
                    .appendTextCell(dto.getStreetAddress())
                    .appendTextCell(dto.getPostalCode())
                    .appendTextCell(dto.getCity())
                    .appendTextCell(dto.getCountry())
                    .appendTextCell(dto.getPhoneNumber())
                    .appendTextCell(dto.getEmail())
                    .appendTextCell(Localiser.select(dto.getHomeMunicipalityName()));
        }

        excelHelper.autoSizeColumns();
    }

    private void createHeaderRow() {
        final String HEADER_PREXIX = "HuntingClubMembersExportView.";

        final String[] HEADERS = new String[]{
                "huntingClub",
                "lastName",
                "firstName",
                "hunterNumber",
                "address",
                "postalcode",
                "city",
                "country",
                "phoneNumber",
                "email",
                "municipality"
        };

        final List<String> headers = Arrays.asList(localiser.translate(HEADER_PREXIX, HEADERS));

        excelHelper.appendRow();
        headers.forEach(header -> excelHelper.appendTextCell(header));
    }

    private String createFilename(final HuntingClub exportedClub) {
        return ContentDispositionUtil.cleanFileName(String.format("%s-%s.xlsx",
                Localiser.select(new LocalisedString(exportedClub.getNameFinnish(), exportedClub.getNameSwedish())),
                DATE_FORMAT.print(DateUtil.today())));
    }
}

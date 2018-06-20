package fi.riista.feature.huntingclub.group.excel;

import fi.riista.config.Constants;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Excel is used to report shooter list to Metsähallitus
 */
public class HuntingClubGroupMemberExportView extends AbstractXlsxView {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd");

    private static final String[] HEADER = new String[]{
            "Seuran nimi",
            "Ryhmän metsästysvuosi",
            "Ryhmän eläinlaji",
            "Ryhmän nimi",
            "Sukunimi",
            "Etunimi",
            "Metsästäjänumero",
            "Ampumakokeen suorituspvm",
            "Katuosoite",
            "Postinumero",
            "Kaupunki",
            "Maa",
            "Puhelinnumero",
            "Sähköposti",
            "Kotikunta",
            "Kuuluuko ampuja jäsenenä muuhun hirveä metsästävään seuraan/seurueeseen, joka hakee pyyntilupaa? (Ei, Kyllä)",
            "(Jos kyllä) Muun seuran nimi ja RHY:n alue",
            "Metsästääkö ampuja hirveä muussa seurassa/seurueessa tulevana metsästyskautena? (Ei, Kyllä)"
    };

    private final String filename;
    private final List<HuntingClubGroupMemberRowDTO> rows;

    public HuntingClubGroupMemberExportView(final HuntingClub club, final List<HuntingClubGroupMemberRowDTO> rows) {
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

        final ExcelHelper helper = new ExcelHelper(workbook, "groups");
        helper.appendHeaderRow(HEADER);

        for (final HuntingClubGroupMemberRowDTO dto : rows) {
            helper.appendRow()
                    .appendTextCell(dto.getClubName())
                    .appendNumberCell(dto.getYear())
                    .appendTextCell(dto.getSpeciesName())
                    .appendTextCell(dto.getGroupName())
                    .appendTextCell(dto.getLastName())
                    .appendTextCell(dto.getFirstName())
                    .appendTextCell(dto.getHunterNumber())
                    // placeholder for passed shooting test date
                    .appendEmptyCell(1)
                    .appendTextCell(dto.getStreetAddress())
                    .appendTextCell(dto.getPostalCode())
                    .appendTextCell(dto.getCity())
                    .appendTextCell(dto.getCountry())
                    .appendTextCell(dto.getPhoneNumber())
                    .appendTextCell(dto.getEmail())
                    .appendTextCell(dto.getHomeMunicipalityName())
                    // placeholders for user-filled data
                    .appendEmptyCell(3);
        }

        helper.autoSizeColumns();
    }

    private static String createFilename(final HuntingClub exportedClub) {
        return ContentDispositionUtil.cleanFileName(String.format("%s-%s.xlsx",
                exportedClub.getNameFinnish(),
                DATE_FORMAT.print(DateUtil.today())));
    }
}

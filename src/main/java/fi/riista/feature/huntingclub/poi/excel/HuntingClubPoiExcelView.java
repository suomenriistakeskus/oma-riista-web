package fi.riista.feature.huntingclub.poi.excel;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.huntingclub.poi.PoiLocationDTO;
import fi.riista.feature.huntingclub.poi.PoiLocationGroupDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparing;

public class HuntingClubPoiExcelView extends AbstractXlsxView {

    private final OrganisationNameDTO club;

    private final List<PoiLocationGroupDTO> pois;

    private final EnumLocaliser i18n;

    private static final List<String> HEADERS = Arrays.asList(
            "poiId",
            "poiDescription",
            "poiType",
            "locationId",
            "locationDescription",
            "latitude",
            "longitude"
    );

    public HuntingClubPoiExcelView(final ClubPoiExcelDTO dto, final EnumLocaliser i18n) {
        this.club = dto.getClub();
        this.pois = dto.getPois();
        this.i18n = i18n;
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> map,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) throws Exception {

        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(response, createFilename());

        final ExcelHelper helper = new ExcelHelper(workbook);

        helper.appendRow().appendTextCellBold(i18n.getTranslation(club.getNameLocalisation()));

        helper.appendRow().appendRow();

        helper.appendHeaderRow(i18n.translateWithPrefix("HuntingClubPoiExcelView.", HEADERS))
                .applyBordersToCurrentRow();

        pois.stream()
                .sorted(comparing(PoiLocationGroupDTO::getVisibleId))
                .forEach(poi -> {
                    final List<PoiLocationDTO> locations = poi.getLocations();
                    if (locations.isEmpty()) {
                        insertPoiCommonInfo(helper, poi);
                    } else {
                        locations.stream()
                                .sorted(comparing(PoiLocationDTO::getVisibleId))
                                .forEach(location -> {
                                    insertPoiCommonInfo(helper, poi);
                                    appendLocation(helper, poi.getVisibleId(), location);
                                });
                    }
                });
        
        helper.withFreezedRows(5);

        helper.autoSizeColumns();
    }

    private void insertPoiCommonInfo(final ExcelHelper helper, final PoiLocationGroupDTO poi) {
        helper.appendRow()
                .appendTextCell(String.valueOf(poi.getVisibleId()))
                .appendTextCell(poi.getDescription())
                .appendTextCell(i18n.getTranslation(poi.getType()));
    }

    private void appendLocation(final ExcelHelper helper, final int poiId, final PoiLocationDTO location) {
        helper.appendTextCell(String.format("%s-%s", poiId, location.getVisibleId()))
                .appendTextCell(location.getDescription())
                .appendNumberCell(location.getGeoLocation().getLatitude())
                .appendNumberCell(location.getGeoLocation().getLongitude());
    }

    private String createFilename() {
        return String.format("%s-%s.xlsx",
                i18n.getTranslation(club.getNameLocalisation()),
                Constants.FILENAME_TS_PATTERN.print(DateUtil.now()));
    }
}

package fi.riista.feature.dashboard;

import fi.riista.config.Constants;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static fi.riista.util.DateUtil.now;

public class DashboardDeerPilotMemberExcelView extends AbstractXlsxView {

    private final List<DashboardDeerPilotMemberDTO> members;

    public DashboardDeerPilotMemberExcelView(final List<DashboardDeerPilotMemberDTO> members) {


        this.members = members;
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> map,
                                      final Workbook workbook,
                                      final HttpServletRequest httpServletRequest,
                                      final HttpServletResponse httpServletResponse) throws Exception {
        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        httpServletResponse.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(httpServletResponse, createFilename());

        final ExcelHelper helper = new ExcelHelper(workbook).appendRow()
                .appendTextCell("Metsästäjä")
                .appendTextCell("Ikä")
                .appendTextCell("Havaintoja")
                .appendTextCell("Saaliita");

        final AtomicInteger counter =  new AtomicInteger(1);

        members.forEach(member -> {
            helper.appendRow()
                    .appendTextCell("Metsästäjä " + counter.getAndIncrement())
                    .appendNumberCell(member.getAge())
                    .appendNumberCell(member.getObservationCount())
                    .appendNumberCell(member.getHarvestCount());
        });

    }

    private static String createFilename() {
        return String.format("%s-%s.xlsx", "pilottitilasto", Constants.FILENAME_TS_PATTERN.print(now()));
    }
}

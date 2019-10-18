package fi.riista.feature.permitplanning.hirvityvitys;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.permitplanning.hirvityvitys.dto.JyvitysExcelApplicationDTO;
import fi.riista.feature.permitplanning.hirvityvitys.dto.JyvitysExcelRhyDTO;
import fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryPostProcessing;
import fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryTemplate;
import fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoTemplate;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static fi.riista.util.DateUtil.now;

public class JyvitysExcelView extends AbstractXlsxView {

    private final EnumLocaliser i18n;

    private final JyvitysExcelRhyDTO rhyDTO;

    private final List<JyvitysExcelApplicationDTO> applicationDTOList;

    public JyvitysExcelView(EnumLocaliser i18n, JyvitysExcelRhyDTO rhyDTO, List<JyvitysExcelApplicationDTO> applicationDTOList) {
        this.i18n = i18n;
        this.rhyDTO = rhyDTO;
        this.applicationDTOList = applicationDTOList;
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> model,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) throws Exception {
        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        ContentDispositionUtil.addHeader(response, constructFilename());

        final JyvitysExcelSummaryTemplate summaryTemplate = new JyvitysExcelSummaryTemplate(workbook, i18n, rhyDTO, applicationDTOList.size());
        summaryTemplate.initializeStaticContent();

        final List<JyvitysExcelVerotuslohkoTemplate> verotuslohkoTemplates = rhyDTO.getVerotuslohkoDTOs().stream()
                .map(lohkoDTO -> new JyvitysExcelVerotuslohkoTemplate(workbook, i18n, lohkoDTO, applicationDTOList.size()))
                .map(JyvitysExcelVerotuslohkoTemplate::initializeStaticContent)
                .collect(Collectors.toList());
        if (!applicationDTOList.isEmpty()) {

            summaryTemplate
                    .applyFormulas()
                    .applyApplicationData(applicationDTOList)
                    .applyStyle();
            verotuslohkoTemplates.forEach(lohko -> lohko
                    .applyFormulas()
                    .applyApplicationData(applicationDTOList)
                    .applyStyles(applicationDTOList.size()));

            JyvitysExcelSummaryPostProcessing.apply(workbook, i18n, rhyDTO.getVerotuslohkoDTOs(), applicationDTOList.size());
        }

    }

    private String constructFilename() {
        return String.format("%s-hirvijyvitys-%s.xlsx", i18n.getTranslation(rhyDTO.getName()), Constants.FILENAME_TS_PATTERN.print(now()));
    }
}

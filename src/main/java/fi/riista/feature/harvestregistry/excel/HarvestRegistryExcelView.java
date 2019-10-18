package fi.riista.feature.harvestregistry.excel;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.harvestregistry.HarvestRegistryItemDTO;
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

public class HarvestRegistryExcelView extends AbstractXlsxView {

    private static final String LOCALISATION_PREFIX = "HarvestRegistryExcelView.";
    private final String filename;
    private EnumLocaliser localiser;
    private final List<HarvestRegistryItemDTO> dtos;

    public HarvestRegistryExcelView(final EnumLocaliser localiser,
                                    final List<HarvestRegistryItemDTO> dtos) {
        this.localiser = localiser;
        this.dtos = dtos;
        this.filename = String.format("%s - %s.xlsx",
                localiser.getTranslation(LOCALISATION_PREFIX + "title"),
                Constants.FILENAME_TS_PATTERN.print(DateUtil.now()));
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> model,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) throws Exception {

        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(response, filename);

        final ExcelHelper helper = new ExcelHelper(workbook, localiser.getTranslation(LOCALISATION_PREFIX + "title"));

        helper.appendHeaderRow(
                translate(new String[]{
                        "shooterName",
                        "shooterHunterNumber",
                        "shooterContactInformation",
                        "pointOfTime",
                        "timeOfDay",
                        "species",
                        "amount",
                        "age",
                        "gender",
                        "weight",
                        "municipality",
                        "latitude",
                        "longitude",
                        "harvestArea",
                        "rka",
                        "rhy"}));

        dtos.forEach(dto -> {
            helper.appendRow();
            helper.appendTextCell(dto.getShooterName());
            helper.appendTextCell(dto.getShooterHunterNumber());
            if (dto.getShooterAddress() == null) {
                helper.appendEmptyCell(1);
            } else {
                helper.appendWrappedTextCell(dto.getShooterAddress().formatToString());
            }
            helper.appendTextCell(dto.getPointOfTime().toString("dd.MM.yyyy"));
            helper.appendTextCell(dto.getPointOfTime().toString("HH.mm"));
            helper.appendTextCell(localiser.getTranslation(dto.getSpecies()));
            helper.appendNumberCell(dto.getAmount());
            helper.appendTextCell(localiser.getTranslation(dto.getAge()));
            helper.appendTextCell(localiser.getTranslation(dto.getGender()));
            helper.appendTextCell(Optional.ofNullable(dto.getWeight()).map(w -> w + " kg").orElse(""));
            helper.appendTextCell(localiser.getTranslation(dto.getMunicipality()));
            if (dto.getGeoLocation() == null) {
                helper.appendEmptyCell(2);
            } else {
                helper.appendNumberCell(dto.getGeoLocation().getLatitude());
                helper.appendNumberCell(dto.getGeoLocation().getLongitude());
            }
            helper.appendTextCell(localiser.getTranslation(dto.getHarvestArea()));
            helper.appendTextCell(localiser.getTranslation(dto.getRka()));
            helper.appendTextCell(localiser.getTranslation(dto.getRhy()));
        });

    }

    private String[] translate(final String[] list) {
        return localiser.translate(LOCALISATION_PREFIX, list);
    }
}

package fi.riista.feature.harvestregistry.excel;

import com.google.common.collect.ImmutableList;
import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.harvestregistry.HarvestRegistryItemDTO;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public class HarvestRegistryExcelView extends AbstractXlsxView {

    private static final String LOCALISATION_PREFIX = "HarvestRegistryExcelView.";
    private static final List<String> ALL_HEADERS = ImmutableList.of(
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
            "rhy");

    private static final List<String> EXCLUDED_FROM_COMMON = ImmutableList.of(
            "shooterName",
            "shooterHunterNumber",
            "shooterContactInformation",
            "harvestArea",
            "rka",
            "rhy");

    private static final List<String> EXCLUDED_FROM_COMMON_WITH_SHOOTER = ImmutableList.of(
            "shooterContactInformation",
            "harvestArea",
            "rka",
            "rhy");

    private final String filename;
    private final HarvestRegistryItemDTO.Fields includedFields;
    private final EnumLocaliser localiser;
    private final List<HarvestRegistryItemDTO> dtos;

    public HarvestRegistryExcelView(@Nonnull final EnumLocaliser localiser,
                                    @Nonnull final List<HarvestRegistryItemDTO> dtos,
                                    @Nonnull final HarvestRegistryItemDTO.Fields includedFields) {
        this.localiser = requireNonNull(localiser);
        this.dtos = requireNonNull(dtos);
        this.filename = String.format("%s - %s.xlsx",
                localiser.getTranslation(LOCALISATION_PREFIX + "title"),
                Constants.FILENAME_TS_PATTERN.print(DateUtil.now()));

        this.includedFields = requireNonNull(includedFields);
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


        final List<String> headers = calculateHeaders();

        helper.appendHeaderRow(localiser.translateWithPrefix(LOCALISATION_PREFIX, headers));

        dtos.forEach(dto -> {
            helper.appendRow();

            if (includedFields == HarvestRegistryItemDTO.Fields.FULL ||
                    includedFields == HarvestRegistryItemDTO.Fields.COMMON_WITH_SHOOTER) {
                helper.appendTextCell(dto.getShooterName());
                helper.appendTextCell(dto.getShooterHunterNumber());
            }

            if (includedFields == HarvestRegistryItemDTO.Fields.FULL) {
                if (dto.getShooterAddress() == null) {
                    helper.appendEmptyCell(1);
                } else {
                    helper.appendWrappedTextCell(dto.getShooterAddress().formatToString());
                }
            }

            helper.appendTextCell(dto.getDate().toString("dd.MM.yyyy"));
            helper.appendTextCell(ofNullable(dto.getTime()).map(time -> time.toString("HH.mm")).orElse(null));
            helper.appendTextCell(localiser.getTranslation(dto.getSpecies()));
            helper.appendNumberCell(dto.getAmount());
            helper.appendTextCell(localiser.getTranslation(dto.getAge()));
            helper.appendTextCell(localiser.getTranslation(dto.getGender()));
            helper.appendTextCell(ofNullable(dto.getWeight()).map(w -> w + " kg").orElse(""));
            helper.appendTextCell(localiser.getTranslation(dto.getMunicipality()));

            if (dto.getGeoLocation() == null) {
                helper.appendEmptyCell(2);
            } else {
                helper.appendNumberCell(dto.getGeoLocation().getLatitude());
                helper.appendNumberCell(dto.getGeoLocation().getLongitude());
            }

            if (includedFields == HarvestRegistryItemDTO.Fields.FULL) {
                helper.appendTextCell(localiser.getTranslation(dto.getHarvestArea()));
                helper.appendTextCell(localiser.getTranslation(dto.getRka()));
                helper.appendTextCell(localiser.getTranslation(dto.getRhy()));
            }
        });

    }

    private List<String> calculateHeaders() {
        if (includedFields == HarvestRegistryItemDTO.Fields.FULL) {
            return ALL_HEADERS;
        }
        final List<String> filteredHeaders = includedFields == HarvestRegistryItemDTO.Fields.COMMON
                ? EXCLUDED_FROM_COMMON
                : EXCLUDED_FROM_COMMON_WITH_SHOOTER;

        return ALL_HEADERS.stream()
                .filter(header -> !filteredHeaders.contains(header))
                .collect(toList());
    }

}

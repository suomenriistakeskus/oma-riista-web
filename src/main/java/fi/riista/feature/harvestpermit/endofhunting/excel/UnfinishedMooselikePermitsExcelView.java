package fi.riista.feature.harvestpermit.endofhunting.excel;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static fi.riista.util.DateUtil.now;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class UnfinishedMooselikePermitsExcelView extends AbstractXlsxView {

    private final int huntingYear;
    private final EnumLocaliser localiser;
    private final List<UnfinishedMooselikePermitDTO> data;

    public UnfinishedMooselikePermitsExcelView(final int huntingYear,
                                               @Nonnull final EnumLocaliser localiser,
                                               @Nonnull final List<UnfinishedMooselikePermitDTO> data) {

        this.huntingYear = huntingYear;
        this.localiser = requireNonNull(localiser);
        this.data = requireNonNull(data);
    }

    private String createFilename() {
        return format("%d_avoimet_hirvieläinluvat-%s.xlsx", huntingYear, Constants.FILENAME_TS_PATTERN.print(now()));
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> map,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {

        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(response, createFilename());

        createSheet(workbook);
    }

    private void createSheet(final Workbook workbook) {
        final ExcelHelper sheetWrapper = new ExcelHelper(workbook)
                // Freeze header row.
                .withFreezedRows(1)
                .appendHeaderRow(getHeaders());

        for (final UnfinishedMooselikePermitDTO dto : data) {
            final PersonContactInfoDTO person = dto.getOriginalContactPerson();
            final AddressDTO address = person.getAddress();

            sheetWrapper
                    .appendRow()
                    .appendTextCell(dto.getPermitNumber())
                    .appendTextCell(localiser.getTranslation(dto.getSpeciesName()))
                    .appendTextCell(dto.getPermitHolderCustomerNumber())
                    .appendTextCell(dto.getPermitHolderName())
                    .appendTextCell(localiser.getTranslation(dto.getRhyName()))
                    .appendTextCell(person.getFirstName())
                    .appendTextCell(person.getLastName())
                    .appendTextCell(person.getHunterNumber())
                    .appendTextCell(person.getPhoneNumber())
                    .appendTextCell(person.getEmail())
                    .appendTextCell(address.getStreetAddress())
                    .appendTextCell(address.getPostalCode())
                    .appendTextCell(address.getCity())
                    .appendTextCell(address.getCountry());
        }

        sheetWrapper.autoSizeColumns();
    }

    private String[] getHeaders() {
        return Arrays
                .stream(UnfinishedMooselikePermitsExcelTitle.values())
                .map(localiser::getTranslation)
                .toArray(String[]::new);
    }
}

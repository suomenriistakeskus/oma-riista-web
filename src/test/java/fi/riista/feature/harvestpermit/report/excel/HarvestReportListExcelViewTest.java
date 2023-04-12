package fi.riista.feature.harvestpermit.report.excel;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.DefaultEntitySupplierProvider;
import fi.riista.util.Locales;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicBoolean;

import static fi.riista.util.DateUtil.now;
import static org.junit.Assert.assertEquals;

public class HarvestReportListExcelViewTest implements DefaultEntitySupplierProvider {

    private ResourceBundleMessageSource messageSource;
    private EnumLocaliser enumLocaliser;
    private XSSFWorkbook workbook;
    private Person author;
    private Person shooter;
    private Harvest harvest;

    @Before
    public void setup() {
        LocaleContextHolder.setLocale(Locales.FI);
        messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("i18n/messages");
        enumLocaliser = new EnumLocaliser(messageSource);
        workbook = new XSSFWorkbook();

        author = getEntitySupplier().newPerson();
        shooter = getEntitySupplier().newPerson();
        harvest = getEntitySupplier().newHarvest(author, shooter);
        harvest.setHarvestReportDate(now());
        harvest.setHarvestReportAuthor(author);
    }

    @Test
    public void testModerator() {
        final HarvestReportListExcelView view =
                HarvestReportListExcelView.create(enumLocaliser, ImmutableList.of(createDTO()), true);
        view.buildForTest(workbook, new MockHttpServletResponse());

        assertEquals(1, workbook.getNumberOfSheets());
        final XSSFSheet sheet = workbook.getSheetAt(0);
        final XSSFRow headerRow = sheet.getRow(0);

        assertEquals(HarvestReportListExcelView.HEADER_LOCALIZATION_KEYS_DETAILS.length, headerRow.getLastCellNum());
        assertContainsDetails(sheet, true);
    }

    @Test
    public void testCoordinator() {
        final HarvestReportListExcelView view =
                HarvestReportListExcelView.create(enumLocaliser, ImmutableList.of(createDTO()), false);
        view.buildForTest(workbook, new MockHttpServletResponse());

        assertEquals(1, workbook.getNumberOfSheets());
        final XSSFSheet sheet = workbook.getSheetAt(0);
        final XSSFRow headerRow = sheet.getRow(0);

        assertEquals(HarvestReportListExcelView.HEADER_LOCALIZATION_KEYS.length, headerRow.getLastCellNum());
        assertContainsDetails(sheet, false);

    }

    private void assertContainsDetails(final XSSFSheet sheet, final boolean containsDetails) {
        AtomicBoolean authorNameFound = new AtomicBoolean(false);
        AtomicBoolean shooterNameFound = new AtomicBoolean(false);
        sheet.rowIterator().forEachRemaining(row->{
            row.cellIterator().forEachRemaining(cell->{
                if ( cell.getCellType().equals(CellType.STRING)){

                    if (cell.getStringCellValue().contains(author.getLastName())) {
                        authorNameFound.set(true);
                    }
                    if (cell.getStringCellValue().contains(shooter.getLastName())) {
                        shooterNameFound.set(true);
                    }

                }
            });
        });
        assertEquals(containsDetails, authorNameFound.get());
        assertEquals(containsDetails,shooterNameFound.get());
    }

    private HarvestReportExcelDTO createDTO(){
        // Always include details for testing
        return HarvestReportExcelDTO.create(harvest, enumLocaliser, true);
    }

}

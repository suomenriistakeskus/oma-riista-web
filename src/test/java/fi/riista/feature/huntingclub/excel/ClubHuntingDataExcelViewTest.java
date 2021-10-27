package fi.riista.feature.huntingclub.excel;

import fi.riista.feature.common.EnumLocaliser;
import fi.riista.util.Locales;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ClubHuntingDataExcelViewTest {

    private ResourceBundleMessageSource messageSource;
    private EnumLocaliser enumLocaliser;
    private XSSFWorkbook workbook;

    private final int HARVESTS_SHEET = 1;
    private final int OBSERVATIONS_SHEET = 2;

    @Before
    public void setup() {
        LocaleContextHolder.setLocale(Locales.FI);
        messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("i18n/messages");
        enumLocaliser = new EnumLocaliser(messageSource);
        workbook = new XSSFWorkbook();
    }

    @Test
    public void testExportData() {
        final ExcelViewForTesting view =  new ExcelViewForTesting(enumLocaliser);
        view.build(workbook);

        assertEquals("Metsästystapa", getCell(HARVESTS_SHEET, "P1").getStringCellValue());
        assertEquals("Metsästystavan kuvaus", getCell(HARVESTS_SHEET, "Q1").getStringCellValue());
        assertEquals("Seuruemetsästys koiran kanssa", getCell(HARVESTS_SHEET, "P2").getStringCellValue());
        assertNull(getCell(HARVESTS_SHEET, "N2"));

        assertEquals("Metsästystapa", getCell(OBSERVATIONS_SHEET, "P1").getStringCellValue());
        assertEquals("Metsästystavan kuvaus", getCell(OBSERVATIONS_SHEET, "Q1").getStringCellValue());
        assertEquals("Muu", getCell(OBSERVATIONS_SHEET, "P2").getStringCellValue());
        assertEquals("Other hunting type", getCell(OBSERVATIONS_SHEET, "Q2").getStringCellValue());
    }

    private XSSFCell getCell(final int sheetIndex, final String cell) {
        final CellAddress cellAddress = new CellAddress(cell);
        return workbook.getSheetAt(sheetIndex)
                .getRow(cellAddress.getRow())
                .getCell(cellAddress.getColumn());
    }

}

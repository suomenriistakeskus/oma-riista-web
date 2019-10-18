package fi.riista.feature.permitplanning.hirvityvitys;


import com.google.common.collect.ImmutableList;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gis.verotuslohko.GISVerotusLohko;
import fi.riista.feature.permitplanning.hirvityvitys.dto.JyvitysExcelApplicationDTO;
import fi.riista.feature.permitplanning.hirvityvitys.dto.JyvitysExcelApplicationVerotuslohkoDTO;
import fi.riista.feature.permitplanning.hirvityvitys.dto.JyvitysExcelRhyDTO;
import fi.riista.feature.permitplanning.hirvityvitys.dto.JyvitysExcelVerotuslohkoDTO;
import fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryPostProcessing;
import fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryTemplate;
import fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoTemplate;
import fi.riista.util.Locales;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryTemplate.STATIC_CONTENT_HEIGHT_ROWS;
import static org.junit.Assert.assertEquals;

public class JyvitysExcelTest {

    private static final int HECTARE_IN_SQUARE_METERS = 10_000;
    private static final String RHY_NAME = "Testi rhy";
    private static final int APPLICATION_COUNT = 2;
    private static final double APPLICATION_PRIVATE_LAND_IN_LOHKO_HA = 1_000.0;
    private static final double APPLICATION_STATE_LAND_IN_LOHKO_HA = 4_000.0;
    private static final int APPLICATION_SHOOTERS_OTHER_CLUB_PASSIVE = 5;
    private static final int APPLICATION_SHOOTER_ONLY_CLUB = 10;
    private static final double APPLIED_AMOUNT = 20.0;

    private ResourceBundleMessageSource messageSource;
    private EnumLocaliser enumLocaliser;

    private XSSFWorkbook workbook;
    private JyvitysExcelSummaryTemplate template;
    private JyvitysExcelVerotuslohkoTemplate verotuslohkoTemplate;
    private JyvitysExcelVerotuslohkoTemplate verotuslohkoTemplate2;
    private JyvitysExcelRhyDTO rhyDTO;
    private ImmutableList<JyvitysExcelVerotuslohkoDTO> lohkoList;
    private List<JyvitysExcelApplicationDTO> applicationDTOS;
    private JyvitysExcelVerotuslohkoDTO lohkoDto;
    private JyvitysExcelVerotuslohkoDTO lohkoDto2;

    @Before
    public void setup() {
        final GISVerotusLohko gisVerotusLohko = new GISVerotusLohko();
        gisVerotusLohko.setName("Eka lohko");
        gisVerotusLohko.setOfficialCode("902001");
        gisVerotusLohko.setPrivateLandSize(2_000.0 * HECTARE_IN_SQUARE_METERS);
        gisVerotusLohko.setStateLandSize(1_000.0 * HECTARE_IN_SQUARE_METERS);
        gisVerotusLohko.setLandSize(3_000.0 * HECTARE_IN_SQUARE_METERS);
        gisVerotusLohko.setWaterSize(500.0 * HECTARE_IN_SQUARE_METERS);
        gisVerotusLohko.setAreaSize(3_500.0 * HECTARE_IN_SQUARE_METERS);
        lohkoDto = JyvitysExcelVerotuslohkoDTO.from(gisVerotusLohko);
        final GISVerotusLohko gisVerotusLohko2 = new GISVerotusLohko();
        gisVerotusLohko2.setName("Toka lohko");
        gisVerotusLohko2.setOfficialCode("902002");
        gisVerotusLohko2.setPrivateLandSize(4_000.0 * HECTARE_IN_SQUARE_METERS);
        gisVerotusLohko2.setStateLandSize(2_000.0 * HECTARE_IN_SQUARE_METERS);
        gisVerotusLohko2.setLandSize(6_000.0 * HECTARE_IN_SQUARE_METERS);
        gisVerotusLohko2.setWaterSize(1_000.0 * HECTARE_IN_SQUARE_METERS);
        gisVerotusLohko2.setAreaSize(7_000.0 * HECTARE_IN_SQUARE_METERS);
        lohkoDto2 = JyvitysExcelVerotuslohkoDTO.from(gisVerotusLohko2);

        LocaleContextHolder.setLocale(Locales.FI);
        messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("i18n/messages");
        enumLocaliser = new EnumLocaliser(messageSource);

        lohkoList = ImmutableList.of(lohkoDto, lohkoDto2);
        rhyDTO = new JyvitysExcelRhyDTO(RHY_NAME, lohkoList);
        applicationDTOS = IntStream.range(0, APPLICATION_COUNT).mapToObj(counter -> createApplication(counter)).collect(Collectors.toList());
        workbook = new XSSFWorkbook();

        template = new JyvitysExcelSummaryTemplate(workbook, enumLocaliser, rhyDTO, APPLICATION_COUNT);
        template.initializeStaticContent();

        // Add first lohko
        assertEquals(1, workbook.getNumberOfSheets());
        verotuslohkoTemplate = new JyvitysExcelVerotuslohkoTemplate(workbook, enumLocaliser, lohkoDto, APPLICATION_COUNT);
        verotuslohkoTemplate.initializeStaticContent();
        assertEquals(2, workbook.getNumberOfSheets());

        // Add another lohko
        verotuslohkoTemplate2 = new JyvitysExcelVerotuslohkoTemplate(workbook, enumLocaliser, lohkoDto2, APPLICATION_COUNT);
        verotuslohkoTemplate2.initializeStaticContent();
        assertEquals(3, workbook.getNumberOfSheets());

        // Apply templates
        template
                .applyApplicationData(applicationDTOS)
                .applyStyle();

        verotuslohkoTemplate
                .applyApplicationData(applicationDTOS)
                .applyStyles(APPLICATION_COUNT);
        verotuslohkoTemplate2
                .applyApplicationData(applicationDTOS)
                .applyStyles(APPLICATION_COUNT);

        template.applyFormulas();
        verotuslohkoTemplate.applyFormulas();
        verotuslohkoTemplate2.applyFormulas();
        JyvitysExcelSummaryPostProcessing.apply(workbook, enumLocaliser, lohkoList, APPLICATION_COUNT);
    }

    // Kept for development time testing
//    @After
    public void write() throws IOException {
        workbook.write(new FileOutputStream(new File("/tmp/testi.xlsx")));
    }

    @Test
    public void testSmoke() {

        final int expectedRowCount = STATIC_CONTENT_HEIGHT_ROWS + APPLICATION_COUNT;
        // Zero-based row number
        assertEquals(expectedRowCount - 1, workbook.getSheetAt(0).getLastRowNum());

        final int lohkoExpectedRowCount = JyvitysExcelVerotuslohkoTemplate.VEROTUSLOHKO_STATIC_CONTENT_HEIGHT_ROWS + APPLICATION_COUNT;
        assertEquals(lohkoExpectedRowCount - 1, workbook.getSheetAt(1).getLastRowNum());
        assertEquals(lohkoExpectedRowCount - 1, workbook.getSheetAt(2).getLastRowNum());

    }

    // TEST VALUES ON SUMMARY SHEET


    @Test
    public void testRhyInformation() {
        assertEquals(rhyDTO.getName(), template.getCell(template.address("A1")).getStringCellValue());
    }

    @Test
    public void testApplicantNames() {
        assertEquals("hakija 0", getSummarySheetCell("A8").getStringCellValue());
        assertEquals("hakija 1", getSummarySheetCell("A9").getStringCellValue());
    }

    @Test
    public void testFormulas_totalQuotaMatches() {
        final String summarySheetQuotaAddress = "H2";
        // On row 12 with two applications
        final CellAddress verotuslohkoSheetQuotaAddress = new CellAddress("C12");

        assertEquals(0.0, getSummarySheetCell(summarySheetQuotaAddress).getNumericCellValue(), 0.01);

        verotuslohkoTemplate.getCell(verotuslohkoSheetQuotaAddress).setCellValue(50.0);
        reEvaluateAllFormulas();
        assertEquals(50.0, getSummarySheetCell(summarySheetQuotaAddress).getNumericCellValue(), 0.01);

        verotuslohkoTemplate2.getCell(verotuslohkoSheetQuotaAddress).setCellValue(20.0);
        reEvaluateAllFormulas();
        assertEquals(70.0, getSummarySheetCell(summarySheetQuotaAddress).getNumericCellValue(), 0.01);
    }


    @Test
    public void testFormulasSummary_sumMatches() {
        reEvaluateAllFormulas();

        assertEquals("SUM(B8:B9)", getSummarySheetCell("B10").getCellFormula());
        assertEquals("SUM(C8:C9)", getSummarySheetCell("C10").getCellFormula());
        assertEquals("SUM(D8:D9)", getSummarySheetCell("D10").getCellFormula());
        assertEquals("SUM(E8:E9)", getSummarySheetCell("E10").getCellFormula());
        assertEquals("SUM(F8:F9)", getSummarySheetCell("F10").getCellFormula());
        assertEquals("SUM(G8:G9)", getSummarySheetCell("G10").getCellFormula());
        assertEquals("SUM(H8:H9)", getSummarySheetCell("H10").getCellFormula());
        assertEquals("SUM(I8:I9)", getSummarySheetCell("I10").getCellFormula());
        assertEquals("SUM(M8:M9)", getSummarySheetCell("M10").getCellFormula());
        assertEquals("SUM(N8:N9)", getSummarySheetCell("N10").getCellFormula());
        assertEquals("SUM(O8:O9)", getSummarySheetCell("O10").getCellFormula());
    }

    @Test
    public void testFormulas_lohkojyvitysIsEvenlyDistributed() {
        // Assign quota for lohkos
        final CellAddress verotuslohkoSheetQuotaAddress = new CellAddress("C12");
        verotuslohkoTemplate.getCell(verotuslohkoSheetQuotaAddress).setCellValue(50.0);
        verotuslohkoTemplate2.getCell(verotuslohkoSheetQuotaAddress).setCellValue(50.0);
        reEvaluateAllFormulas();

        // First applicant
        assertEquals(0.5, evaluateFormulaNumeric(getSummarySheetCell("Q8")), 0.01);
        assertEquals(0.5, evaluateFormulaNumeric(getSummarySheetCell("R8")), 0.01);

        // Second applicant
        assertEquals(0.5, evaluateFormulaNumeric(getSummarySheetCell("Q9")), 0.01);
        assertEquals(0.5, evaluateFormulaNumeric(getSummarySheetCell("R9")), 0.01);

    }

    private double evaluateFormulaNumeric(Cell cell) {
        return workbook.getCreationHelper().createFormulaEvaluator().evaluateInCell(cell).getNumericCellValue();
    }


    // TEST VALUES ON VEROTUSLOHKO SHEETS

    @Test
    public void testVerotuslohkoInformation() {

        final JyvitysExcelVerotuslohkoDTO firstLohko = lohkoList.get(0);
        assertEquals(firstLohko.getName(), getCell(1, "F2").getStringCellValue());
        assertEquals(firstLohko.getOfficialCode(), getCell(1, "G2").getStringCellValue());
        assertEquals(firstLohko.getPrivateLandSize() / HECTARE_IN_SQUARE_METERS, getCell(1, "B21").getNumericCellValue(), 0.01);
        assertEquals(firstLohko.getStateLandSize() / HECTARE_IN_SQUARE_METERS, getCell(1, "B23").getNumericCellValue(), 0.01);
        assertEquals(firstLohko.getLandSize() / HECTARE_IN_SQUARE_METERS, getCell(1, "B25").getNumericCellValue(), 0.01);
        assertEquals(firstLohko.getWaterSize() / HECTARE_IN_SQUARE_METERS, getCell(1, "B27").getNumericCellValue(), 0.01);
        assertEquals(firstLohko.getAreaSize() / HECTARE_IN_SQUARE_METERS, getCell(1, "B29").getNumericCellValue(), 0.01);

        final JyvitysExcelVerotuslohkoDTO secondLohko = lohkoList.get(1);
        assertEquals(secondLohko.getName(), getCell(2, "F2").getStringCellValue());
        assertEquals(secondLohko.getOfficialCode(), getCell(2, "G2").getStringCellValue());
        assertEquals(secondLohko.getPrivateLandSize() / HECTARE_IN_SQUARE_METERS, getCell(2, "B21").getNumericCellValue(), 0.01);
        assertEquals(secondLohko.getStateLandSize() / HECTARE_IN_SQUARE_METERS, getCell(2, "B23").getNumericCellValue(), 0.01);
        assertEquals(secondLohko.getLandSize() / HECTARE_IN_SQUARE_METERS, getCell(2, "B25").getNumericCellValue(), 0.01);
        assertEquals(secondLohko.getWaterSize() / HECTARE_IN_SQUARE_METERS, getCell(2, "B27").getNumericCellValue(), 0.01);
        assertEquals(secondLohko.getAreaSize() / HECTARE_IN_SQUARE_METERS, getCell(2, "B29").getNumericCellValue(), 0.01);
    }

    @Test
    public void testVerotuslohko_quotaByLand() {
        assertEquals("G13*B10/1000", getCell(1, "C13").getCellFormula().replaceAll("\\s", ""));
        assertEquals("G13*B10/1000", getCell(2, "C13").getCellFormula().replaceAll("\\s", ""));
    }

    @Test
    public void testVerotuslohko_totalAreaForCalculcation() {
        assertEquals("J17 + J18", getCell(1, "J16").getCellFormula());
        assertEquals("J17 + J18", getCell(2, "J16").getCellFormula());
    }

    @Test
    public void testVerotuslohko_stateAreaForCalculcation() {
        assertEquals(lohkoList.get(0).getStateLandSize() / HECTARE_IN_SQUARE_METERS, evaluateFormulaNumeric(getCell(1, "J17")), 0.01);
        assertEquals(lohkoList.get(1).getStateLandSize() / HECTARE_IN_SQUARE_METERS, evaluateFormulaNumeric(getCell(2, "J17")), 0.01);
    }

    @Test
    public void testVerotuslohko_privateAreaForCalculcation() {
        assertEquals("B10", getCell(1, "J18").getCellFormula());
        assertEquals("B10", getCell(2, "J18").getCellFormula());
    }

    @Test
    public void testFormulasVerotuslohko_sumMatches() {
        reEvaluateAllFormulas();

        assertEquals("SUM(B8:B9)", getCell(1, "B10").getCellFormula());
        assertEquals("SUM(C8:C9)", getCell(1, "C10").getCellFormula());
        assertEquals("SUM(E8:E9)", getCell(1, "E10").getCellFormula());
        assertEquals("SUM(F8:F9)", getCell(1, "F10").getCellFormula());
        assertEquals("SUM(G8:G9)", getCell(1, "G10").getCellFormula());
        assertEquals("SUM(H8:H9)", getCell(1, "H10").getCellFormula());
        assertEquals("SUM(I8:I9)", getCell(1, "I10").getCellFormula());
        assertEquals("SUM(J8:J9)", getCell(1, "J10").getCellFormula());

        assertEquals("SUM(B8:B9)", getCell(2, "B10").getCellFormula());
        assertEquals("SUM(C8:C9)", getCell(2, "C10").getCellFormula());
        assertEquals("SUM(E8:E9)", getCell(2, "E10").getCellFormula());
        assertEquals("SUM(F8:F9)", getCell(2, "F10").getCellFormula());
        assertEquals("SUM(G8:G9)", getCell(2, "G10").getCellFormula());
        assertEquals("SUM(H8:H9)", getCell(2, "H10").getCellFormula());
        assertEquals("SUM(I8:I9)", getCell(2, "I10").getCellFormula());
        assertEquals("SUM(J8:J9)", getCell(2, "J10").getCellFormula());
    }

    @Test
    public void testFormulas_permitsPer1000Ha() {
        // Permits per 1000ha private land multiplier
        assertEquals("C12 / J16 * 1000", getCell(2, "G13").getCellFormula());
    }

    @Test
    public void testFormulas_permitsPerShooter() {
        // Permits per shooter multiplier
        assertEquals("IF( I10 > 0, C14 / I10, 0)", getCell(2, "G14").getCellFormula());
    }

    @Test
    public void testFormulas_permitsPerShooterZeroWithNoShooters() {
        // Shooter counts to zero for both applications
        getSummarySheetCell("E8").setCellValue(0.0);
        getSummarySheetCell("E9").setCellValue(0.0);
        getSummarySheetCell("F8").setCellValue(0.0);
        getSummarySheetCell("F9").setCellValue(0.0);

        reEvaluateAllFormulas();
        // Total shooter count
        assertEquals(0.0, getCell(1, "H10").getNumericCellValue(), 0.01);
        // Permits per shooter multiplier
        assertEquals(0.0, getCell(1, "G14").getNumericCellValue(), 0.01);

    }


    @Test
    public void testFormulas_suggestion() {
        // By land
        assertEquals("B8 * G13 / 1000", getCell(1, "K8").getCellFormula());
        assertEquals("B9 * G13 / 1000", getCell(1, "K9").getCellFormula());
        assertEquals("B8 * G13 / 1000", getCell(2, "K8").getCellFormula());
        assertEquals("B9 * G13 / 1000", getCell(2, "K9").getCellFormula());

        // By shooters
        assertEquals("I8 * G14", getCell(1, "L8").getCellFormula());
        assertEquals("I9 * G14", getCell(1, "L9").getCellFormula());
        assertEquals("I8 * G14", getCell(2, "L8").getCellFormula());
        assertEquals("I9 * G14", getCell(2, "L9").getCellFormula());

        // Combined
        assertEquals("K8 + L8", getCell(1, "M8").getCellFormula());
        assertEquals("K9 + L9", getCell(1, "M9").getCellFormula());
        assertEquals("K8 + L8", getCell(2, "M8").getCellFormula());
        assertEquals("K9 + L9", getCell(2, "M9").getCellFormula());

        // Total suggestion
        assertEquals("M8", getCell(1, "N8").getCellFormula());
        assertEquals("M9", getCell(1, "N9").getCellFormula());
        assertEquals("M8", getCell(2, "N8").getCellFormula());
        assertEquals("M9", getCell(2, "N9").getCellFormula());

        // Adults
        assertEquals("ROUND( ( 2 * N8 * (100- C15 ) / (200- C15 ) ), 0 )", getCell(1, "O8").getCellFormula());
        assertEquals("ROUND( ( 2 * N9 * (100- C15 ) / (200- C15 ) ), 0 )", getCell(1, "O9").getCellFormula());
        assertEquals("ROUND( ( 2 * N8 * (100- C15 ) / (200- C15 ) ), 0 )", getCell(2, "O8").getCellFormula());
        assertEquals("ROUND( ( 2 * N9 * (100- C15 ) / (200- C15 ) ), 0 )", getCell(2, "O9").getCellFormula());

    }

    private JyvitysExcelApplicationDTO createApplication(final int counter) {
        final List<JyvitysExcelApplicationVerotuslohkoDTO> applicationVerotuslohkoDTOS = lohkoList.stream().map(lohko -> {
            return JyvitysExcelApplicationVerotuslohkoDTO.Builder.builder()
                    .withAreaSize(ha(10_000.0))
                    .withLandSize(ha(2_000))
                    .withPrivateLandSize(ha(APPLICATION_PRIVATE_LAND_IN_LOHKO_HA))
                    .withName(lohko.getName())
                    .withOfficialCode(lohko.getOfficialCode())
                    .withPrivateSize(ha(2_000.0))
                    .withPrivateWaterSize(ha(500.0))
                    .withStateSize(ha(5_000.0))
                    .withStateLandSize(ha(APPLICATION_STATE_LAND_IN_LOHKO_HA))
                    .withStateWaterSize(ha(1_000.0))
                    .build();

        }).collect(Collectors.toList());
        return JyvitysExcelApplicationDTO.Builder.builder()
                .withApplicant("hakija " + counter)
                .withAppliedAmount(APPLIED_AMOUNT)
                .withOtherRhysInArea(counter % 2 == 0 ? ImmutableList.of("Toinen rhy", "Kolmas rhy", "Neljäs rhy") : ImmutableList.of())
                .withShooterOnlyClub(APPLICATION_SHOOTER_ONLY_CLUB)
                .withShooterOtherClubPassive(APPLICATION_SHOOTERS_OTHER_CLUB_PASSIVE)
                .withLohkoList(applicationVerotuslohkoDTOS)
                .build();
    }

    private double ha(final double hectares) {
        return hectares * HECTARE_IN_SQUARE_METERS;
    }


    private void reEvaluateAllFormulas() {
        workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();
    }

    private XSSFCell getSummarySheetCell(final String address) {
        return getCell(0, address);
    }

    private XSSFCell getCell(final int sheetNumber, final String address) {
        final CellAddress celladdress = new CellAddress(address);
        return workbook.getSheetAt(sheetNumber)
                .getRow(celladdress.getRow())
                .getCell(celladdress.getColumn());
    }
}

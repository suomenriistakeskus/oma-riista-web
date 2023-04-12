package fi.riista.feature.otherwisedeceased;

import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.util.Locales;
import fi.riista.util.LocalisedString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static fi.riista.config.Constants.DEFAULT_TIMEZONE;
import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.nullValue;

@RunWith(Theories.class)
public class OtherwiseDeceasedExcelViewTest {

    private ResourceBundleMessageSource messageSource;
    private EnumLocaliser enumLocaliser;
    private XSSFWorkbook workbook;
    private Map<Integer, LocalisedString> speciesNameMap;

    @Before
    public void setup() {
        LocaleContextHolder.setLocale(Locales.FI);
        messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("i18n/messages");
        enumLocaliser = new EnumLocaliser(messageSource);
        workbook = new XSSFWorkbook();
        speciesNameMap = new HashMap<>();
        speciesNameMap.put(1, LocalisedString.of("Laji Yksi"));
    }

    @Theory
    public void validateDataSheet(final GameAge age,
                                  final GameGender gender,
                                  final boolean rejected,
                                  final OtherwiseDeceasedCause cause,
                                  final OtherwiseDeceasedSource source,
                                  final boolean noExactLocation) {

        final OtherwiseDeceasedDTO dto = createOtherwiseDeceasedDTO(age, gender, rejected, cause, source, noExactLocation);
        final OtherwiseDeceasedExcelView actual = new OtherwiseDeceasedExcelView(enumLocaliser, Arrays.asList(dto), speciesNameMap);

        actual.createSheet(workbook);

        assertThat(workbook.getNumberOfSheets(), equalTo(1));
        final Sheet sheet = workbook.getSheetAt(0);
        assertThat(sheet.getFirstRowNum(), equalTo(0));
        assertThat(sheet.getLastRowNum(), equalTo(1));
        final Row row = sheet.getRow(1);
        assertThat(row.getCell(0).getStringCellValue(), equalTo("2.1.2021 03:04"));
        assertThat(row.getCell(1).getStringCellValue(), equalTo("Laji Yksi"));
        assertThat(row.getCell(2).getStringCellValue(), isOneOf("Aikuinen", "Alle 1 v", "Tuntematon"));
        assertThat(row.getCell(3).getStringCellValue(), isOneOf("Uros", "Naaras", "Tuntematon"));
        assertThat(row.getCell(4).getNumericCellValue(), equalTo(12.3D));
        assertThat(row.getCell(5).getStringCellValue(), equalTo(rejected ? "Kyllä" : "Ei"));
        assertThat(row.getCell(6).getStringCellValue(), isOneOf(
                "Liikenneonnettomuus", "Onnettomuus rautatiellä", "Sairaus / nääntyminen", "Poliisin määräyksellä lopetettu",
                "Pakkotila", "Laiton", "Tutkinnassa", "Muu"));
        assertThat(row.getCell(7).getStringCellValue(), equalTo("CauseOther"));
        assertThat(row.getCell(8).getStringCellValue(), isOneOf(
                "Riistakeskus", "Riistanhoitoyhdistys", "Ruokavirasto", "Poliisi", "Rajavartiolaitos",
                "Metsähallitus", "Luonnonvarakeskus, luke", "Kansalainen", "Media", "Muu"));
        assertThat(row.getCell(9).getStringCellValue(), equalTo("SourceOther"));
        assertThat(row.getCell(10).getStringCellValue(), equalTo("ORG FI 001"));
        assertThat(row.getCell(11).getStringCellValue(), equalTo("ORG FI 002"));
        assertThat(row.getCell(12).getStringCellValue(), equalTo("ORG FI 003"));
        assertThat(row.getCell(13).getStringCellValue(), equalTo(noExactLocation ? "Ei" : "Kyllä"));
        assertThat(row.getCell(14).getNumericCellValue(), equalTo(654321D));
        assertThat(row.getCell(15).getNumericCellValue(), equalTo(123456D));
        assertThat(row.getCell(16).getNumericCellValue(), equalTo(5D));
        assertThat(row.getCell(17).getStringCellValue(), equalTo("Description"));
        assertThat(row.getCell(18).getStringCellValue(), equalTo("AdditionalInfo"));
        assertThat(row.getCell(19).getStringCellValue(), equalTo("1.1.2021 00:01"));
        assertThat(row.getCell(20).getStringCellValue(), equalTo("FirstName-1 LastName-1"));
        assertThat(row.getCell(21).getStringCellValue(), equalTo("1.1.2021 00:02"));
        assertThat(row.getCell(22).getStringCellValue(), equalTo("FirstName-2 LastName-2"));
    }

    @Test
    public void itemHasNoModifications() {
        final OtherwiseDeceasedDTO dto = createOtherwiseDeceasedDTO(
                GameAge.UNKNOWN, GameGender.UNKNOWN, false, OtherwiseDeceasedCause.OTHER,
                OtherwiseDeceasedSource.OTHER, false);
        dto.setChangeHistory(Arrays.asList(newChange(3)));
        final OtherwiseDeceasedExcelView actual = new OtherwiseDeceasedExcelView(enumLocaliser, Arrays.asList(dto), speciesNameMap);

        actual.createSheet(workbook);

        assertThat(workbook.getNumberOfSheets(), equalTo(1));
        final Sheet sheet = workbook.getSheetAt(0);
        assertThat(sheet.getFirstRowNum(), equalTo(0));
        assertThat(sheet.getLastRowNum(), equalTo(1));
        final Row row = sheet.getRow(1);

        assertThat(row.getCell(19).getStringCellValue(), equalTo("1.1.2021 00:03"));
        assertThat(row.getCell(20).getStringCellValue(), equalTo("FirstName-3 LastName-3"));
        assertThat(row.getCell(21), is(nullValue()));
        assertThat(row.getCell(22), is(nullValue()));
    }

    private OtherwiseDeceasedDTO createOtherwiseDeceasedDTO(final GameAge age,
                                                            final GameGender gameGender,
                                                            final boolean rejected,
                                                            final OtherwiseDeceasedCause cause,
                                                            final OtherwiseDeceasedSource source,
                                                            final boolean noExactLocation) {
        final OtherwiseDeceasedDTO dto = new OtherwiseDeceasedDTO();
        dto.setPointOfTime(new LocalDateTime(2021, 1, 2, 3, 4));
        dto.setGameSpeciesCode(1);
        dto.setAge(age);
        dto.setGender(gameGender);
        dto.setWeight(12.3);
        dto.setRejected(rejected);
        dto.setCause(cause);
        dto.setCauseDescription("CauseOther");
        dto.setSource(source);
        dto.setSourceDescription("SourceOther");
        dto.setRka(newOrganisationNameDTO("001"));
        dto.setRhy(newOrganisationNameDTO("002"));
        dto.setMunicipality(newOrganisationNameDTO("003"));
        dto.setNoExactLocation(noExactLocation);
        dto.setGeoLocation(newGeoLocation());
        dto.setAttachments(createAttachments(5));
        dto.setDescription("Description");
        dto.setAdditionalInfo("AdditionalInfo");
        dto.setChangeHistory(Arrays.asList(newChange(1), newChange(2)));
        return dto;
    }

    private OrganisationNameDTO newOrganisationNameDTO(final String officialCode) {
        final OrganisationNameDTO dto = new OrganisationNameDTO();
        dto.setNameFI("ORG FI " + officialCode);
        dto.setNameSV("ORG SV " + officialCode);
        dto.setOfficialCode(officialCode);
        return dto;
    }

    private GeoLocation newGeoLocation() {
        final GeoLocation geoLocation = new GeoLocation();
        geoLocation.setSource(GeoLocation.Source.MANUAL);
        geoLocation.setLatitude(123456);
        geoLocation.setLongitude(654321);
        return geoLocation;
    }

    private List<OtherwiseDeceasedAttachmentDTO> createAttachments(final int amount) {
        final List<OtherwiseDeceasedAttachmentDTO> attachments = new ArrayList<>();
        IntStream.range(0, amount).forEach(i -> attachments.add(newAttachmentDTO(i)));
        return attachments;
    }

    private OtherwiseDeceasedAttachmentDTO newAttachmentDTO(final int id) {
        final OtherwiseDeceasedAttachmentDTO dto = new OtherwiseDeceasedAttachmentDTO();
        dto.setId(id);
        dto.setFilename("filename-" + id);
        return dto;
    }

    private OtherwiseDeceasedChangeDTO newChange(final int id) {
        final OtherwiseDeceasedChangeDTO dto = new OtherwiseDeceasedChangeDTO();
        dto.setId(Long.valueOf(id));
        dto.setModificationTime(new DateTime(2021, 1, 1, 0, id % 60).withZone(DEFAULT_TIMEZONE));
        dto.setAuthor(newAuthor(id));

        return dto;
    }

    private OtherwiseDeceasedChangeDTO.AuthorDTO newAuthor(final int id) {
        final OtherwiseDeceasedChangeDTO.AuthorDTO dto = new OtherwiseDeceasedChangeDTO.AuthorDTO();
        dto.setId(Long.valueOf(id));
        dto.setFirstName("FirstName-" + id);
        dto.setLastName("LastName-" + id);
        return dto;
    }

}

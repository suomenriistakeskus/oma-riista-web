package fi.riista.integration.metsahallitus;

import fi.riista.integration.metsahallitus.permit.MetsahallitusPermit;
import fi.riista.integration.metsahallitus.permit.MetsahallitusPermitErrorCollector;
import fi.riista.integration.metsahallitus.permit.MetsahallitusPermitImportDTO;
import fi.riista.integration.metsahallitus.permit.MetsahallitusPermitImportFeature;
import fi.riista.integration.metsahallitus.permit.MetsahallitusPermitRepository;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class MetsahallitusPermitImportFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private MetsahallitusPermitImportFeature metsahallitusPermitImportFeature;

    @Resource
    private MetsahallitusPermitRepository metsahallitusPermitRepository;

    @Test
    public void testAdd() {
        final List<MetsahallitusPermitImportDTO> dtoList = Arrays.asList(
                dtoForSsn(zeroPaddedNumber(10)),
                dtoForHunterNumber(zeroPaddedNumber(10)),
                dtoForSsn(zeroPaddedNumber(10)),
                dtoForHunterNumber(zeroPaddedNumber(10)));

        final MetsahallitusPermitErrorCollector errorCollector = new MetsahallitusPermitErrorCollector();
        final Set<Long> ids = metsahallitusPermitImportFeature.importPermits(dtoList, errorCollector);

        assertEquals(4, ids.size());
        assertEquals(0, errorCollector.getAllErrors().size());

        runInTransaction(() -> assertPermits(dtoList, metsahallitusPermitRepository.findAll()));
    }

    @Test
    public void testUpdate() {
        final List<MetsahallitusPermitImportDTO> dtoList = Arrays.asList(
                dtoForSsn(zeroPaddedNumber(10)),
                dtoForHunterNumber(zeroPaddedNumber(10)),
                dtoForSsn(zeroPaddedNumber(10)),
                dtoForHunterNumber(zeroPaddedNumber(10)));

        metsahallitusPermitImportFeature.importPermits(dtoList, new MetsahallitusPermitErrorCollector());

        final MetsahallitusPermitImportDTO dto = dtoList.get(0);
        dto.setTilauksenTila("cancel");

        metsahallitusPermitImportFeature.importPermits(singletonList(dto), new MetsahallitusPermitErrorCollector());

        runInTransaction(() -> assertPermits(dtoList, metsahallitusPermitRepository.findAll()));
    }

    @Test
    public void testMissingHunterNumberAndSsn() {
        final MetsahallitusPermitImportDTO dto = incompleteDto(zeroPaddedNumber(10));
        final List<MetsahallitusPermitImportDTO> dtoList = singletonList(dto);

        final MetsahallitusPermitErrorCollector errorCollector = new MetsahallitusPermitErrorCollector();
        final Set<Long> ids = metsahallitusPermitImportFeature.importPermits(dtoList, errorCollector);

        assertEquals(0, ids.size());
        assertEquals(1, errorCollector.getErrorCount(dto));

        final Set<String> errorMessages = errorCollector.getAllErrors().get(dto.getLuvanTunnus());
        assertEquals(1, errorMessages.size());
        assertEquals("virheellinen arvo kentässä henkiloTunnus ja metsastajaNumero, kumpikin ovat tyhjät",
                errorMessages.iterator().next());
    }

    private static void assertPermits(final List<MetsahallitusPermitImportDTO> dtoList,
                               final List<MetsahallitusPermit> entityList) {
        assertEquals(dtoList.size(), entityList.size());

        dtoList.forEach(dto -> entityList.stream()
                .filter(e -> e.getPermitIdentifier().equals(dto.getLuvanTunnus()))
                .filter(e -> e.getSsn() != null && e.getSsn().equals(dto.getHenkiloTunnus()) ||
                        e.getHunterNumber() != null && e.getHunterNumber().equals(dto.getMetsastajaNumero()))
                .findFirst()
                .ifPresent(e -> assertPermit(dto, e)));
    }

    private static void assertPermit(final MetsahallitusPermitImportDTO dto, final MetsahallitusPermit entity) {
        assertEquals(dto.getLuvanTunnus(), entity.getPermitIdentifier());
        assertEquals(dto.getTilauksenTila(), entity.getStatus());

        assertEquals(dto.getHenkiloTunnus(), entity.getSsn());
        assertEquals(dto.getMetsastajaNumero(), entity.getHunterNumber());

        assertEquals(dto.getAlueenNimi(), entity.getAreaName());
        assertEquals(dto.getAlueenNimiSE(), entity.getAreaNameSwedish());
        assertEquals(dto.getAlueenNimiEN(), entity.getAreaNameEnglish());

        assertEquals(dto.getAlueNro(), entity.getAreaNumber());

        assertEquals(dto.getLuvanNimi(), entity.getPermitName());
        assertEquals(dto.getLuvanNimiSE(), entity.getPermitNameSwedish());
        assertEquals(dto.getLuvanNimiEN(), entity.getPermitNameEnglish());

        assertEquals(dto.getLupaTyyppi(), entity.getPermitType());
        assertEquals(dto.getLupaTyyppiSE(), entity.getPermitTypeSwedish());
        assertEquals(dto.getLupaTyyppiEN(), entity.getPermitTypeEnglish());
        assertEquals(dto.getUrl(), entity.getUrl());
        assertEquals(dto.getSaalispalauteAnnettu(), entity.getHarvestReportSubmitted());
    }

    private MetsahallitusPermitImportDTO dtoForHunterNumber(final String permitNumber) {
        final MetsahallitusPermitImportDTO dto = incompleteDto(permitNumber);
        dto.setMetsastajaNumero(hunterNumber());
        return dto;
    }

    private MetsahallitusPermitImportDTO dtoForSsn(final String permitNumber) {
        final MetsahallitusPermitImportDTO dto = incompleteDto(permitNumber);
        dto.setHenkiloTunnus(ssn());
        return dto;
    }

    private static MetsahallitusPermitImportDTO incompleteDto(final String permitNumber) {
        final MetsahallitusPermitImportDTO dto = new MetsahallitusPermitImportDTO();
        dto.setTilauksenTila(MetsahallitusPermitImportDTO.PAID_1);
        dto.setLuvanTunnus(permitNumber);

        dto.setLupaTyyppi("testilupatyyppi");
        dto.setLupaTyyppiSE("testilupatyyppi-sv");
        dto.setLupaTyyppiEN("testilupatyyppi-en");

        dto.setLuvanNimi("testiluvannimi");
        dto.setLuvanNimiSE("testiluvannimi-sv");
        dto.setLuvanNimiEN("testiluvannimi-en");

        dto.setAlueenNimi("testialuenimi-fi");
        dto.setAlueenNimiSE("testialuenimi-sv");
        dto.setAlueenNimiEN("testialuenimi-en");

        dto.setAlueNro("123456");
        dto.setAlkuPvm("1.1.2017");
        dto.setLoppuPvm("1.12.2017");
        dto.setUrl("https://www.riista.fi");
        dto.setSaalispalauteAnnettu(false);

        return dto;
    }
}

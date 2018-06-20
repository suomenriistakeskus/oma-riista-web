package fi.riista.integration.metsahallitus;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.metsahallitus.permit.MhPermit;
import fi.riista.feature.metsahallitus.permit.MhPermitRepository;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MhPermitImportRunnerTest extends EmbeddedDatabaseTest {

    @Resource
    private MhPermitImportRunner importRunner;

    @Resource
    private MhPermitRepository mhPermitRepository;

    @Test
    public void testEmptyList() {
        onSavedAndAuthenticated(createApiUser(), () -> {
            final Map<String, Set<String>> allErrors = importRunner.importMhPermits(emptyList());
            assertEquals(0, allErrors.size());
            assertEquals(0, mhPermitRepository.findAll().size());
        });
    }

    @Test
    public void testEmptyObject() {
        onSavedAndAuthenticated(createApiUser(), () -> {
            final Map<String, Set<String>> allErrors = importRunner.importMhPermits(singletonList(new MhPermitImportDTO()));

            assertEquals(1, allErrors.size());
            final Set<String> errors = allErrors.get(null);

            assertEquals(2, errors.size());
            assertTrue(errors.contains("virheellinen arvo kentässä henkiloTunnus ja metsastajaNumero, kumpikin ovat tyhjät"));
            assertTrue(errors.contains("virheellinen arvo kentässä tilauksenTila:null"));

            assertEquals(0, mhPermitRepository.findAll().size());
        });
    }

    @Test
    public void testPersonFoundBySsn() {
        withPerson(person -> onSavedAndAuthenticated(createApiUser(), () -> {
            final List<MhPermitImportDTO> dtos = singletonList(createDto(person.getSsn(), null));
            final Map<String, Set<String>> errors = importRunner.importMhPermits(dtos);
            assertEquals(0, errors.size());
            assertPermits(dtos);
        }));
    }

    @Test
    public void testPersonFoundByHunterNumber() {
        withPerson(person -> onSavedAndAuthenticated(createApiUser(), () -> {
            final List<MhPermitImportDTO> dtos = singletonList(createDto(null, person.getHunterNumber()));
            final Map<String, Set<String>> errors = importRunner.importMhPermits(dtos);
            assertEquals(0, errors.size());
            assertPermits(dtos);
        }));
    }

    @Test
    public void testBeginDateEmpty() {
        withPerson(person -> onSavedAndAuthenticated(createApiUser(), () -> {
            final MhPermitImportDTO dto = createDto(null, person.getHunterNumber());
            dto.setAlkuPvm("");

            final Map<String, Set<String>> allErrors = importRunner.importMhPermits(singletonList(dto));
            assertEquals(1, allErrors.size());

            final Set<String> errors = allErrors.get(dto.getLuvanTunnus());
            assertEquals(2, errors.size());
            assertTrue(errors.contains("tyhjä arvo kentässä:alkuPvm"));
            assertTrue(errors.contains("virheellinen päivämäärä alkuPvm arvo:"));
        }));
    }

    @Test
    public void testEndDateNull() {
        withPerson(person -> onSavedAndAuthenticated(createApiUser(), () -> {
            final MhPermitImportDTO dto = createDto(null, person.getHunterNumber());
            dto.setLoppuPvm(null);

            final Map<String, Set<String>> allErrors = importRunner.importMhPermits(singletonList(dto));
            assertEquals(1, allErrors.size());

            final Set<String> errors = allErrors.get(dto.getLuvanTunnus());
            assertEquals(2, errors.size());
            assertTrue(errors.contains("tyhjä arvo kentässä:loppuPvm"));
            assertTrue(errors.contains("virheellinen päivämäärä loppuPvm arvo:null"));
        }));
    }

    @Test
    public void testStatusIsEmpty() {
        withPerson(person -> onSavedAndAuthenticated(createApiUser(), () -> {
            final MhPermitImportDTO dto = createDto(null, person.getHunterNumber());
            dto.setTilauksenTila("");

            final Map<String, Set<String>> allErrors = importRunner.importMhPermits(singletonList(dto));
            assertEquals(1, allErrors.size());

            final Set<String> errors = allErrors.get(dto.getLuvanTunnus());
            assertEquals(1, errors.size());
            assertTrue(errors.contains("virheellinen arvo kentässä tilauksenTila:"));
        }));
    }

    @Test
    public void existingUpdated() {
        withPerson(person -> onSavedAndAuthenticated(createApiUser(), () -> {
            final MhPermitImportDTO dto = createDto(null, person.getHunterNumber());
            final List<MhPermitImportDTO> dtos = singletonList(dto);
            importRunner.importMhPermits(dtos);

            dto.setAlueenNimi("changed");
            final Map<String, Set<String>> errors = importRunner.importMhPermits(dtos);
            assertEquals(0, errors.size());
            assertPermits(dtos);
        }));
    }

    @Test
    public void existingCancelled() {
        withPerson(person -> onSavedAndAuthenticated(createApiUser(), () -> {
            final MhPermitImportDTO dto = createDto(null, person.getHunterNumber());
            final List<MhPermitImportDTO> dtos = singletonList(dto);
            importRunner.importMhPermits(dtos);

            dto.setTilauksenTila("x");// anything other than MhPermitImportDTO.PAID is considered cancelled, but empty and null are invalid
            final Map<String, Set<String>> errors = importRunner.importMhPermits(dtos);
            assertEquals(0, errors.size());
            assertPermits(emptyList());
        }));
    }

    private void assertPermits(final List<MhPermitImportDTO> dtos) {
        final List<MhPermit> allPermits = mhPermitRepository.findAll();
        assertEquals(dtos.size(), allPermits.size());

        final Map<String, MhPermit> entityMap = F.index(allPermits, MhPermit::getPermitIdentifier);
        for (final MhPermitImportDTO dto : dtos) {
            assertPermitEquals(dto, entityMap.get(dto.getLuvanTunnus()));
        }
    }

    private static void assertPermitEquals(MhPermitImportDTO dto, MhPermit entity) {
        assertEquals(dto.getLuvanTunnus(), entity.getPermitIdentifier());
        assertEquals(dto.getLupaTyyppi(), entity.getPermitType());
        assertEquals(dto.getAlueNro(), entity.getAreaNumber());

        assertEquals(dto.getLuvanNimi(), entity.getPermitName());
        assertEquals(dto.getLuvanNimiSE() != null ? dto.getLuvanNimiSE() : dto.getLuvanNimi(), entity.getPermitNameSwedish());
        assertEquals(dto.getLuvanNimiEN() != null ? dto.getLuvanNimiEN() : dto.getLuvanNimi(), entity.getPermitNameEnglish());

        assertEquals(dto.getAlueenNimi(), entity.getAreaName());
        assertEquals(dto.getAlueenNimiSE() != null ? dto.getAlueenNimiSE() : dto.getAlueenNimi(), entity.getAreaNameSwedish());
        assertEquals(dto.getAlueenNimiEN() != null ? dto.getAlueenNimiEN() : dto.getAlueenNimi(), entity.getAreaNameEnglish());
    }

    private SystemUser createApiUser() {
        final SystemUser u = createNewModerator();
        u.addPrivilege(SystemUserPrivilege.IMPORT_METSAHALLITUS_PERMITS);
        return u;
    }

    private MhPermitImportDTO createDto(final String ssn, final String hunterNumber) {
        final MhPermitImportDTO dto = new MhPermitImportDTO();
        dto.setHenkiloTunnus(ssn);
        dto.setMetsastajaNumero(hunterNumber);

        dto.setTilauksenTila(MhPermitImportDTO.PAID);
        dto.setLuvanTunnus("LuvanTunnus" + model().nextPositiveInt());
        dto.setLupaTyyppi("Lupatyyppi" + model().nextPositiveInt());
        dto.setLuvanNimi("LuvanNimi" + model().nextPositiveInt());
        dto.setAlueNro("AlueNro" + model().nextPositiveInt());
        dto.setAlueenNimi("AlueenNimi" + model().nextPositiveInt());

        dto.setAlkuPvm("1.1.2017");
        dto.setLoppuPvm("1.1.2017");
        return dto;
    }
}
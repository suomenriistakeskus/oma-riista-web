package fi.riista.integration.lupahallinta.support;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermit_;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.integration.common.entity.Integration;
import fi.riista.integration.lupahallinta.HarvestPermitImportException;
import fi.riista.integration.lupahallinta.HarvestPermitImportFeature;
import fi.riista.integration.lupahallinta.HarvestPermitImportResultDTO;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Sort;

import javax.annotation.Resource;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class LupahallintaHarvestPermitImporterTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestPermitImportFeature importFeature;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Test
    public void testLatestLhSyncTimeUsed() {
        final DateTime expected = DateTime.now();
        createIntegration(expected);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            LupahallintaHttpClient mockClient = createMockClient(header());
            LupahallintaPermitImportMailHandler mockMailHandler = createMockMailHandler();
            LupahallintaHarvestPermitImporter importer = new LupahallintaHarvestPermitImporter(
                    importFeature, mockClient, mockMailHandler);

            HarvestPermitImportResultDTO result = importer.doImport();

            assertNotNull(result);
            assertNoChanges(result);

            ArgumentCaptor<DateTime> argument = ArgumentCaptor.forClass(DateTime.class);
            verify(mockClient).getPermits(argument.capture());
            assertEquals(expected, argument.getValue());
        });
    }

    @Test
    public void testNoLhSyncTimeIs24HoursBefore() {
        try {
            createIntegration(null);
            final SystemUser admin = createNewAdmin();
            persistInNewTransaction();

            DateTime fakeNow = new DateTime(2014, 6, 25, 12, 34, 0);
            DateTimeUtils.setCurrentMillisFixed(fakeNow.getMillis());

            authenticate(admin);

            LupahallintaHttpClient mockClient = createMockClient(header());
            LupahallintaPermitImportMailHandler mockMailHandler = createMockMailHandler();
            LupahallintaHarvestPermitImporter importer = new LupahallintaHarvestPermitImporter(
                    importFeature, mockClient, mockMailHandler);

            HarvestPermitImportResultDTO result = importer.doImport();

            assertNotNull(result);
            assertNoChanges(result);

            ArgumentCaptor<DateTime> argument = ArgumentCaptor.forClass(DateTime.class);
            verify(mockClient).getPermits(argument.capture());
            assertEquals(fakeNow.minusDays(1), argument.getValue());
            assertEquals(fakeNow, importFeature.getLastLhSyncTime());
        } finally {
            DateTimeUtils.setCurrentMillisSystem();
        }
    }

    @Test
    public void testLhSyncTimeIsUpdated() {
        try {
            createIntegration(null);
            final SystemUser admin = createNewAdmin();
            persistInNewTransaction();

            authenticate(admin);

            DateTime fakeNow = new DateTime(2014, 6, 25, 12, 34, 0);
            DateTimeUtils.setCurrentMillisFixed(fakeNow.getMillis());
            new LupahallintaHarvestPermitImporter(importFeature, createMockClient(header()), createMockMailHandler())
                    .doImport();

            DateTime newFakeNow = fakeNow.plusMinutes(11);
            DateTimeUtils.setCurrentMillisFixed(newFakeNow.getMillis());
            LupahallintaHttpClient mockClient = createMockClient(header());
            new LupahallintaHarvestPermitImporter(importFeature, mockClient, createMockMailHandler())
                    .doImport();

            ArgumentCaptor<DateTime> argument = ArgumentCaptor.forClass(DateTime.class);
            verify(mockClient).getPermits(argument.capture());
            assertEquals(fakeNow, argument.getValue());
            assertEquals(newFakeNow, importFeature.getLastLhSyncTime());
        } finally {
            DateTimeUtils.setCurrentMillisSystem();
        }
    }

    @Test
    public void testHttpError() {
        createIntegration(null);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            RuntimeException exception = new RuntimeException("http throws this");
            LupahallintaHttpClient mockClient = createMockClient(exception);
            LupahallintaPermitImportMailHandler mockMailHandler = createMockMailHandler();
            LupahallintaHarvestPermitImporter importer = new LupahallintaHarvestPermitImporter(
                    importFeature, mockClient, mockMailHandler);

            HarvestPermitImportResultDTO result = importer.doImport();
            assertNull(result);

            verify(mockMailHandler).handleError(eq(exception));
            assertNull(importFeature.getLastLhSyncTime());
        });
    }

    @Test
    public void testParsingError() {
        createIntegration(null);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            LupahallintaHttpClient mockClient = createMockClient("this;is;incorrect;data;;will;fail;parsing;;;;;;;;;;;;;;");
            LupahallintaPermitImportMailHandler mockMailHandler = createMockMailHandler();
            LupahallintaHarvestPermitImporter importer = new LupahallintaHarvestPermitImporter(
                    importFeature, mockClient, mockMailHandler);

            HarvestPermitImportResultDTO result = importer.doImport();
            assertEquals(0, result.getModifiedOrAddedCount());
            assertNotNull(result.getAllErrors());

            verify(mockMailHandler).handleError(any(HarvestPermitImportException.class));
            assertNull(importFeature.getLastLhSyncTime());
        });
    }

    @Test
    public void testWhenNoPermitsReceivedThenNoChanges() {
        createIntegration(null);
        final HarvestPermit permit = model().newHarvestPermit();

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            LupahallintaHttpClient mockClient = createMockClient(header());
            LupahallintaHarvestPermitImporter importer = new LupahallintaHarvestPermitImporter(
                    importFeature, mockClient, createMockMailHandler());
            HarvestPermitImportResultDTO result = importer.doImport();
            assertNoChanges(result);

            runInTransaction(() -> {
                List<HarvestPermit> all = harvestPermitRepository.findAll();
                assertEquals(1, all.size());
                assertEquals(permit.getId(), all.get(0).getId());
                assertEquals(0, all.get(0).getConsistencyVersion().intValue());
            });
        });
    }

    @Test
    public void testPermitAdded() {
        withRhy(rhy -> {
            createIntegration(null);
            final HarvestPermit permit = model().newHarvestPermit(rhy);
            final GameSpecies species = model().newGameSpecies();
            final Person person = model().newPerson();

            onSavedAndAuthenticated(createNewAdmin(), () -> {
                final String permitNumber = permitNumber("001");
                final LocalDate today = DateUtil.today();
                final String dates = formatDate(today, today.plusDays(14));
                final int amount = 2;
                LupahallintaHttpClient mockClient = createMockClient(newPermitRow(person, species, amount, rhy, permitNumber, dates));

                LupahallintaPermitImportMailHandler mockMailHandler = createMockMailHandler();
                LupahallintaHarvestPermitImporter importer = new LupahallintaHarvestPermitImporter(
                        importFeature, mockClient, mockMailHandler);
                HarvestPermitImportResultDTO result = importer.doImport();
                assertNoErrors(result);

                verifyNoMoreInteractions(mockMailHandler);

                runInTransaction(() -> {
                    List<HarvestPermit> all = harvestPermitRepository.findAll(new Sort(HarvestPermit_.id.getName()));
                    assertEquals(2, all.size());

                    HarvestPermit first = all.get(0);
                    assertEquals(permit.getId(), first.getId());
                    assertEquals(0, first.getConsistencyVersion().intValue());
                    assertNull(first.getLhSyncTime());

                    HarvestPermit second = all.get(1);
                    assertEquals(person.getId(), second.getOriginalContactPerson().getId());
                    assertEquals(permitNumber, second.getPermitNumber());
                    assertNotNull(second.getLhSyncTime());
                    Collection<HarvestPermitSpeciesAmount> speciesAmounts = second.getSpeciesAmounts();
                    assertEquals(1, speciesAmounts.size());
                    HarvestPermitSpeciesAmount speciesAmount = speciesAmounts.iterator().next();
                    assertEquals(species, speciesAmount.getGameSpecies());
                    assertEquals((float)amount, speciesAmount.getAmount(), 0.01f);
                });
            });
        });
    }

    @Test
    public void testPermitAmountSetToZero() {
        withRhy(rhy -> {
            createIntegration(null);
            final HarvestPermit permit = model().newHarvestPermit(rhy);
            final GameSpecies species = model().newGameSpecies();
            final Person person = model().newPerson();

            onSavedAndAuthenticated(createNewAdmin(), () -> {
                final String permitNumber = permit.getPermitNumber();
                final LocalDate today = DateUtil.today();
                final String dates = formatDate(today, today.plusDays(14));
                final int amount = 0;
                LupahallintaHttpClient mockClient = createMockClient(newPermitRow(person, species, amount, rhy, permitNumber, dates));
                LupahallintaPermitImportMailHandler mockMailHandler = createMockMailHandler();
                LupahallintaHarvestPermitImporter importer = new LupahallintaHarvestPermitImporter(importFeature, mockClient, mockMailHandler);

                HarvestPermitImportResultDTO result = importer.doImport();
                assertNoErrors(result);

                ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
                verify(mockMailHandler).handleMessages(captor.capture());
                assertEquals(1, captor.getValue().size());

                runInTransaction(() -> {
                    List<HarvestPermit> all = harvestPermitRepository.findAll(new Sort(HarvestPermit_.id.getName()));
                    assertEquals(1, all.size());

                    HarvestPermit first = all.get(0);
                    Collection<HarvestPermitSpeciesAmount> speciesAmounts = first.getSpeciesAmounts();
                    assertEquals(0, speciesAmounts.size());
                });
            });
        });
    }

    @Test
    public void testPermitAmountSetToZeroWithEmptyDates() {
        withRhy(rhy -> {
            createIntegration(null);
            final HarvestPermit permit = model().newHarvestPermit(rhy);
            final GameSpecies species = model().newGameSpecies();
            final Person person = model().newPerson();

            onSavedAndAuthenticated(createNewAdmin(), () -> {
                final String permitNumber = permit.getPermitNumber();
                final int amount = 0;
                LupahallintaHttpClient mockClient = createMockClient(newPermitRow(person, species, amount, rhy, permitNumber, ""));
                LupahallintaPermitImportMailHandler mockMailHandler = createMockMailHandler();
                LupahallintaHarvestPermitImporter importer = new LupahallintaHarvestPermitImporter(importFeature, mockClient, mockMailHandler);

                HarvestPermitImportResultDTO result = importer.doImport();
                assertNoErrors(result);

                ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
                verify(mockMailHandler).handleMessages(captor.capture());
                assertEquals(1, captor.getValue().size());

                runInTransaction(() -> {
                    List<HarvestPermit> all = harvestPermitRepository.findAll(new Sort(HarvestPermit_.id.getName()));
                    assertEquals(1, all.size());

                    HarvestPermit first = all.get(0);
                    Collection<HarvestPermitSpeciesAmount> speciesAmounts = first.getSpeciesAmounts();
                    assertEquals(0, speciesAmounts.size());
                });
            });
        });
    }

    private void createIntegration(DateTime expected) {
        model().newIntegration(Integration.LH_PERMIT_IMPORT_ID).setLastRun(expected);
    }

    private static String newPermitRow(Person contactPerson, GameSpecies species, int amount,
                                       Riistanhoitoyhdistys rhy, String permitNumber, String dates) {
        String yhteyshenkilo = contactPerson.getSsn();
        String luvansaaja = "111";
        String lupaosakkaat = "222";
        String lupanumero = permitNumber;
        String lupatyypinTunniste = "012";
        String lupatyyppi = "Lupatyyppi testi";
        int elainlajit = species.getOfficialCode();
        int myonnetyt_luvat = amount;
        String lupa_ajat = dates;
        String lupa_ajat2 = "";
        String rajoitteet = "";
        String rajoitettuMaara = "";
        String alkuperainenLupanumero = "";
        String viitenumero = "1232";
        String tulostusUrl = "http://a/test";
        String hta = "";
        String tiedoksiRhyt = "";
        String pintaAla = "";
        return join(Arrays.asList(yhteyshenkilo, luvansaaja, lupaosakkaat, lupanumero, lupatyypinTunniste, lupatyyppi,
                elainlajit, myonnetyt_luvat, lupa_ajat, lupa_ajat2, rhy.getOfficialCode(), rajoitteet, rajoitettuMaara,
                alkuperainenLupanumero, viitenumero, tulostusUrl, hta, tiedoksiRhyt, pintaAla));
    }

    private static String formatDate(LocalDate begin, LocalDate end) {
        DateTimeFormatter format = DateTimeFormat.forPattern("dd.MM.yyyy");
        return format.print(begin) + " - " + format.print(end);
    }

    private static String header() {
        return join(Arrays.asList("yhteyshenkilo", "luvansaaja", "lupaosakkaat", "lupanumero", "paatosmalli", "lupatyyppi",
                "elainlajit", "myonnetyt_luvat", "rajoitus_ehto", "rajoitus_lkm", "lupa_ajat", "lupa_ajat2",
                "RHY", "rajoite_tyyppi", "rajoite_lkm", "alkuperainen_lupanumero", "viite", "paatosURL", "luvan_pinta_ala")) + "\n";
    }

    private static String join(List<? extends Serializable> list) {
        return list
                .stream()
                .map(s -> "\"" + s + "\"")
                .collect(Collectors.joining(";"));
    }

    private static LupahallintaHttpClient createMockClient(Exception e) {
        LupahallintaHttpClient client = Mockito.mock(LupahallintaHttpClient.class);
        when(client.getPermits(any(DateTime.class))).thenThrow(e);
        return client;
    }

    private static LupahallintaHttpClient createMockClient(String... lines) {
        LupahallintaHttpClient client = Mockito.mock(LupahallintaHttpClient.class);
        when(client.getPermits(any(DateTime.class))).thenReturn(new StringReader(StringUtils.join(lines)));
        return client;
    }

    private static LupahallintaPermitImportMailHandler createMockMailHandler() {
        return Mockito.mock(LupahallintaPermitImportMailHandler.class);
    }

    private static void assertNoChanges(HarvestPermitImportResultDTO result) {
        assertEquals(0, result.getModifiedOrAddedCount());
        assertNoErrors(result);
    }

    private static void assertNoErrors(HarvestPermitImportResultDTO result) {
        assertNull(result.getAllErrors());
    }
}

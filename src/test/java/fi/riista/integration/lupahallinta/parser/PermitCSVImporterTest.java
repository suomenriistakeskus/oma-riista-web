package fi.riista.integration.lupahallinta.parser;

import com.google.common.collect.Sets;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesRepository;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.gis.hta.GISHirvitalousalueRepository;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.permit.summary.MooseHuntingSummaryRepository;
import fi.riista.feature.huntingclub.register.RegisterHuntingClubService;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.person.PersonLookupService;
import fi.riista.feature.organization.rhy.MergedRhyMapping;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.util.NumberUtils;
import javaslang.Tuple3;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PermitCSVImporterTest {
    private static final Logger LOG = LoggerFactory.getLogger(PermitCSVImporterTest.class);

    private static final String HTA_ID = "0123_test_hta_id";
    private static final Integer SPECIES1 = 47348;
    private static final GameSpecies GAMESPECIES1 = new GameSpecies(SPECIES1, null, "", "", "");
    private static final Integer SPECIES2 = 47347;
    private static final GameSpecies GAMESPECIES2 = new GameSpecies(SPECIES2, null, "", "", "");
    private static final Integer SPECIES3 = 47346;
    private static final GameSpecies GAMESPECIES3 = new GameSpecies(SPECIES3, null, "", "", "");
    private static final Integer AREA_SIZE = 9870;

    @InjectMocks
    private PermitCSVImporter csvImporter;

    @Mock
    private PersonLookupService personLookupService;

    @Mock
    private RiistanhoitoyhdistysRepository riistanhoitoyhdistysRepository;

    @Mock
    private GameSpeciesRepository gameSpeciesRepository;

    @Mock
    private HarvestPermitRepository harvestPermitRepository;

    @Mock
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    @Mock
    private RegisterHuntingClubService registerHuntingClubService;

    @Mock
    private GISHirvitalousalueRepository hirvitalousalueRepository;

    @Mock
    private MooseHuntingSummaryRepository mooseHuntingSummaryRepository;

    private void mockDefaults(String ssn) {
        Person person = new Person();
        person.setId(123L);

        if (ssn != null) {
            when(personLookupService.findBySsnFallbackVtj(eq(ssn))).thenReturn(Optional.of(person));
        } else {
            when(personLookupService.findBySsnFallbackVtj(anyString())).thenReturn(Optional.empty());
        }

        when(gameSpeciesRepository.findByOfficialCode(eq(SPECIES1))).thenReturn(Optional.of(GAMESPECIES1));
        when(gameSpeciesRepository.findByOfficialCode(eq(SPECIES3))).thenReturn(Optional.of(GAMESPECIES3));

        when(riistanhoitoyhdistysRepository.findByOfficialCode(eq("221"))).thenReturn(new Riistanhoitoyhdistys());

        Riistanhoitoyhdistys lakeudenRhy = new Riistanhoitoyhdistys();
        lakeudenRhy.setOfficialCode(MergedRhyMapping.LAKEUDEN_RHY_334);
        when(riistanhoitoyhdistysRepository.findByOfficialCode(eq(MergedRhyMapping.LAKEUDEN_RHY_334))).thenReturn(lakeudenRhy);

        GISHirvitalousalue hta = new GISHirvitalousalue("", "", "", "", null);
        hta.setId(1);
        when(hirvitalousalueRepository.findByNumber(eq(HTA_ID))).thenReturn(hta);
    }

    private static HarvestPermit assertErrorCountReturnPermit(
            final Tuple3<HarvestPermit, List<String>, PermitCSVLine> res, final int errorCount) {

        final List<String> errorList = res._2();

        if (!errorList.isEmpty() && errorCount != errorList.size()) {
            LOG.info("Errors: {}", errorList);
        }

        assertThat(errorList, Matchers.hasSize(errorCount));

        return res._1();
    }


    public static void assertSpeciesAmounts(Iterable<HarvestPermitSpeciesAmount> speciesAmounts, int code, float amount) {
        for (HarvestPermitSpeciesAmount spa : speciesAmounts) {
            if (spa.getGameSpecies().getOfficialCode() == code && NumberUtils.equal(spa.getAmount(), amount)) {
                return;
            }
        }
        fail("species:" + code + " amount:" + amount + " not found, speciesamounts:" + speciesAmounts);
    }

    @Test
    public void testOk() {
        mockDefaults("111111-109A");

        Tuple3<HarvestPermit, List<String>, PermitCSVLine> res = csvImporter.process(new String[]{
                "111111-109A", "", "", "2014-1-050-00128-6",
                "207", "Karhu 41 A § kannanhoidollinen",
                species(SPECIES1), "1.0", "20.08.2014 - 31.10.2014", "", "221", "", "", "", "", "", "", "", ""});

        HarvestPermit permit = assertErrorCountReturnPermit(res, 0);

        assertNotNull(permit);
        assertEquals(123L, (long) permit.getOriginalContactPerson().getId());
        assertFalse(permit.isHarvestsAsList());
    }

    @Test
    public void testOkHarvestsAsList() {
        mockDefaults("111111-109A");

        Tuple3<HarvestPermit, List<String>, PermitCSVLine> res = csvImporter.process(new String[]{
                "111111-109A", "", "", "2014-1-050-00128-6",
                "210", "41 C§ poikkeuslupa", species(SPECIES1), "1.0", "20.08.2014 - 31.10.2014", "", "221", "", "", "", "", "", "", "", ""});

        HarvestPermit permit = assertErrorCountReturnPermit(res, 0);

        assertNotNull(permit);
        assertEquals(123L, (long) permit.getOriginalContactPerson().getId());
        assertTrue(permit.isHarvestsAsList());
    }

    @Test
    public void testErrorSsnNotFound() {
        mockDefaults(null);

        Tuple3<HarvestPermit, List<String>, PermitCSVLine> res = csvImporter.process(new String[]{
                "111111-109A", "", "", "2014-1-050-00128-6",
                "207", "Karhu 41 A § kannanhoidollinen",
                species(SPECIES1), "1.0", "20.08.2014 - 31.10.2014", "", "221", "", "", "", "", "", "", "", ""});

        HarvestPermit permit = assertErrorCountReturnPermit(res, 1);
        assertNull(permit);
        assertThat(res._2.get(0), startsWith("Henkilöä ei löydy, HETU:"));
    }

    @Test
    public void testRejected() {
        mockDefaults("111111-109A");

        Tuple3<HarvestPermit, List<String>, PermitCSVLine> res = csvImporter.process(new String[]{
                "111111-109A", "", "", "2014-1-050-00128-6",
                "207", "Karhu 41 A § kannanhoidollinen", species(SPECIES1), "0.0", "", "", "221", "", "", "", "", "", "", "", ""});

        HarvestPermit permit = assertErrorCountReturnPermit(res, 0);
        assertNull(permit);
    }

    @Test
    public void testRejected2() {
        mockDefaults("111111-109A");

        Tuple3<HarvestPermit, List<String>, PermitCSVLine> res = csvImporter.process(new String[]{
                "111111-109A", "", "", "2014-1-050-00128-6",
                "207", "Karhu 41 A § kannanhoidollinen", species(SPECIES1, SPECIES2), "0.0,0.0", ",", ",", "221", ",", ",", "", ",", "", "", "", ""});

        HarvestPermit permit = assertErrorCountReturnPermit(res, 0);
        assertNull(permit);
    }

    @Test
    public void testRejectedButDateGiven() {
        mockDefaults("111111-109A");

        Tuple3<HarvestPermit, List<String>, PermitCSVLine> res = csvImporter.process(new String[]{
                "111111-109A", "", "", "2014-1-050-00128-6",
                "207", "Karhu 41 A § kannanhoidollinen", species(SPECIES1), "0.0", "20.08.2014 - 31.10.2014", "", "221", "", "", "", "", "", "", "", ""});

        HarvestPermit permit = assertErrorCountReturnPermit(res, 0);
        assertNull(permit);
    }

    @Test
    public void testRejected_permitAlreadyExists() {
        mockDefaults("111111-109A");
        HarvestPermit existingPermit = new HarvestPermit();
        existingPermit.setId(256L);

        when(harvestPermitRepository.findByPermitNumber(eq("2014-1-050-00128-6"))).thenReturn(existingPermit);

        Tuple3<HarvestPermit, List<String>, PermitCSVLine> res = csvImporter.process(new String[]{
                "111111-109A", "", "", "2014-1-050-00128-6",
                "207", "Karhu 41 A § kannanhoidollinen", species(SPECIES1), "0.0", "", "", "221", "", "", "", "", "", "", "", ""});

        HarvestPermit permit = assertErrorCountReturnPermit(res, 0);
        assertSame(existingPermit, permit);
        assertThat(permit.getSpeciesAmounts(), hasSize(0));
        assertEquals(0, res._2().size());
    }

    @Test
    public void testOneRejectedTwoAccepted() {
        mockDefaults("111111-109A");

        Tuple3<HarvestPermit, List<String>, PermitCSVLine> res = csvImporter.process(new String[]{
                "111111-109A", "", "", "2014-1-050-00128-6",
                "207", "Karhu 41 A § kannanhoidollinen",
                species(SPECIES1, SPECIES2, SPECIES3), "1.0,0.0,2.0", "1.1.2014-2.2.2014,,1.1.2014-2.2.2014", ",,", "221", ",,", ",,", "", ",,", "", "", "", ""});

        HarvestPermit permit = assertErrorCountReturnPermit(res, 0);

        assertThat(permit.getSpeciesAmounts(), hasSize(2));
        assertSpeciesAmounts(permit.getSpeciesAmounts(), SPECIES1, 1.0f);
        assertSpeciesAmounts(permit.getSpeciesAmounts(), SPECIES3, 2.0f);
    }

    @Test
    public void testOldRhyMappedToNew() {
        mockDefaults("111111-109A");

        Tuple3<HarvestPermit, List<String>, PermitCSVLine> res = csvImporter.process(new String[]{
                "111111-109A", "", "", "2014-1-050-00128-6",
                "207", "Karhu 41 A § kannanhoidollinen",
                species(SPECIES1), "1.0", "20.08.2014 - 31.10.2014", "", MergedRhyMapping.SEINAJOEN_RHY_328, "", "", "", "", "", "", "", ""});

        HarvestPermit permit = assertErrorCountReturnPermit(res, 0);

        assertNotNull(permit);
        assertEquals(MergedRhyMapping.LAKEUDEN_RHY_334, permit.getRhy().getOfficialCode());
    }

    @Test
    public void testPermitHolderAndPartnerNotResolved() {
        mockDefaults("111111-109A");

        Tuple3<HarvestPermit, List<String>, PermitCSVLine> res = csvImporter.process(new String[]{
                "111111-109A", "555", "666", "2014-1-050-00128-6",
                "207", "Karhu 41 A § kannanhoidollinen",
                species(SPECIES1), "1.0", "20.08.2014 - 31.10.2014", "", "221", "", "", "", "", "", "", "", ""});

        HarvestPermit permit = assertErrorCountReturnPermit(res, 0);

        assertNotNull(permit);
        verifyNoMoreInteractions(registerHuntingClubService);
    }

    @Test
    public void testPermitHolderAndPartnerAreResolved() {
        mockDefaults("111111-109A");

        when(registerHuntingClubService.findExistingOrCreate(eq("555"))).thenReturn(createClub(1, "555"));
        when(registerHuntingClubService.findExistingOrCreate(eq("666"))).thenReturn(createClub(2, "666"));

        Tuple3<HarvestPermit, List<String>, PermitCSVLine> res = csvImporter.process(new String[]{
                "111111-109A", "555", "555, 666", "2014-1-050-00128-6",
                HarvestPermit.MOOSELIKE_PERMIT_TYPE, "HIRVIELÄIN",
                species(SPECIES1), "1.0", "20.08.2014 - 31.10.2014", "", "221", "", "", "", "1232", "http://invalid/2014-1-050-00999-0", HTA_ID, "221", AREA_SIZE.toString()});

        HarvestPermit permit = assertErrorCountReturnPermit(res, 0);

        assertNotNull(permit);
        assertEquals("555", permit.getPermitHolder().getOfficialCode());
        assertEquals(
                Sets.newHashSet("555", "666"),
                permit.getPermitPartners().stream().map(Organisation::getOfficialCode).collect(Collectors.toSet()));
        assertNotNull(permit.getMooseArea());
        assertEquals(AREA_SIZE, permit.getPermitAreaSize());
    }

    @Test
    public void testPermitHolderAndPartnerShouldBeResolvedButNotFound() {
        mockDefaults("111111-109A");

        Tuple3<HarvestPermit, List<String>, PermitCSVLine> res = csvImporter.process(new String[]{
                "111111-109A", "555", "666", "2014-1-050-00128-6",
                HarvestPermit.MOOSELIKE_PERMIT_TYPE, "HIRVIELÄIN",
                species(SPECIES1), "1.0", "20.08.2014 - 31.10.2014", "", "221", "", "", "", "1232", "http://invalid/2014-1-050-00999-0", HTA_ID, "221", AREA_SIZE.toString()});

        HarvestPermit permit = assertErrorCountReturnPermit(res, 2);

        assertThat(res._2.get(0), is("Luvansaajaa ei löydy:555"));
        assertThat(res._2.get(1), is("Lupaosakasta ei löydy:666"));

        assertNull(permit);
    }

    private static HuntingClub createClub(long id, String officialCode) {
        HuntingClub club = new HuntingClub();
        club.setId(id);
        club.setOfficialCode(officialCode);
        return club;
    }

    @Test
    public void testOriginalPermitNumberResolved() {
        mockDefaults("111111-109A");
        HarvestPermit existingPermit = new HarvestPermit();
        existingPermit.setId(256L);

        existingPermit.setPermitHolder(createClub(1L, "1"));
        existingPermit.setPermitPartners(Sets.newHashSet(createClub(2L, "2"), createClub(3L, "3")));

        when(harvestPermitRepository.findByPermitNumber(eq("2014-1-050-00128-6"))).thenReturn(existingPermit);

        Tuple3<HarvestPermit, List<String>, PermitCSVLine> res = csvImporter.process(new String[]{
                "111111-109A", "555", "666", "2014-1-050-00999-0",
                "190", "Uusi lupa ML 29 §",
                species(SPECIES1), "1.0", "20.08.2014 - 31.10.2014", "", "221", "", "", "2014-1-050-00128-6", "", "", "", "", ""});

        HarvestPermit permit = assertErrorCountReturnPermit(res, 0);

        assertNotNull(permit);

        assertNotNull(permit.getOriginalPermit());
        assertEquals(existingPermit.getId(), permit.getOriginalPermit().getId());

        assertEquals(existingPermit.getPermitHolder(), permit.getPermitHolder());
        assertEquals(existingPermit.getPermitPartners(), permit.getPermitPartners());
    }

    @Test
    public void testOriginalPermitNumberNotFoundThenNoErrorsAndPermitIsSkipped() {
        mockDefaults("111111-109A");

        Tuple3<HarvestPermit, List<String>, PermitCSVLine> res = csvImporter.process(new String[]{
                "111111-109A", "555", "666", "2014-1-050-00999-0",
                "190", "Uusi lupa ML 29 §",
                species(SPECIES1), "1.0", "20.08.2014 - 31.10.2014", "", "221", "", "", "2014-1-050-00128-6", "", "", "", "", ""});

        HarvestPermit permit = assertErrorCountReturnPermit(res, 0);

        assertNull(permit);
    }

    @Test
    public void testMooselikePermitHtaNotGiven() {
        mockDefaults("111111-109A");
        HarvestPermit existingPermit = new HarvestPermit();
        existingPermit.setId(256L);

        existingPermit.setPermitHolder(createClub(1L, "1"));
        existingPermit.setPermitPartners(Sets.newHashSet(createClub(2L, "2"), createClub(3L, "3")));

        when(harvestPermitRepository.findByPermitNumber(eq("2014-1-050-00128-6"))).thenReturn(existingPermit);

        Tuple3<HarvestPermit, List<String>, PermitCSVLine> res = csvImporter.process(new String[]{
                "111111-109A", "555", "666", "2014-1-050-00999-0",
                HarvestPermit.MOOSELIKE_PERMIT_TYPE, "hirvieläin",
                species(SPECIES1), "1.0", "20.08.2014 - 31.10.2014", "", "221", "", "", "2014-1-050-00128-6", "1232", "http://invalid/2014-1-050-00999-0", "", "221", AREA_SIZE.toString()});

        HarvestPermit permit = assertErrorCountReturnPermit(res, 1);

        assertNull(permit);
        assertThat(res._2.get(0), is("Lupatyypille vaaditaan hirvitaloustalue mutta ei löydy:"));
    }

    @Test
    public void testMooselikePermitHtaGivenButNotFound() {
        mockDefaults("111111-109A");
        HarvestPermit existingPermit = new HarvestPermit();
        existingPermit.setId(256L);

        existingPermit.setPermitHolder(createClub(1L, "1"));
        existingPermit.setPermitPartners(Sets.newHashSet(createClub(2L, "2"), createClub(3L, "3")));

        when(harvestPermitRepository.findByPermitNumber(eq("2014-1-050-00128-6"))).thenReturn(existingPermit);

        Tuple3<HarvestPermit, List<String>, PermitCSVLine> res = csvImporter.process(new String[]{
                "111111-109A", "555", "666", "2014-1-050-00999-0",
                HarvestPermit.MOOSELIKE_PERMIT_TYPE, "hirvieläin",
                species(SPECIES1), "1.0", "20.08.2014 - 31.10.2014", "", "221", "", "", "2014-1-050-00128-6", "1232", "http://invalid/2014-1-050-00999-0", "htaXNotFound", "221", AREA_SIZE.toString()});

        HarvestPermit permit = assertErrorCountReturnPermit(res, 1);

        assertNull(permit);
        assertThat(res._2.get(0), is("Lupatyypille vaaditaan hirvitaloustalue mutta ei löydy:htaXNotFound"));
    }

    @Test
    public void testOtherPermitHtaGivenIsSkipped() {
        mockDefaults("111111-109A");
        HarvestPermit existingPermit = new HarvestPermit();
        existingPermit.setId(256L);

        existingPermit.setPermitHolder(createClub(1L, "1"));
        existingPermit.setPermitPartners(Sets.newHashSet(createClub(2L, "2"), createClub(3L, "3")));

        when(harvestPermitRepository.findByPermitNumber(eq("2014-1-050-00128-6"))).thenReturn(existingPermit);

        Tuple3<HarvestPermit, List<String>, PermitCSVLine> res = csvImporter.process(new String[]{
                "111111-109A", "555", "666", "2014-1-050-00999-0",
                "123", "jokulupatyyppi",
                species(SPECIES1), "1.0", "20.08.2014 - 31.10.2014", "", "221", "", "", "2014-1-050-00128-6", "1232", "http://invalid/2014-1-050-00999-0", HTA_ID, "", ""});

        HarvestPermit permit = assertErrorCountReturnPermit(res, 0);

        assertNotNull(permit);
        assertNull(permit.getMooseArea());
    }

    @Test
    public void testOtherPermitHtaGivenButNotFoundIsOk() {
        mockDefaults("111111-109A");
        HarvestPermit existingPermit = new HarvestPermit();
        existingPermit.setId(256L);

        existingPermit.setPermitHolder(createClub(1L, "1"));
        existingPermit.setPermitPartners(Sets.newHashSet(createClub(2L, "2"), createClub(3L, "3")));

        when(harvestPermitRepository.findByPermitNumber(eq("2014-1-050-00128-6"))).thenReturn(existingPermit);

        Tuple3<HarvestPermit, List<String>, PermitCSVLine> res = csvImporter.process(new String[]{
                "111111-109A", "555", "666", "2014-1-050-00999-0",
                "123", "jokulupatyyppi",
                species(SPECIES1), "1.0", "20.08.2014 - 31.10.2014", "", "221", "", "", "2014-1-050-00128-6", "1232", "http://invalid/2014-1-050-00999-0", "htaXNotFound", "", ""});

        HarvestPermit permit = assertErrorCountReturnPermit(res, 0);

        assertNotNull(permit);
        assertNull(permit.getMooseArea());
    }

    @Test
    public void testMooselikePermitUrlIsNotGiven() {
        mockDefaults("111111-109A");
        HarvestPermit existingPermit = new HarvestPermit();
        existingPermit.setId(256L);

        existingPermit.setPermitHolder(createClub(1L, "1"));
        existingPermit.setPermitPartners(Sets.newHashSet(createClub(2L, "2"), createClub(3L, "3")));

        when(harvestPermitRepository.findByPermitNumber(eq("2014-1-050-00128-6"))).thenReturn(existingPermit);

        Tuple3<HarvestPermit, List<String>, PermitCSVLine> res = csvImporter.process(new String[]{
                "111111-109A", "555", "666", "2014-1-050-00999-0",
                HarvestPermit.MOOSELIKE_PERMIT_TYPE, "hirvieläin",
                species(SPECIES1), "1.0", "20.08.2014 - 31.10.2014", "", "221", "", "", "2014-1-050-00128-6", "1232", "", HTA_ID, "221", AREA_SIZE.toString()});

        HarvestPermit permit = assertErrorCountReturnPermit(res, 1);

        assertNull(permit);
        assertThat(res._2.get(0), is("Lupatyypille vaaditaan päätöksen URL, annettu arvo ei kelpaa:"));
    }

    @Test
    public void testMooselikePermitUrlIsNotValidUrl() {
        mockDefaults("111111-109A");
        HarvestPermit existingPermit = new HarvestPermit();
        existingPermit.setId(256L);

        existingPermit.setPermitHolder(createClub(1L, "1"));
        existingPermit.setPermitPartners(Sets.newHashSet(createClub(2L, "2"), createClub(3L, "3")));

        when(harvestPermitRepository.findByPermitNumber(eq("2014-1-050-00128-6"))).thenReturn(existingPermit);

        Tuple3<HarvestPermit, List<String>, PermitCSVLine> res = csvImporter.process(new String[]{
                "111111-109A", "555", "666", "2014-1-050-00999-0",
                HarvestPermit.MOOSELIKE_PERMIT_TYPE, "hirvieläin",
                species(SPECIES1), "1.0", "20.08.2014 - 31.10.2014", "", "221", "", "", "2014-1-050-00128-6", "1232", "asdf", HTA_ID, "221", AREA_SIZE.toString()});

        HarvestPermit permit = assertErrorCountReturnPermit(res, 1);

        assertNull(permit);
        assertThat(res._2.get(0), is("Lupatyypille vaaditaan päätöksen URL, annettu arvo ei kelpaa:asdf"));
    }

    @Test
    public void testMooselikePermitRelatedRhysInvalid() {
        mockDefaults("111111-109A");
        HarvestPermit existingPermit = new HarvestPermit();
        existingPermit.setId(256L);

        existingPermit.setPermitHolder(createClub(1L, "1"));
        existingPermit.setPermitPartners(Sets.newHashSet(createClub(2L, "2"), createClub(3L, "3")));

        when(harvestPermitRepository.findByPermitNumber(eq("2014-1-050-00128-6"))).thenReturn(existingPermit);

        Tuple3<HarvestPermit, List<String>, PermitCSVLine> res = csvImporter.process(new String[]{
                "111111-109A", "555", "666", "2014-1-050-00128-6",
                HarvestPermit.MOOSELIKE_PERMIT_TYPE, "hirvieläin",
                species(SPECIES1), "1.0", "20.08.2014 - 31.10.2014", "", "221", "", "", "2014-1-050-00128-6", "1232", "http://it.works", HTA_ID, "000", AREA_SIZE.toString()});

        HarvestPermit permit = assertErrorCountReturnPermit(res, 1);

        assertNull(permit);
        assertThat(res._2.get(0), is("RHY ei löydy:000"));
    }

    @Test
    public void testMooselikePermitAreaSizeNotNumber() {
        mockDefaults("111111-109A");
        HarvestPermit existingPermit = new HarvestPermit();
        existingPermit.setId(256L);

        existingPermit.setPermitHolder(createClub(1L, "1"));
        existingPermit.setPermitPartners(Sets.newHashSet(createClub(2L, "2"), createClub(3L, "3")));

        when(harvestPermitRepository.findByPermitNumber(eq("2014-1-050-00128-6"))).thenReturn(existingPermit);

        Tuple3<HarvestPermit, List<String>, PermitCSVLine> res = csvImporter.process(new String[]{
                "111111-109A", "555", "666", "2014-1-050-00128-6",
                HarvestPermit.MOOSELIKE_PERMIT_TYPE, "hirvieläin",
                species(SPECIES1), "1.0", "20.08.2014 - 31.10.2014", "", "221", "", "", "2014-1-050-00128-6", "1232", "http://it.works", HTA_ID, "", "x"});

        HarvestPermit permit = assertErrorCountReturnPermit(res, 1);

        assertNull(permit);
        assertThat(res._2.get(0), is("Lupatyypille vaaditaan alueen pinta-ala, annettu arvo ei kelpaa:x"));
    }

    @Test
    public void testMooselikePermitAreaSizeZero() {
        mockDefaults("111111-109A");
        HarvestPermit existingPermit = new HarvestPermit();
        existingPermit.setId(256L);

        existingPermit.setPermitHolder(createClub(1L, "1"));
        existingPermit.setPermitPartners(Sets.newHashSet(createClub(2L, "2"), createClub(3L, "3")));

        when(harvestPermitRepository.findByPermitNumber(eq("2014-1-050-00128-6"))).thenReturn(existingPermit);

        Tuple3<HarvestPermit, List<String>, PermitCSVLine> res = csvImporter.process(new String[]{
                "111111-109A", "555", "666", "2014-1-050-00128-6",
                HarvestPermit.MOOSELIKE_PERMIT_TYPE, "hirvieläin",
                species(SPECIES1), "1.0", "20.08.2014 - 31.10.2014", "", "221", "", "", "2014-1-050-00128-6", "1232", "http://it.works", HTA_ID, "", "0"});

        HarvestPermit permit = assertErrorCountReturnPermit(res, 1);

        assertNull(permit);
        assertThat(res._2.get(0), is("Lupatyypille vaaditaan alueen pinta-ala, annettu arvo ei kelpaa:0"));
    }

    @Test
    public void testMooselikePermitAreaSizeEmptyString() {
        mockDefaults("111111-109A");
        HarvestPermit existingPermit = new HarvestPermit();
        existingPermit.setId(256L);

        existingPermit.setPermitHolder(createClub(1L, "1"));
        existingPermit.setPermitPartners(Sets.newHashSet(createClub(2L, "2"), createClub(3L, "3")));

        when(harvestPermitRepository.findByPermitNumber(eq("2014-1-050-00128-6"))).thenReturn(existingPermit);

        Tuple3<HarvestPermit, List<String>, PermitCSVLine> res = csvImporter.process(new String[]{
                "111111-109A", "555", "666", "2014-1-050-00128-6",
                HarvestPermit.MOOSELIKE_PERMIT_TYPE, "hirvieläin",
                species(SPECIES1), "1.0", "20.08.2014 - 31.10.2014", "", "221", "", "", "2014-1-050-00128-6", "1232", "http://it.works", HTA_ID, "", "  "});

        HarvestPermit permit = assertErrorCountReturnPermit(res, 1);

        assertNull(permit);
        assertThat(res._2.get(0), is("Lupatyypille vaaditaan alueen pinta-ala, annettu arvo ei kelpaa:  "));
    }

    @Test
    public void testMooselikePermitAreaSizeNegative() {
        mockDefaults("111111-109A");
        HarvestPermit existingPermit = new HarvestPermit();
        existingPermit.setId(256L);

        existingPermit.setPermitHolder(createClub(1L, "1"));
        existingPermit.setPermitPartners(Sets.newHashSet(createClub(2L, "2"), createClub(3L, "3")));

        when(harvestPermitRepository.findByPermitNumber(eq("2014-1-050-00128-6"))).thenReturn(existingPermit);

        Tuple3<HarvestPermit, List<String>, PermitCSVLine> res = csvImporter.process(new String[]{
                "111111-109A", "555", "666", "2014-1-050-00128-6",
                HarvestPermit.MOOSELIKE_PERMIT_TYPE, "hirvieläin",
                species(SPECIES1), "1.0", "20.08.2014 - 31.10.2014", "", "221", "", "", "2014-1-050-00128-6", "1232", "http://it.works", HTA_ID, "", "-1"});

        HarvestPermit permit = assertErrorCountReturnPermit(res, 1);

        assertNull(permit);
        assertThat(res._2.get(0), is("Lupatyypille vaaditaan alueen pinta-ala, annettu arvo ei kelpaa:-1"));
    }

    @Test
    public void testMooselikePermit_UpdatedSpeciesAmountsPreserved_removedDeleted() {
        mockDefaults("111111-109A");
        HarvestPermit existingPermit = new HarvestPermit();
        existingPermit.setId(256L);

        HuntingClub club = createClub(1, "555");
        when(registerHuntingClubService.findExistingOrCreate(eq("555"))).thenReturn(club);
        HuntingClub club1 = createClub(2, "666");
        when(registerHuntingClubService.findExistingOrCreate(eq("666"))).thenReturn(club1);

        existingPermit.setPermitHolder(club);
        existingPermit.setPermitPartners(Sets.newHashSet(club1));
        HarvestPermitSpeciesAmount existingHpsa = createSpeciesAmount(existingPermit, 1L, GAMESPECIES1, 1.0f);
        HarvestPermitSpeciesAmount toBeDeletedHpsa = createSpeciesAmount(existingPermit, 2L, GAMESPECIES2, 2.0f);
        HarvestPermitSpeciesAmount newHpsa = createSpeciesAmount(existingPermit, 3L, GAMESPECIES3, 3.0f);

        when(harvestPermitRepository.findByPermitNumber(eq("2014-1-050-00128-6"))).thenReturn(existingPermit);

        Tuple3<HarvestPermit, List<String>, PermitCSVLine> res = csvImporter.process(new String[]{
                "111111-109A", "555", "666", "2014-1-050-00128-6",
                HarvestPermit.MOOSELIKE_PERMIT_TYPE, "hirvieläin",
                species(SPECIES1, SPECIES3), "1.0,13.0", "20.08.2014 - 31.10.2014,20.08.2014 - 31.10.2014", ",", "221", ",", ",", ",", "1232,1232", "http://it.works", HTA_ID, "", AREA_SIZE.toString()});

        HarvestPermit permit = assertErrorCountReturnPermit(res, 0);

        assertNotNull(permit);
        assertEquals(2, permit.getSpeciesAmounts().size());

        // assert new ones are not created
        assertEquals(existingHpsa, permit.getSpeciesAmounts().get(0));
        assertEquals(newHpsa, permit.getSpeciesAmounts().get(1));

        assertSpeciesAmounts(permit.getSpeciesAmounts(), SPECIES1, 1.0f);
        assertSpeciesAmounts(permit.getSpeciesAmounts(), SPECIES3, 13.0f);
    }

    @Test
    public void testOtherThanMooselikePermit_speciesAmountsDeletedAndCreatedNew() {
        mockDefaults("111111-109A");
        HarvestPermit existingPermit = new HarvestPermit();
        existingPermit.setId(256L);

        HarvestPermitSpeciesAmount existingHpsa = createSpeciesAmount(existingPermit, 1L, GAMESPECIES1, 1.0f);
        HarvestPermitSpeciesAmount toBeDeletedHpsa = createSpeciesAmount(existingPermit, 2L, GAMESPECIES2, 2.0f);
        HarvestPermitSpeciesAmount newHpsa = createSpeciesAmount(existingPermit, 3L, GAMESPECIES3, 3.0f);

        when(harvestPermitRepository.findByPermitNumber(eq("2014-1-050-00128-6"))).thenReturn(existingPermit);

        Tuple3<HarvestPermit, List<String>, PermitCSVLine> res = csvImporter.process(new String[]{
                "111111-109A", "", "", "2014-1-050-00128-6",
                "201", "jokumuu",
                species(SPECIES1, SPECIES3), "1.0,13.0", "20.08.2014 - 31.10.2014,20.08.2014 - 31.10.2014", ",", "221", ",", ",", ",", "1232,1232", "http://it.works", HTA_ID, "", ""});

        HarvestPermit permit = assertErrorCountReturnPermit(res, 0);

        assertNotNull(permit);
        assertEquals(2, permit.getSpeciesAmounts().size());

        // assert new ones are created
        assertNotEquals(existingHpsa, permit.getSpeciesAmounts().get(0));
        assertNotEquals(newHpsa, permit.getSpeciesAmounts().get(1));

        assertSpeciesAmounts(permit.getSpeciesAmounts(), SPECIES1, 1.0f);
        assertSpeciesAmounts(permit.getSpeciesAmounts(), SPECIES3, 13.0f);
    }

    private static HarvestPermitSpeciesAmount createSpeciesAmount(HarvestPermit existingPermit, long id, GameSpecies species, float amount) {
        HarvestPermitSpeciesAmount hpsa = new HarvestPermitSpeciesAmount();
        hpsa.setId(id);
        hpsa.setGameSpecies(species);
        hpsa.setAmount(amount);
        existingPermit.getSpeciesAmounts().add(hpsa);
        return hpsa;
    }

    private static String species(Integer... codes) {
        return Stream.of(codes).map(Object::toString).collect(joining(","));
    }
}

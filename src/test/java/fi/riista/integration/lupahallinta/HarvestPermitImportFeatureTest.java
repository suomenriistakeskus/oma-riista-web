package fi.riista.integration.lupahallinta;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.integration.lupahallinta.parser.PermitCSVImporterTest;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class HarvestPermitImportFeatureTest extends EmbeddedDatabaseTest {

    private static final String PERMIT_HOLDER = "20131129206585";
    private static final String PERMIT_NUMBER = "2014-1-050-00128-6";
    private static final String CREDITOR_REFERENCE = "12345 67890 12345 67894";
    private static final String PERMIT_TYPE = "Karhu 41 A § kannanhoidollinen";
    private static final String PERMIT_TYPE_CODE = "207";
    private static final String PRINTING_URL = "http://address.printing.url";
    private static final String GAME_SPECIES_NAME = "Hirvi";
    private static final String PERMIT_AREA_SIZE = "9870";
    private static final String RESTRICTION_TYPE = "AE";

    @Resource
    private HarvestPermitImportFeature harvestPermitImportFeature;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Test
    public void testHappyCase() throws IOException, HarvestPermitImportException {
        final Person person = model().newPerson();
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final GameSpecies species = model().newGameSpecies();

        SystemUser admin = createNewAdmin();
        Reader reader = createReaderForOneRowData(
                person.getSsn(), PERMIT_HOLDER, "", PERMIT_NUMBER, PERMIT_TYPE_CODE, PERMIT_TYPE,
                species.getOfficialCode(), "1.0", "1.04.2014 - 28.5.2014", "15.7.2014 - 23.10.2014",
                rhy.getOfficialCode(), RESTRICTION_TYPE, "1.0", "", CREDITOR_REFERENCE, PRINTING_URL, "", "", "");

        persistInNewTransaction();

        authenticate(admin);

        HarvestPermitImportResultDTO res = harvestPermitImportFeature.doImport(reader, "aTest", DateTime.now());
        assertEquals(1, res.getModifiedOrAddedCount());
        assertNull(res.getAllErrors());

        runInTransaction(() -> {
            List<HarvestPermit> permits = harvestPermitRepository.findAll();
            assertEquals(1, permits.size());

            HarvestPermit permit = permits.get(0);
            assertEquals(person, permit.getOriginalContactPerson());
            assertEquals(PERMIT_NUMBER, permit.getPermitNumber());
            assertEquals(PERMIT_TYPE_CODE, permit.getPermitTypeCode());
            assertEquals(PERMIT_TYPE, permit.getPermitType());
            assertEquals(rhy, permit.getRhy());
            assertEquals(PRINTING_URL, permit.getPrintingUrl());

            assertEquals(1, permit.getSpeciesAmounts().size());
            HarvestPermitSpeciesAmount speciesAmount = permit.getSpeciesAmounts().iterator().next();
            assertEquals(species, speciesAmount.getGameSpecies());
            assertEquals(1.0f, speciesAmount.getSpecimenAmount(), 0.01f);
            assertEquals(HarvestPermitSpeciesAmount.RestrictionType.AE, speciesAmount.getRestrictionType());
            assertEquals(1.0f, speciesAmount.getRestrictionAmount(), 0.01f);
            assertEquals(d(2014, 4, 1), speciesAmount.getBeginDate());
            assertEquals(d(2014, 5, 28), speciesAmount.getEndDate());
            assertEquals(d(2014, 7, 15), speciesAmount.getBeginDate2());
            assertEquals(d(2014, 10, 23), speciesAmount.getEndDate2());
        });
    }

    @Test
    public void testDuplicatePermitNumber() throws IOException {
        Person person = model().newPerson();
        Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        GameSpecies species = model().newGameSpecies();

        SystemUser admin = createNewAdmin();
        String row = csvRow(
                person.getSsn(), PERMIT_HOLDER, "", PERMIT_NUMBER, PERMIT_TYPE_CODE, PERMIT_TYPE,
                species.getOfficialCode(), "1.0", "20.08.2014 - 31.10.2014", "", rhy.getOfficialCode(), "", "", "", "", "", "", "", "");
        StringReader reader = createReaderForRows(row, row);

        persistInNewTransaction();

        authenticate(admin);

        try {
            harvestPermitImportFeature.doImport(reader, "aTest", DateTime.now());
            fail();
        } catch (HarvestPermitImportException hpie) {
            assertEquals(1, hpie.getAllErrors().size());
        }

        runInTransaction(() -> {
            List<HarvestPermit> permits = harvestPermitRepository.findAll();
            assertEquals(0, permits.size());
        });
    }

    @Test
    public void testZeroAmount() throws IOException, HarvestPermitImportException {
        Person person = model().newPerson();
        Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        GameSpecies species = model().newGameSpecies();

        SystemUser admin = createNewAdmin();
        Reader reader = createReaderForOneRowData(
                person.getSsn(), PERMIT_HOLDER, "", PERMIT_NUMBER, PERMIT_TYPE_CODE, PERMIT_TYPE,
                species.getOfficialCode(), "0.0", "20.08.2014 - 31.10.2014", "", rhy.getOfficialCode(), "", "", "", "", "", "", "", "");

        persistInNewTransaction();

        authenticate(admin);

        HarvestPermitImportResultDTO res = harvestPermitImportFeature.doImport(reader, "aTest", DateTime.now());
        assertEquals(0, res.getModifiedOrAddedCount());
        assertNull(res.getAllErrors());
    }

    @Test
    public void testAllZeroAmount() throws IOException, HarvestPermitImportException {
        Person person = model().newPerson();
        Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        GameSpecies species = model().newGameSpecies();
        GameSpecies species2 = model().newGameSpecies();

        SystemUser admin = createNewAdmin();
        Reader reader = createReaderForOneRowData(
                person.getSsn(), PERMIT_HOLDER, "", PERMIT_NUMBER, PERMIT_TYPE_CODE, PERMIT_TYPE,
                speciesCodes(species, species2), "0.0,0.0", ",", ",", rhy.getOfficialCode(), ",", ",", "", ",", "", "", "", "");

        persistInNewTransaction();

        authenticate(admin);

        HarvestPermitImportResultDTO res = harvestPermitImportFeature.doImport(reader, "aTest", DateTime.now());
        assertEquals(0, res.getModifiedOrAddedCount());
        assertNull(res.getAllErrors());
    }

    @Test
    public void testZeroAmountToExistingPermit() throws IOException, HarvestPermitImportException {
        Person person = model().newPerson();
        HarvestPermit permit = model().newHarvestPermit();
        Riistanhoitoyhdistys rhy = permit.getRhy();
        GameSpecies species = model().newGameSpecies();
        persistInNewTransaction();

        permit.getSpeciesAmounts().add(createAmount(permit, species, 1f));

        SystemUser admin = createNewAdmin();
        Reader reader = createReaderForOneRowData(person.getSsn(), PERMIT_HOLDER, "", permit.getPermitNumber(),
                PERMIT_TYPE_CODE, PERMIT_TYPE, species.getOfficialCode(), "0.0", "20.08.2014 - 31.10.2014", "",
                rhy.getOfficialCode(), "", "", "", "", "", "", "", "");

        persistInNewTransaction();

        authenticate(admin);

        HarvestPermitImportResultDTO res = harvestPermitImportFeature.doImport(reader, "aTest", DateTime.now());
        assertEquals(1, res.getModifiedOrAddedCount());
        assertNull(res.getAllErrors());
        assertEquals(1, res.getMessages().size());

        runInTransaction(() -> assertEquals(1, harvestPermitRepository.findAll().size()));
    }

    private HarvestPermitSpeciesAmount createAmount(HarvestPermit permit, GameSpecies species, float amount) {
        HarvestPermitSpeciesAmount a = model().newHarvestPermitSpeciesAmount(permit, species, amount);
        a.setBeginDate(DateUtil.today());
        a.setEndDate(DateUtil.today());
        return a;
    }

    @Test
    public void testOneZeroAmountInMiddle() throws IOException, HarvestPermitImportException {
        Person person = model().newPerson();
        Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final GameSpecies species = model().newGameSpecies();
        final GameSpecies species2 = model().newGameSpecies();
        final GameSpecies species3 = model().newGameSpecies();

        SystemUser admin = createNewAdmin();
        Reader reader = createReaderForOneRowData(
                person.getSsn(), PERMIT_HOLDER, "", PERMIT_NUMBER, PERMIT_TYPE_CODE, PERMIT_TYPE,
                speciesCodes(species, species2, species3),
                "1.0,0.0,2.0",
                "20.08.2014 - 31.10.2014,,20.08.2014 - 31.10.2014",
                ",,",
                rhy.getOfficialCode(),
                ",,", ",,",
                "", ",,", "", "", "", "");

        persistInNewTransaction();

        authenticate(admin);

        HarvestPermitImportResultDTO res = harvestPermitImportFeature.doImport(reader, "aTest", DateTime.now());
        assertNull(res.getAllErrors());
        assertEquals(1, res.getModifiedOrAddedCount());

        runInTransaction(() -> {
            List<HarvestPermit> permits = harvestPermitRepository.findAll();
            assertEquals(1, permits.size());
            HarvestPermit permit = permits.get(0);
            assertEquals(2, permit.getSpeciesAmounts().size());
            PermitCSVImporterTest.assertSpeciesAmounts(permit.getSpeciesAmounts(), species.getOfficialCode(), 1.0f);
            PermitCSVImporterTest.assertSpeciesAmounts(permit.getSpeciesAmounts(), species3.getOfficialCode(), 2.0f);
        });
    }

    @Test
    public void testNewPermitReferencesToOriginalPermit() throws IOException, HarvestPermitImportException {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HarvestPermit originalPermit = model().newHarvestPermit(rhy);
        final Person person = model().newPerson();
        final GameSpecies species = model().newGameSpecies();

        SystemUser admin = createNewAdmin();
        // new permit which references to original permit
        Reader reader = createReaderForOneRowData(
                person.getSsn(), PERMIT_HOLDER, "", PERMIT_NUMBER, PERMIT_TYPE_CODE, PERMIT_TYPE,
                species.getOfficialCode(),
                "1.0",
                "1.04.2014 - 28.5.2014", "15.7.2014 - 23.10.2014",
                rhy.getOfficialCode(),
                RESTRICTION_TYPE, "1.0",
                originalPermit.getPermitNumber(), "", "", "", "", "");

        persistInNewTransaction();

        authenticate(admin);

        HarvestPermitImportResultDTO res = harvestPermitImportFeature.doImport(reader, "aTest", DateTime.now());
        assertEquals(1, res.getModifiedOrAddedCount());
        assertNull(res.getAllErrors());

        runInTransaction(() -> {
            List<HarvestPermit> permits = harvestPermitRepository.findAll();
            assertEquals(2, permits.size());
            Collections.sort(permits, Comparator.comparingLong(HasID::getId));
            HarvestPermit origPermit = permits.get(0);
            HarvestPermit newPermit = permits.get(1);

            assertEquals(originalPermit.getId(), origPermit.getId());
            assertEquals(origPermit.getId(), newPermit.getOriginalPermit().getId());
        });

    }

    @Test
    public void testMooselikePermitUpdatedWhenPartnerFinishedHunting()
            throws IOException, HarvestPermitImportException {

        final GameSpecies species = model().newGameSpecies();
        final Person person = model().newPerson();
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();

        final HuntingClub club = model().newHuntingClub(rhy);
        final HuntingClubGroup group = model().newHuntingClubGroup(club);

        final HarvestPermit originalPermit = model().newMooselikePermit(rhy);
        originalPermit.setHuntingClub(club);
        originalPermit.setPermitHolder(PermitHolder.createHolderForClub(club));
        originalPermit.getPermitPartners().add(club);
        group.updateHarvestPermit(originalPermit);

        final GISHirvitalousalue hta = model().newGISHirvitalousalue();

        // Intermediary flush needed before persisting MooseHuntingSummary in order to have
        // harvest_permit_partners table populated required for foreign key constraint.
        persistInNewTransaction();

        // update should be ok even if partner has finished hunting
        model().newMooseHuntingSummary(originalPermit, club, true);

        SystemUser admin = createNewAdmin();
        Reader reader = createReaderForOneRowData(
                person.getSsn(), club.getOfficialCode(), club.getOfficialCode(), originalPermit.getPermitNumber(),
                PermitTypeCode.MOOSELIKE, GAME_SPECIES_NAME,
                species.getOfficialCode(),
                "1.0",
                "1.04.2014 - 28.5.2014", "15.7.2014 - 31.7.2014",
                rhy.getOfficialCode(),
                RESTRICTION_TYPE, "1.0",
                "", "1232", PRINTING_URL, hta.getNumber(), rhy.getOfficialCode(), PERMIT_AREA_SIZE);

        persistInNewTransaction();

        authenticate(admin);

        HarvestPermitImportResultDTO res = harvestPermitImportFeature.doImport(reader, "aTest", DateTime.now());
        assertEquals(1, res.getModifiedOrAddedCount());
        assertNull(res.getAllErrors());
    }

    @Test
    public void testMooselikePermitUpdatedWhenPartnerHasGroupReferencingToPermit() throws IOException {
        final GameSpecies species = model().newGameSpecies();
        final Person person = model().newPerson();
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();

        final HuntingClub club = model().newHuntingClub(rhy);

        final HuntingClub partner = model().newHuntingClub(rhy);
        final HuntingClubGroup partnerGroup = model().newHuntingClubGroup(partner);

        final HarvestPermit originalPermit = model().newMooselikePermit(rhy);
        originalPermit.setHuntingClub(club);
        originalPermit.setPermitHolder(PermitHolder.createHolderForClub(club));
        originalPermit.getPermitPartners().add(club);
        originalPermit.getPermitPartners().add(partner);
        partnerGroup.updateHarvestPermit(originalPermit);

        final GISHirvitalousalue hta = model().newGISHirvitalousalue();

        SystemUser admin = createNewAdmin();
        Reader reader = createReaderForOneRowData(
                person.getSsn(), club.getOfficialCode(), club.getOfficialCode(), originalPermit.getPermitNumber(),
                PermitTypeCode.MOOSELIKE, GAME_SPECIES_NAME,
                species.getOfficialCode(),
                "1.0",
                "1.04.2014 - 28.5.2014", "15.7.2014 - 31.7.2014",
                rhy.getOfficialCode(),
                RESTRICTION_TYPE, "1.0",
                "", "1232", PRINTING_URL, hta.getNumber(), rhy.getOfficialCode(), PERMIT_AREA_SIZE);

        persistInNewTransaction();

        authenticate(admin);

        try {
            harvestPermitImportFeature.doImport(reader, "aTest", DateTime.now());
            fail("Should throw exception");
        } catch (HarvestPermitImportException ie) {
            assertEquals(1, ie.getAllErrors().size());
            assertEquals(1, ie.getAllErrors().get(0).getErrors().size());
            assertEquals("Osakas yritetään poistaa mutta osakas on lupaan liitettyjä ryhmiä, osakas:" + partner.getOfficialCode(),
                    ie.getAllErrors().get(0).getErrors().get(0));
        }
    }

    private static String speciesCodes(GameSpecies... species) {
        return Arrays.stream(species)
            .filter(Objects::nonNull)
            .map(GameSpecies::getOfficialCode)
            .map(Objects::toString)
            .collect(Collectors.joining(","));
    }

    private static StringReader createReaderForOneRowData(Object... data) {
        return new StringReader(csvRow(data));
    }

    private static StringReader createReaderForRows(String... rows) {
        return new StringReader(StringUtils.join(rows, '\n'));
    }

    private static String csvRow(Object... data) {
        return StringUtils.join(data, HarvestPermitImportFeature.SEPARATOR);
    }

    private static LocalDate d(int year, int month, int day) {
        return new LocalDate(year, month, day);
    }
}

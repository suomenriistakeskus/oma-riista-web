package fi.riista.feature.harvestregistry.quartz;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import fi.riista.config.Constants;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.harvestpermit.season.HarvestArea;
import fi.riista.feature.harvestpermit.season.HarvestQuota;
import fi.riista.feature.harvestpermit.season.HarvestSeason;
import fi.riista.feature.harvestregistry.HarvestRegistryItem;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.DefaultEntitySupplierProvider;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static fi.riista.feature.gamediary.GameGender.FEMALE;
import static fi.riista.feature.gamediary.GameGender.MALE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAN_GOOSE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_CANADIAN_BEAVER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_COMMON_EIDER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_COOT;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_EUROPEAN_BEAVER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_EUROPEAN_POLECAT;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_FALLOW_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_GARGANEY;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_GOOSANDER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_GREY_SEAL;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_LONG_TAILED_DUCK;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_PARTRIDGE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_PINTAIL;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_POCHARD;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RED_BREASTED_MERGANSER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RED_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RINGED_SEAL;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_ROE_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_SHOVELER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_SIKA_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_TUFTED_DUCK;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WIGEON;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_BOAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_FOREST_REINDEER;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_MALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.YOUNG_FEMALE;
import static fi.riista.util.DateUtil.toLocalDateNullSafe;
import static fi.riista.util.DateUtil.today;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class HarvestRegistryHarvest2020MapperTest implements DefaultEntitySupplierProvider {

    private EntitySupplier model;
    private RiistakeskuksenAlue rka;
    private Riistanhoitoyhdistys rhy;
    private Municipality municipality;
    private Person author;
    private Person shooter;

    private Harvest harvest = null;
    private HarvestSpecimen specimen = null;
    private HarvestRegistryItem item = null;
    private Address address;

    @Before
    public void setup() {
        model = getEntitySupplier();
        rka = model.newRiistakeskuksenAlue("050");
        rhy = model.newRiistanhoitoyhdistys(rka, "051");
        author = model.newPerson();
        shooter = model.newPerson();
        address = model.newAddress();
        shooter.setOtherAddress(address);
        municipality = new Municipality("123", "fi", "sv");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooOldHarvest() {
        createHarvest(OFFICIAL_CODE_BEAR);
        harvest.setPointOfTime(new LocalDate(2020, 7, 31).toDateTime(
                new LocalTime(12, 0, 0), Constants.DEFAULT_TIMEZONE));
        transform();
        fail("Should throw an exception");
    }

    @Test
    public void testDoesNotTransformOtherSpecies() {
        createHarvest(OFFICIAL_CODE_CANADIAN_BEAVER);
        final List<HarvestRegistryItem> itemList = HarvestRegistryHarvest2020Mapper.transform(
                harvest,
                shooter,
                ImmutableSet.of(specimen),
                rka.getOfficialCode(),
                rhy.getOfficialCode(),
                ImmutableMap.of(municipality.getOfficialCode(), municipality.getNameLocalisation()))
                .collect(toList());
        assertThat(itemList, hasSize(0));
    }

    @Test
    public void testHarvestsWithCommonAndMunicipalityFields() {
        verifyCommonAndMunicipalityAttributes(OFFICIAL_CODE_BEAN_GOOSE);
        verifyCommonAndMunicipalityAttributes(OFFICIAL_CODE_WIGEON);
        verifyCommonAndMunicipalityAttributes(OFFICIAL_CODE_PINTAIL);
        verifyCommonAndMunicipalityAttributes(OFFICIAL_CODE_GARGANEY);
        verifyCommonAndMunicipalityAttributes(OFFICIAL_CODE_SHOVELER);
        verifyCommonAndMunicipalityAttributes(OFFICIAL_CODE_POCHARD);
        verifyCommonAndMunicipalityAttributes(OFFICIAL_CODE_TUFTED_DUCK);
        verifyCommonAndMunicipalityAttributes(OFFICIAL_CODE_COMMON_EIDER);
        verifyCommonAndMunicipalityAttributes(OFFICIAL_CODE_LONG_TAILED_DUCK);
        verifyCommonAndMunicipalityAttributes(OFFICIAL_CODE_RED_BREASTED_MERGANSER);
        verifyCommonAndMunicipalityAttributes(OFFICIAL_CODE_GOOSANDER);
        verifyCommonAndMunicipalityAttributes(OFFICIAL_CODE_COOT);
        verifyCommonAndMunicipalityAttributes(OFFICIAL_CODE_EUROPEAN_POLECAT);
    }

    @Test
    public void testHarvestsWithCommonPlusGenderAndGeolocationFields() {
        verifyCommonPlusTimeGenderAndGeolocationAttributes(OFFICIAL_CODE_PARTRIDGE);
        verifyCommonPlusTimeGenderAndGeolocationAttributes(OFFICIAL_CODE_EUROPEAN_BEAVER);
        verifyCommonPlusTimeGenderAndGeolocationAttributes(OFFICIAL_CODE_BEAR);
        verifyCommonPlusTimeGenderAndGeolocationAttributes(OFFICIAL_CODE_RINGED_SEAL);
    }

    @Test
    public void testHarvestsWithCommonPlusMunicipalityGenderAndAgeFields() {
        verifyCommonPlusMunicipalityGenderAndAgeAttributes(OFFICIAL_CODE_ROE_DEER);
    }

    private void verifyCommonPlusMunicipalityGenderAndAgeAttributes(final int officialCode) {
        createHarvestAndTransform(officialCode);
        assertCommonAttributes(harvest, item);
        assertPointOfTimeDate(harvest, item);
        assertNull(item.getGeoLocation());
        assertMunicipality(harvest, item);
        assertEquals(specimen.getGender(), item.getGender());
        assertEquals(specimen.getAge(), item.getAge());
    }

    @Test
    public void testHarvestsWithCommonPlusTimeGeolocationGenderAndAgeFields() {
        verifyCommonPlusTimeGeolocationGenderAndAgeAttributes(OFFICIAL_CODE_FALLOW_DEER);
        verifyCommonPlusTimeGeolocationGenderAndAgeAttributes(OFFICIAL_CODE_RED_DEER);
        verifyCommonPlusTimeGeolocationGenderAndAgeAttributes(OFFICIAL_CODE_SIKA_DEER);
        verifyCommonPlusTimeGeolocationGenderAndAgeAttributes(OFFICIAL_CODE_MOOSE);
        verifyCommonPlusTimeGeolocationGenderAndAgeAttributes(OFFICIAL_CODE_WHITE_TAILED_DEER);
        verifyCommonPlusTimeGeolocationGenderAndAgeAttributes(OFFICIAL_CODE_WILD_FOREST_REINDEER);
    }

    @Test
    public void testHarvestsWithCommonPlusMunicipalityGeolocationGenderAndAgeFields() {
        verifyCommonPlusMunicipalityGeolocationGenderAndAgeAttributes(OFFICIAL_CODE_WILD_BOAR);
    }

    @Test
    public void testGreySeal() {
        createHarvest(OFFICIAL_CODE_GREY_SEAL);
        specimen.setWeight(42.2);
        final HarvestArea harvestArea = model.newHarvestArea();
        final HarvestSeason harvestSeason = model.newHarvestSeason(harvest.getSpecies(), today().minusMonths(1),
                today().plusMonths(1),
                today().plusMonths(2));
        final HarvestQuota harvestQuota = model.newHarvestQuota(harvestSeason, harvestArea, 100);
        harvest.setHarvestQuota(harvestQuota);
        transform();

        assertCommonAttributes(harvest, item);
        assertPointOfTimeDateAndTime(harvest, item);
        assertNotNull(item.getShooterAddress());
        assertEquals(specimen.getWeight(), item.getWeight(), 0.01);
        assertEquals(harvestArea.getNameFinnish(), item.getHarvestAreaFinnish());
        assertEquals(harvestArea.getNameSwedish(), item.getHarvestAreaSwedish());
        assertEquals(rhy.getNameFinnish(), item.getRhyFinnish());
        assertEquals(rhy.getNameSwedish(), item.getRhySwedish());
        assertEquals(rka.getNameFinnish(), item.getRkaFinnish());
        assertEquals(rka.getNameSwedish(), item.getRkaSwedish());
    }

    @Test
    public void testExcessAmountsAreMapped() {
        testExcessAmounts(OFFICIAL_CODE_BEAN_GOOSE, false);
        testExcessAmounts(OFFICIAL_CODE_PARTRIDGE, true);
    }

    private void testExcessAmounts(final int speciesCode, final boolean shouldHaveGenderInfo) {

        createHarvest(speciesCode);

        harvest.setAmount(5);
        final HarvestSpecimen specimen1 = model.newHarvestSpecimen(harvest, ADULT_MALE);
        final HarvestSpecimen specimen2 = model.newHarvestSpecimen(harvest, YOUNG_FEMALE);
        final List<HarvestRegistryItem> items = HarvestRegistryHarvest2020Mapper.transform(
                harvest,
                shooter,
                ImmutableSet.of(specimen1, specimen2),
                rka.getOfficialCode(),
                rhy.getOfficialCode(),
                ImmutableMap.of(municipality.getOfficialCode(), municipality.getNameLocalisation())).collect(toList());


        assertThat(items, hasSize(3));

        if (shouldHaveGenderInfo) {
            final HarvestRegistryItem specimen1Item =
                    items.stream().filter(item -> item.getGender() == MALE).findFirst().get();
            final HarvestRegistryItem specimen2Item =
                    items.stream().filter(item -> item.getGender() == FEMALE).findFirst().get();
            assertEquals(1, specimen1Item.getAmount().intValue());
            assertEquals(1, specimen2Item.getAmount().intValue());

            final HarvestRegistryItem genericItem =
                    items.stream().filter(item -> item.getAmount() > 1).findFirst().get();
            assertEquals(3, genericItem.getAmount().intValue());

        }

    }

    private void createHarvestAndTransform(final int officialCode) {
        createHarvest(officialCode);
        transform();
    }

    private void transform() {
        final List<HarvestRegistryItem> itemList = HarvestRegistryHarvest2020Mapper.transform(
                harvest,
                shooter,
                ImmutableSet.of(specimen),
                rka.getOfficialCode(),
                rhy.getOfficialCode(),
                ImmutableMap.of(municipality.getOfficialCode(), municipality.getNameLocalisation()))
                .collect(toList());
        assertThat(itemList, hasSize(1));
        item = itemList.get(0);
    }

    private void verifyCommonAndMunicipalityAttributes(final int officialCode) {
        createHarvestAndTransform(officialCode);
        assertCommonAttributes(harvest, item);
        assertPointOfTimeDate(harvest, item);
        assertMunicipality(harvest, item);
        assertNull(officialCode + " must not have age set", item.getAge());
        assertNull(officialCode + " must not have gender set", item.getGender());
    }

    private void verifyCommonPlusTimeGenderAndGeolocationAttributes(final int officialCode) {
        createHarvestAndTransform(officialCode);

        assertCommonAttributes(harvest, item);
        assertPointOfTimeDateAndTime(harvest, item);
        assertGeolocation(harvest, item);
        assertMunicipalityNull(item);
        assertEquals(officialCode + " must have gender matching", specimen.getGender(), item.getGender());
        assertNull(officialCode + " must not have age present", item.getAge());
    }

    private void verifyCommonPlusTimeGeolocationGenderAndAgeAttributes(final int officialCode) {
        createHarvestAndTransform(officialCode);

        assertCommonAttributes(harvest, item);
        assertPointOfTimeDateAndTime(harvest, item);
        assertGeolocation(harvest, item);
        assertMunicipalityNull(item);
        assertEquals("Gender must be set for " + officialCode, specimen.getGender(), item.getGender());
        assertEquals("Age must be set for " + officialCode, specimen.getAge(), item.getAge());
    }

    private void verifyCommonPlusMunicipalityGeolocationGenderAndAgeAttributes(final int officialCode) {
        createHarvestAndTransform(officialCode);

        assertCommonAttributes(harvest, item);
        assertPointOfTimeDate(harvest, item);
        assertGeolocation(harvest, item);
        assertMunicipality(harvest, item);
        assertEquals("Gender must be set for " + officialCode, specimen.getGender(), item.getGender());
        assertEquals("Age must be set for " + officialCode, specimen.getAge(), item.getAge());
    }

    private static void assertMunicipalityNull(final HarvestRegistryItem item) {
        assertNull(item.getSpecies().getOfficialCode() + " must not have municipality present",
                item.getMunicipalityFinnish());
        assertNull(item.getSpecies().getOfficialCode() + " must not have municipality present",
                item.getMunicipalitySwedish());
    }

    private static void assertCommonAttributes(final Harvest harvest, final HarvestRegistryItem item) {
        assertEquals(harvest.getSpecies().getOfficialCode(), item.getSpecies().getOfficialCode());
        assertEquals(harvest.getActualShooter().getFullName(), item.getShooterName());
        assertEquals(harvest.getActualShooter().getHunterNumber(), item.getShooterHunterNumber());
        assertEquals(harvest.getAmount(), item.getAmount().intValue());
    }

    private static void assertPointOfTimeDate(final Harvest harvest, final HarvestRegistryItem item) {
        assertEquals(toLocalDateNullSafe(harvest.getPointOfTime()), toLocalDateNullSafe(item.getPointOfTime()));
        final DateTime expectedPointOfTime = harvest.getPointOfTime().withTimeAtStartOfDay();

        assertEquals(expectedPointOfTime, item.getPointOfTime());
    }

    private static void assertPointOfTimeDateAndTime(final Harvest harvest, final HarvestRegistryItem item) {
        assertEquals(harvest.getPointOfTime(), item.getPointOfTime());
    }

    private static void assertGeolocation(final Harvest harvest, final HarvestRegistryItem item) {
        assertEquals(harvest.getGeoLocation().getLatitude(), item.getGeoLocation().getLatitude());
        assertEquals(harvest.getGeoLocation().getLongitude(), item.getGeoLocation().getLongitude());
    }

    private void assertMunicipality(final Harvest harvest, final HarvestRegistryItem item) {
        assertEquals(item.getSpecies().getOfficialCode() + " must have municipality present",
                municipality.getNameLocalisation().getFinnish(), item.getMunicipalityFinnish());
        assertEquals(item.getSpecies().getOfficialCode() + " must have municipality present",
                municipality.getNameLocalisation().getSwedish(), item.getMunicipalitySwedish());
    }

    private void createHarvest(final int officialCode) {
        harvest = model.newHarvest(model.newGameSpecies(officialCode), author, shooter);
        harvest.setPointOfTime(new LocalDate(2020, 8, 20)
                .toDateTime(new LocalTime(12, 0), Constants.DEFAULT_TIMEZONE));
        harvest.setRhy(rhy);
        harvest.setMunicipalityCode(municipality.getOfficialCode());
        specimen = model.newHarvestSpecimen(harvest, ADULT_MALE);
    }
}

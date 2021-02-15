package fi.riista.feature.harvestregistry.quartz;

import com.google.common.collect.ImmutableSet;
import fi.riista.config.Constants;
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
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_EUROPEAN_BEAVER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_EUROPEAN_POLECAT;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_FALLOW_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_GREY_SEAL;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_PARTRIDGE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RED_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RINGED_SEAL;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_ROE_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_SIKA_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_BOAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_FOREST_REINDEER;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_MALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.YOUNG_FEMALE;
import static fi.riista.util.DateUtil.today;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class HarvestRegistryHarvest2019MapperTest implements DefaultEntitySupplierProvider {

    private EntitySupplier model;
    private RiistakeskuksenAlue rka;
    private Riistanhoitoyhdistys rhy;
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
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooNewHarvest(){
        createHarvest(OFFICIAL_CODE_BEAR);
        harvest.setPointOfTime(new LocalDate(2020, 8, 1).toDateTime(
                new LocalTime(12, 0, 0), Constants.DEFAULT_TIMEZONE));
        transform();
        fail("Should throw an exception");
    }


    @Test
    public void testDoesNotTransformOtherSpecies() {
        createHarvest(OFFICIAL_CODE_CANADIAN_BEAVER);
        final List<HarvestRegistryItem> itemList = HarvestRegistryHarvest2019Mapper.transform(
                harvest,
                shooter,
                ImmutableSet.of(specimen),
                rka.getOfficialCode(),
                rhy.getOfficialCode())
                .collect(toList());
        assertThat( itemList, hasSize(0));
    }

    @Test
    public void testHarvestsWithCommonFields() {
        verifyCommonAttributes(OFFICIAL_CODE_BEAN_GOOSE);
        verifyCommonAttributes(OFFICIAL_CODE_EUROPEAN_POLECAT);
    }

    @Test
    public void testHarvestsWithCommonPlusGenderFields() {
        verifyCommonPlusGenderAttributes(OFFICIAL_CODE_PARTRIDGE);
        verifyCommonPlusGenderAttributes(OFFICIAL_CODE_EUROPEAN_BEAVER);
        verifyCommonPlusGenderAttributes(OFFICIAL_CODE_BEAR);
        verifyCommonPlusGenderAttributes(OFFICIAL_CODE_RINGED_SEAL);
    }

    @Test
    public void testHarvestsWithCommonPlusGenderAndAgeFields() {
        verifyCommonPlusGenderAndAgeAttributes(OFFICIAL_CODE_WILD_BOAR);
        verifyCommonPlusGenderAndAgeAttributes(OFFICIAL_CODE_FALLOW_DEER);
        verifyCommonPlusGenderAndAgeAttributes(OFFICIAL_CODE_RED_DEER);
        verifyCommonPlusGenderAndAgeAttributes(OFFICIAL_CODE_SIKA_DEER);
        verifyCommonPlusGenderAndAgeAttributes(OFFICIAL_CODE_ROE_DEER);
        verifyCommonPlusGenderAndAgeAttributes(OFFICIAL_CODE_MOOSE);
        verifyCommonPlusGenderAndAgeAttributes(OFFICIAL_CODE_WHITE_TAILED_DEER);
        verifyCommonPlusGenderAndAgeAttributes(OFFICIAL_CODE_WILD_FOREST_REINDEER);

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
        final List<HarvestRegistryItem> items = HarvestRegistryHarvest2019Mapper.transform(
                harvest,
                shooter,
                ImmutableSet.of(specimen1, specimen2),
                rka.getOfficialCode(),
                rhy.getOfficialCode()).collect(toList());


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
        final List<HarvestRegistryItem> itemList = HarvestRegistryHarvest2019Mapper.transform(
                harvest,
                shooter,
                ImmutableSet.of(specimen),
                rka.getOfficialCode(),
                rhy.getOfficialCode()).collect(toList());
        assertThat(itemList, hasSize(1));
        item = itemList.get(0);
    }

    private void verifyCommonAttributes(final int officialCode) {
        createHarvestAndTransform(officialCode);
        assertCommonAttributes(harvest, item);
        assertNull(item.getAge());
        assertNull(item.getGender());
    }

    private void verifyCommonPlusGenderAttributes(final int officialCode) {
        createHarvestAndTransform(officialCode);

        assertCommonAttributes(harvest, item);
        assertEquals(officialCode + " must have gender matching", specimen.getGender(), item.getGender());
        assertNull(officialCode + " must not have age present", item.getAge());
    }

    private void verifyCommonPlusGenderAndAgeAttributes(final int officialCode) {
        createHarvestAndTransform(officialCode);

        assertCommonAttributes(harvest, item);
        assertEquals(specimen.getGender(), item.getGender());
        assertEquals(specimen.getAge(), item.getAge());
    }

    private static void assertCommonAttributes(final Harvest harvest, final HarvestRegistryItem item) {
        assertEquals(harvest.getSpecies().getOfficialCode(), item.getSpecies().getOfficialCode());
        assertEquals(harvest.getActualShooter().getFullName(), item.getShooterName());
        assertEquals(harvest.getActualShooter().getHunterNumber(), item.getShooterHunterNumber());
        assertEquals(harvest.getPointOfTime(), item.getPointOfTime());
        assertEquals(harvest.getAmount(), item.getAmount().intValue());
        assertEquals(harvest.getGeoLocation().getLatitude(), item.getGeoLocation().getLatitude());
        assertEquals(harvest.getGeoLocation().getLongitude(), item.getGeoLocation().getLongitude());
    }


    private void createHarvest(final int officialCode) {
        harvest = model.newHarvest(model.newGameSpecies(officialCode), author, shooter);
        harvest.setPointOfTime(new LocalDate(2019, 8, 20).toDateTimeAtStartOfDay(Constants.DEFAULT_TIMEZONE));
        harvest.setRhy(rhy);
        specimen = model.newHarvestSpecimen(harvest, ADULT_MALE);
    }
}

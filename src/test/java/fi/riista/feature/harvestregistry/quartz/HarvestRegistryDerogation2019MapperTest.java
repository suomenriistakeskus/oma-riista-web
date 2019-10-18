package fi.riista.feature.harvestregistry.quartz;

import com.google.common.collect.ImmutableSet;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestregistry.HarvestRegistryItem;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.PermitNumberUtil;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.test.DefaultEntitySupplierProvider;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_AMERICAN_MINK;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BADGER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BLUE_FOX;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BROWN_HARE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_CANADIAN_BEAVER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_ERMINE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_EUROPEAN_BEAVER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_EUROPEAN_POLECAT;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_FALLOW_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_GREY_SEAL;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_HARBOUR_SEAL;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_LYNX;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOUNTAIN_HARE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MUFFLON;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MUSKRAT;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_NUTRIA;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_OTTER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_PINE_MARTEN;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RABBIT;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RACCOON;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RACCOON_DOG;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RED_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RED_FOX;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RED_SQUIRREL;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RINGED_SEAL;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_ROE_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_SIKA_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_BOAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_FOREST_REINDEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLF;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLVERINE;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HarvestRegistryDerogation2019MapperTest implements DefaultEntitySupplierProvider {

    private EntitySupplier model;
    private RiistakeskuksenAlue rka;
    private Riistanhoitoyhdistys rhy;
    private Person author;
    private Person shooter;

    private Harvest harvest = null;
    private HarvestSpecimen specimen = null;
    private HarvestRegistryItem item = null;
    private Address address;
    private HarvestPermit derogation;

    private static final Set<Integer> SPECIES_WITH_GENDER_FIELD = ImmutableSet.of(
            OFFICIAL_CODE_RABBIT,
            OFFICIAL_CODE_MOUNTAIN_HARE,
            OFFICIAL_CODE_BROWN_HARE,
            OFFICIAL_CODE_RED_SQUIRREL,
            OFFICIAL_CODE_EUROPEAN_BEAVER,
            OFFICIAL_CODE_CANADIAN_BEAVER,
            OFFICIAL_CODE_MUSKRAT,
            OFFICIAL_CODE_NUTRIA,
            OFFICIAL_CODE_WOLF,
            OFFICIAL_CODE_BLUE_FOX,
            OFFICIAL_CODE_RED_FOX,
            OFFICIAL_CODE_RACCOON_DOG,
            OFFICIAL_CODE_BEAR,
            OFFICIAL_CODE_RACCOON,
            OFFICIAL_CODE_BADGER,
            OFFICIAL_CODE_ERMINE,
            OFFICIAL_CODE_EUROPEAN_POLECAT,
            OFFICIAL_CODE_OTTER,
            OFFICIAL_CODE_PINE_MARTEN,
            OFFICIAL_CODE_AMERICAN_MINK,
            OFFICIAL_CODE_WOLVERINE,
            OFFICIAL_CODE_LYNX,
            OFFICIAL_CODE_RINGED_SEAL,
            OFFICIAL_CODE_HARBOUR_SEAL,
            OFFICIAL_CODE_GREY_SEAL,
            OFFICIAL_CODE_WILD_BOAR,
            OFFICIAL_CODE_FALLOW_DEER,
            OFFICIAL_CODE_RED_DEER,
            OFFICIAL_CODE_SIKA_DEER,
            OFFICIAL_CODE_ROE_DEER,
            OFFICIAL_CODE_MOOSE,
            OFFICIAL_CODE_WHITE_TAILED_DEER,
            OFFICIAL_CODE_WILD_FOREST_REINDEER,
            OFFICIAL_CODE_MUFFLON
    );

    @Before
    public void setup() {
        model = getEntitySupplier();
        rka = model.newRiistakeskuksenAlue("050");
        rhy = model.newRiistanhoitoyhdistys(rka, "051");
        author = model.newPerson();
        shooter = model.newPerson();
        address = model.newAddress();
        shooter.setOtherAddress(address);

        derogation = model.newHarvestPermit(PermitNumberUtil.createPermitNumber(2019, 3, 1984));
        derogation.setPermitTypeCode(PermitTypeCode.BEAR_KANNAHOIDOLLINEN);
    }

    @Test
    public void testSmoke() {
        createHarvestAndTransform(OFFICIAL_CODE_BEAR);
        assertCommonAttributes();
        assertNull(item.getAge());
        assertEquals(specimen.getGender(), item.getGender());
    }

    @Test
    public void testSpeciesWithGenderInfo() {
        SPECIES_WITH_GENDER_FIELD.forEach(this::verifyCommonAndGenderAttributes);
    }

    @Test
    public void testSpeciesWithNoGenderInfo() {
        Arrays.stream(GameSpecies.ALL_GAME_SPECIES_CODES)
                .filter(code -> !SPECIES_WITH_GENDER_FIELD.contains(code))
                .forEach(this::verifyCommonAttributes);
    }

    @Test
    public void testExcessAmountsAreMapped() {
        SPECIES_WITH_GENDER_FIELD.forEach(code -> {
                    createHarvest(code);
                    harvest.setAmount(5);
                    final HarvestSpecimen specimen1 = model.newHarvestSpecimen(harvest, GameAge.ADULT, GameGender.MALE);
                    final HarvestSpecimen specimen2 = model.newHarvestSpecimen(harvest, GameAge.YOUNG,
                            GameGender.FEMALE);
                    final List<HarvestRegistryItem> items = HarvestRegistryDerogation2019Mapper.transform(
                            harvest,
                            shooter,
                            ImmutableSet.of(specimen1, specimen2),
                            rka.getOfficialCode(),
                            rhy.getOfficialCode()).collect(toList());


                    assertThat(items, hasSize(3));

                    final HarvestRegistryItem specimen1Item =
                            items.stream().filter(item -> item.getGender() == GameGender.MALE).findFirst().get();
                    assertEquals(1, specimen1Item.getAmount().intValue());
                    final HarvestRegistryItem specimen2Item =
                            items.stream().filter(item -> item.getGender() == GameGender.FEMALE).findFirst().get();
                    assertEquals(1, specimen2Item.getAmount().intValue());

                    final HarvestRegistryItem genericItem =
                            items.stream().filter(item -> item.getAmount() > 1).findFirst().get();
                    assertEquals(3, genericItem.getAmount().intValue());
                }

        );

    }

    private void createHarvestAndTransform(final int officialCode) {
        createHarvest(officialCode);
        transform();
    }

    private void transform() {
        final List<HarvestRegistryItem> itemList = HarvestRegistryDerogation2019Mapper.transform(
                harvest,
                shooter,
                ImmutableSet.of(specimen),
                rka.getOfficialCode(),
                rhy.getOfficialCode()).collect(toList());
        assertThat(itemList, hasSize(1));
        item = itemList.get(0);
    }

    private void verifyCommonAndGenderAttributes(final int officialCode) {
        createHarvestAndTransform(officialCode);
        assertCommonAttributes();
        assertNull(item.getAge());
        assertEquals(specimen.getGender(), item.getGender());
    }

    private void verifyCommonAttributes(final int officialCode) {
        createHarvestAndTransform(officialCode);
        assertCommonAttributes();
        assertNull(item.getAge());
        assertNull(item.getGender());
    }

    private void assertCommonAttributes() {
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
        harvest.setPointOfTime(new LocalDate(2019, 8, 20).toDate());
        specimen = model.newHarvestSpecimen(harvest, GameAge.ADULT, GameGender.MALE);
    }
}

package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersionNotSupportedException;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.harvestpermit.HarvestPermit;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MobileHarvestFeatureV1Test extends MobileHarvestFeatureTest {

    @Override
    protected int getApiVersion() {
        return 1;
    }

    @Override
    public List<HarvestSpecVersion> getTestExecutionVersions() {
        return Arrays.asList(HarvestSpecVersion._1, HarvestSpecVersion._2);
    }

    @Test
    public void testCreateHarvest_nullAmountShouldPass() {
        withPerson(person -> {
            final GameSpecies species = model().newGameSpecies();

            onSavedAndAuthenticated(createUser(person), () -> {

                final MobileHarvestDTO inputDto = create(HarvestSpecVersion._1, species)
                        .withAmount(null)
                        .withSpecimens(null)
                        .build();

                final MobileHarvestDTO outputDto = invokeCreateHarvest(inputDto);

                doCreateAssertions(outputDto.getId(), inputDto, person);
            });
        });
    }

    @Test(expected = HarvestSpecVersionNotSupportedException.class)
    public void testCreateHarvest_withPermit_whenClientDoesNotSupportPermit() {
        final GameSpecies species = model().newGameSpecies();
        final HarvestPermit permit = model().newHarvestPermit();
        model().newHarvestPermitSpeciesAmount(permit, species);

        onSavedAndAuthenticated(createUserWithPerson(), user -> {
            final MobileHarvestDTO inputDto = create(HarvestSpecVersion._1, species)
                    .withPermitNumber(permit.getPermitNumber())
                    .withPermitType(permit.getPermitType())
                    .build();

            invokeCreateHarvest(inputDto);
        });
    }

    @Test
    public void testUpdateHarvest_nullGeoLocationSourceShouldPassWithOldestVersion() {
        withPerson(person -> {

            final Harvest harvest = model().newMobileHarvest(model().newGameSpecies(true), person);

            onSavedAndAuthenticated(createUser(person), () -> {

                final GeoLocation originalLocation = harvest.getGeoLocation();

                final MobileHarvestDTO inputDto = create(HarvestSpecVersion._1, harvest)
                        .withGeoLocation(originalLocation.move(1, 1).withSource(null))
                        .build();

                final MobileHarvestDTO outputDto = invokeUpdateHarvest(inputDto);

                // Geolocation should be left unchanged because source is missing.
                inputDto.setGeoLocation(originalLocation);
                doCreateAssertions(outputDto.getId(), inputDto, person);
            });
        });
    }

    @Test
    public void testUpdateHarvest_nullAmountShouldPass() {
        final HarvestSpecVersion specVersion = HarvestSpecVersion._1;

        withPerson(person -> {

            final Harvest harvest = model().newMobileHarvest(model().newGameSpecies(true), person);
            final List<HarvestSpecimen> specimensToBePreserved = createSpecimens(harvest, 5);
            final GameSpecies updatedSpecies = model().newGameSpecies(true);

            onSavedAndAuthenticated(createUser(person), () -> {

                final MobileHarvestDTO dto = create(specVersion, harvest, updatedSpecies)
                        .withAmount(null)
                        .withSpecimens(null)
                        .build();

                invokeUpdateHarvest(dto);

                dto.setSpecimensMappedFrom(specimensToBePreserved);

                doUpdateAssertions(dto, person, 1);
            });
        });
    }

    @Test
    public void testUpdateHarvest_whenAmountNotModifiedAndSpecimensIsNull() {
        forEachVersion(specVersion -> withPerson(person -> {

            final Harvest harvest = model().newMobileHarvest(model().newGameSpecies(true), person);

            final List<HarvestSpecimen> specimensToBePreserved = createSpecimens(harvest, 5);
            harvest.setAmount(specimensToBePreserved.size());

            final GameSpecies newSpecies = model().newGameSpecies(true);

            onSavedAndAuthenticated(createUser(person), () -> {

                final MobileHarvestDTO dto = create(specVersion, harvest, newSpecies)
                        .mutate()
                        .withAmount(harvest.getAmount())
                        .withSpecimens(null)
                        .build();

                invokeUpdateHarvest(dto);

                dto.setSpecimensMappedFrom(specimensToBePreserved);

                doUpdateAssertions(dto, person, 1);
            });
        }));
    }

    @Test
    public void testUpdateHarvest_withTruncatingSpecimens() {
        forEachVersion(specVersion -> withPerson(person -> {

            final Harvest harvest = model().newMobileHarvest(model().newGameSpecies(true), person);

            // These HarvestSpecimens should be preserved over update operation.
            final List<HarvestSpecimen> specimensToBePreserved = createSpecimens(harvest, 5);

            // These HarvestSpecimens will not exist after update operation.
            model().newHarvestSpecimen(harvest);
            model().newHarvestSpecimen(harvest);

            onSavedAndAuthenticated(createUser(person), () -> {

                final MobileHarvestDTO dto = create(HarvestSpecVersion._1, harvest)
                        .withAmount(specimensToBePreserved.size())
                        .withSpecimens(null)
                        .build();

                invokeUpdateHarvest(dto);

                dto.setSpecimensMappedFrom(specimensToBePreserved);

                doUpdateAssertions(dto, person, 1);
            });
        }));
    }

    @Test
    public void testUpdateHarvest_harvestNotUpdatedWhenClientDoesNotSupportPermit() {
        withPerson(person -> {

            final GameSpecies species = model().newGameSpecies(true);

            final HarvestPermit permit = model().newHarvestPermit();
            model().newHarvestPermitSpeciesAmount(permit, species);

            final Harvest harvest = model().newMobileHarvest(species, person);
            harvest.setHarvestPermit(permit);
            harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.PROPOSED);
            harvest.setRhy(permit.getRhy());

            onSavedAndAuthenticated(createUser(person), () -> {
                final MobileHarvestDTO inputDto = create(HarvestSpecVersion._1, harvest)
                        .mutate()
                        .withPermitNumber(null)
                        .build();

                final MobileHarvestDTO expected = create(HarvestSpecVersion._1, harvest)
                        .withDescription(inputDto.getDescription())
                        .build();

                invokeUpdateHarvest(inputDto);

                doUpdateAssertions(expected, person, 1, h -> {
                    final HarvestPermit permit2 = h.getHarvestPermit();
                    assertNotNull(permit2);
                    assertEquals(permit.getPermitNumber(), permit2.getPermitNumber());
                    assertEquals(permit.getPermitType(), permit2.getPermitType());
                });
            });
        });
    }
}

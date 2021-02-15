package fi.riista.feature.harvestpermit.nestremoval;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAN_GOOSE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_EUROPEAN_BEAVER;
import static org.junit.Assert.assertEquals;

public class HarvestPermitNestRemovalDetailsFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestPermitNestRemovalDetailsFeature feature;

    @Resource
    private HarvestPermitNestRemovalUsageRepository harvestPermitNestRemovalUsageRepository;

    private GeoLocation nestGeoLocation;
    private GameSpecies species;
    private SystemUser user;
    private HarvestPermit permit;
    private HarvestPermitSpeciesAmount speciesAmount;

    @Before
    public void setup() {
        nestGeoLocation = geoLocation();
        species = model().newGameSpecies(OFFICIAL_CODE_BEAN_GOOSE);
        user = createUserWithPerson();
        permit = model().newHarvestPermit(user.getPerson());
        speciesAmount = model().newHarvestPermitSpeciesAmount(permit, species, 10, 20, 30);

        persistInNewTransaction();
    }

    @Test
    public void testGetUsage() {
        final HarvestPermitNestRemovalUsage usage =
                model().newHarvestPermitNestRemovalUsage(speciesAmount, 1, 2, 3, nestGeoLocation, HarvestPermitNestLocationType.NEST);

        onSavedAndAuthenticated(user, () -> {
            final List<HarvestPermitNestRemovalUsageDTO> usages = feature.getPermitUsage(permit.getId());
            assertEquals(1, usages.size());

            final HarvestPermitNestRemovalUsageDTO dto = usages.get(0);
            assertEquals(usage.getNestAmount(), dto.getUsedNestAmount());
            assertEquals(speciesAmount.getNestAmount(), dto.getPermitNestAmount());
            assertEquals(usage.getEggAmount(), dto.getUsedEggAmount());
            assertEquals(speciesAmount.getEggAmount(), dto.getPermitEggAmount());
            assertEquals(usage.getConstructionAmount(), dto.getUsedConstructionAmount());
            assertEquals(speciesAmount.getConstructionAmount(), dto.getPermitConstructionAmount());

            assertEquals(1, dto.getNestLocations().size());
            final HarvestPermitNestLocationDTO nestLocationDTO = dto.getNestLocations().get(0);
            assertEquals(nestGeoLocation, nestLocationDTO.getGeoLocation());
            assertEquals(HarvestPermitNestLocationType.NEST, nestLocationDTO.getNestLocationType());
        });
    }

    @Test
    public void testSaveNewUsage() {
        final HarvestPermitNestRemovalUsageDTO dto = new HarvestPermitNestRemovalUsageDTO();
        dto.setSpeciesCode(species.getOfficialCode());
        dto.setUsedNestAmount(5);
        dto.setUsedEggAmount(15);
        dto.setUsedConstructionAmount(25);

        final HarvestPermitNestLocationDTO nestLocationDTO =
                new HarvestPermitNestLocationDTO(nestGeoLocation, HarvestPermitNestLocationType.NEST);
        dto.setNestLocations(Arrays.asList(nestLocationDTO));

        onSavedAndAuthenticated(user, () -> {
            feature.savePermitUsage(permit.getId(), Arrays.asList(dto));

            runInTransaction(() -> {
                final List<HarvestPermitNestRemovalUsage> usages =
                        harvestPermitNestRemovalUsageRepository.findByHarvestPermitSpeciesAmount(speciesAmount);
                assertEquals(1, usages.size());

                final HarvestPermitNestRemovalUsage usage = usages.get(0);
                assertEquals(species, usage.getHarvestPermitSpeciesAmount().getGameSpecies());
                assertEquals(dto.getUsedNestAmount(), usage.getNestAmount());
                assertEquals(dto.getUsedEggAmount(), usage.getEggAmount());
                assertEquals(dto.getUsedConstructionAmount(), usage.getConstructionAmount());
                assertEquals(1, usage.getHarvestPermitNestLocations().size());

                final HarvestPermitNestLocation location = usage.getHarvestPermitNestLocations().iterator().next();
                assertEquals(nestGeoLocation, location.getGeoLocation());
                assertEquals(HarvestPermitNestLocationType.NEST, location.getHarvestPermitNestLocationType());
            });
        });
    }

    @Test
    public void testSaveNewUsage_partialUsage() {
        final HarvestPermitNestRemovalUsageDTO dto = new HarvestPermitNestRemovalUsageDTO();
        dto.setSpeciesCode(species.getOfficialCode());
        dto.setUsedNestAmount(5);
        dto.setUsedConstructionAmount(25);

        final HarvestPermitNestLocationDTO nestLocationDTO =
                new HarvestPermitNestLocationDTO(nestGeoLocation, HarvestPermitNestLocationType.NEST);
        dto.setNestLocations(Arrays.asList(nestLocationDTO));

        onSavedAndAuthenticated(user, () -> {
            feature.savePermitUsage(permit.getId(), Arrays.asList(dto));

            runInTransaction(() -> {
                final List<HarvestPermitNestRemovalUsage> usages =
                        harvestPermitNestRemovalUsageRepository.findByHarvestPermitSpeciesAmount(speciesAmount);
                assertEquals(1, usages.size());

                final HarvestPermitNestRemovalUsage usage = usages.get(0);
                assertEquals(species, usage.getHarvestPermitSpeciesAmount().getGameSpecies());
                assertEquals(dto.getUsedNestAmount(), usage.getNestAmount());
                assertEquals(dto.getUsedEggAmount(), usage.getEggAmount());
                assertEquals(dto.getUsedConstructionAmount(), usage.getConstructionAmount());
                assertEquals(1, usage.getHarvestPermitNestLocations().size());

                final HarvestPermitNestLocation location = usage.getHarvestPermitNestLocations().iterator().next();
                assertEquals(nestGeoLocation, location.getGeoLocation());
                assertEquals(HarvestPermitNestLocationType.NEST, location.getHarvestPermitNestLocationType());
            });
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveNewUsage_invalidUsage() {
        final HarvestPermitNestRemovalUsageDTO dto = new HarvestPermitNestRemovalUsageDTO();
        dto.setSpeciesCode(species.getOfficialCode());
        dto.setUsedNestAmount(15);

        final HarvestPermitNestLocationDTO nestLocationDTO =
                new HarvestPermitNestLocationDTO(nestGeoLocation, HarvestPermitNestLocationType.NEST);
        dto.setNestLocations(Arrays.asList(nestLocationDTO));

        onSavedAndAuthenticated(user, () -> {
            feature.savePermitUsage(permit.getId(), Arrays.asList(dto));
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveNewUsage_invalidSpeciesInUsage() {
        final HarvestPermitNestRemovalUsageDTO dto = new HarvestPermitNestRemovalUsageDTO();
        final GameSpecies beaverSpecies = model().newGameSpecies(OFFICIAL_CODE_EUROPEAN_BEAVER);
        dto.setSpeciesCode(beaverSpecies.getOfficialCode());

        onSavedAndAuthenticated(user, () -> {
            feature.savePermitUsage(permit.getId(), Arrays.asList(dto));
        });
    }

    @Test
    public void testUpdateUsage() {
        final HarvestPermitNestRemovalUsage usage =
                model().newHarvestPermitNestRemovalUsage(speciesAmount, 1, 2, 3, nestGeoLocation, HarvestPermitNestLocationType.NEST);

        onSavedAndAuthenticated(user, () -> {
            final HarvestPermitNestRemovalUsageDTO dto = new HarvestPermitNestRemovalUsageDTO();
            dto.setSpeciesCode(species.getOfficialCode());
            dto.setUsedNestAmount(5);
            dto.setUsedEggAmount(13);
            dto.setUsedConstructionAmount(25);

            final GeoLocation newLocation = geoLocation();
            final HarvestPermitNestLocationDTO nestLocationDTO =
                    new HarvestPermitNestLocationDTO(newLocation, HarvestPermitNestLocationType.CONSTRUCTION);
            dto.setNestLocations(Arrays.asList(nestLocationDTO));

            feature.savePermitUsage(permit.getId(), Arrays.asList(dto));
            runInTransaction(() -> {
                final List<HarvestPermitNestRemovalUsage> usages =
                        harvestPermitNestRemovalUsageRepository.findByHarvestPermitSpeciesAmount(speciesAmount);
                assertEquals(1, usages.size());

                final HarvestPermitNestRemovalUsage newUsage = usages.get(0);
                assertEquals(species, newUsage.getHarvestPermitSpeciesAmount().getGameSpecies());
                assertEquals(dto.getUsedNestAmount(), newUsage.getNestAmount());
                assertEquals(dto.getUsedEggAmount(), newUsage.getEggAmount());
                assertEquals(dto.getUsedConstructionAmount(), newUsage.getConstructionAmount());
                assertEquals(1, newUsage.getHarvestPermitNestLocations().size());

                final HarvestPermitNestLocation location = newUsage.getHarvestPermitNestLocations().iterator().next();
                assertEquals(newLocation, location.getGeoLocation());
                assertEquals(HarvestPermitNestLocationType.CONSTRUCTION, location.getHarvestPermitNestLocationType());
            });
        });
    }
}

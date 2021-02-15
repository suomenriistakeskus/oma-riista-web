package fi.riista.feature.harvestpermit.usage;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAN_GOOSE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_EUROPEAN_BEAVER;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class PermitUsageFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private PermitUsageFeature feature;

    @Resource
    private PermitUsageRepository permitUsageRepository;

    @Resource
    private PermitUsageLocationRepository permitUsageLocationRepository;

    private GameSpecies species;
    private SystemUser user;
    private HarvestPermit permit;
    private HarvestPermitSpeciesAmount speciesAmount;

    @Before
    public void setup() {
        species = model().newGameSpecies(OFFICIAL_CODE_BEAN_GOOSE);
        user = createUserWithPerson();
        permit = model().newHarvestPermit(user.getPerson());
        speciesAmount = model().newHarvestPermitSpeciesAmount(permit, species, 5.0f);
        speciesAmount.setEggAmount(10);
    }

    @Test
    public void testGetPermitUsage() {
        final PermitUsage usage = model().newPermitUsage(speciesAmount, 3, 5);
        final List<PermitUsageLocation> locations = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            locations.add(model().newPermitUsageLocation(usage));
        }

        onSavedAndAuthenticated(user, () -> {
            final List<PermitUsageDTO> usageDTOList = feature.getPermitUsage(permit.getId());
            assertThat(usageDTOList, hasSize(1));

            final PermitUsageDTO usageDTO = usageDTOList.get(0);
            assertThat(usageDTO.getPermitSpecimenAmount(), is(equalTo(speciesAmount.getSpecimenAmount().intValue())));
            assertThat(usageDTO.getUsedSpecimenAmount(), is(equalTo(usage.getSpecimenAmount())));
            assertThat(usageDTO.getUsedEggAmount(), is(equalTo(usage.getEggAmount())));

            final List<PermitUsageLocationDTO> locationDTOList = usageDTO.getPermitUsageLocations();
            assertThat(locationDTOList, hasSize(3));
        });
    }

    @Test
    public void testSavePermitUsage() {
        onSavedAndAuthenticated(user, () -> {
            final PermitUsageDTO permitUsageDTO = new PermitUsageDTO();
            permitUsageDTO.setSpeciesCode(OFFICIAL_CODE_BEAN_GOOSE);
            permitUsageDTO.setUsedSpecimenAmount(1);
            permitUsageDTO.setUsedEggAmount(2);

            final PermitUsageLocationDTO permitUsageLocationDTO = new PermitUsageLocationDTO(geoLocation());
            permitUsageDTO.setPermitUsageLocations(singletonList(permitUsageLocationDTO));

            feature.savePermitUsage(permit.getId(), singletonList(permitUsageDTO));

            runInTransaction(() -> {
                final List<PermitUsage> usages =
                        permitUsageRepository.findByHarvestPermitSpeciesAmountIn(singletonList(speciesAmount));
                assertThat(usages, hasSize(1));

                final PermitUsage usage = usages.get(0);
                assertThat(usage.getSpecimenAmount(), is(equalTo(permitUsageDTO.getUsedSpecimenAmount())));
                assertThat(usage.getEggAmount(), is(equalTo(permitUsageDTO.getUsedEggAmount())));

                final List<PermitUsageLocation> locations = permitUsageLocationRepository.findByPermitUsageIn(usages);
                assertThat(locations, hasSize(1));
            });
        });
    }

    @Test
    public void testSavePermitUsage_update() {
        final GameSpecies species2 = model().newGameSpecies(OFFICIAL_CODE_EUROPEAN_BEAVER);
        final HarvestPermitSpeciesAmount speciesAmount2 = model().newHarvestPermitSpeciesAmount(permit, species2, 2.0f);

        final PermitUsage usage = model().newPermitUsage(speciesAmount, 3, 5);
        final List<PermitUsageLocation> locations = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            locations.add(model().newPermitUsageLocation(usage));
        }

        onSavedAndAuthenticated(user, () -> {
            final PermitUsageDTO permitUsageDTO = new PermitUsageDTO();
            permitUsageDTO.setId(usage.getId());
            permitUsageDTO.setSpeciesCode(OFFICIAL_CODE_BEAN_GOOSE);
            permitUsageDTO.setUsedSpecimenAmount(5);
            permitUsageDTO.setUsedEggAmount(7);

            final List<PermitUsageLocationDTO> permitUsageLocationDTOs = locations.stream()
                    .map(location -> PermitUsageLocationDTO.create(location))
                    .collect(toList());
            permitUsageLocationDTOs.add(new PermitUsageLocationDTO(geoLocation()));

            permitUsageDTO.setPermitUsageLocations(permitUsageLocationDTOs);

            final PermitUsageDTO permitUsageDTO2 = new PermitUsageDTO();
            permitUsageDTO2.setSpeciesCode(OFFICIAL_CODE_EUROPEAN_BEAVER);
            permitUsageDTO2.setUsedSpecimenAmount(2);

            final PermitUsageLocationDTO permitUsageLocationDTO2 = new PermitUsageLocationDTO(geoLocation());

            permitUsageDTO2.setPermitUsageLocations(singletonList(permitUsageLocationDTO2));

            final Map<Integer, PermitUsageDTO> speciesCodeToUsage = Stream.of(permitUsageDTO, permitUsageDTO2)
                    .collect(Collectors.toMap(
                            usageDTO -> usageDTO.getSpeciesCode(),
                            usageDTO -> usageDTO));
            feature.savePermitUsage(permit.getId(), speciesCodeToUsage.values().stream().collect(toList()));

            runInTransaction(() -> {
                final List<PermitUsage> updatedUsages =
                        permitUsageRepository.findByHarvestPermitSpeciesAmountIn(Arrays.asList(speciesAmount,
                                speciesAmount2));
                assertThat(updatedUsages, hasSize(2));

                updatedUsages.forEach(updatedUsage -> {
                    final int speciesCode =
                            updatedUsage.getHarvestPermitSpeciesAmount().getGameSpecies().getOfficialCode();
                    final PermitUsageDTO usageDTO = speciesCodeToUsage.get(speciesCode);
                    assertThat(usageDTO, is(notNullValue()));
                    assertThat(updatedUsage.getSpecimenAmount(), is(equalTo(usageDTO.getUsedSpecimenAmount())));
                    assertThat(updatedUsage.getEggAmount(), is(equalTo(usageDTO.getUsedEggAmount())));

                    final List<PermitUsageLocation> updatedLocations =
                            permitUsageLocationRepository.findByPermitUsageIn(singletonList(updatedUsage));
                    assertThat(updatedLocations, hasSize(usageDTO.getPermitUsageLocations().size()));
                });
            });
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSavePermitUsage_invalidSpecies() {
        onSavedAndAuthenticated(user, () -> {
            final PermitUsageDTO permitUsageDTO = new PermitUsageDTO();
            permitUsageDTO.setSpeciesCode(OFFICIAL_CODE_BEAR);
            permitUsageDTO.setUsedSpecimenAmount(1);

            feature.savePermitUsage(permit.getId(), singletonList(permitUsageDTO));
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSavePermitUsage_invalidAmount() {
        onSavedAndAuthenticated(user, () -> {
            final PermitUsageDTO permitUsageDTO = new PermitUsageDTO();
            permitUsageDTO.setSpeciesCode(OFFICIAL_CODE_BEAN_GOOSE);
            permitUsageDTO.setUsedSpecimenAmount(10);

            feature.savePermitUsage(permit.getId(), singletonList(permitUsageDTO));
        });
    }
}

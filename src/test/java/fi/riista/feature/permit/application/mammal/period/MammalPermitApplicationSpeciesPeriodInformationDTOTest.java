package fi.riista.feature.permit.application.mammal.period;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.permit.application.mammal.MammalPermitApplication;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.Arrays;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_OTTER;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MammalPermitApplicationSpeciesPeriodInformationDTOTest {


    @Test
    public void testSmoke_updatePermitPeriod_carnivoreValidPeriod() {

        ImmutableList.<Integer>builder()
                .addAll(GameSpecies.LARGE_CARNIVORES)
                .add(OFFICIAL_CODE_OTTER).build()
                .forEach(code -> {

                    final MammalPermitApplicationSpeciesPeriodDTO speciesDTO =
                            new MammalPermitApplicationSpeciesPeriodDTO();

                    // Maximum period for carnivores + otter is 21 days
                    final LocalDate beginDate = new LocalDate(2019, 9, 1);
                    final LocalDate endDate = new LocalDate(2019, 9, 21);
                    speciesDTO.setBeginDate(beginDate);
                    speciesDTO.setEndDate(endDate);
                    speciesDTO.setGameSpeciesCode(code);
                    speciesDTO.setAdditionalPeriodInfo("Additional information");

                    final MammalPermitApplicationSpeciesPeriodInformationDTO dto =
                            new MammalPermitApplicationSpeciesPeriodInformationDTO(
                                    ImmutableList.of(speciesDTO),
                                    1,
                                    null,
                                    null,
                                    null);

                    assertTrue("Should be valid for " + code, speciesDTO.isValidPeriod());
                    assertTrue(dto.isOneYearOrDoesNotContainRestrictedSpecies());
                });

    }

    @Test
    public void testSmoke_updatePermitPeriod_otherSpeciesValidPeriod() {
        Arrays.stream(GameSpecies.ALL_GAME_SPECIES_CODES)
                .filter(code -> !GameSpecies.isLargeCarnivore(code))
                .filter(code -> code != OFFICIAL_CODE_OTTER)
                .forEach(code -> {

                    final MammalPermitApplicationSpeciesPeriodDTO speciesDTO =
                            new MammalPermitApplicationSpeciesPeriodDTO();

                    final LocalDate beginDate = new LocalDate(2019, 9, 2);
                    final LocalDate endDate = new LocalDate(2020, 9, 1);
                    speciesDTO.setBeginDate(beginDate);
                    speciesDTO.setEndDate(endDate);
                    speciesDTO.setGameSpeciesCode(code);
                    speciesDTO.setAdditionalPeriodInfo("Additional information");


                    final MammalPermitApplicationSpeciesPeriodInformationDTO dto =
                            new MammalPermitApplicationSpeciesPeriodInformationDTO(
                                    ImmutableList.of(speciesDTO),
                                    1,
                                    null,
                                    null,
                                    null);
                    assertTrue("Should be valid for " + code, speciesDTO.isValidPeriod());
                    assertTrue(dto.isOneYearOrDoesNotContainRestrictedSpecies());
                });
    }

    @Test
    public void testSmoke_updatePermitPeriod_carnivorePeriodExceeded() {

        ImmutableList.<Integer>builder()
                .addAll(GameSpecies.LARGE_CARNIVORES)
                .add(OFFICIAL_CODE_OTTER).build()
                .forEach(code -> {

                    final MammalPermitApplicationSpeciesPeriodDTO speciesDTO =
                            new MammalPermitApplicationSpeciesPeriodDTO();

                    // Maximum period for carnivores + otter is 21 days
                    final LocalDate beginDate = new LocalDate(2019, 9, 1);
                    final LocalDate endDate = new LocalDate(2019, 9, 22);
                    speciesDTO.setBeginDate(beginDate);
                    speciesDTO.setEndDate(endDate);
                    speciesDTO.setGameSpeciesCode(code);
                    speciesDTO.setAdditionalPeriodInfo("Additional information");

                    final MammalPermitApplicationSpeciesPeriodInformationDTO dto =
                            new MammalPermitApplicationSpeciesPeriodInformationDTO(
                                    ImmutableList.of(speciesDTO),
                                    1,
                                    null,
                                    null,
                                    null);

                    assertFalse("Should not be valid for " + code, speciesDTO.isValidPeriod());
                    assertTrue(dto.isOneYearOrDoesNotContainRestrictedSpecies());
                });

    }


    @Test
    public void testSmoke_updatePermitPeriod_multipleYearsForCarnivore() {

        ImmutableList.<Integer>builder()
                .addAll(GameSpecies.LARGE_CARNIVORES)
                .add(OFFICIAL_CODE_OTTER).build()
                .forEach(code -> {

                    final MammalPermitApplicationSpeciesPeriodDTO speciesDTO =
                            new MammalPermitApplicationSpeciesPeriodDTO();

                    final LocalDate beginDate = new LocalDate(2019, 9, 1);
                    final LocalDate endDate = new LocalDate(2019, 9, 10);
                    speciesDTO.setBeginDate(beginDate);
                    speciesDTO.setEndDate(endDate);
                    speciesDTO.setGameSpeciesCode(code);
                    speciesDTO.setAdditionalPeriodInfo("Additional information");

                    final MammalPermitApplicationSpeciesPeriodInformationDTO dto =
                            new MammalPermitApplicationSpeciesPeriodInformationDTO(
                                    ImmutableList.of(speciesDTO),
                                    3,
                                    null,
                                    null,
                                    null);

                    assertTrue(speciesDTO.isValidPeriod());
                    assertFalse(dto.isOneYearOrDoesNotContainRestrictedSpecies());
                });


    }


    @Test
    public void testSmoke_updatePermitPeriod_otherSpeciesPeriodExceeded() {
        Arrays.stream(GameSpecies.ALL_GAME_SPECIES_CODES)
                .filter(code -> !GameSpecies.isLargeCarnivore(code))
                .filter(code -> code != OFFICIAL_CODE_OTTER)
                .forEach(code -> {

                    final MammalPermitApplicationSpeciesPeriodDTO speciesDTO =
                            new MammalPermitApplicationSpeciesPeriodDTO();

                    final LocalDate beginDate = new LocalDate(2019, 9, 1);
                    final LocalDate endDate = new LocalDate(2020, 9, 1);
                    speciesDTO.setBeginDate(beginDate);
                    speciesDTO.setEndDate(endDate);
                    speciesDTO.setGameSpeciesCode(code);
                    speciesDTO.setAdditionalPeriodInfo("Additional information");


                    final MammalPermitApplicationSpeciesPeriodInformationDTO dto =
                            new MammalPermitApplicationSpeciesPeriodInformationDTO(
                                    ImmutableList.of(speciesDTO),
                                    1,
                                    null,
                                    null,
                                    null);
                    assertFalse(speciesDTO.isValidPeriod());
                    assertTrue(dto.isOneYearOrDoesNotContainRestrictedSpecies());
                });
    }

    @Test
    public void testSmoke_updatePermitPeriod_otherSpeciesMultiYearNoGrounds() {
        Arrays.stream(GameSpecies.ALL_GAME_SPECIES_CODES)
                .filter(code -> !GameSpecies.isLargeCarnivore(code))
                .filter(code -> code != OFFICIAL_CODE_OTTER)
                .forEach(code -> {

                    final MammalPermitApplicationSpeciesPeriodDTO speciesDTO =
                            new MammalPermitApplicationSpeciesPeriodDTO();

                    final LocalDate beginDate = new LocalDate(2019, 9, 1);
                    final LocalDate endDate = new LocalDate(2019, 12, 1);
                    speciesDTO.setBeginDate(beginDate);
                    speciesDTO.setEndDate(endDate);
                    speciesDTO.setGameSpeciesCode(code);
                    speciesDTO.setAdditionalPeriodInfo("Additional information");


                    final MammalPermitApplicationSpeciesPeriodInformationDTO dto =
                            new MammalPermitApplicationSpeciesPeriodInformationDTO(
                                    ImmutableList.of(speciesDTO),
                                    3,
                                    null,
                                    null,
                                    null);
                    assertTrue(speciesDTO.isValidPeriod());
                    assertFalse(dto.isOneYearOrHasExtendedPeriodGrounds());
                });
    }


    @Test
    public void testSmoke_updatePermitPeriod_otherSpecieseMultiYearValidGrounds() {
        Arrays.stream(GameSpecies.ALL_GAME_SPECIES_CODES)
                .filter(code -> !GameSpecies.isLargeCarnivore(code))
                .filter(code -> code != OFFICIAL_CODE_OTTER)
                .forEach(code -> {

                    final MammalPermitApplicationSpeciesPeriodDTO speciesDTO =
                            new MammalPermitApplicationSpeciesPeriodDTO();

                    final LocalDate beginDate = new LocalDate(2019, 9, 1);
                    final LocalDate endDate = new LocalDate(2019, 12, 1);
                    speciesDTO.setBeginDate(beginDate);
                    speciesDTO.setEndDate(endDate);
                    speciesDTO.setGameSpeciesCode(code);
                    speciesDTO.setAdditionalPeriodInfo("Additional information");


                    final MammalPermitApplicationSpeciesPeriodInformationDTO dto =
                            new MammalPermitApplicationSpeciesPeriodInformationDTO(
                                    ImmutableList.of(speciesDTO),
                                    3,
                                    MammalPermitApplication.ExtendedPeriodGrounds.PERMANENT_ESTABLISHMENT,
                                    null,
                                    "Area");
                    assertTrue(speciesDTO.isValidPeriod());
                    assertTrue(dto.isOneYearOrHasExtendedPeriodGrounds());
                });
    }
}

package fi.riista.feature.permit.application.nestremoval.period;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.gamediary.GameSpecies;
import org.joda.time.LocalDate;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class NestRemovalPermitApplicationSpeciesPeriodInformationDTOTest {

    @Test
    public void testSmoke_updatePermitPeriod() {

        ImmutableList.<Integer>builder()
                .addAll(GameSpecies.NEST_REMOVAL_PERMIT_SPECIES)
                .build()
                .forEach(code -> {

                    final NestRemovalPermitApplicationSpeciesPeriodDTO speciesDTO =
                            new NestRemovalPermitApplicationSpeciesPeriodDTO();

                    final LocalDate beginDate = new LocalDate(2019, 1, 1);
                    final LocalDate endDate = new LocalDate(2019, 12, 31);
                    speciesDTO.setBeginDate(beginDate);
                    speciesDTO.setEndDate(endDate);
                    speciesDTO.setGameSpeciesCode(code);

                    final NestRemovalPermitApplicationSpeciesPeriodInformationDTO dto =
                            new NestRemovalPermitApplicationSpeciesPeriodInformationDTO(ImmutableList.of(speciesDTO));

                    assertTrue("Should be valid for " + code, speciesDTO.isValidPeriod());
                });

    }
}

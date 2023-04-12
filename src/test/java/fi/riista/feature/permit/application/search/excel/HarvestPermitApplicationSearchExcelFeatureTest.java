package fi.riista.feature.permit.application.search.excel;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Map;

import static fi.riista.feature.harvestpermit.HarvestPermitCategory.BIRD;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class HarvestPermitApplicationSearchExcelFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestPermitApplicationSearchExcelFeature feature;

    @Test
    public void testCollectGrantedAmountsByApplications() {
        withRhy(rhy -> {
            final GameSpecies magpie = model().newGameSpecies(37122);
            final GameSpecies fieldFare = model().newGameSpecies(33117);
            final HarvestPermitApplication application = model().newHarvestPermitApplication(rhy, null, emptyList());
            application.setHarvestPermitCategory(BIRD);

            final HarvestPermitApplicationSpeciesAmount spa1 = model().newHarvestPermitApplicationSpeciesAmount(application, magpie);
            spa1.setValidityYears(0);

            final HarvestPermitApplicationSpeciesAmount spa2 = model().newHarvestPermitApplicationSpeciesAmount(application, fieldFare);
            spa2.setValidityYears(0);

            application.setSpeciesAmounts(Arrays.asList(spa1, spa2));

            final PermitDecision decision = model().newPermitDecision(application);
            decision.setValidityYears(1);

            final PermitDecisionSpeciesAmount amount1 = model().newPermitDecisionSpeciesAmount(decision, magpie, 10);
            amount1.setBeginDate(new LocalDate(2022, 4, 13));
            amount1.setEndDate(new LocalDate(2022, 4, 23));

            final PermitDecisionSpeciesAmount amount2 = model().newPermitDecisionSpeciesAmount(decision, fieldFare, 20);
            amount2.setBeginDate(new LocalDate(2023, 4, 13));
            amount2.setEndDate(new LocalDate(2023, 4, 23));

            onSavedAndAuthenticated(createNewUser(), () -> {
                Map<Long, Map<Integer, Map<Integer, ApplicationSearchDecisionSpeciesAmountDTO>>> result =
                        feature.collectGrantedAmountsByDecisions(singletonList(decision), null);
                assertThat(result, is(not(nullValue())));
                assertThat(result.keySet(), hasSize(1));

                final Map<Integer, Map<Integer, ApplicationSearchDecisionSpeciesAmountDTO>> speciesResult = result.get(application.getId());
                assertThat(speciesResult, is(not(nullValue())));
                assertThat(speciesResult.keySet(), hasSize(2));

                assertDecisionSpecies(speciesResult, magpie, 2022, amount1);

                assertDecisionSpecies(speciesResult, fieldFare, 2023, amount2);
            });
        });
    }

    private void assertDecisionSpecies(final Map<Integer, Map<Integer, ApplicationSearchDecisionSpeciesAmountDTO>> speciesMap,
                                       final GameSpecies species,
                                       final int year,
                                       final PermitDecisionSpeciesAmount expected) {
        final Map<Integer, ApplicationSearchDecisionSpeciesAmountDTO> decisionAmountsByYear = speciesMap.get(species.getOfficialCode());
        assertThat(decisionAmountsByYear, is(not(nullValue())));
        assertThat(decisionAmountsByYear.keySet(), hasSize(1));

        final ApplicationSearchDecisionSpeciesAmountDTO decisionAmounts = decisionAmountsByYear.get(year);
        assertThat(decisionAmounts, is(not(nullValue())));
        assertThat(decisionAmounts.getSpecimenAmount(), is(equalTo(expected.getSpecimenAmount())));
        assertThat(decisionAmounts.getBeginDate(), is(equalTo(expected.getBeginDate())));
        assertThat(decisionAmounts.getEndDate(), is(equalTo(expected.getEndDate())));
    }
}

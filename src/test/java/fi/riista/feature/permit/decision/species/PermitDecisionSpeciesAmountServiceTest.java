package fi.riista.feature.permit.decision.species;

import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.test.DefaultEntitySupplierProvider;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PermitDecisionSpeciesAmountServiceTest implements DefaultEntitySupplierProvider {

    private EntitySupplier model() {
        return getEntitySupplier();
    }

    @Test
    public void testSmoke_Mooselike() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HarvestPermitApplication application =
                model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.MOOSELIKE);
        application.setApplicationYear(2018);

        final PermitDecisionSpeciesAmountService.Generator generator =
                PermitDecisionSpeciesAmountService.createGenerator(PermitDecision.createForApplication(application));

        final GameSpecies moose = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_MOOSE);
        final GameSpecies deer = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER);

        final HarvestPermitApplicationSpeciesAmount a1 = model().newHarvestPermitApplicationSpeciesAmount(application, moose);
        final HarvestPermitApplicationSpeciesAmount a2 = model().newHarvestPermitApplicationSpeciesAmount(application, deer);
        application.getSpeciesAmounts().add(a1);
        application.getSpeciesAmounts().add(a2);

        final List<PermitDecisionSpeciesAmount> decisionSpeciesAmounts = generator.createAllForMooselike();
        assertEquals(2, decisionSpeciesAmounts.size());

        final PermitDecisionSpeciesAmount b1 = decisionSpeciesAmounts.stream()
                .filter(s -> s.getGameSpecies().getOfficialCode() == a1.getGameSpecies().getOfficialCode())
                .findFirst().orElseThrow(RuntimeException::new);

        final PermitDecisionSpeciesAmount b2 = decisionSpeciesAmounts.stream()
                .filter(s -> s.getGameSpecies().getOfficialCode() == a2.getGameSpecies().getOfficialCode())
                .findFirst().orElseThrow(RuntimeException::new);

        assertEquals(a1.getAmount(), b1.getAmount(), 0.01);
        assertEquals(a2.getAmount(), b2.getAmount(), 0.01);

        assertEquals(new LocalDate(2018, 9, 1), b1.getBeginDate());
        assertEquals(new LocalDate(2018, 9, 1), b2.getBeginDate());

        assertEquals(new LocalDate(2019, 1, 15), b1.getEndDate());
        assertEquals(new LocalDate(2019, 1, 15), b2.getEndDate());

        assertNull(b1.getBeginDate2());
        assertNull(b2.getBeginDate2());
        assertNull(b1.getEndDate2());
        assertNull(b2.getEndDate2());
    }

    @Test
    public void testSmoke_Birdlike() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HarvestPermitApplication application =
                model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.BIRD);
        application.setApplicationYear(2018);
        application.setSubmitDate(new LocalDate(2018, 1, 5).toDateTimeAtCurrentTime());

        final GameSpecies bird1 = model().newGameSpecies(26287);
        final GameSpecies bird2 = model().newGameSpecies(26291);
        final GameSpecies bird3 = model().newGameSpecies(26298);

        final HarvestPermitApplicationSpeciesAmount a1 = model().newHarvestPermitApplicationSpeciesAmount(application, bird1);
        final HarvestPermitApplicationSpeciesAmount a2 = model().newHarvestPermitApplicationSpeciesAmount(application, bird2);
        final HarvestPermitApplicationSpeciesAmount a3 = model().newHarvestPermitApplicationSpeciesAmount(application, bird3);
        application.getSpeciesAmounts().add(a1);
        application.getSpeciesAmounts().add(a2);
        application.getSpeciesAmounts().add(a3);

        a1.setValidityYears(1);
        a2.setValidityYears(2);
        a3.setValidityYears(0);

        a1.setBeginDate(new LocalDate(2018, 2, 18));
        a2.setBeginDate(new LocalDate(2018, 1, 1));
        a3.setBeginDate(new LocalDate(2018, 2, 28));

        a1.setEndDate(new LocalDate(2018, 12, 31));
        a2.setEndDate(new LocalDate(2018, 12, 1));
        a3.setEndDate(new LocalDate(2018, 12, 1));

        final PermitDecisionSpeciesAmountService.Generator generator =
                PermitDecisionSpeciesAmountService.createGenerator(PermitDecision.createForApplication(application));

        final List<PermitDecisionSpeciesAmount> decisionSpeciesAmounts = generator.createAllForBird();
        assertEquals(4, decisionSpeciesAmounts.size());

        final PermitDecisionSpeciesAmount b1 = decisionSpeciesAmounts.stream()
                .filter(s -> s.getGameSpecies().getOfficialCode() == a1.getGameSpecies().getOfficialCode())
                .findFirst().orElseThrow(RuntimeException::new);

        final PermitDecisionSpeciesAmount b2_y1 = decisionSpeciesAmounts.stream()
                .filter(s -> s.getBeginDate().getYear() == 2018)
                .filter(s -> s.getGameSpecies().getOfficialCode() == a2.getGameSpecies().getOfficialCode())
                .findFirst().orElseThrow(RuntimeException::new);

        final PermitDecisionSpeciesAmount b2_y2 = decisionSpeciesAmounts.stream()
                .filter(s -> s.getBeginDate().getYear() == 2019)
                .filter(s -> s.getGameSpecies().getOfficialCode() == a2.getGameSpecies().getOfficialCode())
                .findFirst().orElseThrow(RuntimeException::new);

        final PermitDecisionSpeciesAmount b3 = decisionSpeciesAmounts.stream()
                .filter(s -> s.getGameSpecies().getOfficialCode() == a3.getGameSpecies().getOfficialCode())
                .findFirst().orElseThrow(RuntimeException::new);

        assertEquals(a1.getAmount(), b1.getAmount(), 0.01);
        assertEquals(a2.getAmount(), b2_y1.getAmount(), 0.01);
        assertEquals(a2.getAmount(), b2_y2.getAmount(), 0.01);
        assertEquals(a3.getAmount(), b3.getAmount(), 0.01);

        // application was sent after beginDate -> modify first validity year
        assertEquals(a1.getBeginDate(), b1.getBeginDate());
        assertEquals(new LocalDate(2018, 1, 5), b2_y1.getBeginDate());
        assertEquals(new LocalDate(2019, 1, 1), b2_y2.getBeginDate());
        assertEquals(a3.getBeginDate(), b3.getBeginDate());

        assertEquals(a1.getEndDate(), b1.getEndDate());
        assertEquals(a2.getEndDate(), b2_y1.getEndDate());
        assertEquals(a2.getEndDate().plusYears(1), b2_y2.getEndDate());
        assertEquals(a3.getEndDate(), b3.getEndDate());

        assertNull(b1.getBeginDate2());
        assertNull(b2_y1.getBeginDate2());
        assertNull(b2_y2.getBeginDate2());
        assertNull(b3.getBeginDate2());

        assertNull(b1.getEndDate2());
        assertNull(b2_y1.getEndDate2());
        assertNull(b2_y2.getEndDate2());
        assertNull(b3.getEndDate2());
    }
}

package fi.riista.feature.permit.application.amend;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameCategory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.AmendApplicationFeature;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAmendDTO;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.application.bird.BirdPermitApplication;
import fi.riista.feature.permit.application.bird.cause.BirdPermitApplicationCause;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;

public class AmendApplicationFeatureTest extends EmbeddedDatabaseTest {

    private Riistanhoitoyhdistys rhy;
    private GameSpecies unprotected;

    private HarvestPermitApplication application;

    private BirdPermitApplication birdApplication;
    private SystemUser moderator;

    @Resource
    private AmendApplicationFeature amendApplicationFeature;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Before
    public void setup() {
        moderator = createNewModerator();
        rhy = model().newRiistanhoitoyhdistys();
        unprotected = model().newGameSpecies(456, GameCategory.UNPROTECTED, "naakka", "naakka", "naakka");
        application = model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.BIRD);
        final HarvestPermitApplicationSpeciesAmount speciesAmount =
                model().newHarvestPermitApplicationSpeciesAmount(application, unprotected, 30.0f);
        speciesAmount.setBeginDate(DateUtil.today());
        speciesAmount.setEndDate(DateUtil.today());
        speciesAmount.setValidityYears(1);
        speciesAmount.setCausedDamageAmount(3000);
        speciesAmount.setCausedDamageDescription("Vahingon kuvaus");
        speciesAmount.setEvictionMeasureDescription("Kartoitustoimet");
        speciesAmount.setEvictionMeasureEffect("Karkoitustoimien vaikutukset");
        speciesAmount.setPopulationAmount("Noin 200");
        speciesAmount.setPopulationDescription("Kuvaus kannan tilasta");
        application.setSpeciesAmounts(ImmutableList.of(speciesAmount));
        birdApplication = model().newBirdPermitApplication(application);
        final BirdPermitApplicationCause cause = new BirdPermitApplicationCause();
        cause.setCauseCropsDamage(true);
        birdApplication.setCause(cause);
        application.setAttachments(ImmutableList.of(model().newHarvestPermitApplicationAttachment(application)));
    }

    @Test
    public void testAmend_changeYear() {
        application.setStatus(HarvestPermitApplication.Status.ACTIVE);
        final PermitDecision decision = model().newPermitDecision(application);
        decision.setStatusDraft();
        persistInNewTransaction();

        setSpeciesPeriod(
                new LocalDate(2018, 5, 1),
                new LocalDate(2018, 7, 1));

        onSavedAndAuthenticated(moderator, () -> {
            amendApplicationFeature.startAmendApplication(application.getId());
        });

        setSpeciesPeriod(
                new LocalDate(2019, 5, 1),
                new LocalDate(2019, 7, 1));
        persistInNewTransaction();

        onSavedAndAuthenticated(moderator, () -> {
            final HarvestPermitApplicationAmendDTO dto = new HarvestPermitApplicationAmendDTO();
            dto.setId(application.getId());
            dto.setChangeReason("test");
            dto.setSubmitDate(new LocalDate(2019, 2, 2));
            amendApplicationFeature.stopAmendApplication(dto);
        });

        assertEquals(2019, decision.getDecisionYear());
    }

    private void setSpeciesPeriod(final LocalDate begin, final LocalDate end) {
        runInTransaction(() -> {
            harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application)
                    .forEach(spa -> {
                        spa.setBeginDate(begin);
                        spa.setEndDate(end);
                        harvestPermitApplicationSpeciesAmountRepository.save(spa);
                    });

            harvestPermitApplicationSpeciesAmountRepository.flush();
        });
    }

}

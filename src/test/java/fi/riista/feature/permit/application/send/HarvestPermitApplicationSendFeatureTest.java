package fi.riista.feature.permit.application.send;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationRepository;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.bird.BirdPermitApplication;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.MockTimeProvider;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HarvestPermitApplicationSendFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestPermitApplicationSendFeature feature;

    @Resource
    private HarvestPermitApplicationRepository repository;

    private Riistanhoitoyhdistys rhy;
    private GameSpecies fowl;
    private HarvestPermitApplication application;

    @Before
    public void setup() {
        rhy = model().newRiistanhoitoyhdistys();
        fowl = model().newGameSpecies();
        application = model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.BIRD);
        final HarvestPermitApplicationSpeciesAmount speciesAmount =
                model().newHarvestPermitApplicationSpeciesAmount(application, fowl, 30.0f);
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
        final BirdPermitApplication birdApplication = model().newBirdPermitApplication(application);
        birdApplication.getCause().setCauseAviationSafety(true);
        final HarvestPermitApplicationAttachment protectedArea = model().newHarvestPermitApplicationAttachment(application);
        protectedArea.setAttachmentType(HarvestPermitApplicationAttachment.Type.PROTECTED_AREA);
        application.setAttachments(singletonList(protectedArea));
    }

    @After
    public void tearDown() {
        MockTimeProvider.resetMock();
    }

    @Test
    public void testApplicationYearIsDeterminedBySubmitDate_sentByUser() {

        final LocalDate applicationCreateTime = new LocalDate(2018, 12, 15);
        MockTimeProvider.mockTime(applicationCreateTime.toDate().getTime());
        application.setStatus(HarvestPermitApplication.Status.DRAFT);
        application.setSubmitDate(null);
        application.setApplicationNumber(null);
        application.setDeliveryByMail(false);
        application.setApplicationYear(2018);
        application.setId(432L);

        MockTimeProvider.mockTime(applicationCreateTime.plusMonths(1).toDate().getTime());
        onSavedAndAuthenticated(createNewUser("test", application.getContactPerson()), () -> {
            final HarvestPermitApplicationSendDTO dto = new HarvestPermitApplicationSendDTO();
            dto.setId(application.getId());
            feature.sendApplication(dto);
        });

        runInTransaction(() -> {
            final HarvestPermitApplication persisted = repository.findById(application.getId()).get();
            assertNotNull(persisted);
            assertEquals(2019, persisted.getApplicationYear());
        });
    }
}
